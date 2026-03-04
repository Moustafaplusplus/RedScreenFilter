package com.redscreenfilter.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.feature.settings.model.BrightnessSettingsState

class BrightnessSettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun loadState(): BrightnessSettingsState {
        val brightness = preferencesManager.getScreenBrightness()
        val extraDimIntensity = preferencesManager.getExtraDimIntensity()
        return BrightnessSettingsState(
            brightness = brightness,
            brightnessPercentage = (brightness * 100).toInt(),
            isExtraDimEnabled = preferencesManager.isExtraDimEnabled(),
            extraDimIntensity = extraDimIntensity,
            extraDimIntensityPercentage = (extraDimIntensity * 100).toInt()
        )
    }

    fun onBrightnessChanged(percentage: Int): BrightnessSettingsState {
        val brightness = (percentage / 100f).coerceIn(0.01f, 1f)
        preferencesManager.setScreenBrightness(brightness)
        return loadState()
    }

    fun onExtraDimToggled(enabled: Boolean): BrightnessSettingsState {
        preferencesManager.setExtraDimEnabled(enabled)
        return loadState()
    }

    fun onExtraDimIntensityChanged(percentage: Int): BrightnessSettingsState {
        val intensity = (percentage / 100f).coerceIn(0f, 1f)
        preferencesManager.setExtraDimIntensity(intensity)
        return loadState()
    }
}
