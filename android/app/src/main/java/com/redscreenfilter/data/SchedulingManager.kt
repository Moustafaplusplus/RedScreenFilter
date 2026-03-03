package com.redscreenfilter.data

import android.content.Context
import android.util.Log
import com.redscreenfilter.utils.SunriseCalculator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * SchedulingManager
 * Manages scheduled activation/deactivation of the overlay based on time windows
 * Supports both manual time scheduling and location-based sunrise/sunset scheduling
 * Handles day boundary crossings (e.g., 10 PM to 7 AM)
 */
class SchedulingManager private constructor(private val context: Context) {
    
    private val TAG = "SchedulingManager"
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val locationManager = LocationManager.getInstance(context)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    companion object {
        @Volatile
        private var instance: SchedulingManager? = null
        
        fun getInstance(context: Context): SchedulingManager {
            return instance ?: synchronized(this) {
                instance ?: SchedulingManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Check if scheduling is enabled
     */
    fun isScheduleEnabled(): Boolean {
        return preferencesManager.isScheduleEnabled()
    }
    
    /**
     * Set the scheduled time window
     * @param startTime Time in HH:mm format (24-hour)
     * @param endTime Time in HH:mm format (24-hour)
     */
    fun setSchedule(startTime: String, endTime: String) {
        Log.d(TAG, "setSchedule: Setting schedule from $startTime to $endTime")
        preferencesManager.setScheduleStartTime(startTime)
        preferencesManager.setScheduleEndTime(endTime)
    }
    
    /**
     * Get the scheduled state - returns if overlay should be active based on current time
     * Supports both manual scheduling and location-based sunrise/sunset scheduling
     * @return true if current time falls within scheduled window, false otherwise
     */
    fun getScheduledState(): Boolean {
        if (!isScheduleEnabled()) {
            Log.d(TAG, "getScheduledState: Schedule disabled, returning false")
            return false
        }
        
        // Check if using location-based scheduling
        if (isLocationScheduleEnabled()) {
            return getLocationBasedScheduledState()
        }
        
        // Use manual time-based scheduling
        val startTime = preferencesManager.getScheduleStartTime()
        val endTime = preferencesManager.getScheduleEndTime()
        
        return isCurrentTimeInRange(startTime, endTime)
    }
    
    /**
     * Check if location-based scheduling is enabled
     */
    fun isLocationScheduleEnabled(): Boolean {
        return preferencesManager.getUseLocationSchedule()
    }
    
    /**
     * Enable or disable location-based scheduling
     */
    fun setLocationScheduleEnabled(enabled: Boolean) {
        Log.d(TAG, "setLocationScheduleEnabled: $enabled")
        preferencesManager.setUseLocationSchedule(enabled)
    }
    
    /**
     * Get scheduled state based on location (sunrise/sunset)
     */
    private fun getLocationBasedScheduledState(): Boolean {
        // Get cached location
        val location = locationManager.getCachedLocation()
        if (location == null) {
            Log.w(TAG, "getLocationBasedScheduledState: No cached location, falling back to manual schedule")
            return false
        }
        
        val (latitude, longitude) = location
        
        // Calculate sunrise and sunset times for today
        val calendar = Calendar.getInstance()
        val (sunriseMillis, sunsetMillis) = SunriseCalculator.calculateSunriseSunset(
            latitude, longitude, calendar
        )
        
        // Apply offset (in minutes)
        val offsetMinutes = preferencesManager.getLocationOffsetMinutes()
        val offsetMillis = offsetMinutes * 60 * 1000L
        
        val adjustedSunsetMillis = sunsetMillis + offsetMillis
        val adjustedSunriseMillis = sunriseMillis + offsetMillis
        
        // Get current time
        val currentMillis = System.currentTimeMillis()
        
        // Check if current time is between sunset and sunrise
        val isInRange = if (adjustedSunriseMillis > adjustedSunsetMillis) {
            // Normal case: sunset is before sunrise (same day)
            currentMillis >= adjustedSunsetMillis && currentMillis < adjustedSunriseMillis
        } else {
            // Sunset is after sunrise (crosses midnight)
            currentMillis >= adjustedSunsetMillis || currentMillis < adjustedSunriseMillis
        }
        
        Log.d(TAG, "getLocationBasedScheduledState: sunset=${sunsetMillis}, sunrise=${sunriseMillis}, offset=${offsetMinutes}min, inRange=$isInRange")
        
        return isInRange
    }
    
    /**
     * Check if current time falls within the scheduled time range
     * Handles day boundary crossings (e.g., 22:00 to 07:00)
     */
    private fun isCurrentTimeInRange(startTime: String, endTime: String): Boolean {
        try {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val currentMinutes = currentHour * 60 + currentMinute
            
            // Parse start and end times
            val startParts = startTime.split(":")
            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val startMinutes = startHour * 60 + startMinute
            
            val endParts = endTime.split(":")
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()
            val endMinutes = endHour * 60 + endMinute
            
            val isInRange = if (endMinutes < startMinutes) {
                // Schedule crosses midnight (e.g., 22:00 to 07:00)
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            } else {
                // Schedule is within same day (e.g., 09:00 to 17:00)
                currentMinutes >= startMinutes && currentMinutes < endMinutes
            }
            
            Log.d(TAG, "isCurrentTimeInRange: Current=$currentMinutes, Start=$startMinutes, End=$endMinutes, InRange=$isInRange")
            return isInRange
            
        } catch (e: Exception) {
            Log.e(TAG, "isCurrentTimeInRange: Error parsing time", e)
            return false
        }
    }
    
    /**
     * Get formatted display string for schedule
     */
    fun getScheduleDisplayString(): String {
        if (!isScheduleEnabled()) {
            return "Schedule Disabled"
        }
        
        val startTime = preferencesManager.getScheduleStartTime()
        val endTime = preferencesManager.getScheduleEndTime()
        
        return "$startTime - $endTime"
    }
    
    /**
     * Enable or disable scheduling
     */
    fun setScheduleEnabled(enabled: Boolean) {
        Log.d(TAG, "setScheduleEnabled: $enabled")
        preferencesManager.setScheduleEnabled(enabled)
    }
    
    /**
     * Get start time as hour and minute
     */
    fun getStartTimeComponents(): Pair<Int, Int> {
        val startTime = preferencesManager.getScheduleStartTime()
        val parts = startTime.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }
    
    /**
     * Get end time as hour and minute
     */
    fun getEndTimeComponents(): Pair<Int, Int> {
        val endTime = preferencesManager.getScheduleEndTime()
        val parts = endTime.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }
    
    /**
     * Format time components to HH:mm string
     */
    fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }
    
    // ========== Location-Based Scheduling ==========
    
    /**
     * Get calculated sunset time as string
     * Returns calculated time with offset applied, or null if location not available
     */
    fun getCalculatedSunsetTime(): String? {
        val location = locationManager.getCachedLocation() ?: return null
        val (latitude, longitude) = location
        
        val calendar = Calendar.getInstance()
        val (_, sunsetMillis) = SunriseCalculator.calculateSunriseSunset(latitude, longitude, calendar)
        
        // Apply offset
        val offsetMinutes = preferencesManager.getLocationOffsetMinutes()
        val adjustedSunsetMillis = sunsetMillis + (offsetMinutes * 60 * 1000L)
        
        val sunsetCal = Calendar.getInstance().apply {
            timeInMillis = adjustedSunsetMillis
        }
        
        return formatTime(sunsetCal.get(Calendar.HOUR_OF_DAY), sunsetCal.get(Calendar.MINUTE))
    }
    
    /**
     * Get calculated sunrise time as string
     * Returns calculated time with offset applied, or null if location not available
     */
    fun getCalculatedSunriseTime(): String? {
        val location = locationManager.getCachedLocation() ?: return null
        val (latitude, longitude) = location
        
        val calendar = Calendar.getInstance()
        val (sunriseMillis, _) = SunriseCalculator.calculateSunriseSunset(latitude, longitude, calendar)
        
        // Apply offset
        val offsetMinutes = preferencesManager.getLocationOffsetMinutes()
        val adjustedSunriseMillis = sunriseMillis + (offsetMinutes * 60 * 1000L)
        
        val sunriseCal = Calendar.getInstance().apply {
            timeInMillis = adjustedSunriseMillis
        }
        
        return formatTime(sunriseCal.get(Calendar.HOUR_OF_DAY), sunriseCal.get(Calendar.MINUTE))
    }
    
    /**
     * Set location offset in minutes
     * Positive values delay activation, negative values advance it
     */
    fun setLocationOffset(offsetMinutes: Int) {
        preferencesManager.setLocationOffsetMinutes(offsetMinutes)
        Log.d(TAG, "setLocationOffset: $offsetMinutes minutes")
    }
    
    /**
     * Get location offset in minutes
     */
    fun getLocationOffset(): Int {
        return preferencesManager.getLocationOffsetMinutes()
    }
}
