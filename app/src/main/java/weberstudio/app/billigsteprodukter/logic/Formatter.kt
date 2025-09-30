package weberstudio.app.billigsteprodukter.logic

import java.time.Month

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
     * Often used in combination with [formatInputToDanishCurrency].
     * Will return emptyString if number is not valid
     */
    fun filterInputToValidNumberInput(input: String): String {
        val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }

        if (filtered.count { it == ',' } <= 1) {
            val parts = filtered.split(",")
            val validInput = if (parts.size > 1 && parts[1].length > 2) {
                "${parts[0]},${parts[1].take(2)}"
            } else {
                filtered
            }
            return validInput
        }
        return ""
    }

    /**
     * Formats raw numbers in strings from input fields to danish pretty looking strings that match danish standard
     * "1234" -> "1.234"
     * "12345" -> "12.345"
     * "1234567" -> "1.234.567"
     */
    fun formatInputToDanishCurrency(input: String): String {
        if (input.isEmpty()) return ""

        val parts = input.split(",")
        val intPart = parts[0].replace(".", "")
        val decimalPart = if (parts.size > 1) parts[1] else null

        val formattedInt = if (intPart.length > 3) {
            intPart.reversed()
                .chunked(3)
                .joinToString(".")
                .reversed()
        } else {
            intPart
        }

        if (decimalPart != null) return "$formattedInt,$decimalPart"
        if (input.endsWith(",")) return "$formattedInt,"
        else return formattedInt
    }

    /**
     * Formats the output of [formatInputToDanishCurrency] back into a float
     */
    fun danishCurrencyToFloat(danishCurrency: String): Float {
        if (danishCurrency.isEmpty()) return 0.0f
        return danishCurrency
            .replace(".", "")
            .replace(",", ".")
            .toFloat()
    }
}