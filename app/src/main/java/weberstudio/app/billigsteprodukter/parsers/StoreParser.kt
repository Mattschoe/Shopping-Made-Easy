package weberstudio.app.billigsteprodukter.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.Product

interface StoreParser {
    /**
     * @param receipt the full text containing the receipt
     * @return List<Product> a list of products corresponding to the store
     */
    fun parse(receipt: Text): HashSet<Product>

    //Returns the name of the store
    override fun toString(): String
}