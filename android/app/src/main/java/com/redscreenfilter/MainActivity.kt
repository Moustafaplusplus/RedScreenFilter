package com.redscreenfilter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.service.RedOverlayService

/**
 * MainActivity - Main UI for Red Screen Filter
 * 
 * Features:
 * - Toggle overlay on/off
 * - Adjust opacity with SeekBar
 * - Request SYSTEM_ALERT_WINDOW permission
 * - Start/stop RedOverlayService
 */
class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    
    private lateinit var preferencesManager: PreferencesManager
    
    // UI Components
    private lateinit var switchOverlay: SwitchMaterial
    private lateinit var seekbarOpacity: SeekBar
    private lateinit var textOpacityValue: TextView
    private lateinit var cardPermission: MaterialCardView
    private lateinit var buttonRequestPermission: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "onCreate: Activity starting")
        
        // Initialize PreferencesManager
        preferencesManager = PreferencesManager.getInstance(this)
        
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
}
