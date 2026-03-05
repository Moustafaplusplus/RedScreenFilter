//
//  SunriseCalculator.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Phase 40-50% - Sunrise/Sunset Scheduling
//

import Foundation
import CoreLocation

/// SunriseCalculator - Pure utility for calculating sunrise and sunset times
/// Uses Solar Position Algorithm (SPA) for accurate astronomical calculations
/// All functions are pure - no side effects, deterministic outputs
struct SunriseCalculator {
    
    // MARK: - Public API
    
    /// Calculates sunrise and sunset times for a given location and date
    /// - Parameters:
    ///   - latitude: Latitude in degrees (-90 to 90)
    ///   - longitude: Longitude in degrees (-180 to 180)
    ///   - date: The date for which to calculate times (defaults to today)
    ///   - timezone: TimeZone for the result (defaults to current)
    /// - Returns: Tuple of (sunrise: Date, sunset: Date)
    static func calculateSunriseSunset(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        timezone: TimeZone = .current
    ) -> (sunrise: Date, sunset: Date) {
        
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day], from: date)
        
        guard let year = components.year,
              let month = components.month,
              let day = components.day else {
            // Fallback to default times if date parsing fails
            return defaultSunriseSunset(for: date, timezone: timezone)
        }
        
        // Calculate Julian Day
        let julianDay = calculateJulianDay(year: year, month: month, day: day)
        
        // Calculate sunrise
        let sunriseTime = calculateSunEvent(
            julianDay: julianDay,
            latitude: latitude,
            longitude: longitude,
            isSunrise: true
        )
        
        // Calculate sunset
        let sunsetTime = calculateSunEvent(
            julianDay: julianDay,
            latitude: latitude,
            longitude: longitude,
            isSunrise: false
        )
        
        // Convert decimal hours to Date objects
        let sunrise = timeToDate(
            hours: sunriseTime,
            year: year,
            month: month,
            day: day,
            timezone: timezone
        )
        
        let sunset = timeToDate(
            hours: sunsetTime,
            year: year,
            month: month,
            day: day,
            timezone: timezone
        )
        
        return (sunrise, sunset)
    }
    
    // MARK: - Private Calculation Methods
    
    /// Calculates Julian Day number for a given date
    /// Used as base for astronomical calculations
    private static func calculateJulianDay(year: Int, month: Int, day: Int) -> Double {
        var y = year
        var m = month
        
        if m <= 2 {
            y -= 1
            m += 12
        }
        
        let a = y / 100
        let b = 2 - a + (a / 4)
        
        let jd = Double(Int(365.25 * Double(y + 4716))) +
                 Double(Int(30.6001 * Double(m + 1))) +
                 Double(day) +
                 Double(b) -
                 1524.5
        
        return jd
    }
    
    /// Calculates sunrise or sunset time in decimal hours (local time)
    /// - Parameters:
    ///   - julianDay: Julian day number
    ///   - latitude: Latitude in degrees
    ///   - longitude: Longitude in degrees
    ///   - isSunrise: true for sunrise, false for sunset
    /// - Returns: Time in decimal hours (e.g., 6.5 = 6:30 AM)
    private static func calculateSunEvent(
        julianDay: Double,
        latitude: Double,
        longitude: Double,
        isSunrise: Bool
    ) -> Double {
        
        // Solar zenith angle for sunrise/sunset (90.833° accounts for atmospheric refraction)
        let zenith = 90.833
        
        // Calculate day of year
        let n = julianDay - 2451545.0 + 0.0008
        
        // Mean solar noon
        let jStar = n - (longitude / 360.0)
        
        // Solar mean anomaly
        let m = fmod(357.5291 + 0.98560028 * jStar, 360.0)
        let mRad = m * .pi / 180.0
        
        // Equation of center
        let c = 1.9148 * sin(mRad) +
                0.0200 * sin(2 * mRad) +
                0.0003 * sin(3 * mRad)
        
        // Ecliptic longitude
        let lambda = fmod(m + c + 180.0 + 102.9372, 360.0)
        let lambdaRad = lambda * .pi / 180.0
        
        // Solar transit (solar noon)
        let jTransit = 2451545.0 + jStar +
                       0.0053 * sin(mRad) -
                       0.0069 * sin(2 * lambdaRad)
        
        // Declination of the sun
        let sinDelta = sin(lambdaRad) * sin(23.44 * .pi / 180.0)
        let delta = asin(sinDelta)
        
        // Hour angle
        let latRad = latitude * .pi / 180.0
        let zenithRad = zenith * .pi / 180.0
        
        let cosOmega = (cos(zenithRad) - sin(latRad) * sin(delta)) /
                       (cos(latRad) * cos(delta))
        
        // Check if sun rises/sets (not applicable in polar regions during certain times)
        guard cosOmega >= -1 && cosOmega <= 1 else {
            // Sun doesn't rise or set - return default value
            return isSunrise ? 6.0 : 18.0
        }
        
        let omega = acos(cosOmega) * 180.0 / .pi
        
        // Calculate sunrise or sunset Julian day
        let jEvent: Double
        if isSunrise {
            jEvent = jTransit - (omega / 360.0)
        } else {
            jEvent = jTransit + (omega / 360.0)
        }
        
        // Convert to decimal hours (0-24)
        let hours = fmod((jEvent - floor(jEvent)) * 24.0 + 12.0, 24.0)
        
        return hours
    }
    
    /// Converts decimal hours to Date object
    /// - Parameters:
    ///   - hours: Time in decimal hours (e.g., 6.5 = 6:30)
    ///   - year: Year
    ///   - month: Month
    ///   - day: Day
    ///   - timezone: Target timezone
    /// - Returns: Date object representing the time
    private static func timeToDate(
        hours: Double,
        year: Int,
        month: Int,
        day: Int,
        timezone: TimeZone
    ) -> Date {
        
        let hour = Int(hours)
        let minute = Int((hours - Double(hour)) * 60.0)
        let second = Int(((hours - Double(hour)) * 60.0 - Double(minute)) * 60.0)
        
        var components = DateComponents()
        components.year = year
        components.month = month
        components.day = day
        components.hour = hour
        components.minute = minute
        components.second = second
        components.timeZone = timezone
        
        var calendar = Calendar.current
        calendar.timeZone = timezone
        
        return calendar.date(from: components) ?? Date()
    }
    
    /// Returns default sunrise/sunset times when calculation fails
    /// Sunrise: 6:00 AM, Sunset: 6:00 PM
    private static func defaultSunriseSunset(
        for date: Date,
        timezone: TimeZone
    ) -> (sunrise: Date, sunset: Date) {
        
        var calendar = Calendar.current
        calendar.timeZone = timezone
        
        var sunriseComponents = calendar.dateComponents([.year, .month, .day], from: date)
        sunriseComponents.hour = 6
        sunriseComponents.minute = 0
        
        var sunsetComponents = calendar.dateComponents([.year, .month, .day], from: date)
        sunsetComponents.hour = 18
        sunsetComponents.minute = 0
        
        let sunrise = calendar.date(from: sunriseComponents) ?? date
        let sunset = calendar.date(from: sunsetComponents) ?? date
        
        return (sunrise, sunset)
    }
}

// MARK: - Helper Extensions

extension SunriseCalculator {
    
    /// Formats sunrise/sunset time for display
    /// - Parameter date: The sunrise or sunset date
    /// - Returns: Formatted time string (e.g., "6:15 AM")
    static func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        formatter.dateStyle = .none
        return formatter.string(from: date)
    }
    
    /// Applies offset to a time
    /// - Parameters:
    ///   - date: Original time
    ///   - offsetMinutes: Offset in minutes (positive or negative)
    /// - Returns: Adjusted date
    static func applyOffset(_ date: Date, offsetMinutes: Int) -> Date {
        return Calendar.current.date(byAdding: .minute, value: offsetMinutes, to: date) ?? date
    }
}
