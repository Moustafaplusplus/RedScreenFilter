//
//  Extensions.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import SwiftUI

// MARK: - String Extensions

extension String {
    func toTime() -> (hour: Int, minute: Int)? {
        let components = split(separator: ":").compactMap { Int($0) }
        guard components.count == 2 else { return nil }
        return (components[0], components[1])
    }
}

// MARK: - Date Extensions

extension Date {
    var timeString: String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: self)
    }
    
    var timeOnlyString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: self)
    }
}

// MARK: - Float Extensions

extension Float {
    var percentageString: String {
        return String(format: "%.0f%%", self * 100)
    }
}

// MARK: - Color Extensions

extension Color {
    // Overlay Color Variants
    static let redStandard = Color(red: 1.0, green: 0, blue: 0)
    static let redOrange = Color(red: 1.0, green: 0.39, blue: 0)
    static let redPink = Color(red: 1.0, green: 0, blue: 0.39)
    static let highContrast = Color(red: 1.0, green: 0, blue: 0)
    
    static func fromVariant(_ variant: String) -> Color {
        switch variant {
        case Constants.Colors.redOrange:
            return .redOrange
        case Constants.Colors.redPink:
            return .redPink
        case Constants.Colors.highContrast:
            return .highContrast
        default:
            return .redStandard
        }
    }
    
    // MARK: - RSF Theme Color Access (convenience aliases)
    
    static var rsfPrimary: Color {
        RsfTheme.colors.primary
    }
    
    static var rsfSecondary: Color {
        RsfTheme.colors.secondary
    }
    
    static var rsfTertiary: Color {
        RsfTheme.colors.tertiary
    }
    
    static var rsfBackground: Color {
        RsfTheme.colors.background
    }
    
    static var rsfSurface: Color {
        RsfTheme.colors.surface
    }
    
    static var rsfSurfaceVariant: Color {
        RsfTheme.colors.surfaceVariant
    }
    
    static var rsfError: Color {
        RsfTheme.colors.error
    }
}
