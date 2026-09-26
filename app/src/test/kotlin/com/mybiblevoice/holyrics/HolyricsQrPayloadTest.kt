package com.mybiblevoice.holyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HolyricsQrPayloadTest {

    @Test
    fun `parses the real Holyrics QR payload shape`() {
        val result = parseHolyricsQrPayload(
            """{"enabled":true,"ips":["10.1.10.187"],"port":8091,"token":"8s4F0RX3RLUCOEMw"}"""
        )
        assertEquals(HolyricsQrConfig("10.1.10.187", 8091, "8s4F0RX3RLUCOEMw"), result)
    }

    @Test
    fun `takes the first of multiple ips`() {
        val result = parseHolyricsQrPayload(
            """{"ips":["192.168.1.50","10.1.10.187"],"port":8091,"token":"abc123"}"""
        )
        assertEquals("192.168.1.50", result?.host)
    }

    @Test
    fun `missing ips returns null`() {
        assertNull(parseHolyricsQrPayload("""{"port":8091,"token":"abc123"}"""))
    }

    @Test
    fun `empty ips array returns null`() {
        assertNull(parseHolyricsQrPayload("""{"ips":[],"port":8091,"token":"abc123"}"""))
    }

    @Test
    fun `missing port returns null`() {
        assertNull(parseHolyricsQrPayload("""{"ips":["10.1.10.187"],"token":"abc123"}"""))
    }

    @Test
    fun `out-of-range port returns null`() {
        assertNull(parseHolyricsQrPayload("""{"ips":["10.1.10.187"],"port":70000,"token":"abc123"}"""))
    }

    @Test
    fun `missing token returns null`() {
        assertNull(parseHolyricsQrPayload("""{"ips":["10.1.10.187"],"port":8091}"""))
    }

    @Test
    fun `blank token returns null`() {
        assertNull(parseHolyricsQrPayload("""{"ips":["10.1.10.187"],"port":8091,"token":""}"""))
    }

    @Test
    fun `malformed JSON returns null instead of throwing`() {
        assertNull(parseHolyricsQrPayload("not a QR code we recognize"))
    }

    @Test
    fun `arbitrary unrelated QR code returns null`() {
        assertNull(parseHolyricsQrPayload("https://example.com"))
    }
}
