package com.redscreenfilter.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exempted Apps Manager
 * 
 * Manages the list of applications that are exempt from the red screen overlay.
 * Loads installed apps with their icons and manages exemption state.
 */
class ExemptedAppsManager(private val context: Context) {
    
    private val TAG = "ExemptedAppsManager"
    private val packageManager = context.packageManager
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val foregroundAppDetector = ForegroundAppDetector.getInstance(context)
    
    companion object {
        @Volatile
        private var instance: ExemptedAppsManager? = null
        
        fun getInstance(context: Context): ExemptedAppsManager {
            return instance ?: synchronized(this) {
                instance ?: ExemptedAppsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * User apps that appear in the launcher (matches manifest `<queries>` MAIN/LAUNCHER).
     * Does not use QUERY_ALL_PACKAGES — Play-compliant package visibility only.
     */
    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getInstalledApps: Loading launcher-visible apps")
            val exemptedPackages = preferencesManager.getExemptedApps()
            val apps = mutableListOf<InstalledApp>()
            val seenPackages = mutableSetOf<String>()
            val currentPackageName = context.packageName

            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            }

            for (resolveInfo in resolveInfos) {
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val pkg = appInfo.packageName
                
                // Avoid duplicates and don't include this app itself
                if (!seenPackages.add(pkg) || pkg == currentPackageName) continue

                try {
                    val appName = appInfo.loadLabel(packageManager).toString()
                    val appIcon = appInfo.loadIcon(packageManager)
                    val isExempted = exemptedPackages.contains(pkg)

                    apps.add(
                        InstalledApp(
                            packageName = pkg,
                            appName = appName,
                            icon = appIcon,
                            isExempted = isExempted
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "getInstalledApps: Error loading app", e)
                }
            }

            apps.sort()
            Log.d(TAG, "getInstalledApps: Loaded ${apps.size} apps")
            apps
        } catch (e: Exception) {
            Log.e(TAG, "getInstalledApps: Error loading installed apps", e)
            emptyList()
        }
    }
    
    /**
     * Search installed apps by name or package name
     */
    suspend fun searchApps(query: String): List<InstalledApp> = withContext(Dispatchers.IO) {
        try {
            val lowerQuery = query.lowercase()
            val allApps = getInstalledApps()
            return@withContext allApps.filter { app ->
                app.appName.lowercase().contains(lowerQuery) ||
                app.packageName.lowercase().contains(lowerQuery)
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchApps: Error searching apps", e)
            emptyList()
        }
    }
    
    /**
     * Toggle exemption status for an app
     */
    fun toggleAppExemption(packageName: String, exempt: Boolean) {
        try {
            if (exempt) {
                preferencesManager.addExemptedApp(packageName)
                Log.d(TAG, "toggleAppExemption: Added $packageName to exemptions")
            } else {
                preferencesManager.removeExemptedApp(packageName)
                Log.d(TAG, "toggleAppExemption: Removed $packageName from exemptions")
            }
        } catch (e: Exception) {
            Log.e(TAG, "toggleAppExemption: Error toggling exemption", e)
        }
    }
    
    /**
     * Check if an app is currently exempt
     */
    fun isAppExempt(packageName: String): Boolean {
        return preferencesManager.isAppExempted(packageName)
    }
    
    /**
     * Check if current foreground app is exempt
     */
    fun isForegroundAppExempt(): Boolean {
        return try {
            val foregroundApp = foregroundAppDetector.getForegroundAppPackage()
            if (foregroundApp != null) {
                preferencesManager.isAppExempted(foregroundApp)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "isForegroundAppExempt: Error checking foreground app", e)
            false
        }
    }
    
    /**
     * Get current foreground app package name
     */
    fun getForegroundAppPackage(): String? {
        return foregroundAppDetector.getForegroundAppPackage()
    }
    
    /**
     * Clear all exemptions
     */
    fun clearAllExemptions() {
        try {
            preferencesManager.setExemptedApps(emptySet())
            Log.d(TAG, "clearAllExemptions: All exemptions cleared")
        } catch (e: Exception) {
            Log.e(TAG, "clearAllExemptions: Error clearing exemptions", e)
        }
    }
}
