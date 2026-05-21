package com.fxalways.app.screens.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.screens.SettingChoiceRow
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun SmartAlertsSection(
    baseCurrency: String,
    alertsState: AlertsState,
    suggestions: List<SmartAlertSuggestion>,
    canCreate: Boolean,
    isPremium: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onResumeAlert: (String) -> Unit,
    onCreateManualAlert: (FxRate, AlertDirection, Double, AlertKind) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    SectionLabel(ui("SMART ALERTS"), right = if (isPremium) "FX/ PRO" else ui("Preview"))
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (suggestions.isEmpty()) {
                Text(
                    ui("No smart alert signals yet"),
                    modifier = Modifier.testTag("alert_smart_empty"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            } else {
                suggestions.forEach { suggestion ->
                    val existingSmartAlert = alertsState.alerts.findMatchingAlert(
                        baseCurrency = baseCurrency,
                        quote = suggestion.rate.code,
                        target = suggestion.target,
                        direction = suggestion.direction,
                        kind = suggestion.kind,
                    )
                    val canUseSuggestion = existingSmartAlert != null || canCreate
                    SmartAlertRow(
                        baseCurrency = baseCurrency,
                        suggestion = suggestion,
                        state = when {
                            existingSmartAlert?.enabled == true -> QuickAlertState.Active
                            existingSmartAlert != null -> QuickAlertState.Paused
                            canCreate -> QuickAlertState.Create
                            else -> QuickAlertState.Locked
                        },
                        enabled = canUseSuggestion,
                        onCreate = {
                            onRequestNotificationPermission()
                            if (existingSmartAlert != null) {
                                onResumeAlert(existingSmartAlert.id)
                            } else {
                                onCreateManualAlert(suggestion.rate, suggestion.direction, suggestion.target, suggestion.kind)
                            }
                        },
                        onLocked = onOpenPaywall,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlertTemplatesSection(
    selectedKind: AlertKind,
    selectedDirection: AlertDirection,
    onTemplateSelected: (AlertTemplate) -> Unit,
) {
    SectionLabel(ui("ALERT TEMPLATES"))
    BentoCard(Modifier.testTag("alert_templates"), padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            alertTemplates.forEachIndexed { index, template ->
                SettingChoiceRow(
                    title = ui(template.title),
                    subtitle = ui(template.subtitle),
                    selected = selectedKind == template.kind && selectedDirection == template.direction,
                    actionLabel = ui("Apply"),
                    modifier = Modifier.testTag("alert_template_$index"),
                    onClick = { onTemplateSelected(template) },
                )
            }
        }
    }
}

@Composable
internal fun QuickCreateAlertsSection(
    baseCurrency: String,
    rates: List<FxRate>,
    alerts: List<PriceAlert>,
    canCreate: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onResumeAlert: (String) -> Unit,
    onCreateAlert: (FxRate) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    SectionLabel(ui("QUICK CREATE"))
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rates.take(4).forEach { rate ->
                val quickAlert = alerts.findQuickAlert(baseCurrency, rate)
                val canCreateQuick = quickAlert != null || canCreate
                AlertQuickRow(
                    baseCurrency = baseCurrency,
                    rate = rate,
                    state = when {
                        quickAlert?.enabled == true -> QuickAlertState.Active
                        quickAlert != null -> QuickAlertState.Paused
                        canCreate -> QuickAlertState.Create
                        else -> QuickAlertState.Locked
                    },
                    enabled = canCreateQuick,
                    onCreate = {
                        onRequestNotificationPermission()
                        if (quickAlert != null) {
                            onResumeAlert(quickAlert.id)
                        } else {
                            onCreateAlert(rate)
                        }
                    },
                    onLocked = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
internal fun ActiveAlertsSection(
    alertsState: AlertsState,
    currentRatesByCode: Map<String, FxRate>,
    baseCurrency: String,
    showTestAction: Boolean,
    onToggleAlert: (String) -> Unit,
    onDeleteAlert: (String) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onTestAlert: (PriceAlert) -> Unit,
    onMarkAlertTriggered: (String) -> Unit,
) {
    SectionLabel(ui("ACTIVE ALERTS"))
    if (alertsState.alerts.isEmpty()) {
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Eyebrow(ui("NO ALERTS YET"))
                Text(ui("Create one from a favorite currency or from any detail screen."), style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            alertsState.alerts.forEach { alert ->
                val currentRate = currentRatesByCode[alert.quote]?.rate.takeIf { alert.base == baseCurrency }
                AlertCard(
                    alert = alert,
                    currentRate = currentRate,
                    currentChangePct = currentRatesByCode[alert.quote]?.change24h.takeIf { alert.base == baseCurrency },
                    onToggle = onToggleAlert,
                    onDelete = onDeleteAlert,
                    showTestAction = showTestAction,
                    onTest = {
                        onRequestNotificationPermission()
                        onTestAlert(it)
                        onMarkAlertTriggered(it.id)
                    },
                )
            }
        }
    }
}
