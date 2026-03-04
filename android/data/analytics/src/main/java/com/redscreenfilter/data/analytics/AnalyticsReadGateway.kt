package com.redscreenfilter.data.analytics

interface AnalyticsReadGateway {
    suspend fun getUsageStatsSnapshot(): UsageStatsSnapshot

    data class UsageStatsSnapshot(
        val todayUsageTime: String,
        val weekUsageTime: String,
        val monthUsageTime: String
    )
}
