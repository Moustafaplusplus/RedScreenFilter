//
//  CoreDataStack.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import CoreData

final class CoreDataStack {
    static let shared = CoreDataStack()

    let persistentContainer: NSPersistentContainer

    private init() {
        let model = Self.makeManagedObjectModel()
        persistentContainer = NSPersistentContainer(name: "RedScreenFilter", managedObjectModel: model)
        persistentContainer.loadPersistentStores { _, error in
            if let error {
                AppLogger.lifecycle.error("Failed to load Core Data persistent stores", error: error)
            }
        }
        persistentContainer.viewContext.automaticallyMergesChangesFromParent = true
    }

    func saveContext() {
        let context = persistentContainer.viewContext
        guard context.hasChanges else { return }

        do {
            try context.save()
        } catch {
            AppLogger.lifecycle.error("Failed to save Core Data context", error: error)
            context.rollback()
        }
    }

    private static func makeManagedObjectModel() -> NSManagedObjectModel {
        let model = NSManagedObjectModel()

        let usageEventEntity = NSEntityDescription()
        usageEventEntity.name = "UsageEvent"
        usageEventEntity.managedObjectClassName = NSStringFromClass(UsageEvent.self)

        let timestamp = NSAttributeDescription()
        timestamp.name = "timestamp"
        timestamp.attributeType = .dateAttributeType
        timestamp.isOptional = false

        let overlayEnabled = NSAttributeDescription()
        overlayEnabled.name = "overlayEnabled"
        overlayEnabled.attributeType = .booleanAttributeType
        overlayEnabled.isOptional = false

        let opacity = NSAttributeDescription()
        opacity.name = "opacity"
        opacity.attributeType = .floatAttributeType
        opacity.isOptional = false

        let preset = NSAttributeDescription()
        preset.name = "preset"
        preset.attributeType = .stringAttributeType
        preset.isOptional = false

        usageEventEntity.properties = [timestamp, overlayEnabled, opacity, preset]
        model.entities = [usageEventEntity]
        return model
    }
}
