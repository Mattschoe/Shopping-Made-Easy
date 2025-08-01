package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import kotlin.jvm.Throws

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
}