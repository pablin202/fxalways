package com.fxalways.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(if (enabled) FxTheme.colors.accent else FxTheme.colors.surface3)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = FxTheme.colors.bg,
                    strokeWidth = 2.dp,
                )
            }
            Text(text, style = FxTheme.typography.bodyStrong, color = if (enabled) FxTheme.colors.bg else FxTheme.colors.textDim)
        }
    }
}

@Composable
internal fun GhostButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit = {}) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
    }
}
