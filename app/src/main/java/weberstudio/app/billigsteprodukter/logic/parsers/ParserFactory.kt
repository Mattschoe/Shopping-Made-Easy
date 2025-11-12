package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import org.apache.commons.lang3.NotImplementedException
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option.Option.*
import weberstudio.app.billigsteprodukter.logic.Formatter
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.Store.*
import weberstudio.app.billigsteprodukter.logic.Store.Companion.topAnchors
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
                if (fuzzyMatcher.match(normalizedText, Netto.topAnchors, 0.85f, 0.3f)) return Netto
                else if (fuzzyMatcher.match(normalizedText, Coop365.topAnchors, 0.85f, 0.3f)) return Coop365
                else if (fuzzyMatcher.match(normalizedText, Bilka.topAnchors, 0.85f, 0.3f)) return Bilka
                else if (fuzzyMatcher.match(normalizedText, Rema1000.topAnchors, 0.85f, 0.3f)) return Rema1000
                else if (fuzzyMatcher.match(normalizedText, Menu.topAnchors, 0.85f, 0.3f)) return Menu
                else if (fuzzyMatcher.match(normalizedText, Lidl.topAnchors, 0.85f, 0.3f)) return Lidl
                else if (fuzzyMatcher.match(normalizedText, SuperBrugsen.topAnchors, 0.85f, 0.3f)) return SuperBrugsen
                else if (fuzzyMatcher.match(normalizedText, Foetex.topAnchors, 0.85f, 0.3f)) return Foetex
            }
        }
        return null
    }
}