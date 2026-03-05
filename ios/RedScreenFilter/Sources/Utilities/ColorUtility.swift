//
//  ColorUtility.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import UIKit

enum ColorUtility {
    static func validatedOpacity(_ opacity: Float, for variant: ColorVariant) -> Float {
        let clamped = max(0.0, min(1.0, opacity))
        return max(clamped, variant.minimumOpacity)
    }

    static func uiColor(for variant: ColorVariant, opacity: Float) -> UIColor {
        let components = variant.colorComponents
        let adjustedOpacity = validatedOpacity(opacity, for: variant)

        return UIColor(
            red: CGFloat(components.red),
            green: CGFloat(components.green),
            blue: CGFloat(components.blue),
            alpha: CGFloat(adjustedOpacity)
        )
    }

    static func accessibilityDescription(for variant: ColorVariant) -> String {
        switch variant {
        case .redStandard:
            return "Standard red filter"
        case .redOrange:
            return "Warm orange-red filter"
        case .redPink:
            return "Soft pink-red filter"
        case .highContrast:
            return "High-contrast filter with minimum fifty percent opacity"
        }
    }
}
