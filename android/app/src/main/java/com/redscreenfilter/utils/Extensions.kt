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
 * Supports various input formats and returns consistently formatted time
 */
fun String.formatTime(): String {
    // If already in HH:mm format, return as-is
    if (this.matches(Regex("\\d{2}:\\d{2}"))) {
        return this
    }
    
    // Extract numeric values and format
    val numbers = this.filter { it.isDigit() }
    if (numbers.length >= 4) {
        val hours = numbers.substring(0, 2)
        val minutes = numbers.substring(2, 4)
        return "$hours:$minutes"
    }
    
    // If cannot parse, return original
    return this
}
