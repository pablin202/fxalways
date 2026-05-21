package com.fxalways.app.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.DetailUiState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun FxAppShellFrame(
    startupReady: Boolean,
    showPaywall: Boolean,
    detailNewsStory: NewsStory?,
    detailRate: FxRate?,
    selectedTab: FxTab,
    moreRoute: MoreRoute,
    liveState: LiveRatesState,
    newsState: NewsUiState,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
    detailState: DetailUiState,
    subscriptionState: SubscriptionState,
    subscriptionReady: Boolean,
    subscriptionActionInProgress: Boolean,
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
    detailStore: DetailStore,
    onSelectTab: (FxTab) -> Unit,
    onOpenMoreRoute: (MoreRoute) -> Unit,
    onOpenPaywall: (String) -> Unit,
    onOpenDetail: (FxRate, String) -> Unit,
    onOpenStory: (NewsStory, String) -> Unit,
    onPaywallVisibleChange: (Boolean) -> Unit,
    onDetailNewsStoryChange: (NewsStory?) -> Unit,
    onDetailRateChange: (FxRate?) -> Unit,
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
    onSubscriptionActionInProgressChange: (Boolean) -> Unit,
    onBackupStateChange: (UserBackupState) -> Unit,
    onBackupReadyChange: (Boolean) -> Unit,
    onBackupSyncingChange: (Boolean) -> Unit,
    onLastSyncedAtMillisChange: (Long?) -> Unit,
) {
    FxTheme(dark = themeMode.resolveDarkMode(isSystemInDarkTheme())) {
        CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
            FxShellContent(
                startupReady = startupReady,
                showPaywall = showPaywall,
                detailNewsStory = detailNewsStory,
                detailRate = detailRate,
                selectedTab = selectedTab,
                moreRoute = moreRoute,
                liveState = liveState,
                newsState = newsState,
                alertsState = alertsState,
                watchlistState = watchlistState,
                detailState = detailState,
                subscriptionState = subscriptionState,
                subscriptionReady = subscriptionReady,
                subscriptionActionInProgress = subscriptionActionInProgress,
                userProfile = userProfile,
                compareCurrencyCodes = compareCurrencyCodes,
                converterCurrencyCodes = converterCurrencyCodes,
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
                detailStore = detailStore,
                onSelectTab = onSelectTab,
                onOpenMoreRoute = onOpenMoreRoute,
                onOpenPaywall = onOpenPaywall,
                onOpenDetail = onOpenDetail,
                onOpenStory = onOpenStory,
                onPaywallVisibleChange = onPaywallVisibleChange,
                onDetailNewsStoryChange = onDetailNewsStoryChange,
                onDetailRateChange = onDetailRateChange,
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
                onSubscriptionActionInProgressChange = onSubscriptionActionInProgressChange,
                onBackupStateChange = onBackupStateChange,
                onBackupReadyChange = onBackupReadyChange,
                onBackupSyncingChange = onBackupSyncingChange,
                onLastSyncedAtMillisChange = onLastSyncedAtMillisChange,
            )
        }
    }
}

private fun ThemeMode.resolveDarkMode(systemDark: Boolean): Boolean =
    when (this) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
