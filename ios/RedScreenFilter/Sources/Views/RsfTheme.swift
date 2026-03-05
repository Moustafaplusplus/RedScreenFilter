//
//  RsfTheme.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct RsfTheme {
    struct colors {
        static let primary = Color(red: 0.90, green: 0.12, blue: 0.12)
        static let secondary = Color(red: 0.94, green: 0.45, blue: 0.18)
        static let tertiary = Color(red: 0.95, green: 0.22, blue: 0.40)

        static let background = Color(red: 0.07, green: 0.07, blue: 0.08)
        static let surface = Color(red: 0.12, green: 0.12, blue: 0.14)
        static let surfaceVariant = Color(red: 0.16, green: 0.16, blue: 0.19)

        static let onBackground = Color.white
        static let onSurface = Color.white
        static let onSurfaceVariant = Color.white.opacity(0.72)
        static let onPrimary = Color.white

        static let glass = Color.white.opacity(0.06)
        static let glassStroke = Color.white.opacity(0.15)

        static let error = Color(red: 0.85, green: 0.18, blue: 0.18)
        static let errorContainer = Color(red: 0.28, green: 0.12, blue: 0.12)
        static let warning = Color(red: 0.95, green: 0.66, blue: 0.10)
    }

    struct spacing {
        static let xs: CGFloat = 6
        static let sm: CGFloat = 10
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let xl: CGFloat = 32
    }

    struct radius {
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
    }

    struct elevation {
        static let lg: CGFloat = 10
    }

    struct border {
        static let thin: CGFloat = 1
    }
}

struct RsfCard<Content: View>: View {
    let backgroundColor: Color
    let padding: CGFloat
    let content: Content

    init(backgroundColor: Color = RsfTheme.colors.surfaceVariant, padding: CGFloat = RsfTheme.spacing.md, @ViewBuilder content: () -> Content) {
        self.backgroundColor = backgroundColor
        self.padding = padding
        self.content = content()
    }

    var body: some View {
        content
            .padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(backgroundColor)
            .cornerRadius(RsfTheme.radius.lg)
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.lg)
                    .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
            )
    }
}

struct RsfSectionHeader: View {
    let title: String
    let subtitle: String

    init(_ title: String, subtitle: String) {
        self.title = title
        self.subtitle = subtitle
    }

    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
            Text(title)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(RsfTheme.colors.onBackground)
            Text(subtitle)
                .font(.caption)
                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct RsfSwitch: View {
    @Binding var isOn: Bool
    let label: String

    var body: some View {
        Toggle(isOn: $isOn) {
            Text(label)
                .foregroundColor(RsfTheme.colors.onSurface)
        }
        .toggleStyle(SwitchToggleStyle(tint: RsfTheme.colors.primary))
    }
}

struct RsfStatCard: View {
    let label: String
    let value: String
    let icon: String

    var body: some View {
        RsfCard {
            HStack(spacing: RsfTheme.spacing.md) {
                Image(systemName: icon)
                    .foregroundColor(RsfTheme.colors.primary)
                    .frame(width: 24)
                VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
                    Text(label)
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                    Text(value)
                        .font(.headline)
                        .foregroundColor(RsfTheme.colors.onSurface)
                }
                Spacer()
            }
        }
    }
}

struct RsfColorPill: View {
    let color: Color
    let label: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: RsfTheme.spacing.xs) {
                Circle()
                    .fill(color)
                    .frame(width: 30, height: 30)
                    .overlay(
                        Circle()
                            .stroke(Color.white.opacity(0.7), lineWidth: 1)
                    )
                Text(label)
                    .font(.caption2)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
            .padding(RsfTheme.spacing.sm)
            .frame(maxWidth: .infinity)
            .background(isSelected ? RsfTheme.colors.primary.opacity(0.12) : RsfTheme.colors.glass)
            .cornerRadius(RsfTheme.radius.md)
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .stroke(isSelected ? RsfTheme.colors.primary : RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
            )
        }
        .buttonStyle(.plain)
    }
}
