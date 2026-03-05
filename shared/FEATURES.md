# Supported Features & Roadmap

## Overview

Red Screen Filter includes a comprehensive set of features focused on health, accessibility, and smart automation.

### ⚠️ Platform Differences

**Android**: Full-featured system-wide red screen overlay using native system overlay capabilities.

**iOS**: In-app red overlay with scheduling reminders, smart features, and analytics. System-wide overlay not possible due to iOS sandbox architecture. See [iOS Limitations](../ios/IMPLEMENTATION_PLAN.md#ios-limitations) for details.

## Feature Catalog

### 🔴 Core Features (MVP - Phase 1)

Essential functionality for the app's primary purpose.

| Feature | Description | Android | iOS | Status | Notes |
|---------|-------------|---------|-----|--------|-------|
| **Red Screen Overlay** | Customizable red-tinted overlay with opacity control | ✅ System-wide | ✅ In-app only | Core | iOS overlay appears only within the app; disappears when switching apps |
| **Quick Toggle** | One-tap on/off from home screen | ✅ Quick Tile | ⚠ Widget/Shortcut | Core | iOS can open app or use Siri Shortcut |
| **Settings UI** | Intuitive settings interface for configuration | ✅ | ✅ | Core | - |
| **Basic Scheduling** | Manual time-based start/end scheduling | ✅ | ⚠ Notifications only | Phase 1 | iOS sends reminders at scheduled times; requires manual app opening |
| **Persistence** | Settings saved across sessions | ✅ | ✅ | Core | iOS persists user settings, but in-app overlay doesn't persist across app backgrounding |

---

### 🎨 Customization & Accessibility (Phase 1-2)

**Color Blindness Presets** ⭐ *Low Complexity*
- Support for different color vision types:
  - Red color filtered (standard)
  - Red-Orange blend (for protanopia)
  - Red-Pink tint (for deuteranopia)
  - High contrast red (for achromatopsia)
- Users can cycle through presets
- Accessibility-focused feature

| | Android | iOS | Notes |
|---|---------|-----|-------|
| **Implementation** | Presets stored in `OverlaySettings` | SwiftUI color picker integration | Both platforms support color variants |
| **Persistence** | SharedPreferences (encrypted) | UserDefaults | iOS variants apply in-app only |

**Activity Presets** ⭐ *Low Complexity*
- Pre-configured profiles for different scenarios:
  - **Work Mode**: Medium opacity (40%), no time limit
  - **Gaming Mode**: Lower opacity (30%), brightness boost option
  - **Movie Mode**: Low opacity (20%), minimal distraction
  - **Sleep Mode**: High opacity (80%), full red tint
  - **Custom**: User-defined settings
- One-tap activation from main UI

| | Android | iOS | Notes |
|---|---------|-----|-------|
| **Data Storage** | `PreferencesManager` with profile serialization | Codable structs in UserDefaults | Both support identical preset model |
| **UI Pattern** | Horizontal carousel or tab-based | SwiftUI segmented picker |

---

### ⏰ Smart Scheduling (Phase 2)

**Sunset/Sunrise Activation** ⭐ *Medium Complexity*
- Automatically enable overlay based on sunset time
- Uses device location (with permission)
- Calculates sunset/sunrise using astronomy calculations
- User can set offset (e.g., "30 mins before sunset")

**Implementation**:
- **Android**: 
  - `LocationManager` for GPS coordinates
  - Algorithm: Calculate sunset using standard solar equations
  - Service scheduled with `WorkManager`
- **iOS**:
  - `CoreLocation` for coordinates
  - Local calculation library for solar equations
  - Sends `UNUserNotificationCenter` reminders at calculated time
  - ⚠ No automatic UI overlay at sunset (sends notification only)

| Setting | Default | Range |
|---------|---------|-------|
| Enable Location Scheduling | OFF | Toggle |
| Sunset Buffer | 0 mins | -60 to +60 mins |
| Location Auto-Update | Every 6 hours | 1-24 hours |

---

### 📊 Health & Wellness Features (Phase 2)

**20-20-20 Reminders** ⭐ *Low Complexity*
- Every 20 minutes: Notify user to look away for 20 seconds
- Follows optometry best practice for eye strain prevention
- Optional: Disable during video calls or full-screen apps
- Sound/vibration customizable (silent, subtle tone, strong vibration)

**Implementation**:
- **Android**: `AlarmManager` + `BroadcastReceiver` for precise 20-min intervals
- **iOS**: `UNUserNotificationCenter` scheduled notifications

**Platform Differences**:
- Android overlay automatically hides for calls via TelecomManager
- iOS notifications may be delivered during calls (user can snooze)

**Configuration**:
```
{
  enabled: Boolean,
  intervalMinutes: 20,
  notificationStyle: "sound" | "vibration" | "silent",
  pauseDuringCalls: Boolean,
  pauseDuringFullscreen: Boolean
}
```

**Battery Awareness** ⭐ *Low Complexity*
- Detect low battery state (< 20%)
- Automatically reduce overlay opacity by 30% to save power
- Option to disable overlay entirely when battery critical (< 10%)
- Resume normal settings when charging
- Visual indicator in settings showing current battery optimization status

**Implementation**:
- **Android**: `BatteryManager` broadcasts
- **iOS**: `UIDevice.batteryLevel` + notifications
- Real-time monitoring with background task

---

### 🎯 Intelligent Adaptation (Phase 2)

**Ambient Light Sensing** ⭐ *Medium Complexity*
- Use device light sensor to auto-adjust overlay opacity
- Brighter room → reduce opacity, darker room → increase opacity
- User sets sensitivity (Low/Medium/High)
- Can toggle on/off; manual override available

**Implementation**:
- **Android**: `SensorManager` + `TYPE_LIGHT` sensor
- **iOS**: `AVCaptureDevice` for light level or `ARKit` ambient light estimation
- Smoothing algorithm to prevent flickering
- Sample reading every 5-10 seconds

**Adaptive Configuration**:
```
{
  enabled: Boolean,
  baseOpacity: 0.5,        // 50% default
  sensitivity: "Low" | "Medium" | "High",
  luxThresholds: {
    dark: { lux: < 50, opacity: 0.8 },
    dim: { lux: 50-500, opacity: 0.6 },
    normal: { lux: 500-3000, opacity: 0.4 },
    bright: { lux: > 3000, opacity: 0.2 }
  }
}
```

**Gradual Fade-In** ⭐ *Low Complexity*
- Overlay opacity slowly increases as bedtime approaches
- Example: 8 PM starts at 30%, gradually reaches 80% by 10 PM
- Smooth transition every 2-5 minutes
- Prevents sudden screen changes

---

### 🚀 User Experience (Phase 1-2)

**Quick Tile / Home Screen Widget** ⭐ *Medium Complexity*
- **Android**: Quick Settings tile for instant toggle
- **iOS**: Lock screen widget showing:
  - Current on/off status
  - Opacity level
  - Next scheduled change
- Tap to toggle on main widget, long-press for settings

**Voice Commands** ⭐ *Medium Complexity*
- **Android**: Google Assistant integration
  - "Hey Google, enable red screen filter"
  - "Hey Google, set red filter to 50%"
- **iOS**: Siri Shortcuts
  - "Hey Siri, red screen on"
  - "Hey Siri, change screen filter to sleep mode"
- Voice commands update settings in real-time

**Gesture Shortcuts** ⭐ *Low Complexity*
- Double-tap status bar (iOS) or notification (Android) to toggle
- Long-press widget to access quick preset selection
- Swipe gesture to adjust opacity (optional, on-demand)

---

### 📈 Analytics & Insights (Phase 2-3)

**Daily/Weekly Reports** ⭐ *Medium Complexity*
- Dashboard showing:
  - Total overlay usage time (today, this week)
  - Average opacity used
  - Most used preset
  - Scheduled vs manual activation breakdown
- Visual charts (pie charts, line graphs)
- Push notification summary each morning

**Streak System** ⭐ *Low Complexity*
- Gamification: Track consecutive nights of usage
- Display current streak badge
- Achievements: "7 Day Streak", "30 Day Streak", etc.
- Weekly digest of stats

**Data Storage**: All analytics stored locally (no cloud)
- **Android**: SQLite database
- **iOS**: Core Data

---

### 🔒 Privacy & Control (Phase 1-2)

**Selective App Exemptions** ⚠ *Android Only*
- **Android**: ✅ Fully supported
  - Disable overlay for specific apps (Camera, Banking, etc.)
  - Uses `UsageStatsManager` + `PACKAGE_USAGE_STATS` permission
  - Toggle per-app exemptions in settings
- **iOS**: ❌ Not Possible
  - iOS prevents apps from detecting which app is currently active
  - No method to hide overlay for specific apps
  - App Store policy prohibits this anyway

**Do Not Disturb Integration** ⭐ *Low Complexity*
- Respect system DND mode by default
- Option: Keep overlay active during DND for sleep hours
- Disable notifications during DND window

---

## Implementation Timeline

### Phase 1 (Sprint 1-2) - MVP
- [x] Red screen overlay (core)
- [x] Basic scheduling (time-based)
- [ ] Activity presets
- [ ] Quick tile / widget
- [ ] Color blindness presets
- [ ] Do Not Disturb integration

### Phase 2 (Sprint 3-4)
- [ ] Sunset/sunrise scheduling
- [ ] 20-20-20 reminders
- [ ] Ambient light sensing
- [ ] Battery awareness
- [ ] Selective app exemptions
- [ ] Voice commands
- [ ] Daily/weekly reports
- [ ] Streak system
- [ ] Gradual fade-in

### Phase 3 (Sprint 5+)
- [ ] Sleep schedule integration
- [ ] Smart learning algorithm
- [ ] Advanced analytics
- [ ] Cloud sync (optional, privacy-respecting)
- [ ] Wearable integration

---

## Feature Comparison Matrix

| Feature | Complexity | Phase | Android | iOS | Est. Dev Time |
|---------|-----------|-------|---------|-----|----------------|
| Color Blindness Presets | Low | 1 | ✅ | ✅ | 4h |
| Activity Presets | Low | 1 | ✅ | ✅ | 6h |
| 20-20-20 Reminders | Low | 2 | ✅ | ✅ | 8h |
| Battery Awareness | Low | 2 | ✅ | ✅ | 4h |
| Gradual Fade | Low | 2 | ✅ | ✅ | 6h |
| Do Not Disturb | Low | 1 | ✅ | ✅ | 4h |
| Gesture Shortcuts | Low | 2 | ✅ | ✅ | 6h |
| Streak System | Low | 2 | ✅ | ✅ | 4h |
| Quick Tile / Widget | Medium | 1 | ✅ | ✅ | 10h |
| Sunset/Sunrise | Medium | 2 | ✅ | ✅ | 12h |
| Ambient Light Sensing | Medium | 2 | ✅ | ✅ | 10h |
| Voice Commands | Medium | 2 | ✅ | ✅ | 8h |
| Selective App Exemptions | Medium | 2 | ✅ Android | ❌ iOS | 12h | iOS cannot detect active app |
| Daily/Weekly Reports | Medium | 2 | ✅ | ✅ | 16h | Both support analytics |

---

## Notes

- All features respect system accessibility settings
- No cloud data storage (privacy-first approach)
- Battery optimization prioritized in all background features
- Each feature can be independently toggled by user
- Cross-platform consistency maintained via shared architecture
