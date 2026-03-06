//
//  AccessibilityHelper.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//  Phase 99-100% - Accessibility & Dynamic Type Support
//

import SwiftUI

/// Accessibility helper providing consistent VoiceOver support and Dynamic Type scaling
struct AccessibilityHelper {
    
    // MARK: - Font Scaling
    
    /// Get scaled font size based on Dynamic Type preference
    static func scaledFont(size: CGFloat, weight: Font.Weight = .regular) -> Font {
        return .system(size: size, weight: weight, design: .default)
    }
    
    /// Small text (captions)
    static let caption: Font = {
        let baseSize: CGFloat = 12
        return .system(size: baseSize, weight: .regular, design: .default)
    }()
    
    /// Body text (standard)
    static let body: Font = {
        let baseSize: CGFloat = 16
        return .system(size: baseSize, weight: .regular, design: .default)
    }()
    
    /// Title text
    static let title: Font = {
        let baseSize: CGFloat = 20
        return .system(size: baseSize, weight: .semibold, design: .default)
    }()
    
    /// Large title
    static let largeTitle: Font = {
        let baseSize: CGFloat = 28
        return .system(size: baseSize, weight: .bold, design: .default)
    }()
    
    // MARK: - VoiceOver Labels
    
    /// Format opacity percentage for VoiceOver
    static func opacityLabel(for opacity: Float) -> String {
        return "Opacity: \(Int(opacity * 100)) percent"
    }
    
    /// Format time for VoiceOver
    static func timeLabel(for date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    /// Format duration for VoiceOver
    static func durationLabel(for seconds: TimeInterval) -> String {
        let hours = Int(seconds) / 3600
        let minutes = (Int(seconds) % 3600) / 60
        
        if hours > 0 {
            return "\(hours) hour\(hours == 1 ? "" : "s"), \(minutes) minute\(minutes == 1 ? "" : "s")"
        } else {
            return "\(minutes) minute\(minutes == 1 ? "" : "s")"
        }
    }
    
    // MARK: - Common Accessibility Labels
    
    static let toggleOn = "Toggle is on"
    static let toggleOff = "Toggle is off"
    static let settingsButton = "Settings"
    static let analyticsButton = "Analytics"
    static let presetsButton = "Presets"
    static let mainButton = "Main"
    static let closeButton = "Close"
    static let confirmButton = "Confirm"
    static let cancelButton = "Cancel"
    static let deleteButton = "Delete"
    static let editButton = "Edit"
    static let addButton = "Add"
    static let backButton = "Back"
}

// MARK: - View Modifiers for Accessibility

/// Apply standard accessibility formatting to text
struct AccessibleTextModifier: ViewModifier {
    let font: Font
    let weight: Font.Weight
    let lineLimit: Int?
    
    init(font: Font = AccessibilityHelper.body, weight: Font.Weight = .regular, lineLimit: Int? = nil) {
        self.font = font
        self.weight = weight
        self.lineLimit = lineLimit
    }
    
    func body(content: Content) -> some View {
        content
            .font(font)
            .lineLimit(lineLimit)
            .tracking(0.5) // Slight letter spacing for readability
    }
}

extension View {
    /// Apply accessibility-friendly text formatting
    func accessibleText(font: Font = AccessibilityHelper.body, 
                       weight: Font.Weight = .regular,
                       lineLimit: Int? = nil) -> some View {
        modifier(AccessibleTextModifier(font: font, weight: weight, lineLimit: lineLimit))
    }
    
    /// Set accessibility label with hint for buttons and interactive elements
    func accessibleButton(label: String, hint: String? = nil) -> some View {
        self
            .accessibilityLabel(label)
            .accessibilityHint(hint ?? "")
            .accessibilityAddTraits(.isButton)
    }
    
    /// Set accessibility label for sliders
    func accessibleSlider(label: String, value: String, hint: String? = nil) -> some View {
        self
            .accessibilityLabel(label)
            .accessibilityValue(value)
            .accessibilityHint(hint ?? "")
            .accessibilityAddTraits(.isAdjustable)
    }
    
    /// Set accessibility label for toggles
    func accessibleToggle(label: String, isOn: Bool, hint: String? = nil) -> some View {
        self
            .accessibilityLabel(label)
            .accessibilityValue(isOn ? AccessibilityHelper.toggleOn : AccessibilityHelper.toggleOff)
            .accessibilityHint(hint ?? "")
            .accessibilityAddTraits(.isToggle)
    }
}

// MARK: - Minimum Touch Target Size

/// Ensure buttons and interactive elements meet minimum touch target size (44pt)
struct MinimumTouchTargetModifier: ViewModifier {
    let minimumSize: CGFloat = 44
    
    func body(content: Content) -> some View {
        content
            .frame(minHeight: minimumSize)
            .frame(minWidth: minimumSize)
    }
}

extension View {
    /// Apply minimum touch target size for accessibility
    func accessibleTouchTarget() -> some View {
        modifier(MinimumTouchTargetModifier())
    }
}

// MARK: - High Contrast Support

/// Apply high contrast colors for improved visibility
struct HighContrastModifier: ViewModifier {
    @Environment(\.colorScheme) var colorScheme
    
    func body(content: Content) -> some View {
        content
            .brightness(colorScheme == .dark ? 0.1 : -0.1)
            .contrast(1.2)
    }
}

extension View {
    /// Apply high contrast styling for improved visibility
    func highContrast() -> some View {
        modifier(HighContrastModifier())
    }
}

// MARK: - Focus Ring (for keyboard navigation)

/// Add visible focus ring for keyboard navigation
struct AccessibleFocusRingModifier: ViewModifier {
    @FocusState private var isFocused: Bool
    
    func body(content: Content) -> some View {
        content
            .focused($isFocused)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(isFocused ? Color.blue : Color.clear, lineWidth: 2)
            )
    }
}

extension View {
    /// Apply keyboard focus ring for accessibility
    func accessibleFocusRing() -> some View {
        modifier(AccessibleFocusRingModifier())
    }
}
