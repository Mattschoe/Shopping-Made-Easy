package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import org.apache.commons.lang3.NotImplementedException
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option.Option.*
import weberstudio.app.billigsteprodukter.logic.Formatter
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.Store.*
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher
import kotlin.jvm.Throws

///Parses the given textblock into the correct store
object ParserFactory {
    val fuzzyMatcher = FuzzyMatcher()

    ///Runs through the entire receipt and finds the correct Store
    @Throws(NotImplementedException::class)
    fun parseReceipt(receipt: Text, coop365Option: Coop365Option.Option): StoreParser? {
        val store: Store? = detectStore(receipt)

        return when (store) {
            Bilka -> return BilkaParser
            Coop365 -> {
                return when (coop365Option) {
                    OVER -> CoopParserQuantityAbove
                    UNDER -> CoopParserQuantityBelow
                }
            }
            Foetex -> return FoetexParser
            Lidl -> return LidlParser
            Menu -> return MenuParser
            Netto -> return NettoParser
            Rema1000 -> return RemaParser
            SuperBrugsen -> return SuperBrugsenParser
            else -> return null
        }
    }

    fun detectStore(text: Text): Store? {
        for (block in text.textBlocks) {
            for (line in block.lines) {
                val normalizedText = Formatter.normalizeText(line.text)
                if (fuzzyMatcher.match(normalizedText, listOf("NETTO"), 0.85f, 0.3f)) return Store.Netto
                else if (fuzzyMatcher.match(normalizedText, listOf("365", "365 DISCOUNT", "DET RIGTIGE STED AT SPARE"), 0.85f, 0.3f)) return Store.Coop365
                else if (fuzzyMatcher.match(normalizedText, listOf("BILKA"), 0.85f, 0.3f)) return Store.Bilka
                else if (fuzzyMatcher.match(normalizedText, listOf("REMA"), 0.85f, 0.3f)) return Store.Rema1000
                else if (fuzzyMatcher.match(normalizedText, listOf("MENU"), 0.85f, 0.3f)) return Store.Menu
                else if (fuzzyMatcher.match(normalizedText, listOf("LIDL"), 0.85f, 0.3f)) return Store.Lidl
                else if (fuzzyMatcher.match(normalizedText, listOf("SUPERBRUGSEN", "BRUGSEN"), 0.85f, 0.3f)) return Store.SuperBrugsen
                else if (fuzzyMatcher.match(normalizedText, listOf("FØTEX"), 0.85f, 0.3f)) return Store.Foetex
            }
        }
        return null
    }
}