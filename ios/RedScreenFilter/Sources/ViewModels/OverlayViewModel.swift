//
//  OverlayViewModel.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine

class OverlayViewModel: NSObject, ObservableObject {
    @Published var isEnabled: Bool = false
    @Published var opacity: Float = 0.5
    @Published var settings: OverlaySettings = OverlaySettings()
    @Published var currentPreset: String = "Standard"
    
    private let prefsManager = PreferencesManager.shared
    private let schedulingService = SchedulingService.shared
    private let overlayManager = OverlayWindowManager.shared
    
    override init() {
        super.init()
        loadSettings()
    }
    
    // MARK: - Public Methods
    
    func toggleOverlay() {
        isEnabled.toggle()
        prefsManager.setOverlayEnabled(isEnabled)
        
        if isEnabled {
            overlayManager.showOverlay(opacity: opacity)
        } else {
            overlayManager.hideOverlay()
        }
    }
    
    func updateOpacity(_ newOpacity: Float) {
        opacity = max(0, min(1, newOpacity))  // Clamp between 0-1
        overlayManager.updateOpacity(opacity)
    }
    
    func applyPreset(_ presetName: String) {
        currentPreset = presetName
        // Preset logic will be implemented in Phase 35-40%
    }
    
    func updateColorVariant(_ variant: String) {
        prefsManager.setColorVariant(variant)
        if isEnabled {
            overlayManager.updateOpacity(opacity)
        }
    }
    
    // MARK: - Private Methods
    
    private func loadSettings() {
        settings = prefsManager.loadSettings()
        isEnabled = prefsManager.isOverlayEnabled()
        opacity = prefsManager.getOpacity()
    }
}
