//
//  DailyStats.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import CoreData

/// Core Data entity representing daily usage statistics
@objc(DailyStats)
public class DailyStats: NSManagedObject {
    @NSManaged public var date: Date
    @NSManaged public var totalUsage: TimeInterval
    @NSManaged public var preset: String
}

extension DailyStats {
    @nonobjc public class func fetchRequest() -> NSFetchRequest<DailyStats> {
        return NSFetchRequest<DailyStats>(entityName: "DailyStats")
    }
    
    /// Convenience initializer for creating daily stats
    convenience init(context: NSManagedObjectContext, date: Date, totalUsage: TimeInterval, preset: String) {
        self.init(context: context)
        self.date = date
        self.totalUsage = totalUsage
        self.preset = preset
    }
}
