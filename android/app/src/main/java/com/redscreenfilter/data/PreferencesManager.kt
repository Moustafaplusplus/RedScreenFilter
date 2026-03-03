package com.redscreenfilter.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Preferences Manager
 * Handles all SharedPreferences operations with encryption
 * Wraps secure data storage for overlay settings using EncryptedSharedPreferences
 */
class PreferencesManager private constructor(context: Context) {
    
    private val TAG = "PreferencesManager"
    private val sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "red_screen_filter_prefs"
        
        // Keys
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_OPACITY = "opacity"
        private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
        private const val KEY_SCHEDULE_START = "schedule_start"
        private const val KEY_SCHEDULE_END = "schedule_end"
        private const val KEY_USE_AMBIENT_LIGHT = "use_ambient_light"
        private const val KEY_USE_LOCATION_SCHEDULE = "use_location_schedule"
        private const val KEY_COLOR_VARIANT = "color_variant"
        private const val KEY_BATTERY_OPTIMIZATION = "battery_optimization"
        
        @Volatile
        private var instance: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    init {
        Log.d(TAG, "init: Creating MasterKey")
        // Create MasterKey for encryption
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        Log.d(TAG, "init: Initializing EncryptedSharedPreferences")
        // Initialize EncryptedSharedPreferences
        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "init: Failed to create EncryptedSharedPreferences, using regular SharedPreferences", e)
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        Log.d(TAG, "init: PreferencesManager ready")
    }
    
    // ========== Overlay Settings ==========
    
    fun setOverlayEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }

    fun isOverlayEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    fun setOpacity(opacity: Float) {
        val clampedOpacity = opacity.coerceIn(0.0f, 1.0f)
        sharedPreferences.edit().putFloat(KEY_OPACITY, clampedOpacity).apply()
    }

    fun getOpacity(): Float {
        return sharedPreferences.getFloat(KEY_OPACITY, 0.5f)
    }
    
    // ========== Scheduling Settings ==========
    
    fun setScheduleEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SCHEDULE_ENABLED, enabled).apply()
    }
    
    fun isScheduleEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SCHEDULE_ENABLED, false)
    }
    
    fun setScheduleStartTime(time: String) {
        sharedPreferences.edit().putString(KEY_SCHEDULE_START, time).apply()
    }
    
    fun getScheduleStartTime(): String {
        return sharedPreferences.getString(KEY_SCHEDULE_START, "21:00") ?: "21:00"
    }
    
    fun setScheduleEndTime(time: String) {
        sharedPreferences.edit().putString(KEY_SCHEDULE_END, time).apply()
    }
    
    fun getScheduleEndTime(): String {
        return sharedPreferences.getString(KEY_SCHEDULE_END, "07:00") ?: "07:00"
    }
    
    // ========== Smart Features ==========
    
    fun setUseAmbientLight(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_USE_AMBIENT_LIGHT, enabled).apply()
    }
    
    fun getUseAmbientLight(): Boolean {
        return sharedPreferences.getBoolean(KEY_USE_AMBIENT_LIGHT, false)
    }
    
    fun setUseLocationSchedule(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_USE_LOCATION_SCHEDULE, enabled).apply()
    }
    
    fun getUseLocationSchedule(): Boolean {
        return sharedPreferences.getBoolean(KEY_USE_LOCATION_SCHEDULE, false)
    }
    
    fun setColorVariant(variant: String) {
        sharedPreferences.edit().putString(KEY_COLOR_VARIANT, variant).apply()
    }
    
    fun getColorVariant(): String {
        return sharedPreferences.getString(KEY_COLOR_VARIANT, "red_standard") ?: "red_standard"
    }
    
    fun setBatteryOptimizationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BATTERY_OPTIMIZATION, enabled).apply()
    }
    
    fun getBatteryOptimizationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BATTERY_OPTIMIZATION, true)
    }
    
    // ========== Complete Settings Object ==========
    
    /**
     * Get all settings as a single OverlaySettings object
     */
    fun getSettings(): OverlaySettings {
        return OverlaySettings(
            isEnabled = isOverlayEnabled(),
            opacity = getOpacity(),
            scheduleEnabled = isScheduleEnabled(),
            scheduleStartTime = getScheduleStartTime(),
            scheduleEndTime = getScheduleEndTime(),
            useAmbientLight = getUseAmbientLight(),
            useLocationSchedule = getUseLocationSchedule(),
            colorVariant = getColorVariant()
        )
    }
    
    /**
     * Save complete settings object
     */
    fun saveSettings(settings: OverlaySettings) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_OVERLAY_ENABLED, settings.isEnabled)
            putFloat(KEY_OPACITY, settings.opacity)
            putBoolean(KEY_SCHEDULE_ENABLED, settings.scheduleEnabled)
            putString(KEY_SCHEDULE_START, settings.scheduleStartTime)
            putString(KEY_SCHEDULE_END, settings.scheduleEndTime)
            putBoolean(KEY_USE_AMBIENT_LIGHT, settings.useAmbientLight)
            putBoolean(KEY_USE_LOCATION_SCHEDULE, settings.useLocationSchedule)
            putString(KEY_COLOR_VARIANT, settings.colorVariant)
            apply()
        }
    }
    
    /**
     * Clear all preferences
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
