package com.redscreenfilter.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.redscreenfilter.R
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.core.designsystem.RsfCard
import com.redscreenfilter.core.designsystem.RsfPrimarySwitch
import com.redscreenfilter.core.designsystem.RsfSettingRow
import com.redscreenfilter.core.designsystem.RsfTheme

data class WellnessComposeUiState(
    val isBatteryOptimizationEnabled: Boolean,
    val isEyeStrainReminderEnabled: Boolean,
    val notificationStyle: String
)

@Composable
fun WellnessSettingsSectionCompose(
    uiState: WellnessComposeUiState,
    onBatteryOptimizationToggle: (Boolean) -> Unit,
    onEyeStrainReminderToggle: (Boolean) -> Unit,
    onNotificationStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        RsfCard(modifier = Modifier.fillMaxWidth()) {
            RsfSettingRow(
                title = stringResource(R.string.battery_optimization_title),
                subtitle = stringResource(R.string.battery_optimization_subtitle),
                trailing = {
                    RsfPrimarySwitch(
                        checked = uiState.isBatteryOptimizationEnabled,
                        onCheckedChange = onBatteryOptimizationToggle
                    )
                }
            )
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(RsfTheme.spacing.Md), verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)) {
                RsfSettingRow(
                    title = stringResource(R.string.eye_strain_reminder_toggle),
                    subtitle = stringResource(R.string.eye_strain_reminder_subtitle),
                    trailing = {
                        RsfPrimarySwitch(
                            checked = uiState.isEyeStrainReminderEnabled,
                            onCheckedChange = onEyeStrainReminderToggle
                        )
                    }
                )

                if (uiState.isEyeStrainReminderEnabled) {
                    Text(
                        text = stringResource(R.string.notification_style_label),
                        style = RsfTheme.typography.titleMedium,
                        color = RsfTheme.colors.onSurface
                    )

                    NotificationStyleRow(
                        label = stringResource(R.string.notification_style_sound),
                        selected = uiState.notificationStyle == "sound",
                        onClick = { onNotificationStyleSelected("sound") }
                    )
                    NotificationStyleRow(
                        label = stringResource(R.string.notification_style_vibration),
                        selected = uiState.notificationStyle == "vibration",
                        onClick = { onNotificationStyleSelected("vibration") }
                    )
                    NotificationStyleRow(
                        label = stringResource(R.string.notification_style_silent),
                        selected = uiState.notificationStyle == "silent",
                        onClick = { onNotificationStyleSelected("silent") }
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.info_message),
            style = RsfTheme.typography.bodyMedium,
            color = RsfTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = RsfTheme.spacing.Sm)
        )
    }
}

@Composable
private fun NotificationStyleRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    RsfSettingRow(
        title = label,
        trailing = {
            RadioButton(selected = selected, onClick = onClick)
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun WellnessSettingsSectionComposePreview() {
    RedScreenFilterTheme {
        WellnessSettingsSectionCompose(
            uiState = WellnessComposeUiState(
                isBatteryOptimizationEnabled = true,
                isEyeStrainReminderEnabled = true,
                notificationStyle = "sound"
            ),
            onBatteryOptimizationToggle = {},
            onEyeStrainReminderToggle = {},
            onNotificationStyleSelected = {}
        )
    }
}
