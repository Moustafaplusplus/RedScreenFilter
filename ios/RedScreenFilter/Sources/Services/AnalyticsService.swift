//
//  AnalyticsService.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import CoreData
import Combine
import OSLog

/// AnalyticsService - Manages usage event logging and statistics
/// Provides methods to log events and query analytics data
class AnalyticsService {
    // MARK: - Singleton
    
    static let shared = AnalyticsService()
    
    // MARK: - Properties
    
    private let coreDataStack: CoreDataStack
    private var lastLoggedOpacity: Float = 0.0
    private var lastLoggedPreset: String = ""
    private var lastEventTimestamp: Date = Date(timeIntervalSince1970: 0)
    
    /// Minimum time between logging similar events (prevents spam)
    private let minimumLogInterval: TimeInterval = 60 // 1 minute
    
    // MARK: - Initialization
    
    private init() {
        self.coreDataStack = CoreDataStack.shared
    }
    
    // MARK: - Event Logging
    
    /// Log a usage event (overlay toggled, settings changed, etc.)
    /// - Parameters:
    ///   - overlayEnabled: Whether overlay is currently enabled
    ///   - opacity: Current opacity value
    ///   - preset: Current preset name
    func logEvent(overlayEnabled: Bool, opacity: Float, preset: String) {
        // Create event in background context to avoid blocking UI
        let context = coreDataStack.backgroundContext
        
        context.perform {
            let event = UsageEvent(
                context: context,
                overlayEnabled: overlayEnabled,
                opacity: opacity,
                preset: preset
            )
            
            self.coreDataStack.saveBackgroundContext(context)
            
            AppLogger.analytics.debug("Logged event - Enabled: \(overlayEnabled), Opacity: \(opacity), Preset: \(preset)")
            
            // Update daily stats
            self.updateDailyStats(context: context, preset: preset)
        }
    }
    
    /// Log overlay toggle event
    /// - Parameter isEnabled: Whether overlay is now enabled
    func logOverlayToggled(isEnabled: Bool, opacity: Float, preset: String) {
        logEvent(overlayEnabled: isEnabled, opacity: opacity, preset: preset)
    }
    
    /// Log opacity change (throttled to prevent excessive logging)
    /// - Parameters:
    ///   - opacity: New opacity value
    ///   - preset: Current preset
    func logOpacityChanged(opacity: Float, preset: String) {
        let now = Date()
        let timeSinceLastLog = now.timeIntervalSince(lastEventTimestamp)
        
        // Only log if significant change or enough time has passed
        let opacityDifference = abs(opacity - lastLoggedOpacity)
        
        if opacityDifference > 0.1 && timeSinceLastLog >= minimumLogInterval {
            logEvent(overlayEnabled: true, opacity: opacity, preset: preset)
            lastLoggedOpacity = opacity
            lastEventTimestamp = now
        }
    }
    
    /// Log preset application
    /// - Parameters:
    ///   - preset: Preset name
    ///   - opacity: Opacity value from preset
    func logPresetApplied(preset: String, opacity: Float) {
        let now = Date()
        let timeSinceLastLog = now.timeIntervalSince(lastEventTimestamp)
        
        // Only log if preset changed or enough time has passed
        if preset != lastLoggedPreset || timeSinceLastLog >= minimumLogInterval {
            logEvent(overlayEnabled: true, opacity: opacity, preset: preset)
            lastLoggedPreset = preset
            lastEventTimestamp = now
        }
    }
    
    // MARK: - Daily Stats Management
    
    /// Update or create daily stats entry
    /// - Parameters:
    ///   - context: Managed object context
    ///   - preset: Current preset name
    private func updateDailyStats(context: NSManagedObjectContext, preset: String) {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        
        // Fetch or create today's stats
        let fetchRequest = DailyStats.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "date >= %@ AND date < %@", 
                                             today as NSDate, 
                                             calendar.date(byAdding: .day, value: 1, to: today)! as NSDate)
        
        do {
            let results = try context.fetch(fetchRequest)
            
            if let todayStats = results.first {
                // Update existing stats
                todayStats.totalUsage += 1 // Increment by 1 second (approximate)
                todayStats.preset = preset // Update to latest preset
            } else {
                // Create new stats for today
                _ = DailyStats(context: context, date: today, totalUsage: 0, preset: preset)
            }
            
            try context.save()
        } catch {
            AppLogger.analytics.error("Failed to update daily stats", error: error)
        }
    }
    
    // MARK: - Statistics Queries
    
    /// Get hourly stats for a specific date
    /// - Parameter date: Date to query
    /// - Returns: Array of usage events grouped by hour
    func getHourlyStats(for date: Date) -> [UsageEvent] {
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        let endOfDay = calendar.date(byAdding: .day, value: 1, to: startOfDay)!
        
        return coreDataStack.fetchUsageEvents(from: startOfDay, to: endOfDay)
    }
    
    /// Get usage streak count (consecutive days with usage)
    /// - Returns: Number of consecutive days
    func getStreakCount() -> Int {
        let allStats = coreDataStack.fetchAllDailyStats()
        
        guard !allStats.isEmpty else {
            return 0
        }
        
        let calendar = Calendar.current
        var streakCount = 0
        var currentDate = calendar.startOfDay(for: Date())
        
        for stats in allStats {
            let statsDate = calendar.startOfDay(for: stats.date)
            
            if calendar.isDate(statsDate, inSameDayAs: currentDate) {
                streakCount += 1
                // Move to previous day
                currentDate = calendar.date(byAdding: .day, value: -1, to: currentDate)!
            } else {
                // Streak broken
                break
            }
        }
        
        return streakCount
    }
    
    /// Get total usage time today in seconds
    /// - Returns: Total seconds of usage
    func getTotalTimeTodayInSeconds() -> Int {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        
        if let todayStats = coreDataStack.fetchDailyStats(for: today) {
            return Int(todayStats.totalUsage)
        }
        
        return 0
    }
    
    /// Get total usage time for a date range
    /// - Parameters:
    ///   - startDate: Start date
    ///   - endDate: End date
    /// - Returns: Total seconds of usage
    func getTotalUsage(from startDate: Date, to endDate: Date) -> TimeInterval {
        let events = coreDataStack.fetchUsageEvents(from: startDate, to: endDate)
        
        // Calculate total time overlay was enabled
        var totalTime: TimeInterval = 0
        var lastEnabledTimestamp: Date? = nil
        
        for event in events {
            if event.overlayEnabled {
                lastEnabledTimestamp = event.timestamp
            } else if let lastEnabled = lastEnabledTimestamp {
                // Overlay was disabled, calculate duration
                let duration = event.timestamp.timeIntervalSince(lastEnabled)
                totalTime += duration
                lastEnabledTimestamp = nil
            }
        }
        
        // If still enabled at end of range, count up to endDate
        if let lastEnabled = lastEnabledTimestamp {
            totalTime += endDate.timeIntervalSince(lastEnabled)
        }
        
        return totalTime
    }
    
    /// Get most used preset
    /// - Returns: Preset name and usage count
    func getMostUsedPreset() -> (preset: String, count: Int)? {
        let allEvents = coreDataStack.fetchAllUsageEvents()
        
        // Count occurrences of each preset
        var presetCounts: [String: Int] = [:]
        
        for event in allEvents where event.overlayEnabled {
            presetCounts[event.preset, default: 0] += 1
        }
        
        // Find most used
        guard let mostUsed = presetCounts.max(by: { $0.value < $1.value }) else {
            return nil
        }
        
        return (preset: mostUsed.key, count: mostUsed.value)
    }
    
    /// Get average opacity used
    /// - Returns: Average opacity (0.0-1.0)
    func getAverageOpacity() -> Float {
        let allEvents = coreDataStack.fetchAllUsageEvents()
        
        guard !allEvents.isEmpty else {
            return 0.5 // Default
        }
        
        let totalOpacity = allEvents.reduce(0.0) { $0 + $1.opacity }
        return totalOpacity / Float(allEvents.count)
    }
    
    /// Get weekly statistics
    /// - Returns: Dictionary with days and usage times
    func getWeeklyStats() -> [Date: TimeInterval] {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        let weekAgo = calendar.date(byAdding: .day, value: -7, to: today)!
        
        var weeklyData: [Date: TimeInterval] = [:]
        
        for dayOffset in 0..<7 {
            let day = calendar.date(byAdding: .day, value: -dayOffset, to: today)!
            let dayStart = calendar.startOfDay(for: day)
            let dayEnd = calendar.date(byAdding: .day, value: 1, to: dayStart)!
            
            let usage = getTotalUsage(from: dayStart, to: dayEnd)
            weeklyData[dayStart] = usage
        }
        
        return weeklyData
    }
    
    /// Get monthly statistics
    /// - Returns: Dictionary with days and usage times
    func getMonthlyStats() -> [Date: TimeInterval] {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        
        var monthlyData: [Date: TimeInterval] = [:]
        
        for dayOffset in 0..<30 {
            let day = calendar.date(byAdding: .day, value: -dayOffset, to: today)!
            let dayStart = calendar.startOfDay(for: day)
            let dayEnd = calendar.date(byAdding: .day, value: 1, to: dayStart)!
            
            let usage = getTotalUsage(from: dayStart, to: dayEnd)
            monthlyData[dayStart] = usage
        }
        
        return monthlyData
    }
    
    // MARK: - Reset & Cleanup
    
    /// Delete all analytics data (for testing or user reset)
    func resetAllData() {
        coreDataStack.deleteAllUsageEvents()
        coreDataStack.deleteAllDailyStats()
        AppLogger.analytics.success("All analytics data deleted")
    }
}
