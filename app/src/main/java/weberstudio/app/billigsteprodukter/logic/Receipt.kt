package weberstudio.app.billigsteprodukter.logic

import java.util.Date

data class Receipt(val belongsToStore: Store, val productsInReceipt: ArrayList<Product>, val dateOnReceipt: Date)
