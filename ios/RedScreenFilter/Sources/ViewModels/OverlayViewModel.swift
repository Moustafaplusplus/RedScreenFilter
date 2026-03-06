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
    @Published var ambientLightSensitivity: String = "medium"
    @Published var lightingCondition: String = "Normal"
    @Published var currentLux: Float = 500.0
    @Published var isLightSensorActive: Bool = false
    @Published var batteryOptimizationEnabled: Bool = true
    @Published var batteryLevel: Float = 1.0
    @Published var isBatteryLow: Bool = false
    @Published var isBatteryCritical: Bool = false
    @Published var batteryStatusIcon: String = "🔋"
    @Published var eyeStrainRemindersEnabled: Bool = true
    @Published var reminderInterval: Int = 20
    @Published var notificationStyle: String = "sound"
    
    // MARK: - Dependencies
    
    private let prefsManager: PreferencesManager
    private let schedulingService: SchedulingService
    private let locationService: LocationCalculationService
    private let overlayManager: OverlayWindowManager
    private let batteryMonitor: BatteryMonitor
    private let lightSensorManager: LightSensorManager
    private let eyeStrainReminderService: EyeStrainReminderService
    private let analyticsService: AnalyticsService
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - Initialization
    
    override init() {
        self.prefsManager = PreferencesManager.shared
        self.schedulingService = SchedulingService.shared
        self.locationService = LocationCalculationService.shared
        self.overlayManager = OverlayWindowManager.shared
        self.batteryMonitor = BatteryMonitor.shared
        self.lightSensorManager = LightSensorManager.shared
        self.eyeStrainReminderService = EyeStrainReminderService.shared
        self.analyticsService = AnalyticsService.shared
        
        super.init()
        
        loadSettings()
        setupBindings()
        setupLocationBindings()
        setupBatteryBindings()
        setupLightSensorBindings()
        setupEyeStrainBindings()
        
        // Start battery monitoring
        batteryMonitor.startMonitoring()
        
        // Setup app lifecycle notifications
        setupLifecycleObservation()
        
        // Request notification permission if reminders are enabled
        if eyeStrainRemindersEnabled {
            eyeStrainReminderService.requestNotificationPermission { _ in }
        }
    }
    
    // MARK: - Overlay Control Methods
    
    /// Toggle overlay on/off
    func toggleOverlay() {
        isEnabled.toggle()
        prefsManager.setOverlayEnabled(isEnabled)
        updateOverlayManager()
        
        // Log analytics event
        analyticsService.logOverlayToggled(
            isEnabled: isEnabled,
            opacity: opacity,
            preset: currentPreset
        )
    }
    
    /// Update overlay opacity
    func updateOpacity(_ newOpacity: Float) {
        opacity = max(0, min(1, newOpacity))
        prefsManager.setOpacity(opacity)
        if isEnabled {
            // Apply battery optimization if enabled
            let adjustedOpacity = getEffectiveOpacity()
            overlayManager.updateOpacity(adjustedOpacity)
        }
        
        // Log analytics event (throttled internally)
        analyticsService.logOpacityChanged(
            opacity: opacity,
            preset: currentPreset
        )
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
        
        if enabled {
            lightSensorManager.setSensitivity(lightSensitivityFromString(ambientLightSensitivity))
            lightSensorManager.startMonitoring()
            isLightSensorActive = true
        } else {
            lightSensorManager.stopMonitoring()
            isLightSensorActive = false
        }
        
        // Update overlay opacity with new light settings
        if isEnabled {
            updateOverlayManager()
        }
    }
    
    /// Set light sensor sensitivity
    func setLightSensitivity(_ sensitivity: String) {
        ambientLightSensitivity = sensitivity
        prefsManager.setAmbientLightSensitivity(sensitivity)
        
        if useAmbientLight && isLightSensorActive {
            lightSensorManager.setSensitivity(lightSensitivityFromString(sensitivity))
            // Update overlay opacity with new sensitivity
            if isEnabled {
                updateOverlayManager()
            }
        }
    }
    
    /// Convert string sensitivity to LightSensorManager.LightSensitivity
    private func lightSensitivityFromString(_ sensitivity: String) -> LightSensorManager.LightSensitivity {
        switch sensitivity.lowercased() {
        case "low":
            return .low
        case "high":
            return .high
        default:
            return .medium
        }
    }
    
    // MARK: - Battery Optimization Methods
    
    /// Enable/disable battery optimization
    func setBatteryOptimizationEnabled(_ enabled: Bool) {
        batteryOptimizationEnabled = enabled
        prefsManager.setBatteryOptimizationEnabled(enabled)
        // Update overlay opacity with new settings
        if isEnabled {
            updateOverlayManager()
        }
    }
    
    /// Set battery optimization threshold (5% - 50%)
    func setBatteryOptimizationThreshold(_ threshold: Float) {
        prefsManager.setBatteryOptimizationThreshold(threshold)
        batteryMonitor.setLowBatteryThreshold(threshold)
        // Update overlay opacity with new threshold
        if isEnabled {
            updateOverlayManager()
        }
    }

    var batteryOptimizationThreshold: Float {
        prefsManager.getBatteryOptimizationThreshold()
    }
    
    /// Get effective opacity accounting for battery and light sensor optimization
    func getEffectiveOpacity() -> Float {
        var effectiveOpacity = opacity
        
        // Apply battery optimization if enabled
        if batteryOptimizationEnabled {
            effectiveOpacity = batteryMonitor.getAdjustedOpacity(effectiveOpacity)
        }
        
        // Apply light sensor adjustment if enabled
        if useAmbientLight && isLightSensorActive {
            effectiveOpacity = lightSensorManager.getAdjustedOpacity(effectiveOpacity)
        }
        
        return effectiveOpacity
    }
    
    // MARK: - Eye Strain Reminder Methods
    
    /// Enable/disable eye strain reminders
    func setEyeStrainRemindersEnabled(_ enabled: Bool) {
        eyeStrainRemindersEnabled = enabled
        prefsManager.setEyeStrainRemindersEnabled(enabled)
        
        if enabled {
            // Request notification permission if not already granted
            eyeStrainReminderService.requestNotificationPermission { [weak self] granted in
                if granted {
                    self?.eyeStrainReminderService.enableReminders()
                }
            }
        } else {
            eyeStrainReminderService.disableReminders()
        }
    }
    
    /// Set reminder interval in minutes (15 - 120 minutes)
    func setReminderInterval(_ minutes: Int) {
        reminderInterval = max(15, min(120, minutes))
        prefsManager.setReminderInterval(reminderInterval)
        eyeStrainReminderService.setReminderInterval(reminderInterval)
    }
    
    /// Set notification style for reminders
    func setNotificationStyle(_ style: String) {
        notificationStyle = style
        prefsManager.setNotificationStyle(style)
        
        if let notificationStyle = EyeStrainReminderService.NotificationStyle(rawValue: style) {
            eyeStrainReminderService.setNotificationStyle(notificationStyle)
        }
    }
    
    // MARK: - Preset Methods
    
    /// Apply preset using PresetProfile model
    func applyPreset(_ preset: PresetProfile) {
        currentPreset = preset.name
        prefsManager.setCurrentPreset(preset.name)
        updateOpacity(preset.opacity)
        updateColorVariant(preset.colorVariant.rawValue)
        
        // Enable overlay if it's not already enabled
        if !isEnabled {
            isEnabled = true
            prefsManager.setOverlayEnabled(true)
            updateOverlayManager()
        }
        
        // Log analytics event for preset application
        analyticsService.logPresetApplied(
            preset: preset.name,
            opacity: preset.opacity
        )
    }
    
    /// Apply preset by name
    func applyPreset(_ presetName: String) {
        applyPresetByName(presetName)
    }

    /// Apply preset by name (legacy method for backward compatibility)
    func applyPresetByName(_ presetName: String) {
        currentPreset = presetName
        prefsManager.setCurrentPreset(presetName)
        
        var presetOpacity: Float = 0.5
        
        switch presetName.lowercased() {
        case "work":
            presetOpacity = 0.3
            updateOpacity(presetOpacity)
            settings.colorVariant = Constants.Colors.redStandard
        case "gaming":
            presetOpacity = 0.4
            updateOpacity(presetOpacity)
            settings.colorVariant = Constants.Colors.redOrange
        case "movie":
            presetOpacity = 0.5
            updateOpacity(presetOpacity)
            settings.colorVariant = Constants.Colors.redStandard
        case "sleep":
            presetOpacity = 0.7
            updateOpacity(presetOpacity)
            settings.colorVariant = Constants.Colors.redPink
        default:
            break
        }

        updateColorVariant(settings.colorVariant)
        
        // Log analytics event for preset application
        analyticsService.logPresetApplied(
            preset: presetName,
            opacity: presetOpacity
        )
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
    
    /// Setup bindings for battery monitor updates
    private func setupBatteryBindings() {
        // Set initial battery threshold from preferences
        batteryMonitor.setLowBatteryThreshold(batteryOptimizationThreshold)
        
        // Bind to battery monitor changes
        batteryMonitor.$batteryLevel
            .sink { [weak self] value in
                self?.batteryLevel = value
            }
            .store(in: &cancellables)
        
        batteryMonitor.$isLow
            .sink { [weak self] value in
                self?.isBatteryLow = value
                // Update overlay opacity when battery state changes
                if self?.isEnabled == true && self?.batteryOptimizationEnabled == true {
                    self?.updateOverlayManager()
                }
            }
            .store(in: &cancellables)
        
        batteryMonitor.$isCritical
            .sink { [weak self] value in
                self?.isBatteryCritical = value
                // Update overlay opacity when battery state changes
                if self?.isEnabled == true && self?.batteryOptimizationEnabled == true {
                    self?.updateOverlayManager()
                }
            }
            .store(in: &cancellables)
        
        batteryMonitor.$batteryStatusIcon
            .sink { [weak self] value in
                self?.batteryStatusIcon = value
            }
            .store(in: &cancellables)
        
        // Handle low battery transition (for notifications)
        batteryMonitor.$didTransitionToLow
            .filter { $0 }
            .sink { [weak self] _ in
                self?.showLowBatteryNotification()
            }
            .store(in: &cancellables)
        
        // Handle critical battery transition (for notifications)
        batteryMonitor.$didTransitionToCritical
            .filter { $0 }
            .sink { [weak self] _ in
                self?.showCriticalBatteryNotification()
            }
            .store(in: &cancellables)
    }
    
    private func updateOverlayManager() {
        if isEnabled {
            let effectiveOpacity = getEffectiveOpacity()
            overlayManager.updateOpacity(effectiveOpacity)
        } else {
            overlayManager.hideOverlay()
        }
    }
    
    // MARK: - Battery Notification Methods
    
    /// Show low battery warning notification
    private func showLowBatteryNotification() {
        let notification = NSNotification.Name("BatteryLowWarning")
        NotificationCenter.default.post(name: notification, object: nil, userInfo: [
            "batteryLevel": batteryLevel,
            "threshold": batteryOptimizationThreshold
        ])
    }
    
    /// Show critical battery warning notification
    private func showCriticalBatteryNotification() {
        let notification = NSNotification.Name("BatteryCriticalWarning")
        NotificationCenter.default.post(name: notification, object: nil, userInfo: [
            "batteryLevel": batteryLevel
        ])
    }
    
    /// Setup bindings for light sensor updates
    private func setupLightSensorBindings() {
        // Load light sensor settings from preferences
        ambientLightSensitivity = prefsManager.getAmbientLightSensitivity()
        
        // Bind to light sensor changes
        lightSensorManager.$luxValue
            .sink { [weak self] value in
                self?.currentLux = value
            }
            .store(in: &cancellables)
        
        lightSensorManager.$lightingCondition
            .map { $0.rawValue }
            .sink { [weak self] condition in
                self?.lightingCondition = condition
            }
            .store(in: &cancellables)
        
        // Update overlay opacity when light conditions change
        lightSensorManager.$opacityMultiplier
            .sink { [weak self] _ in
                if self?.isEnabled == true && self?.useAmbientLight == true {
                    self?.updateOverlayManager()
                }
            }
            .store(in: &cancellables)
    }
    
    /// Setup bindings for eye strain reminder service
    private func setupEyeStrainBindings() {
        // Load eye strain settings
        notificationStyle = prefsManager.getNotificationStyle()
        
        // Bind to eye strain service state changes
        eyeStrainReminderService.$isEnabled
            .sink { [weak self] enabled in
                self?.eyeStrainRemindersEnabled = enabled
            }
            .store(in: &cancellables)
        
        eyeStrainReminderService.$reminderInterval
            .sink { [weak self] interval in
                self?.reminderInterval = interval
            }
            .store(in: &cancellables)
        
        eyeStrainReminderService.$notificationStyle
            .map { $0.rawValue }
            .sink { [weak self] style in
                self?.notificationStyle = style
            }
            .store(in: &cancellables)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    /// Setup observation for app lifecycle events
    private func setupLifecycleObservation() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appDidEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appWillEnterForeground),
            name: UIApplication.willEnterForegroundNotification,
            object: nil
        )
    }
    
    /// Handle app entering background
    @objc private func appDidEnterBackground() {
        if useAmbientLight {
            lightSensorManager.pauseMonitoring()
        }
    }
    
    /// Handle app entering foreground
    @objc private func appWillEnterForeground() {
        if useAmbientLight && isLightSensorActive {
            lightSensorManager.resumeMonitoring()
        }
    }
}
