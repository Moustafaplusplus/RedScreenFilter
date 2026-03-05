//
//  BatteryMonitor.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import Combine
import UIKit

/// BatteryMonitor - Monitors device battery state and publishes changes
/// Uses Combine for reactive updates
/// Observes UIDevice.batteryStateDidChangeNotification notifications
class BatteryMonitor: NSObject, ObservableObject {
    static let shared = BatteryMonitor()
    
    // MARK: - @Published Properties
    
    /// Current battery level (0.0 - 1.0)
    @Published var batteryLevel: Float = 0.0
    
    /// Current battery state
    @Published var batteryState: UIDevice.BatteryState = .unknown
    
    /// Indicates if battery is in critical state (< 10%)
    @Published var isCritical: Bool = false
    
    /// Indicates if battery is low (< threshold set by user, default 20%)
    @Published var isLow: Bool = false
    
    /// Flag indicating when battery transitions to low state (for notifications)
    @Published var didTransitionToLow: Bool = false
    
    /// Flag indicating when battery transitions to critical state (for notifications)
    @Published var didTransitionToCritical: Bool = false
    
    /// Current threshold for low battery warning (0.05 - 0.5, default 0.2 = 20%)
    @Published var lowBatteryThreshold: Float = 0.2
    
    private var isMonitoring = false
    private let notificationCenter = NotificationCenter.default
    private var lastKnownState: (level: Float, isCritical: Bool, isLow: Bool) = (1.0, false, false)
    
    // MARK: - Initialization
    
    override init() {
        super.init()
        enableBatteryMonitoring()
        updateBatteryStatus()
    }
    
    deinit {
        stopMonitoring()
    }
    
    // MARK: - Public Methods
    
    /// Enable battery monitoring and start observing notifications
    func startMonitoring() {
        guard !isMonitoring else { return }
        
        enableBatteryMonitoring()
        registerForNotifications()
        updateBatteryStatus()
        isMonitoring = true
    }
    
    /// Disable battery monitoring and stop observing notifications
    func stopMonitoring() {
        guard isMonitoring else { return }
        
        unregisterFromNotifications()
        UIDevice.current.isBatteryMonitoringEnabled = false
        isMonitoring = false
    }
    
    /// Update the low battery threshold (5% - 50%)
    func setLowBatteryThreshold(_ threshold: Float) {
        DispatchQueue.main.async {
            self.lowBatteryThreshold = max(0.05, min(0.5, threshold))
            self.updateBatteryStatus()
        }
    }
    
    /// Get formatted battery level as percentage string
    func getBatteryPercentageString() -> String {
        return "\(Int(batteryLevel * 100))%"
    }
    
    /// Get battery state description
    func getBatteryStateDescription() -> String {
        switch batteryState {
        case .unknown:
            return "Unknown"
        case .unplugged:
            return "Unplugged"
        case .charging:
            return "Charging"
        case .full:
            return "Full"
        @unknown default:
            return "Unknown"
        }
    }
    
    /// Get battery status indicator emoji/icon
    func getBatteryStatusIcon() -> String {
        if batteryState == .charging {
            return "🔌"
        } else if isCritical {
            return "🪫"
        } else if isLow {
            return "⚠️"
        } else {
            return "🔋"
        }
    }
    
    /// Check if battery is in a critical or low state
    func shouldReduceOverlayOpacity() -> Bool {
        return isLow || isCritical
    }
    
    /// Calculate opacity reduction based on battery state
    /// - Parameters:
    ///   - baseOpacity: The base opacity value (0.0 - 1.0)
    /// - Returns: Adjusted opacity for battery optimization
    func getAdjustedOpacity(_ baseOpacity: Float) -> Float {
        if isCritical {
            // Ultra-critical: reduce by 50% or disable completely if baseOpacity < 0.3
            if baseOpacity < 0.3 {
                return 0.0 // Disable overlay
            }
            return baseOpacity * 0.5
        } else if isLow {
            // Low battery: reduce by 30%
            return baseOpacity * 0.7
        }
        return baseOpacity
    }
    
    // MARK: - Private Methods
    
    /// Enable battery monitoring on UIDevice
    private func enableBatteryMonitoring() {
        UIDevice.current.isBatteryMonitoringEnabled = true
    }
    
    /// Register for battery state change notifications
    private func registerForNotifications() {
        notificationCenter.addObserver(
            self,
            selector: #selector(batteryStatusDidChange),
            name: UIDevice.batteryStateDidChangeNotification,
            object: nil
        )
        
        notificationCenter.addObserver(
            self,
            selector: #selector(batteryStatusDidChange),
            name: UIDevice.batteryLevelDidChangeNotification,
            object: nil
        )
    }
    
    /// Unregister from battery state change notifications
    private func unregisterFromNotifications() {
        notificationCenter.removeObserver(self, name: UIDevice.batteryStateDidChangeNotification, object: nil)
        notificationCenter.removeObserver(self, name: UIDevice.batteryLevelDidChangeNotification, object: nil)
    }
    
    /// Update battery status from UIDevice and publish changes
    @objc private func batteryStatusDidChange() {
        updateBatteryStatus()
    }
    
    /// Internal method to read battery status and update @Published properties
    private func updateBatteryStatus() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            let device = UIDevice.current
            let currentLevel = device.batteryLevel
            let currentState = device.batteryState
            
            // Update published properties
            self.batteryLevel = currentLevel
            self.batteryState = currentState
            
            // Check battery thresholds
            let newIsCritical = currentLevel < 0.1 && currentState != .charging
            let newIsLow = currentLevel < self.lowBatteryThreshold && currentState != .charging
            
            // Detect state transitions for notifications
            if newIsCritical && !self.lastKnownState.isCritical {
                self.didTransitionToCritical = true
                // Reset after a short delay to allow observers to react
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    self.didTransitionToCritical = false
                }
            }
            
            if newIsLow && !self.lastKnownState.isLow && !newIsCritical {
                self.didTransitionToLow = true
                // Reset after a short delay to allow observers to react
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    self.didTransitionToLow = false
                }
            }
            
            self.isCritical = newIsCritical
            self.isLow = newIsLow
            
            // Update last known state
            self.lastKnownState = (level: currentLevel, isCritical: newIsCritical, isLow: newIsLow)
        }
    }
}
