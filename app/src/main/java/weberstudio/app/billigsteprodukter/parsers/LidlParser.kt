package weberstudio.app.billigsteprodukter.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.Product

object LidlParser: StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Lidl"
    }
}