package weberstudio.app.billigsteprodukter.logic.parsers

import com.google.mlkit.vision.text.Text
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import kotlin.math.ceil

object NettoParser : StoreParser {
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()
    private val fuzzyMatcherLeven = LevenshteinDistance()

    override fun parse(receipt: Text): HashSet<Product> {
        val products: HashSet<Product> = HashSet<Product>()

        


        if (products.isEmpty()) {
            throw ParsingException("Productlist is empty!")
        }

        //region Filtering and returning
        //Hvis der er mere end to produkter (så ét produkt og ét stopord), så gemmer vi alle dem som har den samme pris som stop ordet (Så hvis "Total" fucker f.eks.)
        val stopWordPrices = if (products.size > 2) {
            products
                .filter { isStopWord(it.name) }
                .map { it.price }
                .toSet()
        } else {
            emptySet()
        }


        //Returner kun produkter som ikke er stop ordet, eller som har den samme pris som stop ordet (så hvis "Total" fucker f.eks.)
        val filteredList = products.filter { product -> !isStopWord(product.name) && product.price !in stopWordPrices }.toHashSet()

        return filteredList
        //endregion
    }

    /**
     * Normalizes the text to uppercase + no danish
     */
        private fun normalizeText(text: String): String {
            //TODO: Det her skal standardiseres igennem hele codebasen og ændres til Compose best practice
            return text
                .replace(Regex("[^A-Za-z0-9 ,.]"), "") //Limits to a-z, digits and whitespaces
                .uppercase()
                .replace("Æ", "AE")
                .replace("Ø", "OE")
                .replace("Å", "AA")
                .replace(",", ".") //Ændrer "12,50" til "12.50" så vi kan parse korrekt senere
                .trim()
        }

    /**
     * Returns whether the two given lines intersect
     */

    /**
     * Checks and see if word given as argument is a stop word using fuzzy search
     */
    private fun isStopWord(input: String): Boolean {
        val stopWords = listOf("TOTAL", "BETALINGSKORT")

        return stopWords.any { stopWord ->
            val jaroSimilarity = fuzzyMatcherJaro.apply(input, stopWord) ?: 0.0
            if (jaroSimilarity >= 0.80) {
                return true
            } //Higher = Fewer false positive

            val levenSimilarity = fuzzyMatcherLeven.apply(input, stopWord)
            if (levenSimilarity <= 2) {
                return true
            } //LMAO good luck adjusting lel

            false
        }
    }

    /**
     * Checks and see if word given as argument is a start word using fuzzy search
     */
    private fun isStartWord(input: String): Boolean {
        val stopWords = listOf("NETTO")

        return stopWords.any { stopWord ->
            val jaroSimilarity = fuzzyMatcherJaro.apply(input, stopWord) ?: 0.0
            if (jaroSimilarity >= 0.85) {
                return true
            } //Higher = Fewer false positive


            val maxEdits = ceil(stopWord.length * 0.5).toInt() ////Lower = Fewer false positive
            val levenSimilarity = fuzzyMatcherLeven.apply(input, stopWord) ?: Int.MAX_VALUE
            if (levenSimilarity <= maxEdits) {
                return true
            }

            false
        }
    }

    override fun toString(): String {
        return "Netto"
    }
}