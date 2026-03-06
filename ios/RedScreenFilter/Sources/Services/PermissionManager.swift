//
//  PermissionManager.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import UserNotifications
import CoreLocation
import Combine
import OSLog

/// PermissionManager - Centralized permission handling
/// Manages location and notification permissions with graceful fallbacks
class PermissionManager: NSObject, ObservableObject {
    // MARK: - Singleton
    
    static let shared = PermissionManager()
    
    // MARK: - Published Properties
    
    @Published var locationPermissionStatus: CLAuthorizationStatus = .notDetermined
    @Published var notificationPermissionStatus: UNAuthorizationStatus = .notDetermined
    @Published var hasLocationPermission: Bool = false
    @Published var hasNotificationPermission: Bool = false
    
    // MARK: - Private Properties
    
    private let locationManager = CLLocationManager()
    private let notificationCenter = UNUserNotificationCenter.current()
    
    // MARK: - Initialization
    
    private override init() {
        super.init()
        locationManager.delegate = self
        checkPermissions()
    }
    
    // MARK: - Permission Checking
    
    /// Check current permission statuses
    func checkPermissions() {
        checkLocationPermission()
        checkNotificationPermission()
    }
    
    /// Check location permission status
    private func checkLocationPermission() {
        if #available(iOS 14.0, *) {
            locationPermissionStatus = locationManager.authorizationStatus
        } else {
            locationPermissionStatus = CLLocationManager.authorizationStatus()
        }
        
        hasLocationPermission = (locationPermissionStatus == .authorizedWhenInUse || 
                                locationPermissionStatus == .authorizedAlways)
        
        AppLogger.permissions.debug("Location permission status: \(locationPermissionStatus.rawValue)")
    }
    
    /// Check notification permission status
    private func checkNotificationPermission() {
        notificationCenter.getNotificationSettings { [weak self] settings in
            DispatchQueue.main.async {
                self?.notificationPermissionStatus = settings.authorizationStatus
                self?.hasNotificationPermission = (settings.authorizationStatus == .authorized)
                
                AppLogger.permissions.debug("Notification permission status: \(settings.authorizationStatus.rawValue)")
            }
        }
    }
    
    // MARK: - Location Permission Request
    
    /// Request location permission with completion handler
    /// - Parameter completion: Called with success/failure
    func requestLocationPermission(completion: @escaping (Bool) -> Void) {
        let status: CLAuthorizationStatus
        if #available(iOS 14.0, *) {
            status = locationManager.authorizationStatus
        } else {
            status = CLLocationManager.authorizationStatus()
        }
        
        switch status {
        case .notDetermined:
            // Request permission
            locationPermissionCompletion = completion
            locationManager.requestWhenInUseAuthorization()
            AppLogger.permissions.info("Requesting location permission...")
            
        case .authorizedWhenInUse, .authorizedAlways:
            // Already authorized
            hasLocationPermission = true
            completion(true)
            AppLogger.permissions.info("Location already authorized")
            
        case .denied, .restricted:
            // Permission denied
            hasLocationPermission = false
            completion(false)
            AppLogger.permissions.warning("Location permission denied")
            showLocationPermissionDeniedAlert()
            
        @unknown default:
            completion(false)
        }
    }
    
    private var locationPermissionCompletion: ((Bool) -> Void)?
    
    // MARK: - Notification Permission Request
    
    /// Request notification permission with completion handler
    /// - Parameter completion: Called with success/failure
    func requestNotificationPermission(completion: @escaping (Bool) -> Void) {
        notificationCenter.getNotificationSettings { [weak self] settings in
            switch settings.authorizationStatus {
            case .notDetermined:
                // Request permission
                self?.notificationCenter.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                    DispatchQueue.main.async {
                        self?.hasNotificationPermission = granted
                        self?.notificationPermissionStatus = granted ? .authorized : .denied
                        completion(granted)
                        
                        if let error = error {
                            AppLogger.permissions.error("Notification permission request failed", error: error)
                        } else {
                            AppLogger.permissions.info("Notification permission: \(granted ? "granted" : "denied")")
                        }
                    }
                }
                
            case .authorized, .provisional:
                // Already authorized
                DispatchQueue.main.async {
                    self?.hasNotificationPermission = true
                    completion(true)
                    AppLogger.permissions.info("Notifications already authorized")
                }
                
            case .denied:
                // Permission denied
                DispatchQueue.main.async {
                    self?.hasNotificationPermission = false
                    completion(false)
                    AppLogger.permissions.warning("Notification permission denied")
                    self?.showNotificationPermissionDeniedAlert()
                }
                
            case .ephemeral:
                // Temporary authorization (App Clips)
                DispatchQueue.main.async {
                    completion(false)
                }
                
            @unknown default:
                DispatchQueue.main.async {
                    completion(false)
                }
            }
        }
    }
    
    // MARK: - Permission Prompts
    
    /// Show alert when location permission is denied
    private func showLocationPermissionDeniedAlert() {
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let window = windowScene.windows.first,
                  let rootViewController = window.rootViewController else {
                return
            }
            
            let alert = UIAlertController(
                title: "Location Permission Required",
                message: "Red Screen Filter needs location access to calculate sunrise and sunset times for automatic scheduling. You can enable this in Settings > Privacy > Location Services > Red Screen Filter.",
                preferredStyle: .alert
            )
            
            alert.addAction(UIAlertAction(title: "Open Settings", style: .default) { _ in
                if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(settingsURL)
                }
            })
            
            alert.addAction(UIAlertAction(title: "Not Now", style: .cancel))
            
            rootViewController.present(alert, animated: true)
        }
    }
    
    /// Show alert when notification permission is denied
    private func showNotificationPermissionDeniedAlert() {
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let window = windowScene.windows.first,
                  let rootViewController = window.rootViewController else {
                return
            }
            
            let alert = UIAlertController(
                title: "Notification Permission Required",
                message: "Red Screen Filter needs notification access to send eye strain reminders and schedule alerts. You can enable this in Settings > Notifications > Red Screen Filter.",
                preferredStyle: .alert
            )
            
            alert.addAction(UIAlertAction(title: "Open Settings", style: .default) { _ in
                if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(settingsURL)
                }
            })
            
            alert.addAction(UIAlertAction(title: "Not Now", style: .cancel))
            
            rootViewController.present(alert, animated: true)
        }
    }
    
    // MARK: - Permission Status Strings
    
    /// Get user-friendly location permission status description
    func getLocationPermissionDescription() -> String {
        switch locationPermissionStatus {
        case .notDetermined:
            return "Not requested"
        case .authorizedWhenInUse, .authorizedAlways:
            return "Authorized"
        case .denied:
            return "Denied"
        case .restricted:
            return "Restricted"
        @unknown default:
            return "Unknown"
        }
    }
    
    /// Get user-friendly notification permission status description
    func getNotificationPermissionDescription() -> String {
        switch notificationPermissionStatus {
        case .notDetermined:
            return "Not requested"
        case .authorized:
            return "Authorized"
        case .denied:
            return "Denied"
        case .provisional:
            return "Provisional"
        case .ephemeral:
            return "Temporary"
        @unknown default:
            return "Unknown"
        }
    }
}

// MARK: - CLLocationManagerDelegate

extension PermissionManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        checkLocationPermission()
        
        // Call completion handler if waiting for permission
        if let completion = locationPermissionCompletion {
            completion(hasLocationPermission)
            locationPermissionCompletion = nil
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        AppLogger.permissions.error("Location manager error", error: error)
    }
}

// MARK: - Permission Status Extensions

extension CLAuthorizationStatus {
    var description: String {
        switch self {
        case .notDetermined: return "Not Determined"
        case .restricted: return "Restricted"
        case .denied: return "Denied"
        case .authorizedAlways: return "Authorized Always"
        case .authorizedWhenInUse: return "Authorized When In Use"
        @unknown default: return "Unknown"
        }
    }
}

extension UNAuthorizationStatus {
    var description: String {
        switch self {
        case .notDetermined: return "Not Determined"
        case .denied: return "Denied"
        case .authorized: return "Authorized"
        case .provisional: return "Provisional"
        case .ephemeral: return "Ephemeral"
        @unknown default: return "Unknown"
        }
    }
}
