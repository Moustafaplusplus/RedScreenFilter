package com.redscreenfilter.data

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed application
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var isExempted: Boolean = false
) : Comparable<InstalledApp> {
    override fun compareTo(other: InstalledApp): Int {
        return appName.compareTo(other.appName)
    }
}
