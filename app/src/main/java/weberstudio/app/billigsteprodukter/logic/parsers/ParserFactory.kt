package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.ui.ParsingState

///Parses the given textblock into the correct store
object ParserFactory {
    ///Runs through the entire receipt and finds the correct Store
    fun parseReceipt(receipt: Text): StoreParser? {
        for (block in receipt.textBlocks) {
            for (line in block.lines) {
                val lineText = normalizeText(line.text)
                if (lineText.contains("ETTO")) return NettoParser
                else if (lineText == "REMA") return RemaParser
                else if (lineText == "COOP") return CoopParser
                else if (lineText == "MENU") return MenuParser
                else if (lineText == "LIDL") return LidlParser
                else if (lineText == "SUPERBRUGSEN") return SuperBrugsenParser
            }
        }
        return null
    }

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
}