package weberstudio.app.billigsteprodukter.parsers

import androidx.annotation.IntegerRes
import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.Product

object NettoParser : StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        val productStrings = ArrayList<String>()
        val productPrices = ArrayList<Float>()
        var noMoreProducts = false

        for (block in receipt.textBlocks) {
            for (line in block.lines) {
                if (line.text == "Du blev betjent af:" || line.text == "Du bley betjent af:") {
                    //We have reached end of products
                    noMoreProducts = true
                }
                if (noMoreProducts) {
                    try {
                        productPrices.add(line.text.toFloat())
                    } catch (e: NumberFormatException) {
                        println("Error parsing ${line.text} to float!")
                    }
                } else if (line.text.first().isLetter() == true) {
                    //If we are dealing with a product we save it in the product list
                    productStrings.add(line.text)
                }
            }
        }
        productStrings.forEach { s -> println(s) }
        productPrices.forEach { f -> println(f) }

        val products = HashSet<Product>()
        return products
    }

    override fun toString(): String {
        return "Netto"
    }
}