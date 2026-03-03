package com.redscreenfilter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.redscreenfilter.BuildConfig
import com.redscreenfilter.MainActivity
import com.redscreenfilter.R
import com.redscreenfilter.data.BatteryMonitor
import com.redscreenfilter.data.ColorVariant
import com.redscreenfilter.data.ExemptedAppsManager
import com.redscreenfilter.data.LightSensorManager
import com.redscreenfilter.data.PreferencesManager

/**
 * Red Screen Overlay Service
 * Foreground service that manages the overlay window using WindowManager.
 * Displays a full-screen red overlay with configurable opacity.
 * Includes battery awareness and light sensing for automatic adjustments.
 */
class RedOverlayService : Service(), BatteryMonitor.BatteryStateListener, LightSensorManager.LightSensorListener {
    
    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null
    private lateinit var preferencesManager: PreferencesManager
    private var batteryManager: BatteryMonitor? = null
    private var lightSensorManager: LightSensorManager? = null
    private var exemptedAppsManager: ExemptedAppsManager? = null
    private var overlayHiddenDueToExemption = false
    private var lastForegroundApp: String? = null
    private val appExemptionHandler = Handler(Looper.getMainLooper())
    private val appExemptionCheckInterval = 3000L
    private val appExemptionRunnable = object : Runnable {
        override fun run() {
            try {
                if (preferencesManager.isOverlayEnabled()) {
                    updateOverlayForCurrentApp()
                }
            } catch (e: Exception) {
                Log.e(TAG, "startAppExemptionCheck: Error in periodic check", e)
            }
            appExemptionHandler.postDelayed(this, appExemptionCheckInterval)
        }
    }
    
    private val TAG = "RedOverlayService"
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "overlay_service_channel"
        const val ACTION_UPDATE_OPACITY = "com.redscreenfilter.UPDATE_OPACITY"
        const val EXTRA_OPACITY = "opacity"
        const val ACTION_UPDATE_COLOR = "com.redscreenfilter.UPDATE_COLOR"
        const val EXTRA_COLOR_VARIANT = "color_variant"
        const val ACTION_TOGGLE_OVERLAY = "com.redscreenfilter.TOGGLE_OVERLAY"
        const val ACTION_STOP_SERVICE = "com.redscreenfilter.STOP_SERVICE"
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
            
            // Initialize BatteryMonitor
            Log.d(TAG, "onCreate: Initializing BatteryMonitor")
            batteryManager = BatteryMonitor.getInstance(this).apply {
                addListener(this@RedOverlayService)
                startMonitoring()
            }
            Log.d(TAG, "onCreate: BatteryMonitor initialized and monitoring started")
            
            // Initialize LightSensorManager
            Log.d(TAG, "onCreate: Initializing LightSensorManager")
            lightSensorManager = LightSensorManager.getInstance(this).apply {
                addListener(this@RedOverlayService)
                if (preferencesManager.isLightSensorEnabled()) {
                    startMonitoring()
                    Log.d(TAG, "onCreate: Light sensor monitoring started")
                } else {
                    Log.d(TAG, "onCreate: Light sensor disabled in preferences")
                }
            }
            Log.d(TAG, "onCreate: LightSensorManager initialized")
            
            // Create notification channel for Android O+
            Log.d(TAG, "onCreate: Creating notification channel")
            createNotificationChannel()
            Log.d(TAG, "onCreate: Notification channel created")
            
            // Initialize ExemptedAppsManager
            Log.d(TAG, "onCreate: Initializing ExemptedAppsManager")
            exemptedAppsManager = ExemptedAppsManager.getInstance(this)
            Log.d(TAG, "onCreate: ExemptedAppsManager initialized")
            
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
            
            // Start checking for exempted apps
            Log.d(TAG, "onCreate: Starting app exemption check")
            startAppExemptionCheck()
            Log.d(TAG, "onCreate: App exemption check started")
            
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
            ACTION_UPDATE_COLOR -> {
                val colorVariantString = intent.getStringExtra(EXTRA_COLOR_VARIANT)
                val variant = ColorVariant.fromString(colorVariantString)
                Log.d(TAG, "onStartCommand: Updating color to $variant")
                updateColorVariant(variant)
            }
            ACTION_TOGGLE_OVERLAY -> {
                val isEnabled = preferencesManager.isOverlayEnabled()
                Log.d(TAG, "onStartCommand: Toggling overlay from $isEnabled to ${!isEnabled}")
                preferencesManager.setOverlayEnabled(!isEnabled)
                if (!isEnabled) {
                    if (overlayView == null) createOverlay()
                    updateOpacity(preferencesManager.getOpacity())
                } else {
                    removeOverlay()
                }
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "onStartCommand: Stop service action triggered")
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service stopping")

        appExemptionHandler.removeCallbacks(appExemptionRunnable)
        
        // Stop battery monitoring
        batteryManager?.let {
            it.removeListener(this)
            it.stopMonitoring()
            Log.d(TAG, "onDestroy: Battery monitoring stopped")
        }
        
        // Stop light sensor monitoring
        lightSensorManager?.let {
            it.removeListener(this)
            it.stopMonitoring()
            Log.d(TAG, "onDestroy: Light sensor monitoring stopped")
        }
        
        removeOverlay()
        windowManager = null
        exemptedAppsManager = null
        batteryManager = null
        lightSensorManager = null
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
        
        // Load saved opacity and color variant from preferences
        val savedOpacity = preferencesManager.getOpacity()
        val colorVariantString = preferencesManager.getColorVariant()
        val colorVariant = ColorVariant.fromString(colorVariantString)
        Log.d(TAG, "createOverlay: Loaded opacity: $savedOpacity, color variant: $colorVariant")
        
        overlayView = OverlayView(this).apply {
            setOpacity(savedOpacity)
            setColorVariant(colorVariant)
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
     * Check if current foreground app is exempted and update overlay visibility
     */
    private fun updateOverlayForCurrentApp() {
        try {
            val foregroundApp = exemptedAppsManager?.getForegroundAppPackage()
            
            // Only update if app changed
            if (foregroundApp == lastForegroundApp) {
                return
            }
            lastForegroundApp = foregroundApp
            
            val isExempted = exemptedAppsManager?.isAppExempt(foregroundApp ?: "") ?: false
            
            if (isExempted && !overlayHiddenDueToExemption && overlayView != null) {
                // Hide overlay for exempted app
                Log.d(TAG, "updateOverlayForCurrentApp: Hiding overlay for exempted app: $foregroundApp")
                removeOverlay()
                overlayHiddenDueToExemption = true
            } else if (!isExempted && overlayHiddenDueToExemption) {
                // Show overlay again when leaving exempted app
                Log.d(TAG, "updateOverlayForCurrentApp: Showing overlay after leaving exempted app: $foregroundApp")
                createOverlay()
                overlayHiddenDueToExemption = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateOverlayForCurrentApp: Error checking app exemption", e)
        }
    }
    
    /**
     * Start periodic check for foreground app changes
     */
    private fun startAppExemptionCheck() {
        appExemptionHandler.removeCallbacks(appExemptionRunnable)
        appExemptionHandler.postDelayed(appExemptionRunnable, appExemptionCheckInterval)
    }
    
    /**
     * Update overlay opacity
     */
    private fun updateOpacity(opacity: Float) {
        Log.d(TAG, "updateOpacity: Setting opacity to $opacity")
        overlayView?.setOpacity(opacity)
    }
    
    /**
     * Update overlay color variant
     */
    private fun updateColorVariant(variant: ColorVariant) {
        Log.d(TAG, "updateColorVariant: Setting color to $variant")
        overlayView?.setColorVariant(variant)
    }
    
    /**
     * Create notification channel for foreground service (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for overlay service
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Red Screen Filter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls red screen overlay"
                setShowBadge(false)
            }
            
            // Channel for health reminders
            val reminderChannel = NotificationChannel(
                com.redscreenfilter.worker.EyeStrainReminder.CHANNEL_ID_REMINDERS,
                com.redscreenfilter.worker.EyeStrainReminder.CHANNEL_NAME_REMINDERS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Health reminders for eye strain prevention"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(reminderChannel)
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
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .addAction(
            R.drawable.ic_filter_inactive,
            "Stop",
            PendingIntent.getService(
                this,
                1,
                Intent(this, RedOverlayService::class.java).apply { action = ACTION_STOP_SERVICE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            R.drawable.ic_filter_active,
            "Settings",
            PendingIntent.getActivity(
                this,
                2,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
    
    /**
     * Handle battery state changes
     */
    override fun onBatteryStateChanged(level: Int, isLow: Boolean, isCritical: Boolean, isCharging: Boolean) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onBatteryStateChanged: level=$level%, isLow=$isLow, isCritical=$isCritical, isCharging=$isCharging")
        }
        
        // Check if battery optimization is enabled
        if (!preferencesManager.getBatteryOptimizationEnabled()) {
            Log.d(TAG, "onBatteryStateChanged: Battery optimization disabled, ignoring")
            return
        }
        
        val isBatteryReduced = preferencesManager.isBatteryReduced()
        
        if ((isLow || isCritical) && !isCharging && !isBatteryReduced) {
            // Battery is low and not charging, reduce opacity
            reduceBatteryOpacity()
        } else if (!isLow && !isCritical && isCharging && isBatteryReduced) {
            // Battery recovered or charging started, restore opacity
            restoreBatteryOpacity()
        } else if (!isLow && !isCritical && !isCharging && isBatteryReduced) {
            // Battery recovered above threshold, restore opacity
            restoreBatteryOpacity()
        }
        
        // Update notification with battery status
        updateNotificationWithBatteryStatus(level, isLow, isCritical)
    }
    
    /**
     * Reduce opacity due to low battery
     */
    private fun reduceBatteryOpacity() {
        Log.d(TAG, "reduceBatteryOpacity: Reducing opacity by 30%")
        
        val currentOpacity = preferencesManager.getOpacity()
        // Store original opacity for restoration
        preferencesManager.setOriginalOpacityPreBattery(currentOpacity)
        
        // Reduce opacity by 30% (multiply by 0.7)
        val reducedOpacity = (currentOpacity * 0.7f).coerceIn(0f, 1f)
        preferencesManager.setOpacity(reducedOpacity)
        preferencesManager.setIsBatteryReduced(true)
        
        // Apply reduced opacity to overlay
        overlayView?.setOpacity(reducedOpacity)
        Log.d(TAG, "reduceBatteryOpacity: Opacity reduced from $currentOpacity to $reducedOpacity")
    }
    
    /**
     * Restore opacity after low battery resolves
     */
    private fun restoreBatteryOpacity() {
        Log.d(TAG, "restoreBatteryOpacity: Restoring original opacity")
        
        val originalOpacity = preferencesManager.getOriginalOpacityPreBattery()
        preferencesManager.setOpacity(originalOpacity)
        preferencesManager.setIsBatteryReduced(false)
        
        // Apply restored opacity to overlay
        overlayView?.setOpacity(originalOpacity)
        Log.d(TAG, "restoreBatteryOpacity: Opacity restored to $originalOpacity")
    }
    
    /**
     * Update notification with battery status indicator
     */
    private fun updateNotificationWithBatteryStatus(level: Int, isLow: Boolean, isCritical: Boolean) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        val statusText = when {
            isCritical -> "🔴 Critical Battery ($level%)"
            isLow -> "🟡 Low Battery ($level%)"
            else -> "Battery: $level%"
        }
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Red Screen Filter Active")
            .setContentText(statusText)
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
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Handle light level changes from light sensor
     */
    override fun onLightLevelChanged(lux: Float, opacity: Float) {
        // Check if light sensor is locked (manual override)
        if (preferencesManager.isLightSensorLocked()) {
            return
        }
        
        // Don't override if battery reduction is active
        if (preferencesManager.isBatteryReduced()) {
            return
        }
        
        // Update opacity
        preferencesManager.setOpacity(opacity)
        overlayView?.setOpacity(opacity)
    }
}
