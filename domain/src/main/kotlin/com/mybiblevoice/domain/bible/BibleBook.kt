package com.mybiblevoice.domain.bible

data class BibleBook(
    val id: String,
    val canonicalName: String,
    val chapters: Int
)
