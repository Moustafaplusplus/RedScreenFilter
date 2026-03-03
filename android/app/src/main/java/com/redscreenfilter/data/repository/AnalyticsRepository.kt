package com.redscreenfilter.data.repository

import android.content.Context
import android.util.Log
import com.redscreenfilter.data.AnalyticsService
import com.redscreenfilter.data.database.UsageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Analytics Repository
 * Clean abstraction layer for analytics operations
 * Wraps AnalyticsService for consistent data access
 */
class AnalyticsRepository(private val context: Context) {
    
    private val TAG = "AnalyticsRepository"
    private val analyticsService = AnalyticsService.getInstance(context)
    
    companion object {
        @Volatile
        private var instance: AnalyticsRepository? = null
        
        fun getInstance(context: Context): AnalyticsRepository {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Log overlay toggled event
     */
    suspend fun logOverlayToggled(enabled: Boolean, opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                analyticsService.logOverlayToggled(enabled, opacity)
            } catch (e: Exception) {
                Log.e(TAG, "logOverlayToggled: Error", e)
            }
        }
    }
    
    /**
     * Log settings changed event
     */
    suspend fun logSettingsChanged(overlayEnabled: Boolean, opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                analyticsService.logSettingsChanged(overlayEnabled, opacity)
            } catch (e: Exception) {
                Log.e(TAG, "logSettingsChanged: Error", e)
            }
        }
    }
    
    /**
     * Log preset applied event
     */
    suspend fun logPresetApplied(presetName: String, opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                analyticsService.logPresetApplied(presetName, opacity)
            } catch (e: Exception) {
                Log.e(TAG, "logPresetApplied: Error", e)
            }
        }
    }
    
    /**
     * Log opacity changed event
     */
    suspend fun logOpacityChanged(opacity: Float) {
        withContext(Dispatchers.IO) {
            try {
                analyticsService.logOpacityChanged(opacity)
            } catch (e: Exception) {
                Log.e(TAG, "logOpacityChanged: Error", e)
            }
        }
    }
    
    /**
     * Get usage statistics for dashboard
     */
    data class UsageStats(
        val todayUsageTime: String,
        val weekUsageTime: String,
        val monthUsageTime: String,
        val averageOpacity: Float,
        val mostUsedPreset: String,
        val currentStreak: Int,
        val totalEvents: Int
    )
    
    suspend fun getUsageStats(): UsageStats {
        return withContext(Dispatchers.IO) {
            try {
                val todayTime = analyticsService.getTodayUsageTime()
                val weekTime = analyticsService.getWeekUsageTime()
                val monthTime = analyticsService.getMonthUsageTime()
                val avgOpacity = analyticsService.getAverageOpacity()
                val mostUsed = analyticsService.getMostUsedPreset()
                val streak = analyticsService.getCurrentStreak()
                val totalEvents = analyticsService.getTotalEventCount()
                
                UsageStats(
                    todayUsageTime = analyticsService.formatTime(todayTime),
                    weekUsageTime = analyticsService.formatTime(weekTime),
                    monthUsageTime = analyticsService.formatTime(monthTime),
                    averageOpacity = avgOpacity,
                    mostUsedPreset = mostUsed,
                    currentStreak = streak,
                    totalEvents = totalEvents
                )
            } catch (e: Exception) {
                Log.e(TAG, "getUsageStats: Error", e)
                UsageStats(
                    todayUsageTime = "00:00:00",
                    weekUsageTime = "00:00:00",
                    monthUsageTime = "00:00:00",
                    averageOpacity = 0.5f,
                    mostUsedPreset = "None",
                    currentStreak = 0,
                    totalEvents = 0
                )
            }
        }
    }
    
    /**
     * Clear all analytics data
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            try {
                analyticsService.clearAllData()
                Log.d(TAG, "clearAllData: All data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "clearAllData: Error", e)
            }
        }
    }
}
