package com.mybiblevoice.domain.bible

import com.mybiblevoice.domain.parser.ParserResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Required test cases from SRS section 18.2, plus the negative cases called out there:
 * nonexistent books, chapter 0, chapter beyond the book limit, verse 0, reversed ranges,
 * incomplete references, ambiguous speech, and ordinary non-Bible sentences.
 */
class BibleReferenceParserTest {

    private fun parseSuccess(input: String): BibleReference {
        val result = BibleReferenceParser.parse(input)
        assertIs<ParserResult.Success>(result, "Expected Success for \"$input\" but got $result")
        return result.reference
    }

    private fun assertReference(
        input: String,
        book: String,
        chapter: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
        translationId: String? = null
    ) {
        val reference = parseSuccess(input)
        assertEquals(book, reference.book.canonicalName, "book mismatch for \"$input\"")
        assertEquals(chapter, reference.chapter, "chapter mismatch for \"$input\"")
        assertEquals(startVerse, reference.startVerse, "startVerse mismatch for \"$input\"")
        assertEquals(endVerse, reference.endVerse, "endVerse mismatch for \"$input\"")
        assertEquals(translationId, reference.translation?.id, "translation mismatch for \"$input\"")
    }

    // --- SRS 18.2 required positive cases ---

    @Test
    fun `Genesis 1-1`() = assertReference("Genesis 1:1", "Genesis", 1, 1)

    @Test
    fun `John 3-16`() = assertReference("John 3:16", "John", 3, 16)

    @Test
    fun `John chapter 3 verse 16`() = assertReference("John chapter 3 verse 16", "John", 3, 16)

    @Test
    fun `John three sixteen`() = assertReference("John three sixteen", "John", 3, 16)

    @Test
    fun `Romans eight twenty eight`() = assertReference("Romans eight twenty eight", "Romans", 8, 28)

    @Test
    fun `First Corinthians thirteen four to seven`() =
        assertReference("First Corinthians thirteen four to seven", "1 Corinthians", 13, 4, 7)

    @Test
    fun `Second Timothy chapter three verse sixteen`() =
        assertReference("Second Timothy chapter three verse sixteen", "2 Timothy", 3, 16)

    @Test
    fun `Psalm 23`() = assertReference("Psalm 23", "Psalms", 23, null, null)

    @Test
    fun `John 3-16 NLT`() = assertReference("John 3:16 NLT", "John", 3, 16, translationId = "nlt")

    @Test
    fun `Open Genesis 1-1`() = assertReference("Open Genesis 1:1", "Genesis", 1, 1)

    // --- Negative / error cases ---

    @Test
    fun `nonexistent book is not a Bible reference`() {
        assertIs<ParserResult.NotBibleReference>(BibleReferenceParser.parse("Frodo 3:16"))
    }

    @Test
    fun `ordinary non-Bible sentence is not a Bible reference`() {
        assertIs<ParserResult.NotBibleReference>(BibleReferenceParser.parse("What is the weather today"))
    }

    @Test
    fun `chapter 0 is invalid`() {
        assertIs<ParserResult.Invalid>(BibleReferenceParser.parse("Genesis 0"))
    }

    @Test
    fun `chapter beyond book limit is invalid`() {
        assertIs<ParserResult.Invalid>(BibleReferenceParser.parse("Genesis 51"))
    }

    @Test
    fun `verse 0 is invalid`() {
        assertIs<ParserResult.Invalid>(BibleReferenceParser.parse("Genesis 1:0"))
    }

    @Test
    fun `reversed verse range is invalid`() {
        assertIs<ParserResult.Invalid>(BibleReferenceParser.parse("John 3:18-16"))
    }

    @Test
    fun `book with no chapter is an incomplete reference`() {
        assertIs<ParserResult.Invalid>(BibleReferenceParser.parse("Genesis"))
    }

    @Test
    fun `three bare numbers with no range marker is ambiguous`() {
        assertIs<ParserResult.Ambiguous>(BibleReferenceParser.parse("Genesis one two three"))
    }

    @Test
    fun `never returns Success for ambiguous input`() {
        val result = BibleReferenceParser.parse("Genesis one two three")
        assert(result !is ParserResult.Success)
    }
}
