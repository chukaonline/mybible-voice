package com.mybiblevoice.mysword

import android.content.Context
import android.content.pm.PackageManager

/**
 * Checks whether MySword is installed.
 *
 * [PACKAGE_ID] is confirmed against MySword's own documentation
 * (mysword.info/news/220-link-or-open-mysword-from-other-apps).
 */
object MySwordAvailability {
    const val PACKAGE_ID = "com.riversoft.android.mysword"

    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(PACKAGE_ID, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
