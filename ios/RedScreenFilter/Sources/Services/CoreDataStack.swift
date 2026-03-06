//
//  CoreDataStack.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import CoreData
import OSLog

/// CoreDataStack - Manages Core Data container and persistence
/// Uses App Groups for shared data access between app and widget
class CoreDataStack {
    // MARK: - Singleton
    
    static let shared = CoreDataStack()
    
    // MARK: - Properties
    
    /// The persistent container for Core Data
    lazy var persistentContainer: NSPersistentContainer = {
        let container = NSPersistentContainer(name: "RedScreenFilter")
        
        // Use App Groups for shared storage between app and widget
        if let appGroupURL = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: "group.com.redscreenfilter") {
            let storeURL = appGroupURL.appendingPathComponent("RedScreenFilter.sqlite")
            let storeDescription = NSPersistentStoreDescription(url: storeURL)
            storeDescription.shouldInferMappingModelAutomatically = true
            storeDescription.shouldMigrateStoreAutomatically = true
            container.persistentStoreDescriptions = [storeDescription]
        }
        
        container.loadPersistentStores { (storeDescription, error) in
            if let error = error as NSError? {
                // In production, handle this error appropriately
                AppLogger.coreData.error("Failed to load persistent store", error: error)
            } else {
                AppLogger.coreData.success("Core Data loaded from: \(storeDescription.url?.path ?? "unknown")")
            }
        }
        
        // Enable automatic merging of changes
        container.viewContext.automaticallyMergesChangesFromParent = true
        container.viewContext.mergePolicy = NSMergeByPropertyObjectTrumpMergePolicy
        
        return container
    }()
    
    /// Main context for UI operations
    var viewContext: NSManagedObjectContext {
        return persistentContainer.viewContext
    }
    
    /// Background context for async operations
    var backgroundContext: NSManagedObjectContext {
        return persistentContainer.newBackgroundContext()
    }
    
    // MARK: - Initialization
    
    private init() {}
    
    // MARK: - Save Operations
    
    /// Save the view context with error handling
    /// - Returns: Success or failure
    @discardableResult
    func saveContext() -> Bool {
        let context = viewContext
        
        guard context.hasChanges else {
            return true // Nothing to save
        }
        
        do {
            try context.save()
            AppLogger.coreData.debug("Main context saved successfully")
            return true
        } catch {
            let nsError = error as NSError
            AppLogger.coreData.error("Failed to save main context", error: nsError)
            return false
        }
    }
    
    /// Save a background context
    /// - Parameter context: The background context to save
    /// - Returns: Success or failure
    @discardableResult
    func saveBackgroundContext(_ context: NSManagedObjectContext) -> Bool {
        guard context.hasChanges else {
            return true
        }
        
        do {
            try context.save()
            AppLogger.coreData.debug("Background context saved successfully")
            return true
        } catch {
            let nsError = error as NSError
            AppLogger.coreData.error("Failed to save background context", error: nsError)
            return false
        }
    }
    
    // MARK: - Fetch Operations
    
    /// Fetch all usage events
    /// - Returns: Array of usage events
    func fetchAllUsageEvents() -> [UsageEvent] {
        let fetchRequest = UsageEvent.fetchRequest()
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "timestamp", ascending: false)]
        
        do {
            return try viewContext.fetch(fetchRequest)
        } catch {
            AppLogger.coreData.error("Failed to fetch usage events", error: error)
            return []
        }
    }
    
    /// Fetch usage events for a specific date range
    /// - Parameters:
    ///   - startDate: Start date of range
    ///   - endDate: End date of range
    /// - Returns: Array of usage events in range
    func fetchUsageEvents(from startDate: Date, to endDate: Date) -> [UsageEvent] {
        let fetchRequest = UsageEvent.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "timestamp >= %@ AND timestamp <= %@", startDate as NSDate, endDate as NSDate)
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "timestamp", ascending: true)]
        
        do {
            return try viewContext.fetch(fetchRequest)
        } catch {
            AppLogger.coreData.error("Failed to fetch usage events for date range", error: error)
            return []
        }
    }
    
    /// Fetch daily stats for a specific date
    /// - Parameter date: The date to fetch stats for
    /// - Returns: DailyStats object or nil
    func fetchDailyStats(for date: Date) -> DailyStats? {
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        let endOfDay = calendar.date(byAdding: .day, value: 1, to: startOfDay)!
        
        let fetchRequest = DailyStats.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "date >= %@ AND date < %@", startOfDay as NSDate, endOfDay as NSDate)
        fetchRequest.fetchLimit = 1
        
        do {
            return try viewContext.fetch(fetchRequest).first
        } catch {
            AppLogger.coreData.error("Failed to fetch daily stats for date", error: error)
            return nil
        }
    }
    
    /// Fetch all daily stats, sorted by date descending
    /// - Returns: Array of daily stats
    func fetchAllDailyStats() -> [DailyStats] {
        let fetchRequest = DailyStats.fetchRequest()
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "date", ascending: false)]
        
        do {
            return try viewContext.fetch(fetchRequest)
        } catch {
            AppLogger.coreData.error("Failed to fetch daily stats", error: error)
            return []
        }
    }
    
    // MARK: - Delete Operations
    
    /// Delete all usage events (useful for testing or reset)
    func deleteAllUsageEvents() {
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = UsageEvent.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        
        do {
            try viewContext.execute(deleteRequest)
            try viewContext.save()
            AppLogger.coreData.success("All usage events deleted")
        } catch {
            AppLogger.coreData.error("Failed to delete usage events", error: error)
        }
    }
    
    /// Delete all daily stats (useful for testing or reset)
    func deleteAllDailyStats() {
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = DailyStats.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        
        do {
            try viewContext.execute(deleteRequest)
            try viewContext.save()
            AppLogger.coreData.success("All daily stats deleted")
        } catch {
            AppLogger.coreData.error("Failed to delete daily stats", error: error)
        }
    }
}
