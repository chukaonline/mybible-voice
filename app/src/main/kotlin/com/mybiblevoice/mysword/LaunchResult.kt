package com.mybiblevoice.mysword

sealed class LaunchResult {
    /** [exactPassage] is false until [MySwordUriBuilder]-based deep-linking lands in Phase 3. */
    data class Launched(val exactPassage: Boolean) : LaunchResult()
    object NotInstalled : LaunchResult()
    data class Failed(val reason: String) : LaunchResult()
}
