package com.mybiblevoice.holyrics

/** Abstraction over the Holyrics API Server transport, so [HolyricsTarget] is testable without
 *  real HTTP (mirrors how [com.mybiblevoice.speech.SpeechRecognizer]/[com.mybiblevoice.mysword.MySwordLauncher]
 *  separate their Android/network-touching implementation from an interface). */
interface HolyricsApi {
    suspend fun showVerse(config: HolyricsConnectionConfig, request: ShowVerseRequest): HolyricsResult<Unit>
    suspend fun getBibleVersions(config: HolyricsConnectionConfig): HolyricsResult<List<HolyricsBibleVersion>>

    /** Lightweight, side-effect-free call used for Test Connection - proves the host/port are
     *  reachable and the token is valid without touching the public presentation. */
    suspend fun getTokenInfo(config: HolyricsConnectionConfig): HolyricsResult<HolyricsTokenInfo>

    /** Generic "next/back" remote-control commands on whatever Holyrics currently has open -
     *  not specific to Bible verses (confirmed against the official documentation, github.com/
     *  holyrics/API-Server): there is no dedicated "next verse" action, so these only make sense
     *  right after a [showVerse] call put a verse presentation on screen. */
    suspend fun actionNext(config: HolyricsConnectionConfig): HolyricsResult<Unit>
    suspend fun actionPrevious(config: HolyricsConnectionConfig): HolyricsResult<Unit>
}
