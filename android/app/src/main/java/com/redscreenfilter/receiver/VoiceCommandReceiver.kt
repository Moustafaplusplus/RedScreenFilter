package com.redscreenfilter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.redscreenfilter.data.PreferencesManager
import com.redscreenfilter.service.RedOverlayService

/**
 * Voice Command Receiver for Google Assistant Integration
 * 
 * Handles voice commands from Google Assistant to control the overlay:
 * - "Turn on red screen filter"
 * - "Turn off red screen filter"
 * - "Set red filter to 50%" (or any value 0-100)
 * 
 * Works with Google Assistant voice commands through intent filters.
 * Available on Android 8.0+ (API 26+)
 */
class VoiceCommandReceiver : BroadcastReceiver() {
    
    private val TAG = "VoiceCommandReceiver"
    
    companion object {
        // Intent action for voice commands
        const val ACTION_VOICE_COMMAND = "android.intent.action.VOICE_COMMAND"
        
        // Intent extras
        const val EXTRA_COMMAND = "command"
        const val EXTRA_OPACITY = "opacity"
        
        // Command types
        const val COMMAND_TURN_ON = "turn_on"
        const val COMMAND_TURN_OFF = "turn_off"
        const val COMMAND_SET_OPACITY = "set_opacity"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Voice command received - action=${intent?.action}")
        
        if (context == null || intent == null) {
            Log.w(TAG, "onReceive: Context or intent is null")
            return
        }
        
        try {
            // Get PreferencesManager instance
            val preferencesManager = PreferencesManager.getInstance(context)
            
            // Parse the voice command from intent
            val command = parseVoiceCommand(intent)
            
            Log.d(TAG, "onReceive: Parsed command=$command")
            
            when (command.type) {
                COMMAND_TURN_ON -> {
                    Log.d(TAG, "onReceive: Executing TURN_ON command")
                    turnOnOverlay(context, preferencesManager)
                }
                COMMAND_TURN_OFF -> {
                    Log.d(TAG, "onReceive: Executing TURN_OFF command")
                    turnOffOverlay(context, preferencesManager)
                }
                COMMAND_SET_OPACITY -> {
                    Log.d(TAG, "onReceive: Executing SET_OPACITY command with value=${command.opacityValue}")
                    setOpacity(context, preferencesManager, command.opacityValue)
                }
                else -> {
                    Log.w(TAG, "onReceive: Unknown command type=${command.type}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onReceive: Error processing voice command", e)
        }
    }
    
    /**
     * Parse voice command from intent
     * Extracts command type and optional opacity value from various intent sources
     */
    private fun parseVoiceCommand(intent: Intent): VoiceCommand {
        // Check for explicit command extra
        val commandExtra = intent.getStringExtra(EXTRA_COMMAND)?.lowercase()
        if (!commandExtra.isNullOrEmpty()) {
            Log.d(TAG, "parseVoiceCommand: Found command extra=$commandExtra")
            
            return when {
                commandExtra.contains("on") || commandExtra.contains("enable") || commandExtra.contains("activate") -> {
                    VoiceCommand(COMMAND_TURN_ON)
                }
                commandExtra.contains("off") || commandExtra.contains("disable") || commandExtra.contains("deactivate") -> {
                    VoiceCommand(COMMAND_TURN_OFF)
                }
                commandExtra.contains("set") || commandExtra.contains("opacity") -> {
                    val opacity = intent.getIntExtra(EXTRA_OPACITY, 50).toFloat()
                    VoiceCommand(COMMAND_SET_OPACITY, opacity)
                }
                else -> VoiceCommand(COMMAND_TURN_ON) // Default to turn on
            }
        }
        
        // Check for voice command string from Google Assistant
        val voiceCommand = intent.getStringExtra("android.intent.extra.VOICE_COMMAND")?.lowercase()
        if (!voiceCommand.isNullOrEmpty()) {
            Log.d(TAG, "parseVoiceCommand: Found voice command extra=$voiceCommand")
            
            return when {
                voiceCommand.contains("turn on") || voiceCommand.contains("enable") || voiceCommand.contains("activate") -> {
                    VoiceCommand(COMMAND_TURN_ON)
                }
                voiceCommand.contains("turn off") || voiceCommand.contains("disable") || voiceCommand.contains("deactivate") -> {
                    VoiceCommand(COMMAND_TURN_OFF)
                }
                voiceCommand.contains("set") -> {
                    // Extract opacity value from voice command (e.g., "set red filter to 50")
                    val opacityValue = extractOpacityFromString(voiceCommand)
                    VoiceCommand(COMMAND_SET_OPACITY, opacityValue)
                }
                else -> VoiceCommand(COMMAND_TURN_ON)
            }
        }
        
        // Default command if nothing found
        Log.w(TAG, "parseVoiceCommand: No command found, defaulting to TURN_ON")
        return VoiceCommand(COMMAND_TURN_ON)
    }
    
    /**
     * Extract opacity value (0-100) from voice command string
     * Looks for patterns like "50", "70 percent", etc.
     */
    private fun extractOpacityFromString(text: String): Float {
        try {
            // Find all numbers in the text
            val numbers = Regex("\\d+").findAll(text)
            for (number in numbers) {
                val value = number.value.toIntOrNull() ?: continue
                // Clamp to 0-100 range
                if (value in 0..100) {
                    Log.d(TAG, "extractOpacityFromString: Extracted opacity=$value from '$text'")
                    return value.toFloat()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "extractOpacityFromString: Error extracting opacity", e)
        }
        // Default to 50% if extraction fails
        Log.w(TAG, "extractOpacityFromString: Could not extract opacity, defaulting to 50")
        return 50f
    }
    
    /**
     * Turn on the overlay via voice command
     */
    private fun turnOnOverlay(context: Context, preferencesManager: PreferencesManager) {
        try {
            Log.d(TAG, "turnOnOverlay: Starting overlay service")
            
            // Update preferences
            preferencesManager.setOverlayEnabled(true)
            
            // Start the overlay service
            val intent = Intent(context, RedOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
                Log.d(TAG, "turnOnOverlay: Started as foreground service (Android O+)")
            } else {
                context.startService(intent)
                Log.d(TAG, "turnOnOverlay: Started as regular service")
            }
            
            Log.d(TAG, "turnOnOverlay: Overlay enabled via voice command")
        } catch (e: Exception) {
            Log.e(TAG, "turnOnOverlay: Error starting overlay", e)
        }
    }
    
    /**
     * Turn off the overlay via voice command
     */
    private fun turnOffOverlay(context: Context, preferencesManager: PreferencesManager) {
        try {
            Log.d(TAG, "turnOffOverlay: Stopping overlay service")
            
            // Update preferences
            preferencesManager.setOverlayEnabled(false)
            
            // Stop the overlay service
            val intent = Intent(context, RedOverlayService::class.java)
            context.stopService(intent)
            
            Log.d(TAG, "turnOffOverlay: Overlay disabled via voice command")
        } catch (e: Exception) {
            Log.e(TAG, "turnOffOverlay: Error stopping overlay", e)
        }
    }
    
    /**
     * Set overlay opacity via voice command
     */
    private fun setOpacity(context: Context, preferencesManager: PreferencesManager, opacity: Float) {
        try {
            Log.d(TAG, "setOpacity: Setting opacity to $opacity")
            
            // Clamp opacity to 0-1 range (0-100%)
            val clampedOpacity = opacity.coerceIn(0f, 100f) / 100f
            
            // Update preferences
            preferencesManager.setOpacity(clampedOpacity)
            
            // Update running overlay service if it's active
            if (preferencesManager.isOverlayEnabled()) {
                val updateIntent = Intent(context, RedOverlayService::class.java).apply {
                    action = RedOverlayService.ACTION_UPDATE_OPACITY
                    putExtra(RedOverlayService.EXTRA_OPACITY, clampedOpacity)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(updateIntent)
                } else {
                    context.startService(updateIntent)
                }
                
                Log.d(TAG, "setOpacity: Sent opacity update to service")
            }
            
            Log.d(TAG, "setOpacity: Opacity set to $clampedOpacity via voice command")
        } catch (e: Exception) {
            Log.e(TAG, "setOpacity: Error setting opacity", e)
        }
    }
    
    /**
     * Data class to represent a parsed voice command
     */
    private data class VoiceCommand(
        val type: String,
        val opacityValue: Float = 50f
    )
}
