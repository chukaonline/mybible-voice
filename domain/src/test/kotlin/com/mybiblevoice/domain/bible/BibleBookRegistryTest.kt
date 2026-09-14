package com.mybiblevoice.domain.bible

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BibleBookRegistryTest {

    @Test
    fun `has all 66 canonical books`() {
        assertEquals(66, BibleBookRegistry.books.size)
        assertEquals(66, BibleBookRegistry.books.map { it.id }.toSet().size)
    }

    @Test
    fun `matches full canonical name`() {
        val (book, consumed) = BibleBookRegistry.matchAtStart(listOf("genesis", "1"))!!
        assertEquals("Genesis", book.canonicalName)
        assertEquals(1, consumed)
    }

    @Test
    fun `matches ordinal-prefixed numbered book`() {
        val (book, consumed) = BibleBookRegistry.matchAtStart(
            listOf("first", "corinthians", "thirteen")
        )!!
        assertEquals("1 Corinthians", book.canonicalName)
        assertEquals(2, consumed)
    }

    @Test
    fun `matches digit-prefixed numbered book`() {
        val (book, consumed) = BibleBookRegistry.matchAtStart(listOf("2", "timothy", "3"))!!
        assertEquals("2 Timothy", book.canonicalName)
        assertEquals(2, consumed)
    }

    @Test
    fun `matches speech-recognizer ordinal form like 1st corinthians`() {
        // Android's speech recognizer commonly transcribes "First Corinthians" as "1st corinthians".
        val (book, consumed) = BibleBookRegistry.matchAtStart(listOf("1st", "corinthians", "9"))!!
        assertEquals("1 Corinthians", book.canonicalName)
        assertEquals(2, consumed)
    }

    @Test
    fun `matches 2nd and 3rd ordinal forms`() {
        assertEquals("2 Peter", BibleBookRegistry.matchAtStart(listOf("2nd", "peter", "1"))!!.first.canonicalName)
        assertEquals("3 John", BibleBookRegistry.matchAtStart(listOf("3rd", "john", "1"))!!.first.canonicalName)
    }

    @Test
    fun `psalm singular alias resolves to canonical Psalms`() {
        val (book, consumed) = BibleBookRegistry.matchAtStart(listOf("psalm", "23"))!!
        assertEquals("Psalms", book.canonicalName)
        assertEquals(1, consumed)
    }

    @Test
    fun `three-word alias song of solomon matches longest form first`() {
        val (book, consumed) = BibleBookRegistry.matchAtStart(
            listOf("song", "of", "solomon", "1")
        )!!
        assertEquals("Song of Solomon", book.canonicalName)
        assertEquals(3, consumed)
    }

    @Test
    fun `plain john resolves to the Gospel, not an epistle`() {
        val (book, _) = BibleBookRegistry.matchAtStart(listOf("john", "3"))!!
        assertEquals("John", book.canonicalName)
    }

    @Test
    fun `first john resolves to the epistle`() {
        val (book, _) = BibleBookRegistry.matchAtStart(listOf("first", "john", "1"))!!
        assertEquals("1 John", book.canonicalName)
    }

    @Test
    fun `unknown word does not match any book`() {
        assertNull(BibleBookRegistry.matchAtStart(listOf("banana", "1")))
    }
}
