package weberstudio.app.billigsteprodukter.parsers

import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Line
import weberstudio.app.billigsteprodukter.Product
import kotlin.system.exitProcess

object NettoParser : StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        var processedLines: ArrayList<String> = processImageText(receipt)

        //Removes non-products (fx: "3 x 12,50)
        processedLines.removeAll { line -> line.contains(" x") || line.contains("x ") }

        val productStrings = ArrayList<String>()
        val products = HashSet<Product>()
        var lastBlock = 0

        //Iterates through all products and saves them
        for (i in receipt.textBlocks.indices) {
            lastBlock = i
            for (line in receipt.textBlocks[i].lines) {
                if (line.text == "TOTAL" || line.text == "RABAT") {
                    //We have reached end of products
                    break
                }
                productStrings.add(line.text)
            }
        }

        //Iterates through each price and creates Product-objects from the former saved products
        var j = 0 //Next product from productStrings
        for (i in lastBlock until receipt.textBlocks.size) {
            for (line in receipt.textBlocks[i].lines) {
                if (line.text.first().isLetter()) continue //Skips if its not a number
                //If it is a number tries to parse to float and instantiate product
                try {
                    products.add(Product(productStrings[j], line.text.toFloat()))
                    j++
                } catch (_: NumberFormatException) {
                    println("Error converting: ${line.text} to float!")
                }
            }
        }
        return products
    }

    ///Processes the image text so we later only parse the products and not the full receipt
    private fun processImageText(originalText: Text): ArrayList<String> {
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
        val linesText: ArrayList<String> = ArrayList<String>()
        for (line in linesInRange) {
            linesText.add(line.text)
        }
        return linesText
    }

    ///Normalizes the text to uppercase + no danish
    private fun normalizeText(text: String): String {
        text
            .replace(Regex("[^A-Za-z0-9 ]"), "") //Limits to a-z, digits and whitespaces
            .uppercase()
            .replace("Æ", "AE")
            .replace("Ø", "OE")
            .replace("Å", "AA")
            .trim()
        return text
    }

    override fun toString(): String {
        return "Netto"
    }
}