//
//  UsageChartView.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import SwiftUI

/// Custom bar chart for usage visualization
struct UsageChartView: View {
    let data: [DayUsageData]
    @State private var animatedValues: [Double] = []
    
    private let chartHeight: CGFloat = 200
    private let barSpacing: CGFloat = 8
    
    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
            // Chart Title
            Text("Usage Over Time")
                .font(.headline)
                .foregroundColor(RsfTheme.colors.onSurface)
            
            if data.isEmpty {
                // Empty state
                VStack(spacing: RsfTheme.spacing.sm) {
                    Image(systemName: "chart.bar")
                        .font(.system(size: 40))
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant.opacity(0.5))
                    Text("No usage data yet")
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
                .frame(height: chartHeight)
                .frame(maxWidth: .infinity)
            } else {
                // Chart
                GeometryReader { geometry in
                    let barWidth = (geometry.size.width - CGFloat(data.count - 1) * barSpacing) / CGFloat(data.count)
                    
                    HStack(alignment: .bottom, spacing: barSpacing) {
                        ForEach(Array(data.enumerated()), id: \.element.id) { index, item in
                            VStack(spacing: 4) {
                                // Bar
                                RoundedRectangle(cornerRadius: 4)
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [
                                                RsfTheme.colors.primary,
                                                RsfTheme.colors.primary.opacity(0.7)
                                            ]),
                                            startPoint: .top,
                                            endPoint: .bottom
                                        )
                                    )
                                    .frame(
                                        width: barWidth,
                                        height: chartHeight * CGFloat(getAnimatedValue(for: index))
                                    )
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 4)
                                            .stroke(RsfTheme.colors.primary.opacity(0.3), lineWidth: 1)
                                    )
                                
                                // Label
                                Text(item.label)
                                    .font(.caption2)
                                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    .lineLimit(1)
                                    .frame(width: barWidth)
                            }
                        }
                    }
                }
                .frame(height: chartHeight + 30) // Extra space for labels
                
                // Legend
                HStack(spacing: RsfTheme.spacing.sm) {
                    Circle()
                        .fill(RsfTheme.colors.primary)
                        .frame(width: 8, height: 8)
                    Text("Usage Time")
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
            }
        }
        .padding(RsfTheme.spacing.md)
        .background(RsfTheme.colors.surface)
        .cornerRadius(RsfTheme.cornerRadius.lg)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
        .onAppear {
            animateChart()
        }
        .onChange(of: data) { _ in
            animateChart()
        }
    }
    
    private func getAnimatedValue(for index: Int) -> Double {
        guard index < animatedValues.count else { return 0 }
        return animatedValues[index]
    }
    
    private func animateChart() {
        // Reset animation
        animatedValues = Array(repeating: 0.0, count: data.count)
        
        // Animate each bar with delay
        for (index, item) in data.enumerated() {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.7).delay(Double(index) * 0.05)) {
                if animatedValues.count > index {
                    animatedValues[index] = item.normalizedValue
                } else {
                    animatedValues.append(item.normalizedValue)
                }
            }
        }
    }
}

/// Animated stat card with icon and value
struct AnimatedStatCard: View {
    let label: String
    let value: String
    let icon: String
    let color: Color
    @State private var isAnimated = false
    
    var body: some View {
        HStack(spacing: RsfTheme.spacing.md) {
            // Icon
            ZStack {
                Circle()
                    .fill(color.opacity(0.2))
                    .frame(width: 50, height: 50)
                
                Image(systemName: icon)
                    .font(.system(size: 24))
                    .foregroundColor(color)
                    .scaleEffect(isAnimated ? 1.0 : 0.5)
                    .opacity(isAnimated ? 1.0 : 0.0)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                
                Text(value)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(RsfTheme.colors.onSurface)
                    .opacity(isAnimated ? 1.0 : 0.0)
                    .offset(y: isAnimated ? 0 : 10)
            }
            
            Spacer()
        }
        .padding(RsfTheme.spacing.md)
        .background(RsfTheme.colors.surface)
        .cornerRadius(RsfTheme.cornerRadius.md)
        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
        .onAppear {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.7).delay(0.1)) {
                isAnimated = true
            }
        }
    }
}

/// Streak badge with animation
struct StreakBadge: View {
    let streakCount: Int
    let emoji: String
    @State private var isAnimated = false
    @State private var isGlowing = false
    
    var body: some View {
        VStack(spacing: RsfTheme.spacing.sm) {
            // Emoji with glow effect
            ZStack {
                if streakCount > 0 {
                    Circle()
                        .fill(
                            RadialGradient(
                                gradient: Gradient(colors: [
                                    Color.orange.opacity(isGlowing ? 0.4 : 0.2),
                                    Color.orange.opacity(0.0)
                                ]),
                                center: .center,
                                startRadius: 20,
                                endRadius: 60
                            )
                        )
                        .frame(width: 100, height: 100)
                        .scaleEffect(isGlowing ? 1.2 : 1.0)
                }
                
                Text(emoji)
                    .font(.system(size: 60))
                    .scaleEffect(isAnimated ? 1.0 : 0.3)
                    .rotationEffect(.degrees(isAnimated ? 0 : -180))
            }
            
            // Streak count
            Text("\(streakCount) Day Streak")
                .font(.headline)
                .foregroundColor(RsfTheme.colors.onSurface)
                .opacity(isAnimated ? 1.0 : 0.0)
            
            if streakCount > 0 {
                Text("Keep it up! 🎯")
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                    .opacity(isAnimated ? 1.0 : 0.0)
            } else {
                Text("Start your streak today")
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                    .opacity(isAnimated ? 1.0 : 0.0)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(RsfTheme.spacing.lg)
        .background(RsfTheme.colors.surface)
        .cornerRadius(RsfTheme.cornerRadius.lg)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
        .onAppear {
            withAnimation(.spring(response: 0.8, dampingFraction: 0.6)) {
                isAnimated = true
            }
            
            // Glow animation for active streaks
            if streakCount > 0 {
                withAnimation(.easeInOut(duration: 2.0).repeatForever(autoreverses: true)) {
                    isGlowing = true
                }
            }
        }
    }
}

struct UsageChartView_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            UsageChartView(data: [
                DayUsageData(date: Date(), label: "Mon", usageSeconds: 3600, usageFormatted: "1h"),
                DayUsageData(date: Date(), label: "Tue", usageSeconds: 7200, usageFormatted: "2h"),
                DayUsageData(date: Date(), label: "Wed", usageSeconds: 5400, usageFormatted: "1.5h"),
                DayUsageData(date: Date(), label: "Thu", usageSeconds: 9000, usageFormatted: "2.5h"),
                DayUsageData(date: Date(), label: "Fri", usageSeconds: 4500, usageFormatted: "1.25h")
            ])
            
            StreakBadge(streakCount: 7, emoji: "⚡️")
        }
        .padding()
        .background(RsfTheme.colors.background)
    }
}
