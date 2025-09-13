package weberstudio.app.billigsteprodukter.data

import androidx.room.TypeConverter
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year

class Converters {
    @TypeConverter
    fun fromStore(store: Store): Int {
        return store.ID
    }

    @TypeConverter
    fun toStore(storeID: Int): Store? {
        return Store.Companion.fromID(storeID)
    }

    @TypeConverter
    fun fromMonth(month: Month?): Int? {
        return month?.value
    }

    @TypeConverter
    fun toMonth(monthValue: Int?): Month? {
        return monthValue?.let { Month.of(it) }
    }

    @TypeConverter
    fun fromYear(year: Year?): Int? {
        return year?.value
    }

    @TypeConverter
    fun toYear(yearValue: Int?): Year? {
        return yearValue?.let { Year.of(it) }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }
}