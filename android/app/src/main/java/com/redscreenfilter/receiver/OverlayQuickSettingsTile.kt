package com.redscreenfilter.receiver

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.redscreenfilter.R
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.service.RedOverlayService

/**
 * Quick Settings Tile for Red Screen Filter
 * 
 * Allows users to toggle the overlay on/off directly from Quick Settings panel.
 * - Tap: Toggle overlay state
 * - Long-click: Open app settings (handled via QS_TILE_PREFERENCES in Manifest)
 * 
 * Available on Android 7.0+ (API 24+)
 */
@RequiresApi(Build.VERSION_CODES.N)
class OverlayQuickSettingsTile : TileService() {
    
    private val TAG = "OverlayQuickSettingsTile"
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Initializing QuickSettingsTile")
        preferencesManager = PreferencesManager.getInstance(this)
    }
    
    /**
     * Called when the tile enters the listening state.
     * Update the tile UI state to reflect current overlay status.
     */
    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening: Updating tile state")
        updateTileState()
    }
    
    /**
     * Called when the user clicks on the tile.
     * Toggle the overlay state.
     */
    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick: Tile clicked")
        
        try {
            val isCurrentlyEnabled = preferencesManager.isOverlayEnabled()
            val newState = !isCurrentlyEnabled
            
            Log.d(TAG, "onClick: Current state=$isCurrentlyEnabled, New state=$newState")
            
            if (newState) {
                // Enable overlay
                startOverlay()
            } else {
                // Disable overlay
                stopOverlay()
            }
            
            // Save preference
            preferencesManager.setOverlayEnabled(newState)
            
            // Update tile UI
            updateTileState()
            
            Log.d(TAG, "onClick: Overlay toggled to $newState")
        } catch (e: Exception) {
            Log.e(TAG, "onClick: Error toggling overlay", e)
        }
    }
    
    /**
     * Update the tile UI state based on overlay enabled/disabled status.
     * Shows different icon, label, and state for Active/Inactive.
     */
    private fun updateTileState() {
        try {
            val isEnabled = preferencesManager.isOverlayEnabled()
            val tile = qsTile ?: run {
                Log.w(TAG, "updateTileState: qsTile is null")
                return
            }
            
            if (isEnabled) {
                // Overlay is active
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Red Filter"
                
                // Set icon based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_filter_active)
                } else {
                    // Fallback for older versions
                    tile.icon = Icon.createWithResource(this, android.R.drawable.ic_dialog_info)
                }
                
                Log.d(TAG, "updateTileState: Tile set to ACTIVE state")
            } else {
                // Overlay is inactive
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Red Filter"
                
                // Set icon based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_filter_inactive)
                } else {
                    // Fallback for older versions
                    tile.icon = Icon.createWithResource(this, android.R.drawable.ic_dialog_info)
                }
                
                Log.d(TAG, "updateTileState: Tile set to INACTIVE state")
            }
            
            // Update the tile display
            tile.updateTile()
        } catch (e: Exception) {
            Log.e(TAG, "updateTileState: Error updating tile state", e)
        }
    }
    
    /**
     * Start the overlay service.
     * Initiates the red overlay display with current settings.
     */
    private fun startOverlay() {
        try {
            Log.d(TAG, "startOverlay: Starting overlay service")
            val intent = Intent(this, RedOverlayService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
                Log.d(TAG, "startOverlay: Started as foreground service (Android O+)")
            } else {
                startService(intent)
                Log.d(TAG, "startOverlay: Started as regular service")
            }
        } catch (e: Exception) {
            Log.e(TAG, "startOverlay: Error starting overlay service", e)
        }
    }
    
    /**
     * Stop the overlay service.
     * Removes the red overlay from the screen.
     */
    private fun stopOverlay() {
        try {
            Log.d(TAG, "stopOverlay: Stopping overlay service")
            val intent = Intent(this, RedOverlayService::class.java)
            stopService(intent)
            Log.d(TAG, "stopOverlay: Service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "stopOverlay: Error stopping overlay service", e)
        }
    }
}
