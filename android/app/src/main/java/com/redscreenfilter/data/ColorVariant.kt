package com.redscreenfilter.data

import android.graphics.Color

/**
 * ColorVariant Enum - Color blindness variants and accessibility options
 * 
 * Provides different red color variations for accessibility:
 * - RED_STANDARD: Pure red for standard users
 * - RED_ORANGE: Red-orange for protanopia users
 * - RED_PINK: Red-pink for deuteranopia users
 * - HIGH_CONTRAST: High contrast red for visibility
 */
enum class ColorVariant(
    val displayName: String,
    val colorValue: Int,
    val descripton: String
) {
    RED_STANDARD(
        displayName = "Red Standard",
        colorValue = Color.argb(255, 255, 0, 0),
        descripton = "Pure red - Standard for most users"
    ),
    RED_ORANGE(
        displayName = "Red-Orange",
        colorValue = Color.argb(255, 255, 100, 0),
        descripton = "Red-orange - Better for protanopia"
    ),
    RED_PINK(
        displayName = "Red-Pink",
        colorValue = Color.argb(255, 255, 0, 100),
        descripton = "Red-pink - Better for deuteranopia"
    ),
    HIGH_CONTRAST(
        displayName = "High Contrast",
        colorValue = Color.argb(255, 255, 50, 50),
        descripton = "High contrast red - Maximum visibility"
    );

    companion object {
        fun fromString(value: String?): ColorVariant {
            return try {
                valueOf(value?.uppercase() ?: RED_STANDARD.name)
            } catch (e: IllegalArgumentException) {
                RED_STANDARD
            }
        }
    }
}
