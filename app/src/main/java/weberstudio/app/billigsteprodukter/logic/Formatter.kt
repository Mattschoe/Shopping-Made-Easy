package weberstudio.app.billigsteprodukter.logic

import java.time.Month
import java.util.Locale
import kotlin.math.abs

/**
 * This object has the single focus of formatting and standardizing text UI for the user.
 */
object Formatter {
    /**
     * Extension function for translating Month objects
     */
    fun Month.toDanishString(): String = when(this) {
        Month.JANUARY -> "Januar"
        Month.FEBRUARY -> "Februar"
        Month.MARCH -> "Marts"
        Month.APRIL -> "April"
        Month.MAY -> "Maj"
        Month.JUNE -> "Juni"
        Month.JULY -> "Juli"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "Oktober"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "December"
    }

    /**
     * Filters, Checks and formats user input into a valid number input.
     * Often used in combination with [formatInputToDanishCurrencyStandard].
     * Will return emptyString if number is not valid
     */
    fun filterInputToValidNumberInput(input: String): String {
        val normalized = input.replace('.', ',')
        val filtered = normalized.filter { it.isDigit() || it == ',' }

        if (filtered.count { it == ',' } > 1) return ""

        val parts = filtered.split(",")

        return if (parts.size > 1) parts[0] + "," + parts[1].take(2)
        else filtered
    }

    /**
     * Formats raw numbers in strings from input fields to danish pretty looking strings that match danish standard
     * "1234" -> "1.234"
     * "12345" -> "12.345"
     * "1234567" -> "1.234.567"
     */
    fun formatInputToDanishCurrencyStandard(input: String): String {
        if (input.isEmpty()) return ""

        val parts = input.split(",")
        val intPart = parts[0].filter { it.isDigit() }
        val decimalPart = parts.getOrNull(1)

        val formattedInt = if (intPart.length > 3) intPart.reversed().chunked(3).joinToString(".").reversed()
        else intPart


        return when {
            decimalPart != null && decimalPart.isNotEmpty() -> "$formattedInt,${decimalPart}"
            input.endsWith(",") -> "$formattedInt,"
            else -> formattedInt
        }
    }

    /**
     * Formats a float to danish currency. A lot like [formatInputToDanishCurrencyStandard], but expects a
     * float standard (so '.' instead of ',') when formatting.
     * 1234.95f -> "1.234,95"
     * 2500.0f -> "2.500"
     */
    fun formatFloatToDanishCurrency(value: Float): String {
        val formatted = String.format(Locale.US, "%.2f", value)

        val parts = formatted.split(".")
        val intPart = parts[0]
        var decimalPart = parts.getOrNull(1) ?: ""

        val formattedInt = if (intPart.length > 3) {
            intPart.reversed()
                .chunked(3)
                .joinToString(".")
                .reversed()
        } else {
            intPart
        }

        return if (decimalPart == "00") formattedInt else "$formattedInt,$decimalPart"
    }

    /**
     * Formats the output of [formatInputToDanishCurrencyStandard] back into a float
     */
    fun formatDanishCurrencyToFloat(danishCurrency: String): Float {
        if (danishCurrency.isEmpty()) return 0.0f
        return danishCurrency
            .replace(".", "")
            .replace(",", ".")
            .toFloat()
    }

    fun normalizeText(text: String): String {
        return text
            .replace(Regex("[^A-Za-zÆØÅæøå0-9 ,.]"), "") //Limits to a-z, digits and whitespaces
            .uppercase()
            .replace(",", ".") //Ændrer "12,50" til "12.50" så vi kan parse korrekt senere
            .trim()
    }

    /**
     * Checks if the two floats given are *ish* equal to eachother
     * @param epsilon the difference between the two floats. Shouldn't be given as standard unless
     * for a special reason
     */
    fun Float.isIshEqualTo(other: Float, epsilon: Float = 0.0001f): Boolean {
        if (this.isNaN() || other.isNaN()) return false
        return abs(this - other) <= epsilon
    }
}