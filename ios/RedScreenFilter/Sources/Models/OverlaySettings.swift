//
//  OverlaySettings.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

struct OverlaySettings: Codable {
    var isEnabled: Bool = false
    var opacity: Float = 0.5
    var scheduleEnabled: Bool = false
    var scheduleStartTime: String = "21:00"
    var scheduleEndTime: String = "07:00"
    var useAmbientLight: Bool = false
    var useLocationSchedule: Bool = false
    var sunsetOffsetMinutes: Int = 0
    var colorVariant: String = "red_standard"
    
    enum CodingKeys: String, CodingKey {
        case isEnabled
        case opacity
        case scheduleEnabled
        case scheduleStartTime
        case scheduleEndTime
        case useAmbientLight
        case useLocationSchedule
        case sunsetOffsetMinutes
        case colorVariant
    }
}
