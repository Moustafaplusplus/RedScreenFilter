package com.redscreenfilter.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.data.ExemptedAppsManager
import com.redscreenfilter.data.ForegroundAppDetector
import com.redscreenfilter.feature.app_exemption.ui.AppExemptionComposeScreen
import com.redscreenfilter.feature.app_exemption.ui.AppExemptionComposeUiState
import kotlinx.coroutines.launch

/**
 * App Exemption Fragment
 * 
 * Displays list of installed apps with checkboxes to toggle exemption from overlay.
 * Includes search functionality to filter apps.
 */
class AppExemptionFragment : Fragment() {

    private lateinit var exemptedAppsManager: ExemptedAppsManager
    private lateinit var foregroundAppDetector: ForegroundAppDetector
    private var composeUiState by mutableStateOf(
        AppExemptionComposeUiState(
            query = "",
            isLoading = false,
            apps = emptyList(),
            hasUsageStatsPermission = true
        )
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RedScreenFilterTheme {
                    AppExemptionComposeScreen(
                        uiState = composeUiState,
                        onQueryChanged = { query ->
                            composeUiState = composeUiState.copy(query = query)
                            if (query.isBlank()) {
                                loadApps()
                            } else {
                                searchApps(query)
                            }
                        },
                        onExemptionChanged = { packageName, isExempt ->
                            exemptedAppsManager.toggleAppExemption(packageName, isExempt)
                            val updated = composeUiState.apps.map {
                                if (it.packageName == packageName) it.copy(isExempted = isExempt) else it
                            }
                            composeUiState = composeUiState.copy(apps = updated)
                        },
                        onRequestPermission = {
                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    )
                }
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        exemptedAppsManager = ExemptedAppsManager.getInstance(requireContext())
        foregroundAppDetector = ForegroundAppDetector.getInstance(requireContext())
        checkPermissionAndLoadApps()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionAndLoadApps()
    }

    private fun checkPermissionAndLoadApps() {
        val hasPermission = foregroundAppDetector.hasUsageStatsPermission()
        composeUiState = composeUiState.copy(hasUsageStatsPermission = hasPermission)
        loadApps()
    }
    
    /**
     * Load all installed apps
     */
    private fun loadApps() {
        viewLifecycleOwner.lifecycleScope.launch {
            composeUiState = composeUiState.copy(isLoading = true)
            val apps = exemptedAppsManager.getInstalledApps()
            composeUiState = composeUiState.copy(isLoading = false, apps = apps)
        }
    }
    
    /**
     * Search apps by name or package
     */
    private fun searchApps(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val results = exemptedAppsManager.searchApps(query)
            composeUiState = composeUiState.copy(apps = results)
        }
    }
}
