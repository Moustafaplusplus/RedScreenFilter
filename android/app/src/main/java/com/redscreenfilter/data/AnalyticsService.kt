package com.redscreenfilter.data

import android.content.Context
import android.util.Log
import com.redscreenfilter.data.database.UsageDatabase
import com.redscreenfilter.data.database.UsageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Analytics Service
 * Handles logging usage events and calculating analytics data
 */
class AnalyticsService(private val context: Context) {
    
    private val TAG = "AnalyticsService"
    private val database = UsageDatabase.getInstance(context)
    private val dao = database.usageEventDao()
    
    companion object {
        // Event types
        const val EVENT_OVERLAY_TOGGLED = "overlay_toggled"
        const val EVENT_SETTINGS_CHANGED = "settings_changed"
        const val EVENT_PRESET_APPLIED = "preset_applied"
        const val EVENT_OPACITY_CHANGED = "opacity_changed"
        
        @Volatile
        private var instance: AnalyticsService? = null
        
        fun getInstance(context: Context): AnalyticsService {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsService(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Log an overlay toggle event
     */
    suspend fun logOverlayToggled(enabled: Boolean, opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                val event = UsageEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = EVENT_OVERLAY_TOGGLED,
                    overlayEnabled = enabled,
                    opacity = opacity
                )
                dao.insertEvent(event)
                Log.d(TAG, "logOverlayToggled: Logged overlay toggled event - enabled=$enabled")
            } catch (e: Exception) {
                Log.e(TAG, "logOverlayToggled: Error logging event", e)
            }
        }
    }
    
    /**
     * Log a settings change event
     */
    suspend fun logSettingsChanged(overlayEnabled: Boolean, opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                val event = UsageEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = EVENT_SETTINGS_CHANGED,
                    overlayEnabled = overlayEnabled,
                    opacity = opacity
                )
                dao.insertEvent(event)
                Log.d(TAG, "logSettingsChanged: Logged settings changed event")
            } catch (e: Exception) {
                Log.e(TAG, "logSettingsChanged: Error logging event", e)
            }
        }
    }
    
    /**
     * Log a preset applied event
     */
    suspend fun logPresetApplied(presetName: String, opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                val event = UsageEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = EVENT_PRESET_APPLIED,
                    overlayEnabled = true,
                    opacity = opacity,
                    preset = presetName
                )
                dao.insertEvent(event)
                Log.d(TAG, "logPresetApplied: Logged preset applied - $presetName")
            } catch (e: Exception) {
                Log.e(TAG, "logPresetApplied: Error logging event", e)
            }
        }
    }
    
    /**
     * Log opacity change event
     */
    suspend fun logOpacityChanged(opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                val event = UsageEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = EVENT_OPACITY_CHANGED,
                    overlayEnabled = true,
                    opacity = opacity
                )
                dao.insertEvent(event)
                Log.d(TAG, "logOpacityChanged: Logged opacity changed - $opacity")
            } catch (e: Exception) {
                Log.e(TAG, "logOpacityChanged: Error logging event", e)
            }
        }
    }
    
    /**
     * Get total overlay usage time in milliseconds for today
     */
    suspend fun getTodayUsageTime(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val todayStart = getTodayStartTime()
                val events = dao.getTodayEvents(todayStart)
                calculateUsageTime(events)
            } catch (e: Exception) {
                Log.e(TAG, "getTodayUsageTime: Error calculating usage time", e)
                0L
            }
        }
    }
    
    /**
     * Get total overlay usage time for this week
     */
    suspend fun getWeekUsageTime(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val weekStart = getWeekStartTime()
                val events = dao.getWeekEvents(weekStart)
                calculateUsageTime(events)
            } catch (e: Exception) {
                Log.e(TAG, "getWeekUsageTime: Error calculating usage time", e)
                0L
            }
        }
    }
    
    /**
     * Get total overlay usage time for this month
     */
    suspend fun getMonthUsageTime(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val monthStart = getMonthStartTime()
                val events = dao.getMonthEvents(monthStart)
                calculateUsageTime(events)
            } catch (e: Exception) {
                Log.e(TAG, "getMonthUsageTime: Error calculating usage time", e)
                0L
            }
        }
    }
    
    /**
     * Get current usage streak (consecutive days with overlay active)
     */
    suspend fun getCurrentStreak(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val allEvents = dao.getAllEvents()
                var streak = 0
                val today = Calendar.getInstance()
                var currentDate = today.clone() as Calendar
                
                // Group events by day and check if overlay was active
                while (true) {
                    val dayStart = getDayStartTime(currentDate.timeInMillis)
                    val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
                    
                    val dayEvents = allEvents.filter { event ->
                        event.timestamp in dayStart..dayEnd && event.overlayEnabled
                    }
                    
                    if (dayEvents.isNotEmpty()) {
                        streak++
                        currentDate.add(Calendar.DAY_OF_MONTH, -1)
                    } else {
                        break
                    }
                }
                
                Log.d(TAG, "getCurrentStreak: Current streak is $streak days")
                streak
            } catch (e: Exception) {
                Log.e(TAG, "getCurrentStreak: Error calculating streak", e)
                0
            }
        }
    }
    
    /**
     * Get average opacity
     */
    suspend fun getAverageOpacity(): Float {
        return withContext(Dispatchers.IO) {
            try {
                val average = dao.getAverageOpacity()
                Log.d(TAG, "getAverageOpacity: Average opacity is $average")
                average.takeIf { it.isFinite() } ?: 0.5f
            } catch (e: Exception) {
                Log.e(TAG, "getAverageOpacity: Error getting average opacity", e)
                0.5f
            }
        }
    }
    
    /**
     * Get most used preset
     */
    suspend fun getMostUsedPreset(): String {
        return withContext(Dispatchers.IO) {
            try {
                val preset = dao.getMostUsedPreset() ?: "None"
                Log.d(TAG, "getMostUsedPreset: Most used preset is $preset")
                preset
            } catch (e: Exception) {
                Log.e(TAG, "getMostUsedPreset: Error getting most used preset", e)
                "None"
            }
        }
    }
    
    /**
     * Get total number of events
     */
    suspend fun getTotalEventCount(): Int {
        return withContext(Dispatchers.IO) {
            try {
                dao.getEventCount()
            } catch (e: Exception) {
                Log.e(TAG, "getTotalEventCount: Error getting event count", e)
                0
            }
        }
    }
    
    /**
     * Clear all analytics data
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            try {
                dao.deleteAllEvents()
                Log.d(TAG, "clearAllData: All analytics data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "clearAllData: Error clearing data", e)
            }
        }
    }
    
    // ========== Helper Functions ==========
    
    /**
     * Calculate total usage time from a list of events
     * Estimates usage based on "overlay_toggled" events with overlayEnabled=true
     */
    private fun calculateUsageTime(events: List<UsageEvent>): Long {
        if (events.isEmpty()) return 0L
        
        var totalTime = 0L
        var lastToggleTime: Long? = null
        var isActive = false
        
        // Events are in descending order, reverse to chronological order
        for (event in events.reversed()) {
            if (event.eventType == EVENT_OVERLAY_TOGGLED) {
                if (event.overlayEnabled && !isActive) {
                    // Overlay turned on
                    lastToggleTime = event.timestamp
                    isActive = true
                } else if (!event.overlayEnabled && isActive && lastToggleTime != null) {
                    // Overlay turned off, calculate duration
                    totalTime += (event.timestamp - lastToggleTime)
                    isActive = false
                    lastToggleTime = null
                }
            }
        }
        
        // If still active, add time from last toggle to now
        if (isActive && lastToggleTime != null) {
            totalTime += (System.currentTimeMillis() - lastToggleTime)
        }
        
        return totalTime
    }
    
    /**
     * Get today's start time (midnight)
     */
    private fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Get this week's start time (Monday)
     */
    private fun getWeekStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Get this month's start time (1st day)
     */
    private fun getMonthStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Get day start time for any given timestamp
     */
    private fun getDayStartTime(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Format milliseconds to HH:MM:SS format
     */
    fun formatTime(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
