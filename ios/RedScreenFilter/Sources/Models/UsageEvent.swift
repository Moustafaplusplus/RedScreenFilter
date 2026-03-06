//
//  UsageEvent.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Updated for Core Data on March 6, 2026.
//

import Foundation
import CoreData

/// Core Data entity representing a single usage event
@objc(UsageEvent)
public class UsageEvent: NSManagedObject {
    @NSManaged public var timestamp: Date
    @NSManaged public var overlayEnabled: Bool
    @NSManaged public var opacity: Float
    @NSManaged public var preset: String
}

extension UsageEvent {
    @nonobjc public class func fetchRequest() -> NSFetchRequest<UsageEvent> {
        return NSFetchRequest<UsageEvent>(entityName: "UsageEvent")
    }
    
    /// Convenience initializer for creating usage events
    convenience init(context: NSManagedObjectContext, overlayEnabled: Bool, opacity: Float, preset: String) {
        self.init(context: context)
        self.timestamp = Date()
        self.overlayEnabled = overlayEnabled
        self.opacity = opacity
        self.preset = preset
    }
}
