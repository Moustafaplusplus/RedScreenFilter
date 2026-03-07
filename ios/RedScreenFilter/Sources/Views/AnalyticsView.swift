//
//  AnalyticsView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Updated with full analytics on March 6, 2026.
//

import SwiftUI

struct AnalyticsView: View {
    @StateObject private var viewModel = AnalyticsViewModel()
    @State private var showingRefreshAnimation = false
    
    var body: some View {
        ZStack {
            RsfTheme.colors.background
                .ignoresSafeArea()
            
            NavigationView {
                ScrollView {
                    VStack(spacing: RsfTheme.spacing.lg) {
                        // Period Picker
                        VStack(spacing: RsfTheme.spacing.sm) {
                            Picker("Period", selection: $viewModel.selectedPeriod) {
                                Text("Today").tag(AnalyticsViewModel.AnalyticsPeriod.today)
                                Text("Week").tag(AnalyticsViewModel.AnalyticsPeriod.week)
                                Text("Month").tag(AnalyticsViewModel.AnalyticsPeriod.month)
                            }
                            .pickerStyle(.segmented)
                            .padding(.horizontal, RsfTheme.spacing.md)
                            
                            // Period description
                            Text(periodDescription)
                                .font(.caption)
                                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                        }
                        .padding(.top, RsfTheme.spacing.md)
                        
                        // Loading indicator
                        if viewModel.isLoading {
                            ProgressView()
                                .scaleEffect(1.5)
                                .padding(RsfTheme.spacing.xl)
                        } else {
                            // Statistics Cards
                            VStack(spacing: RsfTheme.spacing.md) {
                                AnimatedStatCard(
                                    label: "Total Usage",
                                    value: viewModel.totalUsageTime,
                                    icon: "clock.fill",
                                    color: .blue
                                )
                                
                                HStack(spacing: RsfTheme.spacing.md) {
                                    AnimatedStatCard(
                                        label: "Avg Opacity",
                                        value: viewModel.averageOpacity,
                                        icon: "slider.horizontal.3",
                                        color: .purple
                                    )
                                    
                                    AnimatedStatCard(
                                        label: "Top Preset",
                                        value: viewModel.mostUsedPreset,
                                        icon: "star.fill",
                                        color: .orange
                                    )
                                }
                            }
                            .padding(.horizontal, RsfTheme.spacing.md)
                            
                            // Streak Badge
                            StreakBadge(
                                streakCount: extractStreakNumber(from: viewModel.currentStreak),
                                emoji: viewModel.getStreakEmoji(extractStreakNumber(from: viewModel.currentStreak))
                            )
                            .padding(.horizontal, RsfTheme.spacing.md)
                            
                            // Usage Chart
                            UsageChartView(data: viewModel.usageData)
                                .padding(.horizontal, RsfTheme.spacing.md)
                            
                            // Insights Section
                            if !viewModel.usageData.isEmpty {
                                InsightsSection(viewModel: viewModel)
                                    .padding(.horizontal, RsfTheme.spacing.md)
                            }
                            
                            // Tips Section
                            TipsCard()
                                .padding(.horizontal, RsfTheme.spacing.md)
                                .padding(.bottom, RsfTheme.spacing.xl)
                        }
                    }
                }
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .principal) {
                        HStack(spacing: RsfTheme.spacing.xs) {
                            Image(systemName: "chart.bar.fill")
                                .foregroundColor(RsfTheme.colors.primary)
                            Text("Analytics")
                                .font(.headline)
                                .fontWeight(.semibold)
                                .foregroundColor(RsfTheme.colors.onBackground)
                        }
                    }
                    
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(action: {
                            withAnimation {
                                showingRefreshAnimation = true
                            }
                            viewModel.refresh()
                            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                                withAnimation {
                                    showingRefreshAnimation = false
                                }
                            }
                        }) {
                            Image(systemName: "arrow.clockwise")
                                .foregroundColor(RsfTheme.colors.primary)
                                .rotationEffect(.degrees(showingRefreshAnimation ? 360 : 0))
                        }
                    }
                }
            }
        }
    }
    
    private var periodDescription: String {
        switch viewModel.selectedPeriod {
        case .today:
            return "Your usage statistics for today"
        case .week:
            return "Your usage over the past 7 days"
        case .month:
            return "Your usage over the past 30 days"
        }
    }
    
    private func extractStreakNumber(from text: String) -> Int {
        let components = text.components(separatedBy: " ")
        return Int(components.first ?? "0") ?? 0
    }
}

// MARK: - Insights Section

struct InsightsSection: View {
    @ObservedObject var viewModel: AnalyticsViewModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
            Text("Insights")
                .font(.headline)
                .foregroundColor(RsfTheme.colors.onSurface)
            
            VStack(spacing: RsfTheme.spacing.sm) {
                if let totalHours = calculateTotalHours() {
                    InsightRow(
                        icon: "lightbulb.fill",
                        text: viewModel.getUsageLevelDescription(hours: totalHours),
                        color: .yellow
                    )
                }
                
                if extractStreakNumber(from: viewModel.currentStreak) >= 7 {
                    InsightRow(
                        icon: "trophy.fill",
                        text: "Amazing! You've maintained a week-long streak",
                        color: .orange
                    )
                }
                
                if viewModel.averageOpacity.hasPrefix("7") || viewModel.averageOpacity.hasPrefix("8") || viewModel.averageOpacity.hasPrefix("9") {
                    InsightRow(
                        icon: "moon.fill",
                        text: "High opacity usage - great for evening sessions",
                        color: .purple
                    )
                }
                
                InsightRow(
                    icon: "eye.fill",
                    text: "Remember to take 20-second breaks every 20 minutes",
                    color: .green
                )
            }
            .padding(RsfTheme.spacing.md)
            .background(RsfTheme.colors.surface)
            .cornerRadius(RsfTheme.radius.md)
            .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
        }
    }
    
    private func extractStreakNumber(from text: String) -> Int {
        let components = text.components(separatedBy: " ")
        return Int(components.first ?? "0") ?? 0
    }
    
    private func calculateTotalHours() -> Double? {
        let total = viewModel.usageData.reduce(0.0) { $0 + $1.usageHours }
        return total > 0 ? total : nil
    }
}

struct InsightRow: View {
    let icon: String
    let text: String
    let color: Color
    
    var body: some View {
        HStack(spacing: RsfTheme.spacing.sm) {
            Image(systemName: icon)
                .foregroundColor(color)
                .frame(width: 24)
            
            Text(text)
                .font(.caption)
                .foregroundColor(RsfTheme.colors.onSurface)
            
            Spacer()
        }
    }
}

// MARK: - Tips Card

struct TipsCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
            HStack(spacing: RsfTheme.spacing.sm) {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(RsfTheme.colors.primary)
                Text("Eye Health Tips")
                    .font(.headline)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
            
            VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
                TipRow(number: "1", text: "Use the red filter during evening hours")
                TipRow(number: "2", text: "Adjust opacity based on ambient lighting")
                TipRow(number: "3", text: "Enable 20-20-20 reminders in Settings")
                TipRow(number: "4", text: "Maintain a consistent usage streak")
            }
        }
        .padding(RsfTheme.spacing.md)
        .background(
            LinearGradient(
                gradient: Gradient(colors: [
                    RsfTheme.colors.primary.opacity(0.1),
                    RsfTheme.colors.surface
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(RsfTheme.radius.lg)
        .overlay(
            RoundedRectangle(cornerRadius: RsfTheme.radius.lg)
                .stroke(RsfTheme.colors.primary.opacity(0.2), lineWidth: 1)
        )
    }
}

struct TipRow: View {
    let number: String
    let text: String
    
    var body: some View {
        HStack(spacing: RsfTheme.spacing.sm) {
            Text(number)
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.white)
                .frame(width: 20, height: 20)
                .background(Circle().fill(RsfTheme.colors.primary))
            
            Text(text)
                .font(.caption)
                .foregroundColor(RsfTheme.colors.onSurface)
            
            Spacer()
        }
    }
}

struct AnalyticsView_Previews: PreviewProvider {
    static var previews: some View {
        AnalyticsView()
    }
}
