package com.redscreenfilter.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import android.widget.ProgressBar
import com.redscreenfilter.R
import com.redscreenfilter.data.repository.AnalyticsRepository
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
    
    // UI Components
    private lateinit var tabLayout: TabLayout
    private lateinit var cardTodayStats: MaterialCardView
    private lateinit var cardWeekStats: MaterialCardView
    private lateinit var cardMonthStats: MaterialCardView
    private lateinit var cardStreakStats: MaterialCardView
    
    private lateinit var textTodayUsageTime: TextView
    private lateinit var textWeekUsageTime: TextView
    private lateinit var textMonthUsageTime: TextView
    private lateinit var textTodayUsageLabel: TextView
    private lateinit var textWeekUsageLabel: TextView
    private lateinit var textMonthUsageLabel: TextView
    private lateinit var textAnalyticsSubtitle: TextView
    private lateinit var textAverageOpacity: TextView
    private lateinit var textMostUsedPreset: TextView
    private lateinit var textCurrentStreak: TextView
    private lateinit var textTotalEvents: TextView
    
    private lateinit var progressBarToday: ProgressBar
    private lateinit var progressBarWeek: ProgressBar
    private lateinit var progressBarMonth: ProgressBar
    private lateinit var progressBarLoading: ProgressBar

    private var selectedPeriod = AnalyticsRepository.AnalyticsPeriod.TODAY
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "onViewCreated: Initializing Analytics Fragment")
        
        // Initialize repository
        analyticsRepository = AnalyticsRepository.getInstance(requireContext())
        
        // Initialize views
        initializeViews(view)
        
        // Load analytics data
        loadAnalyticsData()
    }
    
    private fun initializeViews(view: View) {
        // Tab layout
        tabLayout = view.findViewById(R.id.tab_layout_analytics)
        
        // Statistics cards
        cardTodayStats = view.findViewById(R.id.card_today_stats)
        cardWeekStats = view.findViewById(R.id.card_week_stats)
        cardMonthStats = view.findViewById(R.id.card_month_stats)
        cardStreakStats = view.findViewById(R.id.card_streak_stats)
        
        // Usage time text views
        textTodayUsageTime = view.findViewById(R.id.text_today_usage_time)
        textWeekUsageTime = view.findViewById(R.id.text_week_usage_time)
        textMonthUsageTime = view.findViewById(R.id.text_month_usage_time)
        textTodayUsageLabel = view.findViewById(R.id.text_today_usage_label)
        textWeekUsageLabel = view.findViewById(R.id.text_week_usage_label)
        textMonthUsageLabel = view.findViewById(R.id.text_month_usage_label)
        textAnalyticsSubtitle = view.findViewById(R.id.text_analytics_subtitle)
        
        // Statistics text views
        textAverageOpacity = view.findViewById(R.id.text_average_opacity)
        textMostUsedPreset = view.findViewById(R.id.text_most_used_preset)
        textCurrentStreak = view.findViewById(R.id.text_current_streak)
        textTotalEvents = view.findViewById(R.id.text_total_events)
        
        // Progress bars
        progressBarToday = view.findViewById(R.id.progress_today)
        progressBarWeek = view.findViewById(R.id.progress_week)
        progressBarMonth = view.findViewById(R.id.progress_month)
        progressBarLoading = view.findViewById(R.id.progress_bar_loading)
        
        // Setup tab layout
        setupTabLayout()
    }
    
    private fun setupTabLayout() {
        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("Today"))
        tabLayout.addTab(tabLayout.newTab().setText("Week"))
        tabLayout.addTab(tabLayout.newTab().setText("Month"))
        tabLayout.addTab(tabLayout.newTab().setText("All Time"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedPeriod = when (tab?.position) {
                    0 -> AnalyticsRepository.AnalyticsPeriod.TODAY
                    1 -> AnalyticsRepository.AnalyticsPeriod.WEEK
                    2 -> AnalyticsRepository.AnalyticsPeriod.MONTH
                    else -> AnalyticsRepository.AnalyticsPeriod.ALL_TIME
                }
                Log.d(TAG, "Tab selected: ${tab?.text}, period=$selectedPeriod")
                updateVisibleUsageCard(selectedPeriod)
                loadPeriodStats(selectedPeriod)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadAnalyticsData() {
        progressBarLoading.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val usageStats = analyticsRepository.getUsageStats()
                
                Log.d(TAG, "Analytics data loaded: $usageStats")
                
                // Update UI with fetched data
                updateUI(usageStats)
                updateVisibleUsageCard(selectedPeriod)
                loadPeriodStats(selectedPeriod)
                
                progressBarLoading.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "loadAnalyticsData: Error loading data", e)
                progressBarLoading.visibility = View.GONE
                showErrorState()
            }
        }
    }
    
    private fun updateUI(usageStats: AnalyticsRepository.UsageStats) {
        // Update usage times
        textTodayUsageTime.text = usageStats.todayUsageTime
        textWeekUsageTime.text = usageStats.weekUsageTime
        textMonthUsageTime.text = usageStats.monthUsageTime
        
        // Update progress bars (visual representation)
        updateProgressBars(usageStats)
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
        textAnalyticsSubtitle.text = when (period) {
            AnalyticsRepository.AnalyticsPeriod.TODAY -> getString(R.string.analytics_period_today)
            AnalyticsRepository.AnalyticsPeriod.WEEK -> getString(R.string.analytics_period_week)
            AnalyticsRepository.AnalyticsPeriod.MONTH -> getString(R.string.analytics_period_month)
            AnalyticsRepository.AnalyticsPeriod.ALL_TIME -> getString(R.string.analytics_period_all_time)
        }

        textAverageOpacity.text = getString(R.string.opacity_percent, (periodStats.averageOpacity * 100).toInt())
        textMostUsedPreset.text = periodStats.mostUsedPreset
        textCurrentStreak.text = getString(R.string.streak_days, periodStats.currentStreak)
        textTotalEvents.text = getString(R.string.total_events_count, periodStats.totalEvents)

        when (period) {
            AnalyticsRepository.AnalyticsPeriod.TODAY -> {
                textTodayUsageLabel.text = getString(R.string.today_usage_label)
                textTodayUsageTime.text = periodStats.usageTime
                progressBarToday.progress = (timeStringToMinutes(periodStats.usageTime) / 480 * 100).coerceIn(0, 100)
            }
            AnalyticsRepository.AnalyticsPeriod.WEEK -> {
                textWeekUsageLabel.text = getString(R.string.week_usage_label)
                textWeekUsageTime.text = periodStats.usageTime
                progressBarWeek.progress = (timeStringToMinutes(periodStats.usageTime) / 3360 * 100).coerceIn(0, 100)
            }
            AnalyticsRepository.AnalyticsPeriod.MONTH -> {
                textMonthUsageLabel.text = getString(R.string.month_usage_label)
                textMonthUsageTime.text = periodStats.usageTime
                progressBarMonth.progress = (timeStringToMinutes(periodStats.usageTime) / 14400 * 100).coerceIn(0, 100)
            }
            AnalyticsRepository.AnalyticsPeriod.ALL_TIME -> {
                textMonthUsageLabel.text = getString(R.string.all_time_usage_label)
                textMonthUsageTime.text = periodStats.usageTime
                progressBarMonth.progress = 100
            }
        }
    }

    private fun updateVisibleUsageCard(period: AnalyticsRepository.AnalyticsPeriod) {
        cardTodayStats.visibility = if (period == AnalyticsRepository.AnalyticsPeriod.TODAY) View.VISIBLE else View.GONE
        cardWeekStats.visibility = if (period == AnalyticsRepository.AnalyticsPeriod.WEEK) View.VISIBLE else View.GONE
        cardMonthStats.visibility = if (
            period == AnalyticsRepository.AnalyticsPeriod.MONTH ||
            period == AnalyticsRepository.AnalyticsPeriod.ALL_TIME
        ) View.VISIBLE else View.GONE
    }
    
    private fun updateProgressBars(usageStats: AnalyticsRepository.UsageStats) {
        // Convert time strings to hours for progress visualization
        val todayHours = timeStringToMinutes(usageStats.todayUsageTime)
        val weekHours = timeStringToMinutes(usageStats.weekUsageTime)
        val monthHours = timeStringToMinutes(usageStats.monthUsageTime)
        
        // Scale to progress bar max (assuming reasonable daily usage is 8 hours)
        progressBarToday.progress = (todayHours / 480 * 100).coerceIn(0, 100) // 480 min = 8 hours
        progressBarWeek.progress = (weekHours / 3360 * 100).coerceIn(0, 100) // 3360 min = 56 hours
        progressBarMonth.progress = (monthHours / 14400 * 100).coerceIn(0, 100) // 14400 min = 240 hours
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
        textTodayUsageTime.text = "Error"
        textWeekUsageTime.text = "Error"
        textMonthUsageTime.text = "Error"
        textAverageOpacity.text = "N/A"
        textMostUsedPreset.text = "N/A"
        textCurrentStreak.text = "0"
        textTotalEvents.text = "0"
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Refreshing analytics data")
        // Refresh data when fragment is resumed
        loadAnalyticsData()
    }
}
