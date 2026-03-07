import Foundation
import UserNotifications

/// Schedules daily notifications that remind the user to run system-wide shortcut actions.
final class SystemWideShortcutReminderService {
    static let shared = SystemWideShortcutReminderService()

    private let notificationCenter = UNUserNotificationCenter.current()
    private let permissionManager = PermissionManager.shared

    private enum Identifiers {
        static let enableReminder = "com.redscreenfilter.systemwide.reminder.enable"
        static let disableReminder = "com.redscreenfilter.systemwide.reminder.disable"
    }

    private init() {}

    func syncWithSchedule(isEnabled: Bool, startTime: String, endTime: String) {
        guard isEnabled else {
            cancelAllReminders()
            return
        }

        guard permissionManager.hasNotificationPermission else {
            permissionManager.requestNotificationPermission { [weak self] granted in
                guard granted else {
                    AppLogger.notifications.warning("System-wide shortcut reminders not scheduled because notification permission was denied")
                    return
                }

                self?.scheduleDailyReminders(startTime: startTime, endTime: endTime)
            }
            return
        }

        scheduleDailyReminders(startTime: startTime, endTime: endTime)
    }

    func cancelAllReminders() {
        notificationCenter.removePendingNotificationRequests(withIdentifiers: [
            Identifiers.enableReminder,
            Identifiers.disableReminder
        ])
    }

    private func scheduleDailyReminders(startTime: String, endTime: String) {
        guard let enableComponents = timeComponents(from: startTime),
              let disableComponents = timeComponents(from: endTime) else {
            AppLogger.notifications.warning("Invalid schedule time format. Expected HH:mm for system-wide shortcut reminders")
            cancelAllReminders()
            return
        }

        cancelAllReminders()

        scheduleReminder(
            identifier: Identifiers.enableReminder,
            title: "Red Filter Reminder",
            body: "Time to enable your system-wide red filter shortcut.",
            dateComponents: enableComponents
        )

        scheduleReminder(
            identifier: Identifiers.disableReminder,
            title: "Red Filter Reminder",
            body: "Time to disable your system-wide red filter shortcut.",
            dateComponents: disableComponents
        )
    }

    private func scheduleReminder(identifier: String, title: String, body: String, dateComponents: DateComponents) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            if let error {
                AppLogger.notifications.error("Failed to schedule system-wide shortcut reminder", error: error)
            }
        }
    }

    private func timeComponents(from time: String) -> DateComponents? {
        let parts = time.split(separator: ":")
        guard parts.count == 2,
              let hour = Int(parts[0]),
              let minute = Int(parts[1]),
              (0...23).contains(hour),
              (0...59).contains(minute) else {
            return nil
        }

        return DateComponents(hour: hour, minute: minute)
    }
}
