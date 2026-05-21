package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransferDecisionSheet(
    decision: TransferDecision,
    copied: Boolean,
    onCopy: () -> Unit,
    onOpenProvider: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val providerUrl = providerExternalUrl(decision.provider)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FxTheme.colors.surface1,
        contentColor = FxTheme.colors.text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .testTag("transfer_decision_sheet")
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(ui("TRANSFER DECISION"), color = FxTheme.colors.accent)
                    Text(ui(decision.provider), style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                }
                Pill(ui(decision.purpose), variant = PillVariant.Accent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(ui("You send"), "${decision.sourceCode} ${formatMoneyValue(decision.amountValue)}", ui("Purpose") + " · " + ui(decision.purpose), Modifier.weight(1f))
                MetricTile(ui("Receiver gets"), decision.receiverGets, ui(decision.provider), Modifier.weight(1f))
            }
            KeyValueRow(ui("Estimated loss"), decision.loss, "${ui("Effective rate")} ${decision.effectiveRate}")
            KeyValueRow(ui("Delivery"), ui(decision.deliverySpeed), "${ui("Payment")} ${ui(decision.paymentMethod)} · ${ui("Risk")} ${ui(decision.riskLabel)}")
            KeyValueRow(ui("External provider"), if (providerUrl == null) ui("Not connected yet") else ui("Open provider"), ui("Provider links can be enabled later with affiliate or deep-link URLs."))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton(
                    text = if (copied) ui("Copied decision") else ui("Copy decision"),
                    modifier = Modifier.weight(1f).testTag("transfer_decision_copy"),
                    onClick = onCopy,
                )
                GhostButton(
                    text = if (providerUrl == null) ui("Provider link pending") else ui("Open provider"),
                    modifier = Modifier.weight(1f).testTag("transfer_decision_provider"),
                    onClick = {
                        if (providerUrl != null) {
                            onOpenProvider()
                        }
                    },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

internal data class TransferDecision(
    val id: String,
    val sourceCode: String,
    val targetCode: String,
    val amountValue: Double,
    val purpose: String,
    val provider: String,
    val receiverGets: String,
    val loss: String,
    val effectiveRate: String,
    val deliverySpeed: String,
    val paymentMethod: String,
    val riskLabel: String,
    val createdAtMillis: Long,
)

internal fun transferDecision(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    quote: EstimatedFeeQuote?,
    purpose: String,
): TransferDecision {
    val fallbackAmount = formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate))
    val provider = quote?.provider ?: "Mid-market"
    val createdAt = Clock.System.now().toEpochMilliseconds()
    return TransferDecision(
        id = "${sourceRate.code}-${targetRate.code}-$createdAt",
        sourceCode = sourceRate.code,
        targetCode = targetRate.code,
        amountValue = amountValue,
        purpose = purpose,
        provider = provider,
        receiverGets = quote?.amount ?: fallbackAmount,
        loss = quote?.loss ?: "${targetRate.code} 0.00",
        effectiveRate = quote?.effectiveRate ?: "${formatRate(if (sourceRate.rate == 0.0) 0.0 else targetRate.rate / sourceRate.rate)} ${targetRate.code}",
        deliverySpeed = quote?.deliverySpeed ?: "Instant",
        paymentMethod = quote?.paymentMethod ?: "Debit/bank",
        riskLabel = quote?.riskLabel ?: "Low",
        createdAtMillis = createdAt,
    )
}

internal fun transferAlertTarget(sourceRate: FxRate, targetRate: FxRate): Double {
    val currentPairRate = if (sourceRate.rate == 0.0) targetRate.rate else targetRate.rate / sourceRate.rate
    return currentPairRate * 1.01
}

internal fun providerExternalUrl(provider: String): String? =
    when (provider) {
        // Keep these disabled until real affiliate/deep-link URLs are configured.
        else -> null
    }

internal fun TransferDecision.shareText(): String = buildString {
    append("FX Always transfer decision\n")
    append("Route: $provider\n")
    append("Pair: $sourceCode/$targetCode\n")
    append("You send: $sourceCode ${formatMoneyValue(amountValue)}\n")
    append("Receiver gets: $receiverGets\n")
    append("Estimated loss: $loss\n")
    append("Effective rate: $effectiveRate\n")
    append("Delivery: $deliverySpeed · Payment: $paymentMethod · Risk: $riskLabel")
}
