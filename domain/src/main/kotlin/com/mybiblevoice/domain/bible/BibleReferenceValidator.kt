package com.mybiblevoice.domain.bible

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

/**
 * Validates chapter/verse numbers against a resolved [BibleBook]. Exact verse-count
 * validation requires per-chapter verse metadata and is intentionally out of scope here
 * (SRS section 10); only book/chapter bounds and basic verse-range sanity are checked.
 */
object BibleReferenceValidator {

    fun validate(book: BibleBook, chapter: Int, startVerse: Int?, endVerse: Int?): ValidationResult {
        if (chapter < 1 || chapter > book.chapters) {
            return ValidationResult.Invalid(
                "${book.canonicalName} has no chapter $chapter (valid range is 1-${book.chapters})."
            )
        }
        if (startVerse != null && startVerse < 1) {
            return ValidationResult.Invalid("Verse must be 1 or greater.")
        }
        if (endVerse != null) {
            if (startVerse == null) {
                return ValidationResult.Invalid("A verse range requires a starting verse.")
            }
            if (endVerse < startVerse) {
                return ValidationResult.Invalid(
                    "Verse range end ($endVerse) cannot be before the start ($startVerse)."
                )
            }
        }
        return ValidationResult.Valid
    }
}
