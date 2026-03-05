import Foundation
import UIKit

final class SiriShortcutsLauncher {
    static let shared = SiriShortcutsLauncher()

    enum Shortcut: CaseIterable {
        case enableSleep
        case disableFilter
        case enableWork

        var title: String {
            switch self {
            case .enableSleep:
                return "Enable Red Filter Sleep Mode"
            case .disableFilter:
                return "Disable Color Filter"
            case .enableWork:
                return "Enable Color Filter - Work Mode"
            }
        }

        var shortcutsName: String {
            switch self {
            case .enableSleep:
                return Constants.SiriShortcuts.enableSleepName
            case .disableFilter:
                return Constants.SiriShortcuts.disableFilterName
            case .enableWork:
                return Constants.SiriShortcuts.enableWorkName
            }
        }

        var shareURLString: String {
            switch self {
            case .enableSleep:
                return Constants.SiriShortcuts.enableSleepShareURL
            case .disableFilter:
                return Constants.SiriShortcuts.disableFilterShareURL
            case .enableWork:
                return Constants.SiriShortcuts.enableWorkShareURL
            }
        }
    }

    private init() {}

    @discardableResult
    func runShortcut(_ shortcut: Shortcut) -> Bool {
        guard let encodedName = shortcut.shortcutsName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "shortcuts://run-shortcut/?name=\(encodedName)") else {
            return false
        }

        return open(url)
    }

    @discardableResult
    func openShortcutsApp() -> Bool {
        guard let url = URL(string: "shortcuts://") else {
            return false
        }

        return open(url)
    }

    func shareURL(for shortcut: Shortcut) -> URL? {
        URL(string: shortcut.shareURLString)
    }

    @discardableResult
    private func open(_ url: URL) -> Bool {
        if UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
            return true
        }

        // Fallback to regular app settings when Shortcuts URL is unavailable.
        if let settingsURL = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(settingsURL) {
            UIApplication.shared.open(settingsURL)
            return true
        }

        return false
    }
}