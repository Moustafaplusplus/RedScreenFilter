//
//  OverlayViewModel.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine

/// OverlayViewModel - MVVM ViewModel managing overlay state
/// Uses @Published properties for reactive updates
/// Observes PreferencesManager changes and synchronizes state
class OverlayViewModel: NSObject, ObservableObject {
    // MARK: - @Published Properties
    
    @Published var isEnabled: Bool = false
    @Published var opacity: Float = 0.5
    @Published var settings: OverlaySettings = OverlaySettings()
    @Published var currentPreset: String = "Standard"
    
    @Published var scheduleEnabled: Bool = false
    @Published var scheduleStartTime: String = "21:00"
    @Published var scheduleEndTime: String = "07:00"
    @Published var useLocationSchedule: Bool = false
    @Published var sunsetTime: Date?
    @Published var sunriseTime: Date?
    @Published var sunsetOffsetMinutes: Int = 0
    @Published var useAmbientLight: Bool = false
    @Published var batteryOptimizationEnabled: Bool = true
    @Published var eyeStrainRemindersEnabled: Bool = true
    @Published var reminderInterval: Int = 20
    
    // MARK: - Dependencies
    
    private let prefsManager: PreferencesManager
    private let schedulingService: SchedulingService
    private let locationService: LocationCalculationService
    private let overlayManager: OverlayWindowManager
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - Initialization
    
    override init() {
        self.prefsManager = PreferencesManager.shared
        self.schedulingService = SchedulingService.shared
        self.locationService = LocationCalculationService.shared
        self.overlayManager = OverlayWindowManager.shared
        
        super.init()
        
        loadSettings()
        setupBindings()
        setupLocationBindings()
    }
    
    // MARK: - Overlay Control Methods
    
    /// Toggle overlay on/off
    func toggleOverlay() {
        isEnabled.toggle()
        prefsManager.setOverlayEnabled(isEnabled)
        updateOverlayManager()
    }
    
    /// Update overlay opacity
    func updateOpacity(_ newOpacity: Float) {
        opacity = max(0, min(1, newOpacity))
        prefsManager.setOpacity(opacity)
        if isEnabled {
            overlayManager.updateOpacity(opacity)
        }
    }
    
    /// Update color variant
    func updateColorVariant(_ variant: String) {
        settings.colorVariant = variant
        prefsManager.setColorVariant(variant)
        if isEnabled {
            overlayManager.updateOpacity(opacity)
        }
    }
    
    // MARK: - Schedule Methods
    
    /// Enable/disable overlay schedule
    func setScheduleEnabled(_ enabled: Bool) {
        scheduleEnabled = enabled
        prefsManager.setScheduleEnabled(enabled)
    }
    
    /// Update schedule times
    func setScheduleTime(start: String, end: String) {
        scheduleStartTime = start
        scheduleEndTime = end
        prefsManager.setScheduleTime(start: start, end: end)
    }
    
    /// Enable/disable location-based scheduling
    func setLocationScheduleEnabled(_ enabled: Bool) {
        useLocationSchedule = enabled
        prefsManager.setLocationScheduleEnabled(enabled)
        
        if enabled {
            // Request location permission
            locationService.requestLocationPermission()
            // Fetch initial sunrise/sunset times
            locationService.fetchSunriseSunsetTimes()
            // Enable in scheduling service
            schedulingService.enableLocationSchedule(offsetMinutes: sunsetOffsetMinutes)
        } else {
            schedulingService.disableLocationSchedule()
        }
    }
    
    /// Set sunset offset in minutes
    func setSunsetOffset(_ offsetMinutes: Int) {
        sunsetOffsetMinutes = offsetMinutes
        prefsManager.setSunsetOffset(offsetMinutes)
        
        // Update scheduling if location schedule is enabled
        if useLocationSchedule {
            schedulingService.enableLocationSchedule(offsetMinutes: offsetMinutes)
        }
    }
    
    /// Manually refresh location and sunrise/sunset times
    func refreshLocationTimes() {
        schedulingService.refreshLocationTimes()
    }
    
    // MARK: - Ambient Light Methods
    
    /// Enable/disable ambient light sensing
    func setAmbientLightEnabled(_ enabled: Bool) {
        useAmbientLight = enabled
        prefsManager.setAmbientLightEnabled(enabled)
    }
    
    // MARK: - Battery Optimization Methods
    
    /// Enable/disable battery optimization
    func setBatteryOptimizationEnabled(_ enabled: Bool) {
        batteryOptimizationEnabled = enabled
        prefsManager.setBatteryOptimizationEnabled(enabled)
    }
    
    /// Set battery optimization threshold (5% - 50%)
    func setBatteryOptimizationThreshold(_ threshold: Float) {
        prefsManager.setBatteryOptimizationThreshold(threshold)
    }

    var batteryOptimizationThreshold: Float {
        prefsManager.getBatteryOptimizationThreshold()
    }
    
    // MARK: - Eye Strain Reminder Methods
    
    /// Enable/disable eye strain reminders
    func setEyeStrainRemindersEnabled(_ enabled: Bool) {
        eyeStrainRemindersEnabled = enabled
        prefsManager.setEyeStrainRemindersEnabled(enabled)
    }
    
    /// Set reminder interval in minutes (5 - 120 minutes)
    func setReminderInterval(_ minutes: Int) {
        reminderInterval = max(5, min(120, minutes))
        prefsManager.setReminderInterval(reminderInterval)
    }
    
    // MARK: - Preset Methods
    
    /// Apply preset using PresetProfile model
    func applyPreset(_ preset: PresetProfile) {
        currentPreset = preset.name
        updateOpacity(preset.opacity)
        updateColorVariant(preset.colorVariant.rawValue)
        
        // Enable overlay if it's not already enabled
        if !isEnabled {
            isEnabled = true
            prefsManager.setOverlayEnabled(true)
            updateOverlayManager()
        }
    }
    
    /// Apply preset by name (legacy method for backward compatibility)
    func applyPresetByName(_ presetName: String) {
        currentPreset = presetName
        
        switch presetName.lowercased() {
        case "work":
            updateOpacity(0.3)
            settings.colorVariant = Constants.Colors.redStandard
        case "gaming":
            updateOpacity(0.4)
            settings.colorVariant = Constants.Colors.redOrange
        case "movie":
            updateOpacity(0.5)
            settings.colorVariant = Constants.Colors.redStandard
        case "sleep":
            updateOpacity(0.7)
            settings.colorVariant = Constants.Colors.redPink
        default:
            break
        }

        updateColorVariant(settings.colorVariant)
    }
    
    // MARK: - Private Methods
    
    private func loadSettings() {
        let prefs = prefsManager
        
        isEnabled = prefs.isOverlayEnabled()
        opacity = prefs.getOpacity()
        settings = OverlaySettings()
        settings.colorVariant = prefs.getColorVariant()
        
        scheduleEnabled = prefs.scheduleEnabled
        let (startTime, endTime) = prefs.getScheduleTime()
        scheduleStartTime = startTime
        scheduleEndTime = endTime
        
        useLocationSchedule = prefs.isLocationScheduleEnabled()
        sunsetOffsetMinutes = prefs.getSunsetOffset()
        useAmbientLight = prefs.isAmbientLightEnabled()
        batteryOptimizationEnabled = prefs.isBatteryOptimizationEnabled()
        eyeStrainRemindersEnabled = prefs.areEyeStrainRemindersEnabled()
        reminderInterval = prefs.getReminderInterval()
    }
    
    /// Setup Combine bindings for reactive updates
    private func setupBindings() {
        // Bind to PreferencesManager changes
        prefsManager.$overlayEnabled
            .sink { [weak self] value in
                if self?.isEnabled != value {
                    self?.isEnabled = value
                }
            }
            .store(in: &cancellables)
        
        prefsManager.$opacity
            .sink { [weak self] value in
                if self?.opacity != value {
                    self?.opacity = value
                }
            }
            .store(in: &cancellables)
        
        prefsManager.$colorVariant
            .sink { [weak self] value in
                if self?.settings.colorVariant != value {
                    self?.settings.colorVariant = value
                }
            }
            .store(in: &cancellables)
        
        prefsManager.$scheduleEnabled
            .sink { [weak self] value in
                self?.scheduleEnabled = value
            }
            .store(in: &cancellables)
    }
    
    /// Setup bindings for location service updates
    private func setupLocationBindings() {
        // Bind to location service sunrise/sunset times
        locationService.$sunriseTime
            .sink { [weak self] value in
                self?.sunriseTime = value
            }
            .store(in: &cancellables)
        
        locationService.$sunsetTime
            .sink { [weak self] value in
                self?.sunsetTime = value
            }
            .store(in: &cancellables)
    }
    
    private func updateOverlayManager() {
        if isEnabled {
            overlayManager.showOverlay(opacity: opacity)
        } else {
            overlayManager.hideOverlay()
        }
    }
}
