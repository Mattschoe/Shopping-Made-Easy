package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.logic.FuzzyMatcher
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedLine
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedProduct
import kotlin.math.sqrt

object NettoParser : StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()
    //endregion

    override fun parse(receipt: Text): HashSet<Product> {
        val includeRABAT = false
        val parsedLines = processImageText(receipt)
        val parsedProducts = ArrayList<ParsedProduct>()
        val parseAfterControlLineFound = ArrayList<Pair<ParsedLine, ParsedLine>>() //Line combos vi parser senere efter at have fundet controllinjen

        //region PARSES LINES INTO PRODUCTS
        var controlLine: ParsedLine? = null
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                if (doesLinesCollide(lineA, lineB)) {
                    //Tries to get a controlLine
                    if (controlLine == null && (fuzzyMatcher.match(lineA.text, listOf("NETTO"), 0.85f, 0.15f) || isStopWord(lineA.text))) controlLine = lineA
                    if (controlLine == null && (fuzzyMatcher.match(lineB.text, listOf("NETTO"), 0.85f, 0.15f) || isStopWord(lineB.text))) controlLine = lineB

                    //Enten parser produkterne, eller gemmer parsningen til efter vi har fundet kontrollinjen.
                    if (controlLine == null) parseAfterControlLineFound.add(Pair(lineA, lineB))
                    else parsedProducts.add(parseLinesToProduct(lineA, lineB, controlLine, includeRABAT, parsedLines, parsedProducts))
                }
            }
        }
        if (controlLine == null) throw ParsingException("No control line found!")

        //Parser de linjecomboer vi missede fordi kontrollinjen endnu ik var fundet
        parseAfterControlLineFound.forEach { pair -> parsedProducts.add(parseLinesToProduct(pair.first, pair.second, controlLine, includeRABAT, parsedLines, parsedProducts)) }
        //endregion

        return parsedProductsToFilteredProductList(parsedProducts)
    }

    /**
     * Parses lines to product
     * @param lineA product name
     * @param lineB product price
     * @param controlLine the line that tells the function where either up or down is. Normally either the storelogo name or the "TOTAL"/"BETALINGSKORT" line
     * @param includeRABAT should the "Rabat" lines be calculated and deducted?
     * @param parsedLines the lines parsed so far. Used to find the line above a quantity line
     * @param parsedProducts the products parsed so far. Used if *includeRABAT* is true to deduct the discount
     */
    private fun parseLinesToProduct(lineA: ParsedLine, lineB: ParsedLine, controlLine: ParsedLine, includeRABAT: Boolean, parsedLines: List<ParsedLine>, parsedProducts: List<ParsedProduct>): ParsedProduct {
        //Snupper navn og pris
        val productName = lineA.text
        val productPrice: Float

        //Prøver at converte prisen til Float
        try {
            productPrice = lineB.text.toFloat()
        } catch (_: NumberFormatException) {
            //TODO: Det her skal give et ("!") ude på UI'en for useren
            Log.d("ERROR", "Couldn't convert ${lineB.text} to Float!")
            return ParsedProduct(productName, 0.0f)
        }

        //region QUANTITY LINE
        // Hvis det er en "3 x 9.95" parser vi den sidste linjes navn
        if (isQuantityLine(productName)) {
            //Finder den linje som er lige over "RABAT" og snupper navnet fra den
            val parentLine = findLineAboveUsingReference(parsedLines, lineA, controlLine) //OBS: BLACK MAGIC FUCKERY
            if (parentLine == null) {
                //TODO: Det her skal give et ("!") ude på UI'en for useren
                Log.d("ERROR", "Couldn't access the previous product from product $productName!");
                return ParsedProduct(productName, productPrice) //Returnerer bare quantity line så vi kan se hvor fejlen er sket
            } else {
                try {
                    //If we successfully got the actual product we save that instead of the original "2 x 25,5", and divide the price of the product by the amount
                    val productPrice = normalizeText(lineB.text).toFloat()/normalizeText(lineA.text[0].toString()).toFloat()
                    return ParsedProduct(parentLine.text, productPrice)
                } catch (_: NumberFormatException) {
                    Log.d("ERROR", "Error dividing the product price with the amount!")
                }
            }
        }
        //endregion

        //region RABAT
        //Hvis det er en "RABAT" så opdaterer vi lige prisen på det sidste parsed produkt
        if (includeRABAT && fuzzyMatcher.match(productName, listOf("RABAT"), 0.85f, 0.15f)) {
            parsedProducts.last().price -= productPrice //Hvis det er en "Rabat" så trækker vi det lige fra det sidste produkt
        }
        //endregion

        return ParsedProduct(productName, productPrice)
    }

    /**
     * Filters the products parsed and returns a filtered set which is cleaned up and ready to be given to the database
     */
    private fun parsedProductsToFilteredProductList(parsedProducts: List<ParsedProduct>): HashSet<Product> {
        val products: HashSet<Product> = HashSet<Product>()

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
        val filteredSet = products.filter { product -> !isStopWord(product.name) && product.price !in stopWordPrices && !fuzzyMatcher.match(product.name, listOf("RABAT"), 0.8f, 0.2f) }.toHashSet()

        if (filteredSet.isEmpty()) throw ParsingException("Final list couldn't be read. Please try again")

        return filteredSet
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
}

