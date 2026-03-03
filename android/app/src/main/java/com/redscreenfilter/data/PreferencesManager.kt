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
        private const val KEY_ORIGINAL_OPACITY_PRE_BATTERY = "original_opacity_pre_battery"
        private const val KEY_IS_BATTERY_REDUCED = "is_battery_reduced"
        
        // Light sensor keys
        private const val KEY_LIGHT_SENSOR_ENABLED = "light_sensor_enabled"
        private const val KEY_LIGHT_SENSOR_SENSITIVITY = "light_sensor_sensitivity"
        private const val KEY_LIGHT_SENSOR_LOCKED = "light_sensor_locked"
        private const val KEY_ORIGINAL_OPACITY_PRE_LIGHT = "original_opacity_pre_light"
        
        // Eye strain reminder keys
        private const val KEY_EYE_STRAIN_REMINDER_ENABLED = "eye_strain_reminder_enabled"
        private const val KEY_EYE_STRAIN_NOTIFICATION_STYLE = "eye_strain_notification_style"
        
        // Location keys
        private const val KEY_LOCATION_LATITUDE = "location_latitude"
        private const val KEY_LOCATION_LONGITUDE = "location_longitude"
        private const val KEY_LOCATION_LAST_UPDATE = "location_last_update"
        private const val KEY_LOCATION_OFFSET_MINUTES = "location_offset_minutes"
        
        // Exempted apps keys
        private const val KEY_EXEMPTED_APPS = "exempted_apps"
        
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
    
    fun setOriginalOpacityPreBattery(opacity: Float) {
        sharedPreferences.edit().putFloat(KEY_ORIGINAL_OPACITY_PRE_BATTERY, opacity).apply()
    }
    
    fun getOriginalOpacityPreBattery(): Float {
        return sharedPreferences.getFloat(KEY_ORIGINAL_OPACITY_PRE_BATTERY, 0.5f)
    }
    
    fun setIsBatteryReduced(reduced: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_BATTERY_REDUCED, reduced).apply()
    }
    
    fun isBatteryReduced(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BATTERY_REDUCED, false)
    }
    
    // ========== Light Sensor Settings ==========
    
    fun setLightSensorEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LIGHT_SENSOR_ENABLED, enabled).apply()
    }
    
    fun isLightSensorEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_LIGHT_SENSOR_ENABLED, false)
    }
    
    fun setLightSensorSensitivity(sensitivity: String) {
        sharedPreferences.edit().putString(KEY_LIGHT_SENSOR_SENSITIVITY, sensitivity).apply()
    }
    
    fun getLightSensorSensitivity(): String {
        return sharedPreferences.getString(KEY_LIGHT_SENSOR_SENSITIVITY, "medium") ?: "medium"
    }
    
    fun setLightSensorLocked(locked: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LIGHT_SENSOR_LOCKED, locked).apply()
    }
    
    fun isLightSensorLocked(): Boolean {
        return sharedPreferences.getBoolean(KEY_LIGHT_SENSOR_LOCKED, false)
    }
    
    fun setOriginalOpacityPreLight(opacity: Float) {
        sharedPreferences.edit().putFloat(KEY_ORIGINAL_OPACITY_PRE_LIGHT, opacity).apply()
    }
    
    fun getOriginalOpacityPreLight(): Float {
        return sharedPreferences.getFloat(KEY_ORIGINAL_OPACITY_PRE_LIGHT, 0.5f)
    }
    
    // ========== Eye Strain Reminder Settings ==========
    
    fun setEyeStrainReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_EYE_STRAIN_REMINDER_ENABLED, enabled).apply()
    }
    
    fun isEyeStrainReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_EYE_STRAIN_REMINDER_ENABLED, false)
    }
    
    fun setEyeStrainNotificationStyle(style: String) {
        // Valid values: "sound", "vibration", "silent"
        sharedPreferences.edit().putString(KEY_EYE_STRAIN_NOTIFICATION_STYLE, style).apply()
    }
    
    fun getEyeStrainNotificationStyle(): String {
        return sharedPreferences.getString(KEY_EYE_STRAIN_NOTIFICATION_STYLE, "sound") ?: "sound"
    }
    
    // ========== Location Settings ==========
    
    fun setLocationLatitude(latitude: Double) {
        sharedPreferences.edit().putFloat(KEY_LOCATION_LATITUDE, latitude.toFloat()).apply()
    }
    
    fun getLocationLatitude(): Double {
        return sharedPreferences.getFloat(KEY_LOCATION_LATITUDE, 0f).toDouble()
    }
    
    fun setLocationLongitude(longitude: Double) {
        sharedPreferences.edit().putFloat(KEY_LOCATION_LONGITUDE, longitude.toFloat()).apply()
    }
    
    fun getLocationLongitude(): Double {
        return sharedPreferences.getFloat(KEY_LOCATION_LONGITUDE, 0f).toDouble()
    }
    
    fun setLocationLastUpdate(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LOCATION_LAST_UPDATE, timestamp).apply()
    }
    
    fun getLocationLastUpdate(): Long {
        return sharedPreferences.getLong(KEY_LOCATION_LAST_UPDATE, 0L)
    }
    
    fun setLocationOffsetMinutes(offsetMinutes: Int) {
        sharedPreferences.edit().putInt(KEY_LOCATION_OFFSET_MINUTES, offsetMinutes).apply()
    }
    
    fun getLocationOffsetMinutes(): Int {
        return sharedPreferences.getInt(KEY_LOCATION_OFFSET_MINUTES, 0)
    }
    
    // ========== App Exemptions ==========
    
    fun getExemptedApps(): Set<String> {
        val exemptedJson = sharedPreferences.getString(KEY_EXEMPTED_APPS, "")
        return if (exemptedJson.isNullOrEmpty()) {
            emptySet()
        } else {
            try {
                val gson = com.google.gson.Gson()
                gson.fromJson(exemptedJson, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                Log.e(TAG, "getExemptedApps: Error deserializing exempted apps", e)
                emptySet()
            }
        }
    }
    
    fun setExemptedApps(packageNames: Set<String>) {
        try {
            val gson = com.google.gson.Gson()
            val exemptedJson = gson.toJson(packageNames.toTypedArray())
            sharedPreferences.edit().putString(KEY_EXEMPTED_APPS, exemptedJson).apply()
        } catch (e: Exception) {
            Log.e(TAG, "setExemptedApps: Error serializing exempted apps", e)
        }
    }
    
    fun isAppExempted(packageName: String): Boolean {
        return getExemptedApps().contains(packageName)
    }
    
    fun addExemptedApp(packageName: String) {
        val currentExempted = getExemptedApps().toMutableSet()
        currentExempted.add(packageName)
        setExemptedApps(currentExempted)
    }
    
    fun removeExemptedApp(packageName: String) {
        val currentExempted = getExemptedApps().toMutableSet()
        currentExempted.remove(packageName)
        setExemptedApps(currentExempted)
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
