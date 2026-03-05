//
//  PresetsView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct PresetsView: View {
    @ObservedObject var viewModel: OverlayViewModel
    @StateObject private var presetsManager = PresetsManager.shared
    
    @State private var showingCreatePreset = false
    @State private var showingEditPreset = false
    @State private var selectedPresetForEdit: PresetProfile?
    @State private var showingDeleteConfirmation = false
    @State private var presetToDelete: PresetProfile?
    
    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // Title
                        RsfSectionHeader("Presets", subtitle: "Quick filter configurations")
                            .padding(.horizontal)
                        
                        // Default Presets Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Default Presets")
                                .font(.headline)
                                .fontWeight(.semibold)
                                .foregroundColor(RsfTheme.colors.onSurface)
                                .padding(.horizontal)
                            
                            VStack(spacing: 12) {
                                ForEach(presetsManager.getDefaultPresets()) { preset in
                                    PresetCard(
                                        preset: preset,
                                        isSelected: presetsManager.currentPresetId == preset.id,
                                        onTap: {
                                            applyPreset(preset)
                                        },
                                        onEdit: nil, // Default presets cannot be edited
                                        onDelete: nil // Default presets cannot be deleted
                                    )
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        // Custom Presets Section
                        let customPresets = presetsManager.getCustomPresets()
                        if !customPresets.isEmpty {
                            Divider()
                                .padding(.vertical, 8)
                            
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Custom Presets")
                                    .font(.headline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(RsfTheme.colors.onSurface)
                                    .padding(.horizontal)
                                
                                VStack(spacing: 12) {
                                    ForEach(customPresets) { preset in
                                        PresetCard(
                                            preset: preset,
                                            isSelected: presetsManager.currentPresetId == preset.id,
                                            onTap: {
                                                applyPreset(preset)
                                            },
                                            onEdit: {
                                                selectedPresetForEdit = preset
                                                showingEditPreset = true
                                            },
                                            onDelete: {
                                                presetToDelete = preset
                                                showingDeleteConfirmation = true
                                            }
                                        )
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                        
                        // Create New Preset Button
                        Button(action: {
                            showingCreatePreset = true
                        }) {
                            HStack {
                                Image(systemName: "plus.circle.fill")
                                    .font(.title3)
                                Text("Create Custom Preset")
                                    .font(.headline)
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(RsfTheme.colors.primary)
                            .cornerRadius(12)
                        }
                        .padding(.horizontal)
                        .padding(.top, 8)
                        
                        Spacer(minLength: 60)
                    }
                    .padding(.vertical)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .sheet(isPresented: $showingCreatePreset) {
                PresetEditorView(
                    mode: .create,
                    onSave: { newPreset in
                        if presetsManager.createPreset(newPreset) {
                            showingCreatePreset = false
                        }
                    }
                )
            }
            .sheet(isPresented: $showingEditPreset) {
                if let preset = selectedPresetForEdit {
                    PresetEditorView(
                        mode: .edit(preset),
                        onSave: { updatedPreset in
                            if presetsManager.updatePreset(updatedPreset) {
                                showingEditPreset = false
                                selectedPresetForEdit = nil
                            }
                        }
                    )
                }
            }
            .alert("Delete Preset", isPresented: $showingDeleteConfirmation) {
                Button("Cancel", role: .cancel) {
                    presetToDelete = nil
                }
                Button("Delete", role: .destructive) {
                    if let preset = presetToDelete {
                        presetsManager.deletePreset(byId: preset.id)
                        presetToDelete = nil
                    }
                }
            } message: {
                if let preset = presetToDelete {
                    Text("Are you sure you want to delete '\(preset.name)'?")
                }
            }
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

// MARK: - Preset Card Component

struct PresetCard: View {
    let preset: PresetProfile
    let isSelected: Bool
    let onTap: () -> Void
    let onEdit: (() -> Void)?
    let onDelete: (() -> Void)?
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 16) {
                // Color variant icon
                Image(systemName: preset.colorVariant.iconName)
                    .font(.system(size: 24))
                    .foregroundColor(colorForVariant(preset.colorVariant))
                    .frame(width: 40)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(preset.name)
                        .font(.headline)
                        .fontWeight(.semibold)
                        .foregroundColor(RsfTheme.colors.onSurface)
                    
                    Text(preset.description)
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                        .lineLimit(2)
                    
                    HStack(spacing: 8) {
                        Text("Opacity:")
                            .font(.caption2)
                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                        Text(preset.opacityPercentage)
                            .font(.caption2)
                            .fontWeight(.bold)
                            .foregroundColor(RsfTheme.colors.primary)
                        
                        Text("•")
                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                        
                        Text(preset.colorVariant.displayName)
                            .font(.caption2)
                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                    }
                }
                
                Spacer()
                
                // Action buttons for custom presets
                if onEdit != nil || onDelete != nil {
                    HStack(spacing: 12) {
                        if let onEdit = onEdit {
                            Button(action: onEdit) {
                                Image(systemName: "pencil.circle.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(RsfTheme.colors.primary)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        
                        if let onDelete = onDelete {
                            Button(action: onDelete) {
                                Image(systemName: "trash.circle.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.red)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }
                
                // Selection indicator
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.green)
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isSelected ? RsfTheme.colors.primary.opacity(0.1) : RsfTheme.colors.glass)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? RsfTheme.colors.primary : RsfTheme.colors.glassStroke, lineWidth: isSelected ? 2 : 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func colorForVariant(_ variant: ColorVariant) -> Color {
        let components = variant.colorComponents
        return Color(red: Double(components.red), green: Double(components.green), blue: Double(components.blue))
    }
}

// MARK: - Preset Editor View

struct PresetEditorView: View {
    enum Mode {
        case create
        case edit(PresetProfile)
    }
    
    let mode: Mode
    let onSave: (PresetProfile) -> Void
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var name: String = ""
    @State private var description: String = ""
    @State private var opacity: Float = 0.5
    @State private var colorVariant: ColorVariant = .redStandard
    
    var body: some View {
        NavigationView {
            ZStack {
                RsfTheme.colors.background
                    .ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 24) {
                        // Name Input
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Preset Name")
                                .font(.headline)
                                .foregroundColor(RsfTheme.colors.onSurface)
                            
                            TextField("e.g., Night Reading", text: $name)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                        }
                        
                        // Description Input
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Description")
                                .font(.headline)
                                .foregroundColor(RsfTheme.colors.onSurface)
                            
                            TextField("Describe this preset", text: $description)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                        }
                        
                        // Opacity Slider
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text("Opacity")
                                    .font(.headline)
                                    .foregroundColor(RsfTheme.colors.onSurface)
                                Spacer()
                                Text("\(Int(opacity * 100))%")
                                    .font(.headline)
                                    .foregroundColor(RsfTheme.colors.primary)
                            }
                            
                            Slider(value: $opacity, in: 0.1...1.0, step: 0.05)
                                .tint(RsfTheme.colors.primary)
                        }
                        
                        // Color Variant Picker
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Color Variant")
                                .font(.headline)
                                .foregroundColor(RsfTheme.colors.onSurface)
                            
                            ForEach(ColorVariant.allCases, id: \.self) { variant in
                                ColorVariantOption(
                                    variant: variant,
                                    isSelected: colorVariant == variant,
                                    onSelect: {
                                        colorVariant = variant
                                    }
                                )
                            }
                        }
                        
                        Spacer()
                    }
                    .padding()
                }
            }
            .navigationTitle(mode.isEditMode ? "Edit Preset" : "Create Preset")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        savePreset()
                    }
                    .disabled(name.isEmpty)
                }
            }
            .onAppear {
                if case .edit(let preset) = mode {
                    name = preset.name
                    description = preset.description
                    opacity = preset.opacity
                    colorVariant = preset.colorVariant
                }
            }
        }
    }
    
    private func savePreset() {
        let preset: PresetProfile
        
        switch mode {
        case .create:
            preset = PresetProfile(
                name: name,
                opacity: opacity,
                colorVariant: colorVariant,
                description: description,
                isDefault: false
            )
        case .edit(let existingPreset):
            preset = PresetProfile(
                id: existingPreset.id,
                name: name,
                opacity: opacity,
                colorVariant: colorVariant,
                description: description,
                isDefault: existingPreset.isDefault
            )
        }
        
        onSave(preset)
    }
}

extension PresetEditorView.Mode {
    var isEditMode: Bool {
        if case .edit = self {
            return true
        }
        return false
    }
}

// MARK: - Color Variant Option

struct ColorVariantOption: View {
    let variant: ColorVariant
    let isSelected: Bool
    let onSelect: () -> Void
    
    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 16) {
                // Color preview circle
                Circle()
                    .fill(colorForVariant(variant))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Circle()
                            .stroke(Color.white, lineWidth: 2)
                    )
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(variant.displayName)
                        .font(.headline)
                        .foregroundColor(RsfTheme.colors.onSurface)
                    
                    Text(descriptionForVariant(variant))
                        .font(.caption)
                        .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                }
                
                Spacer()
                
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isSelected ? RsfTheme.colors.primary.opacity(0.1) : RsfTheme.colors.glass)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? RsfTheme.colors.primary : RsfTheme.colors.glassStroke, lineWidth: isSelected ? 2 : 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func colorForVariant(_ variant: ColorVariant) -> Color {
        let components = variant.colorComponents
        return Color(red: Double(components.red), green: Double(components.green), blue: Double(components.blue))
    }
    
    private func descriptionForVariant(_ variant: ColorVariant) -> String {
        switch variant {
        case .redStandard:
            return "Standard red filter"
        case .redOrange:
            return "Warmer tone, comfortable for long sessions"
        case .redPink:
            return "Softer hue, easy on the eyes"
        case .highContrast:
            return "Maximum blue light blocking"
        }
    }
}

struct PresetsView_Previews: PreviewProvider {
    static var previews: some View {
        PresetsView(viewModel: OverlayViewModel())
    }
}
