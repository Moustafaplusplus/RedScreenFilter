package com.redscreenfilter.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.feature.settings.model.AutomationSettingsState

class AutomationSettingsViewModel(
    private val schedulingManager: SchedulingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun loadState(): AutomationSettingsState {
        val (startHour, startMinute) = schedulingManager.getStartTimeComponents()
        val (endHour, endMinute) = schedulingManager.getEndTimeComponents()
        return AutomationSettingsState(
            isSchedulingEnabled = schedulingManager.isScheduleEnabled(),
            scheduleStartLabel = schedulingManager.formatTime(startHour, startMinute),
            scheduleEndLabel = schedulingManager.formatTime(endHour, endMinute),
            isLocationSchedulingEnabled = schedulingManager.isLocationScheduleEnabled(),
            locationOffsetMinutes = schedulingManager.getLocationOffset(),
            sunsetTime = schedulingManager.getCalculatedSunsetTime(),
            sunriseTime = schedulingManager.getCalculatedSunriseTime()
        )
    }

    fun onSchedulingToggled(isEnabled: Boolean): AutomationSettingsState {
        schedulingManager.setScheduleEnabled(isEnabled)
        return loadState()
    }

    fun onLocationSchedulingToggled(isEnabled: Boolean): AutomationSettingsState {
        schedulingManager.setLocationScheduleEnabled(isEnabled)
        return loadState()
    }

    fun onLocationOffsetChanged(offsetMinutes: Int): AutomationSettingsState {
        schedulingManager.setLocationOffset(offsetMinutes)
        return loadState()
    }

    fun onStartTimeChanged(selectedHour: Int, selectedMinute: Int): AutomationSettingsState {
        val updatedStart = schedulingManager.formatTime(selectedHour, selectedMinute)
        schedulingManager.setSchedule(updatedStart, preferencesManager.getScheduleEndTime())
        return loadState()
    }

    fun onEndTimeChanged(selectedHour: Int, selectedMinute: Int): AutomationSettingsState {
        val updatedEnd = schedulingManager.formatTime(selectedHour, selectedMinute)
        schedulingManager.setSchedule(preferencesManager.getScheduleStartTime(), updatedEnd)
        return loadState()
    }

    fun shouldOverlayBeActiveNow(): Boolean = schedulingManager.getScheduledState()

    fun isOverlayCurrentlyActive(): Boolean = preferencesManager.isOverlayEnabled()

    fun setOverlayCurrentlyActive(isEnabled: Boolean) {
        preferencesManager.setOverlayEnabled(isEnabled)
    }
}
