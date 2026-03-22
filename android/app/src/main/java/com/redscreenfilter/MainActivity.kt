package com.redscreenfilter

import android.Manifest
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import java.util.concurrent.TimeUnit
import com.google.android.material.snackbar.Snackbar
import com.redscreenfilter.core.model.ColorVariant
import com.redscreenfilter.data.LightSensorManager
import com.redscreenfilter.data.LocationManager
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.data.SchedulingManager
import com.redscreenfilter.data.ExemptedAppsManager
import com.redscreenfilter.data.repository.AnalyticsRepository
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.core.designsystem.RsfTheme
import com.redscreenfilter.feature.settings.coordinator.OverlayControlCoordinator
import com.redscreenfilter.feature.settings.coordinator.PermissionCoordinator
import com.redscreenfilter.feature.settings.coordinator.ScheduleCoordinator
import com.redscreenfilter.feature.analytics.ui.AnalyticsComposeScreen
import com.redscreenfilter.feature.analytics.ui.AnalyticsComposeUiState
import com.redscreenfilter.feature.app_exemption.ui.AppExemptionComposeScreen
import com.redscreenfilter.feature.app_exemption.ui.AppExemptionComposeUiState
import com.redscreenfilter.feature.settings.ui.AutomationComposeUiState
import com.redscreenfilter.feature.settings.ui.AutomationSettingsSectionCompose
import com.redscreenfilter.feature.settings.ui.BrightnessComposeUiState
import com.redscreenfilter.feature.settings.ui.DisplayComposeUiState
import com.redscreenfilter.feature.settings.ui.DisplaySettingsSectionCompose
import com.redscreenfilter.feature.settings.ui.OverlayVisibilityComposeUiState
import com.redscreenfilter.feature.settings.ui.OverlayVisibilitySettingsSectionCompose
import com.redscreenfilter.feature.settings.ui.WellnessComposeUiState
import com.redscreenfilter.feature.settings.ui.WellnessSettingsSectionCompose
import com.redscreenfilter.feature.settings.ui.AboutSettingsSectionCompose
import com.redscreenfilter.data.repository.AnalyticsRepository.AnalyticsPeriod
import com.redscreenfilter.feature.settings.viewmodel.AutomationSettingsViewModel
import com.redscreenfilter.feature.settings.viewmodel.BrightnessSettingsViewModel
import com.redscreenfilter.feature.settings.viewmodel.DisplaySettingsViewModel
import com.redscreenfilter.feature.settings.viewmodel.WellnessSettingsViewModel
import com.redscreenfilter.utils.WorkScheduler
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

/**
 * MainActivity - Main UI for Red Screen Filter
 */
class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var schedulingManager: SchedulingManager
    private lateinit var locationManager: LocationManager
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var exemptedAppsManager: ExemptedAppsManager

    private lateinit var displaySettingsViewModel: DisplaySettingsViewModel
    private lateinit var brightnessSettingsViewModel: BrightnessSettingsViewModel
    private lateinit var automationSettingsViewModel: AutomationSettingsViewModel
    private lateinit var wellnessSettingsViewModel: WellnessSettingsViewModel
    private lateinit var permissionCoordinator: PermissionCoordinator
    private lateinit var overlayControlCoordinator: OverlayControlCoordinator
    private lateinit var scheduleCoordinator: ScheduleCoordinator
    private var isLocationLoading: Boolean = false
    private var displayComposeUiState by mutableStateOf(
        DisplayComposeUiState(
            isOverlayEnabled = false,
            opacityPercentage = 50,
            selectedColorVariant = ColorVariant.RED_STANDARD,
            showPermissionCard = false
        )
    )
    private var automationComposeUiState by mutableStateOf(
        AutomationComposeUiState(
            isSchedulingEnabled = false,
            startTime = "21:00",
            endTime = "07:00",
            isLocationSchedulingEnabled = false,
            isLocationLoading = false,
            sunsetTime = "--:--",
            sunriseTime = "--:--",
            locationOffsetMinutes = 0,
            isLightSensorEnabled = false,
            lightSensitivityValue = 1f,
            lightSensitivityLabel = "",
            currentLuxLabel = "",
            isLightSensorLocked = false
        )
    )
    private var brightnessComposeUiState by mutableStateOf(
        BrightnessComposeUiState(
            brightnessPercentage = 50,
            hasSystemBrightnessPermission = false,
            isExtraDimEnabled = false,
            extraDimIntensityPercentage = 35
        )
    )
    private var wellnessComposeUiState by mutableStateOf(
        WellnessComposeUiState(
            isBatteryOptimizationEnabled = true,
            isEyeStrainReminderEnabled = false,
            notificationStyle = "sound"
        )
    )
    private var overlayVisibilityComposeUiState by mutableStateOf(
        OverlayVisibilityComposeUiState(
            hideOnLockScreen = false,
            hideOnHomeScreen = false
        )
    )
    private var analyticsComposeUiState by mutableStateOf(
        AnalyticsComposeUiState(
            selectedPeriod = AnalyticsPeriod.TODAY,
            subtitle = "",
            usageTime = "00:00:00",
            usageLabel = "",
            usageProgress = 0,
            averageOpacityText = "0%",
            mostUsedPreset = "N/A",
            currentStreakText = "0",
            totalEventsText = "0",
            isLoading = false,
            hasError = false
        )
    )
    private var appExemptionComposeUiState by mutableStateOf(
        AppExemptionComposeUiState(
            query = "",
            isLoading = false,
            apps = emptyList(),
            hasUsageStatsPermission = true
        )
    )
    private var selectedRootTab by mutableStateOf(RootTab.SETTINGS)
    private var selectedSettingsTab by mutableStateOf(0)

    private enum class RootTab { SETTINGS, ANALYTICS, EXEMPTIONS }
    
    // Coroutine job for lux updates
    private var luxUpdateJob: Job? = null
    
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
            isLocationLoading = false
            refreshAutomationComposeUiState()
        }
    }

    private val postNotificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.w(TAG, "POST_NOTIFICATIONS permission denied")
        }
    }
    
    // State change receiver - listens for external state changes from service, tiles, etc.
    private val stateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_OVERLAY_STATE_CHANGED -> {
                    Log.d(TAG, "stateChangeReceiver: Overlay state changed externally")
                    refreshDisplayComposeUiState()
                }
                ACTION_OVERLAY_OPACITY_CHANGED -> {
                    Log.d(TAG, "stateChangeReceiver: Overlay opacity changed externally")
                    refreshDisplayComposeUiState()
                    refreshBrightnessComposeUiState()
                }
            }
        }
    }
    
    // Coroutine job for collecting state flows
    private var stateCollectionJob: Job? = null
    
    companion object {
        const val ACTION_OVERLAY_STATE_CHANGED = "com.redscreenfilter.OVERLAY_STATE_CHANGED"
        const val ACTION_OVERLAY_OPACITY_CHANGED = "com.redscreenfilter.OVERLAY_OPACITY_CHANGED"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesManager = PreferencesManager.getInstance(this)
        schedulingManager = SchedulingManager.getInstance(this)
        locationManager = LocationManager.getInstance(this)
        analyticsRepository = AnalyticsRepository.getInstance(this)
        exemptedAppsManager = ExemptedAppsManager.getInstance(this)

        displaySettingsViewModel = DisplaySettingsViewModel(preferencesManager)
        brightnessSettingsViewModel = BrightnessSettingsViewModel(preferencesManager)
        automationSettingsViewModel = AutomationSettingsViewModel(schedulingManager, preferencesManager)
        wellnessSettingsViewModel = WellnessSettingsViewModel(preferencesManager)
        permissionCoordinator = PermissionCoordinator()
        overlayControlCoordinator = OverlayControlCoordinator(this)
        scheduleCoordinator = ScheduleCoordinator(
            overlayControlCoordinator = overlayControlCoordinator,
            permissionCoordinator = permissionCoordinator,
            automationSettingsViewModel = automationSettingsViewModel
        )
        
        loadSettings()
        loadAnalyticsData()
        loadApps()

        setContent {
            RedScreenFilterTheme {
                MainActivityComposeRoot()
            }
        }

        requestRuntimePermissions()
        
        if (preferencesManager.isEyeStrainReminderEnabled()) {
            scheduleEyeStrainReminder()
        }
        
        // Register broadcast receiver for external state changes
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_OVERLAY_STATE_CHANGED)
            addAction(ACTION_OVERLAY_OPACITY_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateChangeReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(stateChangeReceiver, intentFilter)
        }
        
        // Start collecting state flows
        startCollectingStateFlows()
    }
    
    override fun onResume() {
        super.onResume()
        updatePermissionUI()
        refreshDisplayComposeUiState()
        refreshBrightnessComposeUiState()
        refreshAutomationComposeUiState()
        refreshWellnessComposeUiState()
        refreshOverlayVisibilityComposeUiState()
        refreshAppExemptionUiState()
        
        if (preferencesManager.isLightSensorEnabled()) {
            startLuxUpdates()
        }
    }
    
    override fun onPause() {
        super.onPause()
        stopLuxUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLuxUpdates()
        // Stop collecting state flows
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        // Unregister broadcast receiver
        try {
            unregisterReceiver(stateChangeReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "onDestroy: Error unregistering broadcast receiver", e)
        }
    }
    
    private fun startCollectingStateFlows() {
        stateCollectionJob?.cancel()
        stateCollectionJob = lifecycleScope.launch {
            try {
                // Collect overlay enabled state changes
                displaySettingsViewModel.overlayEnabledFlow.collect { isEnabled ->
                    Log.d(TAG, "startCollectingStateFlows: Overlay enabled state changed to $isEnabled")
                    displayComposeUiState = displayComposeUiState.copy(isOverlayEnabled = isEnabled)
                }
            } catch (e: Exception) {
                Log.e(TAG, "startCollectingStateFlows: Error collecting overlay enabled state", e)
            }
        }
        
        lifecycleScope.launch {
            try {
                // Collect opacity value changes
                displaySettingsViewModel.opacityFlow.collect { opacity ->
                    Log.d(TAG, "startCollectingStateFlows: Opacity state changed to $opacity")
                    val percentage = (opacity * 100).toInt()
                    displayComposeUiState = displayComposeUiState.copy(opacityPercentage = percentage)
                    brightnessComposeUiState = brightnessComposeUiState.copy()
                }
            } catch (e: Exception) {
                Log.e(TAG, "startCollectingStateFlows: Error collecting opacity state", e)
            }
        }
    }
    
    private fun startLuxUpdates() {
        stopLuxUpdates()
        luxUpdateJob = lifecycleScope.launch {
            while (isActive && preferencesManager.isLightSensorEnabled()) {
                try {
                    val lightSensorManager = LightSensorManager.getInstance(this@MainActivity)
                    val currentLux = lightSensorManager.getCurrentLux()
                    val luxLabel = getString(R.string.current_lux_label).replace("--", currentLux.toInt().toString())
                    refreshAutomationComposeUiState(luxOverride = luxLabel)
                } catch (e: Exception) {
                    Log.e(TAG, "startLuxUpdates: Error updating lux", e)
                }
                delay(500)
            }
        }
    }
    
    private fun stopLuxUpdates() {
        luxUpdateJob?.cancel()
        luxUpdateJob = null
    }

    @Composable
    private fun MainActivityComposeRoot() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RsfTheme.colors.surface,
                            RsfTheme.colors.onError.copy(alpha = 0.15f),
                            RsfTheme.colors.primary.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedRootTab == RootTab.SETTINGS,
                            onClick = { selectedRootTab = RootTab.SETTINGS },
                            icon = { Icon(Icons.Default.Settings, contentDescription = getString(R.string.settings_label)) },
                            label = { Text(getString(R.string.settings_label)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = RsfTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedRootTab == RootTab.ANALYTICS,
                            onClick = {
                                selectedRootTab = RootTab.ANALYTICS
                                loadAnalyticsData()
                            },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = getString(R.string.analytics_title)) },
                            label = { Text(getString(R.string.analytics_title)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = RsfTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedRootTab == RootTab.EXEMPTIONS,
                            onClick = {
                                selectedRootTab = RootTab.EXEMPTIONS
                                loadApps()
                            },
                            icon = { Icon(Icons.Default.Apps, contentDescription = getString(R.string.app_exemptions)) },
                            label = { Text(getString(R.string.app_exemptions)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = RsfTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            ) { paddingValues ->
                when (selectedRootTab) {
                    RootTab.SETTINGS -> SettingsComposeScreen(paddingValues)
                    RootTab.ANALYTICS -> AnalyticsComposeScreen(
                        uiState = analyticsComposeUiState,
                        onPeriodSelected = { period -> loadPeriodStats(period) },
                        modifier = Modifier.padding(paddingValues)
                    )
                    RootTab.EXEMPTIONS -> AppExemptionComposeScreen(
                        uiState = appExemptionComposeUiState,
                        onQueryChanged = { query ->
                            appExemptionComposeUiState = appExemptionComposeUiState.copy(query = query)
                            if (query.isBlank()) loadApps() else searchApps(query)
                        },
                        onExemptionChanged = { packageName, isExempt ->
                            exemptedAppsManager.toggleAppExemption(packageName, isExempt)
                            val updated = appExemptionComposeUiState.apps.map {
                                if (it.packageName == packageName) it.copy(isExempted = isExempt) else it
                            }
                            appExemptionComposeUiState = appExemptionComposeUiState.copy(apps = updated)
                        },
                        onRequestPermission = { permissionCoordinator.requestUsageStatsPermission(this@MainActivity) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsComposeScreen(paddingValues: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.redscreenfilter.core.designsystem.RsfSegmentedTabs(
                options = listOf(
                    getString(R.string.tab_display),
                    getString(R.string.tab_automation),
                    getString(R.string.tab_wellness),
                    getString(R.string.tab_visibility),
                    getString(R.string.tab_about)
                ),
                selectedIndex = selectedSettingsTab,
                onSelectionChanged = { selectedSettingsTab = it }
            )

            when (selectedSettingsTab) {
                0 -> DisplaySettingsSectionCompose(
                    uiState = displayComposeUiState,
                    onOverlayToggle = { isEnabled -> handleOverlayToggle(isEnabled) },
                    onOpacityChanged = { progress -> onDisplayOpacityChanged(progress) },
                    onOpacityChangeFinished = { progress -> onDisplayOpacityChangeFinished(progress) },
                    onColorVariantSelected = { variant -> handleColorVariantChange(variant) },
                    isExtraDimEnabled = brightnessComposeUiState.isExtraDimEnabled,
                    extraDimIntensityPercentage = brightnessComposeUiState.extraDimIntensityPercentage,
                    onExtraDimToggle = { isEnabled -> handleExtraDimToggle(isEnabled) },
                    onExtraDimIntensityChanged = { value -> handleExtraDimIntensityChanged(value) },
                    onRequestPermission = { requestOverlayPermission() }
                )
                1 -> AutomationSettingsSectionCompose(
                    uiState = automationComposeUiState,
                    onSchedulingToggle = { isEnabled -> handleSchedulingToggle(isEnabled) },
                    onStartTimeClick = { showStartTimePicker() },
                    onEndTimeClick = { showEndTimePicker() },
                    onLocationSchedulingToggle = { isEnabled -> handleLocationSchedulingToggle(isEnabled) },
                    onRequestLocation = { requestLocationPermissionAndUpdate() },
                    onLocationOffsetChanged = { offsetMinutes -> handleLocationOffsetChange(offsetMinutes) },
                    onLightSensorToggle = { isEnabled -> handleLightSensorToggle(isEnabled) },
                    onLightSensitivityChanged = { value -> handleLightSensitivityChange(value) },
                    onLightSensorLockToggle = { isLocked -> handleLightSensorLockToggle(isLocked) }
                )
                2 -> WellnessSettingsSectionCompose(
                    uiState = wellnessComposeUiState,
                    onBatteryOptimizationToggle = { isEnabled -> handleBatteryOptimizationToggle(isEnabled) },
                    onEyeStrainReminderToggle = { isEnabled -> handleEyeStrainReminderToggle(isEnabled) },
                    onNotificationStyleSelected = { style -> handleNotificationStyleChange(style) }
                )
                3 -> OverlayVisibilitySettingsSectionCompose(
                    uiState = overlayVisibilityComposeUiState,
                    onHideOnLockScreenChanged = { isEnabled -> handleHideOverlayOnLockScreenToggle(isEnabled) },
                    onHideOnHomeScreenChanged = { isEnabled -> handleHideOverlayOnHomeScreenToggle(isEnabled) }
                )
                4 -> AboutSettingsSectionCompose()
            }
        }
    }
    
    private fun loadSettings() {
        loadSchedulingSettings()
        loadLocationSchedulingSettings()
        
        // Load initial state for Extra Dim controls
        brightnessSettingsViewModel.loadState()

        refreshDisplayComposeUiState()
        refreshBrightnessComposeUiState()
        refreshAutomationComposeUiState()
        refreshWellnessComposeUiState()
        refreshOverlayVisibilityComposeUiState()
    }
    
    private fun loadSchedulingSettings() {
        val automationState = automationSettingsViewModel.loadState()
        if (automationState.isSchedulingEnabled) {
            WorkScheduler.schedulePeriodicWork(this)
            scheduleCoordinator.refreshSchedule(this)
        }
    }
    
    private fun loadLocationSchedulingSettings() {
        updateCalculatedTimes()
    }
    
    private fun handleOverlayToggle(isEnabled: Boolean) {
        // Update preference FIRST to avoid race condition with service start
        val displayState = displaySettingsViewModel.onOverlayToggled(isEnabled)
        
        if (isEnabled) {
            val hasPermission = permissionCoordinator.hasOverlayPermission(this)
            if (hasPermission) {
                overlayControlCoordinator.startOverlayService()
                logAnalyticsSafely { repository ->
                    repository.logOverlayToggled(true, displayState.opacity)
                }
                updatePermissionUI()
            } else {
                setPermissionCardVisible(true)
            }
        } else {
            if (!preferencesManager.isExtraDimEnabled()) {
                overlayControlCoordinator.stopOverlayService()
            } else {
                // Just update the service to reflect that Red Filter is now OFF (opacity 0)
                // but keep it running for Extra Dim
                overlayControlCoordinator.updateOverlayOpacity(0f)
            }
            logAnalyticsSafely { repository ->
                repository.logOverlayToggled(false, displayState.opacity)
            }
        }
        refreshDisplayComposeUiState()
    }
    
    private fun handleColorVariantChange(variant: ColorVariant) {
        val displayState = displaySettingsViewModel.onColorVariantChanged(variant)
        if (displayState.isOverlayEnabled && permissionCoordinator.hasOverlayPermission(this)) {
            overlayControlCoordinator.updateOverlayColor(variant)
        }
        logAnalyticsSafely { repository ->
            repository.logPresetApplied(variant.name, displayState.opacity)
        }
        refreshDisplayComposeUiState()
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
    
    private fun handleBatteryOptimizationToggle(isEnabled: Boolean) {
        wellnessSettingsViewModel.onBatteryOptimizationToggled(isEnabled)
        refreshWellnessComposeUiState()
    }
    
    private fun handleLightSensorToggle(isEnabled: Boolean) {
        val wellnessState = wellnessSettingsViewModel.onLightSensorToggled(isEnabled)
        if (wellnessState.isLightSensorEnabled) {
            startLuxUpdates()
        } else {
            stopLuxUpdates()
        }
        overlayControlCoordinator.notifyLightSensorChanged()
        refreshAutomationComposeUiState()
    }
    
    private fun handleLightSensitivityChange(value: Float) {
        wellnessSettingsViewModel.onLightSensitivityChanged(value)
        refreshAutomationComposeUiState()
    }
    
    private fun handleLightSensorLockToggle(isLocked: Boolean) {
        wellnessSettingsViewModel.onLightSensorLockToggled(isLocked)
        refreshAutomationComposeUiState()
    }
    
    private fun handleEyeStrainReminderToggle(isEnabled: Boolean) {
        val wellnessState = wellnessSettingsViewModel.onEyeStrainReminderToggled(isEnabled)
        if (wellnessState.isEyeStrainReminderEnabled) {
            scheduleEyeStrainReminder()
        } else {
            cancelEyeStrainReminder()
        }
        refreshWellnessComposeUiState()
    }
    
    private fun scheduleEyeStrainReminder() {
        try {
            val reminderWorker = androidx.work.PeriodicWorkRequestBuilder<com.redscreenfilter.worker.EyeStrainReminder>(
                20, java.util.concurrent.TimeUnit.MINUTES
            ).build()
            
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "eye_strain_reminder",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                reminderWorker
            )
        } catch (e: Exception) {
            Log.e(TAG, "scheduleEyeStrainReminder: Error", e)
        }
    }
    
    private fun cancelEyeStrainReminder() {
        try {
            androidx.work.WorkManager.getInstance(this).cancelUniqueWork("eye_strain_reminder")
        } catch (e: Exception) {
            Log.e(TAG, "cancelEyeStrainReminder: Error", e)
        }
    }
    
    private fun hasOverlayPermission(): Boolean = permissionCoordinator.hasOverlayPermission(this)
    
    private fun requestOverlayPermission() = permissionCoordinator.requestOverlayPermission(this)
    
    private fun updatePermissionUI() = setPermissionCardVisible(!hasOverlayPermission())

    private fun requestRuntimePermissions() {
        if (!hasOverlayPermission()) requestOverlayPermission()
        if (permissionCoordinator.shouldRequestPostNotifications(this)) {
            postNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestLocationPermissionsIfNeeded() {
        if (!permissionCoordinator.hasLocationPermissions(this)) {
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
    
    private fun handleSchedulingToggle(isEnabled: Boolean) {
        if (isEnabled && !permissionCoordinator.hasExactAlarmPermission(this)) {
            permissionCoordinator.requestExactAlarmPermission(this)
            // Note: We don't toggle yet because we need the permission first.
            // The user will come back after granting it.
            return
        }

        val automationState = automationSettingsViewModel.onSchedulingToggled(isEnabled)
        if (automationState.isSchedulingEnabled) {
            scheduleCoordinator.onSchedulingToggled(this, true)
        } else {
            scheduleCoordinator.onSchedulingToggled(this, false)
        }
        refreshAutomationComposeUiState()
    }
    
    private fun showStartTimePicker() {
        val (hour, minute) = schedulingManager.getStartTimeComponents()
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            automationSettingsViewModel.onStartTimeChanged(selectedHour, selectedMinute)
            checkAndApplySchedule()
            refreshAutomationComposeUiState()
        }, hour, minute, true).show()
    }
    
    private fun showEndTimePicker() {
        val (hour, minute) = schedulingManager.getEndTimeComponents()
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            automationSettingsViewModel.onEndTimeChanged(selectedHour, selectedMinute)
            checkAndApplySchedule()
            refreshAutomationComposeUiState()
        }, hour, minute, true).show()
    }
    
    private fun checkAndApplySchedule() {
        val automationState = automationSettingsViewModel.loadState()
        if (!automationState.isSchedulingEnabled) return
        scheduleCoordinator.refreshSchedule(this)
        refreshDisplayComposeUiState()
        refreshAutomationComposeUiState()
    }

    private fun onDisplayOpacityChanged(progress: Int) {
        val displayState = displaySettingsViewModel.onOpacityChanged(progress)
        if (displayState.isOverlayEnabled && permissionCoordinator.hasOverlayPermission(this)) {
            overlayControlCoordinator.updateOverlayOpacity(displayState.opacity)
        }
        refreshDisplayComposeUiState()
    }

    private fun onDisplayOpacityChangeFinished(progress: Int) {
        val finalOpacity = progress / 100f
        logAnalyticsSafely { repository -> repository.logOpacityChanged(finalOpacity) }
    }

    private fun setPermissionCardVisible(visible: Boolean) {}

    private fun refreshDisplayComposeUiState() {
        val displayState = displaySettingsViewModel.loadState()
        displayComposeUiState = DisplayComposeUiState(
            isOverlayEnabled = displayState.isOverlayEnabled,
            opacityPercentage = displayState.opacityPercentage,
            selectedColorVariant = displayState.colorVariant,
            showPermissionCard = !permissionCoordinator.hasOverlayPermission(this)
        )
    }

    private fun refreshAutomationComposeUiState(luxOverride: String? = null) {
        val automationState = automationSettingsViewModel.loadState()
        val wellnessState = wellnessSettingsViewModel.loadState()
        val luxLabel = luxOverride ?: getString(R.string.current_lux_label).replace("--", LightSensorManager.getInstance(this).getCurrentLux().toInt().toString())
        automationComposeUiState = AutomationComposeUiState(
            isSchedulingEnabled = automationState.isSchedulingEnabled,
            startTime = automationState.scheduleStartLabel,
            endTime = automationState.scheduleEndLabel,
            isLocationSchedulingEnabled = automationState.isLocationSchedulingEnabled,
            isLocationLoading = isLocationLoading,
            sunsetTime = automationState.sunsetTime ?: "--:--",
            sunriseTime = automationState.sunriseTime ?: "--:--",
            locationOffsetMinutes = automationState.locationOffsetMinutes,
            isLightSensorEnabled = wellnessState.isLightSensorEnabled,
            lightSensitivityValue = wellnessState.lightSensitivityValue,
            lightSensitivityLabel = getLightSensitivityLabel(wellnessState.lightSensitivityValue),
            currentLuxLabel = luxLabel,
            isLightSensorLocked = wellnessState.isLightSensorLocked
        )
    }

    private fun refreshBrightnessComposeUiState() {
        val brightnessState = brightnessSettingsViewModel.loadState()
        brightnessComposeUiState = BrightnessComposeUiState(
            brightnessPercentage = brightnessState.brightnessPercentage,
            hasSystemBrightnessPermission = true,
            isExtraDimEnabled = brightnessState.isExtraDimEnabled,
            extraDimIntensityPercentage = brightnessState.extraDimIntensityPercentage
        )
    }

    private fun handleExtraDimToggle(isEnabled: Boolean) {
        val brightnessState = brightnessSettingsViewModel.onExtraDimToggled(isEnabled)
        if (isEnabled) {
            if (permissionCoordinator.hasOverlayPermission(this)) {
                overlayControlCoordinator.startOverlayService()
            } else {
                requestOverlayPermission()
            }
        } else if (!preferencesManager.isOverlayEnabled()) {
            overlayControlCoordinator.stopOverlayService()
        }
        overlayControlCoordinator.updateExtraDim(isEnabled, brightnessState.extraDimIntensity)
        refreshBrightnessComposeUiState()
    }

    private fun handleExtraDimIntensityChanged(percentage: Int) {
        val brightnessState = brightnessSettingsViewModel.onExtraDimIntensityChanged(percentage)
        if (brightnessState.isExtraDimEnabled) {
            overlayControlCoordinator.updateExtraDim(true, brightnessState.extraDimIntensity)
        }
        refreshBrightnessComposeUiState()
    }

    private fun refreshWellnessComposeUiState() {
        val wellnessState = wellnessSettingsViewModel.loadState()
        wellnessComposeUiState = WellnessComposeUiState(
            isBatteryOptimizationEnabled = wellnessState.isBatteryOptimizationEnabled,
            isEyeStrainReminderEnabled = wellnessState.isEyeStrainReminderEnabled,
            notificationStyle = wellnessState.notificationStyle
        )
    }

    private fun refreshOverlayVisibilityComposeUiState() {
        overlayVisibilityComposeUiState = OverlayVisibilityComposeUiState(
            hideOnLockScreen = preferencesManager.shouldHideOverlayOnLockScreen(),
            hideOnHomeScreen = preferencesManager.shouldHideOverlayOnHomeScreen()
        )
    }

    private fun handleHideOverlayOnLockScreenToggle(isEnabled: Boolean) {
        preferencesManager.setHideOverlayOnLockScreen(isEnabled)
        refreshOverlayVisibilityComposeUiState()
        overlayControlCoordinator.startOverlayService()
    }

    private fun handleHideOverlayOnHomeScreenToggle(isEnabled: Boolean) {
        preferencesManager.setHideOverlayOnHomeScreen(isEnabled)
        refreshOverlayVisibilityComposeUiState()
        overlayControlCoordinator.startOverlayService()
    }

    private fun refreshAppExemptionUiState() {
        appExemptionComposeUiState = appExemptionComposeUiState.copy(
            hasUsageStatsPermission = permissionCoordinator.hasUsageStatsPermission(this)
        )
    }

    private fun handleLocationOffsetChange(offsetMinutes: Int) {
        automationSettingsViewModel.onLocationOffsetChanged(offsetMinutes)
        updateCalculatedTimes()
        scheduleCoordinator.refreshSchedule(this)
        refreshAutomationComposeUiState()
    }

    private fun handleNotificationStyleChange(style: String) {
        wellnessSettingsViewModel.onNotificationStyleChanged(style)
        refreshWellnessComposeUiState()
    }

    private fun getLightSensitivityLabel(value: Float): String = when (value.toInt()) {
        0 -> getString(R.string.light_sensitivity_low)
        1 -> getString(R.string.light_sensitivity_medium)
        else -> getString(R.string.light_sensitivity_high)
    }
    
    private fun handleLocationSchedulingToggle(isEnabled: Boolean) {
        if (isEnabled && !permissionCoordinator.hasExactAlarmPermission(this)) {
            permissionCoordinator.requestExactAlarmPermission(this)
            return
        }

        val automationState = automationSettingsViewModel.onLocationSchedulingToggled(isEnabled)
        if (automationState.isLocationSchedulingEnabled) {
            if (locationManager.getCachedLocation() == null) requestLocationPermissionsIfNeeded()
            else updateCalculatedTimes()
        }
        scheduleCoordinator.refreshSchedule(this)
        refreshAutomationComposeUiState()
    }
    
    private fun requestLocationPermissionAndUpdate() = requestLocationPermissionsIfNeeded()
    
    private fun requestLocationAndUpdate() {
        isLocationLoading = true
        refreshAutomationComposeUiState()
        locationManager.requestLocation(
            onSuccess = { _, _ ->
                runOnUiThread {
                    isLocationLoading = false
                    updateCalculatedTimes()
                    scheduleCoordinator.refreshSchedule(this)
                    refreshAutomationComposeUiState()
                }
            },
            onError = { _ ->
                runOnUiThread {
                    isLocationLoading = false
                    refreshAutomationComposeUiState()
                }
            }
        )
    }
    
    private fun updateCalculatedTimes() {
        automationSettingsViewModel.loadState()
        refreshAutomationComposeUiState()
    }
    
    private fun loadAnalyticsData() {
        analyticsComposeUiState = analyticsComposeUiState.copy(isLoading = true, hasError = false)
        lifecycleScope.launch {
            try {
                val period = analyticsComposeUiState.selectedPeriod
                val periodStats = analyticsRepository.getPeriodStats(period)
                analyticsComposeUiState = analyticsComposeUiState.copy(
                    usageTime = periodStats.usageTime,
                    averageOpacityText = getString(R.string.opacity_percent, (periodStats.averageOpacity * 100).toInt()),
                    mostUsedPreset = periodStats.mostUsedPreset,
                    currentStreakText = getString(R.string.streak_days, periodStats.currentStreak),
                    totalEventsText = getString(R.string.total_events_count, periodStats.totalEvents),
                    isLoading = false
                )
            } catch (e: Exception) {
                analyticsComposeUiState = analyticsComposeUiState.copy(isLoading = false, hasError = true)
            }
        }
    }

    private fun loadPeriodStats(period: AnalyticsPeriod) {
        analyticsComposeUiState = analyticsComposeUiState.copy(selectedPeriod = period)
        loadAnalyticsData()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            appExemptionComposeUiState = appExemptionComposeUiState.copy(isLoading = true)
            val apps = exemptedAppsManager.getInstalledApps()
            appExemptionComposeUiState = appExemptionComposeUiState.copy(isLoading = false, apps = apps)
        }
    }

    private fun searchApps(query: String) {
        lifecycleScope.launch {
            val results = exemptedAppsManager.searchApps(query)
            appExemptionComposeUiState = appExemptionComposeUiState.copy(apps = results)
        }
    }
}
