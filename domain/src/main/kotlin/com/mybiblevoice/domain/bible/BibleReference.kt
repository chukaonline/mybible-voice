package com.mybiblevoice.domain.bible

import com.mybiblevoice.domain.translation.Translation

data class BibleReference(
    val book: BibleBook,
    val chapter: Int,
    val startVerse: Int? = null,
    val endVerse: Int? = null,
    val translation: Translation? = null
)
