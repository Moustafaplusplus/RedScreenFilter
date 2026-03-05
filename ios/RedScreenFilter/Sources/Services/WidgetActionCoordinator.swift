import Foundation
import WidgetKit

/// Handles actions initiated by the widget extension via shared App Group defaults.
final class WidgetActionCoordinator {
    static let shared = WidgetActionCoordinator()

    private let prefs = PreferencesManager.shared
    private let overlayManager = OverlayWindowManager.shared
    private let appGroupDefaults = UserDefaults(suiteName: Constants.AppGroup.identifier)

    private enum Keys {
        static let pendingAction = "widgetPendingAction"
        static let pendingPreset = "widgetPendingPreset"
    }

    private init() {}

    func handleIncomingURL(_ url: URL) {
        guard url.scheme == "redscreenfilter", url.host == "widget" else { return }

        let action = URLComponents(url: url, resolvingAgainstBaseURL: false)?
            .queryItems?
            .first(where: { $0.name == "action" })?
            .value

        if action == "toggle" {
            toggleOverlay()
        }

        processPendingAction()
    }

    func processPendingAction() {
        guard let action = appGroupDefaults?.string(forKey: Keys.pendingAction) else { return }

        switch action {
        case "toggle":
            toggleOverlay()
        case "preset":
            let preset = appGroupDefaults?.string(forKey: Keys.pendingPreset) ?? "Sleep"
            applyPreset(named: preset)
        default:
            break
        }

        appGroupDefaults?.removeObject(forKey: Keys.pendingAction)
        appGroupDefaults?.removeObject(forKey: Keys.pendingPreset)
        WidgetCenter.shared.reloadAllTimelines()
    }

    private func toggleOverlay() {
        let next = !prefs.isOverlayEnabled()
        prefs.setOverlayEnabled(next)

        if next {
            overlayManager.showOverlay(opacity: prefs.getOpacity())
        } else {
            overlayManager.hideOverlay()
        }
    }

    private func applyPreset(named preset: String) {
        prefs.setCurrentPreset(preset)

        switch preset.lowercased() {
        case "work":
            prefs.setOpacity(0.3)
            prefs.setColorVariant(Constants.Colors.redStandard)
        case "gaming":
            prefs.setOpacity(0.4)
            prefs.setColorVariant(Constants.Colors.redOrange)
        case "movie":
            prefs.setOpacity(0.5)
            prefs.setColorVariant(Constants.Colors.redStandard)
        default:
            prefs.setOpacity(0.7)
            prefs.setColorVariant(Constants.Colors.redPink)
        }

        if prefs.isOverlayEnabled() {
            overlayManager.showOverlay(opacity: prefs.getOpacity())
        }
    }
}