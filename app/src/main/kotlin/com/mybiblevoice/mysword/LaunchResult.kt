package com.mybiblevoice.mysword

/**
 * MySword's external-link mechanism is fire-and-forget: launching it via Intent gives no
 * synchronous way to ask "is translation X installed?" before navigating (SRS section 11
 * requires the requested translation to be installed, and section 17 calls for a distinct
 * "translation unavailable" error, but there is no documented API to check this ahead of
 * launch). [Launched] only confirms MySword accepted the launch request - if the requested
 * translation isn't installed, MySword handles that in its own UI. We never substitute a
 * different translation on our end regardless.
 */
sealed class LaunchResult {
    /** [exactPassage] is false when we fell back to a generic launch (see [MySwordLauncherImpl]). */
    data class Launched(val exactPassage: Boolean) : LaunchResult()
    object NotInstalled : LaunchResult()
    data class Failed(val reason: String) : LaunchResult()
}
