package com.mybiblevoice.domain.translation

/**
 * A Bible translation MyBible Voice can request from MySword.
 *
 * [myswordCode] is a best-effort default; per the technical design, MySword module codes
 * must be verified against a real MySword installation during integration testing.
 */
data class Translation(
    val id: String,
    val name: String,
    val myswordCode: String,
    val aliases: List<String>
)
