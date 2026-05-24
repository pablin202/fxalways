package com.fxalways.app.screens.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.AlertTestNotifier
import com.fxalways.app.NotificationPermissionStatus
import com.fxalways.app.PlatformConfig
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.screens.BackNavButton
import com.fxalways.app.screens.CurrencyPickerSheet
import com.fxalways.app.screens.ScreenScaffold
import com.fxalways.app.screens.alertRates
import com.fxalways.app.screens.compactCurrencyChoices
import com.fxalways.app.screens.formatPercentValue
import com.fxalways.app.screens.parseAmountInput
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.app.screens.shared.ProUpsellCard
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
fun AlertsScreen(
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    showTestAction: Boolean = PlatformConfig.isDebug,
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = { NotificationPermissionStatus.requestIfNeeded() },
    onCreateAlert: (FxRate) -> Unit = {},
    onCreateManualAlert: (FxRate, AlertDirection, Double, AlertKind) -> Unit = { _, _, _, _ -> },
    onResumeAlert: (String) -> Unit = {},
    onToggleAlert: (String) -> Unit = {},
    onDeleteAlert: (String) -> Unit = {},
    onMarkAlertTriggered: (String) -> Unit = {},
    onTestAlert: (PriceAlert) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val canCreate = canCreateAlert(subscriptionState, alertsState.alerts.size)
    val limitLabel = if (access.hasUnlimitedAlerts) ui("Unlimited") else "${alertsState.alerts.size}/${access.alertLimit}"
    val alertRates = remember(
        liveState.baseCurrency,
        liveState.favorites,
        liveState.compare,
        liveState.converter,
        liveState.allFiat,
        liveState.crypto,
        subscriptionState.isPremium,
    ) {
        liveState.alertRates(subscriptionState.isPremium)
    }
    val currentRatesByCode = remember(liveState.baseCurrency, alertRates) {
        alertRates.associateBy { it.code }
    }
    val digestDriver = remember(alertRates) {
        alertRates.maxByOrNull { kotlin.math.abs(it.change24h) }
    }
    val triggeredAlerts = remember(alertsState.alerts) {
        alertsState.alerts
            .filter { it.lastTriggeredAtMillis != null }
            .sortedByDescending { it.lastTriggeredAtMillis }
            .take(4)
    }
    val smartSuggestions = remember(liveState.baseCurrency, alertRates, subscriptionState.isPremium) {
        smartAlertSuggestions(alertRates, subscriptionState.isPremium)
    }
    var selectedRateCode by remember(liveState.baseCurrency) { mutableStateOf(alertRates.firstOrNull()?.code ?: "EUR") }
    val selectedRate = alertRates.firstOrNull { it.code == selectedRateCode } ?: alertRates.firstOrNull() ?: FavoriteRates.first()
    val visibleAlertRates = remember(alertRates, selectedRate.code, subscriptionState.isPremium) {
        compactCurrencyChoices(alertRates, selectedRate.code, if (subscriptionState.isPremium) 8 else 4)
    }
    var showAlertCurrencyPicker by remember { mutableStateOf(false) }
    var selectedKind by remember { mutableStateOf(AlertKind.Target) }
    var selectedDirection by remember { mutableStateOf(AlertDirection.Above) }
    var digestCadence by remember { mutableStateOf("Daily") }
    var targetText by remember(selectedRate.code, selectedDirection, selectedKind) {
        mutableStateOf(defaultAlertInput(selectedRate, selectedDirection, selectedKind))
    }
    val targetValue = parseAmountInput(targetText)
    val selectedDailyChange = selectedRate.change24h
    val matchingCustomAlert = alertsState.alerts.findMatchingAlert(
        baseCurrency = liveState.baseCurrency,
        quote = selectedRate.code,
        target = targetValue,
        direction = selectedDirection,
        kind = selectedKind,
    )
    val canCreateOrUpdate = canCreate || matchingCustomAlert != null
    var customAlertFeedback by remember { mutableStateOf<String?>(null) }
    var customAlertError by remember { mutableStateOf<String?>(null) }
    val existingAlertReactivatedCopy = ui("Existing alert reactivated")
    val alertCreatedCopy = ui("alert created")
    val invalidTargetCopy = ui("Enter a target above 0")
    LaunchedEffect(liveState.baseCurrency, selectedRate.code, selectedDirection, selectedKind, targetText) {
        customAlertFeedback = null
        customAlertError = null
    }
    if (showAlertCurrencyPicker) {
        CurrencyPickerSheet(
            title = ui("Choose alert pair"),
            subtitle = "${alertRates.size} ${ui("currencies")} · ${liveState.baseCurrency} ${ui("base")}",
            currencies = alertRates,
            selectedCode = selectedRate.code,
            onDismiss = { showAlertCurrencyPicker = false },
            onSelect = { code ->
                showAlertCurrencyPicker = false
                selectedRateCode = code
                alertRates.firstOrNull { it.code == code }?.let { rate ->
                    targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
                }
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = ui("More"), onClick = onBack)
        }
        ScreenHeader(ui("Alerts"), sub = ui("PRICE TARGETS"), subtitle = "$limitLabel ${ui("alerts")} · ${liveState.baseCurrency} ${ui("base")}")

        BentoCard(Modifier.fillMaxWidth().heightIn(min = 144.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
                    Pill("${alertsState.activeCount} ${ui("active")}", variant = if (alertsState.activeCount > 0) PillVariant.Up else PillVariant.Ghost)
                }
                Text(ui("Know when a rate is worth acting on."), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    ui("We monitor your saved pairs and notify you when your target is ready."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AlertMonitoringStatusCard(
            activeCount = alertsState.activeCount,
            isPremium = subscriptionState.isPremium,
            modifier = Modifier.testTag("alerts_monitoring_status"),
        )

        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing smart alerts"),
                rows = 5,
                modifier = Modifier.testTag("alerts_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing alert builder"),
                rows = 6,
                modifier = Modifier.testTag("alerts_builder_loading_skeleton"),
            )
        } else {
        SectionLabel(ui("NOTIFICATION DIGEST"), right = if (subscriptionState.isPremium) "FX/ PRO" else ui("Preview"))
        AlertDigestCard(
            activeCount = alertsState.activeCount,
            triggeredCount = triggeredAlerts.size,
            driver = digestDriver,
            cadence = digestCadence,
            isPremium = subscriptionState.isPremium,
            onCadenceSelected = { cadence ->
                if (cadence == "Weekly" && !subscriptionState.isPremium) {
                    onOpenPaywall()
                } else {
                    digestCadence = cadence
                    onRequestNotificationPermission()
                }
            },
            onOpenPaywall = onOpenPaywall,
        )
        AlertActionCenterCard(
            alerts = alertsState.alerts,
            currentRatesByCode = currentRatesByCode,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )

        SmartAlertsSection(
            baseCurrency = liveState.baseCurrency,
            alertsState = alertsState,
            suggestions = smartSuggestions,
            canCreate = canCreate,
            isPremium = subscriptionState.isPremium,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onResumeAlert = onResumeAlert,
            onCreateManualAlert = onCreateManualAlert,
            onOpenPaywall = onOpenPaywall,
        )

        AlertTemplatesSection(
            selectedKind = selectedKind,
            selectedDirection = selectedDirection,
            onTemplateSelected = { template ->
                selectedKind = template.kind
                selectedDirection = template.direction
                targetText = template.targetText(selectedRate)
                Observability.event(
                    "alert_template_selected",
                    mapOf("template" to template.id, "currency" to selectedRate.code),
                )
            },
        )

        CustomAlertSection(
            baseCurrency = liveState.baseCurrency,
            visibleAlertRates = visibleAlertRates,
            selectedRate = selectedRate,
            selectedKind = selectedKind,
            selectedDirection = selectedDirection,
            targetText = targetText,
            targetValue = targetValue,
            selectedDailyChange = selectedDailyChange,
            matchingCustomAlert = matchingCustomAlert,
            canCreate = canCreate,
            customAlertError = customAlertError,
            customAlertFeedback = customAlertFeedback,
            onRateSelected = { rate ->
                selectedRateCode = rate.code
                targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
            },
            onOpenCurrencyPicker = { showAlertCurrencyPicker = true },
            onKindSelected = { kind ->
                selectedKind = kind
                targetText = defaultAlertInput(selectedRate, selectedDirection, kind)
            },
            onDirectionSelected = { direction ->
                selectedDirection = direction
                targetText = defaultAlertInput(selectedRate, direction, selectedKind)
            },
            onPresetSelected = { preset ->
                selectedDirection = if (preset.percent >= 0.0) AlertDirection.Above else AlertDirection.Below
                targetText = if (selectedKind == AlertKind.Target) {
                    formatRate(selectedRate.rate * (1.0 + preset.percent / 100.0))
                } else {
                    formatPercentValue(kotlin.math.abs(preset.percent))
                }
            },
            onTargetTextChanged = { raw ->
                targetText = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
            },
            onCreateClick = {
                if (!canCreateOrUpdate) {
                    onOpenPaywall()
                } else if (targetValue > 0.0) {
                    onRequestNotificationPermission()
                    onCreateManualAlert(selectedRate, selectedDirection, targetValue, selectedKind)
                    customAlertFeedback = if (matchingCustomAlert != null) {
                        "$existingAlertReactivatedCopy ${liveState.baseCurrency}/${selectedRate.code}."
                    } else {
                        "${liveState.baseCurrency}/${selectedRate.code} $alertCreatedCopy."
                    }
                    customAlertError = null
                } else {
                    customAlertError = invalidTargetCopy
                }
            },
        )

        QuickCreateAlertsSection(
            baseCurrency = liveState.baseCurrency,
            rates = liveState.favorites,
            alerts = alertsState.alerts,
            canCreate = canCreate,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onResumeAlert = onResumeAlert,
            onCreateAlert = onCreateAlert,
            onOpenPaywall = onOpenPaywall,
        )

        if (!canCreate) {
            ProUpsellCard(
	                title = ui("Create unlimited alerts"),
	                subtitle = "${ui("Free includes")} ${access.alertLimit}; ${ui("Pro unlocks every pair, range and breakout alert.")}",
                onClick = onOpenPaywall,
            )
        }

        ActiveAlertsSection(
            alertsState = alertsState,
            currentRatesByCode = currentRatesByCode,
            baseCurrency = liveState.baseCurrency,
            showTestAction = showTestAction,
            onToggleAlert = onToggleAlert,
            onDeleteAlert = onDeleteAlert,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onTestAlert = onTestAlert,
            onMarkAlertTriggered = onMarkAlertTriggered,
        )
        SectionLabel(ui("TRIGGER HISTORY"))
        AlertTriggerHistoryCard(
            alerts = triggeredAlerts,
            currentRatesByCode = currentRatesByCode,
            baseCurrency = liveState.baseCurrency,
        )
        }
    }
}

@Composable
private fun AlertMonitoringStatusCard(
    activeCount: Int,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
) {
    BentoCard(modifier.fillMaxWidth(), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("ALERT MONITORING"))
                Pill(if (activeCount > 0) ui("Push active") else ui("Ready"), variant = if (activeCount > 0) PillVariant.Up else PillVariant.Ghost)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(ui("Server checks"), variant = PillVariant.Ghost, modifier = Modifier.weight(1f))
                Pill(ui("Local fallback"), variant = PillVariant.Ghost, modifier = Modifier.weight(1f))
                Pill(if (isPremium) "FX/ PRO" else "FX/ FREE", variant = PillVariant.Ghost, modifier = Modifier.weight(1f))
            }
            Text(
                ui("Alerts are evaluated on the backend and delivered through FCM when available; local checks remain a safety fallback."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
        }
    }
}
