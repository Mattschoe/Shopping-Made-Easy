package weberstudio.app.billigsteprodukter.logic

import androidx.annotation.DrawableRes
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.ui.components.StoreImage

enum class Store(
    val ID: Int,
    @DrawableRes val image: Int,
    val contentDescription: String
) {
    Bilka(1, R.drawable.storelogo_bilka, "Bilka Logo"),
    Coop365(2, R.drawable.storelogo_coop365, "Coop365 Logo"),
    Foetex(3, R.drawable.storelogo_foetex, "Føtex Logo"),
    Lidl(4, R.drawable.storelogo_lidl, "Lidl Logo"),
    Menu(5, R.drawable.storelogo_menu, "Menu Logo"),
    Netto(6, R.drawable.storelogo_netto, "Netto Logo"),
    Rema1000(7, R.drawable.storelogo_rema1000, "Rema 1000 Logo"),
    SuperBrugsen(8, R.drawable.storelogo_superbrugsen, "SuperBrugsen Logo");

    companion object {
        /**
         * Returns the store enum of the store name given as argument
         * @param storeName the name of the store
         */
        fun fromName(storeName: String): Store? =
            Store.entries.firstOrNull() { it.name.equals(storeName, ignoreCase = true) }

        /**
         * Returns the store enum from the ID given as argument
         */
        fun fromID(ID: Int): Store? =
            Store.entries.firstOrNull() { it.ID == ID }
    }


}


