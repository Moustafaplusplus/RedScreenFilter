//
//  ColorVariant.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

struct ColorComponents: Codable, Hashable {
    let red: Float
    let green: Float
    let blue: Float
}

enum ColorVariant: String, CaseIterable, Codable {
    case redStandard = "red_standard"
    case redOrange = "red_orange"
    case redPink = "red_pink"
    case highContrast = "high_contrast"

    var displayName: String {
        switch self {
        case .redStandard:
            return "Standard Red"
        case .redOrange:
            return "Red Orange"
        case .redPink:
            return "Red Pink"
        case .highContrast:
            return "High Contrast"
        }
    }

    var iconName: String {
        switch self {
        case .redStandard:
            return "circle.fill"
        case .redOrange:
            return "sun.max.fill"
        case .redPink:
            return "heart.fill"
        case .highContrast:
            return "exclamationmark.triangle.fill"
        }
    }

    var colorComponents: ColorComponents {
        switch self {
        case .redStandard:
            return ColorComponents(red: 1.0, green: 0.0, blue: 0.0)
        case .redOrange:
            return ColorComponents(red: 1.0, green: 0.39, blue: 0.0)
        case .redPink:
            return ColorComponents(red: 1.0, green: 0.0, blue: 0.39)
        case .highContrast:
            return ColorComponents(red: 1.0, green: 0.0, blue: 0.0)
        }
    }

    var minimumOpacity: Float {
        switch self {
        case .highContrast:
            return 0.5
        default:
            return 0.0
        }
    }
}
