package com.mybiblevoice.mysword

import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.target.BibleTarget
import com.mybiblevoice.target.TargetError
import com.mybiblevoice.target.TargetResult

/**
 * Adapts the existing, already-tested [MySwordLauncher]/[LaunchResult] behind [BibleTarget]
 * without changing either - MySword's behavior must remain exactly what it was before the
 * target abstraction existed.
 */
class MySwordTarget(private val launcher: MySwordLauncher) : BibleTarget {

    override suspend fun open(reference: BibleReference): TargetResult {
        return when (val result = launcher.open(reference)) {
            is LaunchResult.Launched -> TargetResult.Success(
                warning = if (result.exactPassage) {
                    null
                } else {
                    "Opened MySword, but could not navigate to the exact passage on this MySword version."
                }
            )
            LaunchResult.NotInstalled ->
                TargetResult.Failure(TargetError.NotAvailable("MySword is required but is not installed."))
            is LaunchResult.Failed ->
                TargetResult.Failure(TargetError.Unexpected(result.reason))
        }
    }
}
