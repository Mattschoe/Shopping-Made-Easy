package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Formatter.isIshEqualTo
import weberstudio.app.billigsteprodukter.logic.Formatter.normalizeText
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedLine
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedProduct
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanError
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

object SuperBrugsenParser: StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()
    //endregion

    override fun parse(receipt: Text): ParsedImageText {
        val includeRABAT = false
        val parsedLines = processImageText(receipt)
        var controlLine: ParsedLine? = null //A line thats "Above" all products. Often coming from StoreLogo text
        val parsedProducts = ArrayList<ParsedProduct>()
        val isMarked = HashMap<ParsedLine, Boolean>()
        val scanLogicErrors = HashMap<ParsedProduct, ScanError?>()

        //region FINDS CONTROLLINE
        for (line in parsedLines) {
            //Tries to get a controlLine
            if (controlLine == null && (fuzzyMatcher.match(line.text, listOf("SUPER BRUGSEN", "BRUGSEN"), 0.85f, 0.15f))) {
                controlLine = line
            }
        }
        if (controlLine == null) {
            Log.d("ERROR", "Couldn't find any control line!")
            throw ParsingException("No control line found!")
        }
        //endregion

        //region MARKS QUANTITYLINES
        //Hvis linje er en quantity så markerer vi den under den som en der skal have dens pris ændret senere
        for (line in parsedLines) {
            if (isQuantityLine(line.text)) {
                val childLine = getProductLineBelowUsingReference(parsedLines, line, controlLine)
                if (childLine != null) isMarked.put(childLine, true)
                else Log.d("ERROR", "Couldn't find childLine for line: ${line.text}")
                continue
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
                    val (product, error) = parseLinesToProduct(lineA, lineB, controlLine, includeRABAT, parsedLines, parsedProducts, isMarked)
                    parsedProducts.add(product)
                    scanLogicErrors.put(product, error)
                    break
                }
            }
        }
        //endregion

        val (filteredProducts, scanValidation, receiptTotal) = parsedProductsToFilteredProductList(parsedProducts, scanLogicErrors)
        return ParsedImageText(Store.SuperBrugsen, filteredProducts, receiptTotal, scanValidation)
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
        isMarked: Map<ParsedLine, Boolean>
    ): Pair<ParsedProduct, ScanError?> {
        //Snupper navn og pris
        val productName = lineA.text
        val productPrice = lineB.text.toFloatOrNull() ?: 0.0f

        //region QUANTITY LINE
        //Hvis linjen over er en quantity line, så prøver vi at beregne prisen ud fra quantity line
        if (isMarked.contains(lineA) ||isMarked.contains(lineB)) {
            val quantityLine = getQuantityLineAboveUsingReference(parsedLines, lineA, controlLine)
            if (quantityLine == null) {
                return Pair(ParsedProduct(productName, productPrice), ScanError.WRONG_NAME)
            } else {
                try {
                    //Hvis det lykkedes at snuppe quantity linjen, så beregner vi enhedsprisen ud fra det.
                    val productPrice = productPrice/normalizeText(quantityLine.text[0].toString()).toFloat()
                    return Pair(ParsedProduct(lineA.text, productPrice), null)
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

        if (productPrice.isIshEqualTo(0.0f)) {
            return Pair(ParsedProduct(productName, productPrice), ScanError.PRODUCT_WITHOUT_PRICE)
        }
        return Pair(ParsedProduct(productName, productPrice), null)
    }

    /**
     * Filters the products parsed and returns a filtered set which is cleaned up and ready to be given to the database
     */
    private fun parsedProductsToFilteredProductList(
        parsedProducts: List<ParsedProduct>,
        parsedProductErrors: Map<ParsedProduct, ScanError?>
    ): Triple<HashSet<Product>, ScanValidation, Float> {
        var scanValidation = ScanValidation()
        val products: HashSet<Product> = HashSet<Product>()

        //Omdanner de parsedProducts om til Products
        for (parsedProduct in parsedProducts) {
            val product = Product(name = parsedProduct.name, price = parsedProduct.price, store = Store.Coop365)
            val error: ScanError? = parsedProductErrors[parsedProduct]
            products.add(product)
            error?.let { error -> scanValidation = scanValidation.withProductError(product, error) }
        }

        if (products.isEmpty()) {
            Log.d("ERROR", "ProductList is empty!")
            throw ParsingException("Productlist is empty!")
        }

        //region Filtering and returning
        //Hvis der er mere end to produkter (så ét produkt og ét stopord), så gemmer vi alle dem som har den samme pris som stop ordene (Så hvis "Total" fucker f.eks.)
        val total = if (products.size > 2) {
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
            !isQuantityLine(product.name) &&
            !product.price.isIshEqualTo(total)
        }.toHashSet()

        if (filteredSet.isEmpty()) {
            Log.d("ERROR", "Filtered set of products is empty!. Please try again")
            throw ParsingException("Filtered set of products is empty!. Please try again")
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
        val stopWords = listOf("AT BETALE", "VISA", "BETALINGSKORT")

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
        val ignoreWords = listOf("MOMS", "MOMS IALT", "BYTTEPENGE", "PANT", "RABAT", "HERAF")

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
        return "SuperBrugsen"
    }

    /**
     * Returns the next **PRODUCT** below the one given as reference, will avoid quantity lines
     * @param referenceLine a reference to the line where the one directly above it will be returned
     * @param controlLineAbove the control line that shows what's "above" in the image (Often taking from storeLogo text)
     */
    fun getProductLineBelowUsingReference(allLines: List<ParsedLine>, referenceLine: ParsedLine, controlLineAbove: ParsedLine): ParsedLine? {
        //Compute the "upward" unit vector
        val vUpX = (controlLineAbove.center.x - referenceLine.center.x).toFloat()
        val vUpY = (controlLineAbove.center.y - referenceLine.center.y).toFloat()
        val vLen = hypot(vUpX, vUpY)
        if (vLen == 0f) return null
        val upX = vUpX / vLen
        val upY = vUpY / vLen

        val perpX = -upY
        val perpY = upX
        val perpendicularTolerance = 5000f

        //Project, filter, sort by true‐distance, then pick first matching isQuantityLine
        return allLines
            .asSequence()
            .filter { it != referenceLine }
            .map { line ->
                val toLineX = line.center.x - referenceLine.center.x
                val toLineY = line.center.y - referenceLine.center.y
                val dot = toLineX * upX + toLineY * upY
                val perpDist = abs(toLineX * perpX + toLineY * perpY)
                Triple(line, dot, perpDist)
            }
            .filter { (_, _, perpDist) -> perpDist < perpendicularTolerance }  //First: within lane
            .filter { (_, dot, _) -> dot < 0f }  //Then: below
            .sortedBy { (_, dot, _) -> -dot }
            .firstOrNull { (line, _, _) -> !isQuantityLine(line.text) }
            ?.first
    }
    /**
     * Returns the next **QUANTITY** above the one given as reference, will avoid product lines
     * @param referenceLine a reference to the line where the one directly above it will be returned
     * @param controlLineAbove the control line that shows what's "above" in the image (Often taking from storeLogo text)
     */
    fun getQuantityLineAboveUsingReference(allLines: List<ParsedLine>, referenceLine: ParsedLine, controlLineAbove: ParsedLine): ParsedLine? {
        // 1) Compute the "upward" unit vector
        val vUpX = (controlLineAbove.corners[3].x - referenceLine.corners[3].x).toFloat()
        val vUpY = (controlLineAbove.corners[3].y - referenceLine.corners[3].y).toFloat()
        val vLen = hypot(vUpX, vUpY)
        if (vLen == 0f) return null
        val upX = vUpX / vLen
        val upY = vUpY / vLen

        // 2) Project, filter, sort by true‐distance, then pick first matching isQuantityLine
        return allLines
            .asSequence()
            .filter { it != referenceLine }
            .map { line ->
                val toLineX = line.center.x - referenceLine.center.x
                val toLineY = line.center.y - referenceLine.center.y
                val dot  = toLineX * upX + toLineY * upY   // projection
                val dist = hypot(toLineX, toLineY)        // Euclidean distance
                Triple(line, dot, dist)
            }
            .filter { (_, dot, _) -> dot > 0f }           // only “above”
            .sortedBy { (_, _, dist) -> dist }           // nearest first
            .firstOrNull { (line, _, _) -> //Makes sure its not a quantity line
                isQuantityLine(line.text)
            }
            ?.first
    }
}