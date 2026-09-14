package com.mybiblevoice.mysword

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mybiblevoice.domain.bible.BibleReference

/**
 * Launches MySword at the exact requested passage using MySword's documented external-link
 * component (mysword.info/news/220-link-or-open-mysword-from-other-apps). If that component
 * isn't present on the installed MySword build (API drift across versions - SRS sections 14,
 * 24 call for real-device verification), falls back to a generic app launch rather than
 * failing outright; it never silently substitutes a different translation.
 */
/**
 * [context] here is the applicationContext (not an Activity), so every launched Intent MUST
 * carry FLAG_ACTIVITY_NEW_TASK - without it, startActivity() throws
 * AndroidRuntimeException("Calling startActivity() from outside of an Activity context
 * requires the FLAG_ACTIVITY_NEW_TASK flag") the moment this actually runs.
 */
class MySwordLauncherImpl(private val context: Context) : MySwordLauncher {

    private val linkComponent = ComponentName(
        MySwordAvailability.PACKAGE_ID,
        "com.riversoft.android.mysword.MySwordLink"
    )

    override fun open(reference: BibleReference): LaunchResult {
        if (!MySwordAvailability.isInstalled(context)) {
            return LaunchResult.NotInstalled
        }

        val exactIntent = Intent().apply {
            component = linkComponent
            data = Uri.parse(MySwordUriBuilder.buildUri(reference))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(exactIntent)
            return LaunchResult.Launched(exactPassage = true)
        } catch (e: ActivityNotFoundException) {
            // Documented component not present on this MySword build - fall back below.
        }

        val genericIntent = context.packageManager.getLaunchIntentForPackage(MySwordAvailability.PACKAGE_ID)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            ?: return LaunchResult.Failed("MySword is installed but could not be launched.")

        return try {
            context.startActivity(genericIntent)
            LaunchResult.Launched(exactPassage = false)
        } catch (e: ActivityNotFoundException) {
            LaunchResult.Failed("Unable to launch MySword.")
        }
    }
}
