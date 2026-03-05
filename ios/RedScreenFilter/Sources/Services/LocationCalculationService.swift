//
//  LocationCalculationService.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Phase 40-50% - Sunrise/Sunset Scheduling
//

import Foundation
import CoreLocation
import Combine
import os.log

/// LocationCalculationService - Manages location-based sunrise/sunset calculations
/// Fetches user's location using CoreLocation
/// Caches location and times to minimize battery usage
/// Thread-safe with published properties for reactive updates
class LocationCalculationService: NSObject, ObservableObject {
    
    // MARK: - Singleton
    
    static let shared = LocationCalculationService()
    
    // MARK: - Published Properties
    
    @Published var sunriseTime: Date?
    @Published var sunsetTime: Date?
    @Published var lastLocation: CLLocation?
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published var isCalculating: Bool = false
    @Published var error: LocationError?
    
    // MARK: - Private Properties
    
    private let locationManager = CLLocationManager()
    private let logger = OSLog(subsystem: "com.redscreenfilter", category: "Location")
    private let cacheExpirationInterval: TimeInterval = 6 * 60 * 60 // 6 hours
    private var lastCalculationDate: Date?
    private let queue = DispatchQueue(label: "com.redscreenfilter.location", attributes: .concurrent)
    
    // UserDefaults keys for caching
    private let cachedLatitudeKey = "cachedLatitude"
    private let cachedLongitudeKey = "cachedLongitude"
    private let cachedSunriseKey = "cachedSunrise"
    private let cachedSunsetKey = "cachedSunset"
    private let lastCalculationKey = "lastLocationCalculation"
    
    // MARK: - Initialization
    
    private override init() {
        super.init()
        setupLocationManager()
        loadCachedData()
    }
    
    // MARK: - Location Manager Setup
    
    private func setupLocationManager() {
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyKilometer // Low accuracy is sufficient
        locationManager.distanceFilter = 10000 // Only update if moved 10km
        
        authorizationStatus = locationManager.authorizationStatus
    }
    
    // MARK: - Public Methods
    
    /// Requests location permission from the user
    /// Should be called before attempting to fetch location
    func requestLocationPermission() {
        os_log("Requesting location permission", log: logger, type: .info)
        locationManager.requestWhenInUseAuthorization()
    }
    
    /// Fetches current location and calculates sunrise/sunset times
    /// Uses cached data if still valid (less than 6 hours old)
    /// - Parameter forceRefresh: If true, ignores cache and fetches fresh data
    func fetchSunriseSunsetTimes(forceRefresh: Bool = false) {
        queue.async(flags: .barrier) { [weak self] in
            guard let self = self else { return }
            
            // Check authorization
            guard self.locationManager.authorizationStatus == .authorizedWhenInUse ||
                  self.locationManager.authorizationStatus == .authorizedAlways else {
                DispatchQueue.main.async {
                    self.error = .permissionDenied
                    os_log("Location permission not granted", log: self.logger, type: .error)
                }
                return
            }
            
            // Check if cache is still valid
            if !forceRefresh, self.isCacheValid() {
                os_log("Using cached sunrise/sunset times", log: self.logger, type: .info)
                return
            }
            
            DispatchQueue.main.async {
                self.isCalculating = true
                self.error = nil
            }
            
            // Request location update
            self.locationManager.requestLocation()
        }
    }
    
    /// Returns sunrise and sunset times with optional offset
    /// - Parameter offsetMinutes: Minutes to offset from actual times
    /// - Returns: Tuple of (sunrise, sunset) or nil if not available
    func getSunriseSunsetWithOffset(offsetMinutes: Int = 0) -> (sunrise: Date, sunset: Date)? {
        guard let sunrise = sunriseTime,
              let sunset = sunsetTime else {
            return nil
        }
        
        let adjustedSunrise = SunriseCalculator.applyOffset(sunrise, offsetMinutes: offsetMinutes)
        let adjustedSunset = SunriseCalculator.applyOffset(sunset, offsetMinutes: offsetMinutes)
        
        return (adjustedSunrise, adjustedSunset)
    }
    
    /// Formats sunrise and sunset times for display
    /// - Returns: Dictionary with formatted times
    func getFormattedTimes() -> [String: String] {
        var result: [String: String] = [:]
        
        if let sunrise = sunriseTime {
            result["sunrise"] = SunriseCalculator.formatTime(sunrise)
        }
        
        if let sunset = sunsetTime {
            result["sunset"] = SunriseCalculator.formatTime(sunset)
        }
        
        return result
    }
    
    // MARK: - Private Methods
    
    /// Calculates sunrise/sunset for a given location
    private func calculateTimes(for location: CLLocation) {
        let latitude = location.coordinate.latitude
        let longitude = location.coordinate.longitude
        
        os_log("Calculating sunrise/sunset for lat: %f, lon: %f",
               log: logger,
               type: .info,
               latitude,
               longitude)
        
        let (sunrise, sunset) = SunriseCalculator.calculateSunriseSunset(
            latitude: latitude,
            longitude: longitude
        )
        
        // Update published properties on main thread
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            self.sunriseTime = sunrise
            self.sunsetTime = sunset
            self.lastLocation = location
            self.lastCalculationDate = Date()
            self.isCalculating = false
            
            // Cache the results
            self.cacheData(location: location, sunrise: sunrise, sunset: sunset)
            
            os_log("Sunrise: %@, Sunset: %@",
                   log: self.logger,
                   type: .info,
                   SunriseCalculator.formatTime(sunrise),
                   SunriseCalculator.formatTime(sunset))
        }
    }
    
    /// Checks if cached data is still valid
    private func isCacheValid() -> Bool {
        guard let lastCalc = lastCalculationDate else {
            return false
        }
        
        let timeElapsed = Date().timeIntervalSince(lastCalc)
        return timeElapsed < cacheExpirationInterval && sunriseTime != nil && sunsetTime != nil
    }
    
    /// Caches location and calculation results
    private func cacheData(location: CLLocation, sunrise: Date, sunset: Date) {
        let defaults = UserDefaults.standard
        defaults.set(location.coordinate.latitude, forKey: cachedLatitudeKey)
        defaults.set(location.coordinate.longitude, forKey: cachedLongitudeKey)
        defaults.set(sunrise, forKey: cachedSunriseKey)
        defaults.set(sunset, forKey: cachedSunsetKey)
        defaults.set(Date(), forKey: lastCalculationKey)
        
        os_log("Cached location and times", log: logger, type: .debug)
    }
    
    /// Loads cached data on initialization
    private func loadCachedData() {
        let defaults = UserDefaults.standard
        
        guard let cachedSunrise = defaults.object(forKey: cachedSunriseKey) as? Date,
              let cachedSunset = defaults.object(forKey: cachedSunsetKey) as? Date,
              let lastCalc = defaults.object(forKey: lastCalculationKey) as? Date else {
            return
        }
        
        let latitude = defaults.double(forKey: cachedLatitudeKey)
        let longitude = defaults.double(forKey: cachedLongitudeKey)
        
        sunriseTime = cachedSunrise
        sunsetTime = cachedSunset
        lastCalculationDate = lastCalc
        lastLocation = CLLocation(latitude: latitude, longitude: longitude)
        
        os_log("Loaded cached location data", log: logger, type: .info)
    }
}

// MARK: - CLLocationManagerDelegate

extension LocationCalculationService: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
        os_log("Location updated: %f, %f",
               log: logger,
               type: .info,
               location.coordinate.latitude,
               location.coordinate.longitude)
        
        calculateTimes(for: location)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        os_log("Location fetch failed: %{public}@",
               log: logger,
               type: .error,
               error.localizedDescription)
        
        DispatchQueue.main.async { [weak self] in
            self?.isCalculating = false
            self?.error = .fetchFailed(error.localizedDescription)
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        
        os_log("Location authorization changed: %d", log: logger, type: .info, status.rawValue)
        
        DispatchQueue.main.async { [weak self] in
            self?.authorizationStatus = status
        }
        
        // Automatically fetch if authorized
        if status == .authorizedWhenInUse || status == .authorizedAlways {
            fetchSunriseSunsetTimes()
        }
    }
}

// MARK: - Error Types

enum LocationError: Error, LocalizedError {
    case permissionDenied
    case fetchFailed(String)
    case calculationFailed
    
    var errorDescription: String? {
        switch self {
        case .permissionDenied:
            return "Location permission is required to calculate sunrise and sunset times"
        case .fetchFailed(let message):
            return "Failed to fetch location: \(message)"
        case .calculationFailed:
            return "Failed to calculate sunrise/sunset times"
        }
    }
}
