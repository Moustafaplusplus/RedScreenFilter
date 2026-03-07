package com.redscreenfilter.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.receiver.ScheduleReceiver
import java.util.Calendar

/**
 * ExactAlarmScheduler
 * Manages high-precision scheduling for the overlay using AlarmManager.
 * Replaces or augments the periodic WorkManager check with exact transition events.
 */
object ExactAlarmScheduler {
    
    private const val TAG = "ExactAlarmScheduler"
    private const val REQUEST_CODE_SCHEDULE = 1001

    /**
     * Schedules the next transition alarm (either to turn ON or turn OFF)
     */
    fun scheduleNextAlarm(context: Context) {
        val schedulingManager = SchedulingManager.getInstance(context)
        
        if (!schedulingManager.isScheduleEnabled()) {
            cancelAlarms(context)
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTransitionMillis = schedulingManager.getNextTransitionTimeMillis()
        
        if (nextTransitionMillis == -1L) {
            Log.w(TAG, "scheduleNextAlarm: Could not calculate next transition")
            return
        }
        
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            action = ScheduleReceiver.ACTION_SCHEDULE_ALARM
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCHEDULE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Exact alarm handling for different Android versions
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTransitionMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "scheduleNextAlarm: Scheduled exact alarm for ${java.util.Date(nextTransitionMillis)}")
                } else {
                    Log.w(TAG, "scheduleNextAlarm: Cannot schedule exact alarms, falling back to setWindow")
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        nextTransitionMillis,
                        1000 * 60, // 1 minute window
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTransitionMillis,
                    pendingIntent
                )
                Log.d(TAG, "scheduleNextAlarm: Scheduled exact alarm for ${java.util.Date(nextTransitionMillis)}")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTransitionMillis,
                    pendingIntent
                )
                Log.d(TAG, "scheduleNextAlarm: Scheduled exact alarm for ${java.util.Date(nextTransitionMillis)}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "scheduleNextAlarm: SecurityException scheduling alarm", e)
            // Fallback to non-exact if permission is missing
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextTransitionMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancels any pending schedule alarms
     */
    fun cancelAlarms(context: Context) {
        Log.d(TAG, "cancelAlarms: Cancelling any pending alarms")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            action = ScheduleReceiver.ACTION_SCHEDULE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCHEDULE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
