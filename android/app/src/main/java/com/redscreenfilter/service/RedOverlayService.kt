package com.redscreenfilter.service

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import com.redscreenfilter.core.model.ColorVariant
import com.redscreenfilter.data.BatteryMonitor
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
    private val appExemptionCheckInterval = 1500L
    private val keyguardManager by lazy { getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    private val homeLauncherPackage by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    }

    // Receiver to handle screen state changes for "Hide on Lock Screen" feature
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "screenStateReceiver: Action=${intent.action}, KeyguardLocked=${keyguardManager.isKeyguardLocked}")
                    updateOverlayForCurrentApp()
                }
            }
        }
    }

    private val appExemptionRunnable = object : Runnable {
        override fun run() {
            try {
                if (hasAnyOverlayEnabled()) {
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
        const val ACTION_UPDATE_EXTRA_DIM = "com.redscreenfilter.UPDATE_EXTRA_DIM"
        const val EXTRA_EXTRA_DIM_ENABLED = "extra_dim_enabled"
        const val EXTRA_EXTRA_DIM_INTENSITY = "extra_dim_intensity"
        const val ACTION_INCREASE_OPACITY = "com.redscreenfilter.INCREASE_OPACITY"
        const val ACTION_DECREASE_OPACITY = "com.redscreenfilter.DECREASE_OPACITY"
        const val ACTION_OVERLAY_OPACITY_CHANGED = "com.redscreenfilter.OVERLAY_OPACITY_CHANGED"
        private const val OPACITY_STEP = 0.15f
    }
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "onCreate: Service starting")
        
        try {
            preferencesManager = PreferencesManager.getInstance(this)
            
            batteryManager = BatteryMonitor.getInstance(this).apply {
                addListener(this@RedOverlayService)
                startMonitoring()
            }
            
            lightSensorManager = LightSensorManager.getInstance(this).apply {
                addListener(this@RedOverlayService)
                if (preferencesManager.isLightSensorEnabled()) {
                    startMonitoring()
                }
            }
            
            createNotificationChannel()
            exemptedAppsManager = ExemptedAppsManager.getInstance(this)
            startForeground(NOTIFICATION_ID, createNotification())
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // Register screen state receiver
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenStateReceiver, filter)
            
            createOverlay()
            startAppExemptionCheck()
            
            Log.d(TAG, "onCreate: Service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: FATAL ERROR during service creation", e)
            try {
                stopForeground(true)
            } catch (ex: Exception) {
                Log.e(TAG, "onCreate: Error stopping foreground", ex)
            }
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        
        when (intent?.action) {
            ACTION_UPDATE_OPACITY -> {
                val opacity = intent.getFloatExtra(EXTRA_OPACITY, 0.5f)
                updateOpacity(opacity)
            }
            ACTION_UPDATE_COLOR -> {
                val colorVariantString = intent.getStringExtra(EXTRA_COLOR_VARIANT)
                val variant = ColorVariant.fromString(colorVariantString)
                updateColorVariant(variant)
            }
            ACTION_UPDATE_EXTRA_DIM -> {
                val isEnabled = intent.getBooleanExtra(
                    EXTRA_EXTRA_DIM_ENABLED,
                    preferencesManager.isExtraDimEnabled()
                )
                val intensity = intent.getFloatExtra(
                    EXTRA_EXTRA_DIM_INTENSITY,
                    preferencesManager.getExtraDimIntensity()
                )
                updateExtraDimState(isEnabled, intensity)
            }
            ACTION_TOGGLE_OVERLAY -> {
                val isEnabled = preferencesManager.isOverlayEnabled()
                preferencesManager.setOverlayEnabled(!isEnabled)
                if (!isEnabled) {
                    if (overlayView == null) createOverlay()
                    applyCurrentOverlayState()
                } else {
                    if (!hasAnyOverlayEnabled()) {
                        removeOverlay()
                    } else {
                        applyCurrentOverlayState()
                    }
                }
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_INCREASE_OPACITY -> {
                val currentOpacity = preferencesManager.getOpacity()
                val newOpacity = (currentOpacity + OPACITY_STEP).coerceIn(0f, 1f)
                updateOpacity(newOpacity)
                updateNotification()
            }
            ACTION_DECREASE_OPACITY -> {
                val currentOpacity = preferencesManager.getOpacity()
                val newOpacity = (currentOpacity - OPACITY_STEP).coerceIn(0f, 1f)
                updateOpacity(newOpacity)
                updateNotification()
            }
            else -> {
                applyCurrentOverlayState()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service stopping")

        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error unregistering screenStateReceiver", e)
        }

        appExemptionHandler.removeCallbacks(appExemptionRunnable)
        
        batteryManager?.let {
            it.removeListener(this)
            it.stopMonitoring()
        }
        
        lightSensorManager?.let {
            it.removeListener(this)
            it.stopMonitoring()
        }
        
        removeOverlay()
        windowManager = null
        exemptedAppsManager = null
        batteryManager = null
        lightSensorManager = null
    }
    
    private fun createOverlay() {
        if (overlayView != null) return
        
        val savedOpacity = preferencesManager.getOpacity()
        val colorVariantString = preferencesManager.getColorVariant()
        val colorVariant = ColorVariant.fromString(colorVariantString)
        val extraDimEnabled = preferencesManager.isExtraDimEnabled()
        val extraDimIntensity = preferencesManager.getExtraDimIntensity()
        
        overlayView = OverlayView(this).apply {
            setOpacity(if (preferencesManager.isOverlayEnabled()) savedOpacity else 0f, animate = false)
            setColorVariant(colorVariant)
            setDimOpacity(if (extraDimEnabled) extraDimIntensity else 0f, animate = false)
        }
        
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }
        
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
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fitInsetsTypes = 0
            }
        }
        
        try {
            windowManager?.addView(overlayView, params)
            overlayView?.postInvalidate()
            applyCurrentOverlayState()
        } catch (e: Exception) {
            Log.e(TAG, "createOverlay: Failed to add overlay", e)
            stopSelf()
        }
    }
    
    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "removeOverlay: Failed to remove overlay", e)
            }
        }
        overlayView = null
    }
    
    private fun updateOverlayForCurrentApp() {
        try {
            if (!hasAnyOverlayEnabled()) {
                if (overlayView != null) removeOverlay()
                overlayHiddenDueToExemption = false
                return
            }

            val foregroundApp = exemptedAppsManager?.getForegroundAppPackage()
            val isExempted = exemptedAppsManager?.isAppExempt(foregroundApp ?: "") ?: false
            val hiddenForContext = shouldHideForContext(foregroundApp)
            
            Log.d(TAG, "updateOverlayForCurrentApp: App=$foregroundApp, Exempt=$isExempted, ContextHidden=$hiddenForContext")

            if ((isExempted || hiddenForContext) && overlayView != null) {
                removeOverlay()
                overlayHiddenDueToExemption = true
            } else if (!isExempted && !hiddenForContext && (overlayHiddenDueToExemption || overlayView == null)) {
                createOverlay()
                overlayHiddenDueToExemption = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateOverlayForCurrentApp: Error", e)
        }
    }
    
    private fun startAppExemptionCheck() {
        appExemptionHandler.removeCallbacks(appExemptionRunnable)
        appExemptionHandler.postDelayed(appExemptionRunnable, 100)
    }

    private fun hasAnyOverlayEnabled(): Boolean {
        return preferencesManager.isOverlayEnabled() || preferencesManager.isExtraDimEnabled()
    }

    private fun isOnLockScreen(): Boolean {
        return keyguardManager.isKeyguardLocked
    }

    private fun isOnHomeScreen(foregroundApp: String?): Boolean {
        val launcherPackage = homeLauncherPackage ?: return false
        return !foregroundApp.isNullOrBlank() && foregroundApp == launcherPackage
    }

    private fun shouldHideForContext(foregroundApp: String?): Boolean {
        val hideOnLockScreen = preferencesManager.shouldHideOverlayOnLockScreen()
        val hideOnHomeScreen = preferencesManager.shouldHideOverlayOnHomeScreen()

        if (hideOnLockScreen && isOnLockScreen()) {
            Log.d(TAG, "shouldHideForContext: Hiding because lock screen is active")
            return true
        }
        if (hideOnHomeScreen && isOnHomeScreen(foregroundApp)) {
            Log.d(TAG, "shouldHideForContext: Hiding because home screen is active")
            return true
        }

        return false
    }

    private fun applyCurrentOverlayState() {
        if (!hasAnyOverlayEnabled()) {
            removeOverlay()
            return
        }

        val foregroundApp = exemptedAppsManager?.getForegroundAppPackage()
        val isExempted = exemptedAppsManager?.isAppExempt(foregroundApp ?: "") ?: false
        val hiddenForContext = shouldHideForContext(foregroundApp)
        
        if (isExempted || hiddenForContext) {
            removeOverlay()
            overlayHiddenDueToExemption = true
            return
        }

        if (overlayView == null) {
            createOverlay()
            return
        }

        val redOpacity = if (preferencesManager.isOverlayEnabled()) preferencesManager.getOpacity() else 0f
        val colorVariantString = preferencesManager.getColorVariant()
        val colorVariant = ColorVariant.fromString(colorVariantString)
        val dimOpacity = if (preferencesManager.isExtraDimEnabled()) preferencesManager.getExtraDimIntensity() else 0f

        overlayView?.setColorVariant(colorVariant)
        overlayView?.setOpacity(redOpacity)
        overlayView?.setDimOpacity(dimOpacity)
        overlayHiddenDueToExemption = false
    }
    
    private fun updateOpacity(opacity: Float) {
        preferencesManager.setOpacity(opacity)
        applyCurrentOverlayState()
    }
    
    private fun updateColorVariant(variant: ColorVariant) {
        preferencesManager.setColorVariant(variant.name.lowercase())
        applyCurrentOverlayState()
    }

    private fun updateExtraDimState(isEnabled: Boolean, intensity: Float) {
        preferencesManager.setExtraDimEnabled(isEnabled)
        preferencesManager.setExtraDimIntensity(intensity)
        applyCurrentOverlayState()
    }
    
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
    
    private fun createNotification(statusText: String? = null) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Red Screen Filter Active")
        .setContentText(statusText ?: getCurrentStatusText())
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
            android.R.drawable.ic_menu_close_clear_cancel,
            "−",
            PendingIntent.getService(
                this,
                3,
                Intent(this, RedOverlayService::class.java).apply { action = ACTION_DECREASE_OPACITY },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            android.R.drawable.ic_input_add,
            "+",
            PendingIntent.getService(
                this,
                4,
                Intent(this, RedOverlayService::class.java).apply { action = ACTION_INCREASE_OPACITY },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
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
        .build()
    
    private fun getCurrentStatusText(): String {
        val opacity = preferencesManager.getOpacity()
        val percentage = (opacity * 100).toInt()
        return "Opacity: $percentage% • Tap to adjust"
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onBatteryStateChanged(level: Int, isLow: Boolean, isCritical: Boolean, isCharging: Boolean) {
        if (!preferencesManager.getBatteryOptimizationEnabled()) return
        
        val isBatteryReduced = preferencesManager.isBatteryReduced()
        
        if ((isLow || isCritical) && !isCharging) {
            if (!isBatteryReduced) reduceBatteryOpacity()
        } else {
            if (isBatteryReduced) restoreBatteryOpacity()
        }
        
        updateNotificationWithBatteryStatus(level, isLow, isCritical)
    }
    
    private fun reduceBatteryOpacity() {
        val currentOpacity = preferencesManager.getOpacity()
        preferencesManager.setOriginalOpacityPreBattery(currentOpacity)
        val reducedOpacity = (currentOpacity * 0.7f).coerceIn(0f, 1f)
        preferencesManager.setOpacity(reducedOpacity)
        preferencesManager.setIsBatteryReduced(true)
        overlayView?.setOpacity(reducedOpacity)
        broadcastOpacityChange()
    }
    
    private fun restoreBatteryOpacity() {
        val originalOpacity = preferencesManager.getOriginalOpacityPreBattery()
        preferencesManager.setOpacity(originalOpacity)
        preferencesManager.setIsBatteryReduced(false)
        overlayView?.setOpacity(originalOpacity)
        broadcastOpacityChange()
    }
    
    private fun updateNotificationWithBatteryStatus(level: Int, isLow: Boolean, isCritical: Boolean) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val statusText = when {
            isCritical -> "🔴 Critical Battery ($level%)"
            isLow -> "🟡 Low Battery ($level%)"
            else -> "Battery: $level%"
        }
        val notification = createNotification(statusText)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onLightLevelChanged(lux: Float, opacity: Float) {
        if (preferencesManager.isLightSensorLocked() || preferencesManager.isBatteryReduced()) return
        preferencesManager.setOpacity(opacity)
        overlayView?.setOpacity(opacity)
        broadcastOpacityChange()
    }
    
    private fun broadcastOpacityChange() {
        try {
            val intent = Intent(ACTION_OVERLAY_OPACITY_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sendBroadcast(intent, null)
            } else {
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "broadcastOpacityChange: Error", e)
        }
    }
}
