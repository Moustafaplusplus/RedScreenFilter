import AppIntents
import WidgetKit

@available(iOSApplicationExtension 17.0, *)
struct ToggleOverlayIntent: AppIntent {
    static var title: LocalizedStringResource = "Toggle Overlay"
    static var description = IntentDescription("Toggle red overlay and open the app.")
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        WidgetSharedState.toggleOverlay()
        WidgetSharedState.markOpenRequest()
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}

@available(iOSApplicationExtension 17.0, *)
struct ApplySleepPresetIntent: AppIntent {
    static var title: LocalizedStringResource = "Apply Sleep Preset"
    static var description = IntentDescription("Apply Sleep preset from the widget and open the app.")
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        WidgetSharedState.applyPreset(named: PresetOption.sleep.rawValue)
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}

enum PresetOption: String, AppEnum {
    case work = "Work"
    case gaming = "Gaming"
    case movie = "Movie"
    case sleep = "Sleep"

    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Preset"

    static var caseDisplayRepresentations: [PresetOption: DisplayRepresentation] = [
        .work: "Work",
        .gaming: "Gaming",
        .movie: "Movie",
        .sleep: "Sleep"
    ]
}
