package weberstudio.app.billigsteprodukter.logic

import androidx.annotation.DrawableRes
import weberstudio.app.billigsteprodukter.R

enum class Store(
    val ID: Int,
    @DrawableRes val image: Int,
    val contentDescription: String
) {
    Netto(0, R.drawable.storelogo_bilka, "Bilka Logo"),
    Bilka(0, R.drawable.storelogo_coop365, "Coop365 Logo"),
    Coop365(0, R.drawable.storelogo_foetex, "Føtex Logo"),
    Foetex(0, R.drawable.storelogo_lidl, "Lidl Logo"),
    Lidl(0, R.drawable.storelogo_menu, "Menu Logo"),
    Menu(0, R.drawable.storelogo_netto, "Netto Logo"),
    Rema1000(0, R.drawable.storelogo_rema1000, "Rema 1000 Logo"),
    SuperBrugsen(0, R.drawable.storelogo_superbrugsen, "SuperBrugsen Logo"),
}

