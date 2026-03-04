package com.redscreenfilter.feature.app_exemption.ui

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.redscreenfilter.core.designsystem.RsfCard
import com.redscreenfilter.core.designsystem.RsfSectionHeader
import com.redscreenfilter.core.designsystem.RsfTheme
import com.redscreenfilter.data.InstalledApp

data class AppExemptionComposeUiState(
    val query: String,
    val isLoading: Boolean,
    val apps: List<InstalledApp>,
    val hasUsageStatsPermission: Boolean = true
)

@Composable
fun AppExemptionComposeScreen(
    uiState: AppExemptionComposeUiState,
    onQueryChanged: (String) -> Unit,
    onExemptionChanged: (String, Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(RsfTheme.spacing.Md),
        verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Md)
    ) {
        RsfSectionHeader(
            title = "App Exemptions",
            subtitle = "Hide overlay for specific apps"
        )

        if (!uiState.hasUsageStatsPermission) {
            RsfCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(RsfTheme.spacing.Md),
                    verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
                ) {
                    Text(
                        text = "Usage Access permission is required to detect the foreground app and hide the overlay automatically.",
                        style = RsfTheme.typography.bodyMedium,
                        color = RsfTheme.colors.onSurface
                    )
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChanged,
            singleLine = true,
            label = { Text("Search apps") },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        if (!uiState.isLoading && uiState.apps.isEmpty()) {
            Text(
                text = "No apps found",
                style = RsfTheme.typography.bodyMedium,
                color = RsfTheme.colors.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm)
        ) {
            items(uiState.apps, key = { it.packageName }) { app ->
                AppExemptionItem(app = app, onExemptionChanged = onExemptionChanged)
            }
        }
    }
}

@Composable
private fun AppExemptionItem(
    app: InstalledApp,
    onExemptionChanged: (String, Boolean) -> Unit
) {
    RsfCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExemptionChanged(app.packageName, !app.isExempted) }
                .padding(RsfTheme.spacing.Md),
            horizontalArrangement = Arrangement.spacedBy(RsfTheme.spacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(drawable = app.icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, style = RsfTheme.typography.titleMedium, color = RsfTheme.colors.onSurface)
                Text(text = app.packageName, style = RsfTheme.typography.labelMedium, color = RsfTheme.colors.onSurfaceVariant)
            }
            Checkbox(
                checked = app.isExempted,
                onCheckedChange = { checked -> onExemptionChanged(app.packageName, checked) }
            )
        }
    }
}

@Composable
private fun AppIcon(drawable: Drawable?) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            imageView.setImageDrawable(drawable)
        },
        modifier = Modifier
            .size(40.dp)
            .padding(end = RsfTheme.spacing.Xs)
    )
}
