//
//  PermissionManager.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import CoreLocation
import UserNotifications

final class PermissionManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    static let shared = PermissionManager()

    @Published private(set) var hasLocationPermission: Bool = false
    @Published private(set) var hasNotificationPermission: Bool = false

    private let locationManager = CLLocationManager()
    private var pendingLocationCompletion: ((Bool) -> Void)?

    private override init() {
        super.init()
        locationManager.delegate = self
        checkPermissions()
    }

    func checkPermissions() {
        let locationStatus = locationManager.authorizationStatus
        hasLocationPermission = (locationStatus == .authorizedAlways || locationStatus == .authorizedWhenInUse)

        UNUserNotificationCenter.current().getNotificationSettings { [weak self] settings in
            DispatchQueue.main.async {
                self?.hasNotificationPermission = settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
            }
        }
    }

    func requestLocationPermission(completion: @escaping (Bool) -> Void) {
        let status = locationManager.authorizationStatus

        switch status {
        case .authorizedAlways, .authorizedWhenInUse:
            hasLocationPermission = true
            completion(true)
        case .notDetermined:
            pendingLocationCompletion = completion
            locationManager.requestWhenInUseAuthorization()
        default:
            hasLocationPermission = false
            completion(false)
        }
    }

    func requestNotificationPermission(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { [weak self] granted, _ in
            DispatchQueue.main.async {
                self?.hasNotificationPermission = granted
                completion(granted)
            }
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        let granted = (status == .authorizedAlways || status == .authorizedWhenInUse)
        hasLocationPermission = granted

        if status != .notDetermined {
            pendingLocationCompletion?(granted)
            pendingLocationCompletion = nil
        }
    }
}
