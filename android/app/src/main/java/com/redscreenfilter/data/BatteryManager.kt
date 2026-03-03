package com.redscreenfilter.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

/**
 * Battery Monitor
 * Monitors device battery status and notifies listeners of battery state changes.
 * Detects low battery (< 20%) and critical battery (< 10%).
 */
class BatteryMonitor(private val context: Context) {
    
    private val TAG = "BatteryMonitor"
    private val preferencesManager = PreferencesManager.getInstance(context)
    
    private var currentBatteryLevel: Int = 0
    private var isCharging: Boolean = false
    private var isLowBattery: Boolean = false
    private var isCriticalBattery: Boolean = false
    
    private var batteryReceiver: BatteryReceiver? = null
    private var listeners: MutableList<BatteryStateListener> = mutableListOf()
    
    companion object {
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val CRITICAL_BATTERY_THRESHOLD = 10
        
        @Volatile
        private var instance: BatteryMonitor? = null
        
        fun getInstance(context: Context): BatteryMonitor {
            return instance ?: synchronized(this) {
                instance ?: BatteryMonitor(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    init {
        Log.d(TAG, "BatteryManager initialized")
        getInitialBatteryStatus()
    }
    
    /**
     * Get initial battery status from system
     */
    private fun getInitialBatteryStatus() {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        
        if (batteryStatus != null) {
            currentBatteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (scale > 0) {
                (currentBatteryLevel * 100) / scale
            } else {
                currentBatteryLevel
            }
            
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                         status == BatteryManager.BATTERY_STATUS_FULL
            
            updateBatteryThresholds(batteryPct)
            Log.d(TAG, "Initial battery level: $batteryPct%, Charging: $isCharging")
        }
    }
    
    /**
     * Start monitoring battery changes
     */
    fun startMonitoring() {
        Log.d(TAG, "startMonitoring: Starting battery monitoring")
        if (batteryReceiver == null) {
            batteryReceiver = BatteryReceiver()
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            try {
                context.registerReceiver(batteryReceiver, intentFilter)
                Log.d(TAG, "startMonitoring: Battery receiver registered")
            } catch (e: Exception) {
                Log.e(TAG, "startMonitoring: Failed to register battery receiver", e)
            }
        }
    }
    
    /**
     * Stop monitoring battery changes
     */
    fun stopMonitoring() {
        Log.d(TAG, "stopMonitoring: Stopping battery monitoring")
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
                batteryReceiver = null
                Log.d(TAG, "stopMonitoring: Battery receiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "stopMonitoring: Failed to unregister battery receiver", e)
            }
        }
    }
    
    /**
     * Add listener for battery state changes
     */
    fun addListener(listener: BatteryStateListener) {
        listeners.add(listener)
        Log.d(TAG, "addListener: Listener added, total listeners: ${listeners.size}")
    }
    
    /**
     * Remove listener
     */
    fun removeListener(listener: BatteryStateListener) {
        listeners.remove(listener)
        Log.d(TAG, "removeListener: Listener removed, total listeners: ${listeners.size}")
    }
    
    /**
     * Get current battery level (0-100)
     */
    fun getBatteryLevel(): Int {
        return currentBatteryLevel
    }
    
    /**
     * Check if device is charging
     */
    fun isCharging(): Boolean {
        return isCharging
    }
    
    /**
     * Check if battery is low (< 20%)
     */
    fun isLowBattery(): Boolean {
        return isLowBattery
    }
    
    /**
     * Check if battery is critical (< 10%)
     */
    fun isCriticalBattery(): Boolean {
        return isCriticalBattery
    }
    
    /**
     * Update battery thresholds
     */
    private fun updateBatteryThresholds(batteryLevel: Int) {
        val wasLow = isLowBattery
        val wasCritical = isCriticalBattery
        
        isLowBattery = batteryLevel < LOW_BATTERY_THRESHOLD
        isCriticalBattery = batteryLevel < CRITICAL_BATTERY_THRESHOLD
        currentBatteryLevel = batteryLevel
        
        Log.d(TAG, "updateBatteryThresholds: level=$batteryLevel%, low=$isLowBattery, critical=$isCriticalBattery")
        
        // Notify listeners if state changed
        if (wasLow != isLowBattery || wasCritical != isCriticalBattery) {
            notifyListeners()
        }
    }
    
    /**
     * Notify all listeners of battery state change
     */
    private fun notifyListeners() {
        Log.d(TAG, "notifyListeners: Notifying ${listeners.size} listeners")
        listeners.forEach { listener ->
            listener.onBatteryStateChanged(
                level = currentBatteryLevel,
                isLow = isLowBattery,
                isCritical = isCriticalBattery,
                isCharging = isCharging
            )
        }
    }
    
    /**
     * Inner class - Battery state receiver
     */
    private inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = if (scale > 0) {
                    (level * 100) / scale
                } else {
                    level
                }
                
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                             status == BatteryManager.BATTERY_STATUS_FULL
                
                Log.d(TAG, "BatteryReceiver.onReceive: Battery level: $batteryPct%, Charging: $isCharging")
                
                updateBatteryThresholds(batteryPct)
            }
        }
    }
    
    /**
     * Interface for battery state listeners
     */
    interface BatteryStateListener {
        fun onBatteryStateChanged(level: Int, isLow: Boolean, isCritical: Boolean, isCharging: Boolean)
    }
}
