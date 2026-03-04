package com.redscreenfilter

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import java.util.concurrent.TimeUnit
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.redscreenfilter.data.ColorVariant
import com.redscreenfilter.data.LightSensorManager
import com.redscreenfilter.data.LocationManager
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.data.repository.AnalyticsRepository
import com.redscreenfilter.service.RedOverlayService
import com.redscreenfilter.ui.AnalyticsFragment
import com.redscreenfilter.utils.WorkScheduler
import kotlinx.coroutines.launch

/**
 * MainActivity - Main UI for Red Screen Filter
 * 
 * Features:
 * - Toggle overlay on/off
 * - Adjust opacity with SeekBar
 * - Request SYSTEM_ALERT_WINDOW permission
 * - Start/stop RedOverlayService
 * - Schedule overlay based on time
 * - Background scheduling with WorkManager
 * - Location-based sunrise/sunset scheduling
 */
class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var schedulingManager: SchedulingManager
    private lateinit var locationManager: LocationManager
    private lateinit var analyticsRepository: AnalyticsRepository
    
    // UI Components
    private lateinit var switchOverlay: SwitchMaterial
    private lateinit var seekbarOpacity: SeekBar
    private lateinit var textOpacityValue: TextView
    private lateinit var cardPermission: MaterialCardView
    private lateinit var buttonRequestPermission: MaterialButton
    
    // Scheduling UI Components
    private lateinit var switchScheduling: SwitchMaterial
    private lateinit var layoutTimePickers: LinearLayout
    private lateinit var buttonStartTime: MaterialButton
    private lateinit var buttonEndTime: MaterialButton
    
    // Location Scheduling UI Components
    private lateinit var switchLocationScheduling: SwitchMaterial
    private lateinit var layoutLocationSettings: LinearLayout
    private lateinit var buttonGetLocation: MaterialButton
    private lateinit var textSunsetTime: TextView
    private lateinit var textSunriseTime: TextView
    private lateinit var sliderLocationOffset: Slider
    private lateinit var textLocationOffsetValue: TextView
    
    // Color Variant UI Components
    private lateinit var radioGroupColorVariant: RadioGroup
    private lateinit var radioRedStandard: RadioButton
    private lateinit var radioRedOrange: RadioButton
    private lateinit var radioRedPink: RadioButton
    private lateinit var radioHighContrast: RadioButton
    private lateinit var colorPreviewBox: View
    
    // Battery Optimization UI Components
    private lateinit var switchBatteryOptimization: SwitchMaterial
    
    // Light Sensor UI Components
    private lateinit var switchLightSensor: SwitchMaterial
    private lateinit var layoutLightSensorSettings: LinearLayout
    private lateinit var sliderLightSensitivity: Slider
    private lateinit var textLightSensitivityValue: TextView
    private lateinit var switchLightSensorLocked: SwitchMaterial
    private lateinit var textCurrentLux: TextView
    
    // Eye Strain Reminder UI Components
    private lateinit var switchEyeStrainReminder: SwitchMaterial
    private lateinit var layoutEyeStrainSettings: LinearLayout
    private lateinit var radioGroupNotificationStyle: RadioGroup
    private lateinit var radioNotificationSound: RadioButton
    private lateinit var radioNotificationVibration: RadioButton
    private lateinit var radioNotificationSilent: RadioButton
    
    // Navigation Components
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var scrollSettings: NestedScrollView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var settingsTabs: MaterialButtonToggleGroup
    private lateinit var sectionDisplay: View
    private lateinit var sectionAutomation: View
    private lateinit var sectionWellness: View

    private val sectionInterpolator = FastOutSlowInInterpolator()
    
    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            Log.d(TAG, "Location permission granted")
            requestLocationAndUpdate()
        } else {
            Log.w(TAG, "Location permission denied")
            Snackbar.make(
                findViewById(android.R.id.content),
                R.string.location_permission_required,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private val postNotificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.w(TAG, "POST_NOTIFICATIONS permission denied")
        }
    }
    
    // Handler for updating lux display
    private val luxUpdateHandler = Handler(Looper.getMainLooper())
    private val luxUpdateRunnable = object : Runnable {
        override fun run() {
            if (preferencesManager.isLightSensorEnabled()) {
                val lightSensorManager = LightSensorManager.getInstance(this@MainActivity)
                val currentLux = lightSensorManager.getCurrentLux()
                textCurrentLux.text = getString(R.string.current_lux_label).replace("--", currentLux.toInt().toString())
                luxUpdateHandler.postDelayed(this, 500) // Update every 500ms
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "onCreate: Activity starting")
        
        // Initialize PreferencesManager
        preferencesManager = PreferencesManager.getInstance(this)
        
        // Initialize SchedulingManager
        schedulingManager = SchedulingManager.getInstance(this)
        
        // Initialize LocationManager
        locationManager = LocationManager.getInstance(this)

        // Initialize AnalyticsRepository
        analyticsRepository = AnalyticsRepository.getInstance(this)
        
        // Initialize UI components
        initializeViews()
        
        // Load saved settings
        loadSettings()
        
        // Setup listeners
        setupListeners()

        // Setup segmented sections
        setupSectionTabs()
        
        // Setup bottom navigation
        setupBottomNavigation()

        requestRuntimePermissions()
        
        // Schedule reminder worker if enabled
        if (preferencesManager.isEyeStrainReminderEnabled()) {
            scheduleEyeStrainReminder()
        }

        animateSettingsEntrance()
        
        Log.d(TAG, "onCreate: Activity initialized")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Checking permission status")
        // Check permission status when returning to app
        updatePermissionUI()
        
        // Start lux update loop if light sensor is enabled
        if (preferencesManager.isLightSensorEnabled()) {
            luxUpdateHandler.post(luxUpdateRunnable)
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Cleaning up")
        // Stop lux update loop
        luxUpdateHandler.removeCallbacks(luxUpdateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        luxUpdateHandler.removeCallbacksAndMessages(null)
        if (::bottomNavigation.isInitialized) {
            bottomNavigation.setOnItemSelectedListener { false }
        }
    }
    
    private fun initializeViews() {
        switchOverlay = findViewById(R.id.switch_overlay)
        seekbarOpacity = findViewById(R.id.seekbar_opacity)
        textOpacityValue = findViewById(R.id.text_opacity_value)
        cardPermission = findViewById(R.id.card_permission)
        buttonRequestPermission = findViewById(R.id.button_request_permission)
        
        // Scheduling UI
        switchScheduling = findViewById(R.id.switch_scheduling)
        layoutTimePickers = findViewById(R.id.layout_time_pickers)
        buttonStartTime = findViewById(R.id.button_start_time)
        buttonEndTime = findViewById(R.id.button_end_time)
        
        // Location Scheduling UI
        switchLocationScheduling = findViewById(R.id.switch_location_scheduling)
        layoutLocationSettings = findViewById(R.id.layout_location_settings)
        buttonGetLocation = findViewById(R.id.button_get_location)
        textSunsetTime = findViewById(R.id.text_sunset_time)
        textSunriseTime = findViewById(R.id.text_sunrise_time)
        sliderLocationOffset = findViewById(R.id.slider_location_offset)
        textLocationOffsetValue = findViewById(R.id.text_location_offset_value)
        
        // Color Variant UI
        radioGroupColorVariant = findViewById(R.id.radio_group_color_variant)
        radioRedStandard = findViewById(R.id.radio_red_standard)
        radioRedOrange = findViewById(R.id.radio_red_orange)
        radioRedPink = findViewById(R.id.radio_red_pink)
        radioHighContrast = findViewById(R.id.radio_high_contrast)
        colorPreviewBox = findViewById(R.id.color_preview_box)
        
        // Battery Optimization UI
        switchBatteryOptimization = findViewById(R.id.switch_battery_optimization)
        
        // Light Sensor UI
        switchLightSensor = findViewById(R.id.switch_light_sensor)
        layoutLightSensorSettings = findViewById(R.id.layout_light_sensor_settings)
        sliderLightSensitivity = findViewById(R.id.slider_light_sensitivity)
        textLightSensitivityValue = findViewById(R.id.text_light_sensitivity_value)
        switchLightSensorLocked = findViewById(R.id.switch_light_sensor_locked)
        textCurrentLux = findViewById(R.id.text_current_lux)
        
        // Eye Strain Reminder UI
        switchEyeStrainReminder = findViewById(R.id.switch_eye_strain_reminder)
        layoutEyeStrainSettings = findViewById(R.id.layout_eye_strain_settings)
        radioGroupNotificationStyle = findViewById(R.id.radio_group_notification_style)
        radioNotificationSound = findViewById(R.id.radio_notification_sound)
        radioNotificationVibration = findViewById(R.id.radio_notification_vibration)
        radioNotificationSilent = findViewById(R.id.radio_notification_silent)
        
        // Navigation UI
        bottomNavigation = findViewById(R.id.bottom_navigation)
        scrollSettings = findViewById(R.id.scroll_settings)
        fragmentContainer = findViewById(R.id.fragment_container)
        settingsTabs = findViewById(R.id.group_settings_tabs)
        sectionDisplay = findViewById(R.id.section_display)
        sectionAutomation = findViewById(R.id.section_automation)
        sectionWellness = findViewById(R.id.section_wellness)
    }
    
    private fun loadSettings() {
        // Load overlay enabled state
        val isEnabled = preferencesManager.isOverlayEnabled()
        switchOverlay.isChecked = isEnabled
        
        // Load opacity (convert 0.0-1.0 to 0-100)
        val opacity = preferencesManager.getOpacity()
        val opacityPercentage = (opacity * 100).toInt()
        seekbarOpacity.progress = opacityPercentage
        updateOpacityText(opacityPercentage)
        
        // Load scheduling settings
        loadSchedulingSettings()
        
        // Load location scheduling settings
        loadLocationSchedulingSettings()
        
        // Load color variant settings
        loadColorVariantSettings()
        
        // Load battery optimization settings
        loadBatteryOptimizationSettings()
        
        // Load light sensor settings
        loadLightSensorSettings()
        
        // Load eye strain reminder settings
        loadEyeStrainReminderSettings()
    }
    
    private fun loadSchedulingSettings() {
        val isSchedulingEnabled = schedulingManager.isScheduleEnabled()
        switchScheduling.isChecked = isSchedulingEnabled
        
        // Show/hide time pickers based on scheduling state
        layoutTimePickers.visibility = if (isSchedulingEnabled) View.VISIBLE else View.GONE
        
        // Load and display times
        val (startHour, startMinute) = schedulingManager.getStartTimeComponents()
        val (endHour, endMinute) = schedulingManager.getEndTimeComponents()
        
        buttonStartTime.text = schedulingManager.formatTime(startHour, startMinute)
        buttonEndTime.text = schedulingManager.formatTime(endHour, endMinute)
        
        // Ensure WorkManager is scheduled if scheduling is enabled
        if (isSchedulingEnabled) {
            Log.d(TAG, "loadSchedulingSettings: Scheduling is enabled, ensuring WorkManager is scheduled")
            WorkScheduler.schedulePeriodicWork(this)
        }
    }
    
    private fun loadLocationSchedulingSettings() {
        val isLocationSchedulingEnabled = schedulingManager.isLocationScheduleEnabled()
        switchLocationScheduling.isChecked = isLocationSchedulingEnabled
        
        // Show/hide location settings based on state
        layoutLocationSettings.visibility = if (isLocationSchedulingEnabled) View.VISIBLE else View.GONE
        
        // Load and display offset
        val offset = schedulingManager.getLocationOffset()
        sliderLocationOffset.value = offset.toFloat()
        updateLocationOffsetText(offset)
        
        // Update calculated times if location is available
        updateCalculatedTimes()
    }
    
    private fun loadColorVariantSettings() {
        val colorVariantString = preferencesManager.getColorVariant()
        val colorVariant = ColorVariant.fromString(colorVariantString)
        
        // Select the appropriate radio button
        when (colorVariant) {
            ColorVariant.RED_STANDARD -> radioRedStandard.isChecked = true
            ColorVariant.RED_ORANGE -> radioRedOrange.isChecked = true
            ColorVariant.RED_PINK -> radioRedPink.isChecked = true
            ColorVariant.HIGH_CONTRAST -> radioHighContrast.isChecked = true
        }
        
        // Update preview color
        updateColorPreview(colorVariant)
    }
    
    private fun updateColorPreview(variant: ColorVariant) {
        colorPreviewBox.setBackgroundColor(variant.colorValue)
    }
    
    private fun loadBatteryOptimizationSettings() {
        val isBatteryOptimizationEnabled = preferencesManager.getBatteryOptimizationEnabled()
        switchBatteryOptimization.isChecked = isBatteryOptimizationEnabled
        Log.d(TAG, "loadBatteryOptimizationSettings: Battery optimization enabled = $isBatteryOptimizationEnabled")
    }
    
    private fun loadLightSensorSettings() {
        val isLightSensorEnabled = preferencesManager.isLightSensorEnabled()
        switchLightSensor.isChecked = isLightSensorEnabled
        
        // Show/hide light sensor settings based on state
        layoutLightSensorSettings.visibility = if (isLightSensorEnabled) View.VISIBLE else View.GONE
        
        // Load sensitivity level
        val sensitivity = preferencesManager.getLightSensorSensitivity()
        val sensitivityValue = when (sensitivity) {
            "low" -> 0f
            "medium" -> 1f
            "high" -> 2f
            else -> 1f // default to medium
        }
        sliderLightSensitivity.value = sensitivityValue
        updateLightSensitivityText(sensitivityValue)
        
        // Load lock state
        val isLocked = preferencesManager.isLightSensorLocked()
        switchLightSensorLocked.isChecked = isLocked
        
        Log.d(TAG, "loadLightSensorSettings: Light sensor enabled = $isLightSensorEnabled, sensitivity = $sensitivity, locked = $isLocked")
    }
    
    private fun loadEyeStrainReminderSettings() {
        val isRemindersEnabled = preferencesManager.isEyeStrainReminderEnabled()
        switchEyeStrainReminder.isChecked = isRemindersEnabled
        
        // Show/hide eye strain settings based on state
        layoutEyeStrainSettings.visibility = if (isRemindersEnabled) View.VISIBLE else View.GONE
        
        // Load notification style
        val style = preferencesManager.getEyeStrainNotificationStyle()
        when (style) {
            "vibration" -> radioNotificationVibration.isChecked = true
            "silent" -> radioNotificationSilent.isChecked = true
            else -> radioNotificationSound.isChecked = true // default to sound
        }
        
        Log.d(TAG, "loadEyeStrainReminderSettings: Reminders enabled = $isRemindersEnabled, style = $style")
    }
    
    private fun setupListeners() {
        // Overlay toggle switch
        switchOverlay.setOnCheckedChangeListener { _, isChecked ->
            handleOverlayToggle(isChecked)
        }
        
        // Opacity seekbar
        seekbarOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateOpacityText(progress)
                    
                    // Convert percentage (0-100) to opacity (0.0-1.0)
                    val opacity = progress / 100f
                    preferencesManager.setOpacity(opacity)
                    
                    // Update overlay in real-time if it's running
                    if (preferencesManager.isOverlayEnabled() && hasOverlayPermission()) {
                        updateOverlayOpacity(opacity)
                    }
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val finalOpacity = (seekBar?.progress ?: 0) / 100f
                logAnalyticsSafely { repository ->
                    repository.logOpacityChanged(finalOpacity)
                }
            }
        })
        
        // Permission request button
        buttonRequestPermission.setOnClickListener {
            requestOverlayPermission()
        }
        
        // Scheduling toggle switch
        switchScheduling.setOnCheckedChangeListener { _, isChecked ->
            handleSchedulingToggle(isChecked)
        }
        
        // Start time button
        buttonStartTime.setOnClickListener {
            showStartTimePicker()
        }
        
        // End time button
        buttonEndTime.setOnClickListener {
            showEndTimePicker()
        }
        
        // Location scheduling toggle switch
        switchLocationScheduling.setOnCheckedChangeListener { _, isChecked ->
            handleLocationSchedulingToggle(isChecked)
        }
        
        // Get location button
        buttonGetLocation.setOnClickListener {
            requestLocationPermissionAndUpdate()
        }
        
        // Location offset slider
        sliderLocationOffset.addOnChangeListener { _, value, _ ->
            val offsetMinutes = value.toInt()
            schedulingManager.setLocationOffset(offsetMinutes)
            updateLocationOffsetText(offsetMinutes)
            updateCalculatedTimes()
        }
        
        // Color variant radio group
        radioGroupColorVariant.setOnCheckedChangeListener { _, checkedId ->
            val selectedVariant = when (checkedId) {
                R.id.radio_red_standard -> ColorVariant.RED_STANDARD
                R.id.radio_red_orange -> ColorVariant.RED_ORANGE
                R.id.radio_red_pink -> ColorVariant.RED_PINK
                R.id.radio_high_contrast -> ColorVariant.HIGH_CONTRAST
                else -> ColorVariant.RED_STANDARD
            }
            handleColorVariantChange(selectedVariant)
        }
        
        // Battery optimization switch
        switchBatteryOptimization.setOnCheckedChangeListener { _, isChecked ->
            handleBatteryOptimizationToggle(isChecked)
        }
        
        // Light sensor toggle switch
        switchLightSensor.setOnCheckedChangeListener { _, isChecked ->
            handleLightSensorToggle(isChecked)
        }
        
        // Light sensor sensitivity slider
        sliderLightSensitivity.addOnChangeListener { _, value, _ ->
            handleLightSensitivityChange(value)
        }
        
        // Light sensor lock switch
        switchLightSensorLocked.setOnCheckedChangeListener { _, isChecked ->
            handleLightSensorLockToggle(isChecked)
        }
        
        // Eye strain reminder toggle
        switchEyeStrainReminder.setOnCheckedChangeListener { _, isChecked ->
            handleEyeStrainReminderToggle(isChecked)
        }
        
        // Notification style radio group
        radioGroupNotificationStyle.setOnCheckedChangeListener { _, checkedId ->
            handleNotificationStyleChange(checkedId)
        }
    }
    
    private fun handleOverlayToggle(isEnabled: Boolean) {
        Log.d(TAG, "handleOverlayToggle: isEnabled=$isEnabled")
        
        if (isEnabled) {
            // Check permission before enabling
            val hasPermission = hasOverlayPermission()
            Log.d(TAG, "handleOverlayToggle: hasPermission=$hasPermission")
            
            if (hasPermission) {
                Log.d(TAG, "handleOverlayToggle: Starting overlay service")
                startOverlayService()
                preferencesManager.setOverlayEnabled(true)
                logAnalyticsSafely { repository ->
                    repository.logOverlayToggled(true, preferencesManager.getOpacity())
                }
                updatePermissionUI()
            } else {
                // Permission not granted, revert toggle and show permission UI
                Log.w(TAG, "handleOverlayToggle: Permission not granted, reverting toggle")
                switchOverlay.isChecked = false
                cardPermission.visibility = View.VISIBLE
            }
        } else {
            Log.d(TAG, "handleOverlayToggle: Stopping overlay service")
            stopOverlayService()
            preferencesManager.setOverlayEnabled(false)
            logAnalyticsSafely { repository ->
                repository.logOverlayToggled(false, preferencesManager.getOpacity())
            }
        }
    }
    
    private fun startOverlayService() {
        Log.d(TAG, "startOverlayService: Creating intent")
        val intent = Intent(this, RedOverlayService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "startOverlayService: Starting foreground service (Android O+)")
                startForegroundService(intent)
            } else {
                Log.d(TAG, "startOverlayService: Starting service")
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startOverlayService: Error starting service", e)
        }
    }
    
    private fun stopOverlayService() {
        Log.d(TAG, "stopOverlayService: Stopping service")
        val intent = Intent(this, RedOverlayService::class.java)
        try {
            stopService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "stopOverlayService: Error stopping service", e)
        }
    }
    
    private fun updateOverlayOpacity(opacity: Float) {
        val intent = Intent(this, RedOverlayService::class.java).apply {
            action = RedOverlayService.ACTION_UPDATE_OPACITY
            putExtra(RedOverlayService.EXTRA_OPACITY, opacity)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun handleColorVariantChange(variant: ColorVariant) {
        Log.d(TAG, "handleColorVariantChange: variant=$variant")
        
        // Save color variant to preferences
        preferencesManager.setColorVariant(variant.name)
        
        // Update preview color
        updateColorPreview(variant)
        
        // Update overlay if it's running
        if (preferencesManager.isOverlayEnabled() && hasOverlayPermission()) {
            updateOverlayColor(variant)
        }

        logAnalyticsSafely { repository ->
            repository.logPresetApplied(variant.name, preferencesManager.getOpacity())
        }
    }

    private fun logAnalyticsSafely(action: suspend (AnalyticsRepository) -> Unit) {
        lifecycleScope.launch {
            try {
                action(analyticsRepository)
            } catch (e: Exception) {
                Log.e(TAG, "logAnalyticsSafely: Failed to log analytics event", e)
            }
        }
    }
    
    private fun updateOverlayColor(variant: ColorVariant) {
        val intent = Intent(this, RedOverlayService::class.java).apply {
            action = RedOverlayService.ACTION_UPDATE_COLOR
            putExtra(RedOverlayService.EXTRA_COLOR_VARIANT, variant.name)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun handleBatteryOptimizationToggle(isEnabled: Boolean) {
        Log.d(TAG, "handleBatteryOptimizationToggle: isEnabled=$isEnabled")
        preferencesManager.setBatteryOptimizationEnabled(isEnabled)
    }
    
    private fun handleLightSensorToggle(isEnabled: Boolean) {
        Log.d(TAG, "handleLightSensorToggle: isEnabled=$isEnabled")
        preferencesManager.setLightSensorEnabled(isEnabled)
        
        // Show/hide light sensor settings
        layoutLightSensorSettings.visibility = if (isEnabled) View.VISIBLE else View.GONE
        
        // Start or stop lux update loop
        if (isEnabled) {
            luxUpdateHandler.post(luxUpdateRunnable)
        } else {
            luxUpdateHandler.removeCallbacks(luxUpdateRunnable)
        }
        
        // Notify the service
        val intent = Intent(this, RedOverlayService::class.java)
        intent.action = "com.redscreenfilter.LIGHT_SENSOR_CHANGED"
        startService(intent)
    }
    
    private fun handleLightSensitivityChange(value: Float) {
        val sensitivity = when (value.toInt()) {
            0 -> "low"
            1 -> "medium"
            else -> "high"
        }
        Log.d(TAG, "handleLightSensitivityChange: sensitivity=$sensitivity")
        preferencesManager.setLightSensorSensitivity(sensitivity)
        updateLightSensitivityText(value)
    }
    
    private fun handleLightSensorLockToggle(isLocked: Boolean) {
        Log.d(TAG, "handleLightSensorLockToggle: isLocked=$isLocked")
        preferencesManager.setLightSensorLocked(isLocked)
    }
    
    private fun handleEyeStrainReminderToggle(isEnabled: Boolean) {
        Log.d(TAG, "handleEyeStrainReminderToggle: isEnabled=$isEnabled")
        preferencesManager.setEyeStrainReminderEnabled(isEnabled)
        
        // Show/hide eye strain settings
        layoutEyeStrainSettings.visibility = if (isEnabled) View.VISIBLE else View.GONE
        
        // Schedule or cancel the reminder worker
        if (isEnabled) {
            scheduleEyeStrainReminder()
        } else {
            cancelEyeStrainReminder()
        }
    }
    
    private fun handleNotificationStyleChange(checkedId: Int) {
        val style = when (checkedId) {
            R.id.radio_notification_vibration -> "vibration"
            R.id.radio_notification_silent -> "silent"
            else -> "sound" // default to sound
        }
        Log.d(TAG, "handleNotificationStyleChange: style=$style")
        preferencesManager.setEyeStrainNotificationStyle(style)
    }
    
    private fun scheduleEyeStrainReminder() {
        Log.d(TAG, "scheduleEyeStrainReminder: Scheduling 20-minute reminder worker")
        try {
            // Schedule a periodic work that runs every 20 minutes
            val reminderWorker = androidx.work.PeriodicWorkRequestBuilder<com.redscreenfilter.worker.EyeStrainReminder>(
                20, java.util.concurrent.TimeUnit.MINUTES
            ).build()
            
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "eye_strain_reminder",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                reminderWorker
            )
        } catch (e: Exception) {
            Log.e(TAG, "scheduleEyeStrainReminder: Error scheduling worker", e)
        }
    }
    
    private fun cancelEyeStrainReminder() {
        Log.d(TAG, "cancelEyeStrainReminder: Cancelling reminder worker")
        try {
            androidx.work.WorkManager.getInstance(this).cancelUniqueWork("eye_strain_reminder")
        } catch (e: Exception) {
            Log.e(TAG, "cancelEyeStrainReminder: Error cancelling worker", e)
        }
    }
    
    private fun updateLightSensitivityText(value: Float) {
        val sensitivity = when (value.toInt()) {
            0 -> getString(R.string.light_sensitivity_low)
            1 -> getString(R.string.light_sensitivity_medium)
            else -> getString(R.string.light_sensitivity_high)
        }
        textLightSensitivityValue.text = sensitivity
    }
    
    private fun updateOpacityText(percentage: Int) {
        textOpacityValue.text = getString(R.string.opacity_value_format, percentage)
    }
    
    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Permission not required on Android < M
        }
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
    
    private fun updatePermissionUI() {
        if (hasOverlayPermission()) {
            cardPermission.visibility = View.GONE
        } else {
            cardPermission.visibility = View.VISIBLE
        }
    }

    private fun requestRuntimePermissions() {
        if (!hasOverlayPermission()) {
            requestOverlayPermission()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            postNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestLocationPermissionsIfNeeded() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted || !coarseLocationGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            requestLocationAndUpdate()
        }
    }
    
    // ========== Scheduling Methods ==========
    
    private fun handleSchedulingToggle(isEnabled: Boolean) {
        Log.d(TAG, "handleSchedulingToggle: isEnabled=$isEnabled")
        schedulingManager.setScheduleEnabled(isEnabled)
        
        // Show/hide time pickers
        layoutTimePickers.visibility = if (isEnabled) View.VISIBLE else View.GONE
        
        // Schedule or cancel WorkManager periodic work
        if (isEnabled) {
            Log.d(TAG, "handleSchedulingToggle: Scheduling WorkManager periodic work")
            WorkScheduler.schedulePeriodicWork(this)
            checkAndApplySchedule()
        } else {
            Log.d(TAG, "handleSchedulingToggle: Cancelling WorkManager periodic work")
            WorkScheduler.cancelPeriodicWork(this)
        }
    }
    
    private fun showStartTimePicker() {
        val (hour, minute) = schedulingManager.getStartTimeComponents()
        
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // Save the new time
            val timeString = schedulingManager.formatTime(selectedHour, selectedMinute)
            schedulingManager.setSchedule(timeString, preferencesManager.getScheduleEndTime())
            
            // Update button text
            buttonStartTime.text = timeString
            
            // Check if schedule state changed
            checkAndApplySchedule()
            
            Log.d(TAG, "showStartTimePicker: Start time set to $timeString")
        }, hour, minute, true).show()
    }
    
    private fun showEndTimePicker() {
        val (hour, minute) = schedulingManager.getEndTimeComponents()
        
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // Save the new time
            val timeString = schedulingManager.formatTime(selectedHour, selectedMinute)
            schedulingManager.setSchedule(preferencesManager.getScheduleStartTime(), timeString)
            
            // Update button text
            buttonEndTime.text = timeString
            
            // Check if schedule state changed
            checkAndApplySchedule()
            
            Log.d(TAG, "showEndTimePicker: End time set to $timeString")
        }, hour, minute, true).show()
    }
    
    private fun checkAndApplySchedule() {
        if (!schedulingManager.isScheduleEnabled()) {
            return
        }
        
        val shouldBeActive = schedulingManager.getScheduledState()
        val isCurrentlyActive = preferencesManager.isOverlayEnabled()
        
        Log.d(TAG, "checkAndApplySchedule: shouldBeActive=$shouldBeActive, isCurrentlyActive=$isCurrentlyActive")
        
        if (shouldBeActive && !isCurrentlyActive && hasOverlayPermission()) {
            // Schedule says overlay should be active, but it's not
            Log.d(TAG, "checkAndApplySchedule: Activating overlay per schedule")
            switchOverlay.isChecked = true
            startOverlayService()
            preferencesManager.setOverlayEnabled(true)
        } else if (!shouldBeActive && isCurrentlyActive) {
            // Schedule says overlay should be inactive, but it's active
            Log.d(TAG, "checkAndApplySchedule: Deactivating overlay per schedule")
            switchOverlay.isChecked = false
            stopOverlayService()
            preferencesManager.setOverlayEnabled(false)
        }
    }
    
    // ========== Location Scheduling Methods ==========
    
    private fun handleLocationSchedulingToggle(isEnabled: Boolean) {
        Log.d(TAG, "handleLocationSchedulingToggle: isEnabled=$isEnabled")
        schedulingManager.setLocationScheduleEnabled(isEnabled)
        
        // Show/hide location settings
        layoutLocationSettings.visibility = if (isEnabled) View.VISIBLE else View.GONE
        
        // If enabled, ensure we have location data
        if (isEnabled) {
            val cachedLocation = locationManager.getCachedLocation()
            if (cachedLocation == null) {
                Log.d(TAG, "handleLocationSchedulingToggle: No cached location, requesting new location")
                requestLocationPermissionsIfNeeded()
            } else {
                updateCalculatedTimes()
            }
        }
    }
    
    private fun requestLocationPermissionAndUpdate() {
        requestLocationPermissionsIfNeeded()
    }
    
    private fun requestLocationAndUpdate() {
        Log.d(TAG, "requestLocationAndUpdate: Requesting location")
        
        buttonGetLocation.isEnabled = false
        buttonGetLocation.text = "Getting location..."
        
        locationManager.requestLocation(
            onSuccess = { latitude, longitude ->
                Log.d(TAG, "requestLocationAndUpdate: Success - lat=$latitude, lon=$longitude")
                runOnUiThread {
                    buttonGetLocation.isEnabled = true
                    buttonGetLocation.text = getString(R.string.location_get_location)
                    updateCalculatedTimes()
                    
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Location updated successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            },
            onError = { error ->
                Log.e(TAG, "requestLocationAndUpdate: Error - $error")
                runOnUiThread {
                    buttonGetLocation.isEnabled = true
                    buttonGetLocation.text = getString(R.string.location_get_location)
                    
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Failed to get location: $error",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
    
    private fun updateCalculatedTimes() {
        val sunsetTime = schedulingManager.getCalculatedSunsetTime()
        val sunriseTime = schedulingManager.getCalculatedSunriseTime()
        
        if (sunsetTime != null && sunriseTime != null) {
            textSunsetTime.text = sunsetTime
            textSunriseTime.text = sunriseTime
        } else {
            textSunsetTime.text = "--:--"
            textSunriseTime.text = "--:--"
        }
    }
    
    private fun updateLocationOffsetText(offsetMinutes: Int) {
        textLocationOffsetValue.text = getString(R.string.location_offset_value, offsetMinutes)
    }
    
    private fun setupSectionTabs() {
        val sections = listOf(sectionDisplay, sectionAutomation, sectionWellness)
        settingsTabs.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val target = when (checkedId) {
                R.id.tab_automation -> sectionAutomation
                R.id.tab_wellness -> sectionWellness
                else -> sectionDisplay
            }
            switchToSection(target, sections)
        }

        settingsTabs.check(R.id.tab_display)
        switchToSection(sectionDisplay, sections)
    }

    private fun switchToSection(target: View, sections: List<View>) {
        sections.forEach { section ->
            if (section == target) {
                if (!section.isVisible) {
                    section.alpha = 0f
                    section.visibility = View.VISIBLE
                }
                section.animate()
                    .alpha(1f)
                    .setDuration(240)
                    .setInterpolator(sectionInterpolator)
                    .start()
            } else if (section.isVisible) {
                section.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(sectionInterpolator)
                    .withEndAction { section.visibility = View.GONE }
                    .start()
            }
        }
    }

    private fun animateSettingsEntrance() {
        scrollSettings.alpha = 0f
        scrollSettings.translationY = 36f
        scrollSettings.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(420)
            .setInterpolator(sectionInterpolator)
            .start()
    }

    private fun setupBottomNavigation() {
        // Set settings as default selected item
        bottomNavigation.selectedItemId = R.id.menu_settings
        
        // Handle navigation item selection
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_settings -> {
                    Log.d(TAG, "Settings tab selected")
                    showSettingsFragment()
                    true
                }
                R.id.menu_analytics -> {
                    Log.d(TAG, "Analytics tab selected")
                    showAnalyticsFragment()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showSettingsFragment() {
        // Show settings ScrollView
        scrollSettings.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
        // Hide analytics fragment if visible
        supportFragmentManager.findFragmentByTag("analytics_fragment")?.let {
            supportFragmentManager.beginTransaction()
                .hide(it)
                .commit()
        }
        animateSettingsEntrance()
    }
    
    private fun showAnalyticsFragment() {
        // Hide settings ScrollView
        scrollSettings.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        
        // Create or show analytics fragment
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentByTag("analytics_fragment")
        
        if (existingFragment != null) {
            fragmentManager.beginTransaction()
                .show(existingFragment)
                .commit()
        } else {
            val analyticsFragment = AnalyticsFragment()
            fragmentManager.beginTransaction()
                .add(R.id.fragment_container, analyticsFragment, "analytics_fragment")
                .commit()
        }
    }
}
