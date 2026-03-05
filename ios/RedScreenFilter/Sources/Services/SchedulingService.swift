//
//  SchedulingService.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

class SchedulingService {
    static let shared = SchedulingService()
    
    private let prefsManager = PreferencesManager.shared
    
    // MARK: - Public Methods
    
    func determineOverlayState() -> Bool {
        let settings = prefsManager.loadSettings()
        
        guard settings.scheduleEnabled else {
            return prefsManager.isOverlayEnabled()
        }
        
        return isCurrentTimeInSchedule(start: settings.scheduleStartTime, end: settings.scheduleEndTime)
    }
    
    func isScheduleActive() -> Bool {
        let settings = prefsManager.loadSettings()
        return settings.scheduleEnabled
    }
    
    func setSchedule(start: String, end: String) {
        var settings = prefsManager.loadSettings()
        settings.scheduleStartTime = start
        settings.scheduleEndTime = end
        settings.scheduleEnabled = true
        prefsManager.saveSettings(settings)
    }
    
    func disableSchedule() {
        var settings = prefsManager.loadSettings()
        settings.scheduleEnabled = false
        prefsManager.saveSettings(settings)
    }
    
    // MARK: - Private Methods
    
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
        if startMinutes > endMinutes {
            return currentMinutes >= startMinutes || currentMinutes < endMinutes
        } else {
            return currentMinutes >= startMinutes && currentMinutes < endMinutes
        }
    }
    
    private func parseTimeString(_ time: String) -> (Int, Int) {
        let components = time.split(separator: ":")
        let hour = Int(components.first ?? "0") ?? 0
        let minute = Int(components.last ?? "0") ?? 0
        return (hour, minute)
    }
}
