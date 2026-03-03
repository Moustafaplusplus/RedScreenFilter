package com.redscreenfilter.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.service.RedOverlayService

/**
 * ScheduleWorker
 * Periodic background worker that checks if overlay should be active based on schedule
 * Runs every 15 minutes and is resilient to Doze mode
 */
class ScheduleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "ScheduleWorker"
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: ScheduleWorker executing")
        
        try {
            val schedulingManager = SchedulingManager.getInstance(applicationContext)
            val preferencesManager = PreferencesManager.getInstance(applicationContext)
            
            // Check if scheduling is enabled
            if (!schedulingManager.isScheduleEnabled()) {
                Log.d(TAG, "doWork: Scheduling is disabled, skipping")
                return Result.success()
            }
            
            // Get scheduled state (should overlay be active?)
            val shouldBeActive = schedulingManager.getScheduledState()
            val isCurrentlyActive = preferencesManager.isOverlayEnabled()
            
            Log.d(TAG, "doWork: shouldBeActive=$shouldBeActive, isCurrentlyActive=$isCurrentlyActive")
            
            // Check if we have overlay permission
            if (!hasOverlayPermission()) {
                Log.w(TAG, "doWork: No overlay permission, cannot start service")
                return Result.success()
            }
            
            // Update overlay state if needed
            if (shouldBeActive && !isCurrentlyActive) {
                // Schedule says overlay should be active, but it's not
                Log.d(TAG, "doWork: Starting overlay service per schedule")
                startOverlayService()
                preferencesManager.setOverlayEnabled(true)
            } else if (!shouldBeActive && isCurrentlyActive) {
                // Schedule says overlay should be inactive, but it's active
                Log.d(TAG, "doWork: Stopping overlay service per schedule")
                stopOverlayService()
                preferencesManager.setOverlayEnabled(false)
            } else {
                Log.d(TAG, "doWork: Overlay state matches schedule, no action needed")
            }
            
            return Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error in ScheduleWorker", e)
            return Result.retry()
        }
    }
    
    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(applicationContext)
        } else {
            true
        }
    }
    
    private fun startOverlayService() {
        val intent = Intent(applicationContext, RedOverlayService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startOverlayService: Error starting service", e)
        }
    }
    
    private fun stopOverlayService() {
        val intent = Intent(applicationContext, RedOverlayService::class.java)
        try {
            applicationContext.stopService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "stopOverlayService: Error stopping service", e)
        }
    }
}
