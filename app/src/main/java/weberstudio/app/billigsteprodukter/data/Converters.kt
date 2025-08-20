package weberstudio.app.billigsteprodukter.data

import androidx.room.TypeConverter
import weberstudio.app.billigsteprodukter.logic.Store

class Converters {
    @TypeConverter
    fun fromStore(store: Store): Int {
        return store.ID
    }

    @TypeConverter
    fun toStore(storeID: Int): Store? {
        return Store.Companion.fromID(storeID)
    }
}