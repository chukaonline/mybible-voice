package com.mybiblevoice.mysword

import com.mybiblevoice.domain.bible.BibleBookRegistry
import com.mybiblevoice.domain.bible.BibleReference

/**
 * Converts a [BibleReference] into MySword's documented external-link URI
 * (mysword.info/news/220-link-or-open-mysword-from-other-apps).
 *
 * Two different reference forms are needed, both verified against a real MySword install:
 * - No translation requested: the numeric "book.chapter.verse[-endVerse]" form (book number =
 *   canonical 1-Genesis..66-Revelation position). Reliable and needs no book-abbreviation table.
 * - Translation requested: the text "BookCode_chapter_verse[-endVerse]/TRANSLATION" form.
 *   The numeric form does NOT combine correctly with a "/TRANSLATION" suffix - appending one
 *   silently dropped both the verse and the requested translation when tested for real.
 */
object MySwordUriBuilder {

    private const val BASE_URL = "https://mysword.info/b?r="

    fun buildUri(reference: BibleReference): String {
        val translation = reference.translation
        val bookCode = translation?.let { MySwordBookCodes.codeFor(reference.book.id) }

        return if (translation != null && bookCode != null) {
            val versePart = versePart(reference, separator = "_")
            "$BASE_URL${bookCode}_${reference.chapter}$versePart/${translation.myswordCode.uppercase()}"
        } else {
            val bookNumber = BibleBookRegistry.books.indexOf(reference.book) + 1
            check(bookNumber > 0) { "Book ${reference.book.id} is not in the canonical registry." }
            val versePart = versePart(reference, separator = ".")
            "$BASE_URL$bookNumber.${reference.chapter}$versePart"
        }
    }

    private fun versePart(reference: BibleReference, separator: String): String = when {
        reference.startVerse == null -> ""
        reference.endVerse == null -> "$separator${reference.startVerse}"
        else -> "$separator${reference.startVerse}-${reference.endVerse}"
    }
}
