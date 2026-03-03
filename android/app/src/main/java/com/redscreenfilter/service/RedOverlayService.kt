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
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.redscreenfilter.MainActivity
import com.redscreenfilter.R
import com.redscreenfilter.data.PreferencesManager

/**
 * Red Screen Overlay Service
 * Foreground service that manages the overlay window using WindowManager.
 * Displays a full-screen red overlay with configurable opacity.
 */
class RedOverlayService : Service() {
    
    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null
    private lateinit var preferencesManager: PreferencesManager
    
    private val TAG = "RedOverlayService"
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "overlay_service_channel"
        const val ACTION_UPDATE_OPACITY = "com.redscreenfilter.UPDATE_OPACITY"
        const val EXTRA_OPACITY = "opacity"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "onCreate: Service starting")
        
        try {
            // Initialize PreferencesManager
            Log.d(TAG, "onCreate: Initializing PreferencesManager")
            preferencesManager = PreferencesManager.getInstance(this)
            Log.d(TAG, "onCreate: PreferencesManager initialized")
            
            // Create notification channel for Android O+
            Log.d(TAG, "onCreate: Creating notification channel")
            createNotificationChannel()
            Log.d(TAG, "onCreate: Notification channel created")
            
            // Start as foreground service
            Log.d(TAG, "onCreate: Starting foreground service")
            startForeground(NOTIFICATION_ID, createNotification())
            Log.d(TAG, "onCreate: Foreground service started")
            
            // Initialize WindowManager
            Log.d(TAG, "onCreate: Initializing WindowManager")
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            Log.d(TAG, "onCreate: WindowManager initialized")
            
            // Create and show overlay
            Log.d(TAG, "onCreate: Creating overlay")
            createOverlay()
            Log.d(TAG, "onCreate: Overlay created")
            
            Log.d(TAG, "onCreate: Service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: FATAL ERROR during service creation", e)
            e.printStackTrace()
            // Stop the service if we can't initialize
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        
        when (intent?.action) {
            ACTION_UPDATE_OPACITY -> {
                val opacity = intent.getFloatExtra(EXTRA_OPACITY, 0.5f)
                Log.d(TAG, "onStartCommand: Updating opacity to $opacity")
                updateOpacity(opacity)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service stopping")
        removeOverlay()
    }
    
    /**
     * Create and display the overlay window
     */
    private fun createOverlay() {
        if (overlayView != null) {
            Log.d(TAG, "createOverlay: Overlay already exists")
            return // Already created
        }
        
        Log.d(TAG, "createOverlay: Creating overlay view")
        
        // Load saved opacity from preferences
        val savedOpacity = preferencesManager.getOpacity()
        Log.d(TAG, "createOverlay: Loaded opacity: $savedOpacity")
        
        overlayView = OverlayView(this).apply {
            setOpacity(savedOpacity)
        }
        
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }
        
        Log.d(TAG, "createOverlay: Using layout type: $layoutType")
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
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
            Log.d(TAG, "createOverlay: Overlay added to WindowManager successfully")
        } catch (e: Exception) {
            Log.e(TAG, "createOverlay: Failed to add overlay", e)
            // Stop the service if overlay can't be created
            stopSelf()
        }
    }
    
    /**
     * Remove overlay window
     */
    private fun removeOverlay() {
        Log.d(TAG, "removeOverlay: Removing overlay")
        overlayView?.let {
            try {
                windowManager?.removeView(it)
                Log.d(TAG, "removeOverlay: Overlay removed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "removeOverlay: Failed to remove overlay", e)
            }
        }
        overlayView = null
    }
    
    /**
     * Update overlay opacity
     */
    private fun updateOpacity(opacity: Float) {
        Log.d(TAG, "updateOpacity: Setting opacity to $opacity")
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
