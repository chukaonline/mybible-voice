package com.mybiblevoice.domain.parser

/**
 * Normalizes raw recognized speech (or typed text) into a clean token list ready for
 * book/number parsing: lowercased, punctuation isolated, command words and spoken
 * separator synonyms normalized away.
 */
object TextNormalizer {

    private val COMMAND_PHRASES: List<List<String>> = listOf(
        listOf("take", "me", "to"),
        listOf("go", "to"),
        listOf("show", "me"),
        listOf("turn", "to"),
        listOf("open")
    ).sortedByDescending { it.size }

    /** Words that mean "range separator" but should collapse to a single canonical token. */
    private val RANGE_WORD_ALIASES = mapOf(
        "to" to "-",
        "through" to "-",
        "dash" to "-",
        "colon" to ":"
    )

    fun normalize(raw: String): List<String> {
        var text = raw.lowercase().trim()
        text = text.replace(Regex("[:\\-–—]"), " $0 ")
        text = text.replace(Regex("[,.]"), " ")
        text = text.replace(Regex("\\s+"), " ").trim()

        var tokens = if (text.isEmpty()) emptyList() else text.split(" ")
        tokens = stripLeadingCommands(tokens)
        tokens = tokens.map { RANGE_WORD_ALIASES[it] ?: it }
        return tokens
    }

    private fun stripLeadingCommands(tokens: List<String>): List<String> {
        var current = tokens
        var strippedAny: Boolean
        do {
            strippedAny = false
            for (phrase in COMMAND_PHRASES) {
                if (current.size >= phrase.size && current.subList(0, phrase.size) == phrase) {
                    current = current.subList(phrase.size, current.size)
                    strippedAny = true
                    break
                }
            }
        } while (strippedAny && current.isNotEmpty())
        return current
    }
}
