package com.redscreenfilter.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * LocationManager
 * Manages user location for sunrise/sunset calculations
 * Caches location and updates periodically (every 6 hours)
 */
class LocationManager private constructor(private val context: Context) {
    
    private val TAG = "LocationManager"
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    companion object {
        @Volatile
        private var instance: LocationManager? = null
        
        // Cache duration: 6 hours in milliseconds
        private const val CACHE_DURATION_MS = 6 * 60 * 60 * 1000L
        
        fun getInstance(context: Context): LocationManager {
            return instance ?: synchronized(this) {
                instance ?: LocationManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get cached location if available and fresh
     * @return Pair of (latitude, longitude) or null if not available/stale
     */
    fun getCachedLocation(): Pair<Double, Double>? {
        val lastUpdateTime = preferencesManager.getLocationLastUpdate()
        val currentTime = System.currentTimeMillis()
        
        // Check if cache is still valid
        if (currentTime - lastUpdateTime > CACHE_DURATION_MS) {
            Log.d(TAG, "getCachedLocation: Cache is stale")
            return null
        }
        
        val latitude = preferencesManager.getLocationLatitude()
        val longitude = preferencesManager.getLocationLongitude()
        
        // Check if we have valid coordinates
        if (latitude == 0.0 && longitude == 0.0) {
            Log.d(TAG, "getCachedLocation: No cached location")
            return null
        }
        
        Log.d(TAG, "getCachedLocation: Returning cached location: lat=$latitude, lon=$longitude")
        return Pair(latitude, longitude)
    }
    
    /**
     * Request current location from device
     * @param onSuccess Callback with latitude and longitude
     * @param onError Callback with error message
     */
    fun requestLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "requestLocation: No location permission")
            onError("Location permission not granted")
            return
        }
        
        Log.d(TAG, "requestLocation: Requesting current location")
        
        try {
            val cancellationTokenSource = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    
                    Log.d(TAG, "requestLocation: Got location: lat=$latitude, lon=$longitude")
                    
                    // Cache the location
                    cacheLocation(latitude, longitude)
                    
                    onSuccess(latitude, longitude)
                } else {
                    Log.w(TAG, "requestLocation: Location is null, trying last known location")
                    // Try to get last known location as fallback
                    getLastKnownLocation(onSuccess, onError)
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "requestLocation: Failed to get location", exception)
                // Try to get last known location as fallback
                getLastKnownLocation(onSuccess, onError)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "requestLocation: Security exception", e)
            onError("Location permission error")
        } catch (e: Exception) {
            Log.e(TAG, "requestLocation: Unexpected error", e)
            onError("Failed to get location: ${e.message}")
        }
    }
    
    /**
     * Get last known location as fallback
     */
    private fun getLastKnownLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError("Location permission not granted")
            return
        }
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    
                    Log.d(TAG, "getLastKnownLocation: Got last known location: lat=$latitude, lon=$longitude")
                    
                    // Cache the location
                    cacheLocation(latitude, longitude)
                    
                    onSuccess(latitude, longitude)
                } else {
                    Log.w(TAG, "getLastKnownLocation: No last known location available")
                    onError("Unable to determine location. Please ensure location services are enabled.")
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "getLastKnownLocation: Failed", exception)
                onError("Failed to get location: ${exception.message}")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "getLastKnownLocation: Security exception", e)
            onError("Location permission error")
        }
    }
    
    /**
     * Cache location coordinates and timestamp
     */
    private fun cacheLocation(latitude: Double, longitude: Double) {
        preferencesManager.setLocationLatitude(latitude)
        preferencesManager.setLocationLongitude(longitude)
        preferencesManager.setLocationLastUpdate(System.currentTimeMillis())
        Log.d(TAG, "cacheLocation: Cached location: lat=$latitude, lon=$longitude")
    }
    
    /**
     * Clear cached location
     */
    fun clearCache() {
        preferencesManager.setLocationLatitude(0.0)
        preferencesManager.setLocationLongitude(0.0)
        preferencesManager.setLocationLastUpdate(0L)
        Log.d(TAG, "clearCache: Location cache cleared")
    }
}
