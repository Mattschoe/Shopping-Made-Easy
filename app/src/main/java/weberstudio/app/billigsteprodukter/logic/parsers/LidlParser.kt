package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.PointF
import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Formatter.isIshEqualTo
import weberstudio.app.billigsteprodukter.logic.Formatter.normalizeText
import weberstudio.app.billigsteprodukter.logic.Logger
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedLine
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedProduct
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanError
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation
import kotlin.math.sqrt
import kotlin.text.Regex

object LidlParser: StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    //endregion

    override fun parse(receipt: Text): ParsedImageText {
        val parsedLines = processImageText(receipt)
        val collidedLines = mutableMapOf<ParsedLine, ParsedLine>()
        val parsedProducts = ArrayList<ParsedProduct>()
        val product2Quantity = HashMap<ParsedLine, ParsedLine?>()
        val scanLogicErrors = HashMap<ParsedProduct, ScanError?>()

        //region MARKS QUANTITY LINES
        //Hvis nogle af rows'ne indeholder quantity lines så markerer vi det lige så vi har referencen senere
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            //Parser til produkt hvis linjer collider, aka de er på samme "Row" i kvitteringen
            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                //Skips already parsed products
                val isAlreadyParsed = parsedProducts.any { product ->
                    product.name.contains(lineA.text, ignoreCase = true)
                }
                if (isAlreadyParsed) continue

                if (doesLinesCollide(lineA, lineB)) {
                    collidedLines[lineA] = lineB
                    if (isQuantityLine(lineB.text)) product2Quantity.put(lineA, lineB)
                    else if (isQuantityLine(lineA.text)) product2Quantity.put(lineA, null)
                    break
                }
            }
        }
        //endregion

        //PARSES LINES INTO PRODUCTS
        for ((lineA, lineB) in collidedLines) {
            val (product, error) = parseLinesToProduct(lineA, lineB, product2Quantity)
            parsedProducts.add(product)
            scanLogicErrors.put(product, error)
        }

        val (filteredProducts, scanValidation, receiptTotal) = parsedProductsToFilteredProductList(parsedProducts, scanLogicErrors)
        return ParsedImageText(Store.Lidl, filteredProducts, receiptTotal, scanValidation)
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
    private fun parseLinesToProduct(
        lineA: ParsedLine,
        lineB: ParsedLine,
        product2Quantity: Map<ParsedLine, ParsedLine?>
    ): Pair<ParsedProduct, ScanError?> {
        //Snupper navn og pris
        val productName = lineA.text
        val productPrice = productPriceToFloat(lineB.text)

        //region QUANTITY LINE
        //Hvis linjen over er en quantity line, så prøver vi at beregne prisen ud fra quantity line
        val quantityLine = product2Quantity[lineA]
        if (quantityLine != null) {
            //If lineB is the quantityline, we expect this to work
            try {
                val price = quantityLine.text.split(" ")[0].toFloat()
                return Pair(ParsedProduct(productName, price), null)
            } catch (_: NumberFormatException) { /* NO-OP We return failure later */ }

            //Failure in extracting price, so we return product with error
            return Pair(ParsedProduct(productName, 0.0f), ScanError.PRODUCT_WITHOUT_PRICE)
        } else {
            //If the quantity is part of lineA we try and extract it
            withoutQuantity(productName)?.let { (name, quantityLine) ->
                //Det gør den, så vi returner
                return try {
                    val price = quantityLine.split(" ")[0].toFloat()
                    Pair(ParsedProduct(name, price), null)
                } catch (_: NumberFormatException) {
                    Pair(ParsedProduct(productName, 0.0f), ScanError.PRODUCT_WITHOUT_PRICE)
                }
            }

            if (productPrice != null) return Pair(ParsedProduct(productName, productPrice), null) //Success for simple products
            return Pair(ParsedProduct(productName, 0.0f), ScanError.PRODUCT_WITHOUT_PRICE) //Failure in extracting price, so we return product with error
        }
        //endregion


    }

    /**
     * Filters the products parsed and returns a filtered set and the total price read on receipt
     */
    private fun parsedProductsToFilteredProductList(
        parsedProducts: List<ParsedProduct>,
        parsedProductErrors: Map<ParsedProduct, ScanError?>
    ): Triple<HashSet<Product>, ScanValidation, Float> {
        var scanValidation = ScanValidation()
        val products: HashSet<Product> = HashSet<Product>()

        //Omdanner de parsedProducts om til Products
        for (parsedProduct in parsedProducts) {
            val product = Product(name = parsedProduct.name, price = parsedProduct.price, store = Store.Lidl)
            val error = parsedProductErrors[parsedProduct]
            products.add(product)
            error?.let { error -> scanValidation = scanValidation.withProductError(product, error)}
        }

        if (products.isEmpty()) {
            Logger.log(this.toString(), "ProductList is empty!")
            throw ParsingException("Kunne ikke finde nogle produkter i kvitteringen! Prøv at flade kvitteringen ud og tag billedet i flashlight mode")
        }

        //region Filtering and returning
        val total = products
            .filter { isReceiptTotalWord(it.name) }
            .maxOf { it.price }

        //Returner kun produkter som ikke er stop ordet, eller som har den samme pris som stop ordet (så hvis "Total" fucker f.eks.)
        val filteredSet = products.filter { product ->
            !isReceiptTotalWord(product.name) &&
            !isIgnoreWord(product.name) &&
            !isQuantityLine(product.name) &&
            !product.price.isIshEqualTo(total) &&
            !product.name.matches(Regex("[0-9]+")) && //Hvis det bare er tal
            product.name.toFloatOrNull()?.isIshEqualTo(total) != true //Hvis det bare er et tal som er lig total
        }.toHashSet()


        if (filteredSet.isEmpty()) {
            Logger.log(this.toString(), "Filtered set of products is empty!. Please try again")
            throw ParsingException("Kunne ikke finde nogle produkter i kvitteringen! Prøv at flade kvitteringen ud og tag billedet i flashlight mode")
        }

        //Checker om total er det vi regner med, ellers så marker vi at vi tror der er gået noget galt
        val productsTotal = filteredSet.sumOf { product -> product.price.toDouble() }
        if (!productsTotal.isIshEqualTo(total.toDouble())) scanValidation = scanValidation.withTotalError(true)

        return Triple(filteredSet, scanValidation, total)
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
    private fun isReceiptTotalWord(input: String): Boolean {
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

    /**
     * Checks and see if word given as argument is a word that should be ignored when trying to
     * parse into products
     */
    private fun isIgnoreWord(input: String): Boolean {
        val ignoreWords = listOf("RABAT", "PANT", "BYTTEPENGE", "LIDL PLUS-TILBUD", "MOMS", "B 25.0", "INK.MOMS")

        return ignoreWords.any { ignoreWord ->
            val jaroSimilarity = fuzzyMatcherJaro.apply(input, ignoreWord) ?: 0.0
            if (jaroSimilarity >= 0.80) {
                return true
            } //Higher = Fewer false positive

            val levenSimilarity = fuzzyMatcherLeven.apply(input, ignoreWord)
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
        val withDots = productPrice.replace(',', '.').replace(' ', '.')
        val filtered = withDots.filter { it.isDigit() || it == '.' || it == '-' || it == '+' }

        //Fjerner ekstra kommaer/punktummere end decimaltalet
        val normalized = if (filtered.count { it == '.' } > 1) {
            val parts = filtered.split('.')
            parts.first() + "." + parts.drop(1).joinToString("")
        } else {
            filtered
        }

        return normalized.toFloatOrNull()
    }

    /**
     * Splits the productname and the quantity part from the string.
     * @return A [Pair] where:
     * - [Pair.first] is the productName
     * - [Pair.second] is the quantity part (not guaranteed to be correct)
     */
    private fun withoutQuantity(productLine: String): Pair<String, String>? {
        val quantityPattern = Regex("""(\d+[.,]?\d*)\s*([xX*%×])\s*(\d+[.,]?\d*)""")

        //Find the last match
        val match = quantityPattern.findAll(productLine).lastOrNull() ?: return null

        //Split at the start of this match
        val quantityStartIndex = match.range.first

        val productName = productLine.substring(0, quantityStartIndex).trim()
        val quantityString = productLine.substring(quantityStartIndex).trim()

        return Pair(productName, quantityString)
    }
}