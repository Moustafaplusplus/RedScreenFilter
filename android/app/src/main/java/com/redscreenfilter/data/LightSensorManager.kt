package com.redscreenfilter.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

/**
 * Light Sensor Manager
 * Monitors ambient light levels and provides automatic opacity adjustments.
 * Uses a moving average algorithm to smooth out sensor jitter.
 */
class LightSensorManager(private val context: Context) : SensorEventListener {
    
    private val TAG = "LightSensorManager"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    private var currentLux: Float = 0f
    private var listeners: MutableList<LightSensorListener> = mutableListOf()
    private var isMonitoring = false
    
    // Moving average for smoothing
    private val smoothingWindowSize = 5
    private val luxReadings = mutableListOf<Float>()
    
    enum class LightSensitivity {
        LOW, MEDIUM, HIGH
    }
    
    companion object {
        @Volatile
        private var instance: LightSensorManager? = null
        
        fun getInstance(context: Context): LightSensorManager {
            return instance ?: synchronized(this) {
                instance ?: LightSensorManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    init {
        if (lightSensor == null) {
            Log.w(TAG, "Light sensor not available on this device")
        } else {
            Log.d(TAG, "Light sensor available: ${lightSensor.name}")
        }
    }
    
    /**
     * Start monitoring light sensor
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "startMonitoring: Already monitoring")
            return
        }
        
        if (lightSensor == null) {
            Log.w(TAG, "startMonitoring: Light sensor not available")
            return
        }
        
        Log.d(TAG, "startMonitoring: Starting light sensor monitoring")
        // Use SENSOR_DELAY_NORMAL (200,000 microseconds = 5 Hz)
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        isMonitoring = true
    }
    
    /**
     * Stop monitoring light sensor
     */
    fun stopMonitoring() {
        if (!isMonitoring) {
            Log.d(TAG, "stopMonitoring: Not currently monitoring")
            return
        }
        
        Log.d(TAG, "stopMonitoring: Stopping light sensor monitoring")
        sensorManager.unregisterListener(this)
        isMonitoring = false
        luxReadings.clear()
    }
    
    /**
     * Add listener for light sensor changes
     */
    fun addListener(listener: LightSensorListener) {
        listeners.add(listener)
        Log.d(TAG, "addListener: Listener added, total listeners: ${listeners.size}")
    }
    
    /**
     * Remove listener
     */
    fun removeListener(listener: LightSensorListener) {
        listeners.remove(listener)
        Log.d(TAG, "removeListener: Listener removed, total listeners: ${listeners.size}")
    }
    
    /**
     * Get current lux reading
     */
    fun getCurrentLux(): Float {
        return currentLux
    }
    
    /**
     * Map lux to opacity value based on sensitivity
     */
    fun mapLuxToOpacity(lux: Float, sensitivity: LightSensitivity): Float {
        return when (sensitivity) {
            LightSensitivity.LOW -> mapLuxToOpacityLow(lux)
            LightSensitivity.MEDIUM -> mapLuxToOpacityMedium(lux)
            LightSensitivity.HIGH -> mapLuxToOpacityHigh(lux)
        }
    }
    
    /**
     * Low sensitivity mapping (largest changes)
     * Minimal adjustment to opacity
     */
    private fun mapLuxToOpacityLow(lux: Float): Float {
        return when {
            lux in 0f..100f -> 0.9f      // Very dark: high opacity
            lux in 101f..1000f -> 0.7f  // Dim: medium-high opacity
            lux in 1001f..5000f -> 0.5f // Normal: medium opacity
            else -> 0.3f                 // Bright: low opacity
        }
    }
    
    /**
     * Medium sensitivity mapping (as specified in requirements)
     */
    private fun mapLuxToOpacityMedium(lux: Float): Float {
        return when {
            lux in 0f..50f -> 0.8f      // Dark: high opacity
            lux in 51f..500f -> 0.6f    // Dim: medium opacity
            lux in 501f..3000f -> 0.4f  // Normal: lower opacity
            else -> 0.2f                 // Bright: minimal opacity
        }
    }
    
    /**
     * High sensitivity mapping (most aggressive changes)
     */
    private fun mapLuxToOpacityHigh(lux: Float): Float {
        return when {
            lux in 0f..30f -> 1.0f       // Very dark: maximum opacity
            lux in 31f..100f -> 0.85f    // Dark: very high opacity
            lux in 101f..300f -> 0.65f   // Dim: high opacity
            lux in 301f..1000f -> 0.45f  // Normal-dim: medium opacity
            lux in 1001f..3000f -> 0.25f // Normal-bright: low opacity
            else -> 0.1f                  // Very bright: minimal opacity
        }
    }
    
    /**
     * Apply smoothing using moving average
     */
    private fun smoothLux(newReading: Float): Float {
        luxReadings.add(newReading)
        
        // Keep only the last N readings
        if (luxReadings.size > smoothingWindowSize) {
            luxReadings.removeAt(0)
        }
        
        // Calculate moving average
        val average = luxReadings.average().toFloat()
        Log.d(TAG, "smoothLux: raw=$newReading, smoothed=$average, windowSize=${luxReadings.size}")
        return average
    }
    
    /**
     * Notify all listeners of light level change
     */
    private fun notifyListeners(lux: Float, opacity: Float) {
        listeners.forEach { listener ->
            listener.onLightLevelChanged(lux = lux, opacity = opacity)
        }
    }
    
    // SensorEventListener implementation
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        val rawLux = event.values[0]
        
        // Apply smoothing
        val smoothedLux = smoothLux(rawLux)
        
        // Check if change is significant (avoid excessive updates from jitter)
        val luxChange = abs(smoothedLux - currentLux)
        if (luxChange < 10f && currentLux != 0f) {
            // Change too small, ignore
            return
        }
        
        currentLux = smoothedLux
        Log.d(TAG, "onSensorChanged: lux=$currentLux")
        
        // Calculate opacity based on sensitivity and notify listeners
        val prefsManager = PreferencesManager.getInstance(context)
        val sensitivityStr = prefsManager.getLightSensorSensitivity()
        val sensitivity = when (sensitivityStr.lowercase()) {
            "low" -> LightSensitivity.LOW
            "high" -> LightSensitivity.HIGH
            else -> LightSensitivity.MEDIUM
        }
        val opacity = mapLuxToOpacity(currentLux, sensitivity)
        notifyListeners(currentLux, opacity)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged: sensor=${sensor?.name}, accuracy=$accuracy")
    }
    
    /**
     * Interface for light sensor listeners
     */
    interface LightSensorListener {
        fun onLightLevelChanged(lux: Float, opacity: Float)
    }
}
