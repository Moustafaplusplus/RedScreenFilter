# Google Play Console Submission Guide

This document contains all the information you need to submit Red Screen Filter to the Google Play Store.

---

## Table of Contents

1. [Pre-Submission Checklist](#pre-submission-checklist)
2. [App Details](#app-details)
3. [Permission Justifications](#permission-justifications)
4. [Privacy Policy](#privacy-policy)
5. [Content Rating](#content-rating)
6. [Store Listing](#store-listing)
7. [Testing Instructions](#testing-instructions)
8. [Common Rejection Reasons](#common-rejection-reasons)

---

## Pre-Submission Checklist

### 1. Create Release Keystore
- [ ] Follow instructions in `android/KEYSTORE_SETUP.md`
- [ ] Generate keystore: `keytool -genkey -v -keystore release-keystore.jks ...`
- [ ] Create `keystore.properties` file
- [ ] **Backup keystore file securely**
- [ ] Test signing: `./gradlew bundleRelease`

### 2. Build Release Version
- [ ] Verify ProGuard is enabled (`isMinifyEnabled = true`)
- [ ] Build App Bundle: `cd android && ./gradlew bundleRelease`
- [ ] Locate file: `android/app/build/outputs/bundle/release/app-release.aab`
- [ ] Test on real device (install via `bundletool`)

### 3. Create Google Play Console Account
- [ ] Register at https://play.google.com/console
- [ ] Pay one-time $25 registration fee
- [ ] Verify your identity
- [ ] Set up developer profile

### 4. Prepare Assets
- [ ] App icon (512x512 PNG)
- [ ] Feature graphic (1024x500 PNG/JPG)
- [ ] Screenshots (min 2, max 8):
  - Phone: 16:9 or 9:16 ratio
  - 7-inch tablet (optional)
  - 10-inch tablet (optional)
- [ ] Promo video (YouTube URL, optional)

### 5. Host Privacy Policy
- [ ] Upload `PRIVACY_POLICY.md` to your website, or
- [ ] Use GitHub Pages: `https://yourusername.github.io/redscreenfilter/privacy-policy.html`, or
- [ ] Use a free hosting service (termly.io, freeprivacypolicy.com)
- [ ] **Must be publicly accessible HTTPS URL**

---

## App Details

### Basic Information

**App Name**: Red Screen Filter

**Short Description** (80 chars max):
```
Eye-friendly red overlay with smart scheduling and wellness features
```

**Package Name**: `com.redscreenfilter`

**Category**: Health & Fitness (or Tools)

**Contact Email**: your-email@example.com

**Default Language**: English (United States)

---

## Permission Justifications

**CRITICAL**: Google will ask you to justify each permission. Use these detailed explanations.

### SYSTEM_ALERT_WINDOW (Display over other apps)

**Permission Name**: Display over other apps

**Why is it needed?**
```
This permission is essential for the core functionality of Red Screen Filter. 
The app creates a customizable red overlay that displays on top of all other 
apps to reduce blue light exposure and eye strain during nighttime use. 
Without this permission, the app cannot function at all, as its primary 
purpose is to provide a persistent color filter across the entire device.
```

**User disclosure**: 
- Shown in app description
- Requested on first launch with clear explanation
- Required for app to function

---

### ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION (Location)

**Permission Name**: Location

**Why is it needed?**
```
Location permission is OPTIONAL and only used for the "Sunset/Sunrise Scheduling" 
feature. When enabled by the user, the app calculates local sunset and sunrise 
times to automatically enable/disable the overlay. The location data is:
1. Never stored permanently
2. Never transmitted to servers
3. Only used for mathematical calculation of sun times
4. Can be denied - app works fully without it

The app uses coarse location as a fallback to fine location, requiring only 
city-level accuracy for sunset/sunrise calculation.
```

**User disclosure**: 
- Clearly marked as optional in app
- Requested only when user enables sunset/sunrise scheduling
- Explanation shown before permission request

**Alternative**: User can manually set overlay schedule times without location

---

### PACKAGE_USAGE_STATS (Usage access)

**Permission Name**: Usage access

**Why is it needed?**
```
This permission enables the "App Exemption" feature, which allows users to 
selectively disable the overlay for specific apps (e.g., camera apps, photo 
editors, games). The permission is used to:
1. Detect which app is currently in the foreground
2. Check if that app is in the user's exemption list
3. Temporarily hide the overlay for exempted apps

No usage statistics are collected, stored, or transmitted. The permission is 
queried in real-time only to determine the current foreground app.
```

**User disclosure**: 
- Feature is optional
- Explanation provided in settings
- User can grant/deny in Android Settings

**Alternative**: App works without it, but exemptions won't function

---

### QUERY_ALL_PACKAGES (See all apps)

**Permission Name**: Query all packages

**Why is it needed?**
```
This permission allows users to see a complete list of their installed apps 
when choosing which apps should be exempted from the overlay. Without this 
permission, the exemption list would be empty or incomplete due to Android 11+ 
package visibility restrictions. The permission is used solely to:
1. Display app names and icons in the exemption selection UI
2. Allow users to choose which apps should not show the overlay
3. Improve user experience by showing all available apps

No data is transmitted or stored. The app list is queried on-demand only when 
the user opens the App Exemption settings screen.
```

**User disclosure**: 
- Used only for UI display
- No data collection or transmission
- Feature is optional

**Alternative**: Users can manually enter package names (poor UX)

**WARNING**: This is a restricted permission. Google may reject it. Be prepared to:
- Demonstrate the feature in a video
- Explain why alternative APIs (like `<queries>` in manifest) won't work
- Consider removing this permission if rejected and using `<queries>` for common apps only

---

### POST_NOTIFICATIONS (Notifications - Android 13+)

**Permission Name**: Notifications

**Why is it needed?**
```
Required on Android 13+ to display:
1. Persistent notification while overlay service is running (required by Android)
2. Optional wellness reminders (20-20-20 rule for eye strain prevention)
3. Scheduling notifications (e.g., "Overlay enabled at sunset")

Users can deny this permission, but foreground service notification may still 
appear (Android system requirement).
```

**User disclosure**: 
- Standard Android permission flow
- Optional wellness features can be disabled

---

### FOREGROUND_SERVICE & FOREGROUND_SERVICE_SPECIAL_USE

**Permission Name**: Foreground service

**Why is it needed?**
```
Required to keep the overlay active while users navigate other apps. The 
foreground service ensures the overlay persists across app switches and 
device sleep/wake. Declared as SPECIAL_USE type because the overlay is a 
system-wide UI enhancement for accessibility and health purposes.
```

**User disclosure**: 
- Required for core functionality
- Notification always shown (Android requirement)

---

### WRITE_SETTINGS (Modify system settings)

**Permission Name**: Modify system settings

**Why is it needed?**
```
OPTIONAL permission that allows the app to adjust device brightness. Users can 
control brightness independently of the overlay opacity for better customization. 
The app only modifies the brightness setting when:
1. User explicitly adjusts the brightness slider in the app
2. User enables automatic brightness adjustments

The app never modifies system settings without direct user action.
```

**User disclosure**: 
- Optional feature
- Requested only when user tries to use brightness control
- Can be denied - overlay still works

---

### RECEIVE_BOOT_COMPLETED (Start at boot)

**Permission Name**: Run at startup

**Why is it needed?**
```
Allows the app to restore scheduled overlay settings after device reboot. 
If the user has enabled time-based scheduling (e.g., enable overlay at 10 PM), 
this permission ensures the schedule is reactivated after restart. Without it, 
users would need to manually re-enable their schedules after every reboot.
```

**User disclosure**: 
- Standard behavior for scheduling apps
- Only activates if user has scheduling enabled

---

## Privacy Policy

### Required Information

**Privacy Policy URL**: `https://your-domain.com/privacy-policy` OR `https://yourusername.github.io/redscreenfilter/privacy-policy.html`

### Key Points to Include in Play Console

1. **Data Collection**: 
   ```
   Red Screen Filter does not collect, transmit, or share any user data. 
   All settings and preferences are stored locally on the device using 
   Android's encrypted storage. No analytics, no tracking, no servers.
   ```

2. **Location Data**:
   ```
   Location is used ONLY if the user enables sunset/sunrise scheduling. 
   It's used for local calculation only and never transmitted or stored.
   ```

3. **Data Safety Section** (Play Console):
   - ❌ Does your app collect or share any of the required user data types? **NO**
   - ❌ Is all of the user data collected by your app encrypted in transit? **N/A (no transmission)**
   - ✅ Do you provide a way for users to request that their data is deleted? **YES (uninstall app)**

---

## Content Rating

Use the IARC questionnaire in Play Console. Based on our app:

**Expected Rating**: Everyone (all regions)

### Questionnaire Answers:

1. **Does your app contain any violence?** No
2. **Does your app contain any sexual content?** No
3. **Does your app contain any profanity?** No
4. **Does your app contain any drug/alcohol/tobacco references?** No
5. **Does your app have any scary content?** No
6. **Does your app have any gambling?** No
7. **Does your app allow users to communicate?** No
8. **Does your app allow users to share their location?** No (location is only used locally)
9. **Does your app contain any ads?** No
10. **Does your app have in-app purchases?** No

---

## Store Listing

### Title
```
Red Screen Filter - Eye Care & Night Mode
```

### Short Description (80 chars)
```
Eye-friendly red overlay with smart scheduling and wellness features
```

### Full Description (4000 chars max)

```
🌙 Red Screen Filter - Your Eyes' Best Friend at Night

Reduce eye strain and improve sleep quality with Red Screen Filter, a powerful 
yet simple app that applies a customizable red overlay to your entire device.

✨ KEY FEATURES

🎨 Customizable Red Overlay
• Adjust opacity from 0-100%
• Multiple color variants for different vision types
• Extra dim mode for deeper dimming
• Real-time preview

⏰ Smart Scheduling
• Time-based scheduling (e.g., 10 PM - 7 AM)
• Sunset/sunrise automatic scheduling
• Adjustable time offsets
• Persistent across device restarts

🧠 Intelligent Features
• App exemptions (hide overlay for camera, games, etc.)
• Lock screen & home screen exemptions
• Battery-aware (reduces intensity when battery is low)
• Ambient light sensing (auto-adjust based on environment)

💚 Wellness Features
• 20-20-20 eye strain reminders
• Usage analytics and streak tracking
• Customizable notification styles
• No ads, no tracking, complete privacy

🚀 Quick Access
• Quick Settings tile
• Voice command support (Google Assistant)
• Persistent notification controls
• Instant on/off toggle

🔒 Privacy First
• No data collection
• No internet connection required
• All data stays on your device
• No ads, no tracking

WHY RED LIGHT?

Blue light from screens can disrupt your circadian rhythm and cause eye strain. 
Red light is the safest color for nighttime viewing, helping you:
• Fall asleep faster
• Reduce eye fatigue
• Maintain natural sleep cycles
• Protect long-term eye health

PERFECT FOR:

👓 People with light sensitivity
🌙 Night owls and shift workers
📚 Students studying late
💼 Professionals working at night
👨‍💻 Developers and designers
🎮 Late-night gamers

ACCESSIBILITY

Multiple color presets optimized for:
• Protanopia (red-blind)
• Deuteranopia (green-blind)
• General color blindness
• High contrast needs

100% FREE, NO ADS, NO SUBSCRIPTIONS

Red Screen Filter is completely free with all features unlocked. We believe 
eye health should be accessible to everyone.

PERMISSIONS EXPLAINED

• Display over other apps: Show the overlay (required)
• Location (optional): Calculate sunset/sunrise times
• Usage access (optional): Enable app exemptions
• Notifications: Show overlay status and reminders

Your privacy matters. All permissions are explained in detail, and optional 
features work only when you enable them.

Download Red Screen Filter today and give your eyes the care they deserve! 🌟
```

### What's New (500 chars - for updates)
```
Initial release of Red Screen Filter!

✨ Features:
• Customizable red overlay for eye protection
• Smart scheduling with sunset/sunrise support
• App exemptions for camera, games & more
• 20-20-20 wellness reminders
• Usage analytics & streak tracking
• Battery-aware & ambient light sensing
• 100% free, no ads, complete privacy

Your feedback helps us improve! Please rate and review.
```

---

## Testing Instructions

### For Internal Testing (14 days minimum)

1. **Create Internal Testing Track**:
   - Upload your AAB file
   - Add 20+ testers (use opt-in URL)
   - Distribute to testers

2. **Test Scenarios** (provide to testers):
   ```
   Please test the following:
   
   ✅ Basic Functionality:
   - Enable/disable overlay
   - Adjust opacity
   - Change color variants
   - Enable extra dim mode
   
   ✅ Scheduling:
   - Set time-based schedule
   - Test sunset/sunrise scheduling (grant location)
   - Verify schedule persists after reboot
   
   ✅ Advanced Features:
   - Add apps to exemption list
   - Test lock screen hiding
   - Test home screen hiding
   - Enable 20-20-20 reminders
   
   ✅ Permissions:
   - Deny location - app should work
   - Deny notifications - app should work
   - Deny usage access - app should work (exemptions disabled)
   
   ✅ System Integration:
   - Quick Settings tile
   - Notification controls
   - Battery optimization
   
   Report any crashes, bugs, or unexpected behavior.
   ```

3. **Collect Feedback**:
   - Create Google Form for testers
   - Address critical bugs before production

---

## Common Rejection Reasons & How to Avoid

### 1. Missing Privacy Policy
✅ **Fixed**: Privacy policy created and will be hosted

### 2. QUERY_ALL_PACKAGES Rejection
⚠️ **Risk**: High - This permission is heavily scrutinized

**If rejected**:
- Remove the permission
- Use `<queries>` in AndroidManifest.xml to declare common apps:
  ```xml
  <queries>
      <package android:name="com.android.camera2" />
      <package android:name="com.google.android.youtube" />
      <!-- Add more common apps -->
  </queries>
  ```
- Reduce UX but maintain core functionality

### 3. Insufficient Testing
✅ **Fixed**: Internal testing process defined

### 4. Misleading Description
✅ **Fixed**: Clear, accurate description provided

### 5. Permission Not Justified
✅ **Fixed**: Detailed justifications provided above

### 6. Functionality Not Clear
✅ **Fixed**: Add video demonstration showing:
- Overlay in action
- App exemption feature
- Scheduling features
- Permissions explanation

---

## Pre-Launch Checklist

### Before Uploading to Play Console:

- [ ] Build release AAB with signing
- [ ] Test release build on multiple devices
- [ ] Verify ProGuard didn't break anything
- [ ] Check APK size (should be <10 MB)
- [ ] Host privacy policy at public HTTPS URL
- [ ] Prepare all screenshots (min 2)
- [ ] Create feature graphic (1024x500)
- [ ] Export app icon at 512x512
- [ ] Write store description
- [ ] Prepare permission justifications
- [ ] Set up internal testing with 20+ testers
- [ ] Plan 14-day testing period
- [ ] Create support email address
- [ ] Have developer account ready ($25 one-time fee)

### During Play Console Setup:

- [ ] Upload AAB file
- [ ] Fill in all app details
- [ ] Add privacy policy URL
- [ ] Complete Data Safety section (select "NO" for data collection)
- [ ] Upload all graphics
- [ ] Set up pricing & distribution (Free, All countries)
- [ ] Complete content rating questionnaire
- [ ] Set target audience (13+)
- [ ] Add store listing translations (optional)
- [ ] Review all information carefully
- [ ] Submit for internal testing
- [ ] Wait 14 days
- [ ] Submit for production review

### After Submission:

- [ ] Monitor Play Console email for any requests
- [ ] Respond to Google within 7 days if they ask questions
- [ ] Prepare video demonstration if requested
- [ ] Be patient (review takes 1-7 days usually)

---

## Support & Escalation

If your app is rejected:

1. **Read the rejection reason carefully**
2. **Don't immediately resubmit** - fix the issue first
3. **Use Play Console's appeal process** if you believe it's an error
4. **Remove problematic permissions** if necessary (especially QUERY_ALL_PACKAGES)
5. **Provide additional documentation** when requested

---

## Contact Information for Play Console

**Developer Email**: your-email@example.com
**Support Email**: support@redscreenfilter.app (or same as above)
**Website**: https://github.com/yourusername/redscreenfilter
**Privacy Policy**: [TO BE FILLED AFTER HOSTING]

---

## Notes

- First-time submissions take longer (3-7 days)
- Be prepared for follow-up questions
- QUERY_ALL_PACKAGES has ~50% rejection rate - have backup plan
- Location permission is usually approved with proper justification
- Keep app size small (currently should be <10 MB)
- Respond quickly to any Google requests (within 7 days)

Good luck with your submission! 🚀
