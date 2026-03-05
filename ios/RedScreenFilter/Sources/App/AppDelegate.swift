//
//  AppDelegate.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Enhanced in Phase 35-40% with Background Task Scheduling
//

import UIKit
import BackgroundTasks
import os.log

class AppDelegate: NSObject, UIApplicationDelegate {
    
    private let logger = OSLog(subsystem: "com.redscreenfilter", category: "AppDelegate")
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        // Register background task for schedule updates
        BackgroundScheduleTask.registerBackgroundTask()
        
        os_log("AppDelegate initialized - Background task registered", log: logger, type: .info)
        
        return true
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        // Schedule background task when app enters background
        // This ensures schedule checks continue even when app is not active
        BackgroundScheduleTask.scheduleBackgroundTask()
        
        os_log("App entered background - Background task scheduled", log: logger, type: .info)
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        // Sync state when app returns to foreground
        // Check if schedule changed state while app was backgrounded
        syncScheduleState()
        
        os_log("App entering foreground - Syncing schedule state", log: logger, type: .info)
    }
    
    // MARK: - Helper Methods
    
    /// Syncs schedule state when app returns to foreground
    /// Checks App Groups UserDefaults for any state changes from background tasks
    private func syncScheduleState() {
        guard let appGroupDefaults = UserDefaults(suiteName: "group.com.redscreenfilter") else {
            return
        }
        
        // Check if background task updated the state
        if let lastUpdate = appGroupDefaults.object(forKey: "lastScheduleUpdate") as? Date {
            let timeSinceUpdate = Date().timeIntervalSince(lastUpdate)
            
            // If update was recent (within last hour), sync the state
            if timeSinceUpdate < 3600 {
                let shouldBeEnabled = appGroupDefaults.bool(forKey: "overlayEnabled")
                PreferencesManager.shared.setOverlayEnabled(shouldBeEnabled)
                
                os_log("Synced overlay state from background task: %{public}@", 
                       log: logger, 
                       type: .info, 
                       shouldBeEnabled ? "ENABLED" : "DISABLED")
            }
        }
    }
}
