package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.Point
import android.graphics.PointF
import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import kotlin.jvm.Throws
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
    fun parse(receipt: Text): HashSet<Product>

    /**
     * Returns the name of the store
     */
    override fun toString(): String

    /**
     * Given a list of lines, it finds the one line above the *quantityLine* who is just “above” it.
     * OBS: DET HER ER BLACK MAGIC SHIT OG GG MED AT FORSTÅ DET
     * @param referenceLine the line that controls which way is up and down on the Y-axis. Often provided from either the storebrand, "Total", "Betalingskort" and "Moms" lines.
     */
    fun findLineAboveUsingReference(allLines: List<ParsedLine>, quantityLine: ParsedLine, referenceLine: ParsedLine): ParsedLine? {
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
        val verticalBounds = lineHeight * 0.5f //Halvdelen af linjens højde
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

    //region SUPPORT DATA CLASS/FUNCTIONS
    fun normalizeText(text: String): String {
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
    data class ParsedLine(
        val text: String,
        val angle: Float,
        val center: PointF,
        val direction: PointF,
        val corners: Array<Point>
    )
    data class ParsedProduct(val name: String, var price: Float)
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

