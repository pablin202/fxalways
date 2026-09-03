package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.compare.CompareScreen
import com.fxalways.app.screens.news.NewsScreen
import com.fxalways.designsystem.components.FxRate
import com.fxalways.observability.Observability

@Composable
internal fun FxMainTabRoute(
    selectedTab: FxTab,
    moreRoute: MoreRoute,
    liveState: LiveRatesState,
    newsState: NewsUiState,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
    subscriptionState: SubscriptionState,
    subscriptionReady: Boolean,
    userProfile: UserProfile,
    compareCurrencyCodes: List<String>,
    converterCurrencyCodes: List<String>,
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
    onOpenStory: (NewsStory, String) -> Unit,
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
    when (selectedTab) {
        FxTab.Rates -> FxRatesRoute(
            liveState = liveState,
            alertsState = alertsState,
            subscriptionState = subscriptionState,
            compareCurrencyCodes = compareCurrencyCodes,
            userProfile = userProfile,
            liveStore = liveStore,
            alertsStore = alertsStore,
            onSelectTab = onSelectTab,
            onOpenMoreRoute = onOpenMoreRoute,
            onOpenPaywall = onOpenPaywall,
            onOpenDetail = onOpenDetail,
            onCompareCurrencyCodesChange = onCompareCurrencyCodesChange,
            providerPreferenceCodes = providerPreferenceCodes,
        )
        FxTab.Convert -> FxConverterRoute(
            liveState = liveState,
            alertsState = alertsState,
            subscriptionState = subscriptionState,
            converterCurrencyCodes = converterCurrencyCodes,
            providerPreferenceCodes = providerPreferenceCodes,
            alertsStore = alertsStore,
            onOpenPaywall = onOpenPaywall,
            onConverterCurrencyCodesChange = onConverterCurrencyCodesChange,
        )
        FxTab.Compare -> CompareScreen(
            liveState = liveState,
            subscriptionState = subscriptionState,
            selectedCurrencyCodes = compareCurrencyCodes,
            onCurrencyCodesChange = { codes ->
                Observability.event("compare_currencies_changed", mapOf("count" to codes.size.toString()))
                (codes - compareCurrencyCodes.toSet()).forEach { code ->
                    Observability.event("currency_added", mapOf("surface" to "compare", "currency" to code))
                }
                onCompareCurrencyCodesChange(codes)
                AppSettingsPrefs.setCompareCurrencyCodes(codes)
            },
            onOpenPaywall = { onOpenPaywall("compare") },
            onOpenDetail = { onOpenDetail(it, "compare") },
        )
        FxTab.News -> NewsScreen(
            newsState = newsState,
            subscriptionState = subscriptionState,
            onRefresh = {
                Observability.event("news_refresh")
                newsStore.refresh()
            },
            onRegionSelected = newsStore::setRegion,
            onCurrencySelected = newsStore::setCurrency,
            onOpenStory = { onOpenStory(it, "news") },
            onOpenPaywall = { onOpenPaywall("news") },
        )
        FxTab.More -> FxMoreRoute(
            moreRoute = moreRoute,
            liveState = liveState,
            alertsState = alertsState,
            watchlistState = watchlistState,
            subscriptionState = subscriptionState,
            userProfile = userProfile,
            converterCurrencyCodes = converterCurrencyCodes,
            compareCurrencyCodes = compareCurrencyCodes,
            providerPreferenceCodes = providerPreferenceCodes,
            travelerCurrency = travelerCurrency,
            travelerBudgetBase = travelerBudgetBase,
            themeMode = themeMode,
            appLanguage = appLanguage,
            baseCurrency = baseCurrency,
            backupState = backupState,
            backupSyncing = backupSyncing,
            lastSyncedAtMillis = lastSyncedAtMillis,
            subscriptionGateway = subscriptionGateway,
            liveStore = liveStore,
            newsStore = newsStore,
            alertsStore = alertsStore,
            watchlistStore = watchlistStore,
            onSelectTab = onSelectTab,
            onOpenMoreRoute = onOpenMoreRoute,
            onOpenPaywall = onOpenPaywall,
            onOpenDetail = onOpenDetail,
            onMoreRouteChange = onMoreRouteChange,
            onCompareCurrencyCodesChange = onCompareCurrencyCodesChange,
            onConverterCurrencyCodesChange = onConverterCurrencyCodesChange,
            onProviderPreferenceCodesChange = onProviderPreferenceCodesChange,
            onTravelerCurrencyChange = onTravelerCurrencyChange,
            onTravelerBudgetBaseChange = onTravelerBudgetBaseChange,
            onThemeModeChange = onThemeModeChange,
            onLanguageChange = onLanguageChange,
            onBaseCurrencyChange = onBaseCurrencyChange,
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
