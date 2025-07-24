package weberstudio.app.billigsteprodukter.ui.components

import androidx.annotation.DrawableRes
import weberstudio.app.billigsteprodukter.R

enum class StoreImage(@DrawableRes val logoRes: Int) {
    Netto(R.drawable.storelogo_netto),
    Bilka(R.drawable.storelogo_bilka),
    Coop365(R.drawable.storelogo_coop365),
    Foetex(R.drawable.storelogo_foetex),
    Lidl(R.drawable.storelogo_lidl),
    Menu(R.drawable.storelogo_menu),
    Rema1000(R.drawable.storelogo_rema1000),
    SuperBrugsen(R.drawable.storelogo_superbrugsen);

    companion object {
        /**
         * Returns the image of the store given as argument
         * @param storeName the name of the store
         */
        fun fromName(storeName: String): StoreImage? =
            StoreImage.entries.firstOrNull() { it.name.equals(storeName, ignoreCase = true) }
    }
}