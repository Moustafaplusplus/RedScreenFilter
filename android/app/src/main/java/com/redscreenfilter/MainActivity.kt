package com.redscreenfilter

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.redscreenfilter.data.LocationManager
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.service.RedOverlayService
import com.redscreenfilter.utils.WorkScheduler

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
        
        // Initialize UI components
        initializeViews()
        
        // Load saved settings
        loadSettings()
        
        // Setup listeners
        setupListeners()
        
        Log.d(TAG, "onCreate: Activity initialized")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Checking permission status")
        // Check permission status when returning to app
        updatePermissionUI()
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
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
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
                requestLocationPermissionAndUpdate()
            } else {
                updateCalculatedTimes()
            }
        }
    }
    
    private fun requestLocationPermissionAndUpdate() {
        if (locationManager.hasLocationPermission()) {
            requestLocationAndUpdate()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
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
}
