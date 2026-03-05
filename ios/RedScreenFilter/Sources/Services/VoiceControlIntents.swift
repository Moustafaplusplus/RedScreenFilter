import AppIntents

@available(iOS 16.0, *)
struct SetOpacityIntent: AppIntent {
    static var title: LocalizedStringResource = "Set Red Filter Opacity"
    static var description = IntentDescription("Set the red overlay filter opacity to a specific percentage.")
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Opacity Percentage", description: "Set opacity from 0 to 100%")
    var opacityPercentage: Int

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let prefs = PreferencesManager.shared
        let overlayManager = OverlayWindowManager.shared
        
        let clamped = max(0, min(100, opacityPercentage))
        let normalizedOpacity = Float(clamped) / 100.0
        
        // Update opacity
        prefs.setOpacity(normalizedOpacity)
        
        // Enable overlay if not already enabled
        if !prefs.isOverlayEnabled() {
            prefs.setOverlayEnabled(true)
        }
        
        // Show overlay with new opacity
        overlayManager.showOverlay(opacity: normalizedOpacity)
        
        return .result(dialog: "Red filter opacity set to \(clamped)%")
    }
}

@available(iOS 16.0, *)
struct ApplyPresetIntent: AppIntent {
    static var title: LocalizedStringResource = "Apply Red Filter Preset"
    static var description = IntentDescription("Apply a preset configuration to the red overlay filter.")
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Preset Name", description: "Choose a preset: Work, Gaming, Movie, or Sleep")
    var presetName: String

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let prefs = PreferencesManager.shared
        let overlayManager = OverlayWindowManager.shared
        
        let normalizedName = presetName.lowercased().trimmingCharacters(in: .whitespaces)
        
        // Apply the preset settings
        switch normalizedName {
        case "work":
            prefs.setOpacity(0.3)
            prefs.setColorVariant(Constants.Colors.redStandard)
        case "gaming":
            prefs.setOpacity(0.4)
            prefs.setColorVariant(Constants.Colors.redOrange)
        case "movie":
            prefs.setOpacity(0.5)
            prefs.setColorVariant(Constants.Colors.redStandard)
        case "sleep":
            prefs.setOpacity(0.7)
            prefs.setColorVariant(Constants.Colors.redPink)
        default:
            return .result(dialog: "Unknown preset '\(presetName)'. Try: Work, Gaming, Movie, or Sleep")
        }
        
        // Enable overlay if not already enabled
        if !prefs.isOverlayEnabled() {
            prefs.setOverlayEnabled(true)
        }
        
        // Show overlay with preset settings
        overlayManager.showOverlay(opacity: prefs.getOpacity())
        
        return .result(dialog: "Applied \(presetName) preset to red filter")
    }
}

@available(iOS 16.0, *)
struct ToggleRedFilterIntent: AppIntent {
    static var title: LocalizedStringResource = "Toggle Red Filter"
    static var description = IntentDescription("Turn the red overlay filter on or off.")
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let prefs = PreferencesManager.shared
        let overlayManager = OverlayWindowManager.shared
        
        let currentState = prefs.isOverlayEnabled()
        prefs.setOverlayEnabled(!currentState)
        
        if !currentState {
            overlayManager.showOverlay(opacity: prefs.getOpacity())
        } else {
            overlayManager.hideOverlay()
        }
        
        let status = !currentState ? "on" : "off"
        return .result(dialog: "Red filter turned \(status)")
    }
}

@available(iOS 16.0, *)
struct GetRedFilterStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Get Red Filter Status"
    static var description = IntentDescription("Check if the red overlay filter is currently on or off.")
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let prefs = PreferencesManager.shared
        let status = prefs.isOverlayEnabled() ? "on" : "off"
        let opacity = Int(prefs.getOpacity() * 100)
        let variant = prefs.getColorVariant()
        
        return .result(dialog: "Red filter is \(status) at \(opacity)% opacity with \(variant) color")
    }
}
