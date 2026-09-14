package com.mybiblevoice.mysword

/**
 * MySword's classic SWORD-style 3-letter book codes, needed for the text-based reference
 * format ("Joh_3_16") - MySword's numeric book.chapter.verse form ("43.3.16") does not combine
 * correctly with a "/TRANSLATION" suffix (confirmed against a real MySword install: appending
 * "/kjv" to a numeric reference silently dropped both the verse and the requested translation).
 *
 * Keyed by the domain's canonical [com.mybiblevoice.domain.bible.BibleBook.id] - MySword-specific
 * identifiers stay in this integration layer, never in the domain model (SRS section 3.1).
 * Each entry below was verified against a real installed MySword by firing the exact Intent/URI
 * at it directly; this list is otherwise unverified for any book not explicitly checked.
 */
object MySwordBookCodes {

    private val codes: Map<String, String> = mapOf(
        "genesis" to "Gen", "exodus" to "Exo", "leviticus" to "Lev", "numbers" to "Num",
        "deuteronomy" to "Deu", "joshua" to "Jos", "judges" to "Jdg", "ruth" to "Rut",
        "1_samuel" to "1Sa", "2_samuel" to "2Sa", "1_kings" to "1Ki", "2_kings" to "2Ki",
        "1_chronicles" to "1Ch", "2_chronicles" to "2Ch", "ezra" to "Ezr", "nehemiah" to "Neh",
        "esther" to "Est", "job" to "Job", "psalms" to "Psa", "proverbs" to "Pro",
        "ecclesiastes" to "Ecc", "song_of_solomon" to "Sol", "isaiah" to "Isa", "jeremiah" to "Jer",
        "lamentations" to "Lam", "ezekiel" to "Eze", "daniel" to "Dan", "hosea" to "Hos",
        "joel" to "Joe", "amos" to "Amo", "obadiah" to "Oba", "jonah" to "Jon", "micah" to "Mic",
        "nahum" to "Nah", "habakkuk" to "Hab", "zephaniah" to "Zep", "haggai" to "Hag",
        "zechariah" to "Zec", "malachi" to "Mal", "matthew" to "Mat", "mark" to "Mar",
        "luke" to "Luk", "john" to "Joh", "acts" to "Act", "romans" to "Rom",
        "1_corinthians" to "1Co", "2_corinthians" to "2Co", "galatians" to "Gal",
        "ephesians" to "Eph", "philippians" to "Phi", "colossians" to "Col",
        "1_thessalonians" to "1Th", "2_thessalonians" to "2Th", "1_timothy" to "1Ti",
        "2_timothy" to "2Ti", "titus" to "Tit", "philemon" to "Phm", "hebrews" to "Heb",
        "james" to "Jam", "1_peter" to "1Pe", "2_peter" to "2Pe", "1_john" to "1Jo",
        "2_john" to "2Jo", "3_john" to "3Jo", "jude" to "Jud", "revelation" to "Rev"
    )

    fun codeFor(bookId: String): String? = codes[bookId]
}
