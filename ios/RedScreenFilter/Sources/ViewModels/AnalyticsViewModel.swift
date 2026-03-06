//
//  AnalyticsViewModel.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import Combine

/// AnalyticsViewModel - Prepares analytics data for display
/// Formats time intervals, calculates percentages, and manages period selection
class AnalyticsViewModel: ObservableObject {
    // MARK: - Published Properties
    
    @Published var selectedPeriod: AnalyticsPeriod = .today
    @Published var totalUsageTime: String = "0m"
    @Published var averageOpacity: String = "0%"
    @Published var mostUsedPreset: String = "None"
    @Published var currentStreak: String = "0 days"
    @Published var usageData: [DayUsageData] = []
    @Published var isLoading: Bool = false
    
    // MARK: - Dependencies
    
    private let analyticsService: AnalyticsService
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - Analytics Period Enum
    
    enum AnalyticsPeriod: Int, CaseIterable {
        case today = 0
        case week = 1
        case month = 2
        
        var title: String {
            switch self {
            case .today: return "Today"
            case .week: return "Week"
            case .month: return "Month"
            }
        }
    }
    
    // MARK: - Initialization
    
    init(analyticsService: AnalyticsService = .shared) {
        self.analyticsService = analyticsService
        setupBindings()
        loadAnalytics()
    }
    
    // MARK: - Setup
    
    private func setupBindings() {
        // Reload analytics when period changes
        $selectedPeriod
            .sink { [weak self] _ in
                self?.loadAnalytics()
            }
            .store(in: &cancellables)
    }
    
    // MARK: - Data Loading
    
    /// Load analytics data for the selected period
    func loadAnalytics() {
        isLoading = true
        
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self = self else { return }
            
            let calendar = Calendar.current
            let now = Date()
            let startDate: Date
            let endDate = now
            
            // Determine date range based on period
            switch self.selectedPeriod {
            case .today:
                startDate = calendar.startOfDay(for: now)
            case .week:
                startDate = calendar.date(byAdding: .day, value: -7, to: now) ?? now
            case .month:
                startDate = calendar.date(byAdding: .day, value: -30, to: now) ?? now
            }
            
            // Fetch statistics
            let totalUsage = self.analyticsService.getTotalUsage(from: startDate, to: endDate)
            let avgOpacity = self.analyticsService.getAverageOpacity()
            let mostUsed = self.analyticsService.getMostUsedPreset()
            let streak = self.analyticsService.getStreakCount()
            
            // Fetch daily data for charts
            let dailyData = self.loadDailyUsageData(from: startDate, to: endDate)
            
            // Update UI on main thread
            DispatchQueue.main.async {
                self.totalUsageTime = self.formatTimeInterval(totalUsage)
                self.averageOpacity = "\(Int(avgOpacity * 100))%"
                self.mostUsedPreset = mostUsed?.preset ?? "None"
                self.currentStreak = streak > 0 ? "\(streak) \(streak == 1 ? "day" : "days")" : "0 days"
                self.usageData = dailyData
                self.isLoading = false
            }
        }
    }
    
    /// Load daily usage data for chart visualization
    private func loadDailyUsageData(from startDate: Date, to endDate: Date) -> [DayUsageData] {
        let calendar = Calendar.current
        var data: [DayUsageData] = []
        var currentDate = calendar.startOfDay(for: startDate)
        
        while currentDate <= endDate {
            let nextDay = calendar.date(byAdding: .day, value: 1, to: currentDate)!
            let usage = analyticsService.getTotalUsage(from: currentDate, to: min(nextDay, endDate))
            
            let dayName: String
            if calendar.isDateInToday(currentDate) {
                dayName = "Today"
            } else if selectedPeriod == .today {
                dayName = formatHour(currentDate)
            } else {
                dayName = formatDayName(currentDate)
            }
            
            data.append(DayUsageData(
                date: currentDate,
                label: dayName,
                usageSeconds: Int(usage),
                usageFormatted: formatTimeInterval(usage, shortForm: true)
            ))
            
            currentDate = nextDay
        }
        
        return data
    }
    
    // MARK: - Formatting Methods
    
    /// Format time interval into human-readable string
    /// - Parameters:
    ///   - interval: Time interval in seconds
    ///   - shortForm: Use short form (e.g., "2h 30m" vs "2 hours 30 minutes")
    /// - Returns: Formatted string
    func formatTimeInterval(_ interval: TimeInterval, shortForm: Bool = false) -> String {
        let hours = Int(interval) / 3600
        let minutes = (Int(interval) % 3600) / 60
        let seconds = Int(interval) % 60
        
        if shortForm {
            if hours > 0 {
                return minutes > 0 ? "\(hours)h \(minutes)m" : "\(hours)h"
            } else if minutes > 0 {
                return "\(minutes)m"
            } else {
                return "\(seconds)s"
            }
        } else {
            if hours > 0 {
                let hourStr = hours == 1 ? "hour" : "hours"
                if minutes > 0 {
                    let minStr = minutes == 1 ? "minute" : "minutes"
                    return "\(hours) \(hourStr) \(minutes) \(minStr)"
                } else {
                    return "\(hours) \(hourStr)"
                }
            } else if minutes > 0 {
                let minStr = minutes == 1 ? "minute" : "minutes"
                return "\(minutes) \(minStr)"
            } else {
                let secStr = seconds == 1 ? "second" : "seconds"
                return "\(seconds) \(secStr)"
            }
        }
    }
    
    /// Format day name for chart labels
    private func formatDayName(_ date: Date) -> String {
        let formatter = DateFormatter()
        
        switch selectedPeriod {
        case .today:
            formatter.dateFormat = "HH:mm"
        case .week:
            formatter.dateFormat = "EEE" // Mon, Tue, etc.
        case .month:
            formatter.dateFormat = "d" // Day number
        }
        
        return formatter.string(from: date)
    }
    
    /// Format hour for today view
    private func formatHour(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
    
    /// Calculate percentage change from previous period
    func calculatePercentageChange(current: TimeInterval, previous: TimeInterval) -> String {
        guard previous > 0 else {
            return current > 0 ? "+100%" : "0%"
        }
        
        let change = ((current - previous) / previous) * 100
        let sign = change >= 0 ? "+" : ""
        return "\(sign)\(Int(change))%"
    }
    
    /// Get streak emoji based on count
    func getStreakEmoji(_ count: Int) -> String {
        switch count {
        case 0: return "🌱"
        case 1...3: return "🔥"
        case 4...7: return "⚡️"
        case 8...14: return "🌟"
        case 15...30: return "💎"
        default: return "👑"
        }
    }
    
    /// Get usage level description
    func getUsageLevelDescription(hours: Double) -> String {
        switch hours {
        case 0..<1: return "Light usage"
        case 1..<3: return "Moderate usage"
        case 3..<6: return "Heavy usage"
        default: return "Very heavy usage"
        }
    }
    
    // MARK: - Refresh
    
    /// Manually refresh analytics data
    func refresh() {
        loadAnalytics()
    }
}

// MARK: - Supporting Models

/// Represents usage data for a single day
struct DayUsageData: Identifiable {
    let id = UUID()
    let date: Date
    let label: String
    let usageSeconds: Int
    let usageFormatted: String
    
    /// Normalized value for chart (0.0 - 1.0)
    var normalizedValue: Double {
        // Normalize to hours (max 12 hours for scale)
        let hours = Double(usageSeconds) / 3600.0
        return min(hours / 12.0, 1.0)
    }
    
    /// Usage in hours (for display)
    var usageHours: Double {
        return Double(usageSeconds) / 3600.0
    }
}
