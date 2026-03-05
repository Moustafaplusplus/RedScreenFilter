//
//  RsfComponents.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

// MARK: - Cards

/// RSF Card component with consistent styling
struct RsfCard<Content: View>: View {
    let content: Content
    var backgroundColor: Color = RsfTheme.colors.surfaceVariant
    var padding: CGFloat = RsfTheme.spacing.md
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    init(backgroundColor: Color = RsfTheme.colors.surfaceVariant, 
         padding: CGFloat = RsfTheme.spacing.md,
         @ViewBuilder content: () -> Content) {
        self.backgroundColor = backgroundColor
        self.padding = padding
        self.content = content()
    }
    
    var body: some View {
        content
            .padding(padding)
            .background(backgroundColor)
            .cornerRadius(RsfTheme.radius.lg)
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.lg)
                    .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
            )
            .shadow(color: RsfTheme.colors.primary.opacity(0.2), 
                   radius: RsfTheme.elevation.md, 
                   x: 0, y: RsfTheme.elevation.sm)
    }
}

// MARK: - Section Header

/// RSF Section Header with consistent styling
struct RsfSectionHeader: View {
    let title: String
    let subtitle: String?
    
    init(_ title: String, subtitle: String? = nil) {
        self.title = title
        self.subtitle = subtitle
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(RsfTheme.colors.onSurface)
            
            if let subtitle = subtitle {
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
            }
        }
    }
}

// MARK: - Stat Card

/// RSF Stat Card for displaying metrics
struct RsfStatCard: View {
    let label: String
    let value: String
    var icon: String? = nil
    
    var body: some View {
        RsfCard {
            VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
                HStack(spacing: RsfTheme.spacing.sm) {
                    if let icon = icon {
                        Image(systemName: icon)
                            .font(.system(size: 14))
                            .foregroundColor(RsfTheme.colors.primary)
                    }
                    
                    Text(label)
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
                
                Text(value)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
        }
    }
}

// MARK: - Buttons

/// RSF Primary Button
struct RsfPrimaryButton: View {
    let action: () -> Void
    let label: String
    var isLoading: Bool = false
    var isDisabled: Bool = false
    
    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(RsfTheme.colors.onPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, RsfTheme.spacing.md)
                .background(RsfTheme.colors.primary)
                .cornerRadius(RsfTheme.radius.md)
        }
        .disabled(isDisabled || isLoading)
        .opacity(isDisabled ? 0.5 : 1.0)
    }
}

/// RSF Secondary Button
struct RsfSecondaryButton: View {
    let action: () -> Void
    let label: String
    var isLoading: Bool = false
    var isDisabled: Bool = false
    
    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                .frame(maxWidth: .infinity)
                .padding(.vertical, RsfTheme.spacing.md)
                .background(RsfTheme.colors.surfaceVariant)
                .cornerRadius(RsfTheme.radius.md)
                .overlay(
                    RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                        .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
                )
        }
        .disabled(isDisabled || isLoading)
        .opacity(isDisabled ? 0.5 : 1.0)
    }
}

// MARK: - Toggle/Switch

/// RSF Custom Switch
struct RsfSwitch: View {
    @Binding var isOn: Bool
    let label: String?
    
    var body: some View {
        HStack(spacing: RsfTheme.spacing.md) {
            if let label = label {
                Text(label)
                    .font(.body)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
            
            Spacer()
            
            Toggle("", isOn: $isOn)
                .tint(RsfTheme.colors.primary)
        }
    }
}

// MARK: - Segmented Buttons

/// RSF Segmented Button Row for multi-choice selection
struct RsfSegmentedButtons: View {
    @Binding var selectedIndex: Int
    let options: [String]
    let columns: Int
    
    var body: some View {
        let rows = (options.count + columns - 1) / columns
        
        VStack(spacing: RsfTheme.spacing.xs) {
            ForEach(0..<rows, id: \.self) { row in
                HStack(spacing: RsfTheme.spacing.xs) {
                    ForEach(0..<columns, id: \.self) { col in
                        let index = row * columns + col
                        if index < options.count {
                            Button(action: {
                                selectedIndex = index
                            }) {
                                Text(options[index])
                                    .font(.caption)
                                    .fontWeight(.medium)
                                    .foregroundColor(
                                        selectedIndex == index ?
                                        RsfTheme.colors.onPrimary :
                                        RsfTheme.colors.onSurfaceVariant
                                    )
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, RsfTheme.spacing.sm)
                                    .background(
                                        selectedIndex == index ?
                                        RsfTheme.colors.primary :
                                        RsfTheme.colors.surfaceVariant
                                    )
                                    .cornerRadius(RsfTheme.radius.md)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                                            .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
                                    )
                            }
                        } else {
                            Spacer()
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Slider

/// RSF Slider for opacity/intensity control
struct RsfSlider: View {
    @Binding var value: Float
    let range: ClosedRange<Float>
    let label: String?
    let showPercentage: Bool
    
    init(value: Binding<Float>,
         range: ClosedRange<Float> = 0...1,
         label: String? = nil,
         showPercentage: Bool = true) {
        self._value = value
        self.range = range
        self.label = label
        self.showPercentage = showPercentage
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
            if let label = label {
                HStack {
                    Text(label)
                        .font(.body)
                        .fontWeight(.semibold)
                        .foregroundColor(RsfTheme.colors.onSurface)
                    
                    Spacer()
                    
                    if showPercentage {
                        Text(String(format: "%.0f%%", value * 100))
                            .font(.body)
                            .fontWeight(.bold)
                            .foregroundColor(RsfTheme.colors.primary)
                    }
                }
            }
            
            Slider(value: $value, in: range)
                .tint(RsfTheme.colors.primary)
        }
    }
}

// MARK: - Color Pills

/// RSF Color Pill for color variant selection
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
                    .frame(width: 48, height: 48)
                    .overlay(
                        Circle()
                            .stroke(
                                isSelected ?
                                RsfTheme.colors.onSurface :
                                RsfTheme.colors.glassStroke,
                                lineWidth: isSelected ? RsfTheme.border.medium : RsfTheme.border.thin
                            )
                    )
                
                Text(label)
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
        }
    }
}

// MARK: - Input Fields

/// RSF Text Field
struct RsfTextField: View {
    @Binding var text: String
    let placeholder: String
    let label: String?
    var icon: String?
    
    var body: some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
            if let label = label {
                Text(label)
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
            }
            
            HStack(spacing: RsfTheme.spacing.sm) {
                if let icon = icon {
                    Image(systemName: icon)
                        .foregroundColor(RsfTheme.colors.primary)
                }
                
                TextField(placeholder, text: $text)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
            .padding(RsfTheme.spacing.sm)
            .background(RsfTheme.colors.surfaceVariant)
            .cornerRadius(RsfTheme.radius.md)
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
            )
        }
    }
}
