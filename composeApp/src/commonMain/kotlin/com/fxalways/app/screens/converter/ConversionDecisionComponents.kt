package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.fxalways.observability.Observability
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun ConversionDecisionCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    convertedAmount: Double,
    timingInsight: SmartTimingInsight,
    bestRoute: EstimatedFeeQuote?,
    isPremium: Boolean,
    onCreateAlert: () -> Unit,
    onCompareProviders: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    LaunchedEffect(sourceRate.code, targetRate.code, timingInsight.signal) {
        Observability.event(
            "send_decision_viewed",
            mapOf("base" to sourceRate.code, "target" to targetRate.code, "signal" to timingInsight.signal.lowercase().replace(' ', '_'), "plan" to if (isPremium) "pro" else "free"),
        )
    }
    val primaryAction = when {
        amountValue <= 0.0 -> "Enter an amount"
        timingInsight.signal == "Wait" -> "Wait or set an alert"
        else -> "Convert now"
    }
    val routeStatus = bestRoute?.sourceStatusLabel() ?: "Needs amount"
    BentoCard(Modifier.testTag("converter_decision_card"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Eyebrow(ui("CONVERSION DECISION"), color = FxTheme.colors.accent)
                Pill(ui(primaryAction), variant = if (primaryAction == "Convert now") PillVariant.Up else PillVariant.Ghost)
            }
            Text(
                "${sourceRate.code} ${formatMoneyValue(amountValue)} → ${formatConvertedAmount(targetRate, convertedAmount)}",
                style = FxTheme.typography.numberL,
                color = FxTheme.colors.text,
                modifier = Modifier.testTag("converter_decision_amount"),
            )
            KeyValueRow(
                ui("Recommended next step"),
                ui(primaryAction),
                ui(timingInsight.action),
                modifier = Modifier.testTag("converter_decision_recommendation"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton(
                    text = ui("Set better-rate alert"),
                    modifier = Modifier.weight(1f).testTag("converter_decision_alert"),
                    onClick = onCreateAlert,
                )
                GhostButton(
                    text = if (isPremium) ui("Compare providers") else ui("Unlock full comparison"),
                    modifier = Modifier.weight(1f).testTag("converter_decision_compare"),
                    onClick = if (isPremium) onCompareProviders else onOpenPaywall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Timing"),
                    ui(timingInsight.signal),
                    "${timingInsight.score}/100",
                    Modifier.weight(1f).testTag("converter_decision_timing"),
                )
                MetricTile(
                    ui("Best route"),
                    bestRoute?.let { ui(it.provider) } ?: ui("None yet"),
                    ui(routeStatus),
                    Modifier.weight(1f).testTag("converter_decision_route"),
                )
            }
            KeyValueRow(
                ui("Decision reason"),
                ui(timingInsight.action),
                bestRoute?.let { "${ui("Provider rates can differ")} · ${ui(it.sourceTrustLabel())}" }
                    ?: ui("Enter an amount to compare real routes."),
                modifier = Modifier.testTag("converter_decision_reason"),
            )
        }
    }
}
