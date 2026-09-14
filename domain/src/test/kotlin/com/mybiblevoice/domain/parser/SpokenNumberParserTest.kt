package com.mybiblevoice.domain.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class SpokenNumberParserTest {

    @Test
    fun `single number words convert to digits`() {
        assertEquals(listOf("16"), SpokenNumberParser.convert(listOf("sixteen")))
    }

    @Test
    fun `compound tens-and-ones form is one number`() {
        assertEquals(listOf("28"), SpokenNumberParser.convert(listOf("twenty", "eight")))
    }

    @Test
    fun `ones then tens is two separate numbers`() {
        // "Romans eight twenty eight" -> chapter 8, verse 28
        assertEquals(listOf("8", "28"), SpokenNumberParser.convert(listOf("eight", "twenty", "eight")))
    }

    @Test
    fun `teens then ones is two separate numbers`() {
        // "...thirteen four..." -> 13, 4 (not one number)
        assertEquals(listOf("13", "4"), SpokenNumberParser.convert(listOf("thirteen", "four")))
    }

    @Test
    fun `three sixteen is two numbers`() {
        assertEquals(listOf("3", "16"), SpokenNumberParser.convert(listOf("three", "sixteen")))
    }

    @Test
    fun `hundred multiplier combines with tens and ones`() {
        assertEquals(listOf("119"), SpokenNumberParser.convert(listOf("one", "hundred", "nineteen")))
        assertEquals(listOf("150"), SpokenNumberParser.convert(listOf("one", "hundred", "fifty")))
    }

    @Test
    fun `digits pass through unchanged`() {
        assertEquals(listOf("3", "16"), SpokenNumberParser.convert(listOf("3", "16")))
    }

    @Test
    fun `non-number tokens are preserved in place`() {
        assertEquals(
            listOf("chapter", "3", "verse", "16"),
            SpokenNumberParser.convert(listOf("chapter", "three", "verse", "sixteen"))
        )
    }

    @Test
    fun `range word between compound numbers stays untouched`() {
        assertEquals(
            listOf("13", "4", "-", "7"),
            SpokenNumberParser.convert(listOf("thirteen", "four", "-", "seven"))
        )
    }
}
