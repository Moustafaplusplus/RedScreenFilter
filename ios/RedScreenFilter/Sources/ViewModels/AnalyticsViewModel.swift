//
//  AnalyticsViewModel.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import SwiftUI

struct UsageDataPoint: Identifiable {
    let id = UUID()
    let label: String
    let usageHours: Double
}

@MainActor
final class AnalyticsViewModel: ObservableObject {
    enum AnalyticsPeriod {
        case today
        case week
        case month
    }

    @Published var selectedPeriod: AnalyticsPeriod = .today {
        didSet {
            refresh()
        }
    }

    @Published var isLoading: Bool = false
    @Published var totalUsageTime: String = "0h 0m"
    @Published var averageOpacity: String = "0%"
    @Published var mostUsedPreset: String = "Standard"
    @Published var currentStreak: String = "0 days"
    @Published var usageData: [UsageDataPoint] = []

    private let preferences = PreferencesManager.shared
    private let analyticsService = AnalyticsService.shared

    init() {
        refresh()
    }

    func refresh() {
        isLoading = true

        let startDate = periodStartDate(for: selectedPeriod)
        let events = analyticsService.fetchEvents(from: startDate)
        let points = generateUsageData(for: selectedPeriod, events: events)
        usageData = points

        let totalHours = points.reduce(0.0) { $0 + $1.usageHours }
        totalUsageTime = formatHours(totalHours)

        let opacityPercent: Int
        let opacities = events.map(\.opacity)
        if !opacities.isEmpty {
            let avg = opacities.reduce(0, +) / Float(opacities.count)
            opacityPercent = Int((avg * 100).rounded())
        } else {
            opacityPercent = Int((preferences.getOpacity() * 100).rounded())
        }
        averageOpacity = "\(opacityPercent)%"

        mostUsedPreset = mostUsedPreset(from: events)
        currentStreak = "\(computeCurrentStreak(from: points)) days"

        isLoading = false
    }

    func getStreakEmoji(_ days: Int) -> String {
        switch days {
        case 14...:
            return "🔥"
        case 7...:
            return "⭐️"
        case 3...:
            return "👏"
        default:
            return "🙂"
        }
    }

    func getUsageLevelDescription(hours: Double) -> String {
        switch hours {
        case 0..<2:
            return "Light usage period. Keep a consistent routine."
        case 2..<6:
            return "Balanced usage. Good eye-care habits overall."
        default:
            return "High usage detected. Consider more frequent breaks."
        }
    }

    private func generateUsageData(for period: AnalyticsPeriod, events: [UsageEvent]) -> [UsageDataPoint] {
        let grouped = Dictionary(grouping: events) { event in
            bucketLabel(for: event.timestamp, period: period)
        }

        switch period {
        case .today:
            return [
                UsageDataPoint(label: "Morning", usageHours: usageHours(grouped["Morning"] ?? [])),
                UsageDataPoint(label: "Afternoon", usageHours: usageHours(grouped["Afternoon"] ?? [])),
                UsageDataPoint(label: "Evening", usageHours: usageHours(grouped["Evening"] ?? []))
            ]
        case .week:
            let labels = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
            return labels.map { label in
                UsageDataPoint(label: label, usageHours: usageHours(grouped[label] ?? []))
            }
        case .month:
            let labels = ["W1", "W2", "W3", "W4", "W5"]
            let points = labels.map { label in
                UsageDataPoint(label: label, usageHours: usageHours(grouped[label] ?? []))
            }
            if points.last?.usageHours == 0 {
                return Array(points.dropLast())
            }
            return points
        }
    }

    private func computeCurrentStreak(from points: [UsageDataPoint]) -> Int {
        points.reversed().prefix { $0.usageHours > 0.25 }.count
    }

    private func formatHours(_ hours: Double) -> String {
        let wholeHours = Int(hours)
        let minutes = Int((hours - Double(wholeHours)) * 60)
        return "\(wholeHours)h \(minutes)m"
    }

    private func periodStartDate(for period: AnalyticsPeriod) -> Date {
        let calendar = Calendar.current
        let now = Date()

        switch period {
        case .today:
            return calendar.startOfDay(for: now)
        case .week:
            return calendar.date(byAdding: .day, value: -6, to: calendar.startOfDay(for: now)) ?? now
        case .month:
            return calendar.date(byAdding: .day, value: -29, to: calendar.startOfDay(for: now)) ?? now
        }
    }

    private func usageHours(_ events: [UsageEvent]) -> Double {
        guard !events.isEmpty else { return 0 }
        let weighted = events.reduce(0.0) { partialResult, event in
            event.overlayEnabled ? partialResult + Double(event.opacity) : partialResult
        }
        return weighted / 2.0
    }

    private func bucketLabel(for date: Date, period: AnalyticsPeriod) -> String {
        let calendar = Calendar.current
        switch period {
        case .today:
            let hour = calendar.component(.hour, from: date)
            if hour < 12 { return "Morning" }
            if hour < 18 { return "Afternoon" }
            return "Evening"
        case .week:
            let weekday = calendar.component(.weekday, from: date)
            let labels = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
            return labels[max(0, min(labels.count - 1, weekday - 1))]
        case .month:
            let week = calendar.component(.weekOfMonth, from: date)
            return "W\(max(1, min(5, week)))"
        }
    }

    private func mostUsedPreset(from events: [UsageEvent]) -> String {
        let counts = events.reduce(into: [String: Int]()) { partialResult, event in
            partialResult[event.preset, default: 0] += 1
        }
        if let top = counts.max(by: { $0.value < $1.value })?.key, !top.isEmpty {
            return top
        }
        let current = preferences.getCurrentPreset()
        return current.isEmpty ? "Standard" : current
    }
}
