package com.mybiblevoice.domain.bible

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BibleReferenceValidatorTest {

    private val genesis = BibleBookRegistry.findById("genesis")!!

    @Test
    fun `chapter 1 of Genesis is valid`() {
        assertEquals(ValidationResult.Valid, BibleReferenceValidator.validate(genesis, 1, null, null))
    }

    @Test
    fun `chapter 0 is invalid`() {
        assertIs<ValidationResult.Invalid>(BibleReferenceValidator.validate(genesis, 0, null, null))
    }

    @Test
    fun `chapter beyond book limit is invalid`() {
        assertIs<ValidationResult.Invalid>(BibleReferenceValidator.validate(genesis, 51, null, null))
    }

    @Test
    fun `verse 0 is invalid`() {
        assertIs<ValidationResult.Invalid>(BibleReferenceValidator.validate(genesis, 1, 0, null))
    }

    @Test
    fun `reversed verse range is invalid`() {
        assertIs<ValidationResult.Invalid>(BibleReferenceValidator.validate(genesis, 1, 10, 5))
    }

    @Test
    fun `equal start and end verse is a valid single-verse range`() {
        assertEquals(ValidationResult.Valid, BibleReferenceValidator.validate(genesis, 1, 5, 5))
    }
}
