package com.mybiblevoice.holyrics

import com.mybiblevoice.target.TargetError
import org.json.JSONObject

/** Request/response DTOs isolated from domain models (design section 7). */

data class ShowVerseRequest(val references: String, val version: String? = null) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("references", references)
        if (version != null) put("version", version)
    }
}

/** A Bible version installed in the target Holyrics instance (from GetBibleVersionsV2).
 *  [id] is the `version` field - what must be sent back as ShowVerse's `version` parameter,
 *  not the `key` field (they can differ for shortcuts). Never hard-coded (design section 11):
 *  discovered per-installation, since two Holyrics machines need not share Bible modules. */
data class HolyricsBibleVersion(
    val id: String,
    val title: String,
    val languageId: String?,
    val languageIso: String?
)

data class HolyricsTokenInfo(val holyricsVersion: String, val permissions: List<String>)

data class HolyricsConnectionConfig(val host: String, val port: Int, val token: String)

/** Client-level result: like [Result] but with [TargetError]'s shared error vocabulary
 *  instead of a raw Throwable, since callers need to distinguish network/auth/API failures. */
sealed class HolyricsResult<out T> {
    data class Success<out T>(val value: T) : HolyricsResult<T>()
    data class Failure(val error: TargetError) : HolyricsResult<Nothing>()
}
