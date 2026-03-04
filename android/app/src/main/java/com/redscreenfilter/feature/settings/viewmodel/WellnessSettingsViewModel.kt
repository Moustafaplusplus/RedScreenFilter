package com.redscreenfilter.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.feature.settings.model.WellnessSettingsState

class WellnessSettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun loadState(): WellnessSettingsState {
        val sensitivityValue = when (preferencesManager.getLightSensorSensitivity()) {
            "low" -> 0f
            "medium" -> 1f
            "high" -> 2f
            else -> 1f
        }
        return WellnessSettingsState(
            isBatteryOptimizationEnabled = preferencesManager.getBatteryOptimizationEnabled(),
            isLightSensorEnabled = preferencesManager.isLightSensorEnabled(),
            lightSensitivityValue = sensitivityValue,
            isLightSensorLocked = preferencesManager.isLightSensorLocked(),
            isEyeStrainReminderEnabled = preferencesManager.isEyeStrainReminderEnabled(),
            notificationStyle = preferencesManager.getEyeStrainNotificationStyle()
        )
    }

    fun onBatteryOptimizationToggled(isEnabled: Boolean): WellnessSettingsState {
        preferencesManager.setBatteryOptimizationEnabled(isEnabled)
        return loadState()
    }

    fun onLightSensorToggled(isEnabled: Boolean): WellnessSettingsState {
        preferencesManager.setLightSensorEnabled(isEnabled)
        return loadState()
    }

    fun onLightSensitivityChanged(value: Float): WellnessSettingsState {
        val sensitivity = when (value.toInt()) {
            0 -> "low"
            1 -> "medium"
            else -> "high"
        }
        preferencesManager.setLightSensorSensitivity(sensitivity)
        return loadState()
    }

    fun onLightSensorLockToggled(isLocked: Boolean): WellnessSettingsState {
        preferencesManager.setLightSensorLocked(isLocked)
        return loadState()
    }

    fun onEyeStrainReminderToggled(isEnabled: Boolean): WellnessSettingsState {
        preferencesManager.setEyeStrainReminderEnabled(isEnabled)
        return loadState()
    }

    fun onNotificationStyleChanged(checkedId: Int, vibrationId: Int, silentId: Int): WellnessSettingsState {
        val style = when (checkedId) {
            vibrationId -> "vibration"
            silentId -> "silent"
            else -> "sound"
        }
        preferencesManager.setEyeStrainNotificationStyle(style)
        return loadState()
    }

    fun onNotificationStyleChanged(style: String): WellnessSettingsState {
        preferencesManager.setEyeStrainNotificationStyle(style)
        return loadState()
    }
}
