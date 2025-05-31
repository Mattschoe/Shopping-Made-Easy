package weberstudio.app.billigsteprodukter.parsers

import com.google.mlkit.vision.text.Text
import weberstudio.app.billigsteprodukter.MVVM.Model
import weberstudio.app.billigsteprodukter.Product

///Parses the given textblock into the correct store
object ParserFactory {
    ///Runs through the entire receipt and finds the correct Store
    fun parseReceipt(receipt: Text): StoreParser? {
        for (block in receipt.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    if (element.text == "Netto") return NettoParser
                    else if (element.text == "Rema") return RemaParser
                    else if (element.text == "Coop") return CoopParser
                    else if (element.text == "Menu") return MenuParser
                    else if (element.text == "Lidl") return LidlParser
                    else if (element.text == "SuperBrugsen") return SuperBrugsenParser
                }
            }
        }
        return null
    }

}