# iOS Implementation Plan

Complete step-by-step implementation roadmap for Red Screen Filter iOS app (Swift/SwiftUI). Phases organized from 0-100% completion.

## Project Overview

**Target**: Eye health companion app combining in-app red overlay + system-wide color filtering via iOS native features
**Language**: Swift 5.9+
**Framework**: SwiftUI + Combine
**Min OS**: iOS 14.0+
**Architecture**: MVVM with reactive state management
**App Groups**: group.com.redscreenfilter (for session persistence)

### 🎯 System-Wide Filtering Strategy
**Approach**: Siri Shortcuts wrapper around iOS's built-in accessibility features
- iOS provides system-wide **Color Filters** (native accessibility feature)
- We create **convenient Siri Shortcuts** that trigger these filters
- Users can add shortcuts to Control Center for one-tap access
- Result: System-wide color filtering without custom overlay limitations
- See [Siri Shortcuts Integration](#phase-65-75---siri-shortcuts-for-system-wide-control) and [iOS Limitations](#ios-limitations) for details

### ⚠️ iOS Limitation (In-App Overlay)
**iOS does NOT support custom system-wide overlays** that appear over other apps (unlike Android's `SYSTEM_ALERT_WINDOW`). The in-app overlay works **only while the user is within the RedScreenFilter app**. When user switches to Safari, Messages, Camera, etc., the overlay disappears. For system-wide filtering, use the Siri Shortcuts approach (see above).

---

## Phase Breakdown

### Phase 0-10% - Project Setup & Core Foundation
**Objective**: Initialize Xcode project, dependencies, and base reactive architecture

#### 0-5% - Xcode Project Creation
- [ ] Create new iOS app project in Xcode 15.0+
- [ ] Project name: RedScreenFilter
- [ ] Organization: Your Company
- [ ] Bundle ID: com.redscreenfilter
- [ ] Language: Swift
- [ ] User Interface: SwiftUI
- [ ] Deployment Target: iOS 14.0
- [ ] Include Unit Tests: No (you'll test manually)
- [ ] Location: `/ios/RedScreenFilter` folder

**Deliverable**: Runnable empty app on simulator/device

#### 5-10% - Project Structure & Entitlements Setup
- [ ] Create core directory structure:
  ```
  RedScreenFilter/Sources/
  ├── App/
  │   ├── RedScreenFilterApp.swift
  │   └── AppDelegate.swift
  ├── Views/
  │   ├── MainView.swift
  │   ├── SettingsView.swift
  │   └── AnalyticsView.swift
  ├── Services/
  │   ├── OverlayWindowManager.swift
  │   ├── PreferencesManager.swift
  │   └── SchedulingService.swift
  ├── ViewModels/
  │   └── OverlayViewModel.swift
  ├── Models/
  │   ├── OverlaySettings.swift
  │   └── UsageEvent.swift
  └── Utilities/
      ├── Constants.swift
      └── Extensions.swift
  ```

- [ ] Configure App Groups entitlement
  - Project > Signing & Capabilities
  - Add "App Groups" capability
  - Add container: `group.com.redscreenfilter`
  - This allows data sharing for background tasks

- [ ] Configure Background Modes
  - Capabilities: Add "Background Modes"
  - Enable: Background Fetch, Background Processing
  - Enable: Remote Notifications (optional)

- [ ] Update Info.plist
  - NSLocationWhenInUseUsageDescription: "To calculate sunset/sunrise times"
  - NSLocationAlwaysAndWhenInUseUsageDescription: "For background sunrise/sunset scheduling"

**Deliverable**: Project configured with all required entitlements and capabilities

---

### Phase 10-20% - In-App Red Overlay & Main UI
**Objective**: Functional red overlay that works within the app (when user is in RedScreenFilter)

#### 10-15% - In-App Overlay Implementation
- [ ] Create `OverlayView.swift` (SwiftUI)
  - Full-screen red rectangle with configurable opacity
  - Overlay appears only while in the app
  - Methods:
    - `showOverlay(opacity: Float)`
    - `hideOverlay()`
    - `updateOpacity(Float)`
  - Properties: `isVisible: Bool`, `currentOpacity: Float`
  - Uses ZStack to layer overlay above content
  - Color variants: Red Standard, Red-Orange, Red-Pink, High Contrast

- [ ] Implement overlay in MainView
  - Root view with overlay as top layer
  - Allow interaction with controls beneath overlay
  - Smooth transitions on toggle

**Note**: Overlay **does NOT persist** when user switches to other apps. This is an iOS architectural limitation, not a bug.

**Deliverable**: Red overlay appears on screen within app, opacity adjustable

#### 15-20% - Reactive State Management & Settings UI
- [ ] Create `PreferencesManager.swift`
  - Uses UserDefaults (standard, not App Groups - UI-only)
  - Methods:
    - `setOverlayEnabled(_ enabled: Bool)`
    - `isOverlayEnabled() -> Bool`
    - `setOpacity(_ opacity: Float)`
    - `getOpacity() -> Float`
    - Observes changes for reactive updates
  - Implement as singleton
  - Use `@Published` properties for Combine integration

- [ ] Create `OverlaySettings.swift` Codable model
  ```swift
  struct OverlaySettings: Codable {
      var isEnabled: Bool = false
      var opacity: Float = 0.5
      var scheduleEnabled: Bool = false
      var scheduleStartTime: String = "21:00"
      var scheduleEndTime: String = "07:00"
      var useAmbientLight: Bool = false
      var useLocationSchedule: Bool = false
      var colorVariant: String = "red_standard"
  }
  ```

- [ ] Create `OverlayViewModel.swift` (MVVM ViewModel)
  - Manages overlay state with @Published properties
  - Properties: `isEnabled`, `opacity`, `settings`, `currentPreset`
  - Methods: `toggleOverlay()`, `updateOpacity(Float)`, `applyPreset(PresetProfile)`
  - Observes PreferencesManager changes
  - Reactive UI updates

- [ ] Create `MainView.swift` (SwiftUI root)
  - Tab-based navigation
  - Tab 1: Main overlay control
  - Tab 2: Presets
  - Tab 3: Settings
  - Tab 4: Analytics

- [ ] Create main overlay control view
  - Large toggle button (on/off)
  - Opacity slider (0-100%)
  - Current opacity display
  - Quick preset buttons
  - In-app red overlay preview

- [ ] Create `SettingsView.swift`
  - Schedule settings (time-based)
  - Location-based scheduling (sunset/sunrise)
  - Battery optimization toggle
  - Ambient light sensing toggle
  - 20-20-20 reminder settings
  - Color variant picker

**Deliverable**: Full UI for toggle, settings, presets all connected to reactive state

---

### Phase 20-35% - Scheduling & Location Services
**Objective**: Time-based and sunset/sunrise scheduling with notifications

#### 20-25% - Basic Time-Based Scheduling
- [ ] Create `SchedulingService.swift`
  - Methods:
    - `determineOverlayState() -> Bool` - checks if overlay should be active
    - `isScheduleActive() -> Bool` - verify user enabled schedule
    - `setSchedule(start: String, end: String)` - format HH:mm
    - Handles day boundary (10 PM to 7 AM)
    - Thread-safe using DispatchQueue

- [ ] Implement time comparison logic
  - Current time vs scheduled window
  - Handle midnight crossing
  - Return boolean for overlay state

- [ ] Create settings UI in SettingsView
  - DatePicker for start time
  - DatePicker for end time
  - Toggle to enable/disable schedule
  - Display formatted times

**Deliverable**: Overlay state follows scheduled times manually

#### 35-40% - Background Task Scheduling with BGProcessingTaskRequest
- [ ] Create `BackgroundScheduleTask.swift`
  - Extends `BGProcessingTask`
  - Runs periodically (system-determined, ~every 15 mins to hours)
  - Calls `SchedulingService.determineOverlayState()`
  - Updates overlay via OverlayWindowManager
  - Handles task expiration gracefully
  - Uses App Groups to persist state

- [ ] Register background task in AppDelegate
  - `BGTaskScheduler.shared.register(forTaskWithIdentifier:usingQueue:)`
  - Task ID: "com.redscreenfilter.schedule-update"
  - Queue: `.global()` for background work

- [ ] Implement requestBackgroundTask
  - Called from UI when schedule changes
  - Call `BGTaskScheduler.shared.submitBackgroundTask.Request(_:)`
  - Set requirements: `.userInitiated` preferably

- [ ] Update Info.plist
  - Add `BGTaskSchedulerPermittedIdentifiers` array
  - Add: "com.redscreenfilter.schedule-update"

**Deliverable**: Overlay updates automatically every 15 mins based on schedule

#### 40-50% - Sunrise/Sunset Scheduling with CoreLocation
- [ ] Add CoreLocation permission requests
  - Privacy keys in Info.plist (already added Phase 0-10%)
  - Request `CLLocationManager.locationAuthorizationStatus`
  - Use `requestWhenInUseAuthorization()` initially

- [ ] Create `LocationCalculationService.swift`
  - Fetches user's latitude/longitude using `CLLocationManager`
  - Implements solar calculation algorithm (SPA - Solar Position Algorithm)
  - Calculates sunrise/sunset times for today
  - Caches location (update every 6 hours or on manual refresh)
  - Returns: `(sunrise: Date, sunset: Date)`

- [ ] Create `SunriseCalculator.swift` utility
  - Pure function to calculate sunset/sunrise
  - Inputs: latitude, longitude, date
  - Outputs: times as Date objects
  - Algorithm: Solar Zenith calculations (standard astronomy)

- [ ] Integrate with SchedulingService
  - Add `useLocationBasedSchedule: Bool` setting
  - If enabled, overlay starts at (sunset + userOffset)
  - Overlay ends at (sunrise)
  - Prioritize location schedule over time schedule

- [ ] Add location settings UI
  - Toggle: "Use sunset/sunrise scheduling"
  - Offset slider: -60 to +60 minutes before/after sunset
  - Display calculated times (e.g., "Sunset: 6:15 PM")
  - Button to manually refresh location

**Deliverable**: Time and location-based scheduling logic complete

---

### Phase 35-50% - Customization & Presets
**Objective**: Multiple screen modes and color variants

#### 35-40% - Activity Presets System
- [ ] Create `PresetProfile.swift` model
  ```swift
  struct PresetProfile: Identifiable, Codable {
      let id: UUID
      var name: String
      var opacity: Float
      var colorVariant: ColorVariant
      var description: String
  }
  
  enum ColorVariant: String, Codable {
      case redStandard
      case redOrange
      case redPink
      case highContrast
  }
  ```

- [ ] Create `PresetsManager.swift`
  - Store presets in UserDefaults (App Groups)
  - CRUD operations: create, read, update, delete
  - Default presets: Work, Gaming, Movie, Sleep, Custom
  - Serialize/deserialize using Codable + JSONEncoder/Decoder

- [ ] Add preset data persistence
  - Load defaults on first app launch
  - Allow user to create/edit/delete custom presets

- [ ] Create Presets UI in SettingsView
  - ScrollView with preset cards
  - Tap to apply preset instantly
  - Long-press (or swipe) to edit/delete
  - Create new preset button

- [ ] Update MainView
  - Horizontal carousel showing quick preset buttons
  - Tap applies preset (opacity, color)

**Deliverable**: Users can switch between and customize presets

#### 40-50% - Color Blindness Presets
- [ ] Define ColorVariant enum with values:
  ```swift
  enum ColorVariant: String, Codable {
      case redStandard     // RGB(255, 0, 0)
      case redOrange       // RGB(255, 100, 0)
      case redPink         // RGB(255, 0, 100)
      case highContrast    // RGB(255, 0, 0) with higher opacity floor
  }
  ```

- [ ] Create color mapping function
  ```swift
  func getUIColor(for variant: ColorVariant, opacity: Float) -> UIColor {
      switch variant {
          case .redStandard: 
              return UIColor(red: 1.0, green: 0, blue: 0, alpha: opacity)
          case .redOrange: 
              return UIColor(red: 1.0, green: 0.39, blue: 0, alpha: opacity)
          // ... etc
      }
  }
  ```

- [ ] Add color variant settings
  - Create enum to track current selection in PreferencesManager
  - Store selected variant in UserDefaults

- [ ] Create ColorVariant picker UI
  - SegmentedPicker or List with radio buttons
  - Show color preview swatch for each variant
  - Save selection when changed

- [ ] Update OverlayWindowManager
  - Read current color variant from PreferencesManager
  - Apply correct color in `updateOpacity()`
  - Reactive updates when variant changes

**Deliverable**: Users can select color variants with visual preview

---

### Phase 50-65% - Smart Features with Sensors & Battery
**Objective**: Battery awareness, light sensing, health reminders

#### 50-55% - Battery Awareness
- [ ] Enable battery monitoring
  - `UIDevice.current.isBatteryMonitoringEnabled = true`
  - Read `UIDevice.current.batteryLevel` (0.0-1.0)
  - Read `UIDevice.current.batteryState` (.unknown, .unplugged, .charging, .full)

- [ ] Create `BatteryMonitor.swift`
  - Observes battery state changes via `UIDevice.batteryStateDidChangeNotification`
  - Calculates if battery < 20% (critical) or < 10% (ultra-critical)
  - Publishes state through @Published property (Combine)

- [ ] Extend PreferencesManager
  - Add `batteryOptimizationEnabled: Bool` setting
  - Add `batteryOptimizationThreshold: Int` (e.g., 20)

- [ ] Integrate with OverlayViewModel
  - Monitor battery state
  - When battery < threshold:
    - Reduce overlay opacity by 30% (or disable if < 10%)
    - Show visual indicator in UI
    - Store original opacity, restore when charging

- [ ] Add battery settings UI
  - Toggle: "Battery optimization"
  - Threshold slider: 10-30%
  - Status indicator: "Battery: 15% (optimized)"

**Deliverable**: Notifications warn user on low battery

#### 55-60% - Ambient Light Sensing with AVFoundation
- [ ] Create `LightSensorManager.swift`
  - Uses `AVCaptureDevice` to read ambient light level
  - Alternative: Use `ARKit` for light estimation (more reliable)
  - Reads lux value periodically (every 5-10 seconds)
  - Implements smoothing algorithm (exponential moving average) to prevent jitter

- [ ] Implement illuminance-to-opacity mapping
  ```swift
  func getOpacityForLux(_ lux: Float) -> Float {
      switch lux {
          case 0...50: return 0.8      // Very dark
          case 51...500: return 0.6    // Dim
          case 501...3000: return 0.4  // Normal
          default: return 0.2          // Bright
      }
  }
  ```

- [ ] Create sensor monitoring
  - Start/stop sensor in response to app lifecycle
  - Handle permission requests
  - Background-safe (pause when backgrounded unless always-on needed)

- [ ] Add ambient light UI settings
  - Toggle: "Auto-adjust to room brightness"
  - Sensitivity slider: Low / Medium / High
  - Current lux reading display (debug)
  - Manual override to lock current opacity

**Deliverable**: Notifications adjust based on ambient light sensor

#### 60-65% - 20-20-20 Eye Strain Reminders
- [ ] Create `EyeStrainReminderService.swift`
  - Uses `UNUserNotificationCenter` for scheduling notifications
  - Schedule notifications every 20 minutes
  - Uses `UNCalendarNotificationTrigger` for precise timing

- [ ] Create reminder notification content
  - Title: "Eye Break"
  - Body: "Look away for 20 seconds"
  - Sound: Subtle system sound (UILocalNotification)
  - Badge: Optional

- [ ] Add notification request handler
  - `UNUserNotificationCenter.current().requestAuthorization(options:)`
  - Handle user responses (dismiss, open app)

- [ ] Add reminder settings UI
  - Toggle: "Eye strain reminders"
  - Notification interval picker: 15, 20, 25, 30 minutes
  - Notification style: Sound / Vibration / Silent
  - Pause during video calls (detect using `CallKit` or app monitoring)

- [ ] Implement periodic reminder scheduling
  - Schedule first notification + 20 mins
  - Reschedule on app launch
  - Cancel all when disabled

**Deliverable**: Users receive eye health reminders every 20 minutes

---

### Phase 65-80% - User Experience & Accessibility
**Objective**: Quick access, voice control, widgets

#### 65-70% - Lock Screen Widget with WidgetKit
- [ ] Create new Widget Extension target
  - File > New > Target > Widget Extension
  - Widget name: "Red Screen Control"
  - Supports Lock Screen widgets (iOS 16.1+)

- [ ] Create widget content
  - Shows overlay on/off status with icon
  - Opacity level display
  - Next scheduled change time
  - Tap to toggle overlay
  - Long-press for quick preset selection (if iOS 17+)

- [ ] Use App Groups for data sharing
  - Widget reads overlay state from shared UserDefaults (group.com.redscreenfilter)
  - Widget timeline updates every 5 minutes
  - Tap action calls app via deep link or App Intent

- [ ] Implement widget configuration (optional)
  - Allow user to choose between minimal/detailed view

**Deliverable**: Lock screen widget shows status and opens app

#### 70-75% - Siri Shortcuts for System-Wide Control
**Objective**: Enable system-wide color filtering via iOS native accessibility features

- [ ] Understand iOS Color Filters architecture
  - Built-in system accessibility feature (Settings → Accessibility → Display & Text Size → Color Filters)
  - Already system-wide (appears over all apps)
  - Pre-configured color options: Protanopia, Deuteranopia, Tritanopia, etc.
  - Our app creates **shortcuts that trigger these native filters**

- [ ] Create Siri Shortcuts for color filter control
  - **Shortcut 1: "Enable Red Filter Sleep Mode"**
    - Opens iOS Settings app
    - Navigates to Accessibility → Display & Text Size → Color Filters
    - Enables Color Tint with warm/red settings
    - Returns to RedScreenFilter app
  - **Shortcut 2: "Disable Color Filter"**
    - Opens Settings
    - Disables active color filter
    - Returns to app
  - **Shortcut 3: "Enable Color Filter - Work Mode"** (softer variant)
    - Similar but with professional/neutral tint

- [ ] Create launcher UI in MainView
  - **"Enable System-Wide Red Filter"** button
    - Taps shortcut to enable OS Color Filters
    - Shows user instructions first time
    - One-tap system-wide red tinting
  - **"Add to Control Center"** button
    - Instructions to add shortcuts to iOS Control Center
    - Users can then toggle with one tap from anywhere
    - Works from lock screen
  - **Tutorial section**
    - Step-by-step with screenshots
    - How to use shortcuts from Control Center
    - How to set automation (time-based triggers)

- [ ] Implement shortcut launcher in code
  ```swift
  // When user taps "Enable Red Filter" button
  func triggerColorFilterShortcut() {
      guard let url = URL(string: "shortcuts://run-shortcut/?name=Enable%20Red%20Filter%20Sleep%20Mode") else { return }
      UIApplication.shared.open(url)
  }
  ```

- [ ] Handle shortcut automation
  - iOS Shortcuts app supports **Automation** triggers
  - User can set: "At 9 PM, run shortcut to enable red filter"
  - User can set: "At 7 AM, run shortcut to disable red filter"
  - All handled by iOS Shortcuts app, not our app

- [ ] Provide shareable shortcuts (iCloud)
  - Generate **iCloud links** to pre-configured shortcuts
  - Users tap link → Adds shortcut to their Shortcuts app
  - One-tap addition (no manual configuration needed)

**Key Advantage**: 
- ✅ System-wide (works over all apps)
- ✅ On lock screen (one-tap from anywhere)
- ✅ Can be automated with iOS Shortcuts automation
- ✅ No custom overlay limitations
- ⚠️ Requires user action to add shortcut to Control Center first

**Deliverable**: Users can enable system-wide red filtering with one tap via Control Center shortcut

#### 75-80% - Additional Siri Voice Commands (Optional)
- [ ] Create custom Intent handlers for voice control of in-app features
  - Intent: "Set red filter opacity to 50%" (controls in-app overlay)
  - Intent: "Apply sleep preset" (controls in-app overlay settings)
  - These are app-level shortcuts, not system-wide

- [ ] Test with voice
  - "Hey Siri, red filter 80%"
  - "Hey Siri, apply movie mode"

**Deliverable**: Voice control for in-app overlay features

---

### Phase 75-85% - Analytics & Persistence
**Objective**: Usage tracking with local database, insights dashboard

#### 80-85% - Core Data Database Setup
- [ ] Create Core Data model (`RedScreenFilter.xcdatamodeld`)
  - Entity: `UsageEvent`
    - Properties: `timestamp: Date`, `overlayEnabled: Bool`, `opacity: Float`, `preset: String`
  - Entity: `DailyStats`
    - Properties: `date: Date`, `totalUsage: TimeInterval`, `preset: String`

- [ ] Create `CoreDataStack.swift`
  - Container initialization
  - Fetch controller setup
  - Save/load operations with error handling
  - Use App Groups persistent store location for background sync

- [ ] Create `AnalyticsService.swift`
  - Log events: overlay toggled, settings changed, preset applied
  - Query daily/weekly/monthly statistics
  - Calculate usage streaks
  - Methods:
    - `logEvent(_ event: UsageEvent)`
    - `getHourlyStats(for date: Date) -> [UsageEvent]`
    - `getStreakCount() -> Int`
    - `getTotalTimeTodayInSeconds() -> Int`

- [ ] Integrate logging into OverlayViewModel
  - Log when overlay toggled
  - Log when opacity changed
  - Log when preset applied (periodically, not every update)

**Deliverable**: All usage tracked in local database with query support

#### 85-90% - Analytics Dashboard UI
- [ ] Create `AnalyticsView.swift`
  - Tab-based view: Today / Week / Month
  - Display statistics:
    - Total overlay usage time (formatted)
    - Average opacity used
    - Most used preset
    - Current usage streak badge
  - Visual representation:
    - Charts (consider: Charts Swift package or custom DrawCanvas)
    - Progress bars
    - Colored badges for achievements

- [ ] Create analytics models & ViewModels
  - `AnalyticsViewModel` to prepare data for display
  - Format time intervals into readable strings
  - Calculate percentages/comparisons

- [ ] Add animations & visualizations
  - Smooth chart animations on view load
  - Number counters with animations
  - Streak badge with celebration animation (optional)

- [ ] Test on multiple devices
  - Verify charts render correctly
  - Performance on older devices

**Deliverable**: Users can view detailed usage analytics

---

### Phase 98-100% - Permissions, Background Modes & Polish
**Objective**: Final testing, permission handling, performance optimization

#### 98-99% - Runtime Permissions & Lifecycle Management
- [ ] Implement location permission request flow
  - Use `CLLocationManager.requestWhenInUseAuthorization()`
  - Check authorization status before accessing location
  - Graceful handling if user denies

- [ ] Implement notification permission request
  - Use `UNUserNotificationCenter.requestAuthorization(options:)`
  - Ask on first app launch or on settings screen
  - Graceful fallback if denied

- [ ] Update AppDelegate & SceneDelegate
  - Initialize overlay on app launch
  - Restore overlay state on return from background
  - Handle state restoration from App Groups

- [ ] Test permission flows on real device
  - First-time permission prompts
  - Denied permission recovery
  - Settings > App Permissions changes

- [ ] Handle background task lifecycle
  - Implement `sceneWillEnterForeground()` to sync state
  - Implement `sceneDidEnterBackground()` to persist state
  - Ensure overlay survives backgrounding

**Deliverable**: All permissions handled gracefully, background state maintained

#### 99-100% - Final Refinements & Performance Optimization
- [ ] Remove debug logs and clean code
  - Use proper logging framework (OSLog)
  - Remove print() statements except critical errors

- [ ] Optimize memory and performance
  - Profile with Xcode Instruments
  - Check for memory leaks in OverlayWindowManager
  - Verify light sensor stops when app backgrounded
  - Ensure battery monitoring doesn't drain battery

- [ ] Test on multiple devices and iOS versions
  - iPhone 12/13/14/15 simulators
  - iPad if target supports
  - iOS 14.0, 15.0, 16.0, 17.0+
  - Test on physical device (iPhone 12+ recommended)

- [ ] Test notification and reminder behavior
  - Verify scheduled notifications fire at correct times
  - Verify scheduling continues when app backgrounded
  - Test location-based reminders work

- [ ] UI/UX Polish
  - Ensure responsive animations
  - Test dark mode / light mode
  - Verify accessibility (VoiceOver)
  - Check Dynamic Type scaling (text sizes)

- [ ] Final functional testing
  - All preset switches work smoothly
  - Scheduling transitions happen correctly
  - Analytics accumulate correctly
  - No crashes after 30 min of active use

**Deliverable**: Production-ready app ready for personal use or App Store

---

---

## iOS Limitations

**⚠️ Critical Architectural Differences from Android**

### System-Wide Overlay: NOT POSSIBLE
- **Android**: `SYSTEM_ALERT_WINDOW` permission allows overlay above all apps
- **iOS**: Apps are sandboxed; cannot draw outside their window hierarchy
- **Reality**: Red overlay appears **only while user is in RedScreenFilter app**
- **When user switches apps**: Overlay disappears immediately (Safari, Camera, Messages, etc.)
- **App Store Policy**: Explicitly prohibits interfering with other apps' UI

### Foreground App Detection: NOT POSSIBLE  
- **Android**: `UsageStatsManager` + `PACKAGE_USAGE_STATS` permission enables app exemptions
- **iOS**: No public API to detect which app is currently active
- **Reality**: Cannot distinguish between apps or hide overlay for specific apps
- **Feature Impact**: App exemption feature impossible on iOS

### Background Overlay: NOT POSSIBLE
- **Android**: Foreground service maintains overlay while app backgrounded
- **iOS**: All UI rendering suspended when app backgrounded
- **Reality**: Overlay only exists while user is actively in the app
- **Background Tasks**: Can run scheduled work (notifications, API calls) but **cannot** display visual overlays

### Programmatic System Settings: NOT POSSIBLE
- **What works**: User can manually enable iOS built-in Color Filters (Settings → Accessibility → Display & Text Size → Color Filters)
- **What doesn't work**: App **cannot** automatically toggle system settings
- **Workaround**: Provide Siri Shortcuts that users manually add to Control Center (requires user tap each time)
- **Our approach**: Educational guide directing users to native iOS accessibility features

### App Groups Limitations
- Uses standard UserDefaults (not encrypted like EncryptedSharedPreferences on Android)
- Cannot share overlay window state across processes
- Useful only for widget timeline data exchange

---

## Implementation Timeline (Revised)

| Phase | % | Tasks | Est. Time | Priority | Notes |
|-------|---|-------|-----------|----------|-------|
| 0-5% | 5 | Xcode project creation | 20 min | CRITICAL | - |
| 5-10% | 5 | Project structure & entitlements | 1 hour | CRITICAL | Simpler setup (no background modes) |
| 10-15% | 5 | In-app overlay implementation | 2 hours | CRITICAL | Overlay works only within app |
| 15-20% | 5 | ViewModel & settings UI | 1.5 hours | CRITICAL | - |
| 20-25% | 5 | Time-based scheduling logic | 1 hour | HIGH | Sends notifications at scheduled times |
| 25-35% | 10 | Location & sunset/sunrise scheduling | 3 hours | HIGH | Uses CoreLocation + astronomy calculations |
| 35-40% | 5 | Preset system (Work, Gaming, Movie, Sleep) | 1.5 hours | HIGH | - |
| 40-50% | 10 | Color variants & accessibility presets | 2 hours | MEDIUM | 6 color variants for different needs |
| 50-55% | 5 | Battery awareness & notifications | 1 hour | MEDIUM | Warns user on low battery |
| 55-60% | 5 | Ambient light sensing | 1.5 hours | MEDIUM | Uses AVCaptureDevice or ARKit |
| 60-65% | 5 | 20-20-20 eye strain reminders | 1.5 hours | MEDIUM | Scheduled notifications |
| 65-70% | 5 | Lock screen widget | 1.5 hours | LOW | Status display + open app |
| 70-75% | 5 | Siri Shortcuts for system-wide control | 2 hours | **HIGH** | Shortcuts + iOS Color Filters |
| 75-80% | 5 | Optional: Voice commands | 1 hour | LOW | Voice control for in-app features |
| 80-85% | 5 | Core Data setup & event logging | 2 hours | MEDIUM | Local analytics database |
| 85-90% | 5 | Analytics dashboard UI | 2 hours | MEDIUM | Usage charts & statistics |
| 90-100% | 10 | Polish, testing, accessibility | 3 hours | CRITICAL | Dark mode, VoiceOver, performance |
| **TOTAL** | **100%** | **17 phases** | **~29 hours** | — | Includes system-wide filter control via Shortcuts |

---

## Key Milestones & Architecture Decisions

### Milestone 1: MVP (10-20%)
- ✅ In-app red overlay working
- ✅ SwiftUI UI responsive  
- ✅ Toggle and opacity control working
- ✅ Settings persist via UserDefaults
- **Go/No-Go**: Can install, toggle overlay within app, adjust opacity
- **Important**: Overlay disappears when user leaves the app (this is expected iOS behavior)

### Milestone 2: Scheduling & Location (20-35%)
- ✅ Time-based scheduling sends notifications at scheduled times
- ✅ Location services work, sunset/sunrise calculated
- ✅ Notifications appear at schedule time
- **Go/No-Go**: Scheduling logic works, reminders functional

### Milestone 3: Customization (35-50%)
- ✅ Multiple presets selectable
- ✅ Color variants display with visual preview
- ✅ Accessible for color-blind users
- **Go/No-Go**: User experience personalized

### Milestone 4: Smart Features (50-65%)
- ✅ Battery monitoring works, notifications sent on low battery
- ✅ Ambient light sensor reads lux values
- ✅ 20-20-20 reminders notify user
- **Go/No-Go**: Smart automation features working

### Milestone 5: System-Wide Control & Shortcuts (65-75%)
- ✅ Lock screen widget displays app status
- ✅ Siri Shortcuts launcher created for iOS native Color Filters
- ✅ Users can add shortcuts to Control Center
- ✅ System-wide red filtering available via shortcuts
- **Go/No-Go**: System-wide color filtering working
- **Capability**: Users get one-tap system-wide red filter from Control Center (via iOS native Color Filters)

### Milestone 6: Analytics & Polish (75-100%)
- ✅ Core Data logging usage events
- ✅ Analytics dashboard displaying stats
- ✅ In-app voice commands (optional, for overlay control)
- ✅ All permissions requested gracefully
- ✅ Accessibility (VoiceOver) tested
- ✅ App ready for production
- **Go/No-Go**: Ready for App Store submission

---

## Architecture Patterns

### MVVM (Model-View-ViewModel)
- **Models**: `OverlaySettings`, `UsageEvent`, `PresetProfile`
- **ViewModels**: `OverlayViewModel`, `AnalyticsViewModel`
- **Views**: SwiftUI views bound to ViewModels via `@ObservedObject`

### Reactive Programming with Combine
- `@Published` properties for state
- Subscribers for observing changes
- `.sink()` and `.assign()` for binding
- Custom publishers for sensor data (battery, light)

### Service Layer Pattern
- `PreferencesManager`: Handles persistence (standard UserDefaults)
- `SchedulingService`: Business logic for scheduling, sends notifications
- `OverlayView`: Manages in-app red overlay (no system-wide overlay possible)
- `AnalyticsService`: Data aggregation

### In-App Overlay vs System-Wide Filtering

**In-App Overlay** (SwiftUI ZStack)
- Overlay implemented as SwiftUI ZStack layer
- RED FILTER: Only visible while user is IN RedScreenFilter app
- Disappears immediately when user switches to other apps (iOS sandbox)
- Use case: Reading content within the app
- Advantage: Real-time opacity adjustments, color previews
- Limitation: Not persistent across apps

**System-Wide Filtering via Siri Shortcuts** (iOS Native Color Filters)
- RED FILTER: Implemented using iOS's built-in accessibility Color Filters
- Triggered by Siri Shortcuts launcher in our app
- System-wide: Appears over ALL apps once enabled
- User adds shortcut to Control Center for one-tap access
- Use case: Eye protection across entire device usage
- Advantage: True system-wide persistence
- Limitation: Requires user to tap shortcut/Control Center button

**Result**: Users get BOTH capabilities
1. In-app overlay for quick preview/testing
2. System-wide red filter via shortcuts for actual use

### Siri Shortcuts for System Accessibility
- **iOS Color Filters**: Built-in accessibility feature (system-wide)
- **Shortcut Launcher**: Our app creates shortcuts that trigger iOS Color Filters
- **User Workflow**: 
  1. User taps "Enable System Red Filter" in our app
  2. Shortcut runs → Opens iOS Settings → Navigates to Color Filters → Enables filter → Returns to app
  3. Red filter now active system-wide
  4. User can add shortcut to Control Center for faster access
- **Automation**: iOS Shortcuts app supports time-based automation ("At 9 PM, run shortcut")
- **No Custom Overlay Required**: Leverages iOS native capabilities instead

---

## Key Dependencies & Frameworks

**Built-in (No CocoaPods needed)**:
- SwiftUI (UI framework)
- Combine (reactive programming)
- CoreLocation (GPS for sunset/sunrise)
- UserNotifications (reminders)
- BackgroundTasks (scheduling)
- WidgetKit (lock screen widget)
- CoreData (database)
- AVFoundation (light sensing)
- UserNotifications (scheduled reminders)

**Encrypted Storage Not Available**
- iOS doesn't provide encrypted UserDefaults equivalent like Android's EncryptedSharedPreferences
- Consider using Keychain for sensitive data (optional)
- For this app's non-sensitive settings, standard UserDefaults is appropriate

**Optional External**:
```swift
// Charts for analytics visualization (same as Android)
.package(url: "https://github.com/danielgindi/Charts.git", from: "4.0.0")

// Or simpler alternative
.package(url: "https://github.com/weichsel/SwiftUICharts.git", from: "1.0.0")
```

---

## Testing Strategy (Manual in Xcode + Device)

**What to test at each milestone:**

1. **10-30%**: Red overlay appears, toggle works, opacity slider changes color intensity
2. **30-50%**: Scheduling turns overlay on/off at correct times, persists after backgrounding
3. **50-65%**: Presets switch colors/opacity correctly, color variants display properly
4. **65-80%**: Battery monitor reduces opacity, light sensor adjusts opacity, notifications arrive
5. **80-92%**: Widget toggles overlay, Siri commands work, exempted apps don't show overlay
6. **92-100%**: Analytics accumulate, no crashes, smooth animations, permission prompts work

---

## Debugging Tips

**Xcode Console**:
- Print overlay window hierarchy: `po UIApplication.shared.windows`
- Check notification requests: `po UNUserNotificationCenter.current().getPendingNotificationRequests { print($0) }`
- Monitor background tasks: Enable scheme > Scheme > Run > Options > Allow execution of arbitrary Swift code

**Device/Simulator Testing**:
- Settings > Developer Settings > Low Power Mode (simulate battery warning)
- Settings > Accessibility > Display > Color Filters (compare with our overlay)
- Settings > Developer > Low Light Simulation (if available)

---

## File-by-File Checklist

**Phase 0-10%**:
- [ ] `RedScreenFilterApp.swift` - app entry + scene config
- [ ] `Info.plist` - permissions + entitlements
- [ ] Project settings - App Groups + Background Modes

**Phase 10-30%**:
- [ ] `OverlayWindowManager.swift` - core overlay creation
- [ ] `OverlayViewController.swift` - red view controller
- [ ] `PreferencesManager.swift` - UserDefaults wrapper
- [ ] `OverlaySettings.swift` - data model
- [ ] `OverlayViewModel.swift` - MVVM state
- [ ] `MainView.swift` - SwiftUI root
- [ ] `SettingsView.swift` - settings UI

**Phase 30-50%**:
- [ ] `SchedulingService.swift` - schedule logic
- [ ] `BackgroundScheduleTask.swift` - background task
- [ ] `AppDelegate.swift` - lifecycle + task registration
- [ ] `LocationCalculationService.swift` - GPS access
- [ ] `SunriseCalculator.swift` - solar math

**Phase 50-65%**:
- [ ] `PresetsManager.swift` - preset CRUD
- [ ] `PresetProfile.swift` - preset model
- [ ] Color variant UI components

**Phase 65-80%**:
- [ ] `BatteryMonitor.swift` - battery state
- [ ] `LightSensorManager.swift` - ambient light
- [ ] `EyeStrainReminderService.swift` - notifications

**Phase 80-92%**:
- [ ] Widget Extension target + `Widget.swift`
- [ ] `Intents.intentdefinition` - Siri intents
- [ ] `ExemptedAppsManager.swift` - app exemptions

**Phase 92-100%**:
- [ ] `RedScreenFilter.xcdatamodeld` - Core Data model
- [ ] `CoreDataStack.swift` - database setup
- [ ] `AnalyticsService.swift` - data aggregation
- [ ] `AnalyticsView.swift` - UI for analytics
- [ ] Final polish & testing

---

## Next Steps

1. **Start Phase 0-5%**: Create Xcode project in `/ios/RedScreenFilter`
2. **Proceed sequentially** through phases
3. **Test on simulator first**, then physical device after Phase 1
4. **Check off tasks** as completed
5. **Iterate based on real-device testing** feedback

Ready to start? Begin with Phase 0-5% (Xcode project creation).
