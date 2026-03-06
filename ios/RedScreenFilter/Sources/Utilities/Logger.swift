//
//  Logger.swift
//  RedScreenFilter
//
//  Created on 3/6/2026.
//

import Foundation
import OSLog

/// Centralized logging system using OSLog framework
/// Production-ready logging with proper subsystems and categories
struct AppLogger {
    
    // MARK: - Subsystems
    
    private static let subsystem = Bundle.main.bundleIdentifier ?? "com.redscreenfilter"
    
    // MARK: - Loggers by Category
    
    /// Core Data operations and database queries
    static let coreData = Logger(subsystem: subsystem, category: "CoreData")
    
    /// Analytics event tracking and statistics
    static let analytics = Logger(subsystem: subsystem, category: "Analytics")
    
    /// Permission requests and authorization status
    static let permissions = Logger(subsystem: subsystem, category: "Permissions")
    
    /// Location services and sunrise/sunset calculations
    static let location = Logger(subsystem: subsystem, category: "Location")
    
    /// Notification scheduling and delivery
    static let notifications = Logger(subsystem: subsystem, category: "Notifications")
    
    /// Overlay window management
    static let overlay = Logger(subsystem: subsystem, category: "Overlay")
    
    /// Scheduling service and background tasks
    static let scheduling = Logger(subsystem: subsystem, category: "Scheduling")
    
    /// Battery monitoring
    static let battery = Logger(subsystem: subsystem, category: "Battery")
    
    /// Light sensor management
    static let sensor = Logger(subsystem: subsystem, category: "Sensor")
    
    /// Preset management
    static let presets = Logger(subsystem: subsystem, category: "Presets")
    
    /// User preferences and settings
    static let preferences = Logger(subsystem: subsystem, category: "Preferences")
    
    /// App lifecycle events
    static let lifecycle = Logger(subsystem: subsystem, category: "Lifecycle")
    
    /// Siri shortcuts and voice control
    static let siri = Logger(subsystem: subsystem, category: "Siri")
    
    /// Widget operations
    static let widget = Logger(subsystem: subsystem, category: "Widget")
    
    /// General app operations
    static let general = Logger(subsystem: subsystem, category: "General")
}

// MARK: - Logger Extensions for Convenience

extension Logger {
    
    /// Log successful operation
    func success(_ message: String) {
        self.info("✅ \(message)")
    }
    
    /// Log warning
    func warning(_ message: String) {
        self.warning("⚠️ \(message)")
    }
    
    /// Log error with context
    func error(_ message: String, error: Error) {
        self.error("❌ \(message): \(error.localizedDescription)")
    }
    
    /// Log error without Error object
    func error(_ message: String) {
        self.error("❌ \(message)")
    }
    
    /// Log debug information (only in debug builds)
    func debug(_ message: String) {
        #if DEBUG
        self.debug("🐛 \(message)")
        #endif
    }
}
