package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.PriceAlert
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
internal fun TransferIntentCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    quote: EstimatedFeeQuote?,
    purpose: String,
    history: List<TransferDecision>,
    matchingAlert: PriceAlert?,
    isPremium: Boolean,
    onPurposeChange: (String) -> Unit,
    onDecisionSaved: (TransferDecision) -> Unit,
    onCreateAlert: () -> Unit,
    onOpenProviderUrl: (String) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val purposes = if (isPremium) listOf("Family", "Travel", "Invoice", "Savings") else listOf("Family", "Travel")
    val hasAmount = amountValue > 0.0
    var selectedDecision by remember { mutableStateOf<TransferDecision?>(null) }
    var copied by remember { mutableStateOf(false) }
    val currentDecision = remember(sourceRate, targetRate, amountValue, quote, purpose) {
        transferDecision(sourceRate, targetRate, amountValue, quote, purpose)
    }
    if (selectedDecision != null) {
        TransferDecisionSheet(
            decision = selectedDecision ?: currentDecision,
            copied = copied,
            onCopy = {
                clipboard.setText(AnnotatedString((selectedDecision ?: currentDecision).shareText()))
                copied = true
                Observability.event("transfer_decision_copied", mapOf("provider" to (selectedDecision ?: currentDecision).provider))
            },
            onOpenProvider = {
                val url = providerExternalUrl((selectedDecision ?: currentDecision).provider)
                if (url != null) {
                    onOpenProviderUrl(url)
                    Observability.event("transfer_provider_opened", mapOf("provider" to (selectedDecision ?: currentDecision).provider))
                }
            },
            onDismiss = { selectedDecision = null },
        )
    }
    BentoCard(Modifier.testTag("converter_transfer_intent"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("TRANSFER INTENT"), color = FxTheme.colors.accent)
                Pill(if (hasAmount) ui("Ready") else ui("Preview"), variant = if (hasAmount) PillVariant.Up else PillVariant.Ghost)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                purposes.forEach { option ->
                    Pill(
                        text = ui(option),
                        variant = if (purpose == option) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("transfer_purpose_${option.lowercase()}")
                            .clickable {
                                onPurposeChange(option)
                                Observability.event("transfer_intent_purpose_selected", mapOf("purpose" to option))
                            },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(ui("You send"), "${sourceRate.code} ${formatMoneyValue(amountValue)}", ui("Purpose") + " · " + ui(purpose), Modifier.weight(1f).testTag("transfer_intent_send"))
                MetricTile(ui("Receiver gets"), quote?.amount ?: formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)), quote?.provider?.let { ui(it) } ?: ui("Best route now"), Modifier.weight(1f).testTag("transfer_intent_receive"))
            }
            KeyValueRow(
                ui("Best route now"),
                quote?.provider?.let { ui(it) } ?: "--",
                if (hasAmount) {
                    "${ui("Delivery")} ${quote?.deliverySpeed?.let { ui(it) } ?: "--"} · ${ui("Risk")} ${quote?.riskLabel?.let { ui(it) } ?: "--"}"
                } else {
                    ui("Enter an amount to compare real routes.")
                },
                modifier = Modifier.testTag("transfer_intent_best_route"),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton(
                    text = ui("Use this route"),
                    modifier = Modifier.weight(1f).testTag("transfer_intent_use_route"),
                    onClick = {
                        val decision = currentDecision
                        copied = false
                        selectedDecision = decision
                        onDecisionSaved(decision)
                        Observability.event("transfer_intent_route_used", mapOf("provider" to decision.provider))
                    },
                )
                GhostButton(
                    text = if (matchingAlert != null) ui("Better-rate alert active") else ui("Set better-rate alert"),
                    modifier = Modifier.weight(1f).testTag("transfer_intent_set_alert"),
                    onClick = {
                        if (isPremium) {
                            onCreateAlert()
                            Observability.event("transfer_intent_alert_requested", mapOf("target" to targetRate.code))
                        } else {
                            onOpenPaywall()
                        }
                    },
                )
            }
            if (history.isNotEmpty()) {
                SectionLabel(ui("TRANSFER HISTORY"), right = ui("Last decisions"))
                Column(Modifier.testTag("transfer_decision_history"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    history.take(if (isPremium) 5 else 2).forEachIndexed { index, decision ->
                        KeyValueRow(
                            "${decision.sourceCode} ${formatMoneyValue(decision.amountValue)} → ${decision.targetCode}",
                            ui(decision.provider),
                            "${ui("Receiver gets")} ${decision.receiverGets} · ${ui("Risk")} ${ui(decision.riskLabel)}",
                            modifier = Modifier
                                .testTag("transfer_decision_history_$index")
                                .clickable {
                                    copied = false
                                    selectedDecision = decision
                                },
                        )
                    }
                }
            }
        }
    }
}
