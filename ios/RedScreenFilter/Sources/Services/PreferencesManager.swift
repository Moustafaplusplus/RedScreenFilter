//
//  PreferencesManager.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine

/// PreferencesManager - Singleton class managing app preferences with Combine integration
/// Uses UserDefaults for UI-only settings (not App Groups)
/// Publishes changes for reactive UI updates
class PreferencesManager: NSObject, ObservableObject {
    static let shared = PreferencesManager()
    
    private let defaults = UserDefaults.standard
    private let appGroupDefaults = UserDefaults(suiteName: Constants.AppGroup.identifier)
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - @Published Properties for Combine
    
    @Published var overlayEnabled: Bool = false
    @Published var opacity: Float = 0.5
    @Published var colorVariant: String = "red_standard"
    @Published var scheduleEnabled: Bool = false
    @Published var scheduleStartTime: String = "21:00"
    @Published var scheduleEndTime: String = "07:00"
    @Published var useLocationSchedule: Bool = false
    @Published var sunsetOffsetMinutes: Int = 0
    @Published var useAmbientLight: Bool = false
    @Published var ambientLightSensitivity: String = "medium"
    @Published var batteryOptimizationEnabled: Bool = true
    @Published var batteryOptimizationThreshold: Float = 0.2
    @Published var eyeStrainRemindersEnabled: Bool = true
    @Published var reminderInterval: Int = 20 // minutes
    @Published var notificationStyle: String = "sound"
    @Published var currentPreset: String = "Standard"
    
    // MARK: - UserDefaults Keys
    
    private let overlayEnabledKey = "overlayEnabled"
    private let opacityKey = "opacity"
    private let colorVariantKey = "colorVariant"
    private let scheduleEnabledKey = "scheduleEnabled"
    private let scheduleStartTimeKey = "scheduleStartTime"
    private let scheduleEndTimeKey = "scheduleEndTime"
    private let useLocationScheduleKey = "useLocationSchedule"
    private let sunsetOffsetMinutesKey = "sunsetOffsetMinutes"
    private let useAmbientLightKey = "useAmbientLight"
    private let ambientLightSensitivityKey = "ambientLightSensitivity"
    private let batteryOptimizationKey = "batteryOptimization"
    private let batteryThresholdKey = "batteryThreshold"
    private let eyeStrainRemindersKey = "eyeStrainReminders"
    private let reminderIntervalKey = "reminderInterval"
    private let notificationStyleKey = "notificationStyle"
    private let currentPresetKey = "currentPreset"
    private let settingsKey = "overlaySettings"
    
    override init() {
        super.init()
        _ = loadSettings()
        setupPublishers()
    }
    
    // MARK: - Overlay Methods
    
    func setOverlayEnabled(_ enabled: Bool) {
        overlayEnabled = enabled
        defaults.set(enabled, forKey: overlayEnabledKey)
        appGroupDefaults?.set(enabled, forKey: overlayEnabledKey)
    }
    
    func isOverlayEnabled() -> Bool {
        return overlayEnabled
    }
    
    func setOpacity(_ opacity: Float) {
        self.opacity = max(0, min(1, opacity))
        defaults.set(self.opacity, forKey: opacityKey)
        appGroupDefaults?.set(self.opacity, forKey: opacityKey)
    }
    
    func getOpacity() -> Float {
        return opacity
    }
    
    // MARK: - Color Variant Methods
    
    func setColorVariant(_ variant: String) {
        colorVariant = variant
        defaults.set(variant, forKey: colorVariantKey)
        appGroupDefaults?.set(variant, forKey: colorVariantKey)
    }
    
    func getColorVariant() -> String {
        return colorVariant
    }
    
    /// Set color variant using ColorVariant enum
    /// - Parameter variant: The color variant enum value
    func setColorVariantEnum(_ variant: ColorVariant) {
        setColorVariant(variant.rawValue)
    }
    
    /// Get color variant as ColorVariant enum
    /// - Returns: ColorVariant enum value, defaults to redStandard if invalid
    func getColorVariantEnum() -> ColorVariant {
        return ColorVariant(rawValue: colorVariant) ?? .redStandard
    }
    
    // MARK: - Schedule Methods
    
    func setScheduleEnabled(_ enabled: Bool) {
        scheduleEnabled = enabled
        defaults.set(enabled, forKey: scheduleEnabledKey)
        appGroupDefaults?.set(enabled, forKey: scheduleEnabledKey)
    }
    
    func setScheduleTime(start: String, end: String) {
        scheduleStartTime = start
        scheduleEndTime = end
        defaults.set(start, forKey: scheduleStartTimeKey)
        defaults.set(end, forKey: scheduleEndTimeKey)
        appGroupDefaults?.set(start, forKey: scheduleStartTimeKey)
        appGroupDefaults?.set(end, forKey: scheduleEndTimeKey)
    }

    // MARK: - Preset Methods

    func setCurrentPreset(_ preset: String) {
        currentPreset = preset
        defaults.set(preset, forKey: currentPresetKey)
        appGroupDefaults?.set(preset, forKey: currentPresetKey)
    }

    func getCurrentPreset() -> String {
        return currentPreset
    }
    
    func getScheduleTime() -> (start: String, end: String) {
        return (scheduleStartTime, scheduleEndTime)
    }
    
    // MARK: - Location Schedule Methods
    
    func setLocationScheduleEnabled(_ enabled: Bool) {
        useLocationSchedule = enabled
        defaults.set(enabled, forKey: useLocationScheduleKey)
    }
    
    func isLocationScheduleEnabled() -> Bool {
        return useLocationSchedule
    }
    
    func setSunsetOffset(_ offsetMinutes: Int) {
        sunsetOffsetMinutes = offsetMinutes
        defaults.set(offsetMinutes, forKey: sunsetOffsetMinutesKey)
    }
    
    func getSunsetOffset() -> Int {
        return sunsetOffsetMinutes
    }
    
    // MARK: - Ambient Light Methods
    
    func setAmbientLightEnabled(_ enabled: Bool) {
        useAmbientLight = enabled
        defaults.set(enabled, forKey: useAmbientLightKey)
    }
    
    func isAmbientLightEnabled() -> Bool {
        return useAmbientLight
    }
    
    func setAmbientLightSensitivity(_ sensitivity: String) {
        ambientLightSensitivity = sensitivity
        defaults.set(sensitivity, forKey: ambientLightSensitivityKey)
    }
    
    func getAmbientLightSensitivity() -> String {
        return ambientLightSensitivity
    }
    
    // MARK: - Battery Optimization Methods
    
    func setBatteryOptimizationEnabled(_ enabled: Bool) {
        batteryOptimizationEnabled = enabled
        defaults.set(enabled, forKey: batteryOptimizationKey)
    }
    
    func isBatteryOptimizationEnabled() -> Bool {
        return batteryOptimizationEnabled
    }
    
    func setBatteryOptimizationThreshold(_ threshold: Float) {
        batteryOptimizationThreshold = max(0.05, min(0.5, threshold))
        defaults.set(batteryOptimizationThreshold, forKey: batteryThresholdKey)
    }
    
    func getBatteryOptimizationThreshold() -> Float {
        return batteryOptimizationThreshold
    }
    
    // MARK: - Eye Strain Reminder Methods
    
    func setEyeStrainRemindersEnabled(_ enabled: Bool) {
        eyeStrainRemindersEnabled = enabled
        defaults.set(enabled, forKey: eyeStrainRemindersKey)
    }
    
    func areEyeStrainRemindersEnabled() -> Bool {
        return eyeStrainRemindersEnabled
    }
    
    func setReminderInterval(_ minutes: Int) {
        reminderInterval = max(15, min(120, minutes))
        defaults.set(reminderInterval, forKey: reminderIntervalKey)
    }
    
    func getReminderInterval() -> Int {
        return reminderInterval
    }
    
    func setNotificationStyle(_ style: String) {
        notificationStyle = style
        defaults.set(style, forKey: notificationStyleKey)
    }
    
    func getNotificationStyle() -> String {
        return notificationStyle
    }
    
    // MARK: - Settings Model Methods
    
    func saveSettings(_ settings: OverlaySettings) {
        if let encoded = try? JSONEncoder().encode(settings) {
            defaults.set(encoded, forKey: settingsKey)
        }
    }
    
    func loadSettings() -> OverlaySettings {
        // Legacy blob support (fallback only). Individual keys are the source of truth.
        let legacySettings: OverlaySettings? = {
            guard let data = defaults.data(forKey: settingsKey) else { return nil }
            return try? JSONDecoder().decode(OverlaySettings.self, from: data)
        }()

        overlayEnabled = (defaults.object(forKey: overlayEnabledKey) as? Bool) ?? legacySettings?.isEnabled ?? false
        opacity = defaults.object(forKey: opacityKey) != nil
            ? defaults.float(forKey: opacityKey)
            : (legacySettings?.opacity ?? 0.5)
        colorVariant = defaults.string(forKey: colorVariantKey) ?? legacySettings?.colorVariant ?? "red_standard"

        scheduleEnabled = (defaults.object(forKey: scheduleEnabledKey) as? Bool) ?? legacySettings?.scheduleEnabled ?? false
        scheduleStartTime = defaults.string(forKey: scheduleStartTimeKey) ?? legacySettings?.scheduleStartTime ?? "21:00"
        scheduleEndTime = defaults.string(forKey: scheduleEndTimeKey) ?? legacySettings?.scheduleEndTime ?? "07:00"

        useLocationSchedule = (defaults.object(forKey: useLocationScheduleKey) as? Bool) ?? legacySettings?.useLocationSchedule ?? false
        useAmbientLight = (defaults.object(forKey: useAmbientLightKey) as? Bool) ?? legacySettings?.useAmbientLight ?? false
        sunsetOffsetMinutes = defaults.object(forKey: sunsetOffsetMinutesKey) as? Int ?? legacySettings?.sunsetOffsetMinutes ?? 0
        ambientLightSensitivity = defaults.string(forKey: ambientLightSensitivityKey) ?? "medium"

        batteryOptimizationEnabled = defaults.object(forKey: batteryOptimizationKey) as? Bool ?? true
        let threshold = defaults.float(forKey: batteryThresholdKey)
        batteryOptimizationThreshold = threshold > 0 ? threshold : 0.2

        eyeStrainRemindersEnabled = defaults.object(forKey: eyeStrainRemindersKey) as? Bool ?? true
        reminderInterval = defaults.integer(forKey: reminderIntervalKey)
        if reminderInterval == 0 {
            reminderInterval = 20
        }
        notificationStyle = defaults.string(forKey: notificationStyleKey) ?? "sound"
        currentPreset = defaults.string(forKey: currentPresetKey) ?? "Standard"

        // Migration cleanup: do not let stale blob override future launches.
        if legacySettings != nil {
            defaults.removeObject(forKey: settingsKey)
        }

        syncToAppGroup()
        
        return OverlaySettings(
            isEnabled: overlayEnabled,
            opacity: opacity,
            scheduleEnabled: scheduleEnabled,
            scheduleStartTime: scheduleStartTime,
            scheduleEndTime: scheduleEndTime,
            useAmbientLight: useAmbientLight,
            useLocationSchedule: useLocationSchedule,
            sunsetOffsetMinutes: sunsetOffsetMinutes,
            colorVariant: colorVariant
        )
    }
    
    // MARK: - Combine Reactive Setup
    
    private func setupPublishers() {
        // Auto-save when published properties change
        $overlayEnabled
            .dropFirst()
            .sink { [weak self] value in
                self?.defaults.set(value, forKey: self?.overlayEnabledKey ?? "")
                self?.appGroupDefaults?.set(value, forKey: self?.overlayEnabledKey ?? "")
            }
            .store(in: &cancellables)
        
        $opacity
            .dropFirst()
            .sink { [weak self] value in
                self?.defaults.set(value, forKey: self?.opacityKey ?? "")
                self?.appGroupDefaults?.set(value, forKey: self?.opacityKey ?? "")
            }
            .store(in: &cancellables)
        
        $colorVariant
            .dropFirst()
            .sink { [weak self] value in
                self?.defaults.set(value, forKey: self?.colorVariantKey ?? "")
                self?.appGroupDefaults?.set(value, forKey: self?.colorVariantKey ?? "")
            }
            .store(in: &cancellables)
        
        $scheduleEnabled
            .dropFirst()
            .sink { [weak self] value in
                self?.defaults.set(value, forKey: self?.scheduleEnabledKey ?? "")
                self?.appGroupDefaults?.set(value, forKey: self?.scheduleEnabledKey ?? "")
            }
            .store(in: &cancellables)

        $scheduleStartTime
            .dropFirst()
            .sink { [weak self] value in
                self?.appGroupDefaults?.set(value, forKey: self?.scheduleStartTimeKey ?? "")
            }
            .store(in: &cancellables)

        $scheduleEndTime
            .dropFirst()
            .sink { [weak self] value in
                self?.appGroupDefaults?.set(value, forKey: self?.scheduleEndTimeKey ?? "")
            }
            .store(in: &cancellables)

        $currentPreset
            .dropFirst()
            .sink { [weak self] value in
                self?.defaults.set(value, forKey: self?.currentPresetKey ?? "")
                self?.appGroupDefaults?.set(value, forKey: self?.currentPresetKey ?? "")
            }
            .store(in: &cancellables)
    }

    private func syncToAppGroup() {
        appGroupDefaults?.set(overlayEnabled, forKey: overlayEnabledKey)
        appGroupDefaults?.set(opacity, forKey: opacityKey)
        appGroupDefaults?.set(colorVariant, forKey: colorVariantKey)
        appGroupDefaults?.set(scheduleEnabled, forKey: scheduleEnabledKey)
        appGroupDefaults?.set(scheduleStartTime, forKey: scheduleStartTimeKey)
        appGroupDefaults?.set(scheduleEndTime, forKey: scheduleEndTimeKey)
        appGroupDefaults?.set(currentPreset, forKey: currentPresetKey)
    }
}
