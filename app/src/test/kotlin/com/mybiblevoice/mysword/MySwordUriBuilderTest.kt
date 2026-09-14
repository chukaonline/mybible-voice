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
    fun `a requested translation switches to the text book-code form`() {
        // Confirmed against a real MySword install: the numeric form silently drops both the
        // verse and the translation when a "/TRANSLATION" suffix is appended to it, so a
        // translation request must use "BookCode_chapter_verse/TRANSLATION" instead.
        val kjv = TranslationRegistry.translations.first { it.id == "kjv" }
        val reference = BibleReference(book = book("john"), chapter = 3, startVerse = 16, translation = kjv)
        assertEquals("https://mysword.info/b?r=Joh_3_16/KJV", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `a requested translation on a numbered book uses its 3-letter code`() {
        val kjv = TranslationRegistry.translations.first { it.id == "kjv" }
        val reference = BibleReference(
            book = book("1_corinthians"), chapter = 9, startVerse = 23, translation = kjv
        )
        assertEquals("https://mysword.info/b?r=1Co_9_23/KJV", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `a requested translation with a verse range uses the text form`() {
        val kjv = TranslationRegistry.translations.first { it.id == "kjv" }
        val reference = BibleReference(
            book = book("psalms"), chapter = 37, startVerse = 3, endVerse = 6, translation = kjv
        )
        assertEquals("https://mysword.info/b?r=Psa_37_3-6/KJV", MySwordUriBuilder.buildUri(reference))
    }

    @Test
    fun `last book Revelation resolves to book number 66`() {
        val reference = BibleReference(book = book("revelation"), chapter = 1, startVerse = 1)
        assertEquals("https://mysword.info/b?r=66.1.1", MySwordUriBuilder.buildUri(reference))
    }
}
