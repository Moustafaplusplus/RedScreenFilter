//
//  PreferencesManager.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine

class PreferencesManager: NSObject, ObservableObject {
    static let shared = PreferencesManager()
    
    private let defaults = UserDefaults.standard
    
    @Published var overlayEnabled: Bool = false
    @Published var opacity: Float = 0.5
    @Published var colorVariant: String = "red_standard"
    
    private let overlayEnabledKey = "overlayEnabled"
    private let opacityKey = "opacity"
    private let colorVariantKey = "colorVariant"
    private let settingsKey = "overlaySettings"
    
    override init() {
        super.init()
        loadSettings()
    }
    
    // MARK: - Public Methods
    
    func setOverlayEnabled(_ enabled: Bool) {
        overlayEnabled = enabled
        defaults.set(enabled, forKey: overlayEnabledKey)
    }
    
    func isOverlayEnabled() -> Bool {
        return overlayEnabled
    }
    
    func setOpacity(_ opacity: Float) {
        self.opacity = opacity
        defaults.set(opacity, forKey: opacityKey)
    }
    
    func getOpacity() -> Float {
        return opacity
    }
    
    func setColorVariant(_ variant: String) {
        colorVariant = variant
        defaults.set(variant, forKey: colorVariantKey)
    }
    
    func getColorVariant() -> String {
        return colorVariant
    }
    
    func saveSettings(_ settings: OverlaySettings) {
        if let encoded = try? JSONEncoder().encode(settings) {
            defaults.set(encoded, forKey: settingsKey)
        }
    }
    
    func loadSettings() -> OverlaySettings {
        if let data = defaults.data(forKey: settingsKey),
           let decoded = try? JSONDecoder().decode(OverlaySettings.self, from: data) {
            return decoded
        }
        
        // Load individual values for backward compatibility
        overlayEnabled = defaults.bool(forKey: overlayEnabledKey)
        opacity = defaults.float(forKey: opacityKey)
        if opacity == 0 {
            opacity = 0.5
        }
        colorVariant = defaults.string(forKey: colorVariantKey) ?? "red_standard"
        
        return OverlaySettings()
    }
}
