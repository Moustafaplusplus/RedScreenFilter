//
//  MainView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct MainView: View {
    @StateObject private var viewModel = OverlayViewModel()
    @State private var selectedTab: Int = 0
    
    var body: some View {
        ZStack {
            // Background
            RsfTheme.colors.background
                .ignoresSafeArea()
            
            // Content beneath overlay
            TabView(selection: $selectedTab) {
                // Tab 1: Main Control
                MainControlView()
                    .tabItem {
                        Label("Control", systemImage: "circle.fill")
                    }
                    .tag(0)
                
                // Tab 2: In-App Overlay
                PresetsView(viewModel: viewModel)
                    .tabItem {
                        Label("In-App", systemImage: "slider.horizontal.3")
                    }
                    .tag(1)
                
                // Tab 3: Settings
                SettingsView(viewModel: viewModel)
                    .tabItem {
                        Label("Settings", systemImage: "gear")
                    }
                    .tag(2)
                
                // Tab 4: Analytics
                AnalyticsView()
                    .tabItem {
                        Label("Analytics", systemImage: "chart.bar")
                    }
                    .tag(3)
            }
            .preferredColorScheme(.dark)
            .accentColor(RsfTheme.colors.primary)
            
            // In-App Red Overlay (appears on top when enabled)
            OverlayView(
                isVisible: $viewModel.isEnabled,
                currentOpacity: $viewModel.opacity,
                colorVariant: $viewModel.settings.colorVariant
            )
            .ignoresSafeArea()
            .allowsHitTesting(false) // Allow touches to pass through overlay
        }
    }
}

struct MainControlView: View {
    @AppStorage("hasSeenSystemWideShortcutGuide") private var hasSeenSystemWideShortcutGuide: Bool = false
    @AppStorage("hasCompletedSystemWideShortcutSetup") private var hasCompletedSystemWideShortcutSetup: Bool = false
    @AppStorage("setupShortcutAddedEnableSleep") private var setupShortcutAddedEnableSleep: Bool = false
    @AppStorage("setupShortcutAddedDisableFilter") private var setupShortcutAddedDisableFilter: Bool = false
    @AppStorage("setupShortcutAddedEnableWork") private var setupShortcutAddedEnableWork: Bool = false
    @AppStorage("setupShortcutTestedEnableSleep") private var setupShortcutTestedEnableSleep: Bool = false
    @AppStorage("setupShortcutTestedDisableFilter") private var setupShortcutTestedDisableFilter: Bool = false
    @AppStorage("setupShortcutTestedEnableWork") private var setupShortcutTestedEnableWork: Bool = false

    @State private var showShortcutsGuide: Bool = false
    @State private var shortcutStatusMessage: String?
    @State private var isSystemFilterActive: Bool = false
    @State private var awaitingShortcutConfirmation: SiriShortcutsLauncher.Shortcut?
    @State private var showingRunConfirmation: Bool = false

    private var setupCompletedCount: Int {
        [
            setupShortcutAddedEnableSleep,
            setupShortcutAddedDisableFilter,
            setupShortcutAddedEnableWork,
            setupShortcutTestedEnableSleep,
            setupShortcutTestedDisableFilter,
            setupShortcutTestedEnableWork
        ].filter { $0 }.count
    }

    private var setupChecklistComplete: Bool {
        setupCompletedCount == 6
    }

    private var canUseSystemWideControls: Bool {
        hasCompletedSystemWideShortcutSetup || setupChecklistComplete
    }

    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: RsfTheme.spacing.lg) {
                        VStack(spacing: RsfTheme.spacing.xs) {
                            Text("Red Screen Filter")
                                .font(.title)
                                .fontWeight(.bold)
                                .foregroundColor(RsfTheme.colors.onBackground)
                            Text("Eye protection companion")
                                .font(.caption)
                                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                        }
                        .padding(.top, RsfTheme.spacing.md)

                        RsfCard {
                            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                                Text("System-Wide Red Filter")
                                    .font(.headline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(RsfTheme.colors.onSurface)

                                Text("Control iOS Color Filters across all apps using Siri Shortcuts.")
                                    .font(.caption)
                                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                                Text("Setup progress: \(setupCompletedCount)/6")
                                    .font(.caption)
                                    .foregroundColor(canUseSystemWideControls ? RsfTheme.colors.primary : RsfTheme.colors.warning)

                                if canUseSystemWideControls {
                                    Button(action: toggleSystemWideFilter) {
                                        ZStack {
                                            Circle()
                                                .fill(isSystemFilterActive ? RsfTheme.colors.primary : RsfTheme.colors.surfaceVariant)
                                                .frame(width: 140, height: 140)
                                                .shadow(color: RsfTheme.colors.primary.opacity(0.3), radius: RsfTheme.elevation.lg)

                                            VStack(spacing: RsfTheme.spacing.xs) {
                                                Text(isSystemFilterActive ? "ON" : "OFF")
                                                    .font(.title)
                                                    .fontWeight(.bold)
                                                    .foregroundColor(RsfTheme.colors.onPrimary)
                                                Text("System-Wide")
                                                    .font(.caption2)
                                                    .foregroundColor(RsfTheme.colors.onPrimary.opacity(0.85))
                                            }
                                        }
                                        .frame(maxWidth: .infinity)
                                    }
                                    .buttonStyle(.plain)

                                    HStack(spacing: RsfTheme.spacing.sm) {
                                        shortcutButton(
                                            title: "Enable Work Mode",
                                            icon: "briefcase.fill"
                                        ) {
                                            runSystemWideShortcut(.enableWork)
                                        }

                                        shortcutButton(
                                            title: "Disable Filter",
                                            icon: "moon.slash.fill"
                                        ) {
                                            runSystemWideShortcut(.disableFilter)
                                            isSystemFilterActive = false
                                        }
                                    }
                                } else {
                                    VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
                                        Text("Complete setup first. Add and test each shortcut before using ON/OFF controls.")
                                            .font(.caption)
                                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                                        shortcutButton(
                                            title: "Start Setup",
                                            icon: "list.bullet.clipboard"
                                        ) {
                                            showShortcutsGuide = true
                                        }
                                    }
                                }

                                HStack(spacing: RsfTheme.spacing.sm) {
                                    shortcutButton(
                                        title: "Setup Guide",
                                        icon: "list.number"
                                    ) {
                                        showShortcutsGuide = true
                                    }

                                    shortcutButton(
                                        title: "Open Shortcuts App",
                                        icon: "app.badge"
                                    ) {
                                        _ = SiriShortcutsLauncher.shared.openShortcutsApp()
                                    }
                                }

                                Text("Automation tip: In Shortcuts > Automation, create time-based triggers (e.g. 9 PM enable, 7 AM disable).")
                                    .font(.caption2)
                                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                                if let shortcutStatusMessage {
                                    Text(shortcutStatusMessage)
                                        .font(.caption2)
                                        .foregroundColor(RsfTheme.colors.warning)
                                }
                            }
                        }

                        Spacer()
                    }
                    .padding(RsfTheme.spacing.md)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
        .sheet(isPresented: $showShortcutsGuide) {
            SystemWideShortcutsGuideSheet(
                setupShortcutAddedEnableSleep: $setupShortcutAddedEnableSleep,
                setupShortcutAddedDisableFilter: $setupShortcutAddedDisableFilter,
                setupShortcutAddedEnableWork: $setupShortcutAddedEnableWork,
                setupShortcutTestedEnableSleep: $setupShortcutTestedEnableSleep,
                setupShortcutTestedDisableFilter: $setupShortcutTestedDisableFilter,
                setupShortcutTestedEnableWork: $setupShortcutTestedEnableWork,
                runShortcutTest: { shortcut in
                    requestShortcutRunConfirmation(shortcut)
                },
                completeSetup: {
                    if setupChecklistComplete {
                        hasCompletedSystemWideShortcutSetup = true
                        hasSeenSystemWideShortcutGuide = true
                        shortcutStatusMessage = "Setup complete. System-wide controls are now unlocked."
                    }
                }
            )
        }
        .alert(isPresented: $showingRunConfirmation) {
            Alert(
                title: Text("Did the shortcut run successfully?"),
                message: Text(awaitingShortcutConfirmation?.title ?? "Confirm the test result."),
                primaryButton: .default(Text("Yes"), action: {
                    guard let shortcut = awaitingShortcutConfirmation else { return }
                    markShortcutTested(shortcut)
                }),
                secondaryButton: .cancel(Text("No"))
            )
        }
        .onChange(of: setupChecklistComplete) { isComplete in
            if isComplete {
                hasCompletedSystemWideShortcutSetup = true
                hasSeenSystemWideShortcutGuide = true
            }
        }
    }

    private func toggleSystemWideFilter() {
        guard canUseSystemWideControls else {
            showShortcutsGuide = true
            return
        }

        if isSystemFilterActive {
            runSystemWideShortcut(.disableFilter)
            isSystemFilterActive = false
        } else {
            runSystemWideShortcut(.enableSleep)
            isSystemFilterActive = true
        }
    }

    private func runSystemWideShortcut(_ shortcut: SiriShortcutsLauncher.Shortcut) {
        shortcutStatusMessage = nil

        guard canUseSystemWideControls else {
            showShortcutsGuide = true
            return
        }

        let didOpen = SiriShortcutsLauncher.shared.runShortcut(shortcut)
        if !didOpen {
            shortcutStatusMessage = "Unable to open Shortcuts URL. Please run it manually from the Shortcuts app."
        }
    }

    private func requestShortcutRunConfirmation(_ shortcut: SiriShortcutsLauncher.Shortcut) {
        shortcutStatusMessage = nil

        let didOpen = SiriShortcutsLauncher.shared.runShortcut(shortcut)
        if didOpen {
            awaitingShortcutConfirmation = shortcut
            showingRunConfirmation = true
        } else {
            shortcutStatusMessage = "Unable to open Shortcuts URL. Please run it manually from the Shortcuts app."
        }
    }

    private func markShortcutTested(_ shortcut: SiriShortcutsLauncher.Shortcut) {
        switch shortcut {
        case .enableSleep:
            setupShortcutTestedEnableSleep = true
        case .disableFilter:
            setupShortcutTestedDisableFilter = true
        case .enableWork:
            setupShortcutTestedEnableWork = true
        }

        awaitingShortcutConfirmation = nil
    }

    private func shortcutButton(title: String, icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: RsfTheme.spacing.xs) {
                Image(systemName: icon)
                Text(title)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
            }
            .font(.caption.weight(.semibold))
            .foregroundColor(RsfTheme.colors.onPrimary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, RsfTheme.spacing.sm)
            .background(RsfTheme.colors.primary)
            .cornerRadius(RsfTheme.radius.md)
        }
        .buttonStyle(.plain)
    }
}

struct SystemWideShortcutsGuideSheet: View {
    @Binding var setupShortcutAddedEnableSleep: Bool
    @Binding var setupShortcutAddedDisableFilter: Bool
    @Binding var setupShortcutAddedEnableWork: Bool
    @Binding var setupShortcutTestedEnableSleep: Bool
    @Binding var setupShortcutTestedDisableFilter: Bool
    @Binding var setupShortcutTestedEnableWork: Bool

    let runShortcutTest: (SiriShortcutsLauncher.Shortcut) -> Void
    let completeSetup: () -> Void

    @Environment(\.presentationMode) private var presentationMode

    private let launcher = SiriShortcutsLauncher.shared

    private var setupCompletedCount: Int {
        [
            setupShortcutAddedEnableSleep,
            setupShortcutAddedDisableFilter,
            setupShortcutAddedEnableWork,
            setupShortcutTestedEnableSleep,
            setupShortcutTestedDisableFilter,
            setupShortcutTestedEnableWork
        ].filter { $0 }.count
    }

    private var isReadyToFinish: Bool {
        setupCompletedCount == 6
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                    Text("Use Siri Shortcuts For System-Wide Filters")
                        .font(.headline)
                        .foregroundColor(RsfTheme.colors.onSurface)

                    Text("Complete all steps below before using ON/OFF controls.")
                        .font(.body)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                    Text("Steps: 1) Tap Add 2) Return and tap I've Added It 3) Tap Test and confirm success.")
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)

                    Text("Checklist Progress: \(setupCompletedCount)/6")
                        .font(.caption)
                        .foregroundColor(isReadyToFinish ? RsfTheme.colors.primary : RsfTheme.colors.warning)

                    shortcutSetupRow(
                        title: "Enable Red Filter Sleep Mode",
                        shortcut: .enableSleep,
                        isAdded: $setupShortcutAddedEnableSleep,
                        isTested: $setupShortcutTestedEnableSleep
                    )

                    shortcutSetupRow(
                        title: "Disable Color Filter",
                        shortcut: .disableFilter,
                        isAdded: $setupShortcutAddedDisableFilter,
                        isTested: $setupShortcutTestedDisableFilter
                    )

                    shortcutSetupRow(
                        title: "Enable Color Filter - Work Mode",
                        shortcut: .enableWork,
                        isAdded: $setupShortcutAddedEnableWork,
                        isTested: $setupShortcutTestedEnableWork
                    )

                    Text("Finish becomes available after all shortcuts are added and tested.")
                        .font(.caption2)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
                .padding(RsfTheme.spacing.md)
            }
            .background(RsfTheme.colors.background.ignoresSafeArea())
            .navigationTitle("System-Wide Setup")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Finish") {
                        completeSetup()
                        presentationMode.wrappedValue.dismiss()
                    }
                    .disabled(!isReadyToFinish)
                }
            }
        }
    }

    private func shortcutSetupRow(
        title: String,
        shortcut: SiriShortcutsLauncher.Shortcut,
        isAdded: Binding<Bool>,
        isTested: Binding<Bool>
    ) -> some View {
        VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
            Text(title)
                .font(.caption.weight(.semibold))
                .foregroundColor(RsfTheme.colors.onSurface)

            HStack(spacing: RsfTheme.spacing.sm) {
                if let url = launcher.shareURL(for: shortcut) {
                    Link(destination: url) {
                        labelPill(text: "Add")
                    }
                    .buttonStyle(.plain)
                }

                Button(action: {
                    isAdded.wrappedValue = true
                }) {
                    labelPill(text: isAdded.wrappedValue ? "Added" : "I've Added It")
                }
                .disabled(isAdded.wrappedValue)
                .buttonStyle(.plain)

                Button(action: {
                    runShortcutTest(shortcut)
                }) {
                    labelPill(text: isTested.wrappedValue ? "Tested" : "Test")
                }
                .disabled(!isAdded.wrappedValue)
                .buttonStyle(.plain)
            }

            Text(statusText(isAdded: isAdded.wrappedValue, isTested: isTested.wrappedValue))
                .font(.caption2)
                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
        }
        .padding(RsfTheme.spacing.sm)
        .background(RsfTheme.colors.surfaceVariant)
        .cornerRadius(RsfTheme.radius.md)
    }

    private func statusText(isAdded: Bool, isTested: Bool) -> String {
        if isAdded && isTested {
            return "Added and tested"
        }
        if isAdded {
            return "Added. Now run Test and confirm it works"
        }
        return "Add this shortcut first"
    }

    private func labelPill(text: String) -> some View {
        Text(text)
            .font(.caption.weight(.semibold))
            .foregroundColor(RsfTheme.colors.onPrimary)
            .padding(.horizontal, RsfTheme.spacing.sm)
            .padding(.vertical, RsfTheme.spacing.xs)
            .background(RsfTheme.colors.primary)
            .cornerRadius(RsfTheme.radius.sm)
    }
}

struct QuickPresetButton: View {
    let preset: PresetProfile
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: RsfTheme.spacing.xs) {
                // Color preview circle
                ZStack {
                    Circle()
                        .fill(colorForVariant(preset.colorVariant))
                        .frame(width: 50, height: 50)
                        .opacity(0.7)
                    
                    if isSelected {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.white)
                    } else {
                        Image(systemName: preset.colorVariant.iconName)
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                    }
                }
                
                Text(preset.name)
                    .font(.caption)
                    .fontWeight(isSelected ? .bold : .medium)
                    .foregroundColor(RsfTheme.colors.onSurface)
                
                Text(preset.opacityPercentage)
                    .font(.caption2)
                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
            }
            .frame(width: 80)
            .padding(.vertical, RsfTheme.spacing.sm)
            .padding(.horizontal, RsfTheme.spacing.xs)
            .background(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .fill(isSelected ? RsfTheme.colors.primary.opacity(0.1) : RsfTheme.colors.surfaceVariant)
            )
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .stroke(isSelected ? RsfTheme.colors.primary : RsfTheme.colors.glassStroke, lineWidth: isSelected ? 2 : RsfTheme.border.thin)
            )
        }
    }
    
    private func colorForVariant(_ variant: ColorVariant) -> Color {
        let components = variant.colorComponents
        return Color(red: Double(components.red), green: Double(components.green), blue: Double(components.blue))
    }
}

struct QuickActionButton: View {
    let icon: String
    let label: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: RsfTheme.spacing.xs) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(RsfTheme.colors.primary)
                Text(label)
                    .font(.caption)
                    .foregroundColor(RsfTheme.colors.onSurface)
            }
            .frame(maxWidth: .infinity)
            .padding(RsfTheme.spacing.md)
            .background(RsfTheme.colors.surfaceVariant)
            .cornerRadius(RsfTheme.radius.md)
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
            )
        }
    }
}

struct MainView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
    }
}

struct PresetsView: View {
    @ObservedObject var viewModel: OverlayViewModel
    @StateObject private var presetsManager = PresetsManager.shared

    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: RsfTheme.spacing.lg) {
                        RsfSectionHeader("In-App Overlay", subtitle: "Settings that affect only this app")
                            .padding(.top, RsfTheme.spacing.md)

                        VStack(spacing: RsfTheme.spacing.sm) {
                            Button(action: viewModel.toggleOverlay) {
                                ZStack {
                                    Circle()
                                        .fill(viewModel.isEnabled ? RsfTheme.colors.primary : RsfTheme.colors.surfaceVariant)
                                        .frame(width: 120, height: 120)
                                        .shadow(color: RsfTheme.colors.primary.opacity(0.3), radius: RsfTheme.elevation.lg)

                                    VStack(spacing: RsfTheme.spacing.xs) {
                                        Text(viewModel.isEnabled ? "ON" : "OFF")
                                            .font(.title)
                                            .fontWeight(.bold)
                                            .foregroundColor(RsfTheme.colors.onPrimary)

                                        Text(viewModel.opacity.percentageString)
                                            .font(.caption)
                                            .foregroundColor(RsfTheme.colors.onPrimary.opacity(0.8))
                                    }
                                }
                            }

                            Text(viewModel.isEnabled ? "In-App Overlay Active" : "In-App Overlay Inactive")
                                .font(.caption)
                                .foregroundColor(viewModel.isEnabled ? RsfTheme.colors.primary : RsfTheme.colors.onSurfaceVariant)
                                .fontWeight(.semibold)
                        }

                        RsfCard {
                            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                                HStack {
                                    Text("Opacity")
                                        .font(.headline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(RsfTheme.colors.onSurface)
                                    Spacer()
                                    Text(viewModel.opacity.percentageString)
                                        .fontWeight(.bold)
                                        .foregroundColor(RsfTheme.colors.primary)
                                }

                                Slider(value: $viewModel.opacity, in: 0...1)
                                    .accentColor(RsfTheme.colors.primary)
                                    .onChange(of: viewModel.opacity) { newValue in
                                        viewModel.updateOpacity(newValue)
                                    }

                                HStack(spacing: RsfTheme.spacing.md) {
                                    Text("0%")
                                        .font(.caption2)
                                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    Spacer()
                                    Text("50%")
                                        .font(.caption2)
                                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    Spacer()
                                    Text("100%")
                                        .font(.caption2)
                                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                }
                            }
                        }

                        OverlayColorPicker(
                            selectedVariant: $viewModel.settings.colorVariant,
                            onVariantChanged: { variant in
                                viewModel.updateColorVariant(variant)
                            }
                        )

                        RsfCard {
                            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                                Text("Quick Presets")
                                    .font(.headline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(RsfTheme.colors.onSurface)

                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: RsfTheme.spacing.md) {
                                        ForEach(presetsManager.getDefaultPresets().prefix(4)) { preset in
                                            QuickPresetButton(
                                                preset: preset,
                                                isSelected: presetsManager.currentPresetId == preset.id,
                                                action: {
                                                    applyPreset(preset)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                    .padding(RsfTheme.spacing.md)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func applyPreset(_ preset: PresetProfile) {
        withAnimation {
            if let appliedPreset = presetsManager.applyPreset(byId: preset.id) {
                viewModel.applyPreset(appliedPreset)
            }
        }
    }

}

struct OverlayView: View {
    @Binding var isVisible: Bool
    @Binding var currentOpacity: Float
    @Binding var colorVariant: String

    var body: some View {
        Color.fromVariant(colorVariant)
            .opacity(isVisible ? Double(currentOpacity) : 0.0)
            .animation(.easeInOut(duration: 0.2), value: isVisible)
            .animation(.easeInOut(duration: 0.2), value: currentOpacity)
    }
}

struct OverlayColorPicker: View {
    @Binding var selectedVariant: String
    let onVariantChanged: (String) -> Void

    private let variants = [
        Constants.Colors.redStandard,
        Constants.Colors.redOrange,
        Constants.Colors.redPink,
        Constants.Colors.highContrast
    ]
    
    private let variantLabels = [
        "Red",
        "Orange",
        "Pink",
        "High"
    ]

    var body: some View {
        RsfCard {
            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                Text("Color Variant")
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(RsfTheme.colors.onSurface)

                HStack(spacing: RsfTheme.spacing.md) {
                    ForEach(0..<variants.count, id: \.self) { index in
                        RsfColorPill(
                            color: Color.fromVariant(variants[index]),
                            label: variantLabels[index],
                            isSelected: selectedVariant == variants[index],
                            action: {
                                selectedVariant = variants[index]
                                onVariantChanged(variants[index])
                            }
                        )
                    }
                }
            }
        }
    }
}
