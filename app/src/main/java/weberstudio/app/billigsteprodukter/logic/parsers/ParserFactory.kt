package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher

///Parses the given textblock into the correct store
object ParserFactory {
    val fuzzyMatcher = FuzzyMatcher()

    ///Runs through the entire receipt and finds the correct Store
    fun parseReceipt(receipt: Text): StoreParser? {
        for (block in receipt.textBlocks) {
            for (line in block.lines) {
                val lineWords = normalizeText(line.text).split(" ")
                for (word in lineWords) {
                    if (fuzzyMatcher.match(word, listOf("NETTO"), 0.85f, 0.3f)) return NettoParser
                    else if (fuzzyMatcher.match(word, listOf("REMA"), 0.85f, 0.3f)) return RemaParser
                    else if (fuzzyMatcher.match(word, listOf("365", "365 DISCOUNT"), 0.85f, 0.3f)) return CoopParserQuantityBelow
                    else if (fuzzyMatcher.match(word, listOf("MENU"), 0.85f, 0.3f)) return MenuParser
                    else if (fuzzyMatcher.match(word, listOf("LIDL"), 0.85f, 0.3f)) { return LidlParser }
                    else if (fuzzyMatcher.match(word, listOf("SUPERBRUGSEN", "BRUGSEN"), 0.85f, 0.3f)) return SuperBrugsenParser
                }
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