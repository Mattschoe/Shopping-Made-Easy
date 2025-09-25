package weberstudio.app.billigsteprodukter.logic.components

import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance

/**
 * Calculates the match score of a given searchquery
 */
object MatchScoreCalculator {
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()

    fun calculate(productName: String, query: String): Int {
        //TODO: Det her skal normalizes helt (se hvordan i parser), og ikke bar trimmes og lowercases
        val productName = productName.lowercase().trim()
        val query = query.lowercase().trim()

        return when {
            productName == query -> 100
            productName.startsWith(query) -> 75
            productName.contains(query) -> 50
            fuzzyMatch(productName, query) -> 25
            else -> 0
        }
    }

    /**
     * Fuzzy matches the productName and query
     */
    private fun fuzzyMatch(productName: String, query: String): Boolean {
        val jaroSimilarity = fuzzyMatcherJaro.apply(productName, query)
        if (jaroSimilarity >= 0.85) return true

        val levenSimilarity = fuzzyMatcherLeven.apply(productName, query)
        return levenSimilarity != -1 && levenSimilarity <= 2
    }
}