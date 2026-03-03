package com.redscreenfilter.utils

/**
 * Constants for the entire application
 * Colors, dimensions, durations, and other fixed values
 */
object Constants {
    // Colors
    const val RED_COLOR = 0xFFFF0000
    const val RED_ORANGE_COLOR = 0xFFFF6400
    const val RED_PINK_COLOR = 0xFFFF0064
    
    // Opacity ranges
    const val MIN_OPACITY = 0.0f
    const val MAX_OPACITY = 1.0f
    const val DEFAULT_OPACITY = 0.5f
    
    // Time format
    const val TIME_FORMAT = "HH:mm"
    
    // Notification IDs
    const val OVERLAY_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002
    
    // WorkManager tags
    const val SCHEDULE_WORKER_TAG = "overlay_schedule_worker"
    const val REMINDER_WORKER_TAG = "eye_strain_reminder"
}
