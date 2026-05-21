package com.fxalways.app.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.PlatformBackHandler
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupGateway
import com.fxalways.app.UserBackupState
import com.fxalways.app.isDefaultLocalBackup
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.createSubscriptionGateway
import com.fxalways.app.screens.profile.preset
import com.fxalways.app.screens.settings.applyUserBackupSnapshot
import com.fxalways.app.screens.settings.buildUserBackupSnapshot
import com.fxalways.app.screens.providers.defaultProviderPreferenceCodes
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
fun FxAppShell() {
    val initialProfile = remember { AppSettingsPrefs.userProfile() }
    val initialPreset = remember(initialProfile) { initialProfile.preset() }
    var selectedTab by remember { mutableStateOf(FxTab.Rates) }
    var moreRoute by remember { mutableStateOf(MoreRoute.Menu) }
    var detailRate by remember { mutableStateOf<FxRate?>(null) }
    var detailNewsStory by remember { mutableStateOf<NewsStory?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var subscriptionActionInProgress by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(AppSettingsPrefs.themeMode()) }
    var appLanguage by remember { mutableStateOf(AppSettingsPrefs.language()) }
    var baseCurrency by remember { mutableStateOf(AppSettingsPrefs.baseCurrency()) }
    var travelerCurrency by remember { mutableStateOf(AppSettingsPrefs.travelerCurrency()) }
    var travelerBudgetBase by remember { mutableStateOf(AppSettingsPrefs.travelerBudgetBase()) }
    var converterCurrencyCodes by remember { mutableStateOf(AppSettingsPrefs.converterCurrencyCodes()) }
    var compareCurrencyCodes by remember { mutableStateOf(AppSettingsPrefs.compareCurrencyCodes()) }
    var providerPreferenceCodes by remember { mutableStateOf(AppSettingsPrefs.providerPreferenceCodes().ifEmpty { defaultProviderPreferenceCodes(baseCurrency) }) }
    var userProfile by remember { mutableStateOf(AppSettingsPrefs.userProfile()) }
    val liveStore = remember { LiveRatesStore(initialBaseCurrency = baseCurrency) }
    val newsStore = remember { NewsStore(initialLanguage = appLanguage) }
    val alertsStore = remember { AlertsStore() }
    val watchlistStore = remember { WatchlistStore() }
    val detailStore = remember { DetailStore() }
    val subscriptionGateway = remember { createSubscriptionGateway() }
    val cachedPremium = remember { AppSettingsPrefs.cachedPremium() }
    var subscriptionReady by remember { mutableStateOf(cachedPremium != null) }
    var subscriptionState by remember { mutableStateOf(SubscriptionState(isPremium = cachedPremium == true)) }
    var backupState by remember { mutableStateOf(UserBackupState()) }
    var backupReady by remember { mutableStateOf(false) }
    var backupSyncing by remember { mutableStateOf(false) }
    var startupReady by remember { mutableStateOf(false) }
    var lastSyncedAtMillis by remember { mutableStateOf<Long?>(null) }
    val liveState by liveStore.state.collectAsState()
    val newsState by newsStore.state.collectAsState()
    val alertsState by alertsStore.state.collectAsState()
    val watchlistState by watchlistStore.state.collectAsState()
    val detailState by detailStore.state.collectAsState()
    LaunchedEffect(Unit) {
        if (converterCurrencyCodes.isEmpty()) {
            converterCurrencyCodes = initialPreset.converterCodes
            AppSettingsPrefs.setConverterCurrencyCodes(converterCurrencyCodes)
        }
        if (compareCurrencyCodes.isEmpty()) {
            compareCurrencyCodes = initialPreset.compareCodes
            AppSettingsPrefs.setCompareCurrencyCodes(compareCurrencyCodes)
        }
        if (watchlistState.watchlist == Watchlist()) {
            watchlistStore.replaceFromBackup(Watchlist(codes = initialPreset.watchlistCodes))
        }
        if (travelerCurrency == "JPY" && initialPreset.travelerCurrency != "JPY") {
            travelerCurrency = initialPreset.travelerCurrency
            AppSettingsPrefs.setTravelerCurrency(travelerCurrency)
        }
        if (AppSettingsPrefs.converterAmountText() == "1000" && initialPreset.suggestedAmount != "1000") {
            AppSettingsPrefs.setConverterAmountText(initialPreset.suggestedAmount)
        }
        if (AppSettingsPrefs.providerPreferenceCodes().isEmpty()) {
            AppSettingsPrefs.setProviderPreferenceCodes(providerPreferenceCodes)
        }
    }
    fun selectTab(tab: FxTab) {
        showPaywall = false
        detailRate = null
        detailNewsStory = null
        selectedTab = tab
        Observability.event("tab_selected", mapOf("tab" to tab.label))
        if (tab != FxTab.More) {
            moreRoute = MoreRoute.Menu
        }
    }
    fun openPaywall(source: String) {
        Observability.event("paywall_opened", mapOf("source" to source))
        showPaywall = true
    }
    fun openDetail(rate: FxRate, source: String) {
        Observability.event("currency_detail_opened", mapOf("source" to source, "currency" to rate.code))
        detailRate = rate
    }
    fun openStory(story: NewsStory, source: String) {
        Observability.event("news_story_opened", mapOf("source" to source, "tag" to story.tag))
        detailNewsStory = story
    }
    fun openMoreRoute(route: MoreRoute) {
        Observability.event("more_route_opened", mapOf("route" to route.analyticsName))
        moreRoute = route
    }
    LaunchedEffect(selectedTab, moreRoute, detailRate, detailNewsStory, showPaywall, startupReady) {
        if (startupReady) {
            val screenName = when {
                showPaywall -> "paywall"
                detailNewsStory != null -> "news_detail"
                detailRate != null -> "currency_detail"
                selectedTab == FxTab.More -> moreRoute.analyticsName
                else -> selectedTab.label
            }
            Observability.screen(
                screenName,
                mapOf(
                    "tab" to selectedTab.label,
                    "base_currency" to baseCurrency,
                    "language" to appLanguage,
                ),
            )
        }
    }
    LaunchedEffect(subscriptionState.isPremium, backupState.uid) {
        Observability.setUserId(backupState.uid)
        Observability.setUserProperty("premium", subscriptionState.isPremium.toString())
    }
    LaunchedEffect(liveStore) {
        liveStore.startAutoRefresh()
    }
    LaunchedEffect(startupReady, baseCurrency) {
        if (startupReady) {
            newsStore.setCurrency(baseCurrency)
        }
    }
    LaunchedEffect(Unit) {
        subscriptionState = subscriptionGateway.currentState()
        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
        subscriptionReady = true
        backupState = UserBackupGateway.ensureUser()
        if (backupState.isAvailable) {
            runCatching {
                val localSnapshot = buildUserBackupSnapshot(
                    themeMode,
                    appLanguage,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    compareCurrencyCodes,
                    providerPreferenceCodes,
                    userProfile,
                    alertsState,
                    watchlistState,
                )
                val remoteSnapshot = UserBackupGateway.pullSnapshot()
                if (remoteSnapshot != null && localSnapshot.isDefaultLocalBackup()) {
                    themeMode = applyUserBackupSnapshot(
                        snapshot = remoteSnapshot,
                        alertsStore = alertsStore,
                        watchlistStore = watchlistStore,
                        liveStore = liveStore,
                        onConverterCurrencyCodes = { converterCurrencyCodes = it },
                        onCompareCurrencyCodes = { compareCurrencyCodes = it },
                        onProviderPreferenceCodes = { providerPreferenceCodes = it.ifEmpty { defaultProviderPreferenceCodes(baseCurrency) } },
                        onTravelerCurrency = { travelerCurrency = it },
                        onTravelerBudgetBase = { travelerBudgetBase = it },
                        onUserProfile = { userProfile = it },
                        onLanguage = {
                            appLanguage = it
                            newsStore.setLanguage(it)
                        },
                    )
                    baseCurrency = remoteSnapshot.settings.baseCurrency
                } else if (remoteSnapshot == null) {
                    UserBackupGateway.pushSnapshot(localSnapshot)
                    lastSyncedAtMillis = localSnapshot.updatedAtMillis
                } else {
                    lastSyncedAtMillis = remoteSnapshot.updatedAtMillis
                }
            }.onFailure { error ->
                Observability.recordException(error, mapOf("flow" to "startup_backup"))
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
            }
        }
        backupReady = backupState.isAvailable
        startupReady = true
    }
    LaunchedEffect(themeMode, appLanguage, baseCurrency, travelerCurrency, travelerBudgetBase, converterCurrencyCodes, compareCurrencyCodes, providerPreferenceCodes, userProfile, alertsState, watchlistState, backupReady) {
        if (backupReady) {
            runCatching {
                val snapshot = buildUserBackupSnapshot(
                    themeMode,
                    appLanguage,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    compareCurrencyCodes,
                    providerPreferenceCodes,
                    userProfile,
                    alertsState,
                    watchlistState,
                )
                UserBackupGateway.pushSnapshot(snapshot)
                lastSyncedAtMillis = snapshot.updatedAtMillis
            }.onFailure { error ->
                Observability.recordException(error, mapOf("flow" to "auto_backup_sync"))
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
                backupReady = false
            }
        }
    }
    PlatformBackHandler(enabled = showPaywall || detailNewsStory != null || detailRate != null || selectedTab == FxTab.More && moreRoute != MoreRoute.Menu) {
        when {
            showPaywall -> showPaywall = false
            detailNewsStory != null -> detailNewsStory = null
            detailRate != null -> detailRate = null
            selectedTab == FxTab.More && moreRoute != MoreRoute.Menu -> moreRoute = MoreRoute.Menu
        }
    }
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    FxTheme(dark = dark) {
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
	            onSelectTab = ::selectTab,
	            onOpenMoreRoute = ::openMoreRoute,
	            onOpenPaywall = ::openPaywall,
	            onOpenDetail = ::openDetail,
	            onOpenStory = ::openStory,
	            onPaywallVisibleChange = { showPaywall = it },
	            onDetailNewsStoryChange = { detailNewsStory = it },
	            onDetailRateChange = { detailRate = it },
	            onMoreRouteChange = { moreRoute = it },
	            onCompareCurrencyCodesChange = { compareCurrencyCodes = it },
	            onConverterCurrencyCodesChange = { converterCurrencyCodes = it },
	            onProviderPreferenceCodesChange = { providerPreferenceCodes = it },
	            onTravelerCurrencyChange = { travelerCurrency = it },
	            onTravelerBudgetBaseChange = { travelerBudgetBase = it },
	            onThemeModeChange = { themeMode = it },
	            onLanguageChange = { appLanguage = it },
	            onBaseCurrencyChange = { baseCurrency = it },
	            onUserProfileChange = { userProfile = it },
	            onSubscriptionStateChange = { subscriptionState = it },
	            onSubscriptionReadyChange = { subscriptionReady = it },
	            onSubscriptionActionInProgressChange = { subscriptionActionInProgress = it },
	            onBackupStateChange = { backupState = it },
	            onBackupReadyChange = { backupReady = it },
	            onBackupSyncingChange = { backupSyncing = it },
	            onLastSyncedAtMillisChange = { lastSyncedAtMillis = it },
	        )
    }
}
}
