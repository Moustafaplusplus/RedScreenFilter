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
    @State private var overlayIsVisible: Bool = false
    
    var body: some View {
        ZStack {
            // Background
            RsfTheme.colors.background
                .ignoresSafeArea()
            
            // Content beneath overlay
            TabView(selection: $selectedTab) {
                // Tab 1: Main Control
                MainControlView(viewModel: viewModel)
                    .tabItem {
                        Label("Control", systemImage: "circle.fill")
                    }
                    .tag(0)
                
                // Tab 2: Presets
                PresetsView(viewModel: viewModel)
                    .tabItem {
                        Label("Presets", systemImage: "square.grid.2x2")
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
            .preferredColorScheme(nil)
            .tint(RsfTheme.colors.primary)
            
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
    @ObservedObject var viewModel: OverlayViewModel
    @StateObject private var presetsManager = PresetsManager.shared
    
    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: RsfTheme.spacing.lg) {
                        // Title
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
                        
                        // Overlay Preview
                        RsfCard {
                            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                                Text("Live Preview")
                                    .font(.headline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(RsfTheme.colors.onSurface)
                                
                                PreviewOverlayView(
                                    opacity: viewModel.opacity,
                                    colorVariant: viewModel.settings.colorVariant
                                )
                            }
                        }
                        
                        // Toggle Button
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
                            
                            Text(viewModel.isEnabled ? "Overlay Active" : "Overlay Inactive")
                                .font(.caption)
                                .foregroundColor(viewModel.isEnabled ? RsfTheme.colors.primary : RsfTheme.colors.onSurfaceVariant)
                                .fontWeight(.semibold)
                        }
                        
                        // Opacity Slider
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
                                    .tint(RsfTheme.colors.primary)
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
                        
                        // Color Variant Selector
                        OverlayColorPicker(
                            selectedVariant: $viewModel.settings.colorVariant,
                            onVariantChanged: { variant in
                                viewModel.updateColorVariant(variant)
                            }
                        )
                        
                        // Quick Presets Carousel
                        RsfCard {
                            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                                HStack {
                                    Text("Quick Presets")
                                        .font(.headline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(RsfTheme.colors.onSurface)
                                    
                                    Spacer()
                                    
                                    NavigationLink(destination: PresetsView(viewModel: viewModel)) {
                                        Text("See All")
                                            .font(.caption)
                                            .foregroundColor(RsfTheme.colors.primary)
                                    }
                                }
                                
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
                        
                        Spacer()
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

    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()
                
                VStack(spacing: RsfTheme.spacing.md) {
                    RsfSectionHeader("Activity Presets", subtitle: "Quick-apply configurations")
                        .padding(.horizontal, RsfTheme.spacing.md)
                        .padding(.top, RsfTheme.spacing.md)
                    
                    ScrollView {
                        VStack(spacing: RsfTheme.spacing.md) {
                            presetButton(title: "Work", subtitle: "30% • Standard", preset: "work")
                            presetButton(title: "Gaming", subtitle: "40% • Red-Orange", preset: "gaming")
                            presetButton(title: "Movie", subtitle: "50% • Standard", preset: "movie")
                            presetButton(title: "Sleep", subtitle: "70% • Red-Pink", preset: "sleep")
                        }
                        .padding(RsfTheme.spacing.md)
                    }
                    
                    Spacer()
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func presetButton(title: String, subtitle: String, preset: String) -> some View {
        Button(action: {
            viewModel.applyPreset(preset)
        }) {
            HStack(spacing: RsfTheme.spacing.md) {
                VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
                    Text(title)
                        .font(.headline)
                        .foregroundColor(RsfTheme.colors.onSurface)
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
                Spacer()
                if viewModel.currentPreset.lowercased() == preset.lowercased() {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(RsfTheme.colors.primary)
                        .font(.system(size: 20))
                }
            }
            .padding(RsfTheme.spacing.md)
            .background(RsfTheme.colors.surfaceVariant)
            .cornerRadius(RsfTheme.radius.lg)
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.lg)
                    .stroke(
                        viewModel.currentPreset.lowercased() == preset.lowercased() ?
                        RsfTheme.colors.primary :
                        RsfTheme.colors.glassStroke,
                        lineWidth: RsfTheme.border.thin
                    )
            )
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

struct PreviewOverlayView: View {
    let opacity: Float
    let colorVariant: String

    var body: some View {
        RoundedRectangle(cornerRadius: RsfTheme.radius.lg)
            .fill(RsfTheme.colors.surfaceVariant)
            .frame(height: 120)
            .overlay(
                Color.fromVariant(colorVariant)
                    .opacity(Double(opacity))
                    .clipShape(RoundedRectangle(cornerRadius: RsfTheme.radius.lg))
            )
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.lg)
                    .stroke(RsfTheme.colors.glassStroke, lineWidth: RsfTheme.border.thin)
            )
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
