package com.mybiblevoice.holyrics

import com.mybiblevoice.target.TargetError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Transport layer for Holyrics' local-network API Server (design section 8/13). Knows HTTP,
 * URL construction, JSON, and timeouts; everything else talks to Holyrics only through
 * [HolyricsApi]'s typed methods, never by building a URL/request itself.
 *
 * Endpoint pattern, actions, and the {"status":"ok"|"error",...} response envelope are
 * confirmed against the official documentation (github.com/holyrics/API-Server, README-en.md).
 *
 * The token never appears in any exception message or log line this class produces - every
 * user-facing string is built by hand, never by echoing the request URL or a raw Throwable.
 */
class HolyricsApiClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : HolyricsApi {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun showVerse(
        config: HolyricsConnectionConfig,
        request: ShowVerseRequest
    ): HolyricsResult<Unit> = execute(config, "ShowVerse", request.toJson()) { }

    override suspend fun getBibleVersions(config: HolyricsConnectionConfig): HolyricsResult<List<HolyricsBibleVersion>> =
        execute(config, "GetBibleVersionsV2", JSONObject()) { data ->
            val array = data as? JSONArray ?: JSONArray()
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                val language = item.optJSONObject("language")
                HolyricsBibleVersion(
                    id = item.getString("version"),
                    title = item.optString("title", item.getString("version")),
                    languageId = language?.optString("id")?.takeIf { it.isNotBlank() },
                    languageIso = language?.optString("iso")?.takeIf { it.isNotBlank() }
                )
            }
        }

    override suspend fun getTokenInfo(config: HolyricsConnectionConfig): HolyricsResult<HolyricsTokenInfo> =
        execute(config, "GetTokenInfo", JSONObject()) { data ->
            val obj = data as? JSONObject ?: JSONObject()
            HolyricsTokenInfo(
                holyricsVersion = obj.optString("version"),
                permissions = obj.optString("permissions")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            )
        }

    private fun validateConfig(config: HolyricsConnectionConfig): TargetError.Configuration? {
        return when {
            config.host.isBlank() -> TargetError.Configuration("Holyrics host/IP is not configured.")
            config.port !in 1..65535 -> TargetError.Configuration("Holyrics port is invalid.")
            config.token.isBlank() -> TargetError.Configuration("Holyrics API token is not configured.")
            else -> null
        }
    }

    private suspend fun <T> execute(
        config: HolyricsConnectionConfig,
        action: String,
        body: JSONObject,
        parseData: (Any?) -> T
    ): HolyricsResult<T> = withContext(Dispatchers.IO) {
        validateConfig(config)?.let { return@withContext HolyricsResult.Failure(it) }

        val url = "http://${config.host}:${config.port}/api/$action?token=${config.token}"
        // Request.Builder().url() throws IllegalArgumentException for a malformed host
        // (e.g. a mistyped IP) - this is a bad-input/Configuration problem, not a
        // Network one, and must never escape as an uncaught crash.
        val httpRequest = try {
            Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
        } catch (e: IllegalArgumentException) {
            return@withContext HolyricsResult.Failure(
                TargetError.Configuration("Holyrics host/IP or port is invalid.")
            )
        }

        try {
            httpClient.newCall(httpRequest).execute().use { response ->
                if (response.code == 401 || response.code == 403) {
                    return@withContext HolyricsResult.Failure(
                        TargetError.Authentication("Holyrics rejected the configured credentials.")
                    )
                }

                val bodyText = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(bodyText) }.getOrNull()
                    ?: return@withContext HolyricsResult.Failure(
                        TargetError.Unexpected("Holyrics returned an unexpected response.")
                    )

                if (json.optString("status") == "ok") {
                    return@withContext HolyricsResult.Success(parseData(json.opt("data")))
                }

                val errorText = describeError(json.opt("error"))
                val isAuthError = listOf("token", "permission", "unauthorized").any {
                    errorText.contains(it, ignoreCase = true)
                }
                HolyricsResult.Failure(
                    if (isAuthError) TargetError.Authentication(errorText) else TargetError.Unexpected(errorText)
                )
            }
        } catch (e: IOException) {
            HolyricsResult.Failure(TargetError.Network("Could not reach Holyrics at ${config.host}:${config.port}."))
        }
    }

    private fun describeError(error: Any?): String = when (error) {
        is String -> error
        is JSONObject -> error.optString("message", "Holyrics returned an error.")
        else -> "Holyrics returned an error."
    }
}
