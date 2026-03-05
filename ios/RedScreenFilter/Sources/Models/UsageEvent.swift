//
//  UsageEvent.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

struct UsageEvent: Identifiable, Codable {
    let id: UUID = UUID()
    let timestamp: Date
    let overlayEnabled: Bool
    let opacity: Float
    let preset: String?
    
    enum CodingKeys: String, CodingKey {
        case timestamp
        case overlayEnabled
        case opacity
        case preset
    }
}
