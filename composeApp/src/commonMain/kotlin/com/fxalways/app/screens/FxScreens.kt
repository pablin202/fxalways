package com.fxalways.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.AlertTestNotifier
import com.fxalways.app.BackupSettings
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig
import com.fxalways.app.ThemeMode
import com.fxalways.app.PlatformBackHandler
import com.fxalways.app.UserBackupGateway
import com.fxalways.app.UserBackupSnapshot
import com.fxalways.app.UserBackupState
import com.fxalways.app.isDefaultLocalBackup
import com.fxalways.app.data.mock.CompareRates
import com.fxalways.app.data.mock.ConverterRates
import com.fxalways.app.data.mock.CryptoRates
import com.fxalways.app.data.mock.DetailSeries
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.DetailUiState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.SettingsBaseCurrencies
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.subscription.SubscriptionPlan
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.createSubscriptionGateway
import com.fxalways.app.subscription.featureAccess
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BentoTile
import com.fxalways.designsystem.components.BigValueText
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.CurrencyRow
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxBottomBar
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.LiveDot
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Period
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.PriceChart
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.SegmentedPeriods
import com.fxalways.designsystem.components.SparkLine
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class FxTab(val label: String) {
    Rates("Rates"),
    Convert("Convert"),
    Compare("Compare"),
    News("News"),
    More("More"),
}

private enum class MoreRoute {
    Menu,
    Alerts,
    Watchlist,
    Traveler,
    Settings,
}

@Composable
fun FxAppShell() {
    var selectedTab by remember { mutableStateOf(FxTab.Rates) }
    var moreRoute by remember { mutableStateOf(MoreRoute.Menu) }
    var detailRate by remember { mutableStateOf<FxRate?>(null) }
    var detailNewsStory by remember { mutableStateOf<NewsStory?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(AppSettingsPrefs.themeMode()) }
    var baseCurrency by remember { mutableStateOf(AppSettingsPrefs.baseCurrency()) }
    var travelerCurrency by remember { mutableStateOf(AppSettingsPrefs.travelerCurrency()) }
    var travelerBudgetBase by remember { mutableStateOf(AppSettingsPrefs.travelerBudgetBase()) }
    var converterCurrencyCodes by remember { mutableStateOf(AppSettingsPrefs.converterCurrencyCodes()) }
    val liveStore = remember { LiveRatesStore(initialBaseCurrency = baseCurrency) }
    val newsStore = remember { NewsStore() }
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
    val scope = rememberCoroutineScope()
    val liveState by liveStore.state.collectAsState()
    val newsState by newsStore.state.collectAsState()
    val alertsState by alertsStore.state.collectAsState()
    val watchlistState by watchlistStore.state.collectAsState()
    val detailState by detailStore.state.collectAsState()
    fun selectTab(tab: FxTab) {
        showPaywall = false
        detailRate = null
        detailNewsStory = null
        selectedTab = tab
        if (tab != FxTab.More) {
            moreRoute = MoreRoute.Menu
        }
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
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
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
                        onTravelerCurrency = { travelerCurrency = it },
                        onTravelerBudgetBase = { travelerBudgetBase = it },
                    )
                    baseCurrency = remoteSnapshot.settings.baseCurrency
                } else if (remoteSnapshot == null) {
                    UserBackupGateway.pushSnapshot(localSnapshot)
                    lastSyncedAtMillis = localSnapshot.updatedAtMillis
                } else {
                    lastSyncedAtMillis = remoteSnapshot.updatedAtMillis
                }
            }.onFailure { error ->
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
            }
        }
        backupReady = backupState.isAvailable
        startupReady = true
    }
    LaunchedEffect(themeMode, baseCurrency, travelerCurrency, travelerBudgetBase, converterCurrencyCodes, alertsState, watchlistState, backupReady) {
        if (backupReady) {
            runCatching {
                val snapshot = buildUserBackupSnapshot(
                    themeMode,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    alertsState,
                    watchlistState,
                )
                UserBackupGateway.pushSnapshot(snapshot)
                lastSyncedAtMillis = snapshot.updatedAtMillis
            }.onFailure { error ->
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
        Column(
            Modifier
                .fillMaxSize()
                .background(FxTheme.colors.bg)
                .safeContentPadding(),
        ) {
            Box(Modifier.weight(1f)) {
                if (!startupReady) {
                    StartupLoadingScreen(baseCurrency)
                } else if (showPaywall) {
                    PaywallScreen(
                        subscriptionState = subscriptionState,
                        onClose = { showPaywall = false },
                        onStart = { planKind ->
                            scope.launch {
                                subscriptionState = subscriptionGateway.purchasePlan(planKind)
                                AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                subscriptionReady = true
                                showPaywall = !subscriptionState.isPremium
                            }
                        },
                        onRestore = {
                            scope.launch {
                                subscriptionState = subscriptionGateway.restore()
                                AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                subscriptionReady = true
                                showPaywall = !subscriptionState.isPremium
                            }
                        },
                    )
                } else if (detailNewsStory != null) {
                    NewsDetailScreen(
                        story = detailNewsStory,
                        onBack = { detailNewsStory = null },
                        onOpenUrl = ExternalUrlOpener::open,
                    )
                } else if (detailRate != null) {
                    DetailScreen(
                        liveState = liveState,
                        alertsState = alertsState,
                        subscriptionState = subscriptionState,
                        subscriptionReady = subscriptionReady,
                        detailState = detailState,
                        newsState = newsState,
                        rate = detailRate,
                        onBack = { detailRate = null },
                        onOpenPaywall = { showPaywall = true },
                        onLoadHistory = detailStore::load,
                        onOpenUrl = ExternalUrlOpener::open,
                        onOpenStory = { detailNewsStory = it },
                        onCreateAlert = { rate ->
                            if (canCreateAlert(subscriptionState, alertsState.alerts.size)) {
                                alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                            } else {
                                showPaywall = true
                            }
                        },
                    )
                } else {
                    when (selectedTab) {
                        FxTab.Rates -> {
                            if (liveState.errorMessage != null && !liveState.isLive) {
                                OfflineScreen(liveState, onRefresh = liveStore::refresh)
                            } else {
                                DashboardScreen(
                                    liveState = liveState,
                                    subscriptionState = subscriptionState,
                                    onRefresh = liveStore::refresh,
                                    onOpenPaywall = { showPaywall = true },
                                    onOpenDetail = { detailRate = it },
                                )
                            }
                        }
                        FxTab.Convert -> ConverterScreen(
                            liveState = liveState,
                            subscriptionState = subscriptionState,
                            selectedCurrencyCodes = converterCurrencyCodes,
                            onCurrencyCodesChange = { codes ->
                                converterCurrencyCodes = codes
                                AppSettingsPrefs.setConverterCurrencyCodes(codes)
                            },
                            onOpenPaywall = { showPaywall = true },
                        )
                        FxTab.Compare -> CompareScreen(
                            liveState = liveState,
                            subscriptionState = subscriptionState,
                            onOpenPaywall = { showPaywall = true },
                            onOpenDetail = { detailRate = it },
                        )
                        FxTab.News -> NewsScreen(
                            newsState = newsState,
                            subscriptionState = subscriptionState,
                            onRefresh = newsStore::refresh,
                            onRegionSelected = newsStore::setRegion,
                            onCurrencySelected = newsStore::setCurrency,
                            onOpenStory = { detailNewsStory = it },
                            onOpenPaywall = { showPaywall = true },
                        )
                        FxTab.More -> when (moreRoute) {
                            MoreRoute.Menu -> MoreScreen(
                                subscriptionState = subscriptionState,
                                alertsCount = alertsState.activeCount,
                                watchlistCount = watchlistState.watchlist.codes.size,
                                onOpenAlerts = { moreRoute = MoreRoute.Alerts },
                                onOpenWatchlist = { moreRoute = MoreRoute.Watchlist },
                                onOpenTraveler = { moreRoute = MoreRoute.Traveler },
                                onOpenSettings = { moreRoute = MoreRoute.Settings },
                                onOpenNews = { selectTab(FxTab.News) },
                                onOpenPaywall = { showPaywall = true },
                            )
                            MoreRoute.Alerts -> AlertsScreen(
                                liveState = liveState,
                                alertsState = alertsState,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { showPaywall = true },
                                onCreateAlert = { rate ->
                                    if (canCreateAlert(subscriptionState, alertsState.alerts.size)) {
                                        alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                                    } else {
                                        showPaywall = true
                                    }
                                },
                                onCreateManualAlert = { rate, direction, target, kind ->
                                    if (canCreateAlert(subscriptionState, alertsState.alerts.size)) {
                                        alertsStore.addAlert(liveState.baseCurrency, rate.code, target, direction, kind)
                                    } else {
                                        showPaywall = true
                                    }
                                },
                                onResumeAlert = alertsStore::resumeAlert,
                                onToggleAlert = alertsStore::toggleAlert,
                                onDeleteAlert = alertsStore::deleteAlert,
                                onTestAlert = AlertTestNotifier::show,
                            )
                            MoreRoute.Watchlist -> WatchlistScreen(
                                liveState = liveState,
                                watchlistState = watchlistState,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { showPaywall = true },
                                onToggleCurrency = { code ->
                                    val selected = code in watchlistState.watchlist.codes
                                    val canAdd = selected ||
                                        subscriptionState.featureAccess().hasUnlimitedWatchlistCurrencies ||
                                        watchlistState.watchlist.codes.size < subscriptionState.featureAccess().watchlistCurrencyLimit
                                    if (!watchlistStore.toggle(code, canAdd)) {
                                        showPaywall = true
                                    }
                                },
                                onSetHolding = watchlistStore::setHolding,
                                onOpenDetail = { detailRate = it },
                            )
                            MoreRoute.Traveler -> TravelerScreen(
                                liveState = liveState,
                                subscriptionState = subscriptionState,
                                selectedCurrency = travelerCurrency,
                                budgetBase = travelerBudgetBase,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onCurrencySelected = { code ->
                                    travelerCurrency = code
                                    AppSettingsPrefs.setTravelerCurrency(code)
                                },
                                onBudgetChange = { amount ->
                                    travelerBudgetBase = amount
                                    AppSettingsPrefs.setTravelerBudgetBase(amount)
                                },
                                onOpenPaywall = { showPaywall = true },
                            )
                            MoreRoute.Settings -> SettingsScreen(
                                themeMode = themeMode,
                                baseCurrency = baseCurrency,
                                availableBaseCurrencies = liveState.allFiat,
                                backupState = backupState,
                                backupSyncing = backupSyncing,
                                lastSyncedAtMillis = lastSyncedAtMillis,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { showPaywall = true },
                                onRestorePurchase = {
                                    scope.launch {
                                        subscriptionState = subscriptionGateway.restore()
                                        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                        subscriptionReady = true
                                    }
                                },
                                onSyncNow = {
                                    scope.launch {
                                        backupSyncing = true
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(
                                                themeMode,
                                                baseCurrency,
                                                travelerCurrency,
                                                travelerBudgetBase,
                                                converterCurrencyCodes,
                                                alertsState,
                                                watchlistState,
                                            )
                                            UserBackupGateway.pushSnapshot(snapshot)
                                            backupState = UserBackupGateway.ensureUser()
                                            lastSyncedAtMillis = snapshot.updatedAtMillis
                                        }.onFailure { error ->
                                            backupState = backupState.copy(errorMessage = error.message)
                                        }
                                        backupSyncing = false
                                    }
                                },
                                onLinkGoogle = {
                                    scope.launch {
                                        backupSyncing = true
                                        backupReady = false
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(
                                                themeMode,
                                                baseCurrency,
                                                travelerCurrency,
                                                travelerBudgetBase,
                                                converterCurrencyCodes,
                                                alertsState,
                                                watchlistState,
                                            )
                                            val result = when (PlatformConfig.platform) {
                                                Platform.Android -> UserBackupGateway.linkWithGoogle(snapshot)
                                                Platform.Ios -> UserBackupGateway.linkWithApple(snapshot)
                                            }
                                            backupState = result.state
                                            val appliedTheme = applyUserBackupSnapshot(
                                                snapshot = result.snapshot,
                                                alertsStore = alertsStore,
                                                watchlistStore = watchlistStore,
                                                liveStore = liveStore,
                                                onConverterCurrencyCodes = { converterCurrencyCodes = it },
                                                onTravelerCurrency = { travelerCurrency = it },
                                                onTravelerBudgetBase = { travelerBudgetBase = it },
                                            )
                                            themeMode = appliedTheme
                                            baseCurrency = result.snapshot.settings.baseCurrency
                                            lastSyncedAtMillis = result.snapshot.updatedAtMillis
                                            backupReady = true
                                        }.onFailure { error ->
                                            backupState = backupState.copy(errorMessage = error.message)
                                            backupReady = backupState.isAvailable
                                        }
                                        backupSyncing = false
                                    }
                                },
                                onSignOut = {
                                    scope.launch {
                                        backupSyncing = true
                                        backupReady = false
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(
                                                themeMode,
                                                baseCurrency,
                                                travelerCurrency,
                                                travelerBudgetBase,
                                                converterCurrencyCodes,
                                                alertsState,
                                                watchlistState,
                                            )
                                            val result = UserBackupGateway.signOutToAnonymous(snapshot)
                                            backupState = result.state
                                            lastSyncedAtMillis = result.snapshot.updatedAtMillis
                                            backupReady = true
                                        }.onFailure { error ->
                                            backupState = backupState.copy(errorMessage = error.message)
                                            backupReady = backupState.isAvailable
                                        }
                                        backupSyncing = false
                                    }
                                },
                                onDevPremiumChange = { enabled ->
                                    scope.launch {
                                        subscriptionState = subscriptionGateway.setDevPremium(enabled)
                                        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                        subscriptionReady = true
                                    }
                                },
                                onThemeModeChange = { mode ->
                                    themeMode = mode
                                    AppSettingsPrefs.setThemeMode(mode)
                                },
                                onBaseCurrencyChange = { code ->
                                    baseCurrency = code
                                    AppSettingsPrefs.setBaseCurrency(code)
                                    liveStore.setBaseCurrency(code)
                                },
                            )
                        }
                    }
                }
            }
            if (startupReady) {
                FxBottomBar(
                    tabs = FxTab.entries.map { it.label },
                    selectedIndex = selectedTab.ordinal,
                    onSelect = {
                        selectTab(FxTab.entries[it])
                    },
                )
            }
        }
    }
}

@Composable
private fun StartupLoadingScreen(baseCurrency: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        GridBg(Modifier.matchParentSize().alpha(0.18f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiveDot(Modifier.size(10.dp))
            Text("Preparing $baseCurrency rates", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text("Syncing preferences", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun ScreenScaffold(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun DashboardScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    onRefresh: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
) {
    val access = subscriptionState.featureAccess()
    val visibleFavorites = liveState.favorites.take(access.favoriteLimit.cap(liveState.favorites.size))
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LiveDot(Modifier.size(9.dp))
                Eyebrow(if (liveState.isLive) "LIVE" else "CACHED", color = FxTheme.colors.accent)
            }
            Text(liveState.updatedLabel, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, textAlign = TextAlign.End)
        }
        ScreenHeader(
            title = "Rates",
            subtitle = "base · ${liveState.baseCurrency}  ·  ${visibleFavorites.size}/${liveState.favorites.size} favorites · ${liveState.autoRefreshLabel}",
            right = { Text("↻", style = FxTheme.typography.numberL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onRefresh)) },
        )
        if (liveState.errorMessage != null) {
            Text("Live backend unavailable · using cached UI data", style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        HeroRateCard(visibleFavorites.firstOrNull() ?: FavoriteRates.first(), liveState.baseCurrency)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile("VOLATILITY · 24H", "0.42%", null, Modifier.weight(1f).height(76.dp))
            liveState.favorites.firstOrNull { it.code == "GBP" }?.let { MetricTile("GBP · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            liveState.favorites.firstOrNull { it.code == "JPY" }?.let { MetricTile("JPY · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
            liveState.favorites.firstOrNull { it.code == "MXN" }?.let { MetricTile("MXN · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
        }
        SectionLabel("FAVORITES · ${visibleFavorites.size}", right = if (subscriptionState.isPremium) "Edit" else "Pro")
        BentoCard(padding = 0.dp) {
            Column {
                visibleFavorites.forEach { rate ->
                    CurrencyRow(rate, dense = true, onClick = { onOpenDetail(rate) })
                }
            }
        }
        if (!subscriptionState.isPremium) {
            ProUpsellCard(
                title = "Unlock full watchlists",
                subtitle = "Pro adds more favorites, extended history, alerts and complete fee comparison.",
                onClick = onOpenPaywall,
            )
        }
        SectionLabel("CRYPTO", right = "See all")
        BentoCard(padding = 0.dp) {
            Column { liveState.crypto.forEach { rate -> CurrencyRow(rate, dense = true, onClick = { onOpenDetail(rate) }) } }
        }
    }
}

@Composable
private fun HeroRateCard(rate: FxRate, baseCurrency: String) {
    BentoCard(Modifier.fillMaxWidth().height(158.dp), padding = 14.dp) {
        GridBg(Modifier.matchParentSize().alpha(0.22f))
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlagDot(rate.glyph, rate.kind, size = 28.dp)
                    Text("$baseCurrency → ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                }
                Pill("pinned", variant = PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(formatRate(rate.rate), style = FxTheme.typography.numberXL.copy(fontSize = 44.sp, lineHeight = 44.sp), color = FxTheme.colors.text)
                Text(formatChange(rate.change24h), style = FxTheme.typography.numberBody, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Eyebrow("24H RANGE")
                    Text("${formatRate(rate.sparkline.minOrNull()?.toDouble() ?: rate.rate)} — ${formatRate(rate.sparkline.maxOrNull()?.toDouble() ?: rate.rate)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
                }
                SparkLine(rate.sparkline, Modifier.size(108.dp, 38.dp), color = FxTheme.colors.accent, showLastDot = true)
            }
        }
    }
}

@Composable
fun ConverterScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    selectedCurrencyCodes: List<String> = emptyList(),
    onCurrencyCodesChange: (List<String>) -> Unit = {},
    onOpenPaywall: () -> Unit,
) {
    val access = subscriptionState.featureAccess()
    val focusManager = LocalFocusManager.current
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val availableRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat, liveState.crypto) {
        liveState.converterAvailableRates()
    }
    val targetCodes = remember(liveState.baseCurrency, selectedCurrencyCodes, availableRates, access.converterCurrencyLimit) {
        converterTargetCodes(
            selectedCurrencyCodes = selectedCurrencyCodes,
            availableRates = availableRates,
            baseCurrency = liveState.baseCurrency,
            limit = access.converterCurrencyLimit,
        )
    }
    val rates = remember(liveState.baseCurrency, liveState.converter, availableRates, targetCodes) {
        val byCode = (availableRates + liveState.converter.ifEmpty { ConverterRates }).distinctBy { it.code }.associateBy { it.code }
        (listOfNotNull(byCode[liveState.baseCurrency]) + targetCodes.mapNotNull { byCode[it] })
            .distinctBy { it.code }
    }
    val initialTarget = remember(liveState.baseCurrency, rates) {
        rates.firstOrNull { it.code != liveState.baseCurrency }?.code ?: liveState.baseCurrency
    }
    var sourceCode by remember(liveState.baseCurrency) { mutableStateOf(liveState.baseCurrency) }
    var targetCode by remember(liveState.baseCurrency, initialTarget) { mutableStateOf(initialTarget) }
    var amountText by remember(liveState.baseCurrency) { mutableStateOf("1000") }
    var amountFocused by remember { mutableStateOf(false) }
    val sourceRate = rates.firstOrNull { it.code == sourceCode }
        ?: rates.firstOrNull { it.code == liveState.baseCurrency }
        ?: rates.first()
    val targetRate = rates.firstOrNull { it.code == targetCode && it.code != sourceRate.code }
        ?: rates.firstOrNull { it.code != sourceRate.code }
        ?: sourceRate
    val amountValue = parseAmountInput(amountText)
    val feeQuotes = estimatedFeeQuotes(sourceRate, targetRate, amountValue)
        .take(access.feeQuoteLimit.cap(EstimatedFeeQuoteCount))
    if (showCurrencyPicker) {
        ConverterCurrencyPickerSheet(
            currencies = availableRates.filterNot { it.code == liveState.baseCurrency },
            selectedCodes = targetCodes,
            limit = access.converterCurrencyLimit,
            isPremium = subscriptionState.isPremium,
            onDismiss = { showCurrencyPicker = false },
            onOpenPaywall = {
                showCurrencyPicker = false
                onOpenPaywall()
            },
            onApply = { codes ->
                showCurrencyPicker = false
                onCurrencyCodesChange(codes)
                if (targetCode !in codes && codes.isNotEmpty()) {
                    targetCode = codes.first()
                }
            },
        )
    }
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot()
            Eyebrow("MID", color = FxTheme.colors.accent)
            Text(
                liveState.updatedLabel,
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ScreenHeader("Convert", subtitle = "Multi-currency · live to 4 decimals")
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("YOU SEND")
                    Pill(sourceRate.code, variant = PillVariant.Accent)
                }
                BasicTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(14)
                    },
                    singleLine = true,
                    textStyle = FxTheme.typography.numberXL.copy(color = FxTheme.colors.text, fontSize = 38.sp, lineHeight = 40.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FxTheme.shapes.field)
                        .background(if (amountFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                        .border(1.dp, if (amountFocused) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .onFocusChanged { amountFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        if (amountText.isBlank()) {
                            Text(
                                "0.00",
                                style = FxTheme.typography.numberXL.copy(fontSize = 38.sp, lineHeight = 40.sp),
                                color = FxTheme.colors.textGhost,
                            )
                        }
                        innerTextField()
                    },
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Converted to ${targetRate.code}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                    Text(
                        formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)),
                        style = FxTheme.typography.numberBody,
                        color = FxTheme.colors.accent,
                    )
                }
            }
        }
        BentoCard(padding = 8.dp) {
            Column {
                rates.forEach { rate ->
                    ConverterRow(
                        rate = rate,
                        amount = if (rate.code == sourceRate.code) amountValue else convertedAmount(amountValue, sourceRate, rate),
                        selected = rate.code == targetRate.code,
                        source = rate.code == sourceRate.code,
                        onClick = {
                            if (rate.code != sourceRate.code) {
                                targetCode = rate.code
                                focusManager.clearFocus()
                            }
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton(
                "⇄  Reverse",
                Modifier.weight(1f),
                onClick = {
                    val previousSource = sourceRate
                    val previousTarget = targetRate
                    sourceCode = previousTarget.code
                    targetCode = previousSource.code
                    amountText = formatInputAmount(convertedAmount(amountValue, previousSource, previousTarget))
                    focusManager.clearFocus()
                },
            )
            GhostButton("≡  Edit list", Modifier.weight(1f), onClick = { showCurrencyPicker = true })
        }
        SectionLabel("FEES · ${sourceRate.code} → ${targetRate.code}", right = if (access.canUseFullFeeComparison) "Estimated" else "Preview")
        BentoCard(padding = 0.dp) {
            Column { feeQuotes.forEach { FeeComparisonRow(it) } }
        }
        if (!access.canUseFullFeeComparison) {
            ProUpsellCard(
                title = "See the real transfer cost",
                subtitle = "Pro unlocks the complete provider list; estimates update with your amount.",
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
private fun ConverterRow(
    rate: FxRate,
    amount: Double,
    selected: Boolean,
    source: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        selected -> FxTheme.colors.accentSoft
        source -> FxTheme.colors.surface2
        else -> Color.Transparent
    }
    val border = when {
        selected -> FxTheme.colors.accentLine
        source -> FxTheme.colors.border
        else -> Color.Transparent
    }
    val contentColor = when {
        selected -> FxTheme.colors.accent
        source -> FxTheme.colors.textDim
        else -> FxTheme.colors.text
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(bg)
            .border(if (selected || source) 1.dp else 0.dp, border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlagDot(rate.glyph, rate.kind, 32.dp)
        Column(Modifier.weight(1f)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = if (source) FxTheme.colors.textDim else FxTheme.colors.text)
            Text(
                if (source) "Base currency · source amount" else if (selected) "Selected destination" else rate.name,
                style = FxTheme.typography.caption,
                color = if (selected) FxTheme.colors.accent else FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            formatConvertedAmount(rate, amount),
            style = if (source || selected) FxTheme.typography.numberL.copy(fontSize = 24.sp) else FxTheme.typography.numberL,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun FeeComparisonRow(quote: EstimatedFeeQuote) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(quote.provider, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text, modifier = Modifier.weight(1f))
        if (quote.badge != null) Pill(quote.badge, variant = if (quote.isHighFee) PillVariant.Down else PillVariant.Up)
        Text(
            quote.amount,
            style = FxTheme.typography.numberBody,
            color = FxTheme.colors.text,
            modifier = Modifier.widthIn(min = 70.dp),
            textAlign = TextAlign.End,
        )
        Text(
            quote.fee,
            style = FxTheme.typography.captionMono,
            color = FxTheme.colors.textFaint,
            modifier = Modifier.widthIn(min = 48.dp),
            textAlign = TextAlign.End,
        )
    }
}

private const val EstimatedFeeQuoteCount = 4

private data class EstimatedFeeQuote(
    val provider: String,
    val badge: String?,
    val amount: String,
    val fee: String,
    val isHighFee: Boolean = false,
)

private fun estimatedFeeQuotes(
    sourceRate: FxRate,
    targetRate: FxRate,
    amount: Double,
): List<EstimatedFeeQuote> {
    val safeAmount = amount.coerceAtLeast(0.0)

    fun quote(
        provider: String,
        badge: String?,
        feeAmount: Double,
        isHighFee: Boolean = false,
    ): EstimatedFeeQuote {
        val netSource = (safeAmount - feeAmount).coerceAtLeast(0.0)
        return EstimatedFeeQuote(
            provider = provider,
            badge = badge,
            amount = formatConvertedAmount(targetRate, convertedAmount(netSource, sourceRate, targetRate)),
            fee = "${sourceRate.code} ${formatMoneyValue(feeAmount.coerceAtLeast(0.0))}",
            isHighFee = isHighFee,
        )
    }

    return listOf(
        quote("Mid-market", "best", 0.0),
        quote("Wise", null, maxOf(0.35, safeAmount * 0.0045)),
        quote("Revolut", null, safeAmount * 0.008),
        quote("Bank transfer", "high fee", 5.0 + safeAmount * 0.032, isHighFee = true),
    )
}

private fun convertedAmount(amount: Double, sourceRate: FxRate, targetRate: FxRate): Double =
    if (sourceRate.rate == 0.0) {
        0.0
    } else {
        amount / sourceRate.rate * targetRate.rate
    }

private fun formatConvertedAmount(rate: FxRate, amount: Double): String =
    "${rate.code} ${if (rate.kind == CurrencyKind.Crypto) formatCryptoAmount(amount) else formatMoneyValue(amount)}"

private fun formatCryptoAmount(value: Double): String =
    when {
        value <= 0.0 -> "0"
        value < 0.000001 -> "<0.000001"
        value < 1.0 -> formatRate(value)
        else -> formatMoneyValue(value)
    }

private fun formatInputAmount(value: Double): String =
    when {
        value <= 0.0 -> ""
        value >= 100.0 -> formatMoneyValue(value).replace(",", "")
        value >= 1.0 -> formatRate(value)
        else -> formatRate(value)
    }

@Composable
fun DetailScreen(
    liveState: LiveRatesState = LiveRatesState(),
    alertsState: AlertsState = AlertsState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    subscriptionReady: Boolean = true,
    detailState: DetailUiState = DetailUiState(),
    newsState: NewsUiState = NewsUiState(),
    rate: FxRate? = null,
    onBack: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    onLoadHistory: (String, String, Period, List<Float>) -> Unit = { _, _, _, _ -> },
    onOpenUrl: (String) -> Unit = {},
    onOpenStory: (NewsStory) -> Unit = {},
    onCreateAlert: (FxRate) -> Unit = {},
) {
    var period by remember { mutableStateOf(Period.OneMonth) }
    val selected = rate ?: liveState.favorites.firstOrNull { it.code == "EUR" } ?: FavoriteRates.first()
    val activeForPair = alertsState.alerts.count { it.enabled && it.base == liveState.baseCurrency && it.quote == selected.code }
    val alertAccess = subscriptionState.featureAccess()
    val alertLabel = if (alertAccess.hasUnlimitedAlerts) {
        "${alertsState.activeCount} active"
    } else {
        "${alertsState.activeCount}/${alertAccess.alertLimit} active"
    }
    val fallbackSeries = if (selected.code == liveState.favorites.firstOrNull()?.code) liveState.detailSeries else selected.sparkline
    val detailMatches = detailState.base == liveState.baseCurrency && detailState.quote == selected.code && detailState.period == period
    val hasLoadedPeriodData = detailMatches && detailState.points.isNotEmpty()
    val isLoadingNewPeriod = detailMatches && detailState.isLoading && !hasLoadedPeriodData
    val chartData = remember(detailState.series, hasLoadedPeriodData, fallbackSeries, period) {
        if (hasLoadedPeriodData) detailState.series else fallbackSeries.seriesForPeriod(period)
    }
    var visibleChartData by remember(liveState.baseCurrency, selected.code) { mutableStateOf(chartData) }
    LaunchedEffect(chartData, isLoadingNewPeriod) {
        if (!isLoadingNewPeriod) {
            visibleChartData = chartData
        }
    }
    val stats = remember(visibleChartData) { visibleChartData.toDetailStats() }
    val effectivePremium = subscriptionState.isPremium || !subscriptionReady
    val periodIsPro = period == Period.OneYear || period == Period.All
    val historyCaption = if (detailMatches && detailState.points.isNotEmpty()) {
        "${detailState.provider} · ${detailState.points.size} pts · ${detailState.updatedLabel}"
    } else {
        "cached preview"
    }
    LaunchedEffect(liveState.baseCurrency, selected.code, period, fallbackSeries, effectivePremium) {
        if (effectivePremium || !periodIsPro) {
            onLoadHistory(liveState.baseCurrency, selected.code, period, fallbackSeries)
        }
    }
    val relatedStories = remember(newsState.stories, selected.code) {
        newsState.stories.filter { story ->
            story.tag == selected.code || story.moves.any { it.first == selected.code }
        }
    }
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BackNavButton(label = null, onClick = onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill(if (activeForPair > 0) "🔔 $activeForPair alert" else "★ Watching")
                Pill(if (effectivePremium) "Pro" else "Free")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FlagDot(selected.glyph, selected.kind, size = 36.dp)
            Column {
                Text("${liveState.baseCurrency} / ${selected.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(selected.name, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(formatRate(selected.rate), style = FxTheme.typography.numberXL, color = FxTheme.colors.text)
            Text(formatChange(selected.change24h), style = FxTheme.typography.numberBody, color = if (selected.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down, modifier = Modifier.padding(bottom = 7.dp))
        }
        Text("${selected.caption ?: "mid-market"} · ${liveState.updatedLabel}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(if (detailState.isLoading && detailMatches) "LOADING HISTORY" else "HISTORY · ${period.label}")
                    Text(
                        historyCaption,
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isLoadingNewPeriod) {
                    DetailChartLoadingOverlay(visibleChartData, Modifier.fillMaxWidth().height(188.dp))
                } else {
                    PriceChart(visibleChartData, Modifier.fillMaxWidth().height(188.dp))
                }
                SegmentedPeriods(
                    period,
                    { next ->
                        if (!effectivePremium && (next == Period.OneYear || next == Period.All)) {
                            onOpenPaywall()
                        } else {
                            period = next
                        }
                    },
                    Modifier.fillMaxWidth(),
                )
                if (detailMatches && detailState.errorMessage != null) {
                    Text("History unavailable · using cached preview", style = FxTheme.typography.caption, color = FxTheme.colors.down)
                }
            }
        }
        if (periodIsPro && !effectivePremium) {
            ProUpsellCard(
                title = "Unlock long-range history",
                subtitle = "Pro adds 1Y and all-time detail, full event context and deeper market overlays.",
                onClick = onOpenPaywall,
            )
        }
        SectionLabel("STATISTICS · ${period.label}")
        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KeyValueRow("Open", formatRate(stats.open))
                KeyValueRow("High", formatRate(stats.high))
                KeyValueRow("Low", formatRate(stats.low))
                KeyValueRow("Range", "${formatRate(stats.low)} - ${formatRate(stats.high)}")
                KeyValueRow("Volatility", "${formatRate(stats.volatilityPct)}%")
                KeyValueRow("Average", formatRate(stats.average))
            }
        }
        SectionLabel("RELATED NEWS", right = if (newsState.isLoading) "Loading" else if (effectivePremium) "Live" else "Preview")
        if (relatedStories.isEmpty()) {
            EmptyDetailSection(
                title = if (newsState.isLoading) "Loading related news" else "No related news",
                subtitle = if (newsState.isLoading) {
                    "Fetching ${selected.code} market headlines from the live feed."
                } else {
                    "No live headlines are currently tied to ${selected.code}."
                },
            )
        } else {
            relatedStories.take(if (effectivePremium) relatedStories.size else 2).forEach { story ->
                StoryCard(story, onClick = { onOpenStory(story) })
            }
        }
        SectionLabel("EVENTS · ANNOTATED", right = if (effectivePremium) "Derived" else "Preview")
        if (relatedStories.isEmpty()) {
            EmptyDetailSection(
                title = "No annotated events",
                subtitle = "Events will appear here when the live feed includes stories for ${selected.code}.",
            )
        } else {
            BentoCard(padding = 0.dp) {
                Column {
                    relatedStories.take(if (effectivePremium) relatedStories.size else 2).forEach { story ->
                        DetailEventRow(story, onOpenUrl = onOpenUrl)
                    }
                }
            }
        }
        GhostButton(
            text = if (activeForPair > 0) "🔔 Add another ${selected.code} alert · $alertLabel" else "🔔 Alert me above ${formatRate(selected.rate * 1.01)} · $alertLabel",
            onClick = { onCreateAlert(selected) },
        )
    }
}

@Composable
private fun DetailChartLoadingPlaceholder(modifier: Modifier = Modifier) {
    val colors = FxTheme.colors
    Canvas(modifier = modifier) {
        val padX = 8.dp.toPx()
        val padTop = 16.dp.toPx()
        val padBottom = 18.dp.toPx()
        val chartH = size.height - padTop - padBottom
        repeat(4) { i ->
            val y = padTop + chartH * (i / 3f)
            var x = padX
            while (x < size.width - padX) {
                drawLine(
                    colors.border.copy(alpha = 0.72f),
                    Offset(x, y),
                    Offset((x + 4.dp.toPx()).coerceAtMost(size.width - padX), y),
                    strokeWidth = 1f,
                )
                x += 8.dp.toPx()
            }
        }
    }
}

@Composable
private fun DetailChartLoadingOverlay(data: List<Float>, modifier: Modifier = Modifier) {
    val colors = FxTheme.colors
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1_100), repeatMode = RepeatMode.Restart),
    )
    Box(modifier) {
        PriceChart(data, Modifier.matchParentSize().alpha(0.46f))
        Canvas(Modifier.matchParentSize()) {
            val x = size.width * progress
            drawLine(
                colors.accent.copy(alpha = 0.48f),
                Offset(x, 16.dp.toPx()),
                Offset(x, size.height - 18.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}

@Composable
private fun DetailEventRow(story: NewsStory, onOpenUrl: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = story.sourceUrl.isNotBlank()) { onOpenUrl(story.sourceUrl) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, modifier = Modifier.width(58.dp))
        Pill(story.tag, variant = PillVariant.Accent)
        Text(story.title, style = FxTheme.typography.caption, color = FxTheme.colors.text, modifier = Modifier.weight(1f))
        if (story.sourceUrl.isNotBlank()) {
            Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun EmptyDetailSection(title: String, subtitle: String) {
    BentoCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
fun CompareScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
) {
    val access = subscriptionState.featureAccess()
    val compareRates = liveState.compare.take(access.compareLimit.cap(liveState.compare.size))
    ScreenScaffold {
        ScreenHeader("Compare", sub = "${liveState.baseCurrency} BASE", subtitle = "${compareRates.size} currencies · normalized movement")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            compareRates.chunked(2).forEach { rowRates ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowRates.forEach { rate ->
                        CompareTile(
                            rate = rate,
                            baseCurrency = liveState.baseCurrency,
                            onOpenDetail = onOpenDetail,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowRates.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        if (!subscriptionState.isPremium) {
            ProUpsellCard(
                title = "Compare every tracked currency",
                subtitle = "Pro unlocks the full comparison board and advanced overlays.",
                onClick = onOpenPaywall,
            )
        }
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Eyebrow("OVERLAY · 1M")
                OverlayChart()
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot("EUR", FxTheme.colors.accent)
                    LegendDot("GBP", FxTheme.colors.up)
                    LegendDot("JPY", FxTheme.colors.down)
                    LegendDot("BTC", FxTheme.colors.crypto)
                }
            }
        }
    }
}

@Composable
private fun CompareTile(rate: FxRate, baseCurrency: String, onOpenDetail: (FxRate) -> Unit, modifier: Modifier = Modifier) {
    BentoTile(
        modifier = modifier.clickable { onOpenDetail(rate) },
        padding = 10.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlagDot(rate.glyph, rate.kind, 24.dp)
                    Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                }
                Text(formatChange(rate.change24h), style = FxTheme.typography.captionMono, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
            Text(formatRate(rate.rate), style = FxTheme.typography.numberL, color = FxTheme.colors.text)
            Text("per 1 $baseCurrency", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            SparkLine(rate.sparkline, Modifier.fillMaxWidth().height(30.dp))
        }
    }
}

@Composable
fun TravelerScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    selectedCurrency: String = "JPY",
    budgetBase: Double = 100.0,
    onBack: (() -> Unit)? = null,
    onCurrencySelected: (String) -> Unit = {},
    onBudgetChange: (Double) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val travelRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat) {
        liveState.portfolioRates().filterNot { it.code == liveState.baseCurrency }
    }
    val destinationLimit = if (access.canUseAdvancedTraveler) 12 else 8
    val visibleDestinations = remember(travelRates, selectedCurrency, destinationLimit) {
        compactCurrencyChoices(travelRates, selectedCurrency, destinationLimit)
    }
    val selectedRate = travelRates.firstOrNull { it.code == selectedCurrency }
        ?: visibleDestinations.firstOrNull()
        ?: FavoriteRates.first()
    val destination = travelerDestination(selectedRate.code)
    val budgetLocal = budgetBase * selectedRate.rate
    val cheatAmounts = listOf(1, 5, 10, 20, 50, 100, 250, 500).take(access.travelerCheatSheetLimit.cap(8))
    val baseDefinition = liveState.allFiat.firstOrNull { it.code == liveState.baseCurrency }
        ?: SettingsBaseCurrencies.firstOrNull { it.code == liveState.baseCurrency }
    var budgetText by remember { mutableStateOf(if (budgetBase > 0.0) formatMoneyValue(budgetBase) else "") }
    var showDestinationPicker by remember { mutableStateOf(false) }
    if (showDestinationPicker) {
        CurrencyPickerSheet(
            title = "Choose destination",
            subtitle = "${travelRates.size} live currencies · ${liveState.baseCurrency} base",
            currencies = travelRates,
            selectedCode = selectedRate.code,
            onDismiss = { showDestinationPicker = false },
            onSelect = { code ->
                showDestinationPicker = false
                onCurrencySelected(code)
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader(
            "Traveler",
            sub = "${destination.city.uppercase()} · ${selectedRate.code}",
            subtitle = if (liveState.isLive) "Live ${liveState.baseCurrency} rates · ${liveState.updatedLabel}" else "Offline snapshot · ${liveState.baseCurrency} base",
        )
        BentoCard(Modifier.fillMaxWidth().height(156.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.18f))
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlagDot(baseDefinition?.glyph ?: "◆", size = 28.dp)
                    Text("1 ${liveState.baseCurrency}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text("→", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textFaint)
                    Text(selectedRate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    FlagDot(destination.flag, size = 28.dp)
                }
                BigValueText("${destination.symbol}${formatRate(selectedRate.rate)}")
                Text("${formatChange(selectedRate.change24h)} today · mid-market", style = FxTheme.typography.captionMono, color = if (selectedRate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
        }

        SectionLabel("DESTINATION")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleDestinations.chunked(4).forEach { rowRates ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowRates.forEach { rate ->
                            val item = travelerDestination(rate.code)
                            Pill(
                                "${item.flag} ${rate.code}",
                                variant = if (rate.code == selectedRate.code) PillVariant.Accent else PillVariant.Ghost,
                                modifier = Modifier.clickable { onCurrencySelected(rate.code) },
                            )
                        }
                    }
                }
                SettingChoiceRow(
                    title = "More destinations",
                    subtitle = if (access.canUseAdvancedTraveler) {
                        "Search ${travelRates.size} supported live currencies"
                    } else {
                        "Free shows ${visibleDestinations.size}; Pro unlocks every supported currency"
                    },
                    selected = false,
                    actionLabel = "more +",
                    onClick = {
                        if (access.canUseAdvancedTraveler) showDestinationPicker = true else onOpenPaywall()
                    },
                )
                if (!access.canUseAdvancedTraveler && travelRates.size > visibleDestinations.size) {
                    Text("Free keeps the destination picker focused on the most common travel currencies.", style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                }
            }
        }

        SectionLabel("TRIP BUDGET")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("BUDGET · ${liveState.baseCurrency}")
                        BasicTextField(
                            value = budgetText,
                            onValueChange = { raw ->
                                val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                                budgetText = next
                                onBudgetChange(next.replace(",", "").toDoubleOrNull() ?: 0.0)
                            },
                            singleLine = true,
                            textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Eyebrow("LOCAL")
                        Text("${destination.symbol}${formatMoneyValue(budgetLocal)}", style = FxTheme.typography.numberL, color = FxTheme.colors.text)
                    }
                }
                KeyValueRow("Daily range", "${destination.symbol}${formatMoneyValue(budgetLocal / 3.0)} · 3 days")
                KeyValueRow("Cash buffer", "${destination.symbol}${formatMoneyValue(budgetLocal * destination.cashBufferPct)}")
            }
        }

        SectionLabel("CHEAT SHEET")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cheatAmounts.forEach { amount ->
                    KeyValueRow("$amount ${liveState.baseCurrency}", "${destination.symbol}${formatMoneyValue(amount * selectedRate.rate)}")
                }
            }
        }
        if (!access.canUseAdvancedTraveler) {
            ProUpsellCard(
                title = "Unlock full traveler mode",
                subtitle = "Pro adds complete cheat sheets, offline context and more local money tips.",
                onClick = onOpenPaywall,
            )
        }
        SectionLabel("LOCAL ETIQUETTE")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("TIPPING", destination.tipping, destination.tippingNote, Modifier.weight(1f))
            MetricTile("TAX", destination.tax, destination.taxNote, Modifier.weight(1f))
        }
        BentoTile(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Eyebrow("CARDS ACCEPTED")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        destination.paymentRails.forEach { Pill(it) }
                    }
                }
                Text(destination.cashNote, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
        }
        SectionLabel("LOCAL PRICE GUIDE", right = "Estimates")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                destination.priceGuide.forEach { item ->
                    val basePrice = item.localAmount / selectedRate.rate
                    KeyValueRow(item.label, "${destination.symbol}${formatMoneyValue(item.localAmount)} · ${liveState.baseCurrency} ${formatMoneyValue(basePrice)}")
                }
            }
        }
    }
}

private data class TravelerDestination(
    val code: String,
    val city: String,
    val flag: String,
    val symbol: String,
    val tipping: String,
    val tippingNote: String,
    val tax: String,
    val taxNote: String,
    val cashNote: String,
    val cashBufferPct: Double,
    val paymentRails: List<String>,
    val priceGuide: List<TravelerPriceGuide>,
)

private data class TravelerPriceGuide(
    val label: String,
    val localAmount: Double,
)

private fun travelerDestination(code: String): TravelerDestination =
    travelerDestinations[code] ?: TravelerDestination(
        code = code,
        city = code,
        flag = "◆",
        symbol = "$code ",
        tipping = "Check",
        tippingNote = "varies by city",
        tax = "Varies",
        taxNote = "verify locally",
        cashNote = "mixed payments",
        cashBufferPct = 0.20,
        paymentRails = listOf("Visa", "Mastercard"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 4.0),
            TravelerPriceGuide("Casual meal", 18.0),
            TravelerPriceGuide("Taxi start", 8.0),
        ),
    )

private val travelerDestinations = mapOf(
    "JPY" to TravelerDestination(
        code = "JPY",
        city = "Tokyo",
        flag = "🇯🇵",
        symbol = "¥",
        tipping = "0%",
        tippingNote = "not customary",
        tax = "10%",
        taxNote = "often included",
        cashNote = "cash useful",
        cashBufferPct = 0.25,
        paymentRails = listOf("Visa", "Mastercard", "Suica"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 450.0),
            TravelerPriceGuide("Ramen", 1_100.0),
            TravelerPriceGuide("Metro ride", 220.0),
            TravelerPriceGuide("Taxi start", 500.0),
        ),
    ),
    "EUR" to TravelerDestination(
        code = "EUR",
        city = "Eurozone",
        flag = "🇪🇺",
        symbol = "€",
        tipping = "5-10%",
        tippingNote = "service dependent",
        tax = "Included",
        taxNote = "VAT in price",
        cashNote = "cards common",
        cashBufferPct = 0.15,
        paymentRails = listOf("Visa", "Mastercard", "SEPA"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 3.5),
            TravelerPriceGuide("Casual meal", 18.0),
            TravelerPriceGuide("Transit ticket", 2.5),
            TravelerPriceGuide("Taxi start", 5.0),
        ),
    ),
    "GBP" to TravelerDestination(
        code = "GBP",
        city = "London",
        flag = "🇬🇧",
        symbol = "£",
        tipping = "10-12.5%",
        tippingNote = "often optional",
        tax = "Included",
        taxNote = "VAT in price",
        cashNote = "contactless first",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Oyster"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 3.8),
            TravelerPriceGuide("Pub meal", 18.0),
            TravelerPriceGuide("Tube ride", 2.8),
            TravelerPriceGuide("Taxi start", 4.2),
        ),
    ),
    "MXN" to TravelerDestination(
        code = "MXN",
        city = "Mexico City",
        flag = "🇲🇽",
        symbol = "$",
        tipping = "10-15%",
        tippingNote = "restaurants",
        tax = "16%",
        taxNote = "usually included",
        cashNote = "carry cash",
        cashBufferPct = 0.30,
        paymentRails = listOf("Visa", "Mastercard", "Cash"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 55.0),
            TravelerPriceGuide("Tacos", 120.0),
            TravelerPriceGuide("Metro ride", 5.0),
            TravelerPriceGuide("Taxi start", 50.0),
        ),
    ),
    "BRL" to TravelerDestination(
        code = "BRL",
        city = "Sao Paulo",
        flag = "🇧🇷",
        symbol = "R$",
        tipping = "10%",
        tippingNote = "often service charge",
        tax = "Included",
        taxNote = "varies by item",
        cashNote = "cards common",
        cashBufferPct = 0.20,
        paymentRails = listOf("Visa", "Mastercard", "Pix"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 9.0),
            TravelerPriceGuide("Lunch", 45.0),
            TravelerPriceGuide("Metro ride", 5.0),
            TravelerPriceGuide("Taxi start", 6.0),
        ),
    ),
    "AUD" to TravelerDestination(
        code = "AUD",
        city = "Sydney",
        flag = "🇦🇺",
        symbol = "A$",
        tipping = "0-10%",
        tippingNote = "optional",
        tax = "10%",
        taxNote = "GST included",
        cashNote = "cards common",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Opal"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 5.0),
            TravelerPriceGuide("Casual meal", 24.0),
            TravelerPriceGuide("Transit ride", 4.5),
            TravelerPriceGuide("Taxi start", 6.5),
        ),
    ),
    "CAD" to TravelerDestination(
        code = "CAD",
        city = "Toronto",
        flag = "🇨🇦",
        symbol = "C$",
        tipping = "15-20%",
        tippingNote = "restaurants",
        tax = "+ tax",
        taxNote = "often added",
        cashNote = "cards common",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Interac"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 4.5),
            TravelerPriceGuide("Casual meal", 22.0),
            TravelerPriceGuide("Transit fare", 3.4),
            TravelerPriceGuide("Taxi start", 4.5),
        ),
    ),
    "CHF" to TravelerDestination(
        code = "CHF",
        city = "Zurich",
        flag = "🇨🇭",
        symbol = "Fr ",
        tipping = "0-10%",
        tippingNote = "round up",
        tax = "Included",
        taxNote = "VAT in price",
        cashNote = "cards common",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Twint"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 5.0),
            TravelerPriceGuide("Casual meal", 28.0),
            TravelerPriceGuide("Transit ticket", 4.4),
            TravelerPriceGuide("Taxi start", 8.0),
        ),
    ),
)

@Composable
fun MoreScreen(
    subscriptionState: SubscriptionState,
    alertsCount: Int,
    watchlistCount: Int,
    onOpenAlerts: () -> Unit,
    onOpenWatchlist: () -> Unit,
    onOpenTraveler: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    ScreenScaffold {
        ScreenHeader("More", sub = "TOOLS", subtitle = "Travel, preferences and account")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MoreRow(
                    icon = MoreFeatureIcon.Traveler,
                    title = "Traveler",
                    subtitle = "Local cheat sheets and offline rates",
                    onClick = onOpenTraveler,
                )
                MoreRow(
                    icon = MoreFeatureIcon.News,
                    title = "News",
                    subtitle = "Market stream and sentiment",
                    onClick = onOpenNews,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Alerts,
                    title = "Alerts",
                    subtitle = "$alertsCount active · price targets and breakouts",
                    onClick = onOpenAlerts,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Watchlist,
                    title = "Watchlist",
                    subtitle = "$watchlistCount currencies · custom tracking",
                    onClick = onOpenWatchlist,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Settings,
                    title = "Settings",
                    subtitle = "Theme mode, base currency and version",
                    onClick = onOpenSettings,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Pro,
                    title = if (subscriptionState.isPremium) "FX/ Pro active" else "Upgrade to Pro",
                    subtitle = subscriptionState.proStatusLabel(),
                    onClick = onOpenPaywall,
                )
            }
        }
        SectionLabel("COMING NEXT")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("WIDGETS", "Next", "home screen and watch glance", Modifier.weight(1f))
            MetricTile("PRO", if (subscriptionState.isPremium) "Active" else "Ready", "monthly plan controls", Modifier.weight(1f))
        }
    }
}

@Composable
fun AlertsScreen(
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
    onCreateAlert: (FxRate) -> Unit = {},
    onCreateManualAlert: (FxRate, AlertDirection, Double, AlertKind) -> Unit = { _, _, _, _ -> },
    onResumeAlert: (String) -> Unit = {},
    onToggleAlert: (String) -> Unit = {},
    onDeleteAlert: (String) -> Unit = {},
    onTestAlert: (PriceAlert) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val canCreate = canCreateAlert(subscriptionState, alertsState.alerts.size)
    val limitLabel = if (access.hasUnlimitedAlerts) "Unlimited" else "${alertsState.alerts.size}/${access.alertLimit}"
    val alertRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter) { liveState.alertRates() }
    val currentRatesByCode = remember(liveState.baseCurrency, alertRates) {
        alertRates.associateBy { it.code }
    }
    var selectedRateCode by remember(liveState.baseCurrency) { mutableStateOf(alertRates.firstOrNull()?.code ?: "EUR") }
    val selectedRate = alertRates.firstOrNull { it.code == selectedRateCode } ?: alertRates.firstOrNull() ?: FavoriteRates.first()
    var selectedKind by remember { mutableStateOf(AlertKind.Target) }
    var selectedDirection by remember { mutableStateOf(AlertDirection.Above) }
    var targetText by remember(selectedRate.code, selectedDirection, selectedKind) {
        mutableStateOf(defaultAlertInput(selectedRate, selectedDirection, selectedKind))
    }
    val targetValue = parseAmountInput(targetText)
    val selectedDailyChange = selectedRate.change24h
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader("Alerts", sub = "PRICE TARGETS", subtitle = "$limitLabel alerts · ${liveState.baseCurrency} base")

        BentoCard(Modifier.fillMaxWidth().heightIn(min = 144.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
                    Pill("${alertsState.activeCount} active", variant = if (alertsState.activeCount > 0) PillVariant.Up else PillVariant.Ghost)
                }
                Text("Watch breakouts without watching charts.", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    "Android checks every 15 min when online. iOS saves alerts now; push delivery is next.",
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        SectionLabel("CUSTOM ALERT")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Eyebrow("${liveState.baseCurrency} PAIR")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    alertRates.chunked(4).forEach { rowRates ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowRates.forEach { rate ->
                                Pill(
                                    text = rate.code,
                                    variant = if (rate.code == selectedRate.code) PillVariant.Accent else PillVariant.Ghost,
                                    modifier = Modifier.clickable {
                                        selectedRateCode = rate.code
                                        targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
                                    },
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertKind.entries.forEach { kind ->
                        Pill(
                            text = kind.label,
                            variant = if (kind == selectedKind) PillVariant.Accent else PillVariant.Ghost,
                            modifier = Modifier.clickable {
                                selectedKind = kind
                                targetText = defaultAlertInput(selectedRate, selectedDirection, kind)
                            },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertDirection.entries.forEach { direction ->
                        Pill(
                            text = direction.label(selectedKind),
                            variant = if (direction == selectedDirection) PillVariant.Accent else PillVariant.Ghost,
                            modifier = Modifier.clickable {
                                selectedDirection = direction
                                targetText = defaultAlertInput(selectedRate, direction, selectedKind)
                            },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    alertPresets.forEach { preset ->
                        Pill(
                            text = preset.label,
                            variant = PillVariant.Ghost,
                            modifier = Modifier.clickable {
                                selectedDirection = if (preset.percent >= 0.0) AlertDirection.Above else AlertDirection.Below
                                targetText = if (selectedKind == AlertKind.Target) {
                                    formatRate(selectedRate.rate * (1.0 + preset.percent / 100.0))
                                } else {
                                    formatPercentValue(kotlin.math.abs(preset.percent))
                                }
                            },
                        )
                    }
                }
                AlertTargetField(
                    value = targetText,
                    onValueChange = { raw ->
                        targetText = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                    },
                    pair = "${liveState.baseCurrency}/${selectedRate.code}",
                    label = if (selectedKind == AlertKind.Target) "Target rate" else "Daily move %",
                )
                PrimaryButton(
                    text = if (canCreate) "Create ${selectedDirection.label(selectedKind).lowercase()} alert" else "Unlock custom alerts",
                    onClick = {
                        if (!canCreate) {
                            onOpenPaywall()
                        } else if (targetValue > 0.0) {
                            onCreateManualAlert(selectedRate, selectedDirection, targetValue, selectedKind)
                        }
                    },
                )
                Text(
                    alertSummaryLine(selectedKind, selectedRate, selectedDirection, targetValue, selectedDailyChange),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }

        SectionLabel("QUICK CREATE")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                liveState.favorites.take(4).forEach { rate ->
                    val quickAlert = alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate)
                    val canCreateQuick = quickAlert != null || canCreate
                    AlertQuickRow(
                        baseCurrency = liveState.baseCurrency,
                        rate = rate,
                        state = when {
                            quickAlert?.enabled == true -> QuickAlertState.Active
                            quickAlert != null -> QuickAlertState.Paused
                            canCreate -> QuickAlertState.Create
                            else -> QuickAlertState.Locked
                        },
                        enabled = canCreateQuick,
                        onCreate = {
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

        if (!canCreate) {
            ProUpsellCard(
                title = "Create unlimited alerts",
                subtitle = "Free includes ${access.alertLimit}; Pro unlocks every pair, range and breakout alert.",
                onClick = onOpenPaywall,
            )
        }

        SectionLabel("ACTIVE ALERTS")
        if (alertsState.alerts.isEmpty()) {
            BentoCard(padding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Eyebrow("NO ALERTS YET")
                    Text("Create one from a favorite currency or from any detail screen.", style = FxTheme.typography.body, color = FxTheme.colors.textDim)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alertsState.alerts.forEach { alert ->
                    val currentRate = currentRatesByCode[alert.quote]?.rate.takeIf { alert.base == liveState.baseCurrency }
                    AlertCard(
                        alert = alert,
                        currentRate = currentRate,
                        currentChangePct = currentRatesByCode[alert.quote]?.change24h.takeIf { alert.base == liveState.baseCurrency },
                        onToggle = onToggleAlert,
                        onDelete = onDeleteAlert,
                        onTest = onTestAlert,
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistScreen(
    liveState: LiveRatesState,
    watchlistState: WatchlistState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
    onToggleCurrency: (String) -> Unit = {},
    onSetHolding: (String, Double) -> Unit = { _, _ -> },
    onOpenDetail: (FxRate) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val allRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter) { liveState.portfolioRates() }
    val limitLabel = if (access.hasUnlimitedWatchlistCurrencies) "Unlimited" else "${watchlistState.watchlist.codes.size}/${access.watchlistCurrencyLimit}"
    val holdings = remember(liveState.baseCurrency, allRates, watchlistState.watchlist) {
        watchlistState.watchlist.codes.mapNotNull { code ->
            val rate = allRates.firstOrNull { it.code == code } ?: return@mapNotNull null
            PortfolioHolding(
                rate = rate,
                amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0,
            )
        }.sortedWith(compareByDescending<PortfolioHolding> { it.baseValue }.thenBy { it.rate.code })
    }
    val valuedHoldings = holdings.filter { it.amount > 0.0 }
    val portfolioValue = valuedHoldings.sumOf { it.baseValue }
    val portfolioDailyChange = valuedHoldings.sumOf { it.dailyChangeInBase }
    val nonZeroHoldings = holdings.count { it.amount > 0.0 }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader("Watchlist", sub = "CUSTOM TRACKING", subtitle = "$limitLabel currencies · ${liveState.baseCurrency} base")

        BentoCard(Modifier.fillMaxWidth().heightIn(min = 148.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
                    Pill("${holdings.size} tracked", variant = if (holdings.isNotEmpty()) PillVariant.Accent else PillVariant.Ghost)
                }
                Text("Tracked currencies", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                if (nonZeroHoldings == 0) {
                    BigValueText("${holdings.size}", " tracked")
                    Text(
                        "Add amounts below to value your portfolio.",
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    BigValueText("${liveState.baseCurrency} ${formatMoneyValue(portfolioValue)}")
                    Text(
                        "${formatPortfolioChange(portfolioDailyChange, liveState.baseCurrency)} today · $nonZeroHoldings holdings valued",
                        style = FxTheme.typography.caption,
                        color = if (portfolioDailyChange >= 0.0) FxTheme.colors.up else FxTheme.colors.down,
                    )
                }
            }
        }

        SectionLabel("PORTFOLIO HOLDINGS")
        if (holdings.isEmpty()) {
            BentoCard(padding = 14.dp) {
                Text("Choose currencies below to start tracking.", style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        } else {
            if (nonZeroHoldings == 0) {
                BentoCard(padding = 12.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Eyebrow("HOW IT WORKS")
                        Text(
                            "Watchlist follows rates. Portfolio value appears after you enter how much you hold.",
                            style = FxTheme.typography.caption,
                            color = FxTheme.colors.textDim,
                        )
                    }
                }
            }
            BentoCard(padding = 8.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    holdings.forEach { holding ->
                        PortfolioHoldingRow(
                            baseCurrency = liveState.baseCurrency,
                            holding = holding,
                            portfolioValue = portfolioValue,
                            onAmountChange = { amount -> onSetHolding(holding.rate.code, amount) },
                            onOpenDetail = { onOpenDetail(holding.rate) },
                        )
                    }
                }
            }
        }

        SectionLabel("ADD OR REMOVE")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                allRates.forEach { rate ->
                    WatchlistCurrencyRow(
                        rate = rate,
                        selected = rate.code in watchlistState.watchlist.codes,
                        locked = rate.code !in watchlistState.watchlist.codes &&
                            !access.hasUnlimitedWatchlistCurrencies &&
                            watchlistState.watchlist.codes.size >= access.watchlistCurrencyLimit,
                        amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0,
                        onToggle = { onToggleCurrency(rate.code) },
                    )
                }
            }
        }

        if (!access.hasUnlimitedWatchlistCurrencies && watchlistState.watchlist.codes.size >= access.watchlistCurrencyLimit) {
            ProUpsellCard(
                title = "Track unlimited currencies",
                subtitle = "Free includes ${access.watchlistCurrencyLimit}; Pro unlocks bigger watchlists for alerts and widgets.",
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
private fun PortfolioHoldingRow(
    baseCurrency: String,
    holding: PortfolioHolding,
    portfolioValue: Double,
    onAmountChange: (Double) -> Unit,
    onOpenDetail: () -> Unit,
) {
    val rate = holding.rate
    val amount = holding.amount
    val focusManager = LocalFocusManager.current
    var amountText by remember(rate.code, amount) { mutableStateOf(if (amount > 0.0) formatRate(amount) else "") }
    var amountFocused by remember(rate.code) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp, modifier = Modifier.clickable(onClick = onOpenDetail))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("${rate.code} holding", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text, modifier = Modifier.clickable(onClick = onOpenDetail))
            val holdingSubtitle = if (amount <= 0.0) {
                "Tracking live rate ${formatRate(rate.rate)} · enter amount held"
            } else {
                "$baseCurrency ${formatMoneyValue(holding.baseValue)} · ${holding.weightLabel(portfolioValue)} · ${holding.dailyChangeLabel(baseCurrency)}"
            }
            Text(
                holdingSubtitle,
                style = FxTheme.typography.captionMono,
                color = if (amount <= 0.0) FxTheme.colors.textFaint else if (holding.dailyChangeInBase >= 0.0) FxTheme.colors.up else FxTheme.colors.down,
            )
        }
        Column(
            modifier = Modifier.width(104.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            BasicTextField(
                value = amountText,
                onValueChange = { raw ->
                    val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                    amountText = next
                    onAmountChange(parseAmountInput(next))
                },
                singleLine = true,
                textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FxTheme.shapes.field)
                    .background(if (amountFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                    .border(1.dp, if (amountFocused) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .onFocusChanged { amountFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    if (amountText.isBlank()) {
                        Text(
                            "amount",
                            style = FxTheme.typography.captionMono,
                            color = FxTheme.colors.textGhost,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    }
                    innerTextField()
                },
            )
            if (amountFocused) {
                Text(
                    "done",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier.clickable { focusManager.clearFocus() },
                )
            }
        }
    }
}

@Composable
private fun WatchlistCurrencyRow(
    rate: FxRate,
    selected: Boolean,
    locked: Boolean,
    amount: Double,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else Color.Transparent)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(
                if (amount > 0.0) "${formatRate(amount)} held · ${rate.name}" else rate.name,
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
            )
        }
        Text(formatRate(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.textDim)
        Pill(
            text = when {
                selected -> "tracked"
                locked -> "pro"
                else -> "add"
            },
            variant = if (selected) PillVariant.Accent else if (locked) PillVariant.Accent else PillVariant.Ghost,
        )
    }
}

@Composable
private fun AlertTargetField(
    value: String,
    onValueChange: (String) -> Unit,
    pair: String,
    label: String,
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("$label · $pair", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text("0.0000", style = FxTheme.typography.numberL, color = FxTheme.colors.textGhost)
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun AlertQuickRow(
    baseCurrency: String,
    rate: FxRate,
    state: QuickAlertState,
    enabled: Boolean,
    onCreate: () -> Unit,
    onLocked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .clickable(onClick = if (enabled) onCreate else onLocked)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("$baseCurrency / ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text("Above ${formatRate(rate.rate * 1.01)} · current ${formatRate(rate.rate)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        Pill(state.label, variant = state.variant)
    }
}

@Composable
private fun AlertCard(
    alert: PriceAlert,
    currentRate: Double?,
    currentChangePct: Double?,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTest: (PriceAlert) -> Unit,
) {
    val isHit = alert.isHit(currentRate, currentChangePct)
    BentoCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                FlagDot(if (alert.kind == AlertKind.Target) "◎" else "%", CurrencyKind.Fiat, 32.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
                        "${alert.direction.label(alert.kind)} ${alert.targetLabel()} · ${alert.statusLabel(currentRate, currentChangePct)}",
                        style = FxTheme.typography.captionMono,
                        color = if (isHit) FxTheme.colors.up else FxTheme.colors.textFaint,
                    )
                }
                Pill(if (alert.enabled) "on" else "paused", variant = if (alert.enabled) PillVariant.Up else PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetricTile(
                    if (alert.kind == AlertKind.Target) "CURRENT" else "24H MOVE",
                    if (alert.kind == AlertKind.Target) currentRate?.let(::formatRate) ?: "--" else currentChangePct?.let(::formatSignedPercent) ?: "--",
                    alert.distanceLabel(currentRate, currentChangePct),
                    Modifier.weight(1f).height(72.dp),
                )
                MetricTile(
                    "LAST HIT",
                    alert.lastTriggeredAtMillis?.let(::shortAgeLabel) ?: "Never",
                    if (alert.enabled) "monitoring" else "paused",
                    Modifier.weight(1f).height(72.dp),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (alert.enabled) "pause" else "resume",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textDim,
                    modifier = Modifier.clickable { onToggle(alert.id) },
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    "test",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier.clickable { onTest(alert) },
                )
                Spacer(Modifier.width(14.dp))
                Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textFaint, modifier = Modifier.clickable { onDelete(alert.id) })
            }
        }
    }
}

@Composable
private fun MoreRow(
    icon: MoreFeatureIcon,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(FxTheme.shapes.icon)
                .background(FxTheme.colors.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            MoreFeatureIconView(icon)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}

private enum class MoreFeatureIcon {
    Traveler,
    News,
    Alerts,
    Watchlist,
    Settings,
    Pro,
}

@Composable
private fun MoreFeatureIconView(icon: MoreFeatureIcon) {
    val accent = FxTheme.colors.accent
    Canvas(Modifier.size(30.dp)) {
        val lineWidth = 2.2.dp.toPx()
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        val thinStroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        fun iconLine(startX: Float, startY: Float, endX: Float, endY: Float, strokeWidth: Float = lineWidth) {
            drawLine(
                color = accent,
                start = Offset(w * startX, h * startY),
                end = Offset(w * endX, h * endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        when (icon) {
            MoreFeatureIcon.Traveler -> {
                iconLine(0.34f, 0.22f, 0.66f, 0.22f)
                iconLine(0.34f, 0.22f, 0.34f, 0.32f)
                iconLine(0.66f, 0.22f, 0.66f, 0.32f)
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.22f, h * 0.32f),
                    size = Size(w * 0.56f, h * 0.46f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke,
                )
                iconLine(0.36f, 0.44f, 0.36f, 0.66f, thinStroke.width)
                iconLine(0.64f, 0.44f, 0.64f, 0.66f, thinStroke.width)
            }
            MoreFeatureIcon.News -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.24f, h * 0.16f),
                    size = Size(w * 0.52f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                    style = stroke,
                )
                iconLine(0.34f, 0.36f, 0.66f, 0.36f, thinStroke.width)
                iconLine(0.34f, 0.50f, 0.66f, 0.50f, thinStroke.width)
                iconLine(0.34f, 0.64f, 0.56f, 0.64f, thinStroke.width)
            }
            MoreFeatureIcon.Alerts -> {
                val bell = Path().apply {
                    moveTo(w * 0.31f, h * 0.60f)
                    quadraticTo(w * 0.32f, h * 0.34f, w * 0.50f, h * 0.30f)
                    quadraticTo(w * 0.68f, h * 0.34f, w * 0.69f, h * 0.60f)
                    lineTo(w * 0.76f, h * 0.70f)
                    lineTo(w * 0.24f, h * 0.70f)
                    close()
                }
                drawPath(bell, accent, style = stroke)
                iconLine(0.45f, 0.22f, 0.55f, 0.22f)
                drawArc(accent, 15f, 150f, false, Offset(w * 0.42f, h * 0.68f), Size(w * 0.16f, h * 0.16f), style = thinStroke)
            }
            MoreFeatureIcon.Watchlist -> {
                drawCircle(accent, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                val chart = Path().apply {
                    moveTo(w * 0.30f, h * 0.58f)
                    lineTo(w * 0.43f, h * 0.46f)
                    lineTo(w * 0.52f, h * 0.54f)
                    lineTo(w * 0.70f, h * 0.36f)
                }
                drawPath(chart, accent, style = thinStroke)
                drawCircle(accent, radius = w * 0.035f, center = Offset(w * 0.70f, h * 0.36f))
            }
            MoreFeatureIcon.Settings -> {
                drawCircle(accent, radius = w * 0.17f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                listOf(
                    Offset(w * 0.50f, h * 0.18f) to Offset(w * 0.50f, h * 0.28f),
                    Offset(w * 0.50f, h * 0.72f) to Offset(w * 0.50f, h * 0.82f),
                    Offset(w * 0.18f, h * 0.50f) to Offset(w * 0.28f, h * 0.50f),
                    Offset(w * 0.72f, h * 0.50f) to Offset(w * 0.82f, h * 0.50f),
                    Offset(w * 0.28f, h * 0.28f) to Offset(w * 0.35f, h * 0.35f),
                    Offset(w * 0.65f, h * 0.65f) to Offset(w * 0.72f, h * 0.72f),
                    Offset(w * 0.72f, h * 0.28f) to Offset(w * 0.65f, h * 0.35f),
                    Offset(w * 0.35f, h * 0.65f) to Offset(w * 0.28f, h * 0.72f),
                ).forEach { (start, end) ->
                    drawLine(accent, start, end, strokeWidth = lineWidth, cap = StrokeCap.Round)
                }
            }
            MoreFeatureIcon.Pro -> {
                drawCircle(accent, radius = w * 0.18f, center = Offset(w * 0.36f, h * 0.50f), style = stroke)
                drawCircle(accent, radius = w * 0.18f, center = Offset(w * 0.64f, h * 0.50f), style = stroke)
                iconLine(0.44f, 0.38f, 0.56f, 0.62f)
                iconLine(0.44f, 0.62f, 0.56f, 0.38f)
            }
        }
    }
}

@Composable
fun NewsScreen(
    newsState: NewsUiState = NewsUiState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onRefresh: () -> Unit = {},
    onRegionSelected: (String) -> Unit = {},
    onCurrencySelected: (String) -> Unit = {},
    onOpenStory: (NewsStory) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    var query by remember { mutableStateOf("") }
    val filteredStories = remember(newsState.stories, query, newsState.selectedCurrency) {
        newsState.stories.filter { story ->
            val normalizedQuery = query.trim()
            val matchesQuery = normalizedQuery.isBlank() ||
                story.title.contains(normalizedQuery, ignoreCase = true) ||
                story.summary.contains(normalizedQuery, ignoreCase = true) ||
                story.tag.contains(normalizedQuery, ignoreCase = true) ||
                story.moves.any { it.first.contains(normalizedQuery, ignoreCase = true) }
            val matchesCurrency = newsState.selectedCurrency.isBlank() ||
                newsState.selectedCurrency == "USD" ||
                story.moves.any { it.first == newsState.selectedCurrency } ||
                story.tag == newsState.selectedCurrency
            matchesQuery && matchesCurrency
        }.ifEmpty {
            if (query.isBlank()) newsState.stories else emptyList()
        }
    }
    val visibleStories = filteredStories.take(access.newsStoryLimit.cap(filteredStories.size))
    val regionOptions = listOf("US", "AU", "GB", "EU", "BR", "MX", "JP")
    val currencyOptions = (newsState.trackedCurrencies + listOf("USD", "EUR", "GBP", "JPY", "AUD", "BTC")).distinct()
    ScreenScaffold {
        ScreenHeader(
            "News",
            sub = if (access.canUseAdvancedNews) "MARKET STREAM" else "MARKET PREVIEW",
            subtitle = "${newsState.provider} · ${newsState.region} · ${newsState.selectedCurrency} focus",
            right = { Text("↻", style = FxTheme.typography.numberL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onRefresh)) },
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("SENTIMENT")
                    if (newsState.isLoading) {
                        Eyebrow("REFRESHING", color = FxTheme.colors.accent)
                    }
                }
                SentimentBar(newsState.bullish, newsState.neutral, newsState.bearish)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendDot("BULLISH ${newsState.bullish}%", FxTheme.colors.up)
                    LegendDot("NEUTRAL ${newsState.neutral}%", FxTheme.colors.textGhost)
                    LegendDot("BEARISH ${newsState.bearish}%", FxTheme.colors.down)
                }
                KeyValueRow("Feed", "${newsState.language.uppercase()} · ${newsState.trackedCurrencies.joinToString(", ")}")
            }
        }
        BentoCard(padding = 10.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NewsSearchField(query = query, onQueryChange = { query = it })
                NewsFilterRow(
                    label = "REGION",
                    options = regionOptions,
                    selected = newsState.region,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { region ->
                        if (access.canUseAdvancedNews) onRegionSelected(region) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
                    label = "CURRENCY",
                    options = currencyOptions,
                    selected = newsState.selectedCurrency,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { code ->
                        if (access.canUseAdvancedNews) onCurrencySelected(code) else onOpenPaywall()
                    },
                )
            }
        }
        SectionLabel("RECENT LINES · ${filteredStories.size}")
        if (newsState.errorMessage != null) {
            Text("News backend unavailable", style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        if (visibleStories.isEmpty()) {
            BentoCard(padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No matching stories", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text("Try a different currency, region or search term.", style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                }
            }
        }
        visibleStories.forEach { story ->
            StoryCard(story, onClick = { onOpenStory(story) })
        }
        if (!access.canUseAdvancedNews || visibleStories.size < filteredStories.size) {
            ProUpsellCard(
                title = "Personalize the market stream",
                subtitle = if (visibleStories.size < filteredStories.size) {
                    "Showing ${visibleStories.size}/${filteredStories.size} stories. Pro unlocks the full regional stream."
                } else {
                    "Pro unlocks more stories and filters by region, currencies and topics."
                },
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
fun NewsDetailScreen(
    story: NewsStory?,
    onBack: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    val selected = story ?: NewsStory(
        tag = "FX",
        impact = "MED",
        age = "Now",
        title = "Market update",
        summary = "Latest currency market context.",
        moves = emptyList(),
        source = "FX Always",
        sourceUrl = "",
    )
    val impactColor = if (selected.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent
    ScreenScaffold {
        BackNavButton(label = "News", onClick = onBack)
        ScreenHeader(
            "News detail",
            sub = "${selected.tag} · ${selected.impact}",
            subtitle = "${selected.source.ifBlank { "Market source" }} · ${selected.age}",
        )
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(selected.tag, variant = PillVariant.Accent)
                    Eyebrow(selected.impact, color = impactColor)
                }
                Text(selected.title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(selected.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        }
        SectionLabel("MARKET MOVES")
        BentoCard(padding = 12.dp) {
            if (selected.moves.isEmpty()) {
                Text("No direct currency move was detected for this story.", style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    selected.moves.forEach { (code, change) ->
                        KeyValueRow(code, formatChange(change))
                    }
                }
            }
        }
        SectionLabel("SOURCE")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KeyValueRow("Publisher", selected.source.ifBlank { "Market source" })
                KeyValueRow("Published", selected.age)
                if (selected.sourceUrl.isNotBlank()) {
                    GhostButton("Open original source", onClick = { onOpenUrl(selected.sourceUrl) })
                } else {
                    Text("This item is generated from the fallback market brief, so there is no external article link.", style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    baseCurrency: String,
    availableBaseCurrencies: List<FxRate> = SettingsBaseCurrencies,
    backupState: UserBackupState,
    backupSyncing: Boolean,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit,
    onRestorePurchase: () -> Unit,
    onSyncNow: () -> Unit,
    onLinkGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDevPremiumChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
) {
    val access = subscriptionState.featureAccess()
    val fullBaseCurrencies = availableBaseCurrencies.ifEmpty { SettingsBaseCurrencies }
    val canUseAllBaseCurrencies = access.baseCurrencyLimit == Int.MAX_VALUE
    val baseCurrencyLimit = if (canUseAllBaseCurrencies) 12 else access.baseCurrencyLimit.cap(fullBaseCurrencies.size)
    val baseCurrencies = remember(fullBaseCurrencies, baseCurrency, baseCurrencyLimit) {
        compactCurrencyChoices(fullBaseCurrencies, baseCurrency, baseCurrencyLimit)
    }
    var showBaseCurrencyPicker by remember { mutableStateOf(false) }
    if (showBaseCurrencyPicker) {
        CurrencyPickerSheet(
            title = "Choose base currency",
            subtitle = "${fullBaseCurrencies.size} supported live currencies",
            currencies = fullBaseCurrencies,
            selectedCode = baseCurrency,
            onDismiss = { showBaseCurrencyPicker = false },
            onSelect = { code ->
                showBaseCurrencyPicker = false
                onBaseCurrencyChange(code)
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader("Settings", sub = "APP PREFERENCES", subtitle = "Theme, base currency and build info")

        SectionLabel("BACKUP")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AccountBackupCard(
                    backupState = backupState,
                    lastSyncedAtMillis = lastSyncedAtMillis,
                    backupSyncing = backupSyncing,
                    onClick = onSyncNow,
                )
                SettingChoiceRow(
                    title = "Sync now",
                    subtitle = "Push the latest settings, alerts and watchlist to Firebase",
                    selected = false,
                    actionLabel = if (backupSyncing) "syncing" else "sync",
                    onClick = onSyncNow,
                )
                if (backupState.isAnonymous) {
                    val providerLabel = when (PlatformConfig.platform) {
                        Platform.Android -> "Google"
                        Platform.Ios -> "Apple"
                    }
                    val deviceLabel = when (PlatformConfig.platform) {
                        Platform.Android -> "Android phone"
                        Platform.Ios -> "iPhone"
                    }
                    SettingChoiceRow(
                        title = "Sign in with $providerLabel",
                        subtitle = "Keep the same backup and restore it on a new $deviceLabel",
                        selected = false,
                        actionLabel = "connect",
                        onClick = onLinkGoogle,
                    )
                } else {
                    SettingChoiceRow(
                        title = "Sign out",
                        subtitle = "Keep local data and continue with a new guest backup",
                        selected = false,
                        actionLabel = "sign out",
                        onClick = onSignOut,
                    )
                }
            }
        }

        SectionLabel("SUBSCRIPTION")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = if (subscriptionState.isPremium) "FX/ Pro active" else "FX/ Free",
                    subtitle = subscriptionState.statusMessage ?: subscriptionState.proStatusLabel(),
                    selected = subscriptionState.isPremium,
                    actionLabel = if (subscriptionState.isPremium) "view" else "upgrade",
                    onClick = onOpenPaywall,
                )
                SettingChoiceRow(
                    title = "Restore purchase",
                    subtitle = "Recover an existing Play/App Store subscription",
                    selected = false,
                    actionLabel = "restore",
                    onClick = onRestorePurchase,
                )
            }
        }

        SectionLabel("THEME MODE")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ThemeMode.entries.forEach { mode ->
                    SettingChoiceRow(
                        title = mode.label,
                        subtitle = mode.subtitle,
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
        }

        SectionLabel("BASE CURRENCY")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                baseCurrencies.forEach { currency ->
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = currency.name,
                        selected = baseCurrency == currency.code,
                        onClick = { onBaseCurrencyChange(currency.code) },
                    )
                }
                SettingChoiceRow(
                    title = "More currencies",
                    subtitle = if (canUseAllBaseCurrencies) {
                        "Search ${fullBaseCurrencies.size} supported base currencies"
                    } else {
                        "Free includes ${baseCurrencies.size}; Pro unlocks ${fullBaseCurrencies.size}"
                    },
                    selected = false,
                    actionLabel = "more +",
                    onClick = {
                        if (canUseAllBaseCurrencies) showBaseCurrencyPicker = true else onOpenPaywall()
                    },
                )
            }
        }
        if (!canUseAllBaseCurrencies && baseCurrencies.size < fullBaseCurrencies.size) {
            ProUpsellCard(
                title = "Unlock all base currencies",
                subtitle = "Free includes ${baseCurrencies.size}; Pro unlocks ${fullBaseCurrencies.size} supported base currencies.",
                onClick = onOpenPaywall,
            )
        }

        SectionLabel("DEV")
        BentoCard(padding = 8.dp) {
            SettingChoiceRow(
                title = "Simulate ${if (subscriptionState.isPremium) "Free" else "Pro"}",
                subtitle = "Temporary tester switch before RevenueCat is connected",
                selected = subscriptionState.isPremium,
                actionLabel = if (subscriptionState.isPremium) "set free" else "set pro",
                onClick = { onDevPremiumChange(!subscriptionState.isPremium) },
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Version ${PlatformConfig.versionName}",
            style = FxTheme.typography.captionMono,
            color = FxTheme.colors.textFaint,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AccountBackupCard(
    backupState: UserBackupState,
    lastSyncedAtMillis: Long?,
    backupSyncing: Boolean,
    onClick: () -> Unit,
) {
    val signedIn = backupState.isAvailable && !backupState.isAnonymous
    val title = if (signedIn) {
        "Signed in with ${backupState.providerLabel ?: "account"}"
    } else {
        backupState.title
    }
    val identity = when {
        signedIn && backupState.email != null -> backupState.email
        signedIn && backupState.displayName != null -> backupState.displayName
        else -> backupState.subtitle(lastSyncedAtMillis)
    }
    val initial = when {
        signedIn && !backupState.displayName.isNullOrBlank() -> backupState.displayName.first().uppercaseChar().toString()
        signedIn && !backupState.email.isNullOrBlank() -> backupState.email.first().uppercaseChar().toString()
        else -> "G"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(if (signedIn) FxTheme.colors.accentSoft else Color.Transparent)
            .border(1.dp, if (signedIn) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (signedIn) FxTheme.colors.accent else FxTheme.colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, style = FxTheme.typography.bodyStrong, color = if (signedIn) FxTheme.colors.bg else FxTheme.colors.text)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(identity, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            if (signedIn) {
                Text(formatLastSynced(lastSyncedAtMillis), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
            }
            if (backupState.errorMessage != null) {
                Text(backupState.errorMessage, style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
            }
        }
        Pill(
            if (backupSyncing) "syncing" else if (signedIn) backupState.providerLabel ?: "account" else backupState.actionLabel,
            variant = if (signedIn || backupState.isAvailable) PillVariant.Accent else PillVariant.Ghost,
        )
    }
}

@Composable
private fun SettingChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    actionLabel: String = if (selected) "active" else "select",
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else Color.Transparent)
            .border(if (selected) 1.dp else 0.dp, if (selected) FxTheme.colors.accentLine else Color.Transparent, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Pill(actionLabel, variant = if (selected) PillVariant.Accent else PillVariant.Ghost)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    title: String,
    subtitle: String,
    currencies: List<FxRate>,
    selectedCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val rows = remember(currencies, query) {
        val term = query.trim()
        currencies
            .distinctBy { it.code }
            .filter { currency ->
                term.isBlank() ||
                    currency.code.contains(term, ignoreCase = true) ||
                    currency.name.contains(term, ignoreCase = true)
            }
            .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes }.thenBy { it.name })
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FxTheme.colors.surface1,
        contentColor = FxTheme.colors.text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            }
            BentoCard(padding = 12.dp) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it.take(24) },
                    singleLine = true,
                    textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (query.isBlank()) {
                            Text("Search currency", style = FxTheme.typography.body, color = FxTheme.colors.textGhost)
                        }
                        innerTextField()
                    },
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rows.forEach { currency ->
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = currency.name,
                        selected = currency.code == selectedCode,
                        onClick = { onSelect(currency.code) },
                    )
                }
                if (rows.isEmpty()) {
                    Text("No currencies found", style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterCurrencyPickerSheet(
    currencies: List<FxRate>,
    selectedCodes: List<String>,
    limit: Int,
    isPremium: Boolean,
    onDismiss: () -> Unit,
    onOpenPaywall: () -> Unit,
    onApply: (List<String>) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var draftCodes by remember(selectedCodes) { mutableStateOf(selectedCodes) }
    val effectiveLimit = limit.cap(currencies.size).coerceAtLeast(1)
    val rows = remember(currencies, query) {
        val term = query.trim()
        currencies
            .distinctBy { it.code }
            .filter { currency ->
                term.isBlank() ||
                    currency.code.contains(term, ignoreCase = true) ||
                    currency.name.contains(term, ignoreCase = true)
            }
            .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes }.thenBy { it.code })
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FxTheme.colors.surface1,
        contentColor = FxTheme.colors.text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 660.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Edit converter list", style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(
                    if (isPremium) {
                        "${draftCodes.size} selected · every supported currency available"
                    } else {
                        "${draftCodes.size}/$effectiveLimit selected · Pro unlocks more currencies"
                    },
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textFaint,
                )
            }
            BentoCard(padding = 12.dp) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it.take(24) },
                    singleLine = true,
                    textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (query.isBlank()) {
                            Text("Search currency", style = FxTheme.typography.body, color = FxTheme.colors.textGhost)
                        }
                        innerTextField()
                    },
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rows.forEach { currency ->
                    val selected = currency.code in draftCodes
                    val locked = !selected && draftCodes.size >= effectiveLimit
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = if (locked && !isPremium) "Pro unlocks more converter currencies" else currency.name,
                        selected = selected,
                        actionLabel = if (selected) "added" else if (locked) "pro" else "add",
                        onClick = {
                            when {
                                selected -> draftCodes = draftCodes.filterNot { it == currency.code }
                                locked -> onOpenPaywall()
                                else -> draftCodes = (draftCodes + currency.code).distinct()
                            }
                        },
                    )
                }
                if (rows.isEmpty()) {
                    Text("No currencies found", style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostButton("Cancel", Modifier.weight(1f), onClick = onDismiss)
                PrimaryButton(
                    "Apply",
                    Modifier.weight(1f),
                    onClick = {
                        if (draftCodes.isNotEmpty()) {
                            onApply(draftCodes.take(effectiveLimit))
                        }
                    },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

private val PopularCurrencyCodes = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "BRL", "MXN", "NZD", "SGD")

private fun compactCurrencyChoices(
    currencies: List<FxRate>,
    selectedCode: String,
    limit: Int,
): List<FxRate> {
    val distinct = currencies.distinctBy { it.code }
    val byCode = distinct.associateBy { it.code }
    val selected = byCode[selectedCode]
    val popular = PopularCurrencyCodes.mapNotNull { byCode[it] }
    return (listOfNotNull(selected) + popular.filterNot { it.code == selectedCode })
        .take(limit)
        .ifEmpty { distinct.take(limit) }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "System"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
    }

private val ThemeMode.subtitle: String
    get() = when (this) {
        ThemeMode.System -> "Follow device appearance"
        ThemeMode.Light -> "Use the bright interface"
        ThemeMode.Dark -> "Use the dark trading interface"
    }

private val UserBackupState.title: String
    get() = when {
        isAvailable && isAnonymous -> "Guest backup active"
        isAvailable -> "${providerLabel ?: "Account"} backup active"
        else -> "Backup unavailable"
    }

private fun UserBackupState.subtitle(lastSyncedAtMillis: Long?): String {
    val syncLabel = lastSyncedAtMillis?.let { " · ${formatLastSynced(it)}" }.orEmpty()
    val base = when {
        isAvailable && uid?.startsWith("ios-anon-") == true && isAnonymous -> "Local iOS guest ${uid.takeLast(8)}"
        isAvailable && uid != null && isAnonymous -> "Firebase guest ${uid.take(8)}"
        isAvailable && uid != null -> "Restores on any signed-in device"
        isAvailable -> "Preferences, alerts and watchlist sync to Firebase"
        else -> "Firebase Auth has not started on this platform"
    }
    return if (errorMessage != null) "$base · $errorMessage" else "$base$syncLabel"
}

private val UserBackupState.actionLabel: String
    get() = if (isAvailable) "active" else "offline"

private fun formatLastSynced(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 15 -> "synced just now"
        elapsedSeconds < 60 -> "synced ${elapsedSeconds}s ago"
        elapsedSeconds < 3600 -> "synced ${elapsedSeconds / 60}m ago"
        elapsedSeconds < 86_400 -> "synced ${elapsedSeconds / 3600}h ago"
        else -> "synced ${elapsedSeconds / 86_400}d ago"
    }
}

private fun formatLastSynced(millis: Long?): String =
    millis?.let(::formatLastSynced) ?: "Sync pending"

private val AlertKind.label: String
    get() = when (this) {
        AlertKind.Target -> "Target"
        AlertKind.DailyChange -> "Daily move"
    }

private fun AlertDirection.label(kind: AlertKind): String =
    when (kind) {
        AlertKind.Target -> when (this) {
            AlertDirection.Above -> "Above"
            AlertDirection.Below -> "Below"
        }
        AlertKind.DailyChange -> when (this) {
            AlertDirection.Above -> "Up"
            AlertDirection.Below -> "Down"
        }
    }

private enum class QuickAlertState(
    val label: String,
    val variant: PillVariant,
) {
    Create("create", PillVariant.Ghost),
    Active("active", PillVariant.Up),
    Paused("resume", PillVariant.Ghost),
    Locked("pro", PillVariant.Accent),
}

private data class AlertPreset(
    val label: String,
    val percent: Double,
)

private val alertPresets = listOf(
    AlertPreset("-1%", -1.0),
    AlertPreset("-0.5%", -0.5),
    AlertPreset("+0.5%", 0.5),
    AlertPreset("+1%", 1.0),
)

private fun List<PriceAlert>.findQuickAlert(baseCurrency: String, rate: FxRate): PriceAlert? {
    val target = quickAlertTarget(rate)
    return firstOrNull {
        it.base == baseCurrency &&
            it.quote == rate.code &&
            it.kind == AlertKind.Target &&
            it.direction == AlertDirection.Above &&
            kotlin.math.abs(it.target - target) < ALERT_TARGET_TOLERANCE
    }
}

private fun quickAlertTarget(rate: FxRate): Double =
    rate.rate * 1.01

private fun PriceAlert.isHit(currentRate: Double?, currentChangePct: Double?): Boolean =
    when (kind) {
        AlertKind.Target -> {
            if (currentRate == null) false else when (direction) {
                AlertDirection.Above -> currentRate >= target
                AlertDirection.Below -> currentRate <= target
            }
        }
        AlertKind.DailyChange -> {
            if (currentChangePct == null) false else when (direction) {
                AlertDirection.Above -> currentChangePct >= target
                AlertDirection.Below -> currentChangePct <= -target
            }
        }
    }

private fun PriceAlert.statusLabel(currentRate: Double?, currentChangePct: Double?): String =
    when {
        kind == AlertKind.Target && currentRate == null -> "waiting for ${base} live rate"
        kind == AlertKind.DailyChange && currentChangePct == null -> "waiting for 24h change"
        isHit(currentRate, currentChangePct) -> "target hit"
        kind == AlertKind.Target && currentRate != null -> "${distancePercent(currentRate)}% away"
        kind == AlertKind.DailyChange && currentChangePct != null -> "${dailyChangeDistancePercent(currentChangePct)} pts away"
        else -> "waiting"
    }

private fun PriceAlert.distanceLabel(currentRate: Double?, currentChangePct: Double?): String =
    when {
        kind == AlertKind.Target && currentRate == null -> "base changed"
        kind == AlertKind.DailyChange && currentChangePct == null -> "waiting"
        isHit(currentRate, currentChangePct) -> "target reached"
        kind == AlertKind.Target && currentRate != null -> "${distancePercent(currentRate)}% to target"
        kind == AlertKind.DailyChange && currentChangePct != null -> "${dailyChangeDistancePercent(currentChangePct)} pts to move"
        else -> "waiting"
    }

private fun PriceAlert.targetLabel(): String =
    when (kind) {
        AlertKind.Target -> formatRate(target)
        AlertKind.DailyChange -> "${formatPercentValue(target)}%"
    }

private fun PriceAlert.dailyChangeDistancePercent(currentChangePct: Double): String {
    val threshold = if (direction == AlertDirection.Above) target else -target
    val distance = kotlin.math.abs(threshold - currentChangePct).coerceAtLeast(0.0)
    return if (distance < 0.1) "<0.1" else formatPercentValue(distance)
}

private fun PriceAlert.distancePercent(currentRate: Double): String {
    val distance = when (direction) {
        AlertDirection.Above -> (target - currentRate) / currentRate
        AlertDirection.Below -> (currentRate - target) / currentRate
    }.coerceAtLeast(0.0) * 100.0
    return if (distance < 0.1) "<0.1" else ((distance * 10).toInt() / 10.0).toString()
}

private fun defaultAlertInput(rate: FxRate, direction: AlertDirection, kind: AlertKind): String =
    when (kind) {
        AlertKind.Target -> {
            val multiplier = if (direction == AlertDirection.Above) 1.01 else 0.99
            formatRate(rate.rate * multiplier)
        }
        AlertKind.DailyChange -> "1.0"
    }

private fun alertSummaryLine(
    kind: AlertKind,
    rate: FxRate,
    direction: AlertDirection,
    targetValue: Double,
    currentChangePct: Double,
): String =
    when (kind) {
        AlertKind.Target -> "Current ${formatRate(rate.rate)} · target ${if (targetValue > 0.0) formatRate(targetValue) else "--"}"
        AlertKind.DailyChange -> {
            val threshold = if (targetValue > 0.0) "${direction.label(kind).lowercase()} ${formatPercentValue(targetValue)}%" else "--"
            "24h ${formatSignedPercent(currentChangePct)} · alert at $threshold"
        }
    }

private fun formatPercentValue(value: Double): String =
    ((value * 10.0).toInt() / 10.0).toString()

private fun formatSignedPercent(value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return "$sign${formatPercentValue(kotlin.math.abs(value))}%"
}

private fun shortAgeLabel(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 60 -> "Now"
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ago"
        elapsedSeconds < 86_400 -> "${elapsedSeconds / 3600}h ago"
        else -> "${elapsedSeconds / 86_400}d ago"
    }
}

private fun LiveRatesState.alertRates(): List<FxRate> =
    (favorites + compare + converter + allFiat)
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .take(24)

private fun LiveRatesState.portfolioRates(): List<FxRate> =
    (converter + favorites + compare + allFiat)
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code == baseCurrency }.thenBy { it.code })

private fun LiveRatesState.converterAvailableRates(): List<FxRate> =
    (allFiat + favorites + compare + converter + crypto)
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes }.thenBy { it.code })

private fun converterTargetCodes(
    selectedCurrencyCodes: List<String>,
    availableRates: List<FxRate>,
    baseCurrency: String,
    limit: Int,
): List<String> {
    val availableCodes = availableRates.map { it.code }.toSet()
    val selected = selectedCurrencyCodes
        .filter { it != baseCurrency && it in availableCodes }
        .distinct()
    val defaults = PopularCurrencyCodes
        .filter { it != baseCurrency && it in availableCodes && it !in selected }
    val targetLimit = limit.cap(availableRates.size).coerceAtLeast(1)
    return (selected + defaults)
        .take(targetLimit)
        .ifEmpty {
            availableRates
                .map { it.code }
                .filter { it != baseCurrency }
                .take(targetLimit)
        }
}

private data class PortfolioHolding(
    val rate: FxRate,
    val amount: Double,
) {
    val baseValue: Double = amountInBase(rate, amount)
    val dailyChangeInBase: Double = if (rate.rate == 0.0) 0.0 else baseValue * rate.change24h / 100.0
}

private fun amountInBase(rate: FxRate, amount: Double): Double =
    if (rate.rate == 0.0) 0.0 else amount / rate.rate

private fun parseAmountInput(value: String): Double {
    val normalized = if (value.count { it == ',' } == 1 && '.' !in value) {
        value.replace(',', '.')
    } else {
        value.replace(",", "")
    }
    return normalized.toDoubleOrNull() ?: 0.0
}

private fun PortfolioHolding.weightLabel(portfolioValue: Double): String =
    if (portfolioValue <= 0.0 || baseValue <= 0.0) {
        "0%"
    } else {
        "${((baseValue / portfolioValue) * 100.0).toInt()}%"
    }

private fun PortfolioHolding.dailyChangeLabel(baseCurrency: String): String {
    val sign = if (dailyChangeInBase >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(dailyChangeInBase))} today"
}

private fun formatPortfolioChange(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}

private fun formatMoneyValue(value: Double): String =
    when {
        value == 0.0 -> "0.00"
        kotlin.math.abs(value) < 0.01 -> "<0.01"
        else -> formatRate(value)
    }

private fun buildUserBackupSnapshot(
    themeMode: ThemeMode,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
): UserBackupSnapshot =
    UserBackupSnapshot(
        updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
        settings = BackupSettings(
            themeMode = themeMode.name,
            baseCurrency = baseCurrency,
            travelerCurrency = travelerCurrency,
            travelerBudgetBase = travelerBudgetBase,
            converterCurrencyCodes = converterCurrencyCodes,
        ),
        alerts = alertsState.alerts,
        watchlist = watchlistState.watchlist,
    )

private fun applyUserBackupSnapshot(
    snapshot: UserBackupSnapshot,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    liveStore: LiveRatesStore,
    onConverterCurrencyCodes: (List<String>) -> Unit,
    onTravelerCurrency: (String) -> Unit,
    onTravelerBudgetBase: (Double) -> Unit,
): ThemeMode {
    val theme = ThemeMode.entries.firstOrNull { it.name == snapshot.settings.themeMode } ?: ThemeMode.System
    AppSettingsPrefs.setThemeMode(theme)
    AppSettingsPrefs.setBaseCurrency(snapshot.settings.baseCurrency)
    AppSettingsPrefs.setTravelerCurrency(snapshot.settings.travelerCurrency)
    AppSettingsPrefs.setTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    AppSettingsPrefs.setConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    liveStore.setBaseCurrency(snapshot.settings.baseCurrency)
    onConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    onTravelerCurrency(snapshot.settings.travelerCurrency)
    onTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    alertsStore.replaceAll(snapshot.alerts)
    watchlistStore.replaceFromBackup(snapshot.watchlist)
    return theme
}

private fun canCreateAlert(subscriptionState: SubscriptionState, currentCount: Int): Boolean {
    val access = subscriptionState.featureAccess()
    return access.hasUnlimitedAlerts || currentCount < access.alertLimit
}

private const val ALERT_TARGET_TOLERANCE = 0.0000001

private data class DetailStats(
    val open: Double,
    val high: Double,
    val low: Double,
    val average: Double,
    val volatilityPct: Double,
)

private val Period.label: String
    get() = when (this) {
        Period.OneDay -> "1D"
        Period.OneWeek -> "1W"
        Period.OneMonth -> "1M"
        Period.OneYear -> "1Y"
        Period.All -> "ALL"
    }

private fun List<Float>.seriesForPeriod(period: Period): List<Float> {
    val source = if (isEmpty()) DetailSeries else this
    val points = when (period) {
        Period.OneDay -> 6
        Period.OneWeek -> 8
        Period.OneMonth -> 18
        Period.OneYear -> source.size
        Period.All -> source.size
    }
    return source.takeLast(points.coerceAtMost(source.size)).ifEmpty { DetailSeries }
}

private fun List<Float>.toDetailStats(): DetailStats {
    val source = if (isEmpty()) DetailSeries else this
    val values = source.map { it.toDouble() }
    val average = values.average().takeIf { !it.isNaN() } ?: 0.0
    val high = values.maxOrNull() ?: 0.0
    val low = values.minOrNull() ?: 0.0
    val volatility = if (average == 0.0) 0.0 else ((high - low) / average) * 100.0
    return DetailStats(
        open = values.firstOrNull() ?: 0.0,
        high = high,
        low = low,
        average = average,
        volatilityPct = volatility,
    )
}

@Composable
private fun BackNavButton(label: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("←", style = FxTheme.typography.numberL.copy(fontSize = 34.sp), color = FxTheme.colors.text)
        if (label != null) {
            Text(label, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
private fun StoryCard(story: NewsStory, onClick: () -> Unit = {}) {
    BentoCard(
        padding = 12.dp,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(story.tag, variant = PillVariant.Ghost)
                    Eyebrow(story.impact, color = if (story.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent)
                }
                Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            Text(story.title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(story.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            if (story.source.isNotBlank()) {
                Text(
                    if (story.sourceUrl.isNotBlank()) "${story.source} · tap for details" else story.source,
                    style = FxTheme.typography.captionMono,
                    color = if (story.sourceUrl.isNotBlank()) FxTheme.colors.accent else FxTheme.colors.textFaint,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("MOVES")
                story.moves.forEach { (code, change) ->
                    Pill("$code ${formatChange(change)}", variant = if (change >= 0) PillVariant.Up else PillVariant.Down)
                }
            }
        }
    }
}

@Composable
private fun NewsSearchField(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FxTheme.shapes.field)
                    .background(FxTheme.colors.surface2)
                    .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                if (query.isBlank()) {
                    Text("Search headlines, tags or currencies", style = FxTheme.typography.caption, color = FxTheme.colors.textGhost)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun NewsFilterRow(
    label: String,
    options: List<String>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Eyebrow(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .clip(FxTheme.shapes.pill)
                        .background(if (selected == option) FxTheme.colors.accentSoft else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected == option) FxTheme.colors.accentLine else FxTheme.colors.border,
                            FxTheme.shapes.pill,
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (enabled || selected == option) option else "$option Pro",
                        style = FxTheme.typography.captionMono,
                        color = if (selected == option) FxTheme.colors.accent else FxTheme.colors.textDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProUpsellCard(title: String, subtitle: String, onClick: () -> Unit) {
    BentoCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card)
            .clickable(onClick = onClick),
        padding = 12.dp,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FlagDot("∞", CurrencyKind.Crypto, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
                Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
fun PaywallScreen(
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onClose: () -> Unit = {},
    onStart: (SubscriptionPlanKind) -> Unit = {},
    onRestore: () -> Unit = {},
) {
    var selectedKind by remember { mutableStateOf(SubscriptionPlanKind.Monthly) }
    val selectedPlan = subscriptionState.plans.firstOrNull { it.kind == selectedKind && it.isAvailable }
        ?: subscriptionState.plans.firstOrNull { it.isAvailable }
        ?: subscriptionState.plans.first()
    LaunchedEffect(selectedPlan.kind) {
        selectedKind = selectedPlan.kind
    }

    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onClose))
        }
        Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
        Text("The full picture.\nEvery rate. Every market.", style = FxTheme.typography.display, color = FxTheme.colors.text)
        Text(
            "Unlimited alerts, deep history, fee comparison, Watch + widget. All on one membership.",
            style = FxTheme.typography.body,
            color = FxTheme.colors.textDim,
        )
        if (subscriptionState.isPremium) {
            ProActiveCard(subscriptionState = subscriptionState)
        }
        BentoCard(padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BenefitRow("⌖", "Live to the second", "Aggregated mid-market from 14 exchanges.")
                BenefitRow("⬡", "Unlimited alerts", "Price, range, daily and weekly targets.")
                BenefitRow("◐", "Traveler mode", "Auto-location, cheat sheets and offline rates.")
                BenefitRow("⌘", "Real fee comparator", "Wise, Revolut and banks in one place.")
                BenefitRow("⌬", "Watch + widget", "Your favorite pair always one glance away.")
                BenefitRow("∞", "Unlimited history", "Down to the minute, back to 2008.")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subscriptionState.plans.forEach { plan ->
                PlanOption(
                    plan = plan,
                    selected = plan.kind == selectedPlan.kind,
                    onSelect = {
                        if (plan.isAvailable) {
                            selectedKind = plan.kind
                        }
                    },
                )
            }
        }
        BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    selectedPlan.badge?.let { Pill(it, variant = PillVariant.Accent) }
                }
                Text(selectedPlan.title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                BigValueText(selectedPlan.priceLabel, selectedPlan.cadenceLabel)
                Text(
                    "Billed through Google Play on Android and App Store on iOS.",
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                )
            }
        }
        subscriptionState.statusMessage?.let {
            Text(it, style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        PrimaryButton(
            when {
                subscriptionState.isPremium -> "Continue"
                !subscriptionState.canPurchase -> "Purchases unavailable"
                else -> "Start FX/ Pro"
            },
            onClick = {
                if (subscriptionState.isPremium) {
                    onClose()
                } else if (subscriptionState.canPurchase) {
                    onStart(selectedPlan.kind)
                }
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "Restore purchase  ·  Terms  ·  Privacy",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.clickable(onClick = onRestore),
            )
        }
    }
}

@Composable
private fun ProActiveCard(subscriptionState: SubscriptionState) {
    BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card), padding = 12.dp) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot("✓", CurrencyKind.Fiat, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow("ACTIVE", color = FxTheme.colors.accent)
                Text("FX/ Pro is active", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subscriptionState.proStatusLabel(), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }
    }
}

@Composable
private fun PlanOption(
    plan: SubscriptionPlan,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val borderColor = if (selected) FxTheme.colors.accentLine else FxTheme.colors.border
    val contentAlpha = if (plan.isAvailable) 1f else 0.46f
    BentoCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, FxTheme.shapes.card)
            .alpha(contentAlpha)
            .clickable(onClick = onSelect),
        padding = 12.dp,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot(if (selected) "✓" else "○", CurrencyKind.Fiat, 30.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(plan.title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    plan.badge?.let { Pill(it, variant = PillVariant.Accent) }
                }
                Text(plan.cadenceLabel, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(plan.priceLabel, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    if (plan.isAvailable) "Available" else "Not configured",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }
    }
}

private fun SubscriptionState.proStatusLabel(): String =
    if (isPremium) {
        activePlanLabel?.let { "Active plan: $it · Entitlement $entitlementId" }
            ?: "Entitlement $entitlementId is active"
    } else {
        "Alerts, extended history and unlimited watchlists"
    }

@Composable
fun OfflineScreen(
    liveState: LiveRatesState = LiveRatesState(),
    onRefresh: () -> Unit = {},
) {
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot(color = FxTheme.colors.down)
            Eyebrow("OFFLINE", color = FxTheme.colors.down)
            Text("cached · 14:32 UTC", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        ScreenHeader("No connection", subtitle = "Showing rates from your last sync · 4 min ago")
        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Eyebrow("LAST KNOWN · USD → EUR", color = FxTheme.colors.down)
                Text("0.9182", style = FxTheme.typography.numberXL, color = FxTheme.colors.textDim)
                Text("14:28:11 UTC  ·  4 min stale", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
        }
        PrimaryButton("↻  Retry connection", onClick = onRefresh)
        SectionLabel("CACHED FAVORITES")
        BentoCard(padding = 0.dp) { Column { liveState.favorites.take(4).forEach { CurrencyRow(it, dense = true, enabled = false) } } }
        Text("╌╌╌  saved locally  ╌╌╌", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

private data class OnboardingStep(
    val tag: String,
    val title: String,
    val body: String,
    val glyph: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit = {}) {
    val steps = remember {
        listOf(
            OnboardingStep(
                tag = "STEP 01 · LIVE RATES",
                title = "Every rate.\nEvery second.",
                body = "Tap any currency to see the live mid-market rate, refreshed from 14 exchanges every second.",
                glyph = "⌖",
            ),
            OnboardingStep(
                tag = "STEP 02 · FEES THAT MATTER",
                title = "See what your\nbank really charges.",
                body = "Compare Wise, Revolut, Western Union and 30+ banks side-by-side — fees, FX margin, total cost.",
                glyph = "⬢",
            ),
            OnboardingStep(
                tag = "STEP 03 · TRAVEL READY",
                title = "Your wallet\nfollows the map.",
                body = "Auto-detect local currency on landing. Offline-safe last rates. Per-country tipping built in.",
                glyph = "◐",
            ),
        )
    }
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FxTheme.colors.bg),
    ) {
        GridBg(Modifier.matchParentSize().alpha(0.10f), radialMask = false)
        GridBg(Modifier.matchParentSize().alpha(0.30f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("FX/", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    "Skip",
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    modifier = Modifier
                        .clip(FxTheme.shapes.field)
                        .clickable(onClick = onComplete)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                key = { it },
            ) { page ->
                OnboardingPage(step = steps[page])
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    steps.indices.forEach { dot ->
                        val width by animateDpAsState(
                            targetValue = if (dot == pagerState.currentPage) 22.dp else 6.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "onboarding-dot",
                        )
                        Box(
                            Modifier
                                .size(width = width, height = 6.dp)
                                .background(
                                    color = if (dot == pagerState.currentPage) FxTheme.colors.accent else FxTheme.colors.textGhost,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                PrimaryButton(
                    text = if (pagerState.currentPage == steps.lastIndex) "Get started" else "Next  →",
                    modifier = Modifier.width(if (pagerState.currentPage == steps.lastIndex) 154.dp else 126.dp),
                ) {
                    if (pagerState.currentPage == steps.lastIndex) {
                        onComplete()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(step: OnboardingStep) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.weight(0.18f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OnboardingGlyph(step.glyph)
        }
        Spacer(Modifier.weight(0.18f))
        Eyebrow(step.tag, color = FxTheme.colors.accent)
        Spacer(Modifier.height(12.dp))
        Text(step.title, style = FxTheme.typography.titleXL, color = FxTheme.colors.text)
        Spacer(Modifier.height(18.dp))
        Text(step.body, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
        Spacer(Modifier.weight(0.22f))
    }
}

@Composable
private fun OnboardingGlyph(glyph: String) {
    val transition = rememberInfiniteTransition(label = "onboarding-glyph")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "onboarding-glyph-rotation",
    )
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        GridBg(Modifier.fillMaxSize().alpha(0.36f))
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(1.dp, FxTheme.colors.accentLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(156.dp)
                    .border(1.dp, FxTheme.colors.accentLine, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    glyph,
                    style = FxTheme.typography.display.copy(fontSize = 86.sp),
                    color = FxTheme.colors.accent,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit = {}) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.accent)
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.bg)
    }
}

@Composable
private fun GhostButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit = {}) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
    }
}

@Composable
private fun BenefitRow(glyph: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.62f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(FxTheme.colors.accentSoft)
                .border(1.dp, FxTheme.colors.accentLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(glyph, style = FxTheme.typography.numberBody, color = FxTheme.colors.accent, textAlign = TextAlign.Center)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(body, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, FxTheme.shapes.chip))
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
    }
}

@Composable
private fun SentimentBar(
    bullish: Int = 46,
    neutral: Int = 20,
    bearish: Int = 34,
) {
    Row(Modifier.fillMaxWidth().height(10.dp).clip(FxTheme.shapes.pill)) {
        Box(Modifier.weight(bullish.coerceAtLeast(1).toFloat()).background(FxTheme.colors.up))
        Box(Modifier.weight(neutral.coerceAtLeast(1).toFloat()).background(FxTheme.colors.textGhost))
        Box(Modifier.weight(bearish.coerceAtLeast(1).toFloat()).background(FxTheme.colors.down))
    }
}

@Composable
private fun OverlayChart() {
    val colors = listOf(FxTheme.colors.accent, FxTheme.colors.up, FxTheme.colors.down, FxTheme.colors.crypto)
    val border = FxTheme.colors.border
    val series = listOf(
        FavoriteRates[0].sparkline,
        FavoriteRates[1].sparkline,
        FavoriteRates[2].sparkline,
        CryptoRates[0].sparkline,
    )
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
        repeat(5) { i ->
            val y = size.height * (i / 4f)
            drawLine(border, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        series.forEachIndexed { seriesIndex, values ->
            val min = values.minOrNull() ?: return@forEachIndexed
            val max = values.maxOrNull() ?: return@forEachIndexed
            val range = (max - min).coerceAtLeast(1e-9f)
            val path = Path()
            values.forEachIndexed { index, value ->
                val point = Offset(
                    x = (index.toFloat() / values.lastIndex) * size.width,
                    y = (1f - (value - min) / range) * size.height,
                )
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            drawPath(path, colors[seriesIndex], style = Stroke(width = 1.5f))
        }
    }
}
