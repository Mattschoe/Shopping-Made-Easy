package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.Product

object SuperBrugsenParser: StoreParser {
    override fun parse(receipt: Text): HashSet<Product> {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "SuperBrugsen"
    }
}