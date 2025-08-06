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
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * If the receipt has quantity price **BELOW** the product, like so:
 *
 * Tortollini 500g
 *
 * 2 x 12,95...................25,9
 */
object CoopParserQuantityBelow : StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()
    //endregion

    override fun parse(receipt: Text): HashSet<Product> {
        val includeRABAT = false
        val parsedLines = processImageText(receipt)
        var controlLine: ParsedLine? = null //A line thats "Above" all products. Often coming from StoreLogo text
        val parsedProducts = ArrayList<ParsedProduct>()

        //region FINDS CONTROLLINE
        for (line in parsedLines) {
            //Tries to get a controlLine
            if (controlLine == null && (fuzzyMatcher.match(line.text, listOf("DET RIGTIGE STED AT SPARE", "365", "365 DISCOUNT"), 0.85f, 0.15f))) controlLine = line
        }
        if (controlLine == null) {
            Log.d("ERROR", "Couldn't find any control line!")
            throw ParsingException("No control line found!")
        }
        //endregion

        //region PARSES LINES INTO PRODUCTS
        //Parser til produkt hvis linjer collider, aka de er på samme "Row" i kvitteringen
        for (i in parsedLines.indices) {
            val lineA = parsedLines[i]

            for (j in parsedLines.indices) {
                if (i == j) continue
                val lineB = parsedLines[j]

                if (doesLinesCollide(lineA, lineB)) {
                    parsedProducts.add(parseLinesToProduct(lineA, lineB, controlLine, includeRABAT, parsedLines, parsedProducts))
                    break
                }
            }
        }
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
            val parentLine = getLineAboveUsingReference(parsedLines, lineA, controlLine) //OBS: BLACK MAGIC FUCKERY
            if (parentLine == null) {
                //TODO: Det her skal give et ("!") ude på UI'en for useren
                Log.d("ERROR", "Couldn't access the previous product from product $productName!");
                return ParsedProduct(productName, productPrice) //Returnerer bare quantity line så vi kan se hvor fejlen er sket
            } else {
                try {
                    //If we successfully got the actual product we save that instead of the original "2 x 25,5", and divide the price of the product by the amount
                    val productPrice = productPrice/normalizeText(lineA.text[0].toString()).toFloat()
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
            products.add(Product(parsedProduct.name, parsedProduct.price, Store.Coop365))
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
        val filteredSet = products.filter { product ->
            !isStopWord(product.name) &&
            product.price !in stopWordPrices &&
            !fuzzyMatcher.match(product.name, listOf("RABAT"), 0.8f, 0.2f) }.toHashSet()

        if (filteredSet.isEmpty()) throw ParsingException("Filtered set of products is empty!. Please try again")

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
        val stopWords = listOf("AT BETALE", "VISA", "BETALINGSKORT", "RABAT I ALT", "MOMS UDGØR", "BYTTEPENGE")

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
        return "Coop365"
    }
}

/**
 * If the receipt has quantity price **ABOVE** the product, like so:
 *
 * ............2x4,95............
 *
 * Tomato paste...............9,90
 */
object CoopParserQuantityAbove : StoreParser {
    //region FIELDS
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcher = FuzzyMatcher()
    //endregion

    override fun parse(receipt: Text): HashSet<Product> {
        val includeRABAT = false
        val parsedLines = processImageText(receipt)
        var controlLine: ParsedLine? = null //A line thats "Above" all products. Often coming from StoreLogo text
        val parsedProducts = ArrayList<ParsedProduct>()
        val isMarked = HashMap<ParsedLine, Boolean>()

        //region FINDS CONTROLLINE
        for (line in parsedLines) {
            //Tries to get a controlLine
            if (controlLine == null && (fuzzyMatcher.match(line.text, listOf("DET RIGTIGE STED AT SPARE", "365", "365 DISCOUNT"), 0.85f, 0.15f))) controlLine = line
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
                    parsedProducts.add(parseLinesToProduct(lineA, lineB, controlLine, includeRABAT, parsedLines, parsedProducts, isMarked))
                    break
                }
            }
        }
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
    private fun parseLinesToProduct(lineA: ParsedLine, lineB: ParsedLine, controlLine: ParsedLine, includeRABAT: Boolean, parsedLines: List<ParsedLine>, parsedProducts: List<ParsedProduct>, isMarked: Map<ParsedLine, Boolean>): ParsedProduct {
        //Snupper navn og pris
        val productName = lineA.text
        val productPrice: Float

        //Prøver at converte prisen til Float
        try { productPrice = lineB.text.toFloat() }
        catch (_: NumberFormatException) {
            //TODO: Det her skal give et ("!") ude på UI'en for useren
            Log.d("ERROR", "Couldn't convert ${lineB.text} to Float!")
            return ParsedProduct(productName, 0.0f)
        }

        //region QUANTITY LINE
        //Hvis linjen over er en quantity line, så prøver vi at beregne prisen ud fra quantity line
        if (isMarked.getOrDefault(lineA, false)) {
            val quantityLine = getQuantityLineAboveUsingReference(parsedLines, lineA, controlLine)
            if (quantityLine == null) { //TODO: Det her skal give et ("!") ude på UI'en for useren
                Log.d("ERROR", "Couldn't access the previous product from product $productName!");
                return ParsedProduct(productName, productPrice) //Returnerer bare quantity line så vi kan se hvor fejlen er sket
            } else {
                try {
                    //Hvis det lykkedes at snuppe quantity linjen, så beregner vi enhedsprisen ud fra det.
                    val productPrice = productPrice/normalizeText(quantityLine.text[0].toString()).toFloat()
                    return ParsedProduct(lineA.text, productPrice)
                } catch (_: NumberFormatException) {
                    //TODO: Det her skal give et ("!") ude på UI'en for useren
                    Log.d("ERROR", "Error dividing the product price with the amount!")
                    return ParsedProduct(productName, 0.0f) //Returner produktet med pris på 0,0 så vi useren kan se der er noget galt
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
            products.add(Product(parsedProduct.name, parsedProduct.price, Store.Coop365))
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
            !fuzzyMatcher.match(product.name, listOf("RABAT"), 0.8f, 0.2f) }.toHashSet()

        if (filteredSet.isEmpty()) {
            Log.d("ERROR", "Filtered set of products is empty!. Please try again")
            throw ParsingException("Filtered set of products is empty!. Please try again")
        }

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
        val stopWords = listOf("AT BETALE", "VISA", "BETALINGSKORT", "RABAT I ALT", "MOMS UDGØR", "BYTTEPENGE")

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
        return "Coop365"
    }

    /**
     * Returns the next **PRODUCT** below the one given as reference, will avoid quantity lines
     * @param referenceLine a reference to the line where the one directly above it will be returned
     * @param controlLineAbove the control line that shows what's "above" in the image (Often taking from storeLogo text)
     */
    fun getProductLineBelowUsingReference(allLines: List<ParsedLine>, referenceLine: ParsedLine, controlLineAbove: ParsedLine): ParsedLine? {
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
            .filter { (_, dot, _) -> dot < 0f }           // only “above”
            .sortedBy { (_, _, dist) -> dist }           // nearest first
            .firstOrNull { (line, _, _) -> //Makes sure its not a quantity line
                !isQuantityLine(line.text)
            }
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