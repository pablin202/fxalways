package com.fxalways.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun TransactionChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(1.dp, if (selected) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = FxTheme.typography.captionMono, color = if (selected) FxTheme.colors.accent else FxTheme.colors.textDim)
    }
}
