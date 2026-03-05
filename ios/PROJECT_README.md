# Red Screen Filter - iOS Project

## Quick Start

### Opening the Project

1. Navigate to `/ios` folder
2. Double-click `RedScreenFilter.xcodeproj` to open in Xcode
3. Wait for Xcode to index the project

### Building and Running

**Option 1: Using Xcode (Recommended)**
1. Select a simulator or device from the target dropdown (top toolbar)
2. Press `Cmd + R` or click the ▶️ Run button
3. The app will build and launch

**Option 2: Using Command Line**
```bash
cd ios
open RedScreenFilter.xcodeproj
```

### Project Structure

```
RedScreenFilter/
├── RedScreenFilter.xcodeproj/         # Xcode project file
├── RedScreenFilter/
│   ├── Info.plist                     # App configuration
│   └── Sources/
│       ├── App/                       # Main app files
│       │   ├── RedScreenFilterApp.swift   # App entry point
│       │   ├── ContentView.swift          # Main UI view
│       │   └── AppDelegate.swift          # App lifecycle
│       ├── Views/                     # UI components (to be added)
│       ├── Services/                  # Business logic (to be added)
│       ├── ViewModels/                # MVVM view models (to be added)
│       ├── Models/                    # Data models (to be added)
│       ├── Utilities/                 # Helper functions (to be added)
│       └── Resources/
│           └── Assets.xcassets/       # Images and colors
```

### Requirements

- **Xcode**: 15.0 or later
- **macOS**: 13.0 or later  
- **iOS Deployment Target**: 14.0
- **Swift**: 5.9+

### Project Settings

- **Bundle ID**: `com.redscreenfilter`
- **App Name**: Red Screen Filter
- **Version**: 1.0 (Build 1)
- **Language**: Swift (SwiftUI)

### Next Steps

Follow the [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) to continue development:
- Phase 5-10%: Project Structure & Entitlements Setup
- Phase 10-20%: In-App Red Overlay & Main UI
- And so on...

### Troubleshooting

**Issue: "Library not loaded: CoreSimulator.framework"**
- This is a command-line xcodebuild issue
- Solution: Open project in Xcode GUI instead
- Or run: `sudo xcode-select --reset` and `xcodebuild -runFirstLaunch`

**Issue: Signing errors**
- Go to Project Settings → Signing & Capabilities
- Select your development team
- Enable "Automatically manage signing"

### Resources

- [Apple SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [iOS App Architecture](https://developer.apple.com/documentation/uikit/app_and_environment)
