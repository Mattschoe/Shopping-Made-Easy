package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.Product

object MenuParser: StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Menu"
    }
}