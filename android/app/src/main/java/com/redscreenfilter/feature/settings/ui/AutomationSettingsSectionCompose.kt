package com.redscreenfilter.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.redscreenfilter.R
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.core.designsystem.RsfCard
import com.redscreenfilter.core.designsystem.RsfPrimarySwitch
import com.redscreenfilter.core.designsystem.RsfSectionHeader
import com.redscreenfilter.core.designsystem.RsfSettingRow
import com.redscreenfilter.core.designsystem.RsfTheme
import com.redscreenfilter.core.designsystem.RsfValueSlider

data class AutomationComposeUiState(
    val isSchedulingEnabled: Boolean,
    val startTime: String,
    val endTime: String,
    val isLocationSchedulingEnabled: Boolean,
    val isLocationLoading: Boolean,
    val sunsetTime: String,
    val sunriseTime: String,
    val locationOffsetMinutes: Int,
    val isLightSensorEnabled: Boolean,
    val lightSensitivityValue: Float,
    val lightSensitivityLabel: String,
    val currentLuxLabel: String,
    val isLightSensorLocked: Boolean
)

@Composable
fun AutomationSettingsSectionCompose(
    uiState: AutomationComposeUiState,
    onSchedulingToggle: (Boolean) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onLocationSchedulingToggle: (Boolean) -> Unit,
    onRequestLocation: () -> Unit,
    onLocationOffsetChanged: (Int) -> Unit,
    onLightSensorToggle: (Boolean) -> Unit,
    onLightSensitivityChanged: (Float) -> Unit,
    onLightSensorLockToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(RsfTheme.spacing.Md), verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)) {
                RsfSettingRow(
                    title = stringResource(R.string.scheduling_title),
                    subtitle = stringResource(R.string.scheduling_subtitle),
                    trailing = {
                        RsfPrimarySwitch(checked = uiState.isSchedulingEnabled, onCheckedChange = onSchedulingToggle)
                    }
                )

                if (uiState.isSchedulingEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
                    ) {
                        OutlinedButton(onClick = onStartTimeClick, modifier = Modifier.weight(1f)) {
                            Text(text = "${stringResource(R.string.scheduling_start_time)}: ${uiState.startTime}")
                        }
                        OutlinedButton(onClick = onEndTimeClick, modifier = Modifier.weight(1f)) {
                            Text(text = "${stringResource(R.string.scheduling_end_time)}: ${uiState.endTime}")
                        }
                    }
                }
            }
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(RsfTheme.spacing.Md), verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)) {
                RsfSettingRow(
                    title = stringResource(R.string.location_scheduling_title),
                    subtitle = stringResource(R.string.location_scheduling_subtitle),
                    trailing = {
                        RsfPrimarySwitch(
                            checked = uiState.isLocationSchedulingEnabled,
                            onCheckedChange = onLocationSchedulingToggle
                        )
                    }
                )

                if (uiState.isLocationSchedulingEnabled) {
                    Button(
                        onClick = onRequestLocation,
                        enabled = !uiState.isLocationLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.isLocationLoading) "Getting location..." else stringResource(R.string.location_get_location)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${stringResource(R.string.location_sunset_label)}: ${uiState.sunsetTime}")
                        Text(text = "${stringResource(R.string.location_sunrise_label)}: ${uiState.sunriseTime}")
                    }

                    RsfValueSlider(
                        title = stringResource(R.string.location_offset),
                        value = uiState.locationOffsetMinutes.toFloat(),
                        onValueChange = { value -> onLocationOffsetChanged(value.toInt()) },
                        valueRange = -60f..60f,
                        valueLabel = stringResource(R.string.location_offset_value, uiState.locationOffsetMinutes)
                    )
                }
            }
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(RsfTheme.spacing.Md), verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)) {
                RsfSettingRow(
                    title = stringResource(R.string.light_sensor_title),
                    subtitle = stringResource(R.string.light_sensor_subtitle),
                    trailing = {
                        RsfPrimarySwitch(checked = uiState.isLightSensorEnabled, onCheckedChange = onLightSensorToggle)
                    }
                )

                if (uiState.isLightSensorEnabled) {
                    RsfValueSlider(
                        title = stringResource(R.string.light_sensitivity_label),
                        value = uiState.lightSensitivityValue,
                        onValueChange = onLightSensitivityChanged,
                        valueRange = 0f..2f,
                        valueLabel = uiState.lightSensitivityLabel
                    )

                    Text(
                        text = uiState.currentLuxLabel,
                        style = RsfTheme.typography.labelMedium,
                        color = RsfTheme.colors.onSurfaceVariant
                    )

                    RsfSettingRow(
                        title = stringResource(R.string.lock_light_sensor),
                        subtitle = stringResource(R.string.lock_light_sensor_subtitle),
                        trailing = {
                            RsfPrimarySwitch(
                                checked = uiState.isLightSensorLocked,
                                onCheckedChange = onLightSensorLockToggle
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun AutomationSettingsSectionComposePreview() {
    RedScreenFilterTheme {
        AutomationSettingsSectionCompose(
            uiState = AutomationComposeUiState(
                isSchedulingEnabled = true,
                startTime = "21:00",
                endTime = "07:00",
                isLocationSchedulingEnabled = true,
                isLocationLoading = false,
                sunsetTime = "18:42",
                sunriseTime = "06:08",
                locationOffsetMinutes = 0,
                isLightSensorEnabled = true,
                lightSensitivityValue = 1f,
                lightSensitivityLabel = "Medium Sensitivity",
                currentLuxLabel = "Current Light Level: 320 lux",
                isLightSensorLocked = false
            ),
            onSchedulingToggle = {},
            onStartTimeClick = {},
            onEndTimeClick = {},
            onLocationSchedulingToggle = {},
            onRequestLocation = {},
            onLocationOffsetChanged = {},
            onLightSensorToggle = {},
            onLightSensitivityChanged = {},
            onLightSensorLockToggle = {}
        )
    }
}
