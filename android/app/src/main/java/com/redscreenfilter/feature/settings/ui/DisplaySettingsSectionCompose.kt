package com.redscreenfilter.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redscreenfilter.R
import com.redscreenfilter.core.designsystem.RedScreenFilterTheme
import com.redscreenfilter.core.designsystem.RsfCard
import com.redscreenfilter.core.designsystem.RsfPrimarySwitch
import com.redscreenfilter.core.designsystem.RsfSectionHeader
import com.redscreenfilter.core.designsystem.RsfSettingRow
import com.redscreenfilter.core.designsystem.RsfTheme
import com.redscreenfilter.core.designsystem.RsfValueSlider
import com.redscreenfilter.core.model.ColorVariant

data class DisplayComposeUiState(
    val isOverlayEnabled: Boolean,
    val opacityPercentage: Int,
    val selectedColorVariant: ColorVariant,
    val showPermissionCard: Boolean
)

@Composable
fun DisplaySettingsSectionCompose(
    uiState: DisplayComposeUiState,
    onOverlayToggle: (Boolean) -> Unit,
    onOpacityChanged: (Int) -> Unit,
    onOpacityChangeFinished: (Int) -> Unit,
    onColorVariantSelected: (ColorVariant) -> Unit,
    isExtraDimEnabled: Boolean,
    extraDimIntensityPercentage: Int,
    onExtraDimToggle: (Boolean) -> Unit,
    onExtraDimIntensityChanged: (Int) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        // Power Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = RsfTheme.spacing.Lg),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(
                        elevation = RsfTheme.elevation.Lg,
                        shape = CircleShape,
                        ambientColor = RsfTheme.colors.primary.copy(alpha = 0.3f),
                        spotColor = RsfTheme.colors.primary.copy(alpha = 0.45f)
                    )
                    .clip(CircleShape)
                    .background(
                        if (uiState.isOverlayEnabled) {
                            RsfTheme.colors.primary
                        } else {
                            RsfTheme.colors.surfaceVariant
                        }
                    )
                    .clickable { onOverlayToggle(!uiState.isOverlayEnabled) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = stringResource(R.string.overlay_toggle_title),
                    modifier = Modifier.size(70.dp),
                    tint = if (uiState.isOverlayEnabled) {
                        RsfTheme.colors.onPrimary
                    } else {
                        RsfTheme.colors.onSurfaceVariant
                    }
                )
            }
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            RsfValueSlider(
                title = stringResource(R.string.opacity_title),
                value = uiState.opacityPercentage / 100f,
                onValueChange = { value ->
                    onOpacityChanged((value * 100).toInt().coerceIn(0, 100))
                },
                onValueChangeFinished = {
                    onOpacityChangeFinished(uiState.opacityPercentage)
                },
                valueLabel = stringResource(R.string.opacity_value_format, uiState.opacityPercentage),
                valueRange = 0f..1f
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = RsfTheme.spacing.Md, end = RsfTheme.spacing.Md, bottom = RsfTheme.spacing.Sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.opacity_value_format, uiState.opacityPercentage),
                    style = RsfTheme.typography.labelLarge,
                    color = RsfTheme.colors.onSurfaceVariant
                )
            }
        }

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
                            checked = isExtraDimEnabled,
                            onCheckedChange = onExtraDimToggle
                        )
                    }
                )

                if (isExtraDimEnabled) {
                    RsfValueSlider(
                        title = stringResource(R.string.extra_dim_intensity_title),
                        value = extraDimIntensityPercentage / 100f,
                        onValueChange = { value ->
                            onExtraDimIntensityChanged((value * 100).toInt().coerceIn(0, 100))
                        },
                        valueLabel = stringResource(
                            R.string.extra_dim_intensity_value_format,
                            extraDimIntensityPercentage
                        ),
                        valueRange = 0f..1f
                    )
                }
            }
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(RsfTheme.spacing.Md),
                verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
            ) {
                RsfSectionHeader(
                    title = stringResource(R.string.color_variant_title),
                    subtitle = stringResource(R.string.color_variant_subtitle)
                )

                val options = listOf(
                    ColorVariant.RED_STANDARD to stringResource(R.string.color_red_standard),
                    ColorVariant.RED_ORANGE to stringResource(R.string.color_red_orange),
                    ColorVariant.AMBER_WARM to stringResource(R.string.color_amber_warm),
                    ColorVariant.RED_PINK to stringResource(R.string.color_red_pink),
                    ColorVariant.HIGH_CONTRAST to stringResource(R.string.color_high_contrast),
                    ColorVariant.DEEP_MAROON to stringResource(R.string.color_deep_maroon)
                )

                options.forEach { (variant, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(RsfTheme.radius.Sm))
                            .clickable { onColorVariantSelected(variant) }
                            .padding(vertical = RsfTheme.spacing.Xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
                    ) {
                        RadioButton(
                            selected = uiState.selectedColorVariant == variant,
                            onClick = { onColorVariantSelected(variant) }
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(variant.colorValue))
                        )
                        Text(
                            text = label,
                            style = RsfTheme.typography.bodyMedium,
                            color = RsfTheme.colors.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = RsfTheme.spacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
                ) {
                    Text(
                        text = stringResource(R.string.color_preview),
                        style = RsfTheme.typography.bodyMedium,
                        color = RsfTheme.colors.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(RsfTheme.radius.Sm))
                            .background(Color(uiState.selectedColorVariant.colorValue))
                    )
                }
            }
        }

        if (uiState.showPermissionCard) {
            RsfCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(RsfTheme.spacing.Md),
                    verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
                ) {
                    Text(
                        text = stringResource(R.string.permission_required),
                        style = RsfTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.permission_description),
                        style = RsfTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    androidx.compose.material3.Button(
                        onClick = onRequestPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(text = stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun DisplaySettingsSectionComposePreview() {
    RedScreenFilterTheme {
        DisplaySettingsSectionCompose(
            uiState = DisplayComposeUiState(
                isOverlayEnabled = true,
                opacityPercentage = 64,
                selectedColorVariant = ColorVariant.RED_STANDARD,
                showPermissionCard = false
            ),
            onOverlayToggle = {},
            onOpacityChanged = {},
            onOpacityChangeFinished = {},
            onColorVariantSelected = {},
            isExtraDimEnabled = true,
            extraDimIntensityPercentage = 40,
            onExtraDimToggle = {},
            onExtraDimIntensityChanged = {},
            onRequestPermission = {}
        )
    }
}
