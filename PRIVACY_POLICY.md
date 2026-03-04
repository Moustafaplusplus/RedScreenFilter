# Privacy Policy for Red Screen Filter

**Last Updated: March 4, 2026**

## Introduction

Red Screen Filter ("we", "our", or "the app") is committed to protecting your privacy. This Privacy Policy explains how our application handles your information.

## Information We Collect

### Information Collected and Stored Locally

Red Screen Filter stores the following information **only on your device**. We do not transmit, share, or upload this data to any servers:

1. **App Settings and Preferences**
   - Overlay opacity level
   - Color variant preferences
   - Scheduling settings (start/end times)
   - Extra dim settings
   - Automation preferences

2. **Location Data** (Optional)
   - Approximate location (latitude/longitude) for sunset/sunrise calculation
   - Used only when you enable the "Sunset/Sunrise Scheduling" feature
   - Stored locally on your device only
   - Never transmitted to our servers or third parties
   - You can disable this feature at any time

3. **Usage Analytics** (Local Only)
   - Overlay activation/deactivation events
   - Usage duration and patterns
   - Streak tracking
   - All analytics data is stored locally on your device
   - Used only to show you your own usage statistics

4. **App Usage Statistics**
   - Required for "App Exemption" feature to detect foreground apps
   - Allows you to disable overlay for specific apps (e.g., camera, photo editor)
   - Data accessed only while the app is running
   - Never stored permanently or transmitted

5. **Installed Apps List**
   - Used only for the "App Exemption" feature
   - Allows you to select which apps should not show the overlay
   - List is queried on-demand and not stored

## Permissions Explained

### Required Permissions

- **Display over other apps (SYSTEM_ALERT_WINDOW)**
  - Purpose: Display the red screen overlay on top of other apps
  - This is the core functionality of the app

- **Foreground Service (FOREGROUND_SERVICE)**
  - Purpose: Keep the overlay running while you use other apps
  - Ensures the overlay persists across different apps

- **Post Notifications (POST_NOTIFICATIONS)** (Android 13+)
  - Purpose: Show notification for overlay status and wellness reminders
  - You can disable notifications in Android settings

### Optional Permissions (You Control)

- **Location Access (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)**
  - Purpose: Calculate local sunset and sunrise times for automatic scheduling
  - Only used if you enable "Sunset/Sunrise Scheduling" feature
  - Can be denied; app works fully without it
  - Location is used only for calculation, never stored or shared

- **Usage Access (PACKAGE_USAGE_STATS)**
  - Purpose: Detect which app is currently in the foreground
  - Required for "App Exemption" feature to hide overlay in selected apps
  - Can be denied; app works without it (exemptions won't function)

- **Query All Packages (QUERY_ALL_PACKAGES)**
  - Purpose: Show list of installed apps for app exemption selection
  - Used only to display app names and icons when you select exemptions
  - Can be denied; app works without it

- **Modify System Settings (WRITE_SETTINGS)**
  - Purpose: Allow the app to adjust screen brightness
  - Optional feature; app works without it

- **Receive Boot Completed (RECEIVE_BOOT_COMPLETED)**
  - Purpose: Restore scheduled overlays after device restart
  - Only used if you enable scheduling

## Data Storage and Security

- **All data is stored locally** on your device using Android's encrypted SharedPreferences and encrypted DataStore
- **No cloud storage**: We do not store any of your data on remote servers
- **No user accounts**: The app does not require or support user accounts
- **No data transmission**: Your data never leaves your device
- **Encryption**: Sensitive settings are stored using Android's Security Crypto library

## Data Sharing and Third Parties

**We do not share, sell, or transmit your data to any third parties.**

The app does not:
- Use analytics services (e.g., Google Analytics, Firebase Analytics)
- Include advertising networks
- Transmit data to remote servers
- Use tracking technologies
- Share data with third-party services

## Google Play Services

The app uses Google Play Services location APIs solely for sunset/sunrise calculations. This usage is:
- Completely optional (only if you enable the feature)
- Local processing only
- Subject to Google's Privacy Policy when enabled

## Children's Privacy

Red Screen Filter does not knowingly collect any information from children under 13 years of age. The app is suitable for all ages and does not require any personal information.

## Data Backup

- Android may back up app preferences to Google Drive (if you enable Android backup)
- This follows Android's standard backup mechanism
- Backups are encrypted by Android
- You can disable app backup in your Android settings

## Your Rights and Control

You have complete control over your data:

- **Access**: All your data is visible in the app settings
- **Deletion**: Uninstalling the app deletes all data
- **Export**: Not applicable (no cloud storage)
- **Correction**: Modify any setting directly in the app
- **Opt-out**: Disable any optional permission in Android settings

## Changes to Permissions

If we add new permissions in future updates, we will:
- Update this Privacy Policy
- Clearly explain the new permission's purpose
- Require your explicit consent for sensitive permissions

## Third-Party Links

The app does not contain links to external websites or services, except for settings pages within Android system settings.

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. We will notify you of any changes by:
- Updating the "Last Updated" date at the top of this policy
- Showing an in-app notice (for material changes)

Continued use of the app after changes constitutes acceptance of the updated policy.

## Data Retention

- Data is retained as long as the app is installed
- Clearing app data (in Android settings) deletes all stored information
- Uninstalling the app permanently deletes all data

## International Users

Red Screen Filter is available worldwide. All data processing occurs on your local device regardless of your location.

## Contact Us

If you have questions about this Privacy Policy or how the app handles your data, you can contact us:

- **Email**: support@redscreenfilter.app
- **GitHub**: [Repository Issues](https://github.com/yourusername/redscreenfilter/issues)

## Legal Compliance

This app complies with:
- GDPR (General Data Protection Regulation) - EU
- CCPA (California Consumer Privacy Act) - California, USA
- Google Play Developer Program Policies
- Android permissions best practices

## Your Consent

By using Red Screen Filter, you consent to this Privacy Policy.

---

**Summary in Plain English:**

Red Screen Filter is a privacy-focused app. We don't collect, transmit, or share your data. Everything stays on your device. Location is only used if you enable sunset/sunrise scheduling, and it's only used for calculation—never sent anywhere. No ads, no tracking, no accounts, no servers. Your data is yours.



https://moustafaplusplus.github.io/RedScreenFilter/privacy-policy.html