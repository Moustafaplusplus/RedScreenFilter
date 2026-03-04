package com.redscreenfilter.ui

import android.os.Bundle
import android.util.Log
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
import com.redscreenfilter.R
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.data.repository.AnalyticsRepository
import com.redscreenfilter.feature.analytics.ui.AnalyticsComposeScreen
import com.redscreenfilter.feature.analytics.ui.AnalyticsComposeUiState
import kotlinx.coroutines.launch

/**
 * AnalyticsFragment - Dashboard showing usage statistics
 * 
 * Features:
 * - Display today/week/month usage time
 * - Show average opacity and most used preset
 * - Display current usage streak
 * - Tabbed interface for different time periods
 */
class AnalyticsFragment : Fragment() {
    
    private val TAG = "AnalyticsFragment"
    private lateinit var analyticsRepository: AnalyticsRepository

    private var selectedPeriod = AnalyticsRepository.AnalyticsPeriod.TODAY
    private var composeUiState by mutableStateOf(
        AnalyticsComposeUiState(
            selectedPeriod = AnalyticsRepository.AnalyticsPeriod.TODAY,
            subtitle = "",
            usageTime = "00:00:00",
            usageLabel = "",
            usageProgress = 0,
            averageOpacityText = "0%",
            mostUsedPreset = "N/A",
            currentStreakText = "0",
            totalEventsText = "0",
            isLoading = false,
            hasError = false
        )
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RedScreenFilterTheme {
                    AnalyticsComposeScreen(
                        uiState = composeUiState,
                        onPeriodSelected = { period ->
                            selectedPeriod = period
                            loadPeriodStats(period)
                        }
                    )
                }
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "onViewCreated: Initializing Analytics Fragment")
        
        // Initialize repository
        analyticsRepository = AnalyticsRepository.getInstance(requireContext())
        loadAnalyticsData()
    }
    
    private fun loadAnalyticsData() {
        composeUiState = composeUiState.copy(isLoading = true, hasError = false)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val usageStats = analyticsRepository.getUsageStats()
                
                Log.d(TAG, "Analytics data loaded: $usageStats")
                
                // Update UI with fetched data
                updateUI(usageStats)
                loadPeriodStats(selectedPeriod)
                composeUiState = composeUiState.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "loadAnalyticsData: Error loading data", e)
                composeUiState = composeUiState.copy(isLoading = false, hasError = true)
                showErrorState()
            }
        }
    }
    
    private fun updateUI(usageStats: AnalyticsRepository.UsageStats) {
        val usageTime = when (selectedPeriod) {
            AnalyticsRepository.AnalyticsPeriod.TODAY -> usageStats.todayUsageTime
            AnalyticsRepository.AnalyticsPeriod.WEEK -> usageStats.weekUsageTime
            else -> usageStats.monthUsageTime
        }
        composeUiState = composeUiState.copy(
            usageTime = usageTime,
            usageProgress = when (selectedPeriod) {
                AnalyticsRepository.AnalyticsPeriod.TODAY -> (timeStringToMinutes(usageStats.todayUsageTime) / 480 * 100)
                AnalyticsRepository.AnalyticsPeriod.WEEK -> (timeStringToMinutes(usageStats.weekUsageTime) / 3360 * 100)
                else -> (timeStringToMinutes(usageStats.monthUsageTime) / 14400 * 100)
            }.coerceIn(0, 100)
        )
    }

    private fun loadPeriodStats(period: AnalyticsRepository.AnalyticsPeriod) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val periodStats = analyticsRepository.getPeriodStats(period)
                updatePeriodStatsUI(period, periodStats)
            } catch (e: Exception) {
                Log.e(TAG, "loadPeriodStats: Error loading period stats", e)
                showErrorState()
            }
        }
    }

    private fun updatePeriodStatsUI(
        period: AnalyticsRepository.AnalyticsPeriod,
        periodStats: AnalyticsRepository.PeriodStats
    ) {
        val subtitle = when (period) {
            AnalyticsRepository.AnalyticsPeriod.TODAY -> getString(R.string.analytics_period_today)
            AnalyticsRepository.AnalyticsPeriod.WEEK -> getString(R.string.analytics_period_week)
            AnalyticsRepository.AnalyticsPeriod.MONTH -> getString(R.string.analytics_period_month)
            AnalyticsRepository.AnalyticsPeriod.ALL_TIME -> getString(R.string.analytics_period_all_time)
        }

        val usageLabel = when (period) {
            AnalyticsRepository.AnalyticsPeriod.TODAY -> getString(R.string.today_usage_label)
            AnalyticsRepository.AnalyticsPeriod.WEEK -> getString(R.string.week_usage_label)
            AnalyticsRepository.AnalyticsPeriod.MONTH -> getString(R.string.month_usage_label)
            AnalyticsRepository.AnalyticsPeriod.ALL_TIME -> getString(R.string.all_time_usage_label)
        }
        val usageProgress = when (period) {
            AnalyticsRepository.AnalyticsPeriod.TODAY -> (timeStringToMinutes(periodStats.usageTime) / 480 * 100)
            AnalyticsRepository.AnalyticsPeriod.WEEK -> (timeStringToMinutes(periodStats.usageTime) / 3360 * 100)
            AnalyticsRepository.AnalyticsPeriod.MONTH -> (timeStringToMinutes(periodStats.usageTime) / 14400 * 100)
            AnalyticsRepository.AnalyticsPeriod.ALL_TIME -> 100
        }.coerceIn(0, 100)

        composeUiState = composeUiState.copy(
            selectedPeriod = period,
            subtitle = subtitle,
            usageLabel = usageLabel,
            usageTime = periodStats.usageTime,
            usageProgress = usageProgress,
            averageOpacityText = getString(R.string.opacity_percent, (periodStats.averageOpacity * 100).toInt()),
            mostUsedPreset = periodStats.mostUsedPreset,
            currentStreakText = getString(R.string.streak_days, periodStats.currentStreak),
            totalEventsText = getString(R.string.total_events_count, periodStats.totalEvents),
            hasError = false
        )
    }
    
    private fun timeStringToMinutes(timeString: String): Int {
        // Convert HH:MM:SS format to total minutes
        val parts = timeString.split(":")
        return if (parts.size == 3) {
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts[1].toIntOrNull() ?: 0
            hours * 60 + minutes
        } else {
            0
        }
    }
    
    private fun showErrorState() {
        composeUiState = composeUiState.copy(hasError = true, isLoading = false)
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Refreshing analytics data")
        // Refresh data when fragment is resumed
        loadAnalyticsData()
    }
}
