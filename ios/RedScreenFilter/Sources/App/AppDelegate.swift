//
//  AppDelegate.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Enhanced in Phase 35-40% with Background Task Scheduling
//  Enhanced in Phase 98-99% with Permission & Lifecycle Management
//  Enhanced in Phase 99-100% with Optimized Logging
//

import UIKit
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate {
    
    private var appGroupDefaults: UserDefaults?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        AppLogger.lifecycle.info("App launching...")
        
        // Initialize App Groups UserDefaults
        appGroupDefaults = UserDefaults(suiteName: "group.com.redscreenfilter")
        
        // Register background task for schedule updates
        BackgroundScheduleTask.registerBackgroundTask()
        
        // Initialize overlay state from App Groups (for widget sync)
        restoreOverlayState()
        
        // Check and request permissions if needed
        checkInitialPermissions()
        
        AppLogger.lifecycle.success("AppDelegate initialized - Background task registered")
        
        return true
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        AppLogger.lifecycle.info("App entering background...")
        
        // Persist current overlay state to App Groups
        persistOverlayState()
        
        // Schedule background task when app enters background
        // This ensures schedule checks continue even when app is not active
        BackgroundScheduleTask.scheduleBackgroundTask()
        
        // Pause light sensor monitoring to save battery
        LightSensorManager.shared.pauseMonitoring()
        
        AppLogger.lifecycle.info("App entered background - State persisted, background task scheduled")
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        AppLogger.lifecycle.info("App entering foreground...")
        
        // Sync state when app returns to foreground
        // Check if schedule changed state while app was backgrounded
        syncScheduleState()
        
        // Resume light sensor if it was enabled
        if PreferencesManager.shared.isAmbientLightEnabled() {
            LightSensorManager.shared.resumeMonitoring()
        }
        
        // Refresh analytics data
        NotificationCenter.default.post(name: NSNotification.Name("RefreshAnalytics"), object: nil)
        
        AppLogger.lifecycle.info("App entering foreground - State synced")
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        AppLogger.lifecycle.debug("App became active")
        
        // Refresh permission statuses
        PermissionManager.shared.checkPermissions()
        
        // Update battery monitoring
        BatteryMonitor.shared.updateBatteryState()
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        AppLogger.lifecycle.debug("App will resign active")
        
        // Save any pending analytics events
        CoreDataStack.shared.saveContext()
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        AppLogger.lifecycle.info("App will terminate")
        
        // Final state persistence
        persistOverlayState()
        CoreDataStack.shared.saveContext()
    }
    
    // MARK: - State Management
    
    /// Restore overlay state from App Groups on app launch
    private func restoreOverlayState() {
        guard let appGroupDefaults = appGroupDefaults else {
            AppLogger.lifecycle.warning("App Groups not available for state restoration")
            return
        }
        
        // Check if we have saved state
        if appGroupDefaults.object(forKey: "overlayEnabled") != nil {
            let isEnabled = appGroupDefaults.bool(forKey: "overlayEnabled")
            let opacity = appGroupDefaults.float(forKey: "lastOpacity")
            let colorVariant = appGroupDefaults.string(forKey: "lastColorVariant") ?? "red_standard"
            
            // Restore to PreferencesManager
            PreferencesManager.shared.setOverlayEnabled(isEnabled)
            if opacity > 0 {
                PreferencesManager.shared.setOpacity(opacity)
            }
            PreferencesManager.shared.setColorVariant(colorVariant)
            
            AppLogger.lifecycle.debug("Restored overlay state: enabled=\(isEnabled), opacity=\(opacity)")
        }
    }
    
    /// Persist overlay state to App Groups for widget sync and restoration
    private func persistOverlayState() {
        guard let appGroupDefaults = appGroupDefaults else { return }
        
        let prefsManager = PreferencesManager.shared
        
        appGroupDefaults.set(prefsManager.isOverlayEnabled(), forKey: "overlayEnabled")
        appGroupDefaults.set(prefsManager.getOpacity(), forKey: "lastOpacity")
        appGroupDefaults.set(prefsManager.getColorVariant(), forKey: "lastColorVariant")
        appGroupDefaults.set(Date(), forKey: "lastStatePersist")
        
        AppLogger.lifecycle.debug("Persisted overlay state to App Groups")
    }
    
    /// Syncs schedule state when app returns to foreground
    /// Checks App Groups UserDefaults for any state changes from background tasks
    private func syncScheduleState() {
        guard let appGroupDefaults = appGroupDefaults else {
            return
        }
        
        // Check if background task updated the state
        if let lastUpdate = appGroupDefaults.object(forKey: "lastScheduleUpdate") as? Date {
            let timeSinceUpdate = Date().timeIntervalSince(lastUpdate)
            
            // If update was recent (within last hour), sync the state
            if timeSinceUpdate < 3600 {
                let shouldBeEnabled = appGroupDefaults.bool(forKey: "overlayEnabled")
                PreferencesManager.shared.setOverlayEnabled(shouldBeEnabled)
                
                AppLogger.lifecycle.debug("Synced overlay state from background task: \(shouldBeEnabled ? "ENABLED" : "DISABLED")")
            }
        }
        
        // Restore overlay if it was enabled
        if PreferencesManager.shared.isOverlayEnabled() {
            OverlayWindowManager.shared.updateOpacity(PreferencesManager.shared.getOpacity())
        }
    }
    
    // MARK: - Permission Management
    
    /// Check if initial permissions need to be requested on first launch
    private func checkInitialPermissions() {
        let hasLaunchedBefore = UserDefaults.standard.bool(forKey: "hasLaunchedBefore")
        
        if !hasLaunchedBefore {
            AppLogger.lifecycle.info("First launch detected")
            UserDefaults.standard.set(true, forKey: "hasLaunchedBefore")
            
            // Don't request permissions immediately on first launch
            // Let the user explore the app first
            // Permissions will be requested when user enables features
        } else {
            // Check permissions on subsequent launches
            PermissionManager.shared.checkPermissions()
        }
    }
}

