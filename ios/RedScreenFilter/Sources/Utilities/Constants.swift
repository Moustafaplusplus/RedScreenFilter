//
//  Constants.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

struct Constants {
    struct AppGroup {
        static let identifier = "group.com.redscreenfilter"
    }
    
    struct BackgroundTasks {
        static let scheduleUpdateId = "com.redscreenfilter.schedule-update"
    }
    
    struct Colors {
        static let redStandard = "red_standard"
        static let redOrange = "red_orange"
        static let redPink = "red_pink"
        static let highContrast = "high_contrast"
    }
    
    struct Presets {
        static let work = "work"
        static let gaming = "gaming"
        static let movie = "movie"
        static let sleep = "sleep"
        static let custom = "custom"
    }
    
    struct Battery {
        static let optimizationThreshold: Float = 0.2  // 20%
        static let criticalThreshold: Float = 0.1      // 10%
    }

    struct SiriShortcuts {
        static let enableSleepName = "Enable Red Filter Sleep Mode"
        static let disableFilterName = "Disable Color Filter"
        static let enableWorkName = "Enable Color Filter - Work Mode"

        // Replace with actual published iCloud shortcut links when available.
        static let enableSleepShareURL = "https://www.icloud.com/shortcuts/REPLACE_WITH_SLEEP_SHORTCUT_ID"
        static let disableFilterShareURL = "https://www.icloud.com/shortcuts/REPLACE_WITH_DISABLE_SHORTCUT_ID"
        static let enableWorkShareURL = "https://www.icloud.com/shortcuts/REPLACE_WITH_WORK_SHORTCUT_ID"
    }

    struct VoiceCommands {
        // Example Siri voice commands (requires iOS 16+)
        // Usage: "Hey Siri, set opacity to 50 in Red Screen Filter"
        // Usage: "Hey Siri, apply sleep preset in Red Screen Filter"
        // Usage: "Hey Siri, toggle red filter in Red Screen Filter"
        // Usage: "Hey Siri, red filter status in Red Screen Filter"
        
        static let setOpacityExample = "Set red filter opacity to 50"
        static let applyPresetExample = "Apply sleep preset"
        static let toggleExample = "Toggle red filter"
        static let statusExample = "Red filter status"
    }
}
