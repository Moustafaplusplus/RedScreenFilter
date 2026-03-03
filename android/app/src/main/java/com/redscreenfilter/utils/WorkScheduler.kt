package com.redscreenfilter.utils

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.redscreenfilter.worker.ScheduleWorker
import java.util.concurrent.TimeUnit

/**
 * WorkScheduler
 * Manages WorkManager scheduling for periodic overlay state checks
 * Ensures overlay follows scheduled times even when app is in background
 */
object WorkScheduler {
    
    private const val TAG = "WorkScheduler"
    private const val SCHEDULE_WORK_NAME = "red_screen_filter_schedule_work"
    
    // WorkManager minimum interval is 15 minutes
    private const val REPEAT_INTERVAL_MINUTES = 15L
    
    /**
     * Schedule periodic work to check and update overlay state
     * Uses PeriodicWorkRequest with 15-minute interval
     */
    fun schedulePeriodicWork(context: Context) {
        Log.d(TAG, "schedulePeriodicWork: Scheduling periodic schedule check")
        
        // Create constraints for the work
        // Battery not low is optional - we want it to run even on low battery
        // But we can add constraints if needed for optimization
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false) // Run even on low battery
            .build()
        
        // Create periodic work request
        // Minimum interval for PeriodicWorkRequest is 15 minutes
        val scheduleWorkRequest = PeriodicWorkRequestBuilder<ScheduleWorker>(
            REPEAT_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SCHEDULE_WORK_NAME)
            .build()
        
        // Enqueue the work with REPLACE policy
        // This ensures only one instance of the work is running
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SCHEDULE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            scheduleWorkRequest
        )
        
        Log.d(TAG, "schedulePeriodicWork: Periodic work scheduled successfully")
    }
    
    /**
     * Cancel periodic work
     * Call this when scheduling is disabled
     */
    fun cancelPeriodicWork(context: Context) {
        Log.d(TAG, "cancelPeriodicWork: Cancelling periodic schedule check")
        WorkManager.getInstance(context).cancelUniqueWork(SCHEDULE_WORK_NAME)
        Log.d(TAG, "cancelPeriodicWork: Periodic work cancelled")
    }
    
    /**
     * Check if periodic work is scheduled
     */
    fun isWorkScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SCHEDULE_WORK_NAME)
            .get()
        
        return workInfos?.any { !it.state.isFinished } ?: false
    }
}
