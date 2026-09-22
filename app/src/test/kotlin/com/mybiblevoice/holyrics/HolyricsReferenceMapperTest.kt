package com.mybiblevoice.holyrics

import com.mybiblevoice.domain.bible.BibleBookRegistry
import com.mybiblevoice.domain.bible.BibleReference
import kotlin.test.Test
import kotlin.test.assertEquals

class HolyricsReferenceMapperTest {

    private fun book(id: String) = BibleBookRegistry.findById(id)!!

    @Test
    fun `single verse`() {
        val reference = BibleReference(book = book("john"), chapter = 3, startVerse = 16)
        assertEquals("John 3:16", HolyricsReferenceMapper.toReferenceString(reference))
    }

    @Test
    fun `verse range`() {
        val reference = BibleReference(book = book("john"), chapter = 3, startVerse = 16, endVerse = 18)
        assertEquals("John 3:16-18", HolyricsReferenceMapper.toReferenceString(reference))
    }

    @Test
    fun `chapter only uses the canonical plural book name`() {
        val reference = BibleReference(book = book("psalms"), chapter = 23)
        assertEquals("Psalms 23", HolyricsReferenceMapper.toReferenceString(reference))
    }

    @Test
    fun `numbered book keeps its canonical digit prefix`() {
        val reference = BibleReference(book = book("1_corinthians"), chapter = 13, startVerse = 4, endVerse = 7)
        assertEquals("1 Corinthians 13:4-7", HolyricsReferenceMapper.toReferenceString(reference))
    }
}
