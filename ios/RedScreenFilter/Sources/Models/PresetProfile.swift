//
//  PresetProfile.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

/// ColorVariant - Represents different color filter options
/// Designed for accessibility and various use cases
enum ColorVariant: String, Codable, CaseIterable {
    case redStandard = "red_standard"
    case redOrange = "red_orange"
    case redPink = "red_pink"
    case highContrast = "high_contrast"
    
    /// Display name for UI
    var displayName: String {
        switch self {
        case .redStandard:
            return "Red Standard"
        case .redOrange:
            return "Red Orange"
        case .redPink:
            return "Red Pink"
        case .highContrast:
            return "High Contrast"
        }
    }
    
    /// RGB color components (0-1 range)
    var colorComponents: (red: Float, green: Float, blue: Float) {
        switch self {
        case .redStandard:
            return (1.0, 0.0, 0.0)
        case .redOrange:
            return (1.0, 0.39, 0.0)
        case .redPink:
            return (1.0, 0.0, 0.39)
        case .highContrast:
            return (1.0, 0.0, 0.0) // Same as standard but used with higher minimum opacity
        }
    }
    
    /// Icon name for UI representation
    var iconName: String {
        switch self {
        case .redStandard:
            return "circle.fill"
        case .redOrange:
            return "sun.max.fill"
        case .redPink:
            return "heart.fill"
        case .highContrast:
            return "circle.hexagongrid.fill"
        }
    }
}

/// PresetProfile - Represents a customizable filter preset
/// Users can create, edit, and apply presets with specific opacity and color settings
struct PresetProfile: Identifiable, Codable, Equatable {
    let id: UUID
    var name: String
    var opacity: Float
    var colorVariant: ColorVariant
    var description: String
    var isDefault: Bool // True for built-in presets (cannot be deleted)
    
    init(
        id: UUID = UUID(),
        name: String,
        opacity: Float,
        colorVariant: ColorVariant,
        description: String,
        isDefault: Bool = false
    ) {
        self.id = id
        self.name = name
        self.opacity = opacity
        self.colorVariant = colorVariant
        self.description = description
        self.isDefault = isDefault
    }
    
    /// Clamp opacity to valid range (0.0-1.0)
    var validatedOpacity: Float {
        return max(0.0, min(1.0, opacity))
    }
    
    /// Convert opacity to percentage string for display
    var opacityPercentage: String {
        return "\(Int(validatedOpacity * 100))%"
    }
    
    // MARK: - Codable conformance
    
    enum CodingKeys: String, CodingKey {
        case id
        case name
        case opacity
        case colorVariant
        case description
        case isDefault
    }
    
    // MARK: - Equatable conformance
    
    static func == (lhs: PresetProfile, rhs: PresetProfile) -> Bool {
        return lhs.id == rhs.id
    }
}

// MARK: - Default Presets

extension PresetProfile {
    /// Factory method for default presets
    static var defaultPresets: [PresetProfile] {
        return [
            PresetProfile(
                name: "Work",
                opacity: 0.3,
                colorVariant: .redStandard,
                description: "Light filter for productivity and reading",
                isDefault: true
            ),
            PresetProfile(
                name: "Gaming",
                opacity: 0.4,
                colorVariant: .redOrange,
                description: "Balanced filter for gaming sessions",
                isDefault: true
            ),
            PresetProfile(
                name: "Movie",
                opacity: 0.5,
                colorVariant: .redPink,
                description: "Comfortable viewing for videos",
                isDefault: true
            ),
            PresetProfile(
                name: "Sleep",
                opacity: 0.7,
                colorVariant: .redStandard,
                description: "Maximum blue light reduction before bed",
                isDefault: true
            )
        ]
    }
}
