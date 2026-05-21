package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import com.fxalways.app.AlertTestNotifier
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.alerts.AlertsScreen
import com.fxalways.app.screens.alerts.canCreateAlert
import com.fxalways.app.screens.alerts.findMatchingAlert
import com.fxalways.app.screens.alerts.findQuickAlert
import com.fxalways.app.screens.more.MoreScreen
import com.fxalways.app.screens.settings.FxSettingsRoute
import com.fxalways.app.screens.traveler.TravelerScreen
import com.fxalways.app.screens.watchlist.WatchlistScreen
import com.fxalways.designsystem.components.FxRate
import com.fxalways.observability.Observability

@Composable
internal fun FxMoreRoute(
    moreRoute: MoreRoute,
    liveState: LiveRatesState,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
    subscriptionState: SubscriptionState,
    userProfile: UserProfile,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    backupState: UserBackupState,
    backupSyncing: Boolean,
    lastSyncedAtMillis: Long?,
    subscriptionGateway: SubscriptionGateway,
    liveStore: LiveRatesStore,
    newsStore: NewsStore,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    onSelectTab: (FxTab) -> Unit,
    onOpenMoreRoute: (MoreRoute) -> Unit,
    onOpenPaywall: (String) -> Unit,
    onOpenDetail: (FxRate, String) -> Unit,
    onMoreRouteChange: (MoreRoute) -> Unit,
    onCompareCurrencyCodesChange: (List<String>) -> Unit,
    onConverterCurrencyCodesChange: (List<String>) -> Unit,
    onProviderPreferenceCodesChange: (List<String>) -> Unit,
    onTravelerCurrencyChange: (String) -> Unit,
    onTravelerBudgetBaseChange: (Double) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onUserProfileChange: (UserProfile) -> Unit,
    onSubscriptionStateChange: (SubscriptionState) -> Unit,
    onSubscriptionReadyChange: (Boolean) -> Unit,
    onBackupStateChange: (UserBackupState) -> Unit,
    onBackupReadyChange: (Boolean) -> Unit,
    onBackupSyncingChange: (Boolean) -> Unit,
    onLastSyncedAtMillisChange: (Long?) -> Unit,
) {
    when (moreRoute) {
        MoreRoute.Menu -> MoreScreen(
            subscriptionState = subscriptionState,
            alertsCount = alertsState.activeCount,
            watchlistCount = watchlistState.watchlist.codes.size,
            onOpenAlerts = { onOpenMoreRoute(MoreRoute.Alerts) },
            onOpenWatchlist = { onOpenMoreRoute(MoreRoute.Watchlist) },
            onOpenTraveler = { onOpenMoreRoute(MoreRoute.Traveler) },
            onOpenSettings = { onOpenMoreRoute(MoreRoute.Settings) },
            onOpenNews = { onSelectTab(FxTab.News) },
            onOpenPaywall = { onOpenPaywall("more") },
        )
        MoreRoute.Alerts -> AlertsScreen(
            liveState = liveState,
            alertsState = alertsState,
            subscriptionState = subscriptionState,
            onBack = { onMoreRouteChange(MoreRoute.Menu) },
            onOpenPaywall = { onOpenPaywall("alerts") },
            onCreateAlert = { rate ->
                if (
                    canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                    alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate) != null
                ) {
                    Observability.event("alert_created", mapOf("type" to "quick", "currency" to rate.code))
                    alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                } else {
                    onOpenPaywall("alert_limit")
                }
            },
            onCreateManualAlert = { rate, direction, target, kind ->
                if (
                    canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                    alertsState.alerts.findMatchingAlert(liveState.baseCurrency, rate.code, target, direction, kind) != null
                ) {
                    Observability.event("alert_created", mapOf("type" to "manual", "currency" to rate.code))
                    alertsStore.addAlert(liveState.baseCurrency, rate.code, target, direction, kind)
                } else {
                    onOpenPaywall("alert_limit")
                }
            },
            onResumeAlert = alertsStore::resumeAlert,
            onToggleAlert = alertsStore::toggleAlert,
            onDeleteAlert = alertsStore::deleteAlert,
            onMarkAlertTriggered = alertsStore::markTriggered,
            onTestAlert = AlertTestNotifier::show,
        )
        MoreRoute.Watchlist -> WatchlistScreen(
            liveState = liveState,
            watchlistState = watchlistState,
            subscriptionState = subscriptionState,
            onBack = { onMoreRouteChange(MoreRoute.Menu) },
            onOpenPaywall = { onOpenPaywall("watchlist") },
            onToggleCurrency = { code ->
                val selected = code in watchlistState.watchlist.codes
                val canAdd = selected ||
                    subscriptionState.featureAccess().hasUnlimitedWatchlistCurrencies ||
                    watchlistState.watchlist.codes.size < subscriptionState.featureAccess().watchlistCurrencyLimit
                if (!watchlistStore.toggle(code, canAdd)) {
                    onOpenPaywall("watchlist_limit")
                } else {
                    Observability.event("watchlist_toggle", mapOf("currency" to code))
                }
            },
            onSetHolding = watchlistStore::setHolding,
            onSetHoldingCost = watchlistStore::setHoldingCost,
            onRecordTransaction = watchlistStore::recordTransaction,
            onImportPortfolioCsv = watchlistStore::importPortfolioCsv,
            onOpenDetail = { onOpenDetail(it, "watchlist") },
        )
        MoreRoute.Traveler -> TravelerScreen(
            liveState = liveState,
            subscriptionState = subscriptionState,
            selectedCurrency = travelerCurrency,
            budgetBase = travelerBudgetBase,
            onBack = { onMoreRouteChange(MoreRoute.Menu) },
            onCurrencySelected = { code ->
                Observability.event("traveler_currency_changed", mapOf("currency" to code))
                onTravelerCurrencyChange(code)
                AppSettingsPrefs.setTravelerCurrency(code)
            },
            onBudgetChange = { amount ->
                Observability.event("traveler_budget_changed")
                onTravelerBudgetBaseChange(amount)
                AppSettingsPrefs.setTravelerBudgetBase(amount)
            },
            onOpenPaywall = { onOpenPaywall("traveler") },
        )
        MoreRoute.Settings -> FxSettingsRoute(
            themeMode = themeMode,
            appLanguage = appLanguage,
            baseCurrency = baseCurrency,
            travelerCurrency = travelerCurrency,
            travelerBudgetBase = travelerBudgetBase,
            converterCurrencyCodes = converterCurrencyCodes,
            compareCurrencyCodes = compareCurrencyCodes,
            providerPreferenceCodes = providerPreferenceCodes,
            userProfile = userProfile,
            liveState = liveState,
            alertsState = alertsState,
            watchlistState = watchlistState,
            backupState = backupState,
            backupSyncing = backupSyncing,
            lastSyncedAtMillis = lastSyncedAtMillis,
            subscriptionState = subscriptionState,
            subscriptionGateway = subscriptionGateway,
            liveStore = liveStore,
            alertsStore = alertsStore,
            watchlistStore = watchlistStore,
            newsStore = newsStore,
            onBack = { onMoreRouteChange(MoreRoute.Menu) },
            onOpenPaywall = { onOpenPaywall("settings") },
            onThemeModeChange = onThemeModeChange,
            onLanguageChange = onLanguageChange,
            onBaseCurrencyChange = onBaseCurrencyChange,
            onTravelerCurrencyChange = onTravelerCurrencyChange,
            onTravelerBudgetBaseChange = onTravelerBudgetBaseChange,
            onConverterCurrencyCodesChange = onConverterCurrencyCodesChange,
            onCompareCurrencyCodesChange = onCompareCurrencyCodesChange,
            onProviderPreferenceCodesChange = onProviderPreferenceCodesChange,
            onUserProfileChange = onUserProfileChange,
            onSubscriptionStateChange = onSubscriptionStateChange,
            onSubscriptionReadyChange = onSubscriptionReadyChange,
            onBackupStateChange = onBackupStateChange,
            onBackupReadyChange = onBackupReadyChange,
            onBackupSyncingChange = onBackupSyncingChange,
            onLastSyncedAtMillisChange = onLastSyncedAtMillisChange,
        )
    }
}
