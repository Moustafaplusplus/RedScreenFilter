# iOS - Red Screen Filter

Swift-based native iOS application for applying a customizable red-screen overlay.

## Objective

Create a lightweight, persistent red-tint overlay that:
- Runs persistently across app backgrounding using App Groups and background modes
- Provides native SwiftUI settings UI
- Implements day/night scheduling with local notifications
- Integrates with iOS accessibility patterns
- Minimizes battery impact with efficient rendering

## Architecture

```
RedScreenFilter/
├── Sources/
│   ├── App/
│   │   ├── RedScreenFilterApp.swift         # App entry point
│   │   └── AppDelegate.swift                # Lifecycle management
│   ├── Views/
│   │   ├── MainView.swift                   # Home screen
│   │   ├── SettingsView.swift               # Settings UI
│   │   └── OverlayControlView.swift         # Toggle and opacity control
│   ├── Services/
│   │   ├── OverlayWindowManager.swift       # Overlay window management
│   │   ├── SchedulingService.swift          # Day/night scheduling
│   │   ├── PreferencesManager.swift         # UserDefaults wrapper
│   │   └── BackgroundTaskManager.swift      # Background execution
│   ├── Models/
│   │   └── OverlaySettings.swift            # Data models
│   └── Utilities/
│       ├── Constants.swift
│       └── Extensions.swift
├── Resources/
│   └── Assets.xcassets
└── RedScreenFilter.xcodeproj
```

## Key Components

### OverlayWindowManager
- Creates an overlay `UIWindow` with a red-tinted view
- Manages opacity and color adjustments
- Handles touch passthrough for underlying app interaction
- Persists with App Groups data sharing

### SchedulingService
- Determines if overlay should be active based on time/light sensor
- Uses `UNUserNotificationCenter` for scheduling transitions
- Background refresh with `BGProcessingTaskRequest`
- Syncs state across app instances via App Groups

### SettingsView (SwiftUI)
- Toggle on/off
- Opacity slider (0-100%)
- Schedule configuration (start/end time)
- Manual override controls

### PreferencesManager
- Wraps `UserDefaults` with App Groups container
- Persists across app termination
- Syncs between app and background processes

## Capabilities & Entitlements

```xml
<!-- Info.plist additions -->
- Background Modes: Background Fetch, Background Processing
- App Groups: group.com.redscreenfilter
- Local Notifications permission
- Capability: Background Modes (fetch, processing)
```

## Minimum Requirements

- **iOS**: 14.0+
- **Language**: Swift 5.9+
- **UI Framework**: SwiftUI
- **Architecture**: MVVM

## Dependencies

- SwiftUI (native)
- Combine (reactive state)
- UserNotifications (scheduling)
- BackgroundTasks (background execution)
- WidgetKit (optional, for lock screen widget)

## Build & Run

```bash
# From project root
cd ios/RedScreenFilter

# Build
xcodebuild build

# Run on simulator
xcodebuild -scheme RedScreenFilter -destination 'platform=iOS Simulator,name=iPhone 15'

# Run tests
xcodebuild test
```

## Development Notes

- **Window Layering**: Overlay window uses `UIWindowLevelStatusBar + 1` to display above most content
- **Touch Passthrough**: Set overlay view's `isUserInteractionEnabled = false` to prevent blocking interactions
- **Battery**: Use `updateFrequency` controls in background tasks to minimize wake cycles
- **Persistence**: App Groups ensures state survives app termination
- **Color**: Use `UIColor(red: 1.0, green: 0, blue: 0, alpha: opacity)` for pure red tint

## Optional: Native iOS Red Tint Integration

The app can optionally integrate with iOS's native Color Filter accessibility feature:
- Provide quick toggle to enable/disable via Shortcuts automation
- User prefs stored separately from custom overlay
- Lower battery impact if system feature is preferred

## Testing

- Unit tests for `SchedulingService` and `PreferencesManager`
- UI tests for settings interactions
- Manual testing on device for background persistence
- Battery/performance profiling with Instruments
