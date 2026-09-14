package com.mybiblevoice.mysword

import com.mybiblevoice.domain.bible.BibleBookRegistry
import com.mybiblevoice.domain.bible.BibleReference

/**
 * Converts a [BibleReference] into MySword's documented external-link URI:
 * "https://mysword.info/b?r=<book>.<chapter>[.<verse>[-<endVerse>]][/<translationCode>]"
 * using the numeric book.chapter.verse form (book number = canonical 1-Genesis..66-Revelation
 * position, confirmed against MySword's own documented examples "19.37.3-6" = Psalms 37:3-6
 * and "43.3.16" = John 3:16 - see SRS section 24 references).
 *
 * [Translation.myswordCode] values are best-effort defaults and, per the technical design,
 * must be verified against a real MySword installation during integration testing.
 */
object MySwordUriBuilder {

    private const val BASE_URL = "https://mysword.info/b?r="

    fun buildUri(reference: BibleReference): String {
        val bookNumber = BibleBookRegistry.books.indexOf(reference.book) + 1
        check(bookNumber > 0) { "Book ${reference.book.id} is not in the canonical registry." }

        val versePart = when {
            reference.startVerse == null -> ""
            reference.endVerse == null -> ".${reference.startVerse}"
            else -> ".${reference.startVerse}-${reference.endVerse}"
        }
        val translationSuffix = reference.translation?.let { "/${it.myswordCode}" } ?: ""

        return "$BASE_URL$bookNumber.${reference.chapter}$versePart$translationSuffix"
    }
}
