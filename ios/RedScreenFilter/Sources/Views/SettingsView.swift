//
//  SettingsView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct SettingsView: View {
    @ObservedObject var viewModel: OverlayViewModel
    @State private var scheduleEnabled: Bool = false
    @State private var startTime = Date()
    @State private var endTime = Date()
    @State private var selectedColorVariant: String = Constants.Colors.redStandard
    
    var body: some View {
        NavigationView {
            Form {
                // Schedule Section
                Section(header: Text("Schedule")) {
                    Toggle("Enable Schedule", isOn: $scheduleEnabled)
                    
                    if scheduleEnabled {
                        DatePicker("Start Time", selection: $startTime, displayedComponents: .hourAndMinute)
                        DatePicker("End Time", selection: $endTime, displayedComponents: .hourAndMinute)
                    }
                }
                
                // Color Section
                Section(header: Text("Color Variant")) {
                    Picker("Select Color", selection: $selectedColorVariant) {
                        Text("Red Standard").tag(Constants.Colors.redStandard)
                        Text("Red Orange").tag(Constants.Colors.redOrange)
                        Text("Red Pink").tag(Constants.Colors.redPink)
                        Text("High Contrast").tag(Constants.Colors.highContrast)
                    }
                    .onChange(of: selectedColorVariant) { newVariant in
                        viewModel.updateColorVariant(newVariant)
                    }
                    
                    // Color Preview
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.fromVariant(selectedColorVariant))
                        .frame(height: 60)
                        .opacity(0.7)
                }
                
                // Future Features
                Section(header: Text("Coming Soon")) {
                    Text("Battery Optimization")
                        .foregroundColor(.secondary)
                    Text("Ambient Light Sensing")
                        .foregroundColor(.secondary)
                    Text("Eye Strain Reminders")
                        .foregroundColor(.secondary)
                }
            }
            .navigationTitle("Settings")
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(viewModel: OverlayViewModel())
    }
}
