package com.mybiblevoice.holyrics

import org.json.JSONObject

data class HolyricsQrConfig(val host: String, val port: Int, val token: String)

/**
 * Holyrics' API Server settings screen shows a QR code encoding its connection details as
 * JSON, e.g. {"enabled":true,"ips":["10.1.10.187"],"port":8091,"token":"..."}. "ips" can list
 * more than one network interface; we take the first the way Holyrics itself lists first, and
 * the user can still correct it manually if it picks the wrong one.
 */
fun parseHolyricsQrPayload(raw: String): HolyricsQrConfig? = runCatching {
    val json = JSONObject(raw)
    val ips = json.optJSONArray("ips")?.takeIf { it.length() > 0 } ?: return null
    val host = ips.getString(0).takeIf { it.isNotBlank() } ?: return null
    val port = json.optInt("port", -1).takeIf { it in 1..65535 } ?: return null
    val token = json.optString("token").takeIf { it.isNotBlank() } ?: return null
    HolyricsQrConfig(host, port, token)
}.getOrNull()
