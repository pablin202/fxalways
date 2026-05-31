package com.fxalways.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.DetailUiState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.news.NewsDetailScreen
import com.fxalways.app.screens.paywall.FxPaywallRoute
import com.fxalways.designsystem.components.FxBottomBar
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun FxShellContent(
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
    Column(
        Modifier
            .fillMaxSize()
            .background(FxTheme.colors.bg)
            .safeContentPadding(),
    ) {
        Box(Modifier.weight(1f)) {
            when {
                !startupReady -> StartupLoadingScreen(baseCurrency, appLanguage)
                showPaywall -> FxPaywallRoute(
                    subscriptionState = subscriptionState,
                    actionInProgress = subscriptionActionInProgress,
                    userProfile = userProfile,
                    subscriptionGateway = subscriptionGateway,
                    onClose = { onPaywallVisibleChange(false) },
                    onSubscriptionStateChange = onSubscriptionStateChange,
                    onSubscriptionReadyChange = onSubscriptionReadyChange,
                    onActionInProgressChange = onSubscriptionActionInProgressChange,
                    onPaywallVisibleChange = onPaywallVisibleChange,
                )
                detailNewsStory != null -> NewsDetailScreen(
                    story = detailNewsStory,
                    onBack = { onDetailNewsStoryChange(null) },
                    onOpenUrl = ExternalUrlOpener::open,
                )
                detailRate != null -> FxDetailRoute(
                    rate = detailRate,
                    liveState = liveState,
                    alertsState = alertsState,
                    subscriptionState = subscriptionState,
                    subscriptionReady = subscriptionReady,
                    detailState = detailState,
                    newsState = newsState,
                    alertsStore = alertsStore,
                    detailStore = detailStore,
                    onBack = { onDetailRateChange(null) },
                    onOpenPaywall = onOpenPaywall,
                    onOpenStory = onOpenStory,
                )
                else -> FxMainTabRoute(
                    selectedTab = selectedTab,
                    moreRoute = moreRoute,
                    liveState = liveState,
                    newsState = newsState,
                    alertsState = alertsState,
                    watchlistState = watchlistState,
                    subscriptionState = subscriptionState,
                    subscriptionReady = subscriptionReady,
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
                    onSelectTab = onSelectTab,
                    onOpenMoreRoute = onOpenMoreRoute,
                    onOpenPaywall = onOpenPaywall,
                    onOpenDetail = onOpenDetail,
                    onOpenStory = onOpenStory,
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
            if (startupReady && !subscriptionState.isPremium && !showPaywall && detailNewsStory == null && detailRate == null) {
                FloatingProCta(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp),
                    onClick = { onOpenPaywall("global_pro_cta") },
                )
            }
        }
        if (startupReady) {
            FxBottomBar(
                tabs = FxTab.entries.map { ui(it.label) },
                selectedIndex = selectedTab.ordinal,
                onSelect = { onSelectTab(FxTab.entries[it]) },
                iconKeys = FxTab.entries.map { it.label },
            )
        }
    }
}

@Composable
internal fun FloatingProCta(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .testTag("global_go_pro_cta")
            .clip(FxTheme.shapes.pill)
            .background(FxTheme.colors.crypto.copy(alpha = 0.18f))
            .border(1.dp, FxTheme.colors.crypto.copy(alpha = 0.55f), FxTheme.shapes.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("PRO", style = FxTheme.typography.captionMono, color = FxTheme.colors.crypto)
        Text(ui("Upgrade to Pro"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
    }
}
