package com.fxalways.app.screens.alerts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.screens.GhostButton
import com.fxalways.app.screens.PrimaryButton
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun CustomAlertSection(
    baseCurrency: String,
    visibleAlertRates: List<FxRate>,
    selectedRate: FxRate,
    selectedKind: AlertKind,
    selectedDirection: AlertDirection,
    targetText: String,
    targetValue: Double,
    selectedDailyChange: Double,
    matchingCustomAlert: PriceAlert?,
    canCreate: Boolean,
    customAlertError: String?,
    customAlertFeedback: String?,
    onRateSelected: (FxRate) -> Unit,
    onOpenCurrencyPicker: () -> Unit,
    onKindSelected: (AlertKind) -> Unit,
    onDirectionSelected: (AlertDirection) -> Unit,
    onPresetSelected: (AlertPreset) -> Unit,
    onTargetTextChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    SectionLabel(ui("CUSTOM ALERT"))
    BentoCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Eyebrow("$baseCurrency ${ui("PAIR")}")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleAlertRates.chunked(2).forEach { rowRates ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowRates.forEach { rate ->
                            AlertCurrencyChoice(
                                rate = rate,
                                selected = rate.code == selectedRate.code,
                                modifier = Modifier
                                    .clickable { onRateSelected(rate) }
                                    .weight(1f),
                            )
                        }
                        if (rowRates.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            GhostButton(
                text = "≡  ${ui("Choose alert pair")}",
                modifier = Modifier.fillMaxWidth().testTag("alert_choose_pair"),
                onClick = onOpenCurrencyPicker,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertKind.entries.forEach { kind ->
                    Pill(
                        text = ui(kind.label),
                        variant = if (kind == selectedKind) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("alert_kind_${kind.name}")
                            .clickable { onKindSelected(kind) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertDirection.entries.forEach { direction ->
                    Pill(
                        text = ui(direction.label(selectedKind)),
                        variant = if (direction == selectedDirection) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("alert_direction_${direction.name}")
                            .clickable { onDirectionSelected(direction) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                alertPresets.forEach { preset ->
                    Pill(
                        text = preset.label,
                        variant = PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("alert_preset_${preset.label}")
                            .clickable { onPresetSelected(preset) },
                    )
                }
            }
            AlertTargetField(
                value = targetText,
                onValueChange = onTargetTextChanged,
                pair = "$baseCurrency/${selectedRate.code}",
                label = if (selectedKind == AlertKind.Target) ui("Target rate") else ui("Daily move %"),
            )
            PrimaryButton(
                text = when {
                    matchingCustomAlert?.enabled == true -> ui("Keep existing alert active")
                    matchingCustomAlert != null -> ui("Reactivate existing alert")
                    canCreate -> "${ui("Create")} ${ui(selectedDirection.label(selectedKind)).lowercase()} ${ui("alert")}"
                    else -> ui("Unlock custom alerts")
                },
                modifier = Modifier.fillMaxWidth().testTag("alert_create_button"),
                // Dismiss the keyboard so the new card (and the paywall) are not hidden behind the IME on small phones.
                onClick = { focusManager.clearFocus(); onCreateClick() },
            )
            customAlertError?.let { error ->
                Text(
                    error,
                    modifier = Modifier.testTag("alert_target_error"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.down,
                )
            }
            customAlertFeedback?.let { feedback ->
                Text(
                    feedback,
                    modifier = Modifier.testTag("alert_feedback"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                )
            }
            Text(
                localizedAlertSummaryLine(selectedKind, selectedRate, selectedDirection, targetValue, selectedDailyChange),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
            )
        }
    }
}
