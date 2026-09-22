package com.mybiblevoice.target

import com.mybiblevoice.domain.bible.BibleReference

/**
 * A destination MyBible Voice can send a parsed [BibleReference] to. [BibleReference] itself
 * stays target-neutral - no target-specific fields ever get added to it, and a target adapter
 * is responsible for translating the domain model into its own transport format.
 */
interface BibleTarget {
    suspend fun open(reference: BibleReference): TargetResult
}
