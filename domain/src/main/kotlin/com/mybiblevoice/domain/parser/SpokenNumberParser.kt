package com.mybiblevoice.domain.parser

/**
 * Converts spoken English number words (e.g. "twenty eight") into digit tokens, leaving
 * every other token untouched. Digits already present (from speech-recognizer numerals)
 * pass through unchanged.
 *
 * Chapter/verse numbers are cardinal, so ordinal words ("first", "second", "third") are
 * intentionally NOT handled here - those are book-number prefixes, handled by
 * [com.mybiblevoice.domain.bible.BibleBookRegistry].
 */
object SpokenNumberParser {

    private val ONES = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9
    )

    private val TEENS = mapOf(
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
        "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19
    )

    private val TENS = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
        "sixty" to 60, "seventy" to 70, "eighty" to 80, "ninety" to 90
    )

    private fun isNumberWord(token: String): Boolean =
        token in ONES || token in TEENS || token in TENS || token == "hundred"

    /**
     * Replaces every maximal run of number-words in [tokens] with one-or-more digit-string
     * tokens (one per distinct number recognized within the run - see [parseSingleNumber]).
     */
    fun convert(tokens: List<String>): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if (isNumberWord(tokens[i])) {
                val (value, nextIndex) = parseSingleNumber(tokens, i)
                result.add(value.toString())
                i = nextIndex
            } else {
                result.add(tokens[i])
                i++
            }
        }
        return result
    }

    /**
     * Parses a single English cardinal number starting at [startIndex], following standard
     * "[hundred-multiplier] [tens] [ones-or-teens]" grammar, and stops as soon as the number
     * is grammatically complete - e.g. "eight twenty eight" is TWO numbers (8, 28), not one.
     */
    private fun parseSingleNumber(tokens: List<String>, startIndex: Int): Pair<Int, Int> {
        var i = startIndex
        var value = 0

        val onesValue = ONES[tokens[i]]
        if (onesValue != null && i + 1 < tokens.size && tokens[i + 1] == "hundred") {
            value += onesValue * 100
            i += 2
        } else if (tokens[i] == "hundred") {
            value += 100
            i += 1
        }

        if (i < tokens.size && tokens[i] in TENS) {
            value += TENS.getValue(tokens[i])
            i += 1
            if (i < tokens.size && tokens[i] in ONES) {
                value += ONES.getValue(tokens[i])
                i += 1
            }
        } else if (i < tokens.size && tokens[i] in TEENS) {
            value += TEENS.getValue(tokens[i])
            i += 1
        } else if (i == startIndex && tokens[i] in ONES) {
            value += ONES.getValue(tokens[i])
            i += 1
        }

        return value to i
    }
}
