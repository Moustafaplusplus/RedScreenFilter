package com.redscreenfilter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.redscreenfilter.MainActivity
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.service.RedOverlayService
import com.redscreenfilter.utils.ExactAlarmScheduler

/**
 * ScheduleReceiver
 * Handles exact alarms for starting and stopping the overlay service based on schedule
 */
class ScheduleReceiver : BroadcastReceiver() {

    private val TAG = "ScheduleReceiver"
    
    companion object {
        const val ACTION_SCHEDULE_ALARM = "com.redscreenfilter.action.SCHEDULE_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SCHEDULE_ALARM) return
        
        Log.d(TAG, "onReceive: Schedule alarm triggered")
        
        val schedulingManager = SchedulingManager.getInstance(context)
        val preferencesManager = PreferencesManager.getInstance(context)
        
        // 1. Check if scheduling is still enabled
        if (!schedulingManager.isScheduleEnabled()) {
            Log.d(TAG, "onReceive: Scheduling is disabled, skipping")
            return
        }
        
        // 2. Determine what state we should be in NOW
        val shouldBeActive = schedulingManager.getScheduledState()
        val isCurrentlyActive = preferencesManager.isOverlayEnabled()
        
        Log.d(TAG, "onReceive: shouldBeActive=$shouldBeActive, isCurrentlyActive=$isCurrentlyActive")
        
        // 3. Update overlay state if needed
        if (shouldBeActive && !isCurrentlyActive) {
            if (hasOverlayPermission(context)) {
                Log.d(TAG, "onReceive: Starting overlay service")
                // Update preference BEFORE starting service to avoid race condition
                preferencesManager.setOverlayEnabled(true)
                startOverlayService(context)
                broadcastStateChange(context)
            } else {
                Log.w(TAG, "onReceive: No overlay permission")
            }
        } else if (!shouldBeActive && isCurrentlyActive) {
            // Update preference BEFORE stopping/updating service
            preferencesManager.setOverlayEnabled(false)
            
            if (!preferencesManager.isExtraDimEnabled()) {
                Log.d(TAG, "onReceive: Stopping overlay service")
                stopOverlayService(context)
            } else {
                Log.d(TAG, "onReceive: Not stopping service - Extra Dim is enabled, refreshing UI")
                // Refresh service to apply current state (overlay OFF, Extra Dim ON)
                startOverlayService(context)
            }
            broadcastStateChange(context)
        }
        
        // 4. Schedule the NEXT alarm (either the next start or next stop)
        ExactAlarmScheduler.scheduleNextAlarm(context)
    }
    
    private fun broadcastStateChange(context: Context) {
        val intent = Intent(MainActivity.ACTION_OVERLAY_STATE_CHANGED).apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }
    
    private fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    private fun startOverlayService(context: Context) {
        val serviceIntent = Intent(context, RedOverlayService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startOverlayService: Error", e)
        }
    }
    
    private fun stopOverlayService(context: Context) {
        val serviceIntent = Intent(context, RedOverlayService::class.java)
        try {
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "stopOverlayService: Error", e)
        }
    }
}
