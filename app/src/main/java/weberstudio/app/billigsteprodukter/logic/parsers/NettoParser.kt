package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.Point
import android.graphics.PointF
import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.logic.FuzzyMatcher
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt

object NettoParser : StoreParser {
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()

    override fun parse(receipt: Text): HashSet<Product> {
        val products: HashSet<Product> = HashSet<Product>()
        val parsedLines = processImageText(receipt)

        val angleTolerance = Math.toRadians(10.0).toFloat()  //A tolerance for ray hit

        val parsedProducts = ArrayList<ParsedProduct>()
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            //Kigger kun på de linjer hvor deres centrum er verticalTolerance pixels væk fra hinanden, på den måde slipper vi for alt for mange checks
            val corners = lineA.corners
            val lineHeight = ((euclidDistance(corners[0], corners[3]) + euclidDistance(corners[1], corners[2]))/2f)
            val verticalBounds = lineHeight * 0.5f //Halvdelen af linjens højde

            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                //Skipper hvis de ikke er inde for tolerancen
                val angleDifference = angleDifferenceModulosPi(lineA.angle, lineB.angle)
                if (angleDifference > angleTolerance) continue

                //Skipper hvis de ikke er inde for samme "Row" i kvitteringen
                val dx = lineB.center.x - lineA.center.x
                val dy = lineB.center.y - lineA.center.y
                val upX = -lineA.direction.y
                val upY = lineA.direction.x
                val rowOffset = abs(upX * dx + upY * dy)
                if (rowOffset > verticalBounds) continue

                //Accepterer kun "hits" hvor linje B er "hen ad" linje A. På den måde kan vi checke kvitteringen korrekt ind selvom billedet bliver taget 180 grader
                val forwardDotProduct = lineA.direction.x * dx + lineA.direction.y * dy
                val buffer = 0.1f //Allows for a tiny slope
                if (forwardDotProduct < lineHeight * buffer) continue

                //Parser til produkt så længe at raycast hit er langt nok væk for at det faktisk kan være en pris og ikke noise
                val minNameToPriceOffset = lineHeight * 3.0f //How long away the price raycast hit needs to be to be accounted for. Lower = More forgiving, Higher = Less Forgiving. FX: 0.8 = "Atleast 0.8x the line height away"
                if (forwardDotProduct < minNameToPriceOffset) continue

                //Finder den korrekte produktpris
                val productName = lineA.text
                val productPrice: Float

                try {
                    productPrice = lineB.text.toFloat()
                } catch (_: NumberFormatException) {
                    println("Couldn't convert ${lineB.text} to Float!")
                    continue
                }


                //Hvis det er en "3 x 9.95" parser vi den sidste linjes navn
                if (isQuantityLine(productName)) {
                    //Finder den linje som er lige over "RABAT" og snupper navnet fra den
                    val parentLine = parsedLines.filter { otherLine ->
                        otherLine !== lineA &&
                        otherLine.center.y < lineA.center.y &&
                        abs(otherLine.center.x - lineA.center.x) < 80f //Skal være inde for en tolerance (floaten) af x-kordinat før vi overhovedet kigger på den
                    } .maxByOrNull { it.center.y }
                    if (parentLine == null) println("Couldn't access the previous product from product $productName!")
                    else {
                        try {
                            //If we successfully got the actual product we save that instead of the original "2 x 25,5", and divide the price of the product by the amount
                            val productPrice = normalizeText(lineB.text).toFloat()/normalizeText(lineA.text[0].toString()).toFloat()
                            parsedProducts.add(ParsedProduct(parentLine.text, productPrice))
                            break //Success, so we break out of inner loop
                        } catch (_: NumberFormatException) {
                            println("Error dividing the product price with the amount!")
                        }
                    }
                }

                /*
                //Hvis det er en "RABAT" så opdaterer vi lige prisen på det sidste parsed produkt
                if (fuzzyMatcher.match(productName, listOf("RABAT"), 0.85f, 0.15f)) {
                    parsedProducts.last().price -= productPrice
                }
                 */

                //Hvis det er en "Rabat" så trækker vi det lige fra det sidste produkt
                parsedProducts.add(ParsedProduct(productName, productPrice))
                break //Success, so we break out of inner loop
            }
        }

        //Omdanner de parsedProducts om til Products
        for (parsedProduct in parsedProducts) {
            products.add(Product(parsedProduct.name, parsedProduct.price, Store.Netto))
        }

        if (products.isEmpty()) {
            throw ParsingException("Productlist is empty!")
        }

        //region Filtering and returning
        //Hvis der er mere end to produkter (så ét produkt og ét stopord), så gemmer vi alle dem som har den samme pris som stop ordene (Så hvis "Total" fucker f.eks.)
        val stopWordPrices = if (products.size > 2) {
            products
                .filter { isStopWord(it.name) }
                .map { it.price }
                .toSet()
        } else {
            emptySet()
        }

        //Returner kun produkter som ikke er stop ordet, eller som har den samme pris som stop ordet (så hvis "Total" fucker f.eks.)
        val filteredList = products.filter { product -> !isStopWord(product.name) && product.price !in stopWordPrices && !fuzzyMatcher.match(product.name, listOf("RABAT"), 0.8f, 0.2f) }.toHashSet()

        return filteredList
        //endregion
    }

    /**
     * Processes the text from the image into [ParsedLine]'s
     */
    private fun processImageText(text: Text): List<ParsedLine> {
        val allLines = mutableListOf<ParsedLine>()

        //Runs through each line and processes it into a **ParsedLine** by extracting its values
        for (block in text.textBlocks) {
            for (line in block.lines) {
                val corners = line.cornerPoints ?: continue

                //Angle
                val p0 = corners[0]
                val p1 = corners[1]
                val angle = calculateAngle(p0, p1)

                //Direction
                val dx = (p1.x - p0.x).toFloat()
                val dy = (p1.y - p0.y).toFloat()
                val len = sqrt(dx*dx + dy*dy)
                val direction = PointF(dx/len, dy/len)

                //Center
                val center = PointF(
                    corners.map { it.x }.average().toFloat(),
                    corners.map { it.y }.average().toFloat()
                )

                allLines.add(ParsedLine(text = normalizeText(line.text), angle = angle, center = center, corners = corners, direction = direction))
            }
        }
        return allLines
    }

    private fun calculateAngle(p0: Point, p1: Point): Float {
        val dx = (p1.x - p0.x).toFloat()
        val dy = (p1.y - p0.y).toFloat()
        return atan2(dy, dx) //Angle in radians
    }

    /**
     * Normalizes the text to uppercase + no danish
     */
    private fun normalizeText(text: String): String {
            //TODO: Det her skal standardiseres igennem hele codebasen og ændres til Compose best practice
            return text
                .replace(Regex("[^A-Za-z0-9 ,.]"), "") //Limits to a-z, digits and whitespaces
                .uppercase()
                .replace("Æ", "AE")
                .replace("Ø", "OE")
                .replace("Å", "AA")
                .replace(",", ".") //Ændrer "12,50" til "12.50" så vi kan parse korrekt senere
                .trim()
        }

    /**
     * Checks and see if word given as argument is a stop word using fuzzy search
     */
    private fun isStopWord(input: String): Boolean {
        val stopWords = listOf("TOTAL", "BETALINGSKORT", "MOMS UDGØR")

        return stopWords.any { stopWord ->
            val jaroSimilarity = fuzzyMatcherJaro.apply(input, stopWord) ?: 0.0
            if (jaroSimilarity >= 0.80) {
                return true
            } //Higher = Fewer false positive

            val levenSimilarity = fuzzyMatcherLeven.apply(input, stopWord)
            if (levenSimilarity <= 2) {
                return true
            } //LMAO good luck adjusting lel

            false
        }
    }
    /**
     * Øhhhh, såe hvis useren vender kamerat 180 grader og tager billedet så sørger denne her for vi stadig korrekt kan parse produkterne
     */
    private fun angleDifferenceModulosPi(a: Float, b: Float): Float {
        val raw = abs(a - b) % Math.PI.toFloat()
        return min(raw, Math.PI.toFloat() - raw)
    }
    private fun euclidDistance(p1: Point, p2: Point): Float {
        val dx = (p2.x - p1.x).toFloat()
        val dy = (p2.y - p1.y).toFloat()
        return hypot(dx, dy) // same as sqrt(dx*dx + dy*dy)
    }
    /**
     * Checks whether the given string is reminiscent of a quantity line, aka a line like "2 x 14,00"
     */
    private fun isQuantityLine(line: String): Boolean {
        //Snupper alle numberTokens (digits) og checker der mindst to (en mængde og en pris pr. enhed)
        val numberToken = Regex("""\d+[.,]?\d*""")
        val rawTokens = numberToken.findAll(line).map { it.value }.toList()
        if (rawTokens.size < 2) return false

        //Finder linjers position så vi kan se på teksten imellem dem (som helst skal være " x "
        val amount = rawTokens[0]
        val pricePerUnit = rawTokens[1]
        val amountIndex = line.indexOf(amount).takeIf { it >= 0 } ?: return false
        val pricePerUnitIndex = line.indexOf(pricePerUnit, startIndex = amountIndex + amount.length).takeIf { it >= 0 } ?: return false

        //Checker om nogle karakterene er den seperator vi ønsker
        val textSeperator = line.substring(amountIndex + amount.length, pricePerUnitIndex)
        return textSeperator.any() { ch ->
            ch.equals('x', ignoreCase = true) ||
                    ch == '*' ||
                    ch == 'x'
            //Add flere her hvis er
        }
    }

    override fun toString(): String {
        return "Netto"
    }

    data class ParsedLine(
        val text: String,
        val angle: Float,
        val center: PointF,
        val direction: PointF,
        val corners: Array<Point>
    )
    data class ParsedProduct(val name: String, var price: Float)
}

