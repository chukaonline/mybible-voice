package com.mybiblevoice.domain.translation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TranslationRegistryTest {

    @Test
    fun `findById resolves a known translation`() {
        assertEquals("New Living Translation", TranslationRegistry.findById("nlt")?.name)
    }

    @Test
    fun `findById returns null for an unknown id`() {
        assertNull(TranslationRegistry.findById("does-not-exist"))
    }
}
