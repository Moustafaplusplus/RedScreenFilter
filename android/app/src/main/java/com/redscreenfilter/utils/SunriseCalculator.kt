package com.redscreenfilter.utils

import android.util.Log
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * SunriseCalculator
 * Calculates sunrise and sunset times based on geographical coordinates
 * Uses simplified solar position algorithm based on NOAA Solar Calculator
 */
object SunriseCalculator {
    
    private const val TAG = "SunriseCalculator"
    
    /**
     * Calculate sunrise and sunset times for given coordinates and date
     * @param latitude Latitude in degrees (-90 to 90)
     * @param longitude Longitude in degrees (-180 to 180)
     * @param calendar Calendar object for the date (uses device timezone)
     * @return Pair of sunrise and sunset times in milliseconds since epoch
     */
    fun calculateSunriseSunset(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance()
    ): Pair<Long, Long> {
        
        Log.d(TAG, "calculateSunriseSunset: lat=$latitude, lon=$longitude, date=${calendar.time}")
        
        // Get day of year
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        // Calculate sunrise and sunset times (in minutes from midnight, solar/UTC time)
        val sunriseTime = calculateSunEvent(latitude, longitude, dayOfYear, true)
        val sunsetTime = calculateSunEvent(latitude, longitude, dayOfYear, false)
        
        // Convert to epoch milliseconds using UTC calendar to avoid timezone double-application
        val utcCalendar = (calendar.clone() as Calendar).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStartMillis = utcCalendar.timeInMillis
        
        val sunriseMillis = dayStartMillis + (sunriseTime * 60 * 1000).toLong()
        val sunsetMillis = dayStartMillis + (sunsetTime * 60 * 1000).toLong()
        
        Log.d(TAG, "calculateSunriseSunset: sunrise=$sunriseTime min, sunset=$sunsetTime min")
        Log.d(TAG, "calculateSunriseSunset: sunrise UTC=${java.util.Date(sunriseMillis)}, sunset UTC=${java.util.Date(sunsetMillis)}")
        
        return Pair(sunriseMillis, sunsetMillis)
    }
    
    /**
     * Calculate sun event time (sunrise or sunset)
     * @return Time in minutes from midnight (solar time, relative to longitude)
     */
    private fun calculateSunEvent(
        latitude: Double,
        longitude: Double,
        dayOfYear: Int,
        isSunrise: Boolean
    ): Double {
        
        // Convert latitude to radians
        val latRad = Math.toRadians(latitude)
        
        // Calculate solar noon (in minutes from midnight, UTC)
        // Every 15° of longitude = 1 hour difference in solar time
        val solarNoon = 720 - 4 * longitude
        
        // Calculate solar declination
        val declination = calculateDeclination(dayOfYear)
        val decRad = Math.toRadians(declination)
        
        // Calculate hour angle
        val cosHourAngle = (cos(Math.toRadians(90.833)) / (cos(latRad) * cos(decRad))) - 
                          (tan(latRad) * tan(decRad))
        
        // Check if sun rises/sets (polar regions might not have sunrise/sunset)
        if (cosHourAngle > 1.0) {
            // Sun never rises (polar night)
            return if (isSunrise) 0.0 else 1440.0 // midnight or end of day
        } else if (cosHourAngle < -1.0) {
            // Sun never sets (midnight sun)
            return if (isSunrise) 0.0 else 1440.0
        }
        
        val hourAngle = Math.toDegrees(acos(cosHourAngle))
        
        // Calculate time
        val time = if (isSunrise) {
            solarNoon - 4 * hourAngle
        } else {
            solarNoon + 4 * hourAngle
        }
        
        return time
    }
    
    /**
     * Calculate solar declination for given day of year
     * @param dayOfYear Day of year (1-365/366)
     * @return Declination in degrees
     */
    private fun calculateDeclination(dayOfYear: Int): Double {
        // Approximate solar declination using a sinusoidal function
        val n = dayOfYear.toDouble()
        return 23.45 * sin(Math.toRadians((360.0 / 365.0) * (n - 81)))
    }
    
    /**
     * Format time in minutes to HH:mm string
     */
    fun formatTime(minutes: Double): String {
        val totalMinutes = minutes.toInt()
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return String.format("%02d:%02d", hours, mins)
    }
    
    /**
     * Get sunrise time as HH:mm string
     */
    fun getSunriseString(latitude: Double, longitude: Double, calendar: Calendar = Calendar.getInstance()): String {
        val (sunriseMillis, _) = calculateSunriseSunset(latitude, longitude, calendar)
        val sunriseCalendar = Calendar.getInstance().apply {
            timeInMillis = sunriseMillis
        }
        return String.format("%02d:%02d", 
            sunriseCalendar.get(Calendar.HOUR_OF_DAY),
            sunriseCalendar.get(Calendar.MINUTE))
    }
    
    /**
     * Get sunset time as HH:mm string
     */
    fun getSunsetString(latitude: Double, longitude: Double, calendar: Calendar = Calendar.getInstance()): String {
        val (_, sunsetMillis) = calculateSunriseSunset(latitude, longitude, calendar)
        val sunsetCalendar = Calendar.getInstance().apply {
            timeInMillis = sunsetMillis
        }
        return String.format("%02d:%02d", 
            sunsetCalendar.get(Calendar.HOUR_OF_DAY),
            sunsetCalendar.get(Calendar.MINUTE))
    }
}
