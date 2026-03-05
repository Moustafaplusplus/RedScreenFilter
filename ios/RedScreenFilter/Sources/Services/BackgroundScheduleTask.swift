//
//  BackgroundScheduleTask.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Phase 35-40% - Background Task Scheduling
//

import Foundation
import BackgroundTasks
import os.log

/// BackgroundScheduleTask - Manages background task execution for schedule updates
/// Runs periodically to check if overlay should be active based on schedule
/// Uses App Groups for state persistence across app and background tasks
class BackgroundScheduleTask {
    
    // MARK: - Constants
    
    /// Background task identifier registered in Info.plist
    static let taskIdentifier = "com.redscreenfilter.schedule-update"
    
    // MARK: - Dependencies
    
    private let schedulingService = SchedulingService.shared
    private let prefsManager = PreferencesManager.shared
    private let logger = OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask")
    
    // MARK: - Task Registration
    
    /// Registers the background task with BGTaskScheduler
    /// Should be called in AppDelegate's didFinishLaunchingWithOptions
    /// - Returns: Boolean indicating registration success
    @discardableResult
    static func registerBackgroundTask() -> Bool {
        let registered = BGTaskScheduler.shared.register(
            forTaskWithIdentifier: taskIdentifier,
            using: .global()
        ) { task in
            guard let processingTask = task as? BGProcessingTask else {
                os_log("Invalid task type received", log: OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask"), type: .error)
                task.setTaskCompleted(success: false)
                return
            }
            
            BackgroundScheduleTask().handleBackgroundTask(processingTask)
        }
        
        if registered {
            os_log("Background task registered successfully", log: OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask"), type: .info)
        } else {
            os_log("Failed to register background task", log: OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask"), type: .error)
        }
        
        return registered
    }
    
    // MARK: - Task Submission
    
    /// Submits a background task request to the system
    /// Called when schedule changes or app enters background
    /// System determines actual execution time based on device conditions
    static func scheduleBackgroundTask() {
        let request = BGProcessingTaskRequest(identifier: taskIdentifier)
        
        // Allow task to run when device is not being actively used
        request.requiresNetworkConnectivity = false
        request.requiresExternalPower = false
        
        // Set earliest begin date to 15 minutes from now
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        
        do {
            // Cancel any pending tasks before submitting new one
            BGTaskScheduler.shared.cancel(taskIdentifier: taskIdentifier)
            
            try BGTaskScheduler.shared.submit(request)
            os_log("Background task scheduled successfully", log: OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask"), type: .info)
        } catch {
            os_log("Failed to schedule background task: %{public}@", log: OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask"), type: .error, error.localizedDescription)
        }
    }
    
    // MARK: - Task Execution
    
    /// Handles the background task execution
    /// Checks schedule and updates overlay state if needed
    /// - Parameter task: The BGProcessingTask to execute
    private func handleBackgroundTask(_ task: BGProcessingTask) {
        os_log("Background task started", log: logger, type: .info)
        
        // Set expiration handler to gracefully handle task termination
        task.expirationHandler = {
            os_log("Background task expired - cleaning up", log: self.logger, type: .warning)
            task.setTaskCompleted(success: false)
        }
        
        // Perform the schedule check on a background queue
        DispatchQueue.global(qos: .background).async { [weak self] in
            guard let self = self else {
                task.setTaskCompleted(success: false)
                return
            }
            
            do {
                // Check if schedule is active
                guard self.schedulingService.isScheduleActive() else {
                    os_log("Schedule not active - skipping update", log: self.logger, type: .info)
                    task.setTaskCompleted(success: true)
                    self.rescheduleNextTask()
                    return
                }
                
                // Determine if overlay should be active based on current time
                let shouldBeActive = self.schedulingService.determineOverlayState()
                let currentlyActive = self.prefsManager.isOverlayEnabled()
                
                os_log("Schedule check - Should be active: %{public}@, Currently active: %{public}@",
                       log: self.logger,
                       type: .info,
                       shouldBeActive ? "YES" : "NO",
                       currentlyActive ? "YES" : "NO")
                
                // Update state if needed
                if shouldBeActive != currentlyActive {
                    self.updateOverlayState(shouldBeActive)
                    os_log("Overlay state updated to: %{public}@", log: self.logger, type: .info, shouldBeActive ? "ACTIVE" : "INACTIVE")
                }
                
                // Mark task as complete
                task.setTaskCompleted(success: true)
                
                // Schedule next background task
                self.rescheduleNextTask()
                
            } catch {
                os_log("Background task failed: %{public}@", log: self.logger, type: .error, error.localizedDescription)
                task.setTaskCompleted(success: false)
            }
        }
    }
    
    // MARK: - Helper Methods
    
    /// Updates the overlay state in preferences
    /// Uses App Groups to persist state for access from main app
    /// - Parameter shouldBeActive: Whether overlay should be enabled
    private func updateOverlayState(_ shouldBeActive: Bool) {
        prefsManager.setOverlayEnabled(shouldBeActive)
        
        // Save to App Groups UserDefaults for cross-process access
        if let appGroupDefaults = UserDefaults(suiteName: "group.com.redscreenfilter") {
            appGroupDefaults.set(shouldBeActive, forKey: "overlayEnabled")
            appGroupDefaults.set(Date(), forKey: "lastScheduleUpdate")
            appGroupDefaults.synchronize()
        }
        
        // Post notification for app to update UI if running
        NotificationCenter.default.post(
            name: NSNotification.Name("ScheduleStateChanged"),
            object: nil,
            userInfo: ["isEnabled": shouldBeActive]
        )
    }
    
    /// Schedules the next background task execution
    private func rescheduleNextTask() {
        BackgroundScheduleTask.scheduleBackgroundTask()
    }
}
