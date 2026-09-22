package com.mybiblevoice.holyrics

import com.mybiblevoice.target.TargetError

/** Thin wrapper so the Settings UI can discover installed Holyrics Bible versions without
 *  knowing about [HolyricsApi]/[HolyricsResult] directly (design section 7). */
class HolyricsVersionRepository(private val apiClient: HolyricsApi) {

    suspend fun fetchAvailableVersions(config: HolyricsConnectionConfig): Result<List<HolyricsBibleVersion>> {
        return when (val result = apiClient.getBibleVersions(config)) {
            is HolyricsResult.Success -> Result.success(result.value)
            is HolyricsResult.Failure -> Result.failure(HolyricsRequestException(result.error))
        }
    }
}

class HolyricsRequestException(val error: TargetError) : Exception(error.message)
