package com.redscreenfilter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.redscreenfilter.MainActivity
import com.redscreenfilter.R

/**
 * Red Screen Overlay Service
 * Foreground service that manages the overlay window using WindowManager.
 * Displays a full-screen red overlay with configurable opacity.
 */
class RedOverlayService : Service() {
    
    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "overlay_service_channel"
        const val ACTION_UPDATE_OPACITY = "com.redscreenfilter.UPDATE_OPACITY"
        const val EXTRA_OPACITY = "opacity"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel for Android O+
        createNotificationChannel()
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Initialize WindowManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Create and show overlay
        createOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_OPACITY -> {
                val opacity = intent.getFloatExtra(EXTRA_OPACITY, 0.5f)
                updateOpacity(opacity)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
    
    /**
     * Create and display the overlay window
     */
    private fun createOverlay() {
        if (overlayView != null) return // Already created
        
        overlayView = OverlayView(this).apply {
            setOpacity(0.5f) // Default 50% opacity
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle cases where permission not granted
        }
    }
    
    /**
     * Remove overlay window
     */
    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }
    
    /**
     * Update overlay opacity
     */
    private fun updateOpacity(opacity: Float) {
        overlayView?.setOpacity(opacity)
    }
    
    /**
     * Create notification channel for foreground service (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Red Screen Filter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls red screen overlay"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create persistent notification for foreground service
     */
    private fun createNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Red Screen Filter Active")
        .setContentText("Tap to open settings")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}
