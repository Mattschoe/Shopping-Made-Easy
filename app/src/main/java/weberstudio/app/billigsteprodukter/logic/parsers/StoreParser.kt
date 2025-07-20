package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.Product

interface StoreParser {
    /**
     * @param receipt the full text containing the receipt
     * @return List<Product> a list of products corresponding to the store
     */
    fun parse(receipt: Text): HashSet<Product>

    //Returns the name of the store
    override fun toString(): String
}