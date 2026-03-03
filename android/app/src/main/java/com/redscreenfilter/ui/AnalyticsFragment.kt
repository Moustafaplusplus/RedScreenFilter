package com.redscreenfilter.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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
    private lateinit var textAverageOpacity: TextView
    private lateinit var textMostUsedPreset: TextView
    private lateinit var textCurrentStreak: TextView
    private lateinit var textTotalEvents: TextView
    
    private lateinit var progressBarToday: ProgressBar
    private lateinit var progressBarWeek: ProgressBar
    private lateinit var progressBarMonth: ProgressBar
    private lateinit var progressBarLoading: ProgressBar
    
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
                // Tab UI is already updated by TabLayout
                Log.d(TAG, "Tab selected: ${tab?.text}")
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
        
        // Update statistics
        textAverageOpacity.text = getString(R.string.opacity_percent, (usageStats.averageOpacity * 100).toInt())
        textMostUsedPreset.text = usageStats.mostUsedPreset
        textCurrentStreak.text = getString(R.string.streak_days, usageStats.currentStreak)
        textTotalEvents.text = getString(R.string.total_events_count, usageStats.totalEvents)
        
        // Update progress bars (visual representation)
        updateProgressBars(usageStats)
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
