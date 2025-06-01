package weberstudio.app.billigsteprodukter.parsers

import android.graphics.Point
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Line
import weberstudio.app.billigsteprodukter.Product
import kotlin.system.exitProcess

object NettoParser : StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        val products: HashSet<Product> = HashSet<Product>()
        val processedText: ArrayList<Line> = processImageText(receipt)

        //Converts all text.lines into actual lines
        val lines: ArrayList<SimpleLine> = ArrayList<SimpleLine>()
        for (textLine in processedText) {
            lines.add(SimpleLine(textLine.cornerPoints!![3], textLine.cornerPoints!![0]))
        }

        //Collects all lines that intersect
        val intersections: ArrayList<Pair<Line, Line>> = ArrayList<Pair<Line, Line>>()
        for (i in 0 until processedText.size) {
            for (j in i + 1 until processedText.size) {
                val originalLine = lines[i]
                val startPoint = Point((originalLine.start.x + originalLine.end.x)/2, (originalLine.start.y + originalLine.end.y)/2)
                val normalizedLine = SimpleLine(startPoint, Point(Integer.MAX_VALUE, startPoint.y))

                if (doesLinesIntersect(normalizedLine, lines[j])) {
                    //If intersect we first check if its a collection, eg: "2 x 25,5" if it is, we save the previous one


                    intersections.add(Pair(processedText[i], processedText[j]))
                }
            }
        }

        //Instantiates products
        var nextIsNotProduct = false
        for (i in intersections.indices) {
            if (nextIsNotProduct) {
                //Skips if we marked the next line as not a product
                nextIsNotProduct = false
                continue
            }
            val productName: String = normalizeText(intersections[i].first.text)
            if (productName.contains("X ") || productName.contains(" X")) {
                //If its a collection of prices like "2 x 16,00" we take the name of the previous one
                val previousProduct = intersections.getOrNull(i-1)
                if (previousProduct != null) productName == normalizeText(previousProduct.first.text)
                nextIsNotProduct = true
            }

            //Tries to instantiate product
            try {
                val product = Product(productName, normalizeText(intersections[i].second.text).toFloat())
                products.add(product)

                //If the next intersection is a "RABAT" we take that off the product price and skips it
                val nextProduct = intersections.getOrNull(i+1)
                if (normalizeText(nextProduct?.first?.text ?: "no").contains("RABAT")) {
                    try {
                        val discount = normalizeText(intersections[i+1].second.text).toFloat()
                        product.price -= discount
                    } catch (_: NumberFormatException) {
                        println("Error taking ${normalizeText(intersections[i+1].second.text)} off the original ${product.name}'s price!")
                    }
                }
            } catch (_: NumberFormatException) {
                println("Error converting: ${normalizeText(intersections[i].second.text)} to Float!")
            }
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
        if (anchorTop == Int.MAX_VALUE || anchorBottom == Int.MIN_VALUE) {
            println("Error finding anchors!")
            exitProcess(1)
        }


        //If found we collect every line thats inside the bounding box
        val linesInRange: ArrayList<Line> = ArrayList<Line>()
        for (line in allLines) {
            val lineBox = line.boundingBox!!
            if (lineBox.top >= anchorTop && lineBox.bottom <= anchorBottom) linesInRange.add(line)
        }
        //If no line found between anchors
        if (linesInRange.isEmpty()) {
            println("No lines found between anchorTop and anchorBottom!")
            exitProcess(1)
        }
        //Adds all lines to a list of strings
        return linesInRange
    }

    ///Normalizes the text to uppercase + no danish
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