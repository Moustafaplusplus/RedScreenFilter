package com.redscreenfilter.feature.analytics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.core.designsystem.RsfCard
import com.redscreenfilter.core.designsystem.RsfSectionHeader
import com.redscreenfilter.core.designsystem.RsfStatCard
import com.redscreenfilter.core.designsystem.RsfTheme
import com.redscreenfilter.data.repository.AnalyticsRepository

data class AnalyticsComposeUiState(
    val selectedPeriod: AnalyticsRepository.AnalyticsPeriod,
    val subtitle: String,
    val usageTime: String,
    val usageLabel: String,
    val usageProgress: Int,
    val averageOpacityText: String,
    val mostUsedPreset: String,
    val currentStreakText: String,
    val totalEventsText: String,
    val isLoading: Boolean,
    val hasError: Boolean
)

@Composable
fun AnalyticsComposeScreen(
    uiState: AnalyticsComposeUiState,
    onPeriodSelected: (AnalyticsRepository.AnalyticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(RsfTheme.spacing.Md),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        RsfSectionHeader(
            title = "Usage Analytics",
            subtitle = uiState.subtitle
        )

        val periods = listOf(
            AnalyticsRepository.AnalyticsPeriod.TODAY to "Today",
            AnalyticsRepository.AnalyticsPeriod.WEEK to "Week",
            AnalyticsRepository.AnalyticsPeriod.MONTH to "Month",
            AnalyticsRepository.AnalyticsPeriod.ALL_TIME to "All Time"
        )

        TabRow(selectedTabIndex = periods.indexOfFirst { it.first == uiState.selectedPeriod }.coerceAtLeast(0)) {
            periods.forEach { (period, label) ->
                Tab(
                    selected = period == uiState.selectedPeriod,
                    onClick = { onPeriodSelected(period) },
                    text = { Text(label) }
                )
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(RsfTheme.spacing.Md))
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(RsfTheme.spacing.Md),
                verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
            ) {
                Text(
                    text = uiState.usageLabel,
                    style = RsfTheme.typography.bodyMedium,
                    color = RsfTheme.colors.onSurfaceVariant
                )
                Text(
                    text = if (uiState.hasError) "Error" else uiState.usageTime,
                    style = RsfTheme.typography.headlineMedium,
                    color = RsfTheme.colors.primary
                )
                LinearProgressIndicator(
                    progress = { (uiState.usageProgress.coerceIn(0, 100) / 100f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        RsfStatCard(
            label = "Average Opacity",
            value = if (uiState.hasError) "N/A" else uiState.averageOpacityText
        )
        RsfStatCard(
            label = "Most Used Preset",
            value = if (uiState.hasError) "N/A" else uiState.mostUsedPreset
        )
        RsfStatCard(
            label = "Current Streak",
            value = if (uiState.hasError) "0" else uiState.currentStreakText
        )
        RsfStatCard(
            label = "Total Interactions",
            value = if (uiState.hasError) "0" else uiState.totalEventsText
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun AnalyticsComposeScreenPreview() {
    RedScreenFilterTheme {
        AnalyticsComposeScreen(
            uiState = AnalyticsComposeUiState(
                selectedPeriod = AnalyticsRepository.AnalyticsPeriod.TODAY,
                subtitle = "Showing Today data",
                usageTime = "04:10:00",
                usageLabel = "Today's Usage",
                usageProgress = 62,
                averageOpacityText = "68%",
                mostUsedPreset = "RED_STANDARD",
                currentStreakText = "7",
                totalEventsText = "102 events",
                isLoading = false,
                hasError = false
            ),
            onPeriodSelected = {}
        )
    }
}
