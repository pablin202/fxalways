package com.fxalways.app.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.screens.GhostButton
import com.fxalways.app.screens.formatSignedPercent
import com.fxalways.app.screens.localizedShortAgeLabel
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
internal fun AlertCard(
    alert: PriceAlert,
    currentRate: Double?,
    currentChangePct: Double?,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    showTestAction: Boolean,
    onTest: (PriceAlert) -> Unit,
) {
    val isHit = alert.isHit(currentRate, currentChangePct)
    BentoCard(modifier = Modifier.testTag("alert_card_${alert.id}"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                FlagDot(if (alert.kind == AlertKind.Target) "◎" else "%", CurrencyKind.Fiat, 32.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
                        "${ui(alert.direction.label(alert.kind))} ${alert.targetLabel()} · ${localizedAlertStatusLabel(alert, currentRate, currentChangePct)}",
                        style = FxTheme.typography.captionMono,
                        color = if (isHit) FxTheme.colors.up else FxTheme.colors.textFaint,
                    )
                }
                Pill(if (alert.enabled) ui("on") else ui("paused"), variant = if (alert.enabled) PillVariant.Up else PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetricTile(
                    if (alert.kind == AlertKind.Target) ui("CURRENT") else ui("24H MOVE"),
                    if (alert.kind == AlertKind.Target) currentRate?.let(::formatRate) ?: "--" else currentChangePct?.let(::formatSignedPercent) ?: "--",
                    localizedAlertDistanceLabel(alert, currentRate, currentChangePct),
                    Modifier.weight(1f).height(72.dp),
                )
                MetricTile(
                    ui("LAST HIT"),
                    alert.lastTriggeredAtMillis?.let { localizedShortAgeLabel(it) } ?: ui("Never"),
                    if (alert.enabled) ui("monitoring") else ui("paused"),
                    Modifier.weight(1f).height(72.dp),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (alert.enabled) ui("pause") else ui("resume"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textDim,
                    modifier = Modifier
                        .testTag("alert_toggle_${alert.id}")
                        .clickable { onToggle(alert.id) },
                )
                Spacer(Modifier.width(14.dp))
                if (showTestAction) {
                    Text(
                        ui("test"),
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.accent,
                        modifier = Modifier
                            .testTag("alert_test_${alert.id}")
                            .clickable { onTest(alert) },
                    )
                    Spacer(Modifier.width(14.dp))
                }
                Text(
                    "×",
                    style = FxTheme.typography.titleL,
                    color = FxTheme.colors.textFaint,
                    modifier = Modifier
                        .testTag("alert_delete_${alert.id}")
                        .clickable { onDelete(alert.id) },
                )
            }
        }
    }
}

@Composable
internal fun AlertActionCenterCard(
    alerts: List<PriceAlert>,
    currentRatesByCode: Map<String, FxRate>,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val decisionAlert = alerts
        .sortedWith(compareByDescending<PriceAlert> { it.lastTriggeredAtMillis ?: 0L }.thenByDescending { it.enabled })
        .firstOrNull()
    val currentRate = decisionAlert?.let { currentRatesByCode[it.quote]?.rate }
    val currentChange = decisionAlert?.let { currentRatesByCode[it.quote]?.change24h }
    val isHit = decisionAlert?.isHit(currentRate, currentChange) == true
    BentoCard(Modifier.testTag("alert_action_center"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("ACTION CENTER"), color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Preview"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            if (decisionAlert == null) {
                KeyValueRow(
                    ui("No alert has fired yet"),
                    ui("Set next alert"),
                    ui("Create alerts first; fired alerts will become concrete decisions here."),
                    modifier = Modifier.testTag("alert_action_empty"),
                )
            } else {
                KeyValueRow(
                    if (isHit) ui("Alert fired") else ui("Recommended next step"),
                    "${decisionAlert.base}/${decisionAlert.quote} · ${ui(decisionAlert.direction.label(decisionAlert.kind))} ${decisionAlert.targetLabel()}",
                    if (isHit) ui("Review provider cost before moving money.") else localizedAlertDistanceLabel(decisionAlert, currentRate, currentChange),
                    modifier = Modifier.testTag("alert_action_decision"),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostButton(
                        text = ui("Convert now"),
                        modifier = Modifier.weight(1f).testTag("alert_action_convert"),
                        onClick = {
                            if (isPremium) {
                                Observability.event("alert_action_convert", mapOf("quote" to decisionAlert.quote))
                            } else {
                                onOpenPaywall()
                            }
                        },
                    )
                    GhostButton(
                        text = ui("Set next alert"),
                        modifier = Modifier.weight(1f).testTag("alert_action_next"),
                        onClick = { Observability.event("alert_action_next_alert", mapOf("quote" to decisionAlert.quote)) },
                    )
                }
                GhostButton(
                    text = ui("Share decision"),
                    modifier = Modifier.fillMaxWidth().testTag("alert_action_share"),
                    onClick = { Observability.event("alert_action_share", mapOf("quote" to decisionAlert.quote, "hit" to isHit.toString())) },
                )
            }
        }
    }
}

@Composable
internal fun AlertDigestCard(
    activeCount: Int,
    triggeredCount: Int,
    driver: FxRate?,
    cadence: String,
    isPremium: Boolean,
    onCadenceSelected: (String) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    BentoCard(Modifier.testTag("alert_digest"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily", "Weekly").forEach { option ->
                    val locked = option == "Weekly" && !isPremium
                    Pill(
                        text = if (locked) "${ui(option)} · Pro" else ui(option),
                        variant = if (cadence == option && !locked) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("alert_digest_${option.lowercase()}")
                            .clickable { onCadenceSelected(option) },
                    )
                }
            }
            KeyValueRow(
                ui("Digest includes"),
                ui("Active alerts and recent hits"),
                "${activeCount} ${ui("active")} · $triggeredCount ${ui("Last hit").lowercase()}",
                modifier = Modifier.testTag("alert_digest_includes"),
            )
            KeyValueRow(
                if (cadence == "Weekly") ui("Weekly digest") else ui("Daily digest"),
                driver?.let { "${it.code} ${formatSignedPercent(it.change24h)}" } ?: "--",
                if (cadence == "Weekly") ui("Weekly digest groups active alerts, hits and largest watched move.") else ui("Daily digest highlights active alerts, latest hits and today's largest move."),
                modifier = Modifier.testTag("alert_digest_driver"),
            )
            KeyValueRow(
                ui("Next reminder"),
                if (cadence == "Weekly") ui("Monday morning") else ui("Tomorrow morning"),
                driver?.let { "${ui("Watch")} ${it.code} · ${formatSignedPercent(it.change24h)}" } ?: ui("Add alerts to personalize digest."),
                modifier = Modifier.testTag("alert_digest_next_reminder"),
            )
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks weekly digest."),
                    modifier = Modifier.fillMaxWidth().testTag("alert_digest_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
internal fun AlertTriggerHistoryCard(
    alerts: List<PriceAlert>,
    currentRatesByCode: Map<String, FxRate>,
    baseCurrency: String,
) {
    BentoCard(Modifier.testTag("alert_trigger_history"), padding = 8.dp) {
        if (alerts.isEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(ui("No alert hits yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    ui("Triggered alerts will appear here after Android checks rates."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                alerts.forEach { alert ->
                    AlertHistoryRow(
                        alert = alert,
                        currentRate = currentRatesByCode[alert.quote]?.rate.takeIf { alert.base == baseCurrency },
                        currentChangePct = currentRatesByCode[alert.quote]?.change24h.takeIf { alert.base == baseCurrency },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryRow(
    alert: PriceAlert,
    currentRate: Double?,
    currentChangePct: Double?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("alert_history_${alert.id}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(if (alert.kind == AlertKind.Target) "◎" else "%", CurrencyKind.Fiat, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(
                "${ui("Alert triggered")} · ${ui(alert.direction.label(alert.kind))} ${alert.targetLabel()}",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                localizedAlertDistanceLabel(alert, currentRate, currentChangePct),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(ui("Last hit"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Text(
                alert.lastTriggeredAtMillis?.let { localizedShortAgeLabel(it) } ?: ui("Never"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.text,
            )
        }
    }
}
