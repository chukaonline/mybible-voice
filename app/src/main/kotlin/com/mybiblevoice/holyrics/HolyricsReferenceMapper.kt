package com.mybiblevoice.holyrics

import com.mybiblevoice.domain.bible.BibleReference

/**
 * Converts a [BibleReference] into the human-readable reference string Holyrics' ShowVerse
 * action expects (its `references` field, e.g. "John 3:16" - confirmed against the official
 * Holyrics API-Server documentation). Always uses the canonical English book name; never the
 * raw speech-recognizer phrase (design section 21).
 */
object HolyricsReferenceMapper {

    fun toReferenceString(reference: BibleReference): String {
        val versePart = when {
            reference.startVerse == null -> ""
            reference.endVerse == null -> ":${reference.startVerse}"
            else -> ":${reference.startVerse}-${reference.endVerse}"
        }
        return "${reference.book.canonicalName} ${reference.chapter}$versePart"
    }
}
