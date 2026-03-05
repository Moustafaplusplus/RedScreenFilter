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
}
