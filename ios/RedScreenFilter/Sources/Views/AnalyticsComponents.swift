//
//  AnalyticsComponents.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import SwiftUI

struct AnimatedStatCard: View {
    let label: String
    let value: String
    let icon: String
    let color: Color

    @State private var appeared = false

    var body: some View {
        HStack(spacing: RsfTheme.spacing.sm) {
            Image(systemName: icon)
                .foregroundColor(color)
                .frame(width: 24)

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                Text(value)
                    .font(.headline)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }

            Spacer()
        }
        .padding(RsfTheme.spacing.md)
        .background(RsfTheme.colors.surface)
        .cornerRadius(RsfTheme.radius.md)
        .scaleEffect(appeared ? 1.0 : 0.96)
        .opacity(appeared ? 1.0 : 0.0)
        .onAppear {
            withAnimation(.easeOut(duration: 0.25)) {
                appeared = true
            }
        }
    }
}

struct StreakBadge: View {
    let streakCount: Int
    let emoji: String

    var body: some View {
        HStack(spacing: RsfTheme.spacing.sm) {
            Text(emoji)
                .font(.title3)

            Text("\(streakCount)-day streak")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(RsfTheme.colors.onSurface)

            Spacer()
        }
        .padding(RsfTheme.spacing.md)
        .background(RsfTheme.colors.surface)
        .cornerRadius(RsfTheme.radius.md)
    }
}

struct UsageChartView: View {
    let data: [UsageDataPoint]

    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
            Text("Usage Trend")
                .font(.headline)
                .foregroundColor(RsfTheme.colors.onSurface)

            if data.isEmpty {
                Text("No usage data available")
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
            } else {
                let maxValue = max(data.map(\.usageHours).max() ?? 1.0, 1.0)

                VStack(spacing: RsfTheme.spacing.xs) {
                    ForEach(data) { point in
                        HStack(spacing: RsfTheme.spacing.sm) {
                            Text(point.label)
                                .font(.caption)
                                .frame(width: 56, alignment: .leading)
                                .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                            GeometryReader { proxy in
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(RsfTheme.colors.primary.opacity(0.75))
                                    .frame(width: max(6, proxy.size.width * (point.usageHours / maxValue)), height: 10)
                            }
                            .frame(height: 10)

                            Text(String(format: "%.1fh", point.usageHours))
                                .font(.caption)
                                .foregroundColor(RsfTheme.colors.onSurface)
                                .frame(width: 42, alignment: .trailing)
                        }
                    }
                }
            }
        }
        .padding(RsfTheme.spacing.md)
        .background(RsfTheme.colors.surface)
        .cornerRadius(RsfTheme.radius.md)
    }
}
