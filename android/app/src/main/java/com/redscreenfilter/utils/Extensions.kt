package com.redscreenfilter.utils

/**
 * Extension functions for common operations
 */

/**
 * Convert opacity float (0.0-1.0) to alpha byte (0-255)
 */
fun Float.toAlpha(): Int = (this * 255).toInt()

/**
 * Convert alpha byte (0-255) to opacity float (0.0-1.0)
 */
fun Int.toOpacity(): Float = this / 255f

/**
 * Format time string to HH:mm format
 */
fun String.formatTime(): String {
    // TODO: Implement time formatting
    return this
}
