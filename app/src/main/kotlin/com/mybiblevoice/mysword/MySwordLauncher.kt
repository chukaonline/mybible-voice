package com.mybiblevoice.mysword

import com.mybiblevoice.domain.bible.BibleReference

/** Application-facing interface; isolates all MySword integration details (SRS section 14). */
interface MySwordLauncher {
    fun open(reference: BibleReference): LaunchResult
}
