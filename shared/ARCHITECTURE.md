# Architecture & Design

## Overview

Red Screen Filter is a dual-platform mobile app that applies a customizable red overlay for eye comfort. While platforms are implemented natively (Kotlin/Swift), they follow a unified architectural pattern for consistency and feature parity.

## Shared Principles

### 1. Settings Persistence
Both platforms use native storage mechanisms:
- **Android**: `SharedPreferences` (encrypted)
- **iOS**: `UserDefaults` with App Groups

**Shared Data Model**:
```
OverlaySettings {
  isEnabled: Boolean
  opacity: Float (0.0-1.0)
  scheduleEnabled: Boolean
  scheduleStartTime: Time (HH:mm)
  scheduleEndTime: Time (HH:mm)
  useSchedule: Boolean
}
```

### 2. Overlay Rendering
**Android**: Full-screen system overlay
- Uses `WindowManager` to draw above all apps
- **Color**: RGB(255, 0, 0) with configurable alpha
- **Touch Passthrough**: FLAG_NOT_FOCUSABLE, FLAG_NOT_TOUCHABLE
- **Performance**: Hardware-accelerated, minimal CPU/GPU usage
- **Behavior**: Updates in real-time, persists across app backgrounding

**iOS**: In-app overlay only (architectural limitation)
- Red overlay appears only while user is in RedScreenFilter app
- Implemented as SwiftUI ZStack layer on top of content
- Disappears immediately when user switches to other apps
- This is iOS sandbox security (not a bug or limitation to work around)

### 3. Scheduling Engine
**Android**: Automated on/off scheduling with system integration
- **Time-based**: `WorkManager` automatically toggles overlay at configured times
- **Automatic Transitions**: Overlay turns on/off without user action
- **Manual Override**: User can manually toggle regardless of schedule
- **Background Execution**: Works even when app is backgrounded

**iOS**: Notification-based scheduling (reminder approach)
- **Time-based**: `UNUserNotificationCenter` sends reminders to open the app
- **User Action Required**: Overlay won't turn on automatically; user must open app
- **Location-based**: CoreLocation calculates sunset/sunrise, triggers reminders
- **Manual Toggle**: User opens app and toggles overlay manually

### 4. Background Persistence
**Android**: True background persistence
- `WorkManager` maintains scheduling across doze states
- Foreground service keeps overlay active while device in use
- `WorkManager` survives app kills and device reboots
- **Result**: Overlay works even with app backgrounded or not running

**iOS**: Limited background capabilities
- Cannot maintain visual overlay when app backgrounded
- Can schedule notifications that persist in background
- `BGProcessingTaskRequest` runs but cannot display UI
- **Result**: Reminders work in background, but overlay only visible in-app

## Feature Parity Matrix

| Feature | Android | iOS | Status | Notes |
|---------|---------|-----|---------|-------|
| Toggle on/off | ✅ | ✅ | Core | Android: persistent; iOS: in-app only |
| Opacity control | ✅ | ✅ | Core | Both support 0-100% opacity |
| Time-based scheduling | ✅ | ⚠ | Phase 1 | Android: auto on/off; iOS: reminders to open app |
| Background persistence | ✅ | ❌ | Core | Android only (foreground service) |
| Sunset/sunrise scheduling | ✅ | ✅ | Phase 2 | Both use CoreLocation + solar calculations |
| Activity presets | ✅ | ✅ | Phase 1 | Identical feature set |
| Color blindness presets | ✅ | ✅ | Phase 1 | 6 variants on both platforms |
| 20-20-20 reminders | ✅ | ✅ | Phase 2 | Both via notifications |
| Ambient light sensing | ✅ | ✅ | Phase 2 | Android: SensorManager; iOS: AVCaptureDevice |
| Battery awareness | ✅ | ✅ | Phase 2 | Both monitor device battery |
| Quick tile / widget | ✅ | ⚠ | Phase 1 | Android: Quick Settings; iOS: Lock Screen widget (limited) |
| Voice commands | ✅ | ✅ | Phase 2 | Android: Google Assistant; iOS: Siri |
| Streak tracking | ✅ | ✅ | Phase 2 | Both support gamification |
| Daily/weekly reports | ✅ | ✅ | Phase 2 | CoreData (both platforms) |
| Selective app exemptions | ✅ | ❌ | Phase 2 | iOS cannot detect foreground app |

## Development Workflow

### Versions & Release Cycle
- Maintain same major.minor version across platforms
- Android and iOS releases sync within same month

### Code Review Checklist
- [ ] Feature implemented on both platforms
- [ ] Settings model updated if needed
- [ ] Tests written for logic changes
- [ ] UI/UX consistent with other platform
- [ ] No battery/performance regression
- [ ] Documentation updated

### Testing Strategy
1. **Unit Tests**: Settings, scheduling logic
2. **Integration Tests**: Overlay lifecycle, background tasks
3. **Manual Testing**: Cross-platform behavior verification
4. **Performance**: Battery drain, memory usage on target devices

## File Organization

```
RedScreenFilter/
├── /android              # Kotlin Android project
├── /ios                  # Swift iOS project
├── /shared
│   ├── ARCHITECTURE.md   # This file
│   ├── DEVELOPMENT.md    # Setup & contribution guide
│   └── /assets           # Logos, branding (future)
└── /docs                 # API reference (future)
```
