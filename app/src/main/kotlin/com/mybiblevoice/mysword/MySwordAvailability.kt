package com.mybiblevoice.mysword

import android.content.Context
import android.content.pm.PackageManager

/**
 * Checks whether MySword is installed.
 *
 * [PACKAGE_ID] is a best-effort default and, per the technical design (SRS sections 14, 24),
 * must be verified against a real installed MySword build during integration testing.
 */
object MySwordAvailability {
    const val PACKAGE_ID = "mysword"

    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(PACKAGE_ID, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
