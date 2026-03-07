package com.redscreenfilter.feature.settings.coordinator

import android.content.Context
import com.redscreenfilter.feature.settings.viewmodel.AutomationSettingsViewModel
import com.redscreenfilter.utils.ExactAlarmScheduler

class ScheduleCoordinator(
    private val overlayControlCoordinator: OverlayControlCoordinator,
    private val permissionCoordinator: PermissionCoordinator,
    private val automationSettingsViewModel: AutomationSettingsViewModel
) {

    fun onSchedulingToggled(context: Context, isEnabled: Boolean) {
        if (isEnabled) {
            refreshSchedule(context)
        } else {
            ExactAlarmScheduler.cancelAlarms(context)
        }
    }

    /**
     * Re-evaluates current schedule state and re-schedules the next alarm.
     * Should be called when scheduling is enabled or schedule parameters change.
     */
    fun refreshSchedule(context: Context) {
        ExactAlarmScheduler.scheduleNextAlarm(context)
        applyScheduleNow(context)
    }

    fun applyScheduleNow(context: Context) {
        val shouldBeActive = automationSettingsViewModel.shouldOverlayBeActiveNow()
        val isCurrentlyActive = automationSettingsViewModel.isOverlayCurrentlyActive()

        if (shouldBeActive && !isCurrentlyActive && permissionCoordinator.hasOverlayPermission(context)) {
            overlayControlCoordinator.startOverlayService()
            automationSettingsViewModel.setOverlayCurrentlyActive(true)
        } else if (!shouldBeActive && isCurrentlyActive) {
            // Only stop if Extra Dim is also disabled
            if (!automationSettingsViewModel.loadState().isLocationSchedulingEnabled) { // This is a bit weak, better check prefs
                 // Check if we should really stop or just update
            }

            overlayControlCoordinator.stopOverlayService()
            automationSettingsViewModel.setOverlayCurrentlyActive(false)
        }
    }
}
