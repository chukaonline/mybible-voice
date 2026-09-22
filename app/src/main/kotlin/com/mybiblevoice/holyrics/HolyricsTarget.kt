package com.mybiblevoice.holyrics

import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.target.BibleTarget
import com.mybiblevoice.target.TargetError
import com.mybiblevoice.target.TargetResult

/**
 * Application-facing Holyrics adapter (design section 7). [connectionConfigProvider] and
 * [versionMappingProvider] are injected as functions rather than a concrete settings
 * dependency so this class stays unit-testable without DataStore/Android.
 */
class HolyricsTarget(
    private val apiClient: HolyricsApi,
    private val connectionConfigProvider: suspend () -> HolyricsConnectionConfig,
    private val versionMappingProvider: suspend () -> Map<String, String>
) : BibleTarget {

    override suspend fun open(reference: BibleReference): TargetResult {
        val config = connectionConfigProvider()
        if (config.host.isBlank() || config.token.isBlank()) {
            return TargetResult.Failure(
                TargetError.Configuration("Configure the Holyrics host and API token in Settings first.")
            )
        }

        // A translation named IN SPEECH/settings must map to an installed Holyrics version or
        // the request fails outright - it is never silently dropped or substituted
        // (design section 10; SRS HT-REQ-033/AC-04).
        val versionId = reference.translation?.let { translation ->
            val mapping = versionMappingProvider()[translation.id]
                ?: return TargetResult.Failure(
                    TargetError.TranslationUnavailable(
                        "${translation.name} is not mapped to a Holyrics Bible version. " +
                            "Map it in Settings first."
                    )
                )
            mapping
        }

        val referenceText = HolyricsReferenceMapper.toReferenceString(reference)
        val request = ShowVerseRequest(references = referenceText, version = versionId)

        return when (val result = apiClient.showVerse(config, request)) {
            is HolyricsResult.Success -> TargetResult.Success()
            is HolyricsResult.Failure -> TargetResult.Failure(result.error)
        }
    }
}
