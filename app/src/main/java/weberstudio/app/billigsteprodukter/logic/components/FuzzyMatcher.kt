package weberstudio.app.billigsteprodukter.logic

import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance

class FuzzyMatcher {
    val fuzzyMatcherJaro = JaroWinklerSimilarity()
    val fuzzyMatcherLeven = LevenshteinDistance()

    /**
     * Checks and see if word given as argument is a stop word using fuzzy search
     * @param input The input you want compared to the *matchWords*
     * @param matchWords the words you want to match against the *input*
     * @param jaroMaxThreshold the threshold for the Jaro fuzzy matcher (0 - 1) Higher = fewer false positives
     * @param maxEditRatio used for Levenshtein matcher (0.00 - 1.00) Lower = fewer false positives
     */
    fun match(input: String, matchWords: List<String>, jaroMaxThreshold: Float, maxEditRatio: Float): Boolean {
        val normalizedInput = input.uppercase()
        return matchWords.any { matchWord ->
            val jaroSimilarity = fuzzyMatcherJaro.apply(normalizedInput, matchWord) ?: 0.0
            if (jaroSimilarity >= jaroMaxThreshold) {
                return true
            } //Higher = Fewer false positive

            val levenSimilarity = fuzzyMatcherLeven.apply(normalizedInput, matchWord)
            if (levenSimilarity <= maxEditRatio) {
                return true
            }
            false
        }
    }
}