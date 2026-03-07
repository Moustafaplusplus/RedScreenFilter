import Foundation

struct WidgetSharedState {
    static let appGroupId = "group.com.redscreenfilter"

    private enum Keys {
        static let overlayEnabled = "overlayEnabled"
        static let opacity = "opacity"
        static let scheduleEnabled = "scheduleEnabled"
        static let scheduleStartTime = "scheduleStartTime"
        static let scheduleEndTime = "scheduleEndTime"
        static let currentPreset = "currentPreset"
        static let widgetPendingAction = "widgetPendingAction"
        static let widgetPendingPreset = "widgetPendingPreset"
        static let widgetActionTimestamp = "widgetActionTimestamp"
    }

    let overlayEnabled: Bool
    let opacity: Float
    let scheduleEnabled: Bool
    let scheduleStartTime: String
    let scheduleEndTime: String
    let currentPreset: String

    init(defaults: UserDefaults? = UserDefaults(suiteName: WidgetSharedState.appGroupId)) {
        let store = defaults
        overlayEnabled = store?.bool(forKey: Keys.overlayEnabled) ?? false

        if let store, store.object(forKey: Keys.opacity) != nil {
            opacity = store.float(forKey: Keys.opacity)
        } else {
            opacity = 0.5
        }

        scheduleEnabled = store?.bool(forKey: Keys.scheduleEnabled) ?? false
        scheduleStartTime = store?.string(forKey: Keys.scheduleStartTime) ?? "21:00"
        scheduleEndTime = store?.string(forKey: Keys.scheduleEndTime) ?? "07:00"
        currentPreset = store?.string(forKey: Keys.currentPreset) ?? "Standard"
    }

    static func toggleOverlay() {
        guard let defaults = UserDefaults(suiteName: appGroupId) else { return }
        let next = !(defaults.bool(forKey: Keys.overlayEnabled))
        defaults.set(next, forKey: Keys.overlayEnabled)
        defaults.set("toggle", forKey: Keys.widgetPendingAction)
        defaults.set(Date().timeIntervalSince1970, forKey: Keys.widgetActionTimestamp)
    }

    static func applyPreset(named preset: String) {
        guard let defaults = UserDefaults(suiteName: appGroupId) else { return }
        defaults.set(preset, forKey: Keys.currentPreset)
        defaults.set(preset, forKey: Keys.widgetPendingPreset)
        defaults.set("preset", forKey: Keys.widgetPendingAction)
        defaults.set(Date().timeIntervalSince1970, forKey: Keys.widgetActionTimestamp)
    }

    static func markOpenRequest() {
        guard let defaults = UserDefaults(suiteName: appGroupId) else { return }
        defaults.set("open", forKey: Keys.widgetPendingAction)
        defaults.set(Date().timeIntervalSince1970, forKey: Keys.widgetActionTimestamp)
    }
}
