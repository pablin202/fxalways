package com.fxalways.app.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.screens.localizedCurrencyName
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

internal data class SmartAlertSuggestion(
    val rate: FxRate,
    val title: String,
    val subtitle: String,
    val target: Double,
    val direction: AlertDirection,
    val kind: AlertKind = AlertKind.Target,
    val strength: Double,
) {
    val strengthLabel: String
        get() = "${(strength * 100.0).toInt()}%"
}

@Composable
internal fun AlertTargetField(
    value: String,
    onValueChange: (String) -> Unit,
    pair: String,
    label: String,
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("$label · $pair", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.testTag("alert_target_input"),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text("0.0000", style = FxTheme.typography.numberL, color = FxTheme.colors.textGhost)
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
internal fun AlertCurrencyChoice(
    rate: FxRate,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .testTag("alert_currency_${rate.code}")
            .heightIn(min = 54.dp)
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(
                1.dp,
                if (selected) FxTheme.colors.accentLine else FxTheme.colors.border,
                FxTheme.shapes.field,
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 26.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = if (selected) FxTheme.colors.accent else FxTheme.colors.text)
            Text(
                localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Text("✓", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.accent)
        }
    }
}

@Composable
internal fun AlertQuickRow(
    baseCurrency: String,
    rate: FxRate,
    state: QuickAlertState,
    enabled: Boolean,
    onCreate: () -> Unit,
    onLocked: () -> Unit,
) {
    val chipFocusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_quick_${rate.code}")
            .clip(FxTheme.shapes.field)
            .clickable(onClick = { chipFocusManager.clearFocus(); if (enabled) onCreate() else onLocked() })
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("$baseCurrency / ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text("${ui("Above")} ${formatRate(rate.rate * 1.01)} · ${ui("current")} ${formatRate(rate.rate)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        Pill(ui(state.label), variant = state.variant)
    }
}

@Composable
internal fun SmartAlertRow(
    baseCurrency: String,
    suggestion: SmartAlertSuggestion,
    state: QuickAlertState,
    enabled: Boolean,
    onCreate: () -> Unit,
    onLocked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_smart_${suggestion.rate.code}")
            .clip(FxTheme.shapes.field)
            .clickable(onClick = if (enabled) onCreate else onLocked)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(suggestion.rate.glyph, suggestion.rate.kind, 30.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "$baseCurrency / ${suggestion.rate.code}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                ui(suggestion.title),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${ui(suggestion.direction.label(suggestion.kind))} ${formatRate(suggestion.target)} · ${ui(suggestion.subtitle)}",
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Pill(ui(state.label), variant = state.variant)
            Text(suggestion.strengthLabel, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}
