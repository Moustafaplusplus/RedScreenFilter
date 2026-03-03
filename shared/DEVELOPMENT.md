# Development Guide

## Prerequisites

### For Android Development
- **Android Studio** 2024.1+
- **Java/Kotlin**: JDK 17+
- **Android SDK**: API 28 minimum, 34+ target
- **Gradle**: 8.0+

### For iOS Development
- **Xcode** 15.0+
- **Swift**: 5.9+
- **macOS**: 13.0+
- **CocoaPods** (if using dependencies)

## Environment Setup

### Android

1. **Clone/Open Project**
   ```bash
   cd android
   ```

2. **Sync Gradle**
   - Open in Android Studio
   - Allow Gradle sync automatically

3. **Create Emulator** (or use physical device)
   ```bash
   # List available emulators
   emulator -list-avds
   
   # Start emulator
   emulator -avd Nexus_5X_API_33
   ```

4. **Run App**
   ```bash
   ./gradlew installDebug
   ```

### iOS

1. **Open Project**
   ```bash
   cd ios/RedScreenFilter
   open RedScreenFilter.xcodeproj
   ```

2. **Install Dependencies** (if CocoaPods used)
   ```bash
   pod install
   ```

3. **Select Team**
   - In Xcode: Project > Signing & Capabilities
   - Select personal/team account

4. **Run on Simulator/Device**
   - Cmd + R or Product > Run

## Project Structure Quick Reference

```
RedScreenFilter/
├── android/
│   ├── app/src/main/
│   │   ├── java/com/redscreenfilter/     # Source code
│   │   ├── res/                          # Resources (UI, strings)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── gradle/
├── ios/
│   ├── RedScreenFilter/
│   │   ├── Sources/                      # Swift source code
│   │   └── Resources/                    # Assets
│   ├── RedScreenFilter.xcodeproj/
│   └── Podfile (if needed)
└── shared/
    ├── ARCHITECTURE.md                   # Design patterns
    └── DEVELOPMENT.md                    # This file
```

## Feature Roadmap

### Phase 1 (MVP)
- Red overlay + basic scheduling
- Activity presets
- Color blindness presets
- Quick tile / widget
- Do Not Disturb integration

### Phase 2 (Health-Focused)
- Sunset/sunrise scheduling
- Ambient light sensing
- 20-20-20 reminders
- Battery awareness
- Voice commands
- Selective app exemptions
- Analytics & insights

### Phase 3 (Advanced)
- Sleep schedule integration
- Smart learning algorithm
- Cloud sync (optional)
- Wearable integration

See [FEATURES.md](./FEATURES.md) for complete roadmap with complexity estimates and timeline.

## Development Workflow

### Versions & Release Cycle
- Maintain same major.minor version across platforms
- Android and iOS releases sync within same month
- Each phase gets a minor version bump (v1.0 → v2.0 → v3.0)

## Common Tasks

### Adding a New Feature

1. **Android**:
   - Create data class in `models/`
   - Add UI component in `ui/`
   - Update `PreferencesManager` for persistence

2. **iOS**:
   - Add property to `OverlaySettings.swift`
   - Create SwiftUI view component
   - Update `PreferencesManager`

3. **Both**:
   - Test on target device
   - Update [ARCHITECTURE.md](./ARCHITECTURE.md) if needed

### Running Tests

**Android**:
```bash
cd android
./gradlew test                # Unit tests
./gradlew connectedAndroidTest # Instrumentation tests
```

**iOS**:
```bash
cd ios/RedScreenFilter
xcodebuild test -scheme RedScreenFilter
```

### Building Release Builds

**Android**:
```bash
./gradlew assembleRelease  # Creates AAB/APK
```

**iOS**:
```bash
# Archive for App Store
xcodebuild -scheme RedScreenFilter -configuration Release archive
```

## Debugging

### Android
- Use Logcat in Android Studio
- Set breakpoints and use debugger
- Android Device Monitor for performance profiling

### iOS
- Xcode debugger with breakpoints
- Xcode Instruments for performance analysis
- Console logs via `print()` statements

## Useful Documentation

- **Android**: 
  - https://developer.android.com/guide/components/services
  - https://developer.android.com/guide/topics/permissions/overview

- **iOS**:
  - https://developer.apple.com/documentation/uikit/app_and_environment/windows
  - https://developer.apple.com/documentation/backgroundtasks

## Troubleshooting

### "System alert window permission denied" (Android)
- Settings > Apps > RedScreenFilter > Permissions > Other > Display over other apps > Allow

### "Overlay not visible" (iOS)
- Check window level is set correctly
- Verify `isUserInteractionEnabled = false` on overlay view
- Confirm App Groups entitlement is added

### Background task not triggering
- **Android**: Check Doze Mode exceptions in Settings > Battery
- **iOS**: Enable Background App Refresh in Settings > RedScreenFilter

## Git Workflow

```bash
# Feature branch
git checkout -b feature/opacity-slider

# Commit with meaningful messages
git commit -m "feat: add opacity slider to settings UI"

# Push and create pull request
git push origin feature/opacity-slider
```

## Questions?

Refer to platform-specific READMEs:
- [Android README](../android/README.md)
- [iOS README](../ios/README.md)
