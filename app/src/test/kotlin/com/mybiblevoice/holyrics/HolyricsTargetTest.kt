package com.mybiblevoice.holyrics

import com.mybiblevoice.domain.bible.BibleBookRegistry
import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.domain.translation.TranslationRegistry
import com.mybiblevoice.target.TargetError
import com.mybiblevoice.target.TargetResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HolyricsTargetTest {

    private val johnThreeSixteen = BibleReference(book = BibleBookRegistry.findById("john")!!, chapter = 3, startVerse = 16)
    private val validConfig = HolyricsConnectionConfig(host = "192.168.1.50", port = 8091, token = "abc123")

    private class FakeHolyricsApi(
        private val showVerseResult: HolyricsResult<Unit> = HolyricsResult.Success(Unit)
    ) : HolyricsApi {
        var lastShowVerseRequest: ShowVerseRequest? = null

        override suspend fun showVerse(config: HolyricsConnectionConfig, request: ShowVerseRequest): HolyricsResult<Unit> {
            lastShowVerseRequest = request
            return showVerseResult
        }

        override suspend fun getBibleVersions(config: HolyricsConnectionConfig) =
            HolyricsResult.Success(emptyList<HolyricsBibleVersion>())

        override suspend fun getTokenInfo(config: HolyricsConnectionConfig) =
            HolyricsResult.Success(HolyricsTokenInfo("2.25.0", emptyList()))
    }

    // Block bodies (not "= runBlocking { ... }") on purpose: if the last statement inside
    // runBlocking were an assertIs<T> call, the block's inferred type would be T (assertIs
    // returns the narrowed value), making the whole test function return non-Unit - which
    // JUnit4 rejects with "should be void".
    @Test
    fun `missing configuration fails without calling the API`() {
        runBlocking {
            val api = FakeHolyricsApi()
            val target = HolyricsTarget(
                apiClient = api,
                connectionConfigProvider = { HolyricsConnectionConfig(host = "", port = 8091, token = "") },
                versionMappingProvider = { emptyMap() }
            )

            val result = target.open(johnThreeSixteen)
            assertIs<TargetResult.Failure>(result)
            assertIs<TargetError.Configuration>(result.error)
        }
    }

    @Test
    fun `no translation requested sends no version and succeeds`() {
        runBlocking {
            val api = FakeHolyricsApi()
            val target = HolyricsTarget(
                apiClient = api,
                connectionConfigProvider = { validConfig },
                versionMappingProvider = { emptyMap() }
            )

            val result = target.open(johnThreeSixteen)
            assertIs<TargetResult.Success>(result)
            assertEquals("John 3:16", api.lastShowVerseRequest?.references)
            assertEquals(null, api.lastShowVerseRequest?.version)
        }
    }

    @Test
    fun `requested translation resolves to its mapped Holyrics version`() {
        runBlocking {
            val kjv = TranslationRegistry.translations.first { it.id == "kjv" }
            val reference = johnThreeSixteen.copy(translation = kjv)
            val api = FakeHolyricsApi()
            val target = HolyricsTarget(
                apiClient = api,
                connectionConfigProvider = { validConfig },
                versionMappingProvider = { mapOf("kjv" to "en_kjv") }
            )

            val result = target.open(reference)
            assertIs<TargetResult.Success>(result)
            assertEquals("en_kjv", api.lastShowVerseRequest?.version)
        }
    }

    @Test
    fun `requested translation with no mapping fails rather than silently substituting`() {
        runBlocking {
            val kjv = TranslationRegistry.translations.first { it.id == "kjv" }
            val reference = johnThreeSixteen.copy(translation = kjv)
            val api = FakeHolyricsApi()
            val target = HolyricsTarget(
                apiClient = api,
                connectionConfigProvider = { validConfig },
                versionMappingProvider = { emptyMap() }
            )

            val result = target.open(reference)
            assertIs<TargetResult.Failure>(result)
            assertIs<TargetError.TranslationUnavailable>(result.error)
        }
    }

    @Test
    fun `API failure is passed through unchanged`() {
        runBlocking {
            val api = FakeHolyricsApi(
                showVerseResult = HolyricsResult.Failure(TargetError.Network("Could not reach Holyrics."))
            )
            val target = HolyricsTarget(
                apiClient = api,
                connectionConfigProvider = { validConfig },
                versionMappingProvider = { emptyMap() }
            )

            val result = target.open(johnThreeSixteen)
            assertIs<TargetResult.Failure>(result)
            assertIs<TargetError.Network>(result.error)
        }
    }
}
