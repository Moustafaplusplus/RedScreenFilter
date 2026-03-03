# Red Screen Filter

A native cross-platform application that adds a customizable red screen overlay for night-time eye comfort and accessibility.

## Overview

This is a monorepo containing native implementations for both Android (Kotlin) and iOS (Swift), allowing users to apply a red-tinted overlay with configurable opacity and scheduling.

## Project Structure

```
RedScreenFilter/
├── android/          # Kotlin-based Android application
├── ios/              # Swift-based iOS application
├── shared/           # Shared documentation and assets
└── README.md         # This file
```

## Features

This app includes a comprehensive set of health-focused, intelligent features:

**Core Features:**
- Red screen overlay with customizable opacity
- Time-based and sunset/sunrise scheduling
- Activity-based presets (Work, Gaming, Sleep, Movie)
- 20-20-20 eye strain reminders
- Ambient light auto-adjustment

**Smart Features:**
- Battery awareness (reduce intensity on low battery)
- Selective app exemptions
- Voice commands (Siri/Google Assistant)
- Quick tile & home screen widgets
- Color blindness presets

**Analytics & Insights:**
- Daily/weekly usage reports
- Streak tracking
- Eye strain reduction analytics

See [FEATURES.md](./shared/FEATURES.md) for complete feature list and roadmap.

## Platform-Specific Details

### Android (Kotlin)
- Custom overlay service using `WindowManager`
- Background service for scheduling
- Material Design 3 UI
- Minimum API: 28+

See [android/README.md](android/README.md) for details.

### iOS (Swift)
- UIWindow-based overlay implementation
- App Groups for background task coordination
- Native iOS design patterns
- Minimum iOS: 14+

See [ios/README.md](ios/README.md) for details.

## Getting Started

1. Clone repository and navigate to platform folder
2. Android: Open `/android` in Android Studio
3. iOS: Open `/ios` in Xcode

## Development

Each platform is independently maintained but follows shared architectural patterns:
- Settings persistence layer
- Overlay service abstraction
- Scheduling engine
- UI/UX consistency

See individual platform READMEs for setup and build instructions.
