//
//  LightSensorManager.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine
import ARKit
import AVFoundation

/// LightSensorManager - Monitors ambient light levels using ARKit or AVCaptureDevice
/// Provides reactive lux value updates and opacity adjustments based on light conditions
class LightSensorManager: NSObject, ObservableObject {
    static let shared = LightSensorManager()
    
    // MARK: - @Published Properties
    
    /// Current ambient light level in lux (candelas per square meter)
    @Published var luxValue: Float = 500.0
    
    /// Estimated lighting condition (very dark, dim, normal, bright)
    @Published var lightingCondition: LightingCondition = .normal
    
    /// Suggested opacity adjustment multiplier (0.0 - 1.0)
    @Published var opacityMultiplier: Float = 1.0
    
    /// Current sensitivity level for light detection
    @Published var sensitivity: LightSensitivity = .medium
    
    /// Flag indicating if sensor is actively monitoring
    @Published var isMonitoring: Bool = false
    
    /// Current raw lux reading (before smoothing)
    @Published var rawLux: Float = 500.0
    
    // MARK: - Types
    
    enum LightingCondition: String {
        case veryDark = "Very Dark"
        case dim = "Dim"
        case normal = "Normal"
        case bright = "Bright"
        
        var emoji: String {
            switch self {
            case .veryDark:
                return "🌙"
            case .dim:
                return "🌑"
            case .normal:
                return "☀️"
            case .bright:
                return "☀️☀️"
            }
        }
    }
    
    enum LightSensitivity: String, CaseIterable {
        case low = "Low"
        case medium = "Medium"
        case high = "High"
        
        /// Get threshold range for this sensitivity level
        var thresholds: (dark: Float, dim: Float, normal: Float) {
            switch self {
            case .low:
                return (30, 300, 2000)      // Less reactive
            case .medium:
                return (50, 500, 3000)      // Default
            case .high:
                return (80, 800, 5000)      // More reactive
            }
        }
    }
    
    // MARK: - Properties
    
    private let arSession = ARSession()
    private var displayLink: CADisplayLink?
    private var luxHistory: [Float] = []
    private let maxHistorySize = 10
    private let smoothingFactor: Float = 0.3  // Exponential moving average factor
    private var isARKitAvailable = false
    private var lastUpdateTime: Date = Date()
    private let updateInterval: TimeInterval = 0.5  // Update every 500ms
    
    // MARK: - Initialization
    
    override init() {
        super.init()
        checkARKitAvailability()
    }
    
    deinit {
        stopMonitoring()
    }
    
    // MARK: - Public Methods
    
    /// Start monitoring ambient light levels
    func startMonitoring() {
        guard !isMonitoring else { return }
        
        if isARKitAvailable {
            startARKitMonitoring()
        } else {
            startFallbackMonitoring()
        }
        
        isMonitoring = true
    }
    
    /// Stop monitoring ambient light levels
    func stopMonitoring() {
        guard isMonitoring else { return }
        
        if isARKitAvailable {
            arSession.pause()
        }
        
        if displayLink != nil {
            displayLink?.invalidate()
            displayLink = nil
        }
        
        isMonitoring = false
    }
    
    /// Pause monitoring (e.g., app enters background)
    func pauseMonitoring() {
        guard isMonitoring else { return }
        
        if isARKitAvailable {
            arSession.pause()
        }
        
        if displayLink != nil {
            displayLink?.invalidate()
            displayLink = nil
        }
    }
    
    /// Resume monitoring (e.g., app enters foreground)
    func resumeMonitoring() {
        guard isMonitoring else { return }
        
        if isARKitAvailable {
            startARKitMonitoring()
        } else {
            startFallbackMonitoring()
        }
    }
    
    /// Set light sensitivity level
    func setSensitivity(_ level: LightSensitivity) {
        DispatchQueue.main.async {
            self.sensitivity = level
            self.updateOpacityForCurrentLux()
        }
    }
    
    /// Get opacity suggestion based on current ambient light
    /// - Parameters:
    ///   - baseOpacity: The base opacity value to adjust
    /// - Returns: Adjusted opacity value (0.0 - 1.0)
    func getAdjustedOpacity(_ baseOpacity: Float) -> Float {
        return baseOpacity * opacityMultiplier
    }
    
    /// Get lighting condition description with emoji
    func getLightingDescription() -> String {
        return "\(lightingCondition.emoji) \(lightingCondition.rawValue)"
    }
    
    /// Get formatted lux reading
    func getFormattedLuxReading() -> String {
        return String(format: "%.0f lux", luxValue)
    }
    
    // MARK: - Private Methods
    
    /// Check ARKit availability
    private func checkARKitAvailability() {
        let isARKitSupported = ARWorldTrackingConfiguration.isSupported
        
        if isARKitSupported {
            let arKitConfig = ARWorldTrackingConfiguration()
            // Check if frame semantics are available
            if arKitConfig.frameSemantics.contains(.personSegmentationWithDepth) {
                isARKitAvailable = true
            } else {
                isARKitAvailable = true  // Still use ARKit without semantics
            }
        }
    }
    
    /// Start ARKit-based light monitoring
    private func startARKitMonitoring() {
        let configuration = ARWorldTrackingConfiguration()
        configuration.planeDetection = []  // No plane detection needed
        
        // Request light estimation
        if #available(iOS 13.0, *) {
            // Use the new light estimation
        }
        
        arSession.run(configuration)
        setupDisplayLink()
    }
    
    /// Start fallback light monitoring using AVCaptureDevice
    private func startFallbackMonitoring() {
        setupDisplayLink()
    }
    
    /// Setup display link for periodic updates
    private func setupDisplayLink() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            let displayLink = CADisplayLink(
                target: self,
                selector: #selector(self.updateLightLevel)
            )
            displayLink.preferredFramesPerSecond = 2  // Update twice per second
            displayLink.add(to: .main, forMode: .common)
            self.displayLink = displayLink
        }
    }
    
    /// Update light level readings
    @objc private func updateLightLevel() {
        let now = Date()
        guard now.timeIntervalSince(lastUpdateTime) >= updateInterval else { return }
        
        lastUpdateTime = now
        
        if isARKitAvailable {
            updateFromARKit()
        } else {
            updateFromAVCapture()
        }
    }
    
    /// Update light level from ARKit
    private func updateFromARKit() {
        let frame = arSession.currentFrame
        
        // Get light estimation
        if let lightEstimate = frame?.lightEstimate {
            let ambientIntensity = lightEstimate.ambientIntensity
            // Convert intensity (lumens) to approximate lux
            // Typical ranges: 100-5000 lumens in indoor settings
            let estimatedLux = Float(ambientIntensity) * 10.0  // Approximate conversion
            
            updateWithLuxValue(estimatedLux)
        }
    }
    
    /// Update light level from AVCaptureDevice (fallback)
    private func updateFromAVCapture() {
        guard let device = AVCaptureDevice.default(for: .video) else {
            return
        }
        
        do {
            try device.lockForConfiguration()
            defer { device.unlockForConfiguration() }
            
            // Get ISO and exposure to estimate lux
            // Formula: lux ≈ (ISO * shutter_speed) / (f_number^2)
            // For simplicity, estimate based on current ISO
            let iso = Float(device.iso)
            let estimatedLux = iso * 5.0  // Rough estimation
            
            updateWithLuxValue(estimatedLux)
        } catch {
            // Silent fail - device might not support lock
        }
    }
    
    /// Update lux value with smoothing algorithm
    private func updateWithLuxValue(_ newLux: Float) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            self.rawLux = newLux
            
            // Apply exponential moving average smoothing
            if self.luxHistory.isEmpty {
                self.luxValue = newLux
            } else {
                let smoothedValue = (self.luxValue * (1 - self.smoothingFactor)) +
                                   (newLux * self.smoothingFactor)
                self.luxValue = smoothedValue
            }
            
            // Keep history for additional analysis
            self.luxHistory.append(newLux)
            if self.luxHistory.count > self.maxHistorySize {
                self.luxHistory.removeFirst()
            }
            
            self.updateOpacityForCurrentLux()
        }
    }
    
    /// Calculate opacity multiplier and lighting condition based on current lux
    private func updateOpacityForCurrentLux() {
        let thresholds = sensitivity.thresholds
        let lux = luxValue
        
        // Determine lighting condition
        if lux < thresholds.dark {
            lightingCondition = .veryDark
            opacityMultiplier = 0.8  // Make overlay very visible in darkness
        } else if lux < thresholds.dim {
            lightingCondition = .dim
            opacityMultiplier = 0.7  // Reduce to 70%
        } else if lux < thresholds.normal {
            lightingCondition = .normal
            opacityMultiplier = 0.5  // Reduce to 50% in normal light
        } else {
            lightingCondition = .bright
            opacityMultiplier = 0.3  // Reduce to 30% in bright light
        }
    }
}
