package com.redscreenfilter.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * Foreground App Detector
 * 
 * Detects the currently active foreground app using UsageStatsManager.
 * Requires PACKAGE_USAGE_STATS permission.
 */
class ForegroundAppDetector(private val context: Context) {
    
    private val TAG = "ForegroundAppDetector"
    private val usageStatsManager: UsageStatsManager? = 
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val appOpsManager: AppOpsManager? = 
        context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
    
    companion object {
        @Volatile
        private var instance: ForegroundAppDetector? = null
        
        fun getInstance(context: Context): ForegroundAppDetector {
            return instance ?: synchronized(this) {
                instance ?: ForegroundAppDetector(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Get the currently active foreground app's package name
     * Returns null if unable to determine or if permission is not granted
     */
    fun getForegroundAppPackage(): String? {
        return try {
            // Check if we have permission to access usage stats
            if (!hasUsageStatsPermission()) {
                Log.w(TAG, "getForegroundAppPackage: No PACKAGE_USAGE_STATS permission")
                return null
            }
            
            val currentTime = System.currentTimeMillis()
            // Increase range slightly for better reliability if no recent events
            val timeRange = TimeUnit.MINUTES.toMillis(2)
            val startTime = currentTime - timeRange
            
            // Query usage stats for the last 2 minutes
            val usageEvents = usageStatsManager?.queryEvents(startTime, currentTime)
            
            var lastEventTime = 0L
            var foregroundPackage: String? = null
            
            usageEvents?.let { events ->
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    
                    // Look for ACTIVITY_RESUMED events (foreground app)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        if (event.timeStamp > lastEventTime) {
                            lastEventTime = event.timeStamp
                            foregroundPackage = event.packageName
                        }
                    }
                }
            }
            
            // Fallback: If no RESUMED events found in the window, use queryUsageStats
            if (foregroundPackage == null) {
                val stats = usageStatsManager?.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime)
                if (stats != null && stats.isNotEmpty()) {
                    foregroundPackage = stats.maxByOrNull { it.lastTimeUsed }?.packageName
                    Log.d(TAG, "getForegroundAppPackage: Fallback to queryUsageStats found: $foregroundPackage")
                }
            }
            
            Log.d(TAG, "getForegroundAppPackage: Foreground app=$foregroundPackage")
            foregroundPackage
        } catch (e: Exception) {
            Log.e(TAG, "getForegroundAppPackage: Error detecting foreground app", e)
            null
        }
    }
    
    /**
     * Check if we have permission to access usage stats
     */
    fun hasUsageStatsPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager?.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } else {
                @Suppress("DEPRECATION")
                appOpsManager?.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
        } catch (e: Exception) {
            Log.w(TAG, "hasUsageStatsPermission: Error checking permission", e)
            false
        }
    }
}
