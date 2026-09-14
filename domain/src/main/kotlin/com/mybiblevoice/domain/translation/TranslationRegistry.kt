package com.mybiblevoice.domain.translation

/**
 * Registry of the initial supported translations (SRS section 11.1) and spoken-alias extraction.
 */
object TranslationRegistry {

    val translations: List<Translation> = listOf(
        Translation("kjv", "King James Version", "kjv", listOf("kjv", "king james version", "king james")),
        Translation("nkjv", "New King James Version", "nkjv", listOf("nkjv", "new king james version", "new king james")),
        Translation("niv", "New International Version", "niv", listOf("niv", "new international version")),
        Translation("nlt", "New Living Translation", "nlt", listOf("nlt", "new living translation")),
        Translation("esv", "English Standard Version", "esv", listOf("esv", "english standard version")),
        Translation("nasb", "New American Standard Bible", "nasb", listOf("nasb", "new american standard bible", "new american standard")),
        Translation("amp", "Amplified Bible", "amp", listOf("amp", "amplified bible", "amplified")),
        Translation("csb", "Christian Standard Bible", "csb", listOf("csb", "christian standard bible", "christian standard")),
        Translation("gnt", "Good News Translation", "gnt", listOf("gnt", "good news translation", "good news bible", "good news")),
        Translation("msg", "The Message", "msg", listOf("msg", "the message", "message")),
        Translation("net", "New English Translation", "net", listOf("net", "new english translation"))
    )

    /** Longest alias we'll ever try to match, in tokens (e.g. "new international version"). */
    private const val MAX_ALIAS_TOKENS = 4

    private val aliasMap: Map<String, Translation> = buildMap {
        for (translation in translations) {
            for (alias in translation.aliases) {
                put(alias.lowercase(), translation)
            }
        }
    }

    private val idMap: Map<String, Translation> = translations.associateBy { it.id }

    fun findById(id: String): Translation? = idMap[id]

    /**
     * Looks for a translation alias at the END of [tokens] (the doc's examples always place
     * the translation last, e.g. "John 3:16 NLT"), preferring the longest alias.
     *
     * @return the remaining tokens (translation removed) paired with the matched translation,
     *   or [tokens] paired with null if none matched.
     */
    fun extractTrailing(tokens: List<String>): Pair<List<String>, Translation?> {
        val maxLen = minOf(MAX_ALIAS_TOKENS, tokens.size)
        for (len in maxLen downTo 1) {
            val candidate = tokens.takeLast(len).joinToString(" ")
            aliasMap[candidate]?.let { translation ->
                return tokens.subList(0, tokens.size - len) to translation
            }
        }
        return tokens to null
    }
}
