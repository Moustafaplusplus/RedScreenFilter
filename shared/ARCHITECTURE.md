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
Both platforms render a red-tinted layer:
- **Color**: RGB(255, 0, 0) with configurable alpha
- **Touch Passthrough**: Overlay doesn't block user interaction
- **Performance**: Minimal CPU/GPU usage with hardware acceleration

**Behavior**:
- Overlay updates in real-time when settings change
- No flicker or visual glitches
- Persists across app backgrounding

### 3. Scheduling Engine
Both platforms support automated on/off scheduling:
- **Time-based**: User sets start/end times (e.g., 9 PM to 7 AM)
- **Automatic Transitions**: Overlay toggles at configured times
- **Manual Override**: User can manually toggle regardless of schedule

### 4. Background Persistence
Both apps maintain overlay state in background:
- **Android**: `WorkManager` schedules tasks across doze states
- **iOS**: `BGProcessingTaskRequest` + App Groups for state sharing
- **Result**: Overlay persists even when app is backgrounded or phone is locked

## Feature Parity Matrix

| Feature | Android | iOS | Status |
|---------|---------|-----|---------|
| Toggle on/off | ✅ | ✅ | Core |
| Opacity control | ✅ | ✅ | Core |
| Time-based scheduling | ✅ | ✅ | Phase 1 |
| Background persistence | ✅ | ✅ | Core |
| Sunset/sunrise scheduling | ✅ | ✅ | Phase 2 |
| Activity presets | ✅ | ✅ | Phase 1 |
| Color blindness presets | ✅ | ✅ | Phase 1 |
| 20-20-20 reminders | ✅ | ✅ | Phase 2 |
| Ambient light sensing | ✅ | ✅ | Phase 2 |
| Battery awareness | ✅ | ✅ | Phase 2 |
| Quick tile / widget | ✅ | ✅ | Phase 1 |
| Voice commands | ✅ | ✅ | Phase 2 |
| Streak tracking | ✅ | ✅ | Phase 2 |
| Daily/weekly reports | ✅ | ✅ | Phase 2 |
| Selective app exemptions | ✅ | ✅ | Phase 2 |

**Full feature list available in [FEATURES.md](./FEATURES.md)**

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
