package com.mybiblevoice.holyrics

import com.mybiblevoice.target.TargetError
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HolyricsApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: HolyricsApiClient
    private lateinit var config: HolyricsConnectionConfig

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = HolyricsApiClient()
        config = HolyricsConnectionConfig(host = server.hostName, port = server.port, token = "abc123")
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    // Block bodies (not "= runBlocking { ... }") on purpose: if the last statement inside
    // runBlocking were an assertIs<T> call, the block's inferred type would be T (assertIs
    // returns the narrowed value), making the whole test function return non-Unit - which
    // JUnit4 rejects with "should be void".

    @Test
    fun `showVerse success`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":"ok"}"""))

            val result = client.showVerse(config, ShowVerseRequest(references = "John 3:16"))
            assertIs<HolyricsResult.Success<Unit>>(result)

            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/ShowVerse?token=abc123", request.path)
            assertEquals("""{"references":"John 3:16"}""", request.body.readUtf8())
        }
    }

    @Test
    fun `getBibleVersions parses the documented response shape`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "status": "ok",
                      "data": [
                        {
                          "key": "en_kjv",
                          "version": "en_kjv",
                          "title": "King James Version",
                          "language": {"id": "en", "iso": "en", "name": "English", "alt_name": "English"}
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )

            val result = client.getBibleVersions(config)
            assertIs<HolyricsResult.Success<List<HolyricsBibleVersion>>>(result)
            assertEquals(1, result.value.size)
            assertEquals("en_kjv", result.value[0].id)
            assertEquals("King James Version", result.value[0].title)
            assertEquals("en", result.value[0].languageIso)
        }
    }

    @Test
    fun `getTokenInfo parses version and permissions`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"status":"ok","data":{"version":"2.25.0","permissions":"ShowVerse,GetBibleVersionsV2"}}"""
                )
            )

            val result = client.getTokenInfo(config)
            assertIs<HolyricsResult.Success<HolyricsTokenInfo>>(result)
            assertEquals("2.25.0", result.value.holyricsVersion)
            assertEquals(listOf("ShowVerse", "GetBibleVersionsV2"), result.value.permissions)
        }
    }

    @Test
    fun `HTTP 401 maps to Authentication failure`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(401).setBody("""{"status":"error"}"""))

            val result = client.showVerse(config, ShowVerseRequest(references = "John 3:16"))
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Authentication>(result.error)
        }
    }

    @Test
    fun `error body mentioning token maps to Authentication failure`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":"error","error":"invalid token"}"""))

            val result = client.showVerse(config, ShowVerseRequest(references = "John 3:16"))
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Authentication>(result.error)
        }
    }

    @Test
    fun `error body unrelated to auth maps to Unexpected failure`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":"error","error":"module not found"}"""))

            val result = client.showVerse(config, ShowVerseRequest(references = "John 3:16"))
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Unexpected>(result.error)
        }
    }

    @Test
    fun `malformed JSON body maps to Unexpected failure`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("not json"))

            val result = client.showVerse(config, ShowVerseRequest(references = "John 3:16"))
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Unexpected>(result.error)
        }
    }

    @Test
    fun `unreachable server maps to Network failure`() {
        runBlocking {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val fastTimeoutClient = HolyricsApiClient(
                OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.MILLISECONDS)
                    .readTimeout(200, TimeUnit.MILLISECONDS)
                    .build()
            )

            val result = fastTimeoutClient.showVerse(config, ShowVerseRequest(references = "John 3:16"))
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Network>(result.error)
        }
    }

    @Test
    fun `malformed host fails as a Configuration error instead of crashing`() {
        // Regression test: a mistyped/garbled host (e.g. "92.161..99te1toktn231a9a.162.1.99")
        // made OkHttp's Request.Builder().url() throw IllegalArgumentException, which was
        // thrown outside the try/catch that only caught IOException - crashing the app
        // instead of surfacing a Configuration error.
        runBlocking {
            val result = client.showVerse(
                config.copy(host = "92.161..99te1toktn231a9a.162.1.99"),
                ShowVerseRequest(references = "John 3:16")
            )
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Configuration>(result.error)
        }
    }

    @Test
    fun `blank host fails validation without making an HTTP call`() {
        runBlocking {
            val result = client.showVerse(
                config.copy(host = ""),
                ShowVerseRequest(references = "John 3:16")
            )
            assertIs<HolyricsResult.Failure>(result)
            assertIs<TargetError.Configuration>(result.error)
            assertEquals(0, server.requestCount)
        }
    }

    @Test
    fun `network failure message never contains the token`() {
        runBlocking {
            // Port 1 is a privileged port nothing is listening on - connection is refused
            // without needing to tear down the shared MockWebServer mid-test.
            val result = client.showVerse(
                config.copy(port = 1),
                ShowVerseRequest(references = "John 3:16")
            )
            assertIs<HolyricsResult.Failure>(result)
            assertEquals(false, result.error.message.contains(config.token))
        }
    }
}
