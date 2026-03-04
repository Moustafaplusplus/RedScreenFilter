package com.redscreenfilter.feature.settings.coordinator

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.redscreenfilter.core.model.ColorVariant
import com.redscreenfilter.service.RedOverlayService

class OverlayControlCoordinator(
    private val context: Context
) {
    private val tag = "OverlayControlCoordinator"

    fun startOverlayService() {
        val intent = Intent(context, RedOverlayService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (error: Exception) {
            Log.e(tag, "startOverlayService: Error starting service", error)
        }
    }

    fun stopOverlayService() {
        val intent = Intent(context, RedOverlayService::class.java)
        try {
            context.stopService(intent)
        } catch (error: Exception) {
            Log.e(tag, "stopOverlayService: Error stopping service", error)
        }
    }

    fun updateOverlayOpacity(opacity: Float) {
        val intent = Intent(context, RedOverlayService::class.java).apply {
            action = RedOverlayService.ACTION_UPDATE_OPACITY
            putExtra(RedOverlayService.EXTRA_OPACITY, opacity)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (error: Exception) {
            Log.e(tag, "updateOverlayOpacity: Error", error)
        }
    }

    fun updateOverlayColor(variant: ColorVariant) {
        val intent = Intent(context, RedOverlayService::class.java).apply {
            action = RedOverlayService.ACTION_UPDATE_COLOR
            putExtra(RedOverlayService.EXTRA_COLOR_VARIANT, variant.name)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (error: Exception) {
            Log.e(tag, "updateOverlayColor: Error", error)
        }
    }

    fun updateExtraDim(enabled: Boolean, intensity: Float) {
        val intent = Intent(context, RedOverlayService::class.java).apply {
            action = RedOverlayService.ACTION_UPDATE_EXTRA_DIM
            putExtra(RedOverlayService.EXTRA_EXTRA_DIM_ENABLED, enabled)
            putExtra(RedOverlayService.EXTRA_EXTRA_DIM_INTENSITY, intensity)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (error: Exception) {
            Log.e(tag, "updateExtraDim: Error", error)
        }
    }

    fun notifyLightSensorChanged() {
        val intent = Intent(context, RedOverlayService::class.java).apply {
            action = "com.redscreenfilter.LIGHT_SENSOR_CHANGED"
        }
        try {
            context.startService(intent)
        } catch (error: Exception) {
            Log.e(tag, "notifyLightSensorChanged: Error", error)
        }
    }
}
