package com.redscreenfilter.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import com.redscreenfilter.core.designsystem.RsfValueSlider

data class BrightnessComposeUiState(
    val brightnessPercentage: Int,
    val hasSystemBrightnessPermission: Boolean,
    val isExtraDimEnabled: Boolean,
    val extraDimIntensityPercentage: Int
)

@Composable
fun BrightnessSettingsSectionCompose(
    uiState: BrightnessComposeUiState,
    onBrightnessChanged: (Int) -> Unit,
    onExtraDimToggle: (Boolean) -> Unit,
    onExtraDimIntensityChanged: (Int) -> Unit,
    onRequestSystemBrightnessPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        // Brightness Card
        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(RsfTheme.spacing.Md),
                verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
            ) {
                RsfValueSlider(
                    title = stringResource(R.string.brightness_title),
                    value = uiState.brightnessPercentage / 100f,
                    onValueChange = { value ->
                        onBrightnessChanged((value * 100).toInt().coerceIn(1, 100))
                    },
                    valueLabel = stringResource(R.string.brightness_value_format, uiState.brightnessPercentage),
                    valueRange = 0.01f..1f
                )

                // Permission Status or Message
                if (!uiState.hasSystemBrightnessPermission) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
                    ) {
                        Divider(modifier = Modifier.fillMaxWidth())
                        
                        Text(
                            text = stringResource(R.string.brightness_permission_required),
                            style = RsfTheme.typography.bodyMedium,
                            color = RsfTheme.colors.error
                        )
                        
                        Button(
                            onClick = onRequestSystemBrightnessPermission,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.grant_brightness_permission))
                        }
                        
                        Text(
                            text = "Step-by-step: \n1. Tap the button above\n2. Find and enable \"Allow modify system settings\"\n3. Return to this app",
                            style = RsfTheme.typography.bodySmall,
                            color = RsfTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Extra Dim Card
        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(RsfTheme.spacing.Md),
                verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
            ) {
                RsfSettingRow(
                    title = stringResource(R.string.extra_dim_title),
                    subtitle = stringResource(R.string.extra_dim_subtitle),
                    trailing = {
                        RsfPrimarySwitch(
                            checked = uiState.isExtraDimEnabled,
                            onCheckedChange = onExtraDimToggle
                        )
                    }
                )

                if (uiState.isExtraDimEnabled) {
                    RsfValueSlider(
                        title = stringResource(R.string.extra_dim_intensity_title),
                        value = uiState.extraDimIntensityPercentage / 100f,
                        onValueChange = { value ->
                            onExtraDimIntensityChanged((value * 100).toInt().coerceIn(0, 100))
                        },
                        valueLabel = stringResource(
                            R.string.extra_dim_intensity_value_format,
                            uiState.extraDimIntensityPercentage
                        ),
                        valueRange = 0f..1f
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun BrightnessSettingsSectionComposePreview() {
    RedScreenFilterTheme {
        BrightnessSettingsSectionCompose(
            uiState = BrightnessComposeUiState(
                brightnessPercentage = 45,
                hasSystemBrightnessPermission = false,
                isExtraDimEnabled = true,
                extraDimIntensityPercentage = 35
            ),
            onBrightnessChanged = {},
            onExtraDimToggle = {},
            onExtraDimIntensityChanged = {},
            onRequestSystemBrightnessPermission = {}
        )
    }
}
