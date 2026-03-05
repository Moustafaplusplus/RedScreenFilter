//
//  SettingsView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct SettingsView: View {
    @ObservedObject var viewModel: OverlayViewModel
    @State private var showingScheduleHelp = false
    @State private var selectedColor: String = Constants.Colors.redStandard
    @State private var showingStartTimePicker = false
    @State private var showingEndTimePicker = false
    @State private var tempStartTime = Date()
    @State private var tempEndTime = Date()
    
    var body: some View {
        ZStack {
            RsfTheme.colors.background
                .ignoresSafeArea()
            
            NavigationView {
                ScrollView {
                    VStack(spacing: RsfTheme.spacing.lg) {
                        // Title
                        RsfSectionHeader("Settings", subtitle: "Customize your experience")
                            .padding(RsfTheme.spacing.md)
                        
                        // MARK: - Schedule Section
                        SettingsSectionCard(title: "Schedule") {
                            VStack(spacing: RsfTheme.spacing.md) {
                                RsfSwitch(
                                    isOn: Binding(
                                        get: { viewModel.scheduleEnabled },
                                        set: { viewModel.setScheduleEnabled($0) }
                                    ),
                                    label: "Enable Schedule"
                                )
                                
                                if viewModel.scheduleEnabled {
                                    Divider()
                                        .background(RsfTheme.colors.glassStroke)
                                    
                                    HStack {
                                        Text("Start Time")
                                            .foregroundColor(RsfTheme.colors.onSurface)
                                        Spacer()
                                        Text(viewModel.scheduleStartTime)
                                            .font(.body)
                                            .fontWeight(.semibold)
                                            .foregroundColor(RsfTheme.colors.primary)
                                    }
                                    .onTapGesture {
                                        showTimePickerStart()
                                    }
                                    
                                    HStack {
                                        Text("End Time")
                                            .foregroundColor(RsfTheme.colors.onSurface)
                                        Spacer()
                                        Text(viewModel.scheduleEndTime)
                                            .font(.body)
                                            .fontWeight(.semibold)
                                            .foregroundColor(RsfTheme.colors.primary)
                                    }
                                    .onTapGesture {
                                        showTimePickerEnd()
                                    }
                                    
                                    Text("Overlay will be active between start and end times")
                                        .font(.caption)
                                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                }
                            }
                        }
                        
                        // MARK: - Location Section
                        SettingsSectionCard(title: "Location-Based Scheduling") {
                            VStack(spacing: RsfTheme.spacing.md) {
                                RsfSwitch(
                                    isOn: Binding(
                                        get: { viewModel.useLocationSchedule },
                                        set: { viewModel.setLocationScheduleEnabled($0) }
                                    ),
                                    label: "Use Sunset/Sunrise Schedule"
                                )
                                
                                if viewModel.useLocationSchedule {
                                    Divider()
                                        .background(RsfTheme.colors.glassStroke)
                                    
                                    Text("Overlay will activate at sunset and deactivate at sunrise based on your location")
                                        .font(.caption)
                                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    
                                    // Display calculated sunset/sunrise times
                                    if let sunsetTime = viewModel.sunsetTime,
                                       let sunriseTime = viewModel.sunriseTime {
                                        VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
                                            HStack {
                                                Image(systemName: "sunset.fill")
                                                    .foregroundColor(RsfTheme.colors.warning)
                                                Text("Sunset:")
                                                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                                Spacer()
                                                Text(formatTime(sunsetTime))
                                                    .fontWeight(.semibold)
                                                    .foregroundColor(RsfTheme.colors.primary)
                                            }
                                            
                                            HStack {
                                                Image(systemName: "sunrise.fill")
                                                    .foregroundColor(RsfTheme.colors.secondary)
                                                Text("Sunrise:")
                                                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                                Spacer()
                                                Text(formatTime(sunriseTime))
                                                    .fontWeight(.semibold)
                                                    .foregroundColor(RsfTheme.colors.primary)
                                            }
                                        }
                                        .font(.caption)
                                    }
                                    
                                    // Offset slider
                                    VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
                                        HStack {
                                            Text("Sunset offset:")
                                                .font(.caption)
                                                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                            
                                            Spacer()
                                            
                                            Text(formatOffset(viewModel.sunsetOffsetMinutes))
                                                .font(.caption)
                                                .fontWeight(.bold)
                                                .foregroundColor(RsfTheme.colors.primary)
                                        }
                                        
                                        Slider(
                                            value: Binding(
                                                get: { Double(viewModel.sunsetOffsetMinutes) },
                                                set: { viewModel.setSunsetOffset(Int($0)) }
                                            ),
                                            in: -60...60,
                                            step: 5
                                        )
                                        .tint(RsfTheme.colors.primary)
                                        
                                        Text("Activate overlay earlier (-) or later (+) than actual sunset")
                                            .font(.caption2)
                                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    }
                                    
                                    // Refresh location button
                                    Button(action: {
                                        viewModel.refreshLocationTimes()
                                    }) {
                                        HStack {
                                            Image(systemName: "location.fill")
                                            Text("Refresh Location")
                                        }
                                        .font(.caption)
                                        .foregroundColor(RsfTheme.colors.primary)
                                        .padding(.vertical, RsfTheme.spacing.xs)
                                    }
                                }
                            }
                        }
                        
                        // MARK: - Color Variant Section
                        SettingsSectionCard(title: "Color Variant") {
                            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                                Text("Choose the color that works best for you")
                                    .font(.caption)
                                    .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                
                                VStack(spacing: RsfTheme.spacing.sm) {
                                    ForEach(ColorVariant.allCases, id: \.self) { variant in
                                        ColorVariantOptionRow(
                                            variant: variant,
                                            isSelected: viewModel.settings.colorVariant == variant.rawValue,
                                            onSelect: {
                                                viewModel.updateColorVariant(variant.rawValue)
                                            }
                                        )
                                    }
                                }
                                
                                Divider()
                                    .padding(.vertical, RsfTheme.spacing.sm)
                                
                                // Preview strip showing all color variants
                                HStack(spacing: 0) {
                                    ForEach(ColorVariant.allCases, id: \.self) { variant in
                                        Color.fromVariant(variant, opacity: 0.8)
                                            .frame(height: 40)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: RsfTheme.radius.sm)
                                                    .stroke(
                                                        viewModel.settings.colorVariant == variant.rawValue ? RsfTheme.colors.primary : Color.clear,
                                                        lineWidth: 2
                                                    )
                                                    .padding(2)
                                            )
                                    }
                                }
                                .cornerRadius(RsfTheme.radius.md)
                            }
                        }
                        
                        // MARK: - Smart Features Section
                        SettingsSectionCard(title: "Smart Features") {
                            VStack(spacing: RsfTheme.spacing.md) {
                                // Battery Optimization
                                RsfSwitch(
                                    isOn: Binding(
                                        get: { viewModel.batteryOptimizationEnabled },
                                        set: { viewModel.setBatteryOptimizationEnabled($0) }
                                    ),
                                    label: "Battery Optimization"
                                )
                                
                                if viewModel.batteryOptimizationEnabled {
                                    VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
                                        HStack {
                                            Text("Threshold:")
                                                .font(.caption)
                                                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                            
                                            Spacer()
                                            
                                            Text("\(Int(viewModel.batteryOptimizationThreshold * 100))%")
                                                .font(.caption)
                                                .fontWeight(.bold)
                                                .foregroundColor(RsfTheme.colors.primary)
                                        }
                                        
                                        Text("Reduces overlay opacity when battery is below threshold")
                                            .font(.caption)
                                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    }
                                }
                                
                                Divider()
                                    .background(RsfTheme.colors.glassStroke)
                                
                                // Ambient Light Sensing
                                VStack(spacing: RsfTheme.spacing.md) {
                                    RsfSwitch(
                                        isOn: Binding(
                                            get: { viewModel.useAmbientLight },
                                            set: { viewModel.setAmbientLightEnabled($0) }
                                        ),
                                        label: "Ambient Light Sensing"
                                    )
                                    
                                    if viewModel.useAmbientLight {
                                        HStack(spacing: RsfTheme.spacing.sm) {
                                            Image(systemName: "lightbulb.fill")
                                                .foregroundColor(RsfTheme.colors.secondary)
                                            Text("Coming in Phase 55-60%")
                                                .font(.caption)
                                                .foregroundColor(RsfTheme.colors.secondary)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // MARK: - Eye Health Section
                        SettingsSectionCard(title: "Eye Health") {
                            VStack(spacing: RsfTheme.spacing.md) {
                                RsfSwitch(
                                    isOn: Binding(
                                        get: { viewModel.eyeStrainRemindersEnabled },
                                        set: { viewModel.setEyeStrainRemindersEnabled($0) }
                                    ),
                                    label: "20-20-20 Eye Strain Reminders"
                                )
                                
                                if viewModel.eyeStrainRemindersEnabled {
                                    VStack(alignment: .leading, spacing: RsfTheme.spacing.sm) {
                                        HStack {
                                            Text("Remind me every")
                                                .font(.caption)
                                                .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                            
                                            Spacer()
                                            
                                            Picker("Minutes", selection: $viewModel.reminderInterval) {
                                                Text("5 min").tag(5)
                                                Text("10 min").tag(10)
                                                Text("15 min").tag(15)
                                                Text("20 min").tag(20)
                                                Text("25 min").tag(25)
                                                Text("30 min").tag(30)
                                            }
                                            .pickerStyle(.menu)
                                            .tint(RsfTheme.colors.primary)
                                            .onChange(of: viewModel.reminderInterval) { value in
                                                viewModel.setReminderInterval(value)
                                            }
                                        }
                                        
                                        Text("The 20-20-20 rule: Every 20 minutes, look at something 20 feet away for 20 seconds")
                                            .font(.caption)
                                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        
                        // MARK: - Info Section
                        SettingsSectionCard(backgroundColor: RsfTheme.colors.errorContainer) {
                            HStack(spacing: RsfTheme.spacing.md) {
                                Image(systemName: "info.circle.fill")
                                    .foregroundColor(RsfTheme.colors.error)
                                    .font(.system(size: 18))
                                
                                VStack(alignment: .leading, spacing: RsfTheme.spacing.xs) {
                                    Text("In-App Overlay Only")
                                        .font(.caption)
                                        .fontWeight(.semibold)
                                        .foregroundColor(RsfTheme.colors.onSurface)
                                    
                                    Text("The red overlay appears only while you're using this app. When you switch to other apps, the overlay disappears due to iOS limitations.")
                                        .font(.caption)
                                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                                }
                                Spacer()
                            }
                        }
                        
                        Spacer(minLength: RsfTheme.spacing.xl)
                    }
                    .padding(RsfTheme.spacing.md)
                }
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .principal) {
                        Text("Settings")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .foregroundColor(RsfTheme.colors.onBackground)
                    }
                }
            }
            .sheet(isPresented: $showingStartTimePicker) {
                TimePickerSheet(
                    title: "Start Time",
                    selectedTime: $tempStartTime,
                    onSave: {
                        let timeString = dateToTimeString(tempStartTime)
                        viewModel.setScheduleTime(start: timeString, end: viewModel.scheduleEndTime)
                    }
                )
            }
            .sheet(isPresented: $showingEndTimePicker) {
                TimePickerSheet(
                    title: "End Time",
                    selectedTime: $tempEndTime,
                    onSave: {
                        let timeString = dateToTimeString(tempEndTime)
                        viewModel.setScheduleTime(start: viewModel.scheduleStartTime, end: timeString)
                    }
                )
            }
        }
    }
    
    // MARK: - Helper Methods
    
    private func showTimePickerStart() {
        // Parse current start time string to Date
        tempStartTime = timeStringToDate(viewModel.scheduleStartTime)
        showingStartTimePicker = true
    }
    
    private func showTimePickerEnd() {
        // Parse current end time string to Date
        tempEndTime = timeStringToDate(viewModel.scheduleEndTime)
        showingEndTimePicker = true
    }
    
    /// Converts time string (HH:mm) to Date object for DatePicker
    private func timeStringToDate(_ timeString: String) -> Date {
        let components = timeString.split(separator: ":")
        let hour = Int(components.first ?? "0") ?? 0
        let minute = Int(components.last ?? "0") ?? 0
        
        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute
        
        return Calendar.current.date(from: dateComponents) ?? Date()
    }
    
    /// Converts Date object to time string (HH:mm)
    private func dateToTimeString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
    
    /// Formats time for display (e.g., "6:15 PM")
    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        formatter.dateStyle = .none
        return formatter.string(from: date)
    }
    
    /// Formats offset minutes (e.g., "+30 min", "-15 min", "On time")
    private func formatOffset(_ minutes: Int) -> String {
        if minutes == 0 {
            return "On time"
        } else if minutes > 0 {
            return "+\(minutes) min"
        } else {
            return "\(minutes) min"
        }
    }
}

// MARK: - Settings Section Card Helper

struct SettingsSectionCard<Content: View>: View {
    let title: String?
    let backgroundColor: Color
    let content: Content
    
    init(title: String? = nil, backgroundColor: Color = RsfTheme.colors.surfaceVariant, @ViewBuilder content: () -> Content) {
        self.title = title
        self.backgroundColor = backgroundColor
        self.content = content()
    }
    
    var body: some View {
        RsfCard(backgroundColor: backgroundColor, padding: RsfTheme.spacing.md) {
            VStack(alignment: .leading, spacing: RsfTheme.spacing.md) {
                if let title = title {
                    Text(title)
                        .font(.headline)
                        .fontWeight(.semibold)
                        .foregroundColor(RsfTheme.colors.onSurface)
                }
                
                content
            }
        }
    }
}

// MARK: - Time Picker Sheet

struct TimePickerSheet: View {
    let title: String
    @Binding var selectedTime: Date
    let onSave: () -> Void
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()
                
                VStack(spacing: RsfTheme.spacing.lg) {
                    DatePicker(
                        "Select Time",
                        selection: $selectedTime,
                        displayedComponents: .hourAndMinute
                    )
                    .datePickerStyle(.wheel)
                    .labelsHidden()
                    .padding()
                    
                    Spacer()
                }
                .padding()
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(RsfTheme.colors.onSurface)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        onSave()
                        dismiss()
                    }
                    .foregroundColor(RsfTheme.colors.primary)
                    .fontWeight(.semibold)
                }
            }
        }
    }
}

// MARK: - Color Variant Option Row

struct ColorVariantOptionRow: View {
    let variant: ColorVariant
    let isSelected: Bool
    let onSelect: () -> Void
    
    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: RsfTheme.spacing.md) {
                // Color preview circle
                Circle()
                    .fill(Color.fromVariant(variant, opacity: 0.8))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Circle()
                            .stroke(Color.white, lineWidth: 2)
                    )
                    .shadow(radius: 2)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(variant.displayName)
                        .font(.headline)
                        .fontWeight(.semibold)
                        .foregroundColor(RsfTheme.colors.onSurface)
                    
                    Text(descriptionForVariant(variant))
                        .font(.caption)
                        .lineLimit(2)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
                
                Spacer()
                
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.green)
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .fill(isSelected ? RsfTheme.colors.primary.opacity(0.1) : RsfTheme.colors.glass)
            )
            .overlay(
                RoundedRectangle(cornerRadius: RsfTheme.radius.md)
                    .stroke(isSelected ? RsfTheme.colors.primary : RsfTheme.colors.glassStroke, lineWidth: isSelected ? 2 : 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
        .accessibilityElement(children: .combine)
        .accessibilityLabel(variant.displayName)
        .accessibilityValue(isSelected ? "Selected" : "Not selected")
        .accessibilityHint(ColorUtility.accessibilityDescription(for: variant))
    }
    
    private func descriptionForVariant(_ variant: ColorVariant) -> String {
        switch variant {
        case .redStandard:
            return "Standard red for most users"
        case .redOrange:
            return "Warmer tone, comfortable for long sessions"
        case .redPink:
            return "Softer hue, easy on the eyes"
        case .highContrast:
            return "Maximum blue light blocking (min 50% opacity)"
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(viewModel: OverlayViewModel())
    }
}
