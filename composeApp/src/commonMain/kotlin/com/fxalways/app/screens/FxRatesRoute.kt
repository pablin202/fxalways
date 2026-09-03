package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fxalways.app.data.ExchangeApi
import com.fxalways.app.domain.ProviderQuoteDto
import com.fxalways.app.screens.dashboard.defaultCorridor
import com.fxalways.app.screens.providers.normalizeProviderPreferenceCodes
import com.fxalways.app.screens.providers.quoteCapableProviderCodes
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.alerts.canCreateAlert
import com.fxalways.app.screens.alerts.findMatchingAlert
import com.fxalways.app.screens.dashboard.DashboardScreen
import com.fxalways.app.screens.dashboard.suggestedProfileAlert
import com.fxalways.app.screens.dashboard.suggestedProfileAlertState
import com.fxalways.designsystem.components.FxRate
import com.fxalways.observability.Observability

@Composable
internal fun FxRatesRoute(
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState,
    compareCurrencyCodes: List<String>,
    userProfile: UserProfile,
    liveStore: LiveRatesStore,
    alertsStore: AlertsStore,
    onSelectTab: (FxTab) -> Unit,
    onOpenMoreRoute: (MoreRoute) -> Unit,
    onOpenPaywall: (String) -> Unit,
    onOpenDetail: (FxRate, String) -> Unit,
    onCompareCurrencyCodesChange: (List<String>) -> Unit,
    providerPreferenceCodes: List<String> = emptyList(),
) {
    if (liveState.errorMessage != null && !liveState.isLive) {
        OfflineScreen(
            liveState,
            onRefresh = {
                Observability.event("rates_refresh", mapOf("source" to "offline"))
                liveStore.refresh()
            },
        )
    } else {
        val corridor = remember { AppSettingsPrefs.corridor() }
        val decisionCorridor = corridor ?: userProfile.defaultCorridor(liveState.baseCurrency)
        var backendQuotes by remember { mutableStateOf(emptyList<ProviderQuoteDto>()) }
        val quoteApi = remember { ExchangeApi() }
        LaunchedEffect(userProfile, liveState.baseCurrency, decisionCorridor.target, decisionCorridor.amount, subscriptionState.isPremium, providerPreferenceCodes) {
            if (userProfile != UserProfile.Remittances && userProfile != UserProfile.Freelancer) return@LaunchedEffect
            if (decisionCorridor.target == liveState.baseCurrency || decisionCorridor.amount <= 0.0) return@LaunchedEffect
            val codes = normalizeProviderPreferenceCodes(providerPreferenceCodes, liveState.baseCurrency, decisionCorridor.target).quoteCapableProviderCodes()
            backendQuotes = runCatching {
                quoteApi.providerQuotes(liveState.baseCurrency, decisionCorridor.target, decisionCorridor.amount, codes, subscriptionState.isPremium).quotes
            }.getOrDefault(emptyList())
        }
        val createSuggestedAlert: () -> Unit = {
                val suggestion = suggestedProfileAlert(
                    profile = userProfile,
                    liveState = liveState,
                    isPremium = subscriptionState.isPremium,
                    corridor = corridor,
                )
                if (suggestion == null) {
                    onOpenMoreRoute(MoreRoute.Alerts)
                    onSelectTab(FxTab.More)
                } else {
                    val existing = alertsState.alerts.findMatchingAlert(
                        baseCurrency = liveState.baseCurrency,
                        quote = suggestion.rate.code,
                        target = suggestion.target,
                        direction = suggestion.direction,
                        kind = suggestion.kind,
                    )
                    when {
                        existing != null -> {
                            Observability.event("profile_alert_reactivated", mapOf("profile" to userProfile.name, "currency" to suggestion.rate.code))
                            alertsStore.resumeAlert(existing.id)
                            onSelectTab(FxTab.More)
                            onOpenMoreRoute(MoreRoute.Alerts)
                        }
                        canCreateAlert(subscriptionState, alertsState.alerts.size) -> {
                            Observability.event("profile_alert_created", mapOf("profile" to userProfile.name, "currency" to suggestion.rate.code, "kind" to suggestion.kind.name))
                            alertsStore.addAlert(liveState.baseCurrency, suggestion.rate.code, suggestion.target, suggestion.direction, suggestion.kind)
                            onSelectTab(FxTab.More)
                            onOpenMoreRoute(MoreRoute.Alerts)
                        }
                        else -> onOpenPaywall("dashboard_profile_alert_limit")
                    }
                }
        }
        DashboardScreen(
            liveState = liveState,
            subscriptionState = subscriptionState,
            trackedCurrencyCodes = compareCurrencyCodes,
            userProfile = userProfile,
            suggestedProfileAlertState = suggestedProfileAlertState(
                profile = userProfile,
                liveState = liveState,
                isPremium = subscriptionState.isPremium,
                alerts = alertsState.alerts,
                corridor = corridor,
            ),
            onRefresh = {
                Observability.event("rates_refresh", mapOf("source" to "dashboard"))
                liveStore.refresh()
            },
            onOpenPaywall = { onOpenPaywall("dashboard") },
            onOpenDetail = { onOpenDetail(it, "dashboard") },
            onEditFavorites = {
                if (subscriptionState.isPremium) {
                    onSelectTab(FxTab.More)
                    onOpenMoreRoute(MoreRoute.Watchlist)
                } else {
                    onOpenPaywall("dashboard_favorites")
                }
            },
            onSeeAllCrypto = {
                val cryptoCodes = liveState.visibleDashboardCryptoRates(subscriptionState.isPremium, compareCurrencyCodes).map { it.code }
                if (cryptoCodes.isNotEmpty()) {
                    Observability.event("dashboard_crypto_see_all", mapOf("count" to cryptoCodes.size.toString()))
                    onCompareCurrencyCodesChange(cryptoCodes)
                    AppSettingsPrefs.setCompareCurrencyCodes(cryptoCodes)
                    onSelectTab(FxTab.Compare)
                }
            },
            onCreateSuggestedAlert = createSuggestedAlert,
            onCreateCorridorAlert = {
                Observability.event("send_decision_alert_created", mapOf("base" to liveState.baseCurrency, "target" to decisionCorridor.target))
                createSuggestedAlert()
            },
            corridor = corridor,
            providerPreferenceCodes = providerPreferenceCodes,
            backendProviderQuotes = backendQuotes,
            onOpenConverter = { onSelectTab(FxTab.Convert) },
            onOpenTraveler = {
                onSelectTab(FxTab.More)
                onOpenMoreRoute(MoreRoute.Traveler)
            },
            onOpenWatchlist = {
                onSelectTab(FxTab.More)
                onOpenMoreRoute(MoreRoute.Watchlist)
            },
        )
    }
}
