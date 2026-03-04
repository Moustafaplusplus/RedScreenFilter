package com.redscreenfilter.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF050608)
@Composable
private fun ComposeSandboxPreview() {
    RedScreenFilterTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(RsfSpacing.Lg)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(RsfSpacing.Sm)) {
                Text(text = "Red Screen Filter", style = MaterialTheme.typography.headlineMedium)
                Text(text = "Compose foundation is active", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Phase 1 preview sandbox", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
