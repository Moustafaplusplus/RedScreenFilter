//
//  ThemeColors.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

/// RSF Color Palette - Mirrored from Android Design System
struct RsfColors {
    // Core Colors - Dark Theme Foundation
    static let carbonBlack = Color(red: 5/255, green: 6/255, blue: 8/255)        // #050608
    static let charcoalSurface = Color(red: 20/255, green: 22/255, blue: 30/255) // #14161E
    static let cardOverlay = Color(red: 30/255, green: 33/255, blue: 46/255)     // #1E212E
    
    // Brand Colors
    static let scarletCore = Color(red: 1.0, green: 27/255, blue: 60/255)        // #FF1B3C (Primary)
    static let emberOrange = Color(red: 1.0, green: 77/255, blue: 46/255)        // #FF4D2E (Secondary)
    static let pulseMagenta = Color(red: 1.0, green: 53/255, blue: 109/255)      // #FF356D (Tertiary)
    
    // Text Colors
    static let textPrimaryOnDark = Color(red: 246/255, green: 246/255, blue: 249/255)    // #F6F6F9
    static let textSecondaryOnDark = Color(red: 165/255, green: 167/255, blue: 181/255) // #A5A7B5
    
    // Surface & Border
    static let surfaceTint = Color(white: 1.0, opacity: 0.15)      // Semi-transparent white
    static let glassStroke = Color(white: 1.0, opacity: 0.15)      // Border color
    
    // Error Colors
    static let error = Color(red: 1.0, green: 91/255, blue: 110/255)             // #FF5B6E
    static let onError = Color(red: 42/255, green: 0, blue: 6/255)               // #2A0006
    static let errorContainer = Color(red: 93/255, green: 16/255, blue: 32/255)  // #5D1020
    static let onErrorContainer = Color(red: 1.0, green: 217/255, blue: 223/255) // #FFFFD9DF
}

/// RSF Semantic Colors - Semantic meaning for UI elements
struct RsfSemanticColors {
    static let primary = RsfColors.scarletCore
    static let onPrimary = RsfColors.textPrimaryOnDark
    
    static let secondary = RsfColors.emberOrange
    static let onSecondary = RsfColors.textPrimaryOnDark
    
    static let tertiary = RsfColors.pulseMagenta
    static let onTertiary = RsfColors.textPrimaryOnDark
    
    static let background = RsfColors.carbonBlack
    static let onBackground = RsfColors.textPrimaryOnDark
    
    static let surface = RsfColors.charcoalSurface
    static let onSurface = RsfColors.textPrimaryOnDark
    
    static let surfaceVariant = RsfColors.cardOverlay
    static let onSurfaceVariant = RsfColors.textSecondaryOnDark
    
    static let error = RsfColors.error
    static let onError = RsfColors.onError
    static let errorContainer = RsfColors.errorContainer
    static let onErrorContainer = RsfColors.onErrorContainer
}

/// RSF Spacing Tokens - Consistent spacing throughout the app
struct RsfSpacing {
    static let xs: CGFloat = 4    // Extra small
    static let sm: CGFloat = 8    // Small
    static let md: CGFloat = 16   // Medium
    static let lg: CGFloat = 24   // Large
    static let xl: CGFloat = 32   // Extra large
}

/// RSF Radius Tokens - Consistent corner radius
struct RsfRadius {
    static let sm: CGFloat = 8    // Small
    static let md: CGFloat = 12   // Medium
    static let lg: CGFloat = 16   // Large
    static let xl: CGFloat = 24   // Extra large
}

/// RSF Elevation/Shadow Tokens
struct RsfElevation {
    static let none: CGFloat = 0
    static let sm: CGFloat = 2
    static let md: CGFloat = 6
    static let lg: CGFloat = 12
    static let xl: CGFloat = 16
}

/// RSF Border Tokens
struct RsfBorder {
    static let thin: CGFloat = 1
    static let medium: CGFloat = 2
    static let thick: CGFloat = 3
}
