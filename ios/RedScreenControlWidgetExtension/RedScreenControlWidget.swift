import SwiftUI
import WidgetKit

struct RedScreenWidgetEntry: TimelineEntry {
    let date: Date
    let state: WidgetSharedState
    let nextScheduleChange: Date?
}

struct RedScreenWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> RedScreenWidgetEntry {
        RedScreenWidgetEntry(date: Date(), state: WidgetSharedState(), nextScheduleChange: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (RedScreenWidgetEntry) -> Void) {
        completion(makeEntry(for: Date()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<RedScreenWidgetEntry>) -> Void) {
        let now = Date()
        let entry = makeEntry(for: now)
        let refreshDate = Calendar.current.date(byAdding: .minute, value: 5, to: now) ?? now.addingTimeInterval(300)
        completion(Timeline(entries: [entry], policy: .after(refreshDate)))
    }

    private func makeEntry(for date: Date) -> RedScreenWidgetEntry {
        let state = WidgetSharedState()
        return RedScreenWidgetEntry(
            date: date,
            state: state,
            nextScheduleChange: nextChangeDate(from: date, state: state)
        )
    }

    private func nextChangeDate(from now: Date, state: WidgetSharedState) -> Date? {
        guard state.scheduleEnabled else { return nil }

        let calendar = Calendar.current
        guard let start = parseTime(state.scheduleStartTime, from: now),
              let end = parseTime(state.scheduleEndTime, from: now) else {
            return nil
        }

        let startMinutes = calendar.component(.hour, from: start) * 60 + calendar.component(.minute, from: start)
        let endMinutes = calendar.component(.hour, from: end) * 60 + calendar.component(.minute, from: end)
        let nowMinutes = calendar.component(.hour, from: now) * 60 + calendar.component(.minute, from: now)

        if startMinutes <= endMinutes {
            if nowMinutes < startMinutes { return start }
            if nowMinutes < endMinutes { return end }
            return calendar.date(byAdding: .day, value: 1, to: start)
        }

        if nowMinutes >= startMinutes {
            return calendar.date(byAdding: .day, value: 1, to: end)
        }

        if nowMinutes < endMinutes { return end }
        return start
    }

    private func parseTime(_ value: String, from date: Date) -> Date? {
        let parts = value.split(separator: ":")
        guard parts.count == 2,
              let hour = Int(parts[0]),
              let minute = Int(parts[1]) else {
            return nil
        }

        var comps = Calendar.current.dateComponents([.year, .month, .day], from: date)
        comps.hour = hour
        comps.minute = minute
        comps.second = 0
        return Calendar.current.date(from: comps)
    }
}

struct RedScreenControlWidgetEntryView: View {
    var entry: RedScreenWidgetProvider.Entry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        switch family {
        case .accessoryInline:
            inlineView
        case .accessoryCircular:
            circularView
        case .accessoryRectangular:
            rectangularView
        default:
            rectangularView
        }
    }

    private var inlineView: some View {
        Text(entry.state.overlayEnabled ? "Red filter on" : "Red filter off")
    }

    private var circularView: some View {
        ZStack {
            AccessoryWidgetBackground()
            Image(systemName: entry.state.overlayEnabled ? "moon.stars.fill" : "moon.stars")
                .foregroundStyle(entry.state.overlayEnabled ? .red : .secondary)
        }
        .widgetURL(URL(string: "redscreenfilter://widget?action=open"))
    }

    private var rectangularView: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 6) {
                Image(systemName: entry.state.overlayEnabled ? "lightbulb.max.fill" : "lightbulb.slash")
                    .foregroundStyle(entry.state.overlayEnabled ? .red : .secondary)
                Text(entry.state.overlayEnabled ? "Overlay On" : "Overlay Off")
                    .font(.system(size: 12, weight: .semibold))
            }

            Text("Opacity \(Int(entry.state.opacity * 100))%")
                .font(.system(size: 11))

            if let next = entry.nextScheduleChange {
                Text("Next \(next, style: .time)")
                    .font(.system(size: 10))
                    .foregroundStyle(.secondary)
            } else {
                Text("No schedule")
                    .font(.system(size: 10))
                    .foregroundStyle(.secondary)
            }

            if #available(iOSApplicationExtension 17.0, *) {
                HStack(spacing: 6) {
                    Button(intent: ToggleOverlayIntent()) {
                        Label("Toggle", systemImage: "arrow.triangle.2.circlepath")
                    }
                    .buttonStyle(.borderless)

                    Button(intent: ApplySleepPresetIntent()) {
                        Text("Sleep")
                            .font(.system(size: 10, weight: .semibold))
                    }
                    .buttonStyle(.borderless)
                }
            }
        }
        .widgetURL(URL(string: "redscreenfilter://widget?action=open"))
    }
}

struct RedScreenControlWidget: Widget {
    let kind: String = "RedScreenControlWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: RedScreenWidgetProvider()) { entry in
            RedScreenControlWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Red Screen Control")
        .description("Overlay status, opacity, next schedule change, and quick actions.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}
