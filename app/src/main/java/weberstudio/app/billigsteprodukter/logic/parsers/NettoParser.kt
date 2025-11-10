package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Formatter.isIshEqualTo
import weberstudio.app.billigsteprodukter.logic.Formatter.normalizeText
import weberstudio.app.billigsteprodukter.logic.Logger
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedLine
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedProduct
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanError
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation
import kotlin.math.sqrt

object NettoParser : StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()
    //endregion

    override fun parse(receipt: Text): ParsedImageText {
        val includeRABAT = false
        val parsedLines = processImageText(receipt)
        val parsedProducts = ArrayList<ParsedProduct>()
        val parseAfterControlLineFound = ArrayList<Pair<ParsedLine, ParsedLine>>() //Line combos vi parser senere efter at have fundet controllinjen
        val scanLogicErrors = HashMap<ParsedProduct, ScanError?>() //Errors fundet under ParsedProduct processen

        //region PARSES LINES INTO PRODUCTS
        var controlLine: ParsedLine? = null
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                //Tries to get a controlLine
                if (controlLine == null && (fuzzyMatcher.match(lineB.text, listOf("NETTO"), 0.85f, 0.15f))) controlLine = lineA
                if (controlLine == null && (fuzzyMatcher.match(lineB.text, listOf("NETTO"), 0.85f, 0.15f))) controlLine = lineB

                if (doesLinesCollide(lineA, lineB)) {
                    //Enten parser produkterne, eller gemmer parsningen til efter vi har fundet kontrollinjen.
                    if (controlLine == null) parseAfterControlLineFound.add(Pair(lineA, lineB))
                    else {
                        val (product, error) = parseLinesToProduct(lineA, lineB, controlLine, includeRABAT, parsedLines, parsedProducts)
                        parsedProducts.add(product)
                        scanLogicErrors.put(product, error)
                    }
                }
            }
        }
        if (controlLine == null) {
            Logger.log(this.toString(), "No control line found!")
            throw ParsingException("Kunne ikke finde butikken. Husk at inkludere butikslogoet i billedet.")
        }

        //Parser de linjecomboer vi missede fordi kontrollinjen endnu ik var fundet
        parseAfterControlLineFound.forEach { pair ->
            val (product, error) = parseLinesToProduct(pair.first, pair.second, controlLine, includeRABAT, parsedLines, parsedProducts)
            parsedProducts.add(product)
            scanLogicErrors.put(product, error)
        }
        //endregion


        val (filteredProducts, scanValidation, receiptTotal) = parsedProductsToFilteredProductList(parsedProducts, scanLogicErrors)
        return ParsedImageText(Store.Netto, filteredProducts, receiptTotal, scanValidation)
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
        controlLine: ParsedLine,
        includeRABAT: Boolean,
        parsedLines: List<ParsedLine>,
        parsedProducts: List<ParsedProduct>,
    ): Pair<ParsedProduct, ScanError?> {
        val productName = lineA.text
        val productPrice: Float

        //Prøver at converte prisen til Float
        try {
            productPrice = lineB.text.toFloat()
        } catch (_: NumberFormatException) {
            return Pair(ParsedProduct(productName, 0.0f), ScanError.PRODUCT_WITHOUT_PRICE)
        }

        //region QUANTITY LINE
        // Hvis det er en "3 x 9.95" parser vi den sidste linjes navn
        if (isQuantityLine(productName)) {
            //Finder den linje som er lige over "RABAT" og snupper navnet fra den
            val parentLine = getLineAboveUsingReference(parsedLines, lineA, controlLine) //OBS: BLACK MAGIC FUCKERY
            val wrongName: Boolean = parsedProducts.any { products -> products.name == parentLine?.text } //We might have already parsed a product with this name, if we have something is wrong
            if (parentLine == null || wrongName) {
                return Pair(ParsedProduct(productName, productPrice), ScanError.WRONG_NAME)
            } else {
                try {
                    //If we successfully got the actual product we save that instead of the original "2 x 25,5", and divide the price of the product by the amount
                    val productPrice = normalizeText(lineB.text).toFloat()/normalizeText(lineA.text[0].toString()).toFloat()
                    return Pair(ParsedProduct(parentLine.text, productPrice), null)
                } catch (_: NumberFormatException) {
                    return Pair(ParsedProduct(productName, 0.0f), ScanError.PRODUCT_WITHOUT_PRICE)
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


        return Pair(ParsedProduct(productName, productPrice), null)
    }

    /**
     * Filters the products parsed.
     * @return the filtered map of products with the errors in parsing, and the total price read on receipt
     */
    private fun parsedProductsToFilteredProductList(
        parsedProducts: List<ParsedProduct>,
        parsedProductErrors: Map<ParsedProduct, ScanError?>
    ): Triple<HashSet<Product>, ScanValidation,  Float>  {
        var scanValidation = ScanValidation()
        val products = HashSet<Product>()

        //Omdanner de parsedProducts om til Products
        for (parsedProduct in parsedProducts) {
            val product = Product(name = parsedProduct.name, price = parsedProduct.price, store = Store.Netto)
            val error: ScanError? = parsedProductErrors[parsedProduct]
            products.add(product)
            error?.let { error -> scanValidation = scanValidation.withProductError(product, error) }
        }

        if (products.isEmpty()) {
            Logger.log(this.toString(), "Productlist is empty!")
            throw ParsingException("Kunne ikke finde nogle produkter i kvitteringen! Prøv at flade kvitteringen ud og tag billedet i flashlight mode")
        }

        //region Filtering and returning
        //Hvis der er mere end to produkter (så ét produkt og ét stopord), så gemmer vi alle dem som har den samme pris som stop ordene (Så hvis "Total" fucker f.eks.)
        val total: Float = if (products.size > 2) {
            products
                .filter { isReceiptTotalWord(it.name) }
                .maxOf { it.price }
        } else {
            0.0f
        }

        //Returner kun produkter som ikke er stop ordet, eller som har den samme pris som stop ordet (så hvis "Total" fucker f.eks.)
        val filteredSet = products.filter { product ->
            !isReceiptTotalWord(product.name) &&
            !isIgnoreWord(product.name) &&
            !product.price.isIshEqualTo(total)
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
        val stopWords = listOf("TOTAL", "BETALINGSKORT")

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
        val ignoreWords = listOf("MOMS", "UDGØR", "PANT", "RABAT")

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
        return "Netto"
    }
}

