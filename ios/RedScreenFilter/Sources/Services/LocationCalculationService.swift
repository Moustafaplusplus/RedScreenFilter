//
//  LocationCalculationService.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import CoreLocation

final class LocationCalculationService: NSObject, ObservableObject {
    static let shared = LocationCalculationService()

    @Published private(set) var sunriseTime: Date?
    @Published private(set) var sunsetTime: Date?

    private let locationManager = CLLocationManager()
    private var lastLocation: CLLocation?

    private override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    func requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    func fetchSunriseSunsetTimes(forceRefresh: Bool = false) {
        if forceRefresh || lastLocation == nil {
            locationManager.requestLocation()
        }

        let calculatedTimes = calculateTimes(for: lastLocation)
        sunriseTime = calculatedTimes.sunrise
        sunsetTime = calculatedTimes.sunset
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
        lastLocation = locations.last
        let calculatedTimes = calculateTimes(for: lastLocation)
        sunriseTime = calculatedTimes.sunrise
        sunsetTime = calculatedTimes.sunset
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        let calculatedTimes = calculateTimes(for: nil)
        sunriseTime = calculatedTimes.sunrise
        sunsetTime = calculatedTimes.sunset
    }
}
