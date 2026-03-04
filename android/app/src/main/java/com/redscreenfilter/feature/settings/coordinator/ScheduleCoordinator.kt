package com.redscreenfilter.feature.settings.coordinator

import android.content.Context
import com.redscreenfilter.feature.settings.viewmodel.AutomationSettingsViewModel
import com.redscreenfilter.utils.WorkScheduler

class ScheduleCoordinator(
    private val overlayControlCoordinator: OverlayControlCoordinator,
    private val permissionCoordinator: PermissionCoordinator,
    private val automationSettingsViewModel: AutomationSettingsViewModel
) {

    fun onSchedulingToggled(context: Context, isEnabled: Boolean) {
        if (isEnabled) {
            WorkScheduler.schedulePeriodicWork(context)
            applyScheduleNow(context)
        } else {
            WorkScheduler.cancelPeriodicWork(context)
        }
    }

    fun applyScheduleNow(context: Context) {
        val shouldBeActive = automationSettingsViewModel.shouldOverlayBeActiveNow()
        val isCurrentlyActive = automationSettingsViewModel.isOverlayCurrentlyActive()

        if (shouldBeActive && !isCurrentlyActive && permissionCoordinator.hasOverlayPermission(context)) {
            overlayControlCoordinator.startOverlayService()
            automationSettingsViewModel.setOverlayCurrentlyActive(true)
        } else if (!shouldBeActive && isCurrentlyActive) {
            overlayControlCoordinator.stopOverlayService()
            automationSettingsViewModel.setOverlayCurrentlyActive(false)
        }
    }
}
