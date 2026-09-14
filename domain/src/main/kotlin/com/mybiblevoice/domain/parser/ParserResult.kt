package com.mybiblevoice.domain.parser

import com.mybiblevoice.domain.bible.BibleReference

sealed class ParserResult {
    data class Success(val reference: BibleReference) : ParserResult()
    data class Ambiguous(val message: String) : ParserResult()
    data class Invalid(val message: String) : ParserResult()
    data class NotBibleReference(val message: String) : ParserResult()
}
