//
//  PresetsManager.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import Foundation

final class PresetsManager: ObservableObject {
    static let shared = PresetsManager()

    @Published private(set) var currentPresetId: String?

    private let defaultPresets: [PresetProfile] = [
        PresetProfile(id: Constants.Presets.work, name: "Work", opacity: 0.3, colorVariant: .redStandard),
        PresetProfile(id: Constants.Presets.gaming, name: "Gaming", opacity: 0.4, colorVariant: .redOrange),
        PresetProfile(id: Constants.Presets.movie, name: "Movie", opacity: 0.5, colorVariant: .redStandard),
        PresetProfile(id: Constants.Presets.sleep, name: "Sleep", opacity: 0.7, colorVariant: .redPink)
    ]

    private init() {}

    func getDefaultPresets() -> [PresetProfile] {
        defaultPresets
    }

    func applyPreset(byId id: String) -> PresetProfile? {
        guard let preset = defaultPresets.first(where: { $0.id == id }) else {
            return nil
        }

        currentPresetId = preset.id
        return preset
    }
}
