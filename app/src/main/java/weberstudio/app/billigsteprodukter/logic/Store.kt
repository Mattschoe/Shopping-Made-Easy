package weberstudio.app.billigsteprodukter.logic

import androidx.annotation.DrawableRes
import weberstudio.app.billigsteprodukter.R

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

        val Store.topAnchors: List<String>
            get() = when (this) {
                Coop365 -> listOf("365", "365 DISCOUNT", "DET RIGTIGE STED AT SPARE")
                Netto -> listOf("NETTO")
                Rema1000 -> listOf("REMA", "REMA1000")
                Bilka -> listOf("BILKA")
                Foetex -> listOf("FØTEX", "FOETEX")
                Menu -> listOf("MENU")
                Lidl -> listOf("LIDL", "BONKOPI")
                SuperBrugsen -> listOf("SUPERBRUGSEN", "BRUGSEN")
            }

        val Store.bottomAnchors: List<String>
            get() = when (this) {
                Coop365 -> listOf("AT BETALE", "VISA", "BETALINGSKORT")
                Netto -> listOf("AT BETALE", "TOTAL", "KONTANT")
                Rema1000 -> listOf("AT BETALE", "TOTAL")
                Bilka -> listOf("AT BETALE", "TOTAL")
                Foetex -> listOf("AT BETALE", "TOTAL")
                Menu -> listOf("AT BETALE", "TOTAL")
                Lidl -> listOf("SUM", "KORT")
                SuperBrugsen -> listOf("AT BETALE", "TOTAL")
            }
    }
}


