package com.mybiblevoice.mysword

import com.mybiblevoice.domain.bible.BibleBookRegistry
import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.domain.translation.TranslationRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class MySwordUriBuilderTest {

    private fun book(id: String) = BibleBookRegistry.findById(id)!!

    @Test
    fun `chapter and verse matches MySword's documented John 3-16 example`() {
        val reference = BibleReference(book = book("john"), chapter = 3, startVerse = 16)
        assertEquals("https://mysword.info/b?r=43.3.16", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `verse range matches MySword's documented Psalms 37-3-6 example`() {
        val reference = BibleReference(book = book("psalms"), chapter = 37, startVerse = 3, endVerse = 6)
        assertEquals("https://mysword.info/b?r=19.37.3-6", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `chapter-only reference omits the verse segment`() {
        val reference = BibleReference(book = book("genesis"), chapter = 1)
        assertEquals("https://mysword.info/b?r=1.1", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `translation is appended as a suffix`() {
        val nlt = TranslationRegistry.translations.first { it.id == "nlt" }
        val reference = BibleReference(book = book("john"), chapter = 3, startVerse = 16, translation = nlt)
        assertEquals("https://mysword.info/b?r=43.3.16/nlt", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `last book Revelation resolves to book number 66`() {
        val reference = BibleReference(book = book("revelation"), chapter = 1, startVerse = 1)
        assertEquals("https://mysword.info/b?r=66.1.1", MySwordUriBuilder.buildUri(reference))
    }
}
