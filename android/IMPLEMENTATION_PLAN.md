# Android Implementation Plan

Complete step-by-step implementation roadmap for Red Screen Filter Android app (Kotlin). Phases organized from 0-100% completion.

## Project Overview

**Target**: Full-featured red screen overlay app with health-focused features
**Language**: Kotlin
**Min API**: 28 (Android 9.0)
**Target API**: 34+ (Android 14+)
**Architecture**: MVVM with Repository pattern

---

## Phase Breakdown

### Phase 0-10% - Project Setup & Core Foundation
**Objective**: Initialize project structure, dependencies, and base architecture

#### 0-5% - Android Studio Project Creation ✅
- [x] Create new Android project (Empty Activity template)
- [x] Project name: RedScreenFilter
- [x] Package: com.redscreenfilter
- [x] Kotlin selected
- [x] API 28 as minimum
- [x] Location: `/android` folder

**Deliverable**: Runnable empty app on emulator/device ✅

#### 5-10% - Gradle Dependencies & Project Structure ✅
- [x] Update `build.gradle.kts` (Project level)
  - Kotlin version: 1.9+ ✅
  - Android Gradle Plugin: 8.0+ ✅
  
- [x] Update `build.gradle.kts` (App level)
  - **Core Android**: androidx.core, androidx.appcompat, androidx.lifecycle ✅
  - **UI**: Material Design 3 (XML-based) ✅
  - **Background**: androidx.work (WorkManager) ✅
  - **Storage**: androidx.datastore + androidx.security-crypto ✅
  - **Database**: androidx.room ✅
  - **Serialization**: Gson ✅
  - **Testing**: JUnit (included by default)

- [x] Create directory structure:
  ```
  app/src/main/java/com/redscreenfilter/
  ├── MainActivity.kt ✅
  ├── service/
  │   └── RedOverlayService.kt ✅
  ├── ui/
  │   └── MainActivity.kt ✅
  ├── data/
  │   ├── PreferencesManager.kt ✅
  │   └── OverlaySettings.kt ✅
  ├── utils/
  │   ├── Constants.kt ✅
  │   └── Extensions.kt ✅
  └── receiver/
      └── BootCompletedReceiver.kt ✅
  ```

**Deliverable**: Project compiles with all dependencies resolved ✅

---

### Phase 10-30% - Core Overlay Service
**Objective**: Functional red overlay that can be toggled on/off

#### 10-15% - WindowManager Overlay Base ✅
- [x] Create `RedOverlayService.kt` (Foreground Service)
  - Extends `Service` ✅
  - Handles overlay window creation with `WindowManager` ✅
  - Implements lifecycle (onCreate, onStartCommand, onDestroy) ✅
  - Uses `WindowManager.LayoutParams` for overlay positioning ✅
  - Set window type to `TYPE_APPLICATION_OVERLAY` (API 26+, fallback for older) ✅
  - Foreground notification with NotificationChannel ✅
  - ACTION_UPDATE_OPACITY intent handling ✅

- [x] Create overlay view (`OverlayView.kt`)
  - Custom View extending `View` ✅
  - Draws red rectangle with configurable opacity ✅
  - Color: `Color.RED` (rgb(255, 0, 0)) with alpha channel ✅
  - Touch events pass through (FLAG_NOT_TOUCHABLE) ✅
  - setOpacity() and setOverlayColor() methods ✅

- [x] Add permissions to `AndroidManifest.xml`
  - `SYSTEM_ALERT_WINDOW` permission ✅
  - `FOREGROUND_SERVICE` permission ✅
  - `FOREGROUND_SERVICE_SYSTEM_EXEMPTED` permission ✅
  - `POST_NOTIFICATIONS` permission (Android 13+) ✅
  - Service registered with foregroundServiceType="systemExempted" ✅

**Deliverable**: Service can be started, red overlay appears on screen ✅

#### 15-20% - Settings Persistence Layer ✅
- [x] Create `PreferencesManager.kt`
  - Wraps EncryptedSharedPreferences ✅
  - Singleton pattern with getInstance(Context) ✅
  - MasterKey with AES256_GCM encryption ✅
  - Core methods:
    - `setOverlayEnabled(Boolean)` / `isOverlayEnabled()` ✅
    - `setOpacity(Float)` / `getOpacity()` with clamping ✅
  - Scheduling methods:
    - `setScheduleEnabled()` / `isScheduleEnabled()` ✅
    - `setScheduleStartTime()` / `getScheduleStartTime()` ✅
    - `setScheduleEndTime()` / `getScheduleEndTime()` ✅
  - Smart features methods:
    - `setUseAmbientLight()` / `getUseAmbientLight()` ✅
    - `setUseLocationSchedule()` / `getUseLocationSchedule()` ✅
    - `setColorVariant()` / `getColorVariant()` ✅
    - `setBatteryOptimizationEnabled()` / `getBatteryOptimizationEnabled()` ✅
  - Bulk operations:
    - `getSettings()` - returns complete OverlaySettings object ✅
    - `saveSettings(OverlaySettings)` - saves all settings at once ✅
    - `clearAll()` - clears all preferences ✅

- [x] Data class `OverlaySettings.kt` (already created in Phase 5-10%)
  - All required fields with defaults ✅
  - `isEnabled`, `opacity`, `scheduleEnabled` ✅
  - `scheduleStartTime`, `scheduleEndTime` ✅
  - `useAmbientLight`, `useLocationSchedule`, `colorVariant` ✅

**Deliverable**: Settings persist across app closures ✅

#### 20-30% - MainActivity & Basic UI ✅
- [x] Create `MainActivity.kt`
  - Display toggle button (on/off overlay) ✅
  - Display opacity slider (0-100%) ✅
  - Request `SYSTEM_ALERT_WINDOW` permission at runtime ✅
  - Start/stop `RedOverlayService` ✅
  - Real-time opacity updates via intent ✅
  - Load/save settings with PreferencesManager ✅
  - Permission status checking on resume ✅
  
- [x] Created layout with Material Design 3 (XML-based) ✅
  
- [x] Add layout file `activity_main.xml`
  - MaterialCardView containers with elevation ✅
  - SwitchMaterial for toggle with title/subtitle ✅
  - SeekBar for opacity (0-100) with min/max labels ✅
  - TextView showing current opacity percentage ✅
  - Permission card (appears when permission not granted) ✅
  - MaterialButton for permission request ✅
  - Info text at bottom ✅
  - 24dp padding, 12dp card radius, Material Design 3 styling ✅

**Deliverable**: Can toggle overlay and adjust opacity from UI ✅

---

### Phase 30-50% - Scheduling Engine
**Objective**: Automatic on/off based on time or sunrise/sunset

#### 30-35% - Basic Time-Based Scheduling ✅
- [x] Create `SchedulingManager.kt`
  - Methods:
    - `getScheduledState(): Boolean` - returns if overlay should be active
    - `isScheduleEnabled(): Boolean`
    - `setSchedule(startTime, endTime)`
    - Check if current time falls within scheduled window
    - Handle day boundary crossings (e.g., 10 PM to 7 AM)

- [x] Create scheduling UI fragment/screen
  - Time pickers for start/end times
  - Toggle to enable/disable scheduling
  - Display formatted times

**Deliverable**: Overlay state follows scheduled times ✅

#### 35-40% - WorkManager for Background Scheduling ✅
- [x] Create `ScheduleWorker.kt` (Extends `CoroutineWorker`)
  - Runs periodically (every 15 minutes)
  - Calls `SchedulingManager.getScheduledState()`
  - Updates overlay state if needed
  - Resilient to Doze mode

- [x] Register periodic WorkRequest
  - Use `PeriodicWorkRequestBuilder`
  - 15-minute interval with flexibility
  - Ensure runs across device sleep

**Deliverable**: Overlay automatically updates every 15 minutes per schedule ✅

#### 40-50% - Sunrise/Sunset Scheduling ✅
- [x] Add location permission
  ```xml
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  ```

- [x] Create `LocationManager.kt`
  - Request user's latitude/longitude
  - Calculate sunset time using solar equation (search: "java sunset calculator")
  - Cache location, update every 6 hours

- [x] Create `SunriseCalculator.kt` (Utility)
  - Implement SPA (Solar Position Algorithm) or use library
  - Input: lat/lon, date → Output: sunrise/sunset times (Epoch milliseconds)

- [x] Extend `SchedulingManager`
  - Add `useLocationBasedSchedule: Boolean`
  - If enabled, use sunset+offset as start time
  - Sunrise as end time

- [x] Add UI for location scheduling
  - Toggle: "Use sunset/sunrise scheduling"
  - Offset slider: -60 to +60 minutes
  - Display calculated times

**Deliverable**: Overlay can schedule based on device location's sunrise/sunset ✅

---

### Phase 50-65% - Customization & Presets
**Objective**: Multiple screen modes and color variants

#### 50-55% - Activity Presets
- [ ] Create preset data model `PresetProfile.kt`
  ```kt
  data class PresetProfile(
      val name: String,
      val opacity: Float,
      val red: Float = 1.0f,
      val green: Float = 0f,
      val blue: Float = 0f,
      val description: String
  )
  ```

- [ ] Create preset list with 5 defaults:
  - Work (40% opacity, pure red)
  - Gaming (30% opacity, pure red)
  - Movie (20% opacity, pure red)
  - Sleep (80% opacity, pure red)
  - Custom (user-editable)

- [ ] Store presets in SharedPreferences
  - Serialize to JSON using Gson/Kotlinx.serialization
  - Load/save/delete preset methods

- [ ] Add preset UI
  - Horizontal scrollable preset buttons/cards
  - Tap to apply preset instantly
  - Long-press to edit/delete

**Deliverable**: Users can switch between preset profiles

#### 55-65% - Color Blindness Presets
- [ ] Create color variant system
  - **Red Standard**: RGB(255, 0, 0)
  - **Red-Orange**: RGB(255, 100, 0) - for protanopia
  - **Red-Pink**: RGB(255, 0, 100) - for deuteranopia
  - **High Contrast Red**: Brighter, higher opacity floor

- [ ] Add ColorVariant enum
  ```kt
  enum class ColorVariant {
      RED_STANDARD,
      RED_ORANGE,
      RED_PINK,
      HIGH_CONTRAST
  }
  ```

- [ ] Create color picker UI
  - Radio button group or dropdown
  - Preview color swatch
  - Save selected variant to preferences

- [ ] Update `OverlayView` to use variant color
  - Read from `PreferencesManager`
  - Apply in real-time

**Deliverable**: Users can select color blindness variants

---

### Phase 65-80% - Smart Features
**Objective**: Battery awareness, light sensing, and intelligent automation

#### 65-70% - Battery Awareness
- [ ] Create `BatteryManager.kt`
  - Register BroadcastReceiver for battery changes
  - Detect low battery (< 20%) and critical (< 10%)
  - Store battery state

- [ ] Extend `PreferencesManager`
  - `setBatteryOptimizationEnabled(Boolean)`
  - `getBatteryOptimizationEnabled(): Boolean`

- [ ] Integrate with `RedOverlayService`
  - When battery < 20%, reduce opacity by 30%
  - Visual indicator in notification
  - Resume normal opacity when charging detected

**Deliverable**: Overlay reduces intensity on low battery

#### 70-75% - Ambient Light Sensing
- [ ] Create `LightSensorManager.kt`
  - Register `SensorManager` listener for `TYPE_LIGHT`
  - Callback updates lux reading every 5-10 seconds
  - Smoothing algorithm to prevent jitter (moving average)

- [ ] Create illuminance-to-opacity mapping
  ```kt
  when(lux) {
      in 0..50 -> 0.8f      // Dark: high opacity
      in 51..500 -> 0.6f    // Dim: medium opacity
      in 501..3000 -> 0.4f  // Normal: lower opacity
      else -> 0.2f          // Bright: minimal opacity
  }
  ```

- [ ] Add UI settings
  - Toggle: "Auto-adjust brightness"
  - Sensitivity slider: Low/Medium/High
  - Manual override to lock current opacity

**Deliverable**: Overlay auto-adjusts based on room light

#### 75-80% - 20-20-20 Eye Strain Reminders
- [ ] Create `EyeStrainReminder.kt` Worker
  - Runs every 20 minutes
  - Sends local notification
  - Message: "Look away for 20 seconds"
  - Sound/vibration customizable

- [ ] Create notification configuration
  - Notification channel: "Health Reminders"
  - Priority: DEFAULT
  - Sound: Subtle tone (get from system sounds)

- [ ] Add UI settings
  - Toggle: "20-20-20 Reminders"
  - Notification style: Sound / Vibration / Silent
  - Pause during video calls (detect using `TelecomManager`)

**Deliverable**: Reminders notify user every 20 minutes

---

### Phase 80-92% - User Experience & Accessibility
**Objective**: Quick access, voice control, widgets

#### 80-85% - Quick Settings Tile
- [ ] Create `OverlayQuickSettingsTile.kt` (extends `TileService`)
  - Extends `android.service.quicksettings.TileService`
  - Toggle overlay state on tap
  - Update tile state (Active/Inactive) with icon change
  - Long-click opens app settings

- [ ] Add to manifest
  ```xml
  <service
      android:name=".receiver.OverlayQuickSettingsTile"
      android:permission="android.permission.BIND_QUICK_SETTINGS_TILE" />
  ```

**Deliverable**: Quick toggle from Quick Settings panel

#### 85-90% - Voice Commands with Google Assistant
- [ ] Create voice command intents
  - Action: `android.intent.action.MAIN`
  - Enable via Google Assistant integration
  - Phrases:
    - "Turn on red screen filter"
    - "Turn off red screen filter"
    - "Set red filter to 50%"

- [ ] Create `VoiceCommandReceiver.kt`
  - Broadcast receiver for voice intents
  - Parse intent extra for command (on/off/opacity value)
  - Update overlay accordingly

- [ ] Register in manifest for voice commands

**Deliverable**: Can control app via voice with Google Assistant

#### 90-92% - Selective App Exemptions
- [ ] Create `ExemptedAppsManager.kt`
  - Maintain list of package names to exempt
  - Serialize to JSON in SharedPreferences

- [ ] Create app exemption UI
  - RecyclerView listing installed apps
  - Checkboxes to toggle exemption
  - Search field to filter apps
  - Load apps with icon + name

- [ ] Integration with overlay service
  - Detect current foreground app (use `UsageStatsManager`)
  - Skip overlay if app is exempted
  - Listen for app changes

**Deliverable**: Overlay hides for specified apps (e.g., camera)

---

### Phase 92-98% - Analytics & Insights
**Objective**: Usage tracking and user engagement

#### 92-95% - Usage Tracking & SQLite Database
- [ ] Create Room database setup
  - Add dependency: androidx.room
  - Create `UsageEvent.kt` entity
    ```kt
    @Entity
    data class UsageEvent(
        @PrimaryKey val id: Int = 0,
        val timestamp: Long,
        val overlayEnabled: Boolean,
        val opacity: Float,
        val preset: String
    )
    ```
  - Create `UsageDao.kt` for queries
  - Create `UsageDatabase.kt`

- [ ] Create `AnalyticsService.kt`
  - Log events: overlay toggled, settings changed, preset applied
  - Calculate daily/weekly usage time
  - Query streak (consecutive usage days)

- [ ] Create analytics repository
  - Wrapper around database for clean data access

**Deliverable**: All usage tracked in local database

#### 95-98% - Analytics Dashboard UI
- [ ] Create `AnalyticsFragment.kt` / Dashboard screen
  - Tabs: Today / Week / Month / All Time
  - Display stats:
    - Total overlay usage time (formatted HH:MM)
    - Average opacity used
    - Most used preset
    - Current usage streak (days)
  - Show with charts (consider: MPAndroidChart library or simple progress bars)

- [ ] Add navigation to dashboard
  - Bottom navigation or tab layout
  - Accessible from MainActivity

**Deliverable**: Users can view usage analytics

---

### Phase 98-100% - Polish & Finalization
**Objective**: Final testing, UI refinement, performance optimization

#### 98-99% - Notifications & Permissions Handling
- [ ] Implement runtime permission requests
  - `SYSTEM_ALERT_WINDOW`: On app launch or settings
  - `ACCESS_FINE_LOCATION`: Conditional when user enables location scheduling
  - `POST_NOTIFICATIONS`: For Android 13+

- [ ] Create notification channel for persistent service notification
  - Channel name: "Red Screen Filter"
  - Importance: DEFAULT or LOW
  - Show persistent notification with action buttons

- [ ] Test permission flows on API 28+ devices

**Deliverable**: All permissions handled gracefully

#### 99-100% - Final Refinements
- [ ] Remove debug logs / add proper logging
- [ ] Optimize memory usage
  - Overlay view should not leak
  - Unregister sensors/listeners properly
- [ ] Test background behavior
  - Kill app from recents
  - Verify overlay persists
  - Test WorkManager scheduling after device restart
- [ ] Performance validation
  - Check CPU/memory usage profiling in Android Studio
  - Ensure battery impact minimal
  - Verify no ANRs (Application Not Responding)

- [ ] UI Polish
  - Test on multiple devices/screen sizes
  - Fix any layout issues
  - Ensure material design compliance

**Deliverable**: Production-ready app

---

## Implementation Timeline

| Phase | % | Tasks | Est. Time | Priority | Status |
|-------|---|-------|-----------|----------|--------|
| 0-5% | 5 | Project creation | 30 min | CRITICAL | ✅ DONE |
| 5-10% | 5 | Dependencies & structure | 1 hour | CRITICAL | ✅ DONE |
| 10-15% | 5 | Overlay service base | 2 hours | CRITICAL | ✅ DONE |
| 15-20% | 5 | Settings persistence | 1.5 hours | CRITICAL | ✅ DONE |
| 20-30% | 10 | Main UI & toggle | 2 hours | CRITICAL | ✅ DONE |
| 30-35% | 5 | Basic scheduling | 1.5 hours | HIGH | ✅ DONE |
| 35-40% | 5 | WorkManager integration | 1.5 hours | HIGH | ✅ DONE |
| 40-50% | 10 | Sunrise/sunset scheduling | 3 hours | HIGH | ✅ DONE |
| 50-55% | 5 | Preset system | 1.5 hours | HIGH | 🔄 NEXT |
| 55-65% | 10 | Color blindness variants | 2 hours | MEDIUM | ⏳ TODO |
| 65-70% | 5 | Battery awareness | 1 hour | MEDIUM | ⏳ TODO |
| 70-75% | 5 | Ambient light sensing | 1.5 hours | MEDIUM | ⏳ TODO |
| 75-80% | 5 | 20-20-20 reminders | 1.5 hours | MEDIUM | ⏳ TODO |
| 80-85% | 5 | Quick Settings tile | 1 hour | LOW | ⏳ TODO |
| 85-90% | 5 | Voice commands | 1.5 hours | LOW | ⏳ TODO |
| 90-92% | 2 | App exemptions | 1.5 hours | MEDIUM | ⏳ TODO |
| 92-95% | 3 | Database & analytics | 2 hours | LOW | ⏳ TODO |
| 95-98% | 3 | Analytics UI | 2 hours | LOW | ⏳ TODO |
| 98-100% | 2 | Final polish | 1 hour | CRITICAL | ⏳ TODO |
| **TOTAL** | **100%** | **32 tasks** | **~30 hours** | — | **50% Complete** |

---

## Key Dependencies & Milestones

### Milestone 1: MVP (10-30%)
- ✅ Running overlay with on/off toggle
- ✅ Opacity control
- ✅ Settings persist
- **Go/No-Go Decision**: Can install and use basic app

### Milestone 2: Smart Scheduling (30-50%)
- ✅ Time-based scheduling works
- ✅ Sunrise/sunset auto-detection
- **Go/No-Go Decision**: Can schedule automatically

### Milestone 3: Customization (50-65%)
- ⏳ Presets working (TODO)
- ⏳ Color variants available (TODO)
- **Go/No-Go Decision**: Users can personalize experience

### Milestone 4: Health & Features (65-80%)
- ⏳ Battery awareness implemented (TODO)
- ⏳ Light sensing works (TODO)
- ⏳ 20-20-20 reminders active (TODO)
- **Go/No-Go Decision**: Feature-complete for Phase 1 release

### Milestone 5: Polish & Release (80-100%)
- ⏳ All UI refinements done (TODO)
- ⏳ Performance optimized (TODO)
- ⏳ Ready for testing on device (TODO)
- **Go/No-Go Decision**: Release to Play Store (or personal use)

---

## Testing Strategy (You'll test in Android Studio + Device)

**What to test at each milestone:**
1. **10-30%**: App launches, overlay appears, toggle works
2. **30-50%**: Scheduling logic, time-based auto-on/off, sunrise/sunset calculation
3. **50-65%**: Presets switch correctly, colors display properly
4. **65-80%**: Battery drain low, light sensor responsive, reminders notify
5. **80-100%**: All features work on real device, no crashes, smooth UX

---

## Libraries & Dependencies Summary

```gradle
dependencies {
    // Core Android
    implementation 'androidx.core:core:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    
    // Compose or Material
    // Option A: Jetpack Compose
    implementation 'androidx.compose.ui:ui:1.6.0'
    implementation 'androidx.compose.material3:material3:1.2.0'
    // Option B: Material Design (XML)
    implementation 'com.google.android.material:material:1.11.0'
    
    // Background Tasks
    implementation 'androidx.work:work-runtime-ktx:2.9.0'
    
    // Storage
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    implementation 'androidx.datastore:datastore-preferences:1.0.0'
    
    // Permissions
    implementation 'androidx.activity:activity-ktx:1.8.1'
    
    // Database (for analytics)
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    
    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Charts (optional for analytics)
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

---

## Next Steps

1. **Start Phase 0-5%**: Create Android Studio project
2. **Proceed sequentially** through phases, testing at each milestone
3. **Check off tasks** as completed
4. **Test on device** after Phase 1 (30%) and each subsequent milestone
5. **Iterate based on real-device testing** feedback

Ready to start? Begin with Phase 0-5% project setup.
