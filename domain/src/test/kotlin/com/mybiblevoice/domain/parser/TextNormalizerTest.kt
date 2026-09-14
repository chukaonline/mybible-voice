package com.mybiblevoice.domain.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class TextNormalizerTest {

    @Test
    fun `strips leading open command`() {
        assertEquals(
            listOf("john", "chapter", "3", "verse", "16"),
            TextNormalizer.normalize("Open John chapter 3 verse 16")
        )
    }

    @Test
    fun `strips leading go to command`() {
        assertEquals(
            listOf("romans", "8", ":", "28"),
            TextNormalizer.normalize("Go to Romans 8:28")
        )
    }

    @Test
    fun `strips leading show me command`() {
        assertEquals(
            listOf("first", "corinthians", "thirteen", "four", "-", "seven"),
            TextNormalizer.normalize("Show me First Corinthians thirteen four to seven")
        )
    }

    @Test
    fun `normalizes colon and dash spacing`() {
        assertEquals(
            listOf("john", "3", ":", "16", "-", "18"),
            TextNormalizer.normalize("John 3:16-18")
        )
    }

    @Test
    fun `empty input normalizes to empty token list`() {
        assertEquals(emptyList(), TextNormalizer.normalize("   "))
    }
}
