# iOS Implementation Plan

Complete step-by-step implementation roadmap for Red Screen Filter iOS app (Swift/SwiftUI). Phases organized from 0-100% completion.

## Project Overview

**Target**: Full-featured red screen overlay app with health-focused features
**Language**: Swift 5.9+
**Framework**: SwiftUI + Combine
**Min OS**: iOS 14.0+
**Architecture**: MVVM with reactive state management
**App Groups**: group.com.redscreenfilter (for background persistence)

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

### Phase 10-30% - Core Overlay Window Layer
**Objective**: Functional red overlay that can be toggled on/off using UIWindow

#### 10-15% - UIWindow Overlay Base
- [ ] Create `OverlayWindowManager.swift`
  - Class manages overlay UIWindow lifecycle
  - Creates `UIWindow` with `UIWindowLevelStatusBar + 1` window level
  - Overlay view controller with red background view
  - Methods:
    - `showOverlay(opacity: Float)`
    - `hideOverlay()`
    - `updateOpacity(Float)`
    - Properties: `isVisible: Bool`, `currentOpacity: Float`

- [ ] Create `OverlayViewController.swift`
  - UIViewController subclass
  - Root view is red rectangle view
  - `isUserInteractionEnabled = false` on overlay view (passes touches through)
  - Updates color with alpha based on opacity parameter
  - Color: `UIColor(red: 1.0, green: 0, blue: 0, alpha: opacity)`

- [ ] Ensure overlay appears across all screens
  - Set window's `windowScene`
  - Make window `keyWindow` and visible
  - Correct z-ordering

**Deliverable**: Red overlay appears on screen when toggled

#### 15-20% - Reactive ViewModel & PreferencesManager
- [ ] Create `PreferencesManager.swift`
  - Uses UserDefaults with App Groups container
  - Methods:
    - `setOverlayEnabled(_ enabled: Bool)`
    - `isOverlayEnabled() -> Bool`
    - `setOpacity(_ opacity: Float)`
    - `getOpacity() -> Float`
    - Observes changes for reactive updates
  - Implement as singleton
  - Use `@Published` properties if Combine integration

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
  - Properties: `isEnabled`, `opacity`, `settings`
  - Methods: `toggleOverlay()`, `updateOpacity(Float)`
  - Observes PreferencesManager changes
  - Coordinates with OverlayWindowManager

**Deliverable**: Reactive state management working, preferences persist

#### 20-30% - SwiftUI MainView & Settings
- [ ] Create `MainView.swift` (SwiftUI root view)
  - Tab-based navigation (or NavigationStack)
  - Tab 1: Main overlay control
  - Tab 2: Settings
  - Tab 3: Analytics (placeholder)

- [ ] Create main overlay control view
  - Large toggle button (on/off)
  - OpenActivity slider for opacity (0-100%)
  - Current opacity display
  - Quick preset buttons
  - Visual feedback

- [ ] Create `SettingsView.swift`
  - Basic settings form
  - Toggle for overlay enable/disable
  - Opacity slider duplication
  - Placeholder sections for future features
  - Linked to OverlayViewModel

- [ ] Implement app lifecycle
  - `@UIApplicationDelegateAdaptor` in RedScreenFilterApp
  - Initialize OverlayWindowManager on app launch
  - Restore overlay state on comeback from background

**Deliverable**: Full UI for toggle and settings, all connected to state

---

### Phase 30-50% - Background Scheduling Engine
**Objective**: Automatic on/off based on time, with persistence across app backgrounding

#### 30-35% - Basic Time-Based Scheduling
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

**Deliverable**: Overlay schedules based on device location's sunset/sunrise

---

### Phase 50-65% - Customization & Presets
**Objective**: Multiple screen modes and color variants

#### 50-55% - Activity Presets System
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

#### 55-65% - Color Blindness Presets
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

### Phase 65-80% - Smart Features with Sensors & Battery
**Objective**: Battery awareness, light sensing, health reminders

#### 65-70% - Battery Awareness
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

**Deliverable**: Overlay reduces intensity automatically on low battery

#### 70-75% - Ambient Light Sensing with AVFoundation
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

**Deliverable**: Overlay auto-adjusts based on ambient light

#### 75-80% - 20-20-20 Eye Strain Reminders
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

### Phase 80-92% - User Experience & Accessibility
**Objective**: Quick access, voice control, widgets

#### 80-85% - Lock Screen Widget with WidgetKit
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

**Deliverable**: Lock screen widget shows status and allows quick toggle

#### 85-90% - Siri Shortcuts Integration
- [ ] Create Siri Shortcuts via Intent Framework
  - Define custom intents in `Intents.intentdefinition` file
  - Intent: "Toggle Red Screen Filter"
  - Intent: "Set Red Filter Opacity"
  - Intent: "Apply Red Filter Preset"

- [ ] Implement intent handler in main app
  - Handle intent execution
  - Update overlay state
  - Return confirmation message

- [ ] Create app shortcuts (iOS 17.1+)
  - Available through Settings > Siri & Search
  - "Turn on red screen filter"
  - "Set opacity to 50%"
  - "Apply sleep mode"

- [ ] Test with voice
  - "Hey Siri, turn on red screen filter"
  - "Hey Siri, red screen to 80%"

**Deliverable**: Users can control via Siri voice commands

#### 90-92% - Selective App Exemptions
- [ ] Create `ExemptedAppsManager.swift`
  - Maintain list of bundle IDs to exempt
  - Serialize to JSON in UserDefaults (App Groups)
  - CRUD operations

- [ ] Create exemption UI
  - Query installed apps via `LSApplicationWorkspace` (limited in iOS)
  - Alternative: Hardcoded list of common apps (Camera, Photos, Banking apps)
  - RecyclerView-like UI with toggle switches
  - Search functionality to filter apps

- [ ] Detect foreground app (limited iOS options)
  - Use `UsageStatsManager` equivalent (Scene Delegate + UIWindow observation)
  - Monitor active scene to detect app changes
  - iOS limitations: Cannot access other apps' lifecycles directly
  - Fallback: Manual app blacklist

- [ ] Integrate with overlay service
  - Before showing overlay, check if current app is exempted
  - Skip overlay display for exempted apps
  - Resume overlay after app changes

**Deliverable**: Overlay can be disabled for specific apps

---

### Phase 92-98% - Analytics & Persistence
**Objective**: Usage tracking with local database, insights dashboard

#### 92-95% - Core Data Database Setup
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

#### 95-98% - Analytics Dashboard UI
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

- [ ] Verify background behavior
  - Kill app from app switcher
  - Verify overlay persists via App Groups
  - Verify scheduling works without app running
  - Restart device and check state restoration

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

## Implementation Timeline

| Phase | % | Tasks | Est. Time | Priority |
|-------|---|-------|-----------|----------|
| 0-5% | 5 | Xcode project creation | 20 min | CRITICAL |
| 5-10% | 5 | Structure & entitlements | 1.5 hours | CRITICAL |
| 10-15% | 5 | UIWindow overlay base | 2 hours | CRITICAL |
| 15-20% | 5 | ViewModel & preferences | 1.5 hours | CRITICAL |
| 20-30% | 10 | SwiftUI MainView & settings | 2.5 hours | CRITICAL |
| 30-35% | 5 | Basic scheduling logic | 1 hour | HIGH |
| 35-40% | 5 | Background task scheduling | 1.5 hours | HIGH |
| 40-50% | 10 | Sunrise/sunset with CoreLocation | 3 hours | HIGH |
| 50-55% | 5 | Preset system | 1.5 hours | HIGH |
| 55-65% | 10 | Color blindness variants | 2 hours | MEDIUM |
| 65-70% | 5 | Battery awareness | 1 hour | MEDIUM |
| 70-75% | 5 | Ambient light sensing | 1.5 hours | MEDIUM |
| 75-80% | 5 | 20-20-20 reminders | 1.5 hours | MEDIUM |
| 80-85% | 5 | Lock screen widget | 1.5 hours | LOW |
| 85-90% | 5 | Siri Shortcuts | 1.5 hours | LOW |
| 90-92% | 2 | App exemptions | 1.5 hours | MEDIUM |
| 92-95% | 3 | Core Data setup & logging | 2 hours | LOW |
| 95-98% | 3 | Analytics dashboard UI | 2 hours | LOW |
| 98-100% | 2 | Permissions & final polish | 1.5 hours | CRITICAL |
| **TOTAL** | **100%** | **32 tasks** | **~31 hours** | — |

---

## Key Milestones & Architecture Decisions

### Milestone 1: MVP (10-30%)
- ✅ UIWindow overlay appearing
- ✅ SwiftUI UI responsive
- ✅ Toggle and opacity control working
- ✅ Settings persist via UserDefaults
- **Go/No-Go**: Can install, toggle overlay, adjust opacity

### Milestone 2: Background Scheduling (30-50%)
- ✅ Time-based scheduling works
- ✅ BGProcessingTask scheduling enabled
- ✅ Sunrise/sunset auto-calculated
- **Go/No-Go**: Overlay persists after app backgrounding, auto-toggles

### Milestone 3: Customization (50-65%)
- ✅ Multiple presets switchable
- ✅ Color blindness variants display correctly
- ✅ All variants apply properly
- **Go/No-Go**: User experience personalized

### Milestone 4: Smart Features (65-80%)
- ✅ Battery monitoring active, opacity reduced on low battery
- ✅ Light sensor reading illuminance correctly
- ✅ 20-20-20 reminders notifying user
- **Go/No-Go**: Smart automation features working

### Milestone 5: Widgets & Voice (80-92%)
- ✅ Lock screen widget displaying and toggling
- ✅ Siri Shortcuts callable and functional
- ✅ App exemptions list populated
- **Go/No-Go**: Quick access methods working

### Milestone 6: Analytics & Final (92-100%)
- ✅ Core Data logging usage events
- ✅ Analytics dashboard displaying stats
- ✅ All permissions requested gracefully
- **Go/No-Go**: Ready for use

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
- `PreferencesManager`: Handles persistence
- `SchedulingService`: Business logic for scheduling
- `OverlayWindowManager`: Overlay UI management
- `AnalyticsService`: Data aggregation

### App Groups for Background Persistence
- Shared container: `group.com.redscreenfilter`
- UserDefaults reads/writes to shared container
- CoreData store in App Groups directory
- Allows background tasks to access app state

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

**Optional External**:
```swift
// Charts for analytics visualization
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
