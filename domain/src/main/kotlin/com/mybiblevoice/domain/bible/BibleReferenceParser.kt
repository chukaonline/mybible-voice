package com.mybiblevoice.domain.bible

import com.mybiblevoice.domain.parser.ParserResult
import com.mybiblevoice.domain.parser.SpokenNumberParser
import com.mybiblevoice.domain.parser.TextNormalizer
import com.mybiblevoice.domain.translation.TranslationRegistry

/**
 * Converts raw recognized speech (or typed text) into a [ParserResult].
 *
 * Pipeline (SRS section 9.2): normalize -> extract translation -> identify book ->
 * parse chapter/verse expression -> convert spoken numbers -> validate -> result.
 *
 * Never guesses when materially different references are plausible: such input yields
 * [ParserResult.Ambiguous] rather than a launch.
 */
object BibleReferenceParser {

    fun parse(rawInput: String): ParserResult {
        val trimmedInput = rawInput.trim()
        val normalizedTokens = TextNormalizer.normalize(trimmedInput)
        if (normalizedTokens.isEmpty()) {
            return ParserResult.NotBibleReference("No speech was recognized.")
        }

        val (tokensAfterTranslation, translation) = TranslationRegistry.extractTrailing(normalizedTokens)

        val bookMatch = BibleBookRegistry.matchAtStart(tokensAfterTranslation)
            ?: return ParserResult.NotBibleReference(
                "\"$trimmedInput\" was not recognized as a Bible reference."
            )
        val (book, consumedTokenCount) = bookMatch
        val remainderTokens = tokensAfterTranslation.subList(consumedTokenCount, tokensAfterTranslation.size)

        if (remainderTokens.isEmpty()) {
            return ParserResult.Invalid("No chapter was given for ${book.canonicalName}.")
        }

        val numericTokens = SpokenNumberParser.convert(remainderTokens)

        return when (val chapterVerse = parseChapterVerse(numericTokens)) {
            is ChapterVerseParse.Ambiguous -> ParserResult.Ambiguous(chapterVerse.reason)
            is ChapterVerseParse.Invalid -> ParserResult.Invalid(chapterVerse.reason)
            is ChapterVerseParse.Ok -> resolve(book, chapterVerse, translation)
        }
    }

    private fun resolve(
        book: BibleBook,
        chapterVerse: ChapterVerseParse.Ok,
        translation: com.mybiblevoice.domain.translation.Translation?
    ): ParserResult {
        val validation = BibleReferenceValidator.validate(
            book = book,
            chapter = chapterVerse.chapter,
            startVerse = chapterVerse.startVerse,
            endVerse = chapterVerse.endVerse
        )
        return when (validation) {
            is ValidationResult.Invalid -> ParserResult.Invalid(validation.reason)
            ValidationResult.Valid -> ParserResult.Success(
                BibleReference(
                    book = book,
                    chapter = chapterVerse.chapter,
                    startVerse = chapterVerse.startVerse,
                    endVerse = chapterVerse.endVerse,
                    translation = translation
                )
            )
        }
    }

    private sealed class ChapterVerseParse {
        data class Ok(val chapter: Int, val startVerse: Int?, val endVerse: Int?) : ChapterVerseParse()
        data class Ambiguous(val reason: String) : ChapterVerseParse()
        data class Invalid(val reason: String) : ChapterVerseParse()
    }

    /**
     * Interprets the numeric/keyword tokens that follow the book name. Supports:
     * explicit "chapter N verse M[-K]", "N:M[-K]", and compact "N M[to K]" forms
     * (SRS section 9.1). Anything else is rejected rather than guessed at.
     */
    private fun parseChapterVerse(tokens: List<String>): ChapterVerseParse {
        val cleaned = tokens.filter { it.isNotBlank() }
        val numbers = cleaned.mapNotNull { it.toIntOrNull() }
        val phrase = cleaned.joinToString(" ")

        if (numbers.isEmpty()) {
            return ChapterVerseParse.Invalid("No chapter number was recognized in \"$phrase\".")
        }

        val hasChapterWord = cleaned.contains("chapter")
        val hasVerseWord = cleaned.contains("verse")
        val hasColon = cleaned.contains(":")
        val hasRange = cleaned.contains("-")

        return when {
            hasChapterWord || hasVerseWord -> when (numbers.size) {
                1 -> ChapterVerseParse.Ok(numbers[0], null, null)
                2 -> ChapterVerseParse.Ok(numbers[0], numbers[1], null)
                3 -> if (hasRange) {
                    ChapterVerseParse.Ok(numbers[0], numbers[1], numbers[2])
                } else {
                    ambiguous(phrase)
                }
                else -> ambiguous(phrase)
            }
            hasColon -> when (numbers.size) {
                2 -> ChapterVerseParse.Ok(numbers[0], numbers[1], null)
                3 -> if (hasRange) ChapterVerseParse.Ok(numbers[0], numbers[1], numbers[2]) else ambiguous(phrase)
                else -> ChapterVerseParse.Invalid("Could not parse \"$phrase\" as chapter:verse.")
            }
            else -> when {
                numbers.size == 1 -> ChapterVerseParse.Ok(numbers[0], null, null)
                numbers.size == 2 && !hasRange -> ChapterVerseParse.Ok(numbers[0], numbers[1], null)
                numbers.size == 3 && hasRange -> ChapterVerseParse.Ok(numbers[0], numbers[1], numbers[2])
                else -> ambiguous(phrase)
            }
        }
    }

    private fun ambiguous(phrase: String) =
        ChapterVerseParse.Ambiguous("Unclear how \"$phrase\" maps to a chapter and verse.")
}
