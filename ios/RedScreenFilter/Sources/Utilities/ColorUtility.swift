//
//  ColorUtility.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import UIKit
import SwiftUI

/// ColorUtility - Utility functions for color variant management and conversions
struct ColorUtility {
    
    // MARK: - UIColor Conversion
    
    /// Convert ColorVariant to UIColor without alpha
    /// - Parameter variant: The color variant to convert
    /// - Returns: UIColor with full opacity (1.0)
    static func uiColor(for variant: ColorVariant) -> UIColor {
        return uiColor(for: variant, opacity: 1.0)
    }
    
    /// Convert ColorVariant to UIColor with specified opacity
    /// - Parameters:
    ///   - variant: The color variant to convert
    ///   - opacity: Alpha value (0.0-1.0)
    /// - Returns: UIColor with specified opacity
    static func uiColor(for variant: ColorVariant, opacity: Float) -> UIColor {
        let components = variant.colorComponents
        return UIColor(
            red: CGFloat(components.red),
            green: CGFloat(components.green),
            blue: CGFloat(components.blue),
            alpha: CGFloat(max(0.0, min(1.0, opacity)))
        )
    }
    
    // MARK: - SwiftUI Color Conversion
    
    /// Convert ColorVariant to SwiftUI Color without alpha
    /// - Parameter variant: The color variant to convert
    /// - Returns: SwiftUI Color with full opacity
    static func color(for variant: ColorVariant) -> Color {
        return color(for: variant, opacity: 1.0)
    }
    
    /// Convert ColorVariant to SwiftUI Color with specified opacity
    /// - Parameters:
    ///   - variant: The color variant to convert
    ///   - opacity: Alpha value (0.0-1.0)
    /// - Returns: SwiftUI Color with specified opacity
    static func color(for variant: ColorVariant, opacity: Float) -> Color {
        let uiColor = uiColor(for: variant, opacity: opacity)
        return Color(uiColor)
    }
    
    // MARK: - High Contrast Mode
    
    /// Get minimum opacity for high contrast mode
    /// High contrast variant requires higher minimum opacity for better visibility
    /// - Parameter variant: The color variant
    /// - Returns: Minimum allowed opacity (0.0-1.0) for this variant
    static func minimumOpacity(for variant: ColorVariant) -> Float {
        switch variant {
        case .highContrast:
            return 0.5 // High contrast requires at least 50% opacity
        default:
            return 0.1 // Other variants can go as low as 10%
        }
    }
    
    /// Validate and clamp opacity to variant's minimum requirement
    /// - Parameters:
    ///   - opacity: The requested opacity
    ///   - variant: The color variant
    /// - Returns: Clamped opacity value
    static func validatedOpacity(_ opacity: Float, for variant: ColorVariant) -> Float {
        let minimum = minimumOpacity(for: variant)
        return max(minimum, min(1.0, opacity))
    }
    
    // MARK: - Description Methods
    
    /// Get accessibility description for a color variant
    /// - Parameter variant: The color variant
    /// - Returns: Description suitable for VoiceOver
    static func accessibilityDescription(for variant: ColorVariant) -> String {
        switch variant {
        case .redStandard:
            return "Red Standard - Pure red, best for most users"
        case .redOrange:
            return "Red Orange - Warmer tone, comfortable for extended viewing"
        case .redPink:
            return "Red Pink - Softer hue, easy on the eyes"
        case .highContrast:
            return "High Contrast - Maximum blue light blocking for intense use"
        }
    }
}

// MARK: - SwiftUI Color Extension

extension Color {
    /// Create a SwiftUI Color from a ColorVariant
    /// - Parameter variant: The color variant to convert
    /// - Returns: SwiftUI Color matching the variant
    static func fromVariant(_ variant: ColorVariant) -> Color {
        return ColorUtility.color(for: variant)
    }
    
    /// Create a SwiftUI Color from a ColorVariant with opacity
    /// - Parameters:
    ///   - variant: The color variant to convert
    ///   - opacity: Alpha value (0.0-1.0)
    /// - Returns: SwiftUI Color with opacity applied
    static func fromVariant(_ variant: ColorVariant, opacity: Float) -> Color {
        return ColorUtility.color(for: variant, opacity: opacity)
    }
}

// MARK: - UIColor Extension

extension UIColor {
    /// Create a UIColor from a ColorVariant
    /// - Parameter variant: The color variant to convert
    /// - Returns: UIColor matching the variant
    static func fromVariant(_ variant: ColorVariant) -> UIColor {
        return ColorUtility.uiColor(for: variant)
    }
    
    /// Create a UIColor from a ColorVariant with opacity
    /// - Parameters:
    ///   - variant: The color variant to convert
    ///   - opacity: Alpha value (0.0-1.0)
    /// - Returns: UIColor with opacity applied
    static func fromVariant(_ variant: ColorVariant, opacity: Float) -> UIColor {
        return ColorUtility.uiColor(for: variant, opacity: opacity)
    }
}
