package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import org.apache.commons.lang3.NotImplementedException
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText

object FoetexParser : StoreParser {
    override fun parse(receipt: Text): ParsedImageText {
        throw NotImplementedException("$this scanner ikke implementeret endnu!")
    }

    override fun toString(): String {
        return "Føtex"
    }
}