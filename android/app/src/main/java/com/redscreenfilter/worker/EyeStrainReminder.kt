package com.redscreenfilter.worker

import android.content.Context
import android.app.KeyguardManager
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redscreenfilter.R
import com.redscreenfilter.data.PreferencesManager

/**
 * Worker for periodic 20-20-20 eye strain reminders.
 * Runs every 20 minutes (as configured in WorkManager).
 * Sends local notification with customizable sound/vibration.
 *
 * Respects:
 * - User's notification style preference (Sound/Vibration/Silent)
 * - Active video call status (skips reminder if in call)
 * - User's enabled preference
 */
class EyeStrainReminder(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "EyeStrainReminder"
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_NAME_REMINDERS = "Health Reminders"
        const val CHANNEL_ID_REMINDERS = "com.redscreenfilter.reminders"
    }
    
    private val preferencesManager = PreferencesManager.getInstance(context)
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "doWork: 20-20-20 eye strain reminder check")
            
            // Check if reminders are enabled
            if (!preferencesManager.isEyeStrainReminderEnabled()) {
                Log.d(TAG, "doWork: Eye strain reminders disabled")
                return Result.success()
            }
            
            // Check if user is in a video call - skip reminder if active
            if (isInVideoCall()) {
                Log.d(TAG, "doWork: User is in video call, skipping reminder")
                return Result.success()
            }

            // Only remind when user is actively using phone
            if (!isUserActivelyUsingPhone()) {
                Log.d(TAG, "doWork: Device is not actively in use, skipping reminder")
                return Result.success()
            }
            
            // Send notification
            sendReminderNotification()
            
            Log.d(TAG, "doWork: Reminder notification sent successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error sending reminder", e)
            // Retry on exception
            Result.retry()
        }
    }
    
    /**
     * Check if user is currently in an active video call
     * Uses TelecomManager to detect call state
     */
    private fun isInVideoCall(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                telecomManager?.isInCall == true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "isInVideoCall: Error checking call state", e)
            false
        }
    }

    /**
     * Consider phone actively used when screen is interactive and not locked.
     */
    private fun isUserActivelyUsingPhone(): Boolean {
        return try {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val keyguardManager = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

            val isInteractive = powerManager?.isInteractive == true
            val isLocked = keyguardManager?.isKeyguardLocked == true
            isInteractive && !isLocked
        } catch (e: Exception) {
            Log.w(TAG, "isUserActivelyUsingPhone: Error checking device active state", e)
            false
        }
    }
    
    /**
     * Send the 20-20-20 reminder notification
     * Style (sound/vibration/silent) is configurable
     */
    private fun sendReminderNotification() {
        val context = applicationContext
        val style = preferencesManager.getEyeStrainNotificationStyle()
        
        // Create notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.eye_strain_reminder_title))
            .setContentText(context.getString(R.string.eye_strain_reminder_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        // Apply notification style (sound/vibration/silent)
        when (style) {
            "sound" -> {
                // Add subtle system sound
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(soundUri)
            }
            "vibration" -> {
                // Vibration: short pulse
                builder.setVibrate(longArrayOf(0, 300))
            }
            "silent" -> {
                // Silent - no sound or vibration
                builder.setSound(null)
                builder.setVibrate(null)
            }
        }
        
        // Send notification
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }
}
