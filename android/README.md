# Android - Red Screen Filter

Kotlin-based native Android application for applying a customizable red-screen overlay.

## Objective

Create a lightweight, persistent red-tint overlay service that:
- Runs as a background service for continuous operation
- Provides a settings UI for configuration
- Implements day/night scheduling
- Minimizes battery and performance impact
- Respects system doze mode and app standby states

## Architecture

```
app/src/main/
├── java/com/redscreenfilter/
│   ├── MainActivity.kt              # Main settings screen
│   ├── service/
│   │   ├── RedOverlayService.kt     # Background overlay service
│   │   └── SchedulingManager.kt     # Day/night scheduling logic
│   ├── ui/
│   │   ├── OverlayFragment.kt       # Overlay configuration UI
│   │   ├── SettingsFragment.kt      # App settings
│   │   └── components/              # Reusable UI components
│   ├── data/
│   │   ├── PreferencesManager.kt    # SharedPreferences wrapper
│   │   └── models/
│   │       └── OverlaySettings.kt   # Data models
│   ├── utils/
│   │   ├── PermissionHelper.kt      # Runtime permissions
│   │   └── Constants.kt
│   └── receiver/
│       └── BootCompletedReceiver.kt # Auto-start on device boot
├── res/
│   ├── layout/                      # XML layouts
│   ├── values/                      # Strings, colors, dimens
│   └── drawable/                    # Icons and drawables
└── AndroidManifest.xml              # App manifest
```

## Key Components

### RedOverlayService
- Uses `WindowManager` to create system overlay
- Handles overlay updates and removal
- Listens to settings changes via `SharedPreferences`
- Implements lifecycle awareness

### SchedulingManager
- Determines if overlay should be active (time/sensor based)
- Integrates with WorkManager for reliable scheduling
- Handles day/night transitions

### MainActivity  
- Settings UI using Material Design 3
- Opacity slider
- On/Off toggle
- Schedule configuration
- Permission management

## Permissions Required

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## Minimum Requirements

- **API Level**: 28 (Android 9.0)
- **Target API**: 34+ (Android 14+)
- **Language**: Kotlin
- **Build System**: Gradle

## Dependencies

- AndroidX libraries (core, appcompat)
- Material Components
- WorkManager (for scheduling)
- Jetpack Compose or XML layouts (TBD)

## Build & Run

```bash
# From project root
cd android

# Build debug APK
./gradlew assembleDebug

# Run on emulator
./gradlew installDebugAndroidTest

# Run tests
./gradlew test
```

## Development Notes

- **Notification**: Service runs with a persistent notification (Android 12+)
- **DozeMode**: WorkManager handles scheduling across doze states
- **Memory**: Overlay is lightweight canvas-based rendering, not resource-intensive
- **Battery**: Service is disabled when app backgrounded (unless scheduled active)

## Testing

- Unit tests for `SchedulingManager` and `PreferencesManager`
- Integration tests for overlay service
- Manual testing on devices with different Android versions
