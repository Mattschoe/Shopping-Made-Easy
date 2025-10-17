package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.Point
import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

interface StoreParser {
    /**
     * @param receipt the full text containing the receipt
     * @return List<Product> a list of products corresponding to the store
     * @throws ParsingException if there has been a error while trying to parse the receipt. Most commonly it's because no anchor was found, or there was no text between the anchors
     */
    @Throws(ParsingException::class)
    fun parse(receipt: Text): ParsedImageText

    /**
     * Returns the name of the store
     */
    override fun toString(): String

    /**
     * Given a list of lines, it finds the one line above the *quantityLine* who is just “above” it.
     * @param referenceLine the line that controls which way is up and down on the Y-axis. **ITS VERY
     * IMPORTANT** that the referenceLine is ABOVE the quantityLine, and NOT below!
     */
    fun getLineAboveUsingReference(allLines: List<ParsedLine>, quantityLine: ParsedLine, referenceLine: ParsedLine): ParsedLine? {
        // 1) Compute the "upward" unit vector (from quantityLine → referenceLine)
        val vUpX = referenceLine.center.x - quantityLine.center.x
        val vUpY = referenceLine.center.y - quantityLine.center.y
        val vLen = hypot(vUpX, vUpY)
        if (vLen == 0f) return null  // Avoid division by zero if same point
        val upX = vUpX / vLen
        val upY = vUpY / vLen

        // 2) Project all other lines onto this vector
        return allLines
            .asSequence()
            .filter { it != quantityLine }
            .map { line ->
                val toLineX = line.center.x - quantityLine.center.x
                val toLineY = line.center.y - quantityLine.center.y
                val dot = toLineX * upX + toLineY * upY  // signed projection
                val dist = hypot(toLineX, toLineY)
                Triple(line, dot, dist)
            }
            .filter { (_, dot, _) -> dot > 0f }  // Only lines "below" in projected direction
            .minByOrNull { (_, _, dist) -> dist }
            ?.first
    }

    /**
     * Given a list of lines, it finds the one line **BELOW** the *quantityLine* who is just “below” it.
     * OBS: DET HER ER BLACK MAGIC SHIT OG GG MED AT FORSTÅ DET
     * @param referenceLine the line that controls which way is up and down on the Y-axis. Often provided from either the "Total", "Betalingskort" and "Moms" lines.
     */
    fun getLineBelowUsingReference(allLines: List<ParsedLine>, quantityLine: ParsedLine, referenceLine: ParsedLine): ParsedLine? {
        Log.d("DEBUG", "Quantity line: ${quantityLine.text}")
        // 1) Compute the "upward" unit vector (from quantityLine → referenceLine)
        val vUpX = referenceLine.center.x - quantityLine.center.x
        val vUpY = referenceLine.center.y - quantityLine.center.y
        val vLen = hypot(vUpX, vUpY)
        if (vLen == 0f) return null  // Avoid division by zero if same point
        val upX = vUpX / vLen
        val upY = vUpY / vLen

        // 2) Project all other lines onto this vector
        return allLines
            .asSequence()
            .filter { it != quantityLine }
            .map { line ->
                val toLineX = line.center.x - quantityLine.center.x
                val toLineY = line.center.y - quantityLine.center.y
                val dot = toLineX * upX + toLineY * upY  // signed projection
                val dist = hypot(toLineX, toLineY)
                Log.d("DEBUG", "${line.text} | $dot | $dist")
                Triple(line, dot, dist)
            }
            .filter { (_, dot, _) -> dot > 0f }  // Only lines "above" in projected direction
            .minByOrNull { (_, _, dist) -> dist }
            ?.first
    }

    /**
     * Checks whether lineA and lineB collides. **ALSO** makes sure that lineB is along lineA, if thats not the case, or if lineA is down lineB it will return false.
     */
    fun doesLinesCollide(lineA: ParsedLine, lineB: ParsedLine): Boolean {
        //region SETTINGS
        //Kigger kun på de linjer hvor deres centrum er verticalTolerance pixels væk fra hinanden, på den måde slipper vi for alt for mange checks
        val angleTolerance = Math.toRadians(10.0).toFloat()  //A tolerance for ray hit
        val corners = lineA.corners
        val lineHeight = ((euclidDistance(corners[0], corners[3]) + euclidDistance(corners[1], corners[2]))/2f)
        val verticalBounds = lineHeight * 0.6f //Halvdelen af linjens højde
        //endregion

        //Skipper hvis de ikke er inde for tolerancen
        val angleDifference = angleDifferenceModulosPi(lineA.angle, lineB.angle)
        if (angleDifference > angleTolerance) return false

        //Skipper hvis de ikke er inde for samme "Row" i kvitteringen
        val dx = lineB.center.x - lineA.center.x
        val dy = lineB.center.y - lineA.center.y
        val upX = -lineA.direction.y
        val upY = lineA.direction.x
        val rowOffset = abs(upX * dx + upY * dy)
        if (rowOffset > verticalBounds) return false

        //Accepterer kun "hits" hvor linje B er "hen ad" linje A. På den måde kan vi checke kvitteringen korrekt ind selvom billedet bliver taget 180 grader
        val forwardDotProduct = lineA.direction.x * dx + lineA.direction.y * dy
        val buffer = 0.1f //Allows for a tiny slope
        if (forwardDotProduct < lineHeight * buffer) return false

        //Parser til produkt så længe at raycast hit er langt nok væk for at det faktisk kan være en pris og ikke noise
        val minNameToPriceOffset = lineHeight * 3.0f //How long away the price raycast hit needs to be to be accounted for. Lower = More forgiving, Higher = Less Forgiving. FX: 0.8 = "Atleast 0.8x the line height away"
        if (forwardDotProduct < minNameToPriceOffset) return false

        return true
    }

    /**
     * Checks whether the given string is reminiscent of a quantity line, aka a line like "2 x 14,00"
     */
    fun isQuantityLine(line: String): Boolean {
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
                    ch == 'x' ||
                    ch == '%'
            //Add flere her hvis er
        }
    }

    //region SUPPORT DATA CLASS/FUNCTIONS


    /**
     * Checks if the two floats given are *ish* equal to eachother
     * @param epsilon the difference between the two floats. Shouldn't be given as standard unless
     * for a special reason
     */
    fun Double.isIshEqualTo(other: Double, epsilon: Double = 0.0001): Boolean {
        if (this.isNaN() || other.isNaN()) return false
        return abs(this - other) <= epsilon
    }

    data class ParsedLine(
        val text: String,
        val angle: Float,
        val center: PointF,
        val direction: PointF,
        val corners: Array<Point>
    )
    data class ParsedProduct(val name: String, var price: Float)
    data class ParsedImageText(val store: Store, val products: HashSet<Product>, val total: Float, val scanErrors: ScanValidation)
    data class ScanValidation(
        val productErrors: Map<Product, ScanError> = emptyMap(),
        val totalError: Boolean = false
    ) {
        fun withProductError(product: Product, error: ScanError) = copy(
            productErrors = productErrors + (product to error)
        )

        fun withTotalError(hasError: Boolean) = copy(
            totalError = hasError
        )
    }

    enum class ScanError {
        PRODUCT_WITHOUT_PRICE,
        WRONG_NAME,
        RECEIPT_TOTAL_ERROR;
    }

    //endregion

    //region MATH SUPPORT FUNCTIONS
    /**
     * Øhhhh, såe hvis useren vender kamerat 180 grader og tager billedet så sørger denne her for vi stadig korrekt kan parse produkterne
     */
    fun angleDifferenceModulosPi(a: Float, b: Float): Float {
        val raw = abs(a - b) % Math.PI.toFloat()
        return min(raw, Math.PI.toFloat() - raw)
    }
    fun calculateAngle(p0: Point, p1: Point): Float {
        val dx = (p1.x - p0.x).toFloat()
        val dy = (p1.y - p0.y).toFloat()
        return atan2(dy, dx) //Angle in radians
    }
    fun euclidDistance(p1: Point, p2: Point): Float {
        val dx = (p2.x - p1.x).toFloat()
        val dy = (p2.y - p1.y).toFloat()
        return hypot(dx, dy) // same as sqrt(dx*dx + dy*dy)
    }
    //endregion
}

