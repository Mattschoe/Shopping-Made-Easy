package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedLine
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedProduct
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation
import kotlin.math.sqrt
import kotlin.text.Regex

object LidlParser: StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()
    //endregion

    override fun parse(receipt: Text): ParsedImageText {
        val includeRABAT = false
        val parsedLines = processImageText(receipt)
        val parsedProducts = ArrayList<ParsedProduct>()
        val product2Quantity = HashMap<ParsedLine, ParsedLine>()

        //region MARKS QUANTITY LINES
        //Hvis nogle af rows'ne indeholder quantity lines så markerer vi det lige så vi har referencen senere
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            //Parser til produkt hvis linjer collider, aka de er på samme "Row" i kvitteringen
            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                if (doesLinesCollide(lineA, lineB) && (isQuantityLine(lineB.text) || Regex("^\\d+$").matches(lineB.text))) {
                    product2Quantity.put(lineA, lineB)
                }
            }
        }
        //endregion

        //region PARSES LINES INTO PRODUCTS
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            //Parser til produkt hvis linjer collider, aka de er på samme "Row" i kvitteringen
            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                if (doesLinesCollide(lineA, lineB)) {
                    if (product2Quantity.get(lineA) == lineB) continue //If lineA is a in the map and the value is lineB, we skip it so it parses with the price and not the quantity
                    parsedProducts.add(parseLinesToProduct(lineA, lineB, includeRABAT, parsedProducts, product2Quantity))
                    break
                }
            }
        }
        //endregion

        val (filteredProducts, receiptTotal) = parsedProductsToFilteredProductList(parsedProducts)
        return ParsedImageText(Store.Lidl, filteredProducts, receiptTotal, ScanValidation())
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
    private fun parseLinesToProduct(lineA: ParsedLine, lineB: ParsedLine, includeRABAT: Boolean, parsedProducts: List<ParsedProduct>, product2Quantity: Map<ParsedLine, ParsedLine>): ParsedProduct {
        //Snupper navn og pris
        val productName = lineA.text
        val productPrice = productPriceToFloat(lineB.text)

        //Prøver at converte prisen til Float
        if (productPrice == null) {
            //TODO: Det her skal give et ("!") ude på UI'en for useren
            Log.d("ERROR", "Couldn't convert ${lineB.text} to Float!")
            return ParsedProduct(productName, 0.0f)
        }

        //region QUANTITY LINE
        //Hvis linjen over er en quantity line, så prøver vi at beregne prisen ud fra quantity line
        val quantityLine = product2Quantity[lineA]
        if (quantityLine != null) {
            val quantity = productPriceToFloat(quantityLine.text.last().toString())
            if (quantity == null) {
                //TODO: Det her skal give et ("!") ude på UI'en for useren
                Log.d("ERROR", "Error dividing the product price of quantityLine: $quantityLine, with the amount!")
                return ParsedProduct(productName, 0.0f) //Returner produktet med pris på 0,0 så vi useren kan se der er noget galt
            }

            //Hvis det lykkedes at snuppe quantity linjen, så beregner vi enhedsprisen ud fra det.
            val productPrice = productPrice/quantity
            return ParsedProduct(lineA.text, productPrice)
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
     * Filters the products parsed and returns a filtered set and the total price read on receipt
     */
    private fun parsedProductsToFilteredProductList(parsedProducts: List<ParsedProduct>): Pair<HashSet<Product>, Float> {
        val products: HashSet<Product> = HashSet<Product>()

        //Omdanner de parsedProducts om til Products
        for (parsedProduct in parsedProducts) {
            products.add(Product(name = parsedProduct.name, price = parsedProduct.price, store = Store.Lidl))
        }

        if (products.isEmpty()) {
            Log.d("ERROR", "ProductList is empty!")
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
        val filteredSet = products.filter { product ->
            !isStopWord(product.name) &&
            product.price !in stopWordPrices &&
            !isQuantityLine(product.name) &&
            !product.name.matches(Regex("[0-9]+")) && //Hvis det bare er tal
            !fuzzyMatcher.match(product.name, listOf("RABAT", "LIDL PLUS-TILBUD"), 0.8f, 0.2f) }.toHashSet()


        if (filteredSet.isEmpty()) {
            Log.d("ERROR", "Filtered set of products is empty!. Please try again")
            throw ParsingException("Filtered set of products is empty!. Please try again")
        }

        return Pair(filteredSet, stopWordPrices.first())
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
        val stopWords = listOf("SUM", "KORT")

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

    override fun toString(): String {
        return "Lidl"
    }

    /**
     * Converts the product price to a Float
     * @return **null** if the float couldn't be converted
     */
    private fun productPriceToFloat(productPrice: String): Float? {
        //Filter så vi kun har hvad der er gyldigt for Float-literal
        val withDots = productPrice.replace(',', '.')
        val filtered = withDots.filter { it.isDigit() || it == '.' || it == '-' || it == '+' }

        //Fjerner ekstra kommaer/punktummere end decimaltalet
        val normalized = if (filtered.count { it == '.' } > 1) {
            val parts = filtered.split('.')
            parts.first() + "." + parts.drop(1).joinToString("")
        } else {
            filtered
        }

        // 4. Finally, parse
        return normalized.toFloatOrNull()
    }
}