package com.redscreenfilter.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.redscreenfilter.core.model.ColorVariant
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.feature.settings.model.DisplaySettingsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class DisplaySettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Expose reactive state flows from PreferencesManager
    val overlayEnabledFlow: StateFlow<Boolean> = preferencesManager.overlayEnabledFlow
    val opacityFlow: StateFlow<Float> = preferencesManager.opacityFlow

    fun loadState(): DisplaySettingsState {
        val opacity = preferencesManager.getOpacity()
        return DisplaySettingsState(
            isOverlayEnabled = preferencesManager.isOverlayEnabled(),
            opacity = opacity,
            opacityPercentage = (opacity * 100).toInt(),
            colorVariant = ColorVariant.fromString(preferencesManager.getColorVariant())
        )
    }

    fun onOverlayToggled(isEnabled: Boolean): DisplaySettingsState {
        preferencesManager.setOverlayEnabled(isEnabled)
        return loadState()
    }

    fun onOpacityChanged(percentage: Int): DisplaySettingsState {
        val opacity = percentage / 100f
        preferencesManager.setOpacity(opacity)
        return loadState()
    }

    fun onColorVariantChanged(variant: ColorVariant): DisplaySettingsState {
        preferencesManager.setColorVariant(variant.name)
        return loadState()
    }
}
