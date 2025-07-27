package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.FuzzyMatcher
import weberstudio.app.billigsteprodukter.ui.ParsingState

///Parses the given textblock into the correct store
object ParserFactory {
    val fuzzyMatcher = FuzzyMatcher()

    ///Runs through the entire receipt and finds the correct Store
    fun parseReceipt(receipt: Text): StoreParser? {
        for (block in receipt.textBlocks) {
            for (line in block.lines) {
                val lineText = normalizeText(line.text)

                if (fuzzyMatcher.match("NETTO", listOf("NETTO"), 0.85f, 0.3f)) return NettoParser
                else if (fuzzyMatcher.match("REMA", listOf("REMA"), 0.85f, 0.3f)) return RemaParser
                else if (fuzzyMatcher.match("COOP", listOf("COOP"), 0.85f, 0.3f)) return CoopParser
                else if (fuzzyMatcher.match("MENU", listOf("MENU"), 0.85f, 0.3f)) return MenuParser
                else if (fuzzyMatcher.match("LIDL", listOf("LIDL"), 0.85f, 0.3f)) return LidlParser
                else if (fuzzyMatcher.match("SUPERBRUGSEN", listOf("SUPERBRUGSEN"), 0.85f, 0.3f)) return SuperBrugsenParser
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