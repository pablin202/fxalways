package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant

@Composable
internal fun RemittancePlannerCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    quote: EstimatedFeeQuote?,
    cadence: String,
    isPremium: Boolean,
    onCadenceChange: (String) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val cadenceMultiplier = when (cadence) {
        "Biweekly" -> 26
        "One-time" -> 1
        else -> 12
    }
    val yearlyLoss = (quote?.lossTargetValue ?: 0.0) * cadenceMultiplier
    val nextSendWindow = when (cadence) {
        "Biweekly" -> "Next 14 days"
        "One-time" -> "This week"
        else -> "Before payday"
    }
    val planConfidence = when {
        quote == null || amountValue <= 0.0 -> "Needs amount"
        quote.lossPercentValue < 1.0 -> "Good route"
        quote.lossPercentValue < 3.0 -> "Watch fees"
        else -> "Avoid route"
    }
    val cadenceOptions = if (isPremium) {
        listOf("One-time", "Monthly", "Biweekly")
    } else {
        listOf("One-time", "Monthly")
    }
    BentoCard(Modifier.testTag("converter_remittance_planner"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Family route"),
                    "${sourceRate.code} → ${targetRate.code}",
                    quote?.provider?.let { ui(it) } ?: ui("based on current best route"),
                    Modifier.weight(1f).testTag("remittance_family_route"),
                )
                MetricTile(
                    ui("Recipient estimate"),
                    quote?.amount ?: formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)),
                    ui("based on current best route"),
                    Modifier.weight(1f).testTag("remittance_recipient_estimate"),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cadenceOptions.forEach { option ->
                    Pill(
                        ui(option),
                        variant = if (cadence == option) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("remittance_cadence_$option")
                            .clickable { onCadenceChange(option) },
                    )
                }
            }
            KeyValueRow(
                ui("Recurring amount"),
                "${sourceRate.code} ${formatMoneyValue(amountValue * cadenceMultiplier)} / year",
                "${ui(cadence)} · ${quote?.loss ?: "${targetRate.code} 0.00"} ${ui("vs mid-market")}",
                modifier = Modifier.testTag("remittance_recurring_amount"),
            )
            KeyValueRow(
                ui("Reminder cadence"),
                if (isPremium) ui("Before payday") else ui("Monthly"),
                if (isPremium) "${ui("Family route")} · ${sourceRate.code}/${targetRate.code}" else ui("Pro unlocks reminder planning and extra cadences."),
                modifier = Modifier.testTag("remittance_reminder_cadence"),
            )
            KeyValueRow(
                ui("Next send window"),
                ui(nextSendWindow),
                "${ui("Plan confidence")} · ${ui(planConfidence)}",
                modifier = Modifier.testTag("remittance_next_window"),
            )
            KeyValueRow(
                ui("Annual fee drag"),
                "${targetRate.code} ${formatMoneyValue(yearlyLoss)}",
                "${cadenceMultiplier} ${ui("planned sends")} · ${ui("estimated from current route")}",
                modifier = Modifier.testTag("remittance_annual_fee_drag"),
            )
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks reminder planning and extra cadences."),
                    modifier = Modifier.fillMaxWidth().testTag("remittance_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}
