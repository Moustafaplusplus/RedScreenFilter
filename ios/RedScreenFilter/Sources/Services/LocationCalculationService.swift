//
//  LocationCalculationService.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Enhanced in Phase 98-99% with permission handling
//

import Foundation
import CoreLocation
import OSLog

final class LocationCalculationService: NSObject, ObservableObject {
    static let shared = LocationCalculationService()

    @Published private(set) var sunriseTime: Date?
    @Published private(set) var sunsetTime: Date?
    @Published private(set) var isLocationAvailable: Bool = false

    private let locationManager = CLLocationManager()
    private let permissionManager = PermissionManager.shared
    private var lastLocation: CLLocation?
    private var locationRequestCompletion: ((Bool) -> Void)?

    private override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
        
        // Calculate default times even without location
        let defaultTimes = calculateTimes(for: nil)
        sunriseTime = defaultTimes.sunrise
        sunsetTime = defaultTimes.sunset
    }

    /// Request location permission with completion handler
    /// - Parameter completion: Called with success/failure
    func requestLocationPermission(completion: ((Bool) -> Void)? = nil) {
        permissionManager.requestLocationPermission { [weak self] granted in
            if granted {
                self?.fetchSunriseSunsetTimes()
            }
            completion?(granted)
        }
    }

    /// Fetch sunrise/sunset times - checks permission first
    /// - Parameter forceRefresh: Force a new location request
    func fetchSunriseSunsetTimes(forceRefresh: Bool = false) {
        // Check permission status first
        guard permissionManager.hasLocationPermission else {
            AppLogger.location.warning("Location permission not granted - using approximate times")
            updateTimesWithoutLocation()
            return
        }
        
        if forceRefresh || lastLocation == nil {
            locationManager.requestLocation()
        } else {
            // Use cached location
            let calculatedTimes = calculateTimes(for: lastLocation)
            sunriseTime = calculatedTimes.sunrise
            sunsetTime = calculatedTimes.sunset
            isLocationAvailable = true
        }
    }
    
    /// Update times without location (uses approximate times)
    private func updateTimesWithoutLocation() {
        let calculatedTimes = calculateTimes(for: nil)
        sunriseTime = calculatedTimes.sunrise
        sunsetTime = calculatedTimes.sunset
        isLocationAvailable = false
    }

    func getSunriseSunsetWithOffset(offsetMinutes: Int) -> (sunrise: Date, sunset: Date)? {
        guard let sunriseTime, let sunsetTime else {
            return nil
        }

        let offset = TimeInterval(offsetMinutes * 60)
        return (sunrise: sunriseTime.addingTimeInterval(offset), sunset: sunsetTime.addingTimeInterval(offset))
    }

    func getFormattedTimes() -> [String: String] {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        formatter.dateStyle = .none

        return [
            "sunrise": sunriseTime.map { formatter.string(from: $0) } ?? "--",
            "sunset": sunsetTime.map { formatter.string(from: $0) } ?? "--"
        ]
    }

    private func calculateTimes(for location: CLLocation?) -> (sunrise: Date, sunset: Date) {
        let today = Date()
        let calendar = Calendar.current

        var sunriseComponents = calendar.dateComponents([.year, .month, .day], from: today)
        var sunsetComponents = sunriseComponents

        let longitude = location?.coordinate.longitude ?? 0
        let hourShift = Int((longitude / 15.0).rounded())

        sunriseComponents.hour = max(4, min(9, 6 + hourShift))
        sunriseComponents.minute = 30

        sunsetComponents.hour = max(16, min(21, 18 + hourShift))
        sunsetComponents.minute = 30

        let sunrise = calendar.date(from: sunriseComponents) ?? today
        let sunset = calendar.date(from: sunsetComponents) ?? today

        return (sunrise: sunrise, sunset: sunset)
    }
}

extension LocationCalculationService: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
        lastLocation = location
        isLocationAvailable = true
        
        let calculatedTimes = calculateTimes(for: location)
        sunriseTime = calculatedTimes.sunrise
        sunsetTime = calculatedTimes.sunset
        
        AppLogger.location.debug("Location updated: \(location.coordinate.latitude), \(location.coordinate.longitude)")
        AppLogger.location.debug("Sunrise: \(getFormattedTimes()["sunrise"] ?? "N/A"), Sunset: \(getFormattedTimes()["sunset"] ?? "N/A")")
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        AppLogger.location.error("Location error", error: error)
        updateTimesWithoutLocation()
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        let hasPermission = (status == .authorizedAlways || status == .authorizedWhenInUse)

        // Keep PermissionManager in sync for callers that read its cached state.
        permissionManager.checkPermissions()
        isLocationAvailable = hasPermission

        if hasPermission {
            fetchSunriseSunsetTimes()
        }
    }
}

