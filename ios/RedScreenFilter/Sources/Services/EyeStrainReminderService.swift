//
//  EyeStrainReminderService.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Enhanced in Phase 98-99% with permission management
//

import Foundation
import Combine
import UserNotifications
import CallKit
import OSLog

/// EyeStrainReminderService - Manages 20-20-20 eye strain reminder notifications
/// Schedules periodic reminders to take eye breaks
/// Uses UNUserNotificationCenter for system notifications
class EyeStrainReminderService: NSObject, ObservableObject {
    static let shared = EyeStrainReminderService()
    
    // MARK: - @Published Properties
    
    /// Flag indicating if reminders are currently enabled
    @Published var isEnabled: Bool = false
    
    /// Interval between reminders in minutes
    @Published var reminderInterval: Int = 20
    
    /// Notification style preference
    @Published var notificationStyle: NotificationStyle = .sound
    
    /// Authorization status for notifications
    @Published var authorizationStatus: UNAuthorizationStatus = .notDetermined
    
    /// Flag indicating if reminders are paused
    @Published var isPaused: Bool = false
    
    // MARK: - Types
    
    enum NotificationStyle: String, CaseIterable {
        case silent = "Silent"
        case sound = "Sound"
        case vibration = "Vibration"
        
        var description: String {
            self.rawValue
        }
    }
    
    // MARK: - Constants
    
    private let notificationCategoryIdentifier = "EYE_STRAIN_REMINDER"
    private let notificationRequestIdentifier = "com.redscreenfilter.eyestrain"
    private let reminderIntervalKey = "eyeStrainReminderInterval"
    private let notificationStyleKey = "eyeStrainNotificationStyle"
    
    // MARK: - Properties
    
    private let notificationCenter = UNUserNotificationCenter.current()
    private let permissionManager = PermissionManager.shared
    private var removalObserver: NSObjectProtocol?
    private var callObserver: CXCallObserverDelegate?
    
    // MARK: - Initialization
    
    override init() {
        super.init()
        loadSettings()
        setupNotificationCategories()
        checkAuthorizationStatus()
    }
    
    deinit {
        if let observer = removalObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    // MARK: - Public Methods
    
    /// Request user permission for notifications
    func requestNotificationPermission(completion: @escaping (Bool) -> Void) {
        permissionManager.requestNotificationPermission(completion: completion)
    }
    
    /// Enable eye strain reminders - now with permission check
    func enableReminders() {
        // Check permission first
        guard permissionManager.hasNotificationPermission else {
            AppLogger.notifications.info("Requesting notification permission for reminders...")
            requestNotificationPermission { [weak self] granted in
                if granted {
                    self?.enableRemindersInternal()
                } else {
                    AppLogger.notifications.warning("Notification permission denied - reminders cannot be enabled")
                }
            }
            return
        }
        
        enableRemindersInternal()
    }
    
    /// Internal method to enable reminders (after permission check)
    private func enableRemindersInternal() {
                    self?.enableReminders()
                }
            }
            return
        }
        
        isEnabled = true
        scheduleNextReminder()
    }
    
    /// Disable eye strain reminders
    func disableReminders() {
        isEnabled = false
        cancelAllReminders()
    }
    
    /// Set reminder interval (15-120 minutes)
    func setReminderInterval(_ minutes: Int) {
        reminderInterval = max(15, min(120, minutes))
        
        // Reschedule if currently enabled
        if isEnabled {
            cancelAllReminders()
            scheduleNextReminder()
        }
    }
    
    /// Set notification style
    func setNotificationStyle(_ style: NotificationStyle) {
        notificationStyle = style
    }
    
    /// Pause reminders (e.g., during video calls)
    func pauseReminders() {
        isPaused = true
        cancelAllReminders()
    }
    
    /// Resume reminders
    func resumeReminders() {
        guard isPaused else { return }
        
        isPaused = false
        if isEnabled {
            scheduleNextReminder()
        }
    }
    
    /// Check if currently in a call (for auto-pause feature)
    func isInCall(completion: @escaping (Bool) -> Void) {
        let callObserver = CXCallObserver()
        callObserver.setDelegate(self, queue: .main)
        
        // Check current calls
        let hasCalls = !callObserver.calls.isEmpty
        completion(hasCalls)
    }
    
    // MARK: - Private Methods
    
    /// Load settings from UserDefaults
    private func loadSettings() {
        let defaults = UserDefaults.standard
        reminderInterval = defaults.integer(forKey: reminderIntervalKey)
        if reminderInterval == 0 {
            reminderInterval = 20
        }
        
        if let styleString = defaults.string(forKey: notificationStyleKey),
           let style = NotificationStyle(rawValue: styleString) {
            notificationStyle = style
        } else {
            notificationStyle = .sound
        }
    }
    
    /// Save settings to UserDefaults
    private func saveSettings() {
        let defaults = UserDefaults.standard
        defaults.set(reminderInterval, forKey: reminderIntervalKey)
        defaults.set(notificationStyle.rawValue, forKey: notificationStyleKey)
    }
    
    /// Setup notification categories and actions
    private func setupNotificationCategories() {
        let openAction = UNNotificationAction(
            identifier: "OPEN_APP",
            title: "Open",
            options: [.foreground]
        )
        
        let dismissAction = UNNotificationAction(
            identifier: "DISMISS",
            title: "Dismiss",
            options: []
        )
        
        let category = UNNotificationCategory(
            identifier: notificationCategoryIdentifier,
            actions: [openAction, dismissAction],
            intentIdentifiers: [],
            options: [.customDismissAction]
        )
        
        notificationCenter.setNotificationCategories([category])
    }
    
    /// Check authorization status
    private func checkAuthorizationStatus() {
        notificationCenter.getNotificationSettings { [weak self] settings in
            DispatchQueue.main.async {
                self?.authorizationStatus = settings.authorizationStatus
            }
        }
    }
    
    /// Schedule next eye strain reminder
    private func scheduleNextReminder() {
        guard isEnabled && !isPaused else { return }
        
        // Cancel existing reminder
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [notificationRequestIdentifier])
        
        // Create notification content
        let content = createReminderContent()
        
        // Calculate next trigger time
        let calendar = Calendar.current
        let now = Date()
        let nextTime = calendar.date(byAdding: .minute, value: reminderInterval, to: now) ?? now.addingTimeInterval(Double(reminderInterval) * 60)
        
        // Create trigger
        let components = calendar.dateComponents([.hour, .minute], from: nextTime)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
        
        // Create request
        let request = UNNotificationRequest(
            identifier: notificationRequestIdentifier,
            content: content,
            trigger: trigger
        )
        
        // Schedule
        notificationCenter.add(request) { [weak self] error in
            if let error = error {
                AppLogger.notifications.error("Failed to schedule eye strain reminder", error: error)
            } else {
                AppLogger.notifications.debug("Scheduled next eye strain reminder in \(self?.reminderInterval ?? 20) minutes")
                
                // Schedule another notification after this one fires
                DispatchQueue.main.asyncAfter(deadline: .now() + Double((self?.reminderInterval ?? 20) * 60) + 1) {
                    if self?.isEnabled == true && self?.isPaused == false {
                        self?.scheduleNextReminder()
                    }
                }
            }
        }
    }
    
    /// Create notification content for eye strain reminder
    private func createReminderContent() -> UNMutableNotificationContent {
        let content = UNMutableNotificationContent()
        
        content.title = "👀 Eye Break"
        content.body = "Look away for 20 seconds at something 20 feet away"
        content.categoryIdentifier = notificationCategoryIdentifier
        content.badge = NSNumber(value: 1)
        
        // Apply sound based on preference
        switch notificationStyle {
        case .sound:
            content.sound = .default
        case .vibration:
            content.sound = nil
            // Add vibration via UNNotificationSound if available
            if #available(iOS 16.0, *) {
                content.interruptionLevel = .timeSensitive
            }
        case .silent:
            content.sound = nil
        }
        
        // Add thread identifier for grouping
        content.threadIdentifier = "eye_strain_reminders"
        content.summaryArgument = "Eye Break"
        
        return content
    }
    
    /// Cancel all scheduled reminders
    private func cancelAllReminders() {
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [notificationRequestIdentifier])
    }
}

// MARK: - CXCallObserverDelegate

extension EyeStrainReminderService: CXCallObserverDelegate {
    func callObserver(_ callObserver: CXCallObserver, callChanged call: CXCall) {
        if call.isOutgoing || !call.isOnHold {
            pauseReminders()
        } else {
            resumeReminders()
        }
    }
}
