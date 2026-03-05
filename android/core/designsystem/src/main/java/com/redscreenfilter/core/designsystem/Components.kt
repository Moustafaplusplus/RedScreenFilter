package com.redscreenfilter.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RsfCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = RsfTheme.elevation.Lg,
                shape = RoundedCornerShape(RsfTheme.radius.Lg),
                ambientColor = RsfTheme.colors.primary.copy(alpha = 0.15f),
                spotColor = RsfTheme.colors.primary.copy(alpha = 0.25f)
            )
            .border(
                width = RsfTheme.border.Thin,
                color = RsfColors.GlassStroke,
                shape = RoundedCornerShape(RsfTheme.radius.Lg)
            ),
        shape = RoundedCornerShape(RsfTheme.radius.Lg),
        colors = CardDefaults.cardColors(containerColor = RsfTheme.colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun RsfSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Xs)) {
        Text(
            text = title,
            style = RsfTheme.typography.titleLarge,
            color = RsfTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = RsfTheme.typography.bodyMedium,
                color = RsfTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RsfSettingRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RsfTheme.spacing.Md, vertical = RsfTheme.spacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Xs)
        ) {
            Text(
                text = title,
                style = RsfTheme.typography.titleMedium,
                color = RsfTheme.colors.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = RsfTheme.typography.bodyMedium,
                    color = RsfTheme.colors.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun RsfPrimarySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = RsfTheme.colors.onPrimary,
            checkedTrackColor = RsfTheme.colors.primary,
            uncheckedThumbColor = RsfTheme.colors.onSurfaceVariant,
            uncheckedTrackColor = RsfTheme.colors.surface
        )
    )
}

@Composable
fun RsfValueSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueLabel: String,
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RsfTheme.spacing.Md, vertical = RsfTheme.spacing.Sm),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = RsfTheme.typography.titleMedium,
                color = RsfTheme.colors.onSurface
            )
            Text(
                text = valueLabel,
                style = RsfTheme.typography.labelLarge,
                color = RsfTheme.colors.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RsfSegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        for (row in 0 until (options.size + 1) / 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    if (index < options.size) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .shadow(
                                    elevation = if (selectedIndex == index) RsfTheme.elevation.Md else RsfTheme.elevation.Sm,
                                    shape = RoundedCornerShape(RsfTheme.radius.Md),
                                    ambientColor = if (selectedIndex == index) {
                                        RsfTheme.colors.primary.copy(alpha = 0.2f)
                                    } else {
                                        RsfTheme.colors.surfaceVariant.copy(alpha = 0.1f)
                                    },
                                    spotColor = if (selectedIndex == index) {
                                        RsfTheme.colors.primary.copy(alpha = 0.3f)
                                    } else {
                                        RsfTheme.colors.surfaceVariant.copy(alpha = 0.15f)
                                    }
                                ),
                            onClick = { onSelectionChanged(index) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedIndex == index) {
                                    RsfTheme.colors.primary
                                } else {
                                    RsfTheme.colors.surfaceVariant
                                },
                                contentColor = if (selectedIndex == index) {
                                    RsfTheme.colors.onPrimary
                                } else {
                                    RsfTheme.colors.onSurfaceVariant
                                }
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Text(
                                text = options[index],
                                style = RsfTheme.typography.labelMedium,
                                maxLines = 1,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    } else {
                        // Empty spacer for odd number of items
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RsfStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = RsfTheme.elevation.Md,
                shape = RoundedCornerShape(RsfTheme.radius.Lg),
                ambientColor = RsfTheme.colors.primary.copy(alpha = 0.2f),
                spotColor = RsfTheme.colors.primary.copy(alpha = 0.3f)
            )
            .border(
                width = RsfTheme.border.Thin,
                color = RsfColors.GlassStroke,
                shape = RoundedCornerShape(RsfTheme.radius.Lg)
            ),
        shape = RoundedCornerShape(RsfTheme.radius.Lg),
        colors = CardDefaults.cardColors(containerColor = RsfTheme.colors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(RsfTheme.spacing.Md),
            verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Xs)
        ) {
            Text(
                text = label,
                style = RsfTheme.typography.bodyMedium,
                color = RsfTheme.colors.onSurfaceVariant
            )
            Text(
                text = value,
                style = RsfTheme.typography.headlineMedium,
                color = RsfTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
            if (!supportingText.isNullOrBlank()) {
                Text(
                    text = supportingText,
                    style = RsfTheme.typography.labelMedium,
                    color = RsfTheme.colors.tertiary
                )
            }
        }
    }
}

@Composable
fun RsfDesignSystemSpec(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(RsfTheme.spacing.Lg),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        RsfSectionHeader(
            title = "Design System v1",
            subtitle = "Vibrant Material 3 primitives"
        )

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(RsfTheme.spacing.Sm),
                verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Xs)
            ) {
                RsfSettingRow(
                    title = "Overlay",
                    subtitle = "Primary setting row",
                    trailing = {
                        RsfPrimarySwitch(checked = true, onCheckedChange = {})
                    }
                )
                RsfValueSlider(
                    title = "Opacity",
                    value = 0.64f,
                    onValueChange = {},
                    onValueChangeFinished = null,
                    valueLabel = "64%"
                )
            }
        }

        RsfSegmentedTabs(
            options = listOf("Display", "Automation", "Wellness"),
            selectedIndex = 0,
            onSelectionChanged = {},
            modifier = Modifier.widthIn(max = 560.dp)
        )

        RsfStatCard(
            label = "Today usage",
            value = "04:32:10",
            supportingText = "+12% from yesterday"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun RsfDesignSystemSpecPreview() {
    RedScreenFilterTheme {
        RsfDesignSystemSpec()
    }
}
