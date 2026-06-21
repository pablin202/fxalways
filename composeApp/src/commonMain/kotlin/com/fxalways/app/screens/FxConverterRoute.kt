package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.alerts.canCreateAlert
import com.fxalways.app.screens.alerts.findMatchingAlert
import com.fxalways.app.screens.converter.ConverterScreen
import com.fxalways.observability.Observability

@Composable
internal fun FxConverterRoute(
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState,
    converterCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    alertsStore: AlertsStore,
    onOpenPaywall: (String) -> Unit,
    onConverterCurrencyCodesChange: (List<String>) -> Unit,
) {
    ConverterScreen(
        liveState = liveState,
        alertsState = alertsState,
        subscriptionState = subscriptionState,
        selectedCurrencyCodes = converterCurrencyCodes,
        selectedProviderCodes = providerPreferenceCodes,
        onCurrencyCodesChange = { codes ->
            Observability.event("converter_currencies_changed", mapOf("count" to codes.size.toString()))
            (codes - converterCurrencyCodes.toSet()).forEach { code ->
                Observability.event("currency_added", mapOf("surface" to "converter", "currency" to code))
            }
            onConverterCurrencyCodesChange(codes)
            AppSettingsPrefs.setConverterCurrencyCodes(codes)
        },
        onOpenPaywall = { onOpenPaywall("converter") },
        onOpenPaywallSource = onOpenPaywall,
        onCreateTransferAlert = { source, target, alertTarget ->
            val existing = alertsState.alerts.findMatchingAlert(
                baseCurrency = source.code,
                quote = target.code,
                target = alertTarget,
                direction = AlertDirection.Above,
                kind = AlertKind.Target,
            )
            when {
                existing != null -> {
                    alertsStore.resumeAlert(existing.id)
                    Observability.event("transfer_intent_alert_reactivated", mapOf("source" to source.code, "target" to target.code))
                }
                canCreateAlert(subscriptionState, alertsState.alerts.size) -> {
                    alertsStore.addAlert(source.code, target.code, alertTarget, AlertDirection.Above, AlertKind.Target)
                    Observability.event("transfer_intent_alert_created", mapOf("source" to source.code, "target" to target.code))
                }
                else -> onOpenPaywall("converter_transfer_alert_limit")
            }
        },
        onOpenProviderUrl = ExternalUrlOpener::open,
        enableLiveProviderQuotes = true,
    )
}
