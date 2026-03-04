package com.redscreenfilter.feature.settings.coordinator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

class BrightnessControlCoordinator {

    private val TAG = "BrightnessControlCoord"

    fun canWriteSystemSettings(context: Context): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val canWrite = Settings.System.canWrite(context)
            Log.d(TAG, "canWriteSystemSettings: Android ${Build.VERSION.SDK_INT}, canWrite=$canWrite")
            canWrite
        } else {
            Log.d(TAG, "canWriteSystemSettings: Android version ${Build.VERSION.SDK_INT} < M, returning true")
            true
        }
        
        Log.d(TAG, "canWriteSystemSettings result: $hasPermission")
        return hasPermission
    }

    fun requestWriteSettingsPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Log.d(TAG, "Requesting WRITE_SETTINGS permission")
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
                Log.d(TAG, "Started WRITE_SETTINGS settings activity")
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting write settings permission", e)
                // Fallback for some devices/OS versions
                try {
                    Log.d(TAG, "Trying fallback WRITE_SETTINGS intent")
                    activity.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
                } catch (e2: Exception) {
                    Log.e(TAG, "Second attempt failed", e2)
                }
            }
        } else {
            Log.d(TAG, "requestWriteSettingsPermission: Device is < Android 6.0, permission not needed")
        }
    }

    fun applyBrightness(context: Context, brightness: Float): Boolean {
        val clampedBrightness = brightness.coerceIn(0.01f, 1f)
        Log.d(TAG, "applyBrightness called with brightness=$brightness (clamped=$clampedBrightness)")
        
        // 1. Apply to current activity window for immediate feedback
        if (context is Activity) {
            try {
                val params = context.window.attributes
                params.screenBrightness = clampedBrightness
                context.window.attributes = params
                Log.d(TAG, "Successfully applied brightness to activity window: $clampedBrightness")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply to activity window", e)
            }
        }

        // 2. Try to apply to system settings
        val hasPermission = canWriteSystemSettings(context)
        Log.d(TAG, "applyBrightness: hasPermission=$hasPermission")
        
        if (!hasPermission) {
            Log.w(TAG, "Cannot write system settings - permission missing. Brightness limited to current window only.")
            return false
        }

        try {
            val systemValue = (clampedBrightness * 255).toInt().coerceIn(1, 255)
            Log.d(TAG, "applyBrightness: Setting system brightness to $systemValue (0.01-1.0 → 1-255)")
            
            // Set to manual mode first to ensure system respects the value
            val modeResult = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Log.d(TAG, "applyBrightness: Set brightness mode to MANUAL, result=$modeResult")
            
            // Set brightness value
            val result = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                systemValue
            )
            Log.d(TAG, "applyBrightness: Set system brightness value to $systemValue, result=$result")
            
            if (!result) {
                Log.w(TAG, "Settings.System.putInt returned false - check permissions")
            }
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error applying system brightness", e)
            return false
        }
    }
}
