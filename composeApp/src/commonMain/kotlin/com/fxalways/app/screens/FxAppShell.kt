package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserProfile
import com.fxalways.app.UserBackupState
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.createSubscriptionGateway
import com.fxalways.app.screens.profile.preset
import com.fxalways.app.screens.providers.defaultProviderPreferenceCodes
import com.fxalways.designsystem.components.FxRate
import com.fxalways.app.ui.supportedLanguageOrDefault
import com.fxalways.app.PendingNavigation
import com.fxalways.observability.Observability

@Composable
fun FxAppShell() {
    val initialProfile = remember { AppSettingsPrefs.userProfile() }
    val initialPreset = remember(initialProfile) { initialProfile.preset() }
    var selectedTab by remember { mutableStateOf(initialPreset.initialTab) }
    var moreRoute by remember { mutableStateOf(initialPreset.moreRoute) }
    var detailRate by remember { mutableStateOf<FxRate?>(null) }
    var detailNewsStory by remember { mutableStateOf<NewsStory?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var paywallSource by remember { mutableStateOf("unknown") }
    var subscriptionActionInProgress by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(AppSettingsPrefs.themeMode()) }
    var appLanguage by remember { mutableStateOf(supportedLanguageOrDefault(AppSettingsPrefs.language())) }
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
    FxAppPresetDefaultsEffect(
        initialPreset = initialPreset,
        converterCurrencyCodes = converterCurrencyCodes,
        compareCurrencyCodes = compareCurrencyCodes,
        providerPreferenceCodes = providerPreferenceCodes,
        travelerCurrency = travelerCurrency,
        watchlistState = watchlistState,
        watchlistStore = watchlistStore,
        onConverterCurrencyCodesChange = { converterCurrencyCodes = it },
        onCompareCurrencyCodesChange = { compareCurrencyCodes = it },
        onTravelerCurrencyChange = { travelerCurrency = it },
    )
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
        paywallSource = source
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
        if (route == MoreRoute.Alerts) {
            // Alerts is a main tab now; keep old call sites working.
            selectedTab = FxTab.Alerts
            moreRoute = MoreRoute.Menu
        } else {
            moreRoute = route
        }
    }
    val pendingNavigation by PendingNavigation.source.collectAsState()
    LaunchedEffect(pendingNavigation) {
        val source = pendingNavigation ?: return@LaunchedEffect
        val (tab, route) = widgetSourceDestination(source)
        selectTab(tab)
        if (route != MoreRoute.Menu) openMoreRoute(route)
        PendingNavigation.consume()
    }
    FxAppScreenTrackingEffect(
        selectedTab = selectedTab,
        moreRoute = moreRoute,
        detailRateVisible = detailRate != null,
        detailNewsStoryVisible = detailNewsStory != null,
        showPaywall = showPaywall,
        startupReady = startupReady,
        baseCurrency = baseCurrency,
        appLanguage = appLanguage,
    )
    FxAppUserTrackingEffect(subscriptionState, backupState, userProfile, baseCurrency)
    FxAppRateRefreshEffect(liveStore)
    FxAppNewsCurrencyEffect(startupReady, baseCurrency, newsStore)
    FxAppStartupBackupEffect(
        themeMode = themeMode,
        appLanguage = appLanguage,
        baseCurrency = baseCurrency,
        travelerCurrency = travelerCurrency,
        travelerBudgetBase = travelerBudgetBase,
        converterCurrencyCodes = converterCurrencyCodes,
        compareCurrencyCodes = compareCurrencyCodes,
        providerPreferenceCodes = providerPreferenceCodes,
        userProfile = userProfile,
        alertsState = alertsState,
        watchlistState = watchlistState,
        subscriptionGateway = subscriptionGateway,
        alertsStore = alertsStore,
        watchlistStore = watchlistStore,
        liveStore = liveStore,
        newsStore = newsStore,
        onSubscriptionStateChange = { subscriptionState = it },
        onSubscriptionReadyChange = { subscriptionReady = it },
        onBackupStateChange = { backupState = it },
        onBackupReadyChange = { backupReady = it },
        onStartupReadyChange = { startupReady = it },
        onLastSyncedAtMillisChange = { lastSyncedAtMillis = it },
        onThemeModeChange = { themeMode = it },
        onLanguageChange = { appLanguage = it },
        onBaseCurrencyChange = { baseCurrency = it },
        onConverterCurrencyCodesChange = { converterCurrencyCodes = it },
        onCompareCurrencyCodesChange = { compareCurrencyCodes = it },
        onProviderPreferenceCodesChange = { providerPreferenceCodes = it },
        onTravelerCurrencyChange = { travelerCurrency = it },
        onTravelerBudgetBaseChange = { travelerBudgetBase = it },
        onUserProfileChange = { userProfile = it },
    )
    FxAppAutoBackupEffect(
        themeMode = themeMode,
        appLanguage = appLanguage,
        baseCurrency = baseCurrency,
        travelerCurrency = travelerCurrency,
        travelerBudgetBase = travelerBudgetBase,
        converterCurrencyCodes = converterCurrencyCodes,
        compareCurrencyCodes = compareCurrencyCodes,
        providerPreferenceCodes = providerPreferenceCodes,
        userProfile = userProfile,
        alertsState = alertsState,
        watchlistState = watchlistState,
        backupReady = backupReady,
        backupState = backupState,
        onBackupStateChange = { backupState = it },
        onBackupReadyChange = { backupReady = it },
        onLastSyncedAtMillisChange = { lastSyncedAtMillis = it },
    )
    FxAppBackHandler(
        showPaywall = showPaywall,
        detailNewsStoryVisible = detailNewsStory != null,
        detailRateVisible = detailRate != null,
        selectedTab = selectedTab,
        moreRoute = moreRoute,
        onClosePaywall = { showPaywall = false },
        onCloseNewsStory = { detailNewsStory = null },
        onCloseDetail = { detailRate = null },
        onCloseMoreRoute = { moreRoute = MoreRoute.Menu },
    )
    FxAppShellFrame(
        startupReady = startupReady,
        showPaywall = showPaywall,
        paywallSource = paywallSource,
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
