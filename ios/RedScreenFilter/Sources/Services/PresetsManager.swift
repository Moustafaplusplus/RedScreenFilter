//
//  PresetsManager.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine

/// PresetsManager - Manages preset profiles with CRUD operations
/// Uses UserDefaults (App Groups) for persistence
/// Publishes changes via Combine for reactive UI updates
class PresetsManager: ObservableObject {
    
    // MARK: - Singleton
    
    static let shared = PresetsManager()
    
    // MARK: - Published Properties
    
    @Published var presets: [PresetProfile] = []
    @Published var currentPresetId: UUID?
    
    // MARK: - Constants
    
    private let presetsKey = "com.redscreenfilter.presets"
    private let currentPresetIdKey = "com.redscreenfilter.currentPresetId"
    private let appGroupIdentifier = "group.com.redscreenfilter"
    private let firstLaunchKey = "com.redscreenfilter.presetsFirstLaunch"
    
    // MARK: - UserDefaults
    
    private var userDefaults: UserDefaults {
        return UserDefaults(suiteName: appGroupIdentifier) ?? UserDefaults.standard
    }
    
    // MARK: - Initialization
    
    private init() {
        loadPresets()
        loadCurrentPresetId()
        
        // Load default presets on first launch
        if !userDefaults.bool(forKey: firstLaunchKey) {
            loadDefaultPresets()
            userDefaults.set(true, forKey: firstLaunchKey)
        }
    }
    
    // MARK: - CRUD Operations
    
    /// Create a new preset
    /// - Parameter preset: The preset to create
    /// - Returns: True if successful
    @discardableResult
    func createPreset(_ preset: PresetProfile) -> Bool {
        // Check for duplicate names (excluding the preset itself if editing)
        if presets.contains(where: { $0.name == preset.name && $0.id != preset.id }) {
            return false
        }
        
        presets.append(preset)
        savePresets()
        return true
    }
    
    /// Read all presets
    /// - Returns: Array of all presets
    func getAllPresets() -> [PresetProfile] {
        return presets
    }
    
    /// Read a specific preset by ID
    /// - Parameter id: The preset ID
    /// - Returns: The preset if found
    func getPreset(byId id: UUID) -> PresetProfile? {
        return presets.first(where: { $0.id == id })
    }
    
    /// Read the current active preset
    /// - Returns: The current preset if one is selected
    func getCurrentPreset() -> PresetProfile? {
        guard let currentId = currentPresetId else { return nil }
        return getPreset(byId: currentId)
    }
    
    /// Update an existing preset
    /// - Parameter preset: The preset with updated values
    /// - Returns: True if successful
    @discardableResult
    func updatePreset(_ preset: PresetProfile) -> Bool {
        guard let index = presets.firstIndex(where: { $0.id == preset.id }) else {
            return false
        }
        
        // Check for duplicate names (excluding the preset itself)
        if presets.contains(where: { $0.name == preset.name && $0.id != preset.id }) {
            return false
        }
        
        presets[index] = preset
        savePresets()
        return true
    }
    
    /// Delete a preset by ID
    /// - Parameter id: The preset ID to delete
    /// - Returns: True if successful
    @discardableResult
    func deletePreset(byId id: UUID) -> Bool {
        guard let index = presets.firstIndex(where: { $0.id == id }) else {
            return false
        }
        
        let preset = presets[index]
        
        // Prevent deletion of default presets
        if preset.isDefault {
            return false
        }
        
        presets.remove(at: index)
        
        // Clear current preset if it was deleted
        if currentPresetId == id {
            currentPresetId = nil
            saveCurrentPresetId()
        }
        
        savePresets()
        return true
    }
    
    /// Apply a preset (set as current)
    /// - Parameter id: The preset ID to apply
    /// - Returns: The preset that was applied, or nil if not found
    @discardableResult
    func applyPreset(byId id: UUID) -> PresetProfile? {
        guard let preset = getPreset(byId: id) else {
            return nil
        }
        
        currentPresetId = id
        saveCurrentPresetId()
        return preset
    }
    
    /// Clear the current preset selection
    func clearCurrentPreset() {
        currentPresetId = nil
        saveCurrentPresetId()
    }
    
    // MARK: - Persistence
    
    /// Save presets to UserDefaults
    private func savePresets() {
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(presets)
            userDefaults.set(data, forKey: presetsKey)
        } catch {
            print("❌ PresetsManager: Failed to save presets: \(error)")
        }
    }
    
    /// Load presets from UserDefaults
    private func loadPresets() {
        guard let data = userDefaults.data(forKey: presetsKey) else {
            presets = []
            return
        }
        
        do {
            let decoder = JSONDecoder()
            presets = try decoder.decode([PresetProfile].self, from: data)
        } catch {
            print("❌ PresetsManager: Failed to load presets: \(error)")
            presets = []
        }
    }
    
    /// Save current preset ID to UserDefaults
    private func saveCurrentPresetId() {
        if let id = currentPresetId {
            userDefaults.set(id.uuidString, forKey: currentPresetIdKey)
        } else {
            userDefaults.removeObject(forKey: currentPresetIdKey)
        }
    }
    
    /// Load current preset ID from UserDefaults
    private func loadCurrentPresetId() {
        guard let idString = userDefaults.string(forKey: currentPresetIdKey),
              let id = UUID(uuidString: idString) else {
            currentPresetId = nil
            return
        }
        
        currentPresetId = id
    }
    
    /// Load default presets on first app launch
    private func loadDefaultPresets() {
        let defaults = PresetProfile.defaultPresets
        
        for preset in defaults {
            createPreset(preset)
        }
    }
    
    // MARK: - Utility Methods
    
    /// Reset all presets to defaults (useful for debugging or user request)
    func resetToDefaults() {
        // Remove all non-default presets
        presets.removeAll(where: { !$0.isDefault })
        
        // Restore default presets if missing
        let defaults = PresetProfile.defaultPresets
        for defaultPreset in defaults {
            if !presets.contains(where: { $0.name == defaultPreset.name }) {
                createPreset(defaultPreset)
            }
        }
        
        savePresets()
    }
    
    /// Get presets filtered by type
    func getDefaultPresets() -> [PresetProfile] {
        return presets.filter { $0.isDefault }
    }
    
    func getCustomPresets() -> [PresetProfile] {
        return presets.filter { !$0.isDefault }
    }
}
