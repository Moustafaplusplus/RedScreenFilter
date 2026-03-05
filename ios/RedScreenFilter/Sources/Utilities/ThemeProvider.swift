//
//  ThemeProvider.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

/// Global theme provider to access design tokens consistently
/// Usage: RsfTheme.colors.primary, RsfTheme.spacing.md, etc.
struct RsfTheme {
    // Color tokens
    static let colors = RsfSemanticColors.self
    
    // Spacing tokens
    static let spacing = RsfSpacing.self
    
    // Radius tokens
    static let radius = RsfRadius.self
    
    // Elevation tokens
    static let elevation = RsfElevation.self
    
    // Border tokens
    static let border = RsfBorder.self
}

/// View modifier to apply standard app background
struct RsfBackgroundModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(RsfTheme.colors.background)
            .foregroundColor(RsfTheme.colors.onBackground)
    }
}

extension View {
    func rsfBackground() -> some View {
        modifier(RsfBackgroundModifier())
    }
}
