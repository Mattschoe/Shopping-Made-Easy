package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText

object MenuParser: StoreParser {
    override fun parse(receipt: Text): ParsedImageText {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Rema1000"
    }
}