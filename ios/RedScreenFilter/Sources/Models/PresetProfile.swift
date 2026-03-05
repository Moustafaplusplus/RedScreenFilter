//
//  PresetProfile.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

struct PresetProfile: Identifiable, Codable, Hashable {
    let id: String
    let name: String
    let opacity: Float
    let colorVariant: ColorVariant

    var opacityPercentage: String {
        "\(Int(opacity * 100))%"
    }
}
