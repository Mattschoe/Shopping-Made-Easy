package weberstudio.app.billigsteprodukter.logic.parsers

import android.graphics.Point
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Line
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import java.util.Collections
import kotlin.jvm.Throws
import kotlin.system.exitProcess

object NettoParser : StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        val products: HashSet<Product> = HashSet<Product>()
        val processedText: ArrayList<Line> = processImageText(receipt)

        if (processedText.isEmpty()) return hashSetOf() //Returner empty Set hvis error

        //Converts all text.lines into actual lines
        val lines: ArrayList<SimpleLine> = ArrayList<SimpleLine>()
        for (textLine in processedText) {
            lines.add(SimpleLine(textLine.cornerPoints!![3], textLine.cornerPoints!![0]))
        }

        //Collects all lines that intersect
        val intersections: ArrayList<Pair<Line, Float>> = ArrayList<Pair<Line, Float>>()
        for (i in 0 until processedText.size) {
            for (j in i + 1 until processedText.size) {
                val originalLine = lines[i]
                val startPoint = Point((originalLine.start.x + originalLine.end.x)/2, (originalLine.start.y + originalLine.end.y)/2) //Middle of the two points
                val normalizedLine = SimpleLine(startPoint, Point(Integer.MAX_VALUE, startPoint.y)) //Line starting at middle and going towards infinity on the right

                if (doesLinesIntersect(normalizedLine, lines[j])) {
                    //If intersect we first check if its a collection, eg: "2 x 25,5" if it is, we save the previous one
                    val productName = normalizeText(processedText[i].text)
                    if (productName.contains("X ") || productName.contains(" X")) {
                        val previousProduct = processedText.getOrNull(i-1)
                        if (previousProduct == null) println("Couldn't access the previous product from product $productName!")
                        else {
                            try {
                                //If we successfully got the actual product we save that instead of the original "2 x 25,5", and divide the price of the product by the amount
                                val productPrice = normalizeText(processedText[j].text).toFloat()/normalizeText(processedText[i].text[0].toString()).toFloat()
                                intersections.add(Pair(processedText[i-1], productPrice))
                                break //Only connects one line to another, not multiple
                            } catch (_: NumberFormatException) {
                                println("Error dividing the product price with the amount!")
                            }
                        }
                    }

                    try {
                        intersections.add(Pair(processedText[i], normalizeText(processedText[j].text).toFloat()))
                        break //Only connects one line to another, not multiple
                    } catch (_: NumberFormatException) {
                        println("Error converting: ${normalizeText(processedText[j].text)} to Float!")
                    }
                }
            }
        }

        //Instantiates products
        for (pair in intersections) {
            products.add(Product(pair.first.text, pair.second))
        }

        //Cleanup in instantiated objects
        products.removeAll {
            it.name.contains("TOTAL") //We don't need total
        }

        return products
    }

    ///Processes the image text so we later only parse the products and not the full receipt
    private fun processImageText(originalText: Text): ArrayList<Line> {
        val allLines: ArrayList<Line> = ArrayList<Line>()
        var anchorTop = Int.MAX_VALUE
        var anchorBottom = Int.MIN_VALUE

        //Finds anchors
        for (block in originalText.textBlocks) {
            for (line in block.lines) {
                allLines.add(line)
                val normalizedText: String = normalizeText(line.text)
                if (normalizedText.contains("NETTO") || normalizedText.contains("ETTO")) anchorTop = minOf(anchorTop, line.boundingBox!!.top) //Found the top
                if (normalizedText.contains("TOTAL")) anchorBottom = maxOf(anchorBottom, line.boundingBox!!.bottom) //Found bottom
            }
        }
        //If anchors not found
        if (anchorTop == Int.MAX_VALUE || anchorBottom == Int.MIN_VALUE) throw ParsingException("Error finding anchors!")

        //If found we collect every line thats inside the bounding box
        val linesInRange: ArrayList<Line> = ArrayList<Line>()
        for (line in allLines) {
            val lineBox = line.boundingBox!!
            if (lineBox.top >= anchorTop && lineBox.bottom <= anchorBottom) linesInRange.add(line)
        }
        //If no line found between anchors
        if (linesInRange.isEmpty()) {
            throw ParsingException("No lines found between anchorTop and anchorBottom!")
        }
        //Adds all lines to a list of strings
        return linesInRange
    }

    /**
     * Normalizes the text to uppercase + no danish
     */
    private fun normalizeText(text: String): String {
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
     * Returns whether the two given lines intersect
     */
    private fun doesLinesIntersect(line1: SimpleLine, line2: SimpleLine): Boolean {
        val x1 = line1.start.x.toFloat()
        val x2 = line1.end.x.toFloat()
        val x3 = line2.start.x.toFloat()
        val x4 = line2.end.x.toFloat()
        val y1 = line1.start.y.toFloat()
        val y2 = line1.end.y.toFloat()
        val y3 = line2.start.y.toFloat()
        val y4 = line2.end.y.toFloat()

        //Black magic fuckery
        val uA: Float = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
        val uB: Float = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))

        //Bro, Kilde ig: https://www.jeffreythompson.org/collision-detection/line-line.php
        return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1
    }

    data class SimpleLine(val start: Point, val end: Point)

    override fun toString(): String {
        return "Netto"
    }
}