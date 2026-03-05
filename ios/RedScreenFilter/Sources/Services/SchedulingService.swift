//
//  SchedulingService.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Enhanced with thread-safety in Phase 20-25%
//  Enhanced with background task scheduling in Phase 35-40%
//  Enhanced with location-based scheduling in Phase 40-50%
//

import Foundation
import BackgroundTasks
import os.log

/// SchedulingService - Manages time-based scheduling logic
/// Thread-safe using DispatchQueue for concurrent data access
/// Determines when overlay should be active based on user's schedule
/// Supports both manual time-based and location-based (sunrise/sunset) scheduling
class SchedulingService {
    static let shared = SchedulingService()
    
    private let prefsManager = PreferencesManager.shared
    private let locationService = LocationCalculationService.shared
    private let logger = OSLog(subsystem: "com.redscreenfilter", category: "Scheduling")
    
    // Thread-safe access using a concurrent queue with barrier for writes
    private let queue = DispatchQueue(label: "com.redscreenfilter.scheduling", attributes: .concurrent)
    
    // MARK: - Public Methods
    
    /// Determines if overlay should be active based on current schedule
    /// Thread-safe - can be called from any thread
    /// Prioritizes location-based schedule over time-based schedule
    /// - Returns: Boolean indicating if overlay should be enabled
    func determineOverlayState() -> Bool {
        return queue.sync {
            let settings = prefsManager.loadSettings()
            
            guard settings.scheduleEnabled else {
                return prefsManager.isOverlayEnabled()
            }
            
            // Prioritize location-based schedule if enabled
            if settings.useLocationSchedule {
                return isCurrentTimeInLocationSchedule()
            }
            
            // Fall back to time-based schedule
            return isCurrentTimeInSchedule(start: settings.scheduleStartTime, end: settings.scheduleEndTime)
        }
    }
    
    /// Checks if user has enabled schedule feature
    /// - Returns: Boolean indicating if schedule is active
    func isScheduleActive() -> Bool {
        return queue.sync {
            let settings = prefsManager.loadSettings()
            return settings.scheduleEnabled
        }
    }
    
    /// Sets schedule times and enables scheduling
    /// Thread-safe with barrier for write operations
    /// Triggers background task scheduling for automatic updates
    /// - Parameters:
    ///   - start: Start time in HH:mm format (e.g., "21:00")
    ///   - end: End time in HH:mm format (e.g., "07:00")
    func setSchedule(start: String, end: String) {
        queue.async(flags: .barrier) {
            var settings = self.prefsManager.loadSettings()
            settings.scheduleStartTime = start
            settings.scheduleEndTime = end
            settings.scheduleEnabled = true
            self.prefsManager.saveSettings(settings)
            
            // Schedule background task to automatically update overlay state
            self.requestBackgroundTaskScheduling()
        }
    }
    
    /// Disables scheduling feature
    /// Thread-safe with barrier for write operations
    /// Cancels any pending background tasks
    func disableSchedule() {
        queue.async(flags: .barrier) {
            var settings = self.prefsManager.loadSettings()
            settings.scheduleEnabled = false
            self.prefsManager.saveSettings(settings)
            
            // Cancel background task when schedule is disabled
            BGTaskScheduler.shared.cancel(taskIdentifier: BackgroundScheduleTask.taskIdentifier)
        }
    }
    
    /// Requests background task scheduling for automatic overlay updates
    /// Called when schedule settings change
    /// System determines actual execution time based on device conditions
    func requestBackgroundTaskScheduling() {
        BackgroundScheduleTask.scheduleBackgroundTask()
    }
    
    // MARK: - Location-Based Scheduling Methods
    
    /// Enables location-based scheduling using sunrise/sunset times
    /// Fetches initial location and times
    /// - Parameter offsetMinutes: Offset in minutes from actual sunrise/sunset
    func enableLocationSchedule(offsetMinutes: Int = 0) {
        queue.async(flags: .barrier) {
            var settings = self.prefsManager.loadSettings()
            settings.useLocationSchedule = true
            self.prefsManager.saveSettings(settings)
            
            // Fetch sunrise/sunset times
            self.locationService.fetchSunriseSunsetTimes()
            
            // Schedule background task
            self.requestBackgroundTaskScheduling()
            
            os_log("Location-based scheduling enabled with offset: %d minutes",
                   log: self.logger,
                   type: .info,
                   offsetMinutes)
        }
    }
    
    /// Disables location-based scheduling
    func disableLocationSchedule() {
        queue.async(flags: .barrier) {
            var settings = self.prefsManager.loadSettings()
            settings.useLocationSchedule = false
            self.prefsManager.saveSettings(settings)
            
            os_log("Location-based scheduling disabled", log: self.logger, type: .info)
        }
    }
    
    /// Manually refreshes location and sunrise/sunset times
    func refreshLocationTimes() {
        locationService.fetchSunriseSunsetTimes(forceRefresh: true)
        os_log("Manually refreshing location times", log: logger, type: .info)
    }
    
    /// Gets formatted sunrise/sunset times for display
    /// - Returns: Dictionary with "sunrise" and "sunset" keys
    func getFormattedSunriseSunsetTimes() -> [String: String] {
        return locationService.getFormattedTimes()
    }
    
    // MARK: - Private Methods
    
    /// Determines if current time is within location-based schedule
    /// Location schedule: active from sunset to sunrise
    /// - Returns: Boolean indicating if within schedule
    private func isCurrentTimeInLocationSchedule() -> Bool {
        guard let times = locationService.getSunriseSunsetWithOffset(offsetMinutes: 0) else {
            os_log("No location times available, falling back to manual schedule",
                   log: logger,
                   type: .warning)
            return false
        }
        
        let now = Date()
        let calendar = Calendar.current
        
        // Get today's dates for comparison
        let todaySunrise = times.sunrise
        let todaySunset = times.sunset
        
        // Active from sunset to sunrise (next day)
        // Check if current time is after sunset OR before sunrise
        if now >= todaySunset {
            // After sunset today - active until sunrise tomorrow
            return true
        } else if now < todaySunrise {
            // Before sunrise today - active (from yesterday's sunset)
            return true
        }
        
        return false
    }
    
    /// Determines if current time falls within scheduled window
    /// Handles day boundary crossing (e.g., 10 PM to 7 AM)
    /// - Parameters:
    ///   - start: Start time string in HH:mm format
    ///   - end: End time string in HH:mm format
    /// - Returns: Boolean indicating if current time is within schedule
    private func isCurrentTimeInSchedule(start: String, end: String) -> Bool {
        let calendar = Calendar.current
        let now = Date()
        let currentTime = calendar.dateComponents([.hour, .minute], from: now)
        
        let startComponents = parseTimeString(start)
        let endComponents = parseTimeString(end)
        
        let currentMinutes = (currentTime.hour ?? 0) * 60 + (currentTime.minute ?? 0)
        let startMinutes = startComponents.0 * 60 + startComponents.1
        let endMinutes = endComponents.0 * 60 + endComponents.1
        
        // Handle midnight crossing (e.g., 21:00 to 07:00)
        // If start time is after end time, it crosses midnight
        if startMinutes > endMinutes {
            // Active if current time is after start OR before end
            return currentMinutes >= startMinutes || currentMinutes < endMinutes
        } else {
            // Normal case: active if current time is between start and end
            return currentMinutes >= startMinutes && currentMinutes < endMinutes
        }
    }
    
    /// Parses time string in HH:mm format to hour and minute components
    /// - Parameter time: Time string (e.g., "21:00")
    /// - Returns: Tuple of (hour, minute)
    private func parseTimeString(_ time: String) -> (Int, Int) {
        let components = time.split(separator: ":")
        let hour = Int(components.first ?? "0") ?? 0
        let minute = Int(components.last ?? "0") ?? 0
        return (hour, minute)
    }
}
