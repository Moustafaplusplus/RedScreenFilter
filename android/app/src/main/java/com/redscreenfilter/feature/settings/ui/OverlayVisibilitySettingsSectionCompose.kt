package com.redscreenfilter.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

data class OverlayVisibilityComposeUiState(
    val hideOnLockScreen: Boolean,
    val hideOnHomeScreen: Boolean
)

@Composable
fun OverlayVisibilitySettingsSectionCompose(
    uiState: OverlayVisibilityComposeUiState,
    onHideOnLockScreenChanged: (Boolean) -> Unit,
    onHideOnHomeScreenChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(RsfTheme.spacing.Md)) {
                RsfSettingRow(
                    title = stringResource(R.string.hide_overlay_lockscreen_title),
                    subtitle = stringResource(R.string.hide_overlay_lockscreen_subtitle),
                    trailing = {
                        RsfPrimarySwitch(
                            checked = uiState.hideOnLockScreen,
                            onCheckedChange = onHideOnLockScreenChanged
                        )
                    }
                )
            }
        }

        RsfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(RsfTheme.spacing.Md)) {
                RsfSettingRow(
                    title = stringResource(R.string.hide_overlay_homescreen_title),
                    subtitle = stringResource(R.string.hide_overlay_homescreen_subtitle),
                    trailing = {
                        RsfPrimarySwitch(
                            checked = uiState.hideOnHomeScreen,
                            onCheckedChange = onHideOnHomeScreenChanged
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 420)
@Composable
private fun OverlayVisibilitySettingsSectionComposePreview() {
    RedScreenFilterTheme {
        OverlayVisibilitySettingsSectionCompose(
            uiState = OverlayVisibilityComposeUiState(
                hideOnLockScreen = true,
                hideOnHomeScreen = false
            ),
            onHideOnLockScreenChanged = {},
            onHideOnHomeScreenChanged = {}
        )
    }
}
