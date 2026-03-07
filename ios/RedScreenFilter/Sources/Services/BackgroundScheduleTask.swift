//
//  BackgroundScheduleTask.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation
import BackgroundTasks
import os.log

enum BackgroundScheduleTask {
    static let taskIdentifier = Constants.BackgroundTasks.scheduleUpdateId

    private static let logger = OSLog(subsystem: "com.redscreenfilter", category: "BackgroundTask")

    static func registerBackgroundTask() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskIdentifier, using: nil) { task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }

            handleAppRefresh(task: refreshTask)
        }
    }

    static func scheduleBackgroundTask() {
        let request = BGAppRefreshTaskRequest(identifier: taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)

        do {
            BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: taskIdentifier)
            try BGTaskScheduler.shared.submit(request)
            os_log("Scheduled app refresh task", log: logger, type: .info)
        } catch {
            os_log(
                "Failed to schedule app refresh task: %{public}@",
                log: logger,
                type: .error,
                String(describing: error)
            )
        }
    }

    private static func handleAppRefresh(task: BGAppRefreshTask) {
        scheduleBackgroundTask()

        let operation = BlockOperation {
            let shouldEnable = SchedulingService.shared.determineOverlayState()

            if let appGroupDefaults = UserDefaults(suiteName: Constants.AppGroup.identifier) {
                appGroupDefaults.set(shouldEnable, forKey: "overlayEnabled")
                appGroupDefaults.set(Date(), forKey: "lastScheduleUpdate")
            }

            PreferencesManager.shared.setOverlayEnabled(shouldEnable)
        }

        task.expirationHandler = {
            operation.cancel()
        }

        operation.completionBlock = {
            task.setTaskCompleted(success: !operation.isCancelled)
        }

        OperationQueue().addOperation(operation)
    }
}
