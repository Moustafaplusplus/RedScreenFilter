package com.redscreenfilter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.utils.ExactAlarmScheduler

/**
 * Boot Completed Receiver
 * Handles BOOT_COMPLETED broadcast to restore overlay state and scheduling on device startup
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    private val TAG = "BootCompletedReceiver"
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || (intent?.action != Intent.ACTION_BOOT_COMPLETED && intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            return
        }
        
        Log.d(TAG, "onReceive: Boot completed, checking scheduling state")
        
        // Check if scheduling is enabled
        val schedulingManager = SchedulingManager.getInstance(context)
        if (schedulingManager.isScheduleEnabled()) {
            Log.d(TAG, "onReceive: Scheduling is enabled, re-scheduling Alarms")
            ExactAlarmScheduler.scheduleNextAlarm(context)
        }
    }
}
