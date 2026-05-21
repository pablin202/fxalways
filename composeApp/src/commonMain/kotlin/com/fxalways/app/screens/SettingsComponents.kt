package com.fxalways.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun SettingChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    actionLabel: String = if (selected) ui("active") else ui("select"),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else Color.Transparent)
            .border(if (selected) 1.dp else 0.dp, if (selected) FxTheme.colors.accentLine else Color.Transparent, FxTheme.shapes.field)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp).testTag("settings_link_account_loading"),
                color = FxTheme.colors.accent,
                strokeWidth = 2.dp,
            )
        }
        Pill(actionLabel, variant = if (selected || isLoading) PillVariant.Accent else PillVariant.Ghost)
    }
}

@Composable
internal fun userFriendlyNetworkError(message: String?): String {
    if (message.isNullOrBlank()) {
        return ui("Please check your connection and try again.")
    }
    return when {
        message.contains("network", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("interrupted", ignoreCase = true) ||
            message.contains("unreachable", ignoreCase = true) -> ui("Please check your connection and try again.")
        message.contains("RevenueCat", ignoreCase = true) -> ui("Purchases are temporarily unavailable. Try again later.")
        else -> message
    }
}
