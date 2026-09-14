package com.mybiblevoice.mysword

import android.content.ActivityNotFoundException
import android.content.Context
import com.mybiblevoice.domain.bible.BibleReference

/**
 * Phase 2 placeholder: launches MySword generically but does NOT yet navigate to the exact
 * passage. Passage-accurate deep-linking (a MySwordUriBuilder) is Phase 3 work, gated on
 * verifying MySword's external-link URI/component against a real installed build
 * (SRS sections 14, 24) - it must not be guessed at.
 */
class MySwordLauncherImpl(private val context: Context) : MySwordLauncher {

    override fun open(reference: BibleReference): LaunchResult {
        if (!MySwordAvailability.isInstalled(context)) {
            return LaunchResult.NotInstalled
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(MySwordAvailability.PACKAGE_ID)
            ?: return LaunchResult.Failed("MySword is installed but could not be launched.")

        return try {
            context.startActivity(launchIntent)
            LaunchResult.Launched(exactPassage = false)
        } catch (e: ActivityNotFoundException) {
            LaunchResult.Failed("Unable to launch MySword.")
        }
    }
}
