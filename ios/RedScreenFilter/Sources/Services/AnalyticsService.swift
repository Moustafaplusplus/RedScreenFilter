//
//  AnalyticsService.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import CoreData

/// Lightweight analytics logger used by OverlayViewModel.
final class AnalyticsService {
    static let shared = AnalyticsService()

    private var lastOpacityLogDate: Date?
    private let minimumOpacityLogInterval: TimeInterval = 0.5
    private let coreDataStack = CoreDataStack.shared

    private init() {}

    func logOverlayToggled(isEnabled: Bool, opacity: Float, preset: String) {
        persistEvent(overlayEnabled: isEnabled, opacity: opacity, preset: preset)
    }

    func logOpacityChanged(opacity: Float, preset: String) {
        let now = Date()
        if let lastOpacityLogDate, now.timeIntervalSince(lastOpacityLogDate) < minimumOpacityLogInterval {
            return
        }

        lastOpacityLogDate = now
        persistEvent(
            overlayEnabled: PreferencesManager.shared.isOverlayEnabled(),
            opacity: opacity,
            preset: preset
        )
    }

    func logPresetApplied(preset: String, opacity: Float) {
        persistEvent(
            overlayEnabled: PreferencesManager.shared.isOverlayEnabled(),
            opacity: opacity,
            preset: preset
        )
    }

    func fetchEvents(from startDate: Date) -> [UsageEvent] {
        let context = coreDataStack.persistentContainer.viewContext
        let request: NSFetchRequest<UsageEvent> = UsageEvent.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(key: "timestamp", ascending: true)]
        request.predicate = NSPredicate(format: "timestamp >= %@", startDate as NSDate)

        do {
            return try context.fetch(request)
        } catch {
            AppLogger.lifecycle.error("Failed to fetch analytics events", error: error)
            return []
        }
    }

    private func persistEvent(overlayEnabled: Bool, opacity: Float, preset: String) {
        let context = coreDataStack.persistentContainer.viewContext
        context.perform {
            _ = UsageEvent(
                context: context,
                overlayEnabled: overlayEnabled,
                opacity: opacity,
                preset: preset
            )
            self.coreDataStack.saveContext()
        }
    }
}
