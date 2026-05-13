package com.fxalways.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.AlertTestNotifier
import com.fxalways.app.BackupSettings
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
import com.fxalways.app.data.mock.EventItem
import com.fxalways.app.data.mock.Events
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.data.mock.FeeQuote
import com.fxalways.app.data.mock.FeeQuotes
import com.fxalways.app.data.mock.NewsStories
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.SettingsBaseCurrencies
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.subscription.PlaceholderSubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
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
import kotlin.math.roundToInt

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
    var showPaywall by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(AppSettingsPrefs.themeMode()) }
    var baseCurrency by remember { mutableStateOf(AppSettingsPrefs.baseCurrency()) }
    val liveStore = remember { LiveRatesStore(initialBaseCurrency = baseCurrency) }
    val newsStore = remember { NewsStore() }
    val alertsStore = remember { AlertsStore() }
    val watchlistStore = remember { WatchlistStore() }
    val subscriptionGateway = remember { PlaceholderSubscriptionGateway() }
    var subscriptionState by remember { mutableStateOf(SubscriptionState(isPremium = false)) }
    var backupState by remember { mutableStateOf(UserBackupState()) }
    var backupReady by remember { mutableStateOf(false) }
    var backupSyncing by remember { mutableStateOf(false) }
    var lastSyncedAtMillis by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val liveState by liveStore.state.collectAsState()
    val newsState by newsStore.state.collectAsState()
    val alertsState by alertsStore.state.collectAsState()
    val watchlistState by watchlistStore.state.collectAsState()
    LaunchedEffect(Unit) {
        subscriptionState = subscriptionGateway.currentState()
        backupState = UserBackupGateway.ensureUser()
        if (backupState.isAvailable) {
            runCatching {
                val localSnapshot = buildUserBackupSnapshot(themeMode, baseCurrency, alertsState, watchlistState)
                val remoteSnapshot = UserBackupGateway.pullSnapshot()
                if (remoteSnapshot != null && localSnapshot.isDefaultLocalBackup()) {
                    themeMode = applyUserBackupSnapshot(
                        snapshot = remoteSnapshot,
                        alertsStore = alertsStore,
                        watchlistStore = watchlistStore,
                        liveStore = liveStore,
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
    }
    LaunchedEffect(themeMode, baseCurrency, alertsState, watchlistState, backupReady) {
        if (backupReady) {
            runCatching {
                val snapshot = buildUserBackupSnapshot(themeMode, baseCurrency, alertsState, watchlistState)
                UserBackupGateway.pushSnapshot(snapshot)
                lastSyncedAtMillis = snapshot.updatedAtMillis
            }.onFailure { error ->
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
                backupReady = false
            }
        }
    }
    PlatformBackHandler(enabled = showPaywall || detailRate != null || selectedTab == FxTab.More && moreRoute != MoreRoute.Menu) {
        when {
            showPaywall -> showPaywall = false
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
                if (showPaywall) {
                    PaywallScreen(
                        subscriptionState = subscriptionState,
                        onClose = { showPaywall = false },
                        onStart = {
                            scope.launch {
                                subscriptionState = subscriptionGateway.purchaseMonthly()
                                showPaywall = false
                            }
                        },
                        onRestore = {
                            scope.launch {
                                subscriptionState = subscriptionGateway.restore()
                                showPaywall = false
                            }
                        },
                    )
                } else if (detailRate != null) {
                    DetailScreen(
                        liveState = liveState,
                        alertsState = alertsState,
                        subscriptionState = subscriptionState,
                        rate = detailRate,
                        onBack = { detailRate = null },
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
                                onOpenNews = { selectedTab = FxTab.News },
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
                                onCreateManualAlert = { rate, direction, target ->
                                    if (canCreateAlert(subscriptionState, alertsState.alerts.size)) {
                                        alertsStore.addAlert(liveState.baseCurrency, rate.code, target, direction)
                                    } else {
                                        showPaywall = true
                                    }
                                },
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
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { showPaywall = true },
                            )
                            MoreRoute.Settings -> SettingsScreen(
                                themeMode = themeMode,
                                baseCurrency = baseCurrency,
                                backupState = backupState,
                                backupSyncing = backupSyncing,
                                lastSyncedAtMillis = lastSyncedAtMillis,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { showPaywall = true },
                                onRestorePurchase = {
                                    scope.launch {
                                        subscriptionState = subscriptionGateway.restore()
                                    }
                                },
                                onSyncNow = {
                                    scope.launch {
                                        backupSyncing = true
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(themeMode, baseCurrency, alertsState, watchlistState)
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
                                            val result = UserBackupGateway.linkWithGoogle(
                                                buildUserBackupSnapshot(themeMode, baseCurrency, alertsState, watchlistState),
                                            )
                                            backupState = result.state
                                            val appliedTheme = applyUserBackupSnapshot(
                                                snapshot = result.snapshot,
                                                alertsStore = alertsStore,
                                                watchlistStore = watchlistStore,
                                                liveStore = liveStore,
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
                                            val snapshot = buildUserBackupSnapshot(themeMode, baseCurrency, alertsState, watchlistState)
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
            FxBottomBar(
                tabs = FxTab.entries.map { it.label },
                selectedIndex = selectedTab.ordinal,
                onSelect = {
                    selectedTab = FxTab.entries[it]
                    if (selectedTab != FxTab.More) moreRoute = MoreRoute.Menu
                },
            )
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
            subtitle = "base · ${liveState.baseCurrency}  ·  ${visibleFavorites.size}/${liveState.favorites.size} favorites · ${access.historyLabel}",
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
    onOpenPaywall: () -> Unit,
) {
    var focusedCode by remember(liveState.baseCurrency) { mutableStateOf(liveState.baseCurrency) }
    val access = subscriptionState.featureAccess()
    val visibleQuotes = FeeQuotes.take(access.feeQuoteLimit.cap(FeeQuotes.size))
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot()
            Eyebrow("MID", color = FxTheme.colors.accent)
            Text("14:32 · mid-market", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        ScreenHeader("Convert", subtitle = "Multi-currency · live to 4 decimals")
        BentoCard(padding = 8.dp) {
            Column {
                liveState.converter.forEach { rate ->
                    ConverterRow(rate, focusedCode == rate.code) { focusedCode = rate.code }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton("⇄  Reverse", Modifier.weight(1f))
            GhostButton("≡  Edit list", Modifier.weight(1f))
        }
        SectionLabel("FEES · ${liveState.baseCurrency} → ${liveState.favorites.firstOrNull()?.code ?: "EUR"}", right = if (access.canUseFullFeeComparison) "Full" else "Preview")
        BentoCard(padding = 0.dp) {
            Column { visibleQuotes.forEach { FeeComparisonRow(it) } }
        }
        if (!access.canUseFullFeeComparison) {
            ProUpsellCard(
                title = "See the real transfer cost",
                subtitle = "Pro unlocks complete bank and provider fee comparisons.",
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
private fun ConverterRow(rate: FxRate, focused: Boolean, onClick: () -> Unit) {
    val bg = if (focused) FxTheme.colors.accentSoft else Color.Transparent
    val border = if (focused) FxTheme.colors.accentLine else Color.Transparent
    Row(
        Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(bg)
            .border(if (focused) 1.dp else 0.dp, border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlagDot(rate.glyph, rate.kind, 32.dp)
        Column(Modifier.weight(1f)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(rate.name, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        val amount = if (focused) {
            "1,000.00"
        } else if (rate.code == "BTC") {
            "0.01540"
        } else {
            formatRate(rate.rate * 1000)
        }
        Text(
            amount,
            style = if (focused) FxTheme.typography.numberL.copy(fontSize = 24.sp) else FxTheme.typography.numberL,
            color = if (focused) FxTheme.colors.accent else FxTheme.colors.text,
        )
    }
}

@Composable
private fun FeeComparisonRow(quote: FeeQuote) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(quote.provider, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text, modifier = Modifier.weight(1f))
        if (quote.badge != null) Pill(quote.badge, variant = if (quote.isHighFee) PillVariant.Down else PillVariant.Up)
        Text(quote.amount, style = FxTheme.typography.numberBody, color = FxTheme.colors.text, modifier = Modifier.widthIn(min = 70.dp), textAlign = TextAlign.End)
        Text(quote.fee, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, modifier = Modifier.widthIn(min = 48.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun DetailScreen(
    liveState: LiveRatesState = LiveRatesState(),
    alertsState: AlertsState = AlertsState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    rate: FxRate? = null,
    onBack: () -> Unit = {},
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
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BackNavButton(label = null, onClick = onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill("★ Watching")
                Pill("🔒")
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
        Text("mid-market · 14:32:08 UTC · refresh 1s", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val chartData = if (selected.code == "EUR") liveState.detailSeries else selected.sparkline
                PriceChart(chartData, Modifier.fillMaxWidth().height(188.dp), focusIndex = 8)
                SegmentedPeriods(period, { period = it }, Modifier.fillMaxWidth())
            }
        }
        SectionLabel("STATISTICS · 1M")
        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KeyValueRow("Open", "0.9038", "Apr 13")
                KeyValueRow("High", "0.9241", "Apr 28")
                KeyValueRow("Low", "0.9028", "Apr 02")
                KeyValueRow("Volatility", "0.42 %")
                KeyValueRow("Avg 30-day", "0.9156")
            }
        }
        SectionLabel("EVENTS · ANNOTATED", right = "Filter")
        BentoCard(padding = 0.dp) { Column { Events.forEach { EventRow(it) } } }
        GhostButton(
            text = if (activeForPair > 0) "🔔  Add another ${selected.code} alert                                      $alertLabel" else "🔔  Alert me above ${formatRate(selected.rate * 1.01)}                                      $alertLabel",
            onClick = { onCreateAlert(selected) },
        )
    }
}

@Composable
private fun EventRow(item: EventItem) {
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(item.date, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, modifier = Modifier.width(42.dp))
        Pill(item.tag, variant = PillVariant.Accent)
        Text(item.headline, style = FxTheme.typography.caption, color = FxTheme.colors.text, modifier = Modifier.weight(1f))
        Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
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
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val jpy = liveState.favorites.firstOrNull { it.code == "JPY" }?.rate ?: 156.42
    val baseDefinition = SettingsBaseCurrencies.firstOrNull { it.code == liveState.baseCurrency }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader("Traveler", sub = "TOKYO · JPY", subtitle = "Local currency detected")
        BentoCard(Modifier.fillMaxWidth().height(156.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.18f))
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlagDot(baseDefinition?.glyph ?: "◆", size = 28.dp)
                    Text("1 ${liveState.baseCurrency}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text("→", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textFaint)
                    Text("JPY", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    FlagDot("🇯🇵", size = 28.dp)
                }
                BigValueText("¥${formatRate(jpy)}")
                Text("+0.68% today · mid-market", style = FxTheme.typography.captionMono, color = FxTheme.colors.up)
            }
        }
        SectionLabel("CHEAT SHEET")
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(1, 5, 10, 20, 50, 100).take(access.travelerCheatSheetLimit.cap(6)).forEach { amount ->
                    KeyValueRow("$amount ${liveState.baseCurrency}", "¥${(amount * jpy).roundToInt()}")
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
            MetricTile("TIPPING · RESTAURANT", "0%", "not customary · can offend", Modifier.weight(1f))
            MetricTile("TAX · INCLUDED", "10%", "consumption tax", Modifier.weight(1f))
        }
        BentoTile(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Eyebrow("CARDS ACCEPTED")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("Visa")
                        Pill("Mastercard")
                        Pill("Suica")
                    }
                }
                Text("cash preferred", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
        }
    }
}

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
                    glyph = "◐",
                    title = "Traveler",
                    subtitle = "Local cheat sheets and offline rates",
                    onClick = onOpenTraveler,
                )
                MoreRow(
                    glyph = "≋",
                    title = "News",
                    subtitle = "Market stream and sentiment",
                    onClick = onOpenNews,
                )
                MoreRow(
                    glyph = "🔔",
                    title = "Alerts",
                    subtitle = "$alertsCount active · price targets and breakouts",
                    onClick = onOpenAlerts,
                )
                MoreRow(
                    glyph = "⌁",
                    title = "Watchlist",
                    subtitle = "$watchlistCount currencies · custom tracking",
                    onClick = onOpenWatchlist,
                )
                MoreRow(
                    glyph = "⚙",
                    title = "Settings",
                    subtitle = "Theme mode, base currency and version",
                    onClick = onOpenSettings,
                )
                MoreRow(
                    glyph = "∞",
                    title = if (subscriptionState.isPremium) "FX/ Pro active" else "Upgrade to Pro",
                    subtitle = if (subscriptionState.isPremium) "Entitlement ${subscriptionState.entitlementId} is active" else "Alerts, extended history and unlimited watchlists",
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
    onCreateManualAlert: (FxRate, AlertDirection, Double) -> Unit = { _, _, _ -> },
    onToggleAlert: (String) -> Unit = {},
    onDeleteAlert: (String) -> Unit = {},
    onTestAlert: (PriceAlert) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val canCreate = canCreateAlert(subscriptionState, alertsState.alerts.size)
    val limitLabel = if (access.hasUnlimitedAlerts) "Unlimited" else "${alertsState.alerts.size}/${access.alertLimit}"
    val alertRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter) { liveState.alertRates() }
    var selectedRateCode by remember(liveState.baseCurrency) { mutableStateOf(alertRates.firstOrNull()?.code ?: "EUR") }
    val selectedRate = alertRates.firstOrNull { it.code == selectedRateCode } ?: alertRates.firstOrNull() ?: FavoriteRates.first()
    var selectedDirection by remember { mutableStateOf(AlertDirection.Above) }
    var targetText by remember(selectedRate.code, selectedDirection) {
        val multiplier = if (selectedDirection == AlertDirection.Above) 1.01 else 0.99
        mutableStateOf(formatRate(selectedRate.rate * multiplier))
    }
    val targetValue = targetText.replace(",", "").toDoubleOrNull()
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader("Alerts", sub = "PRICE TARGETS", subtitle = "$limitLabel alerts · ${liveState.baseCurrency} base")

        BentoCard(Modifier.fillMaxWidth().height(132.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
                    Pill("${alertsState.activeCount} active", variant = if (alertsState.activeCount > 0) PillVariant.Up else PillVariant.Ghost)
                }
                Text("Watch breakouts without watching charts.", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text("Quick alerts trigger at 1% above the current mid-market rate.", style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
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
                                        val multiplier = if (selectedDirection == AlertDirection.Above) 1.01 else 0.99
                                        targetText = formatRate(rate.rate * multiplier)
                                    },
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertDirection.entries.forEach { direction ->
                        Pill(
                            text = direction.label,
                            variant = if (direction == selectedDirection) PillVariant.Accent else PillVariant.Ghost,
                            modifier = Modifier.clickable {
                                selectedDirection = direction
                                val multiplier = if (direction == AlertDirection.Above) 1.01 else 0.99
                                targetText = formatRate(selectedRate.rate * multiplier)
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
                                targetText = formatRate(selectedRate.rate * (1.0 + preset.percent / 100.0))
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
                )
                PrimaryButton(
                    text = if (canCreate) "Create ${selectedDirection.label.lowercase()} alert" else "Unlock custom alerts",
                    onClick = {
                        if (!canCreate) {
                            onOpenPaywall()
                        } else if (targetValue != null && targetValue > 0.0) {
                            onCreateManualAlert(selectedRate, selectedDirection, targetValue)
                        }
                    },
                )
                Text(
                    "Current ${formatRate(selectedRate.rate)} · target ${targetValue?.let(::formatRate) ?: "--"}",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }

        SectionLabel("QUICK CREATE")
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                liveState.favorites.take(4).forEach { rate ->
                    AlertQuickRow(
                        baseCurrency = liveState.baseCurrency,
                        rate = rate,
                        enabled = canCreate,
                        onCreate = { onCreateAlert(rate) },
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
                    AlertCard(alert = alert, onToggle = onToggleAlert, onDelete = onDeleteAlert, onTest = onTestAlert)
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
    val selectedRates = watchlistState.watchlist.codes.mapNotNull { code -> allRates.firstOrNull { it.code == code } }
    val limitLabel = if (access.hasUnlimitedWatchlistCurrencies) "Unlimited" else "${watchlistState.watchlist.codes.size}/${access.watchlistCurrencyLimit}"
    val portfolioValue = selectedRates.sumOf { rate ->
        val amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0
        amountInBase(rate, amount)
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = "More", onClick = onBack)
        }
        ScreenHeader("Watchlist", sub = "CUSTOM TRACKING", subtitle = "$limitLabel currencies · ${liveState.baseCurrency} base")

        BentoCard(Modifier.fillMaxWidth().height(138.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
                    Pill("${selectedRates.size} tracked", variant = if (selectedRates.isNotEmpty()) PillVariant.Accent else PillVariant.Ghost)
                }
                Text(watchlistState.watchlist.name, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                BigValueText("${liveState.baseCurrency} ${formatRate(portfolioValue)}")
                Text("Estimated value from holdings in tracked currencies.", style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }

        SectionLabel("PORTFOLIO")
        if (selectedRates.isEmpty()) {
            BentoCard(padding = 14.dp) {
                Text("Choose currencies below to start tracking.", style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        } else {
            BentoCard(padding = 8.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedRates.forEach { rate ->
                        PortfolioHoldingRow(
                            baseCurrency = liveState.baseCurrency,
                            rate = rate,
                            amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0,
                            onAmountChange = { amount -> onSetHolding(rate.code, amount) },
                            onOpenDetail = { onOpenDetail(rate) },
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
    rate: FxRate,
    amount: Double,
    onAmountChange: (Double) -> Unit,
    onOpenDetail: () -> Unit,
) {
    var amountText by remember(rate.code, amount) { mutableStateOf(if (amount > 0.0) formatRate(amount) else "") }
    val baseValue = amountInBase(rate, amount)
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
            Text("$baseCurrency ${formatRate(baseValue)} · ${formatChange(rate.change24h)} today", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        BasicTextField(
            value = amountText,
            onValueChange = { raw ->
                val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                amountText = next
                onAmountChange(next.replace(",", "").toDoubleOrNull() ?: 0.0)
            },
            singleLine = true,
            textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(92.dp),
            decorationBox = { innerTextField ->
                if (amountText.isBlank()) {
                    Text("0.00", style = FxTheme.typography.numberBody, color = FxTheme.colors.textGhost, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                }
                innerTextField()
            },
        )
    }
}

@Composable
private fun WatchlistCurrencyRow(
    rate: FxRate,
    selected: Boolean,
    locked: Boolean,
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
            Text(rate.name, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
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
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(pair, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
        Pill(if (enabled) "create" else "pro", variant = if (enabled) PillVariant.Ghost else PillVariant.Accent)
    }
}

@Composable
private fun AlertCard(
    alert: PriceAlert,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTest: (PriceAlert) -> Unit,
) {
    BentoCard(padding = 12.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FlagDot("🔔", CurrencyKind.Fiat, 32.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text("${alert.direction.label} ${formatRate(alert.target)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            Pill(if (alert.enabled) "on" else "paused", variant = if (alert.enabled) PillVariant.Up else PillVariant.Ghost)
            Text(
                if (alert.enabled) "pause" else "resume",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textDim,
                modifier = Modifier.clickable { onToggle(alert.id) },
            )
            Text(
                "test",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                modifier = Modifier.clickable { onTest(alert) },
            )
            Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textFaint, modifier = Modifier.clickable { onDelete(alert.id) })
        }
    }
}

@Composable
private fun MoreRow(
    glyph: String,
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
                .size(36.dp)
                .clip(FxTheme.shapes.icon)
                .background(FxTheme.colors.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(glyph, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.accent)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}

@Composable
fun NewsScreen(
    newsState: NewsUiState = NewsUiState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onRefresh: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val visibleStories = newsState.stories.take(access.newsStoryLimit.cap(newsState.stories.size))
    ScreenScaffold {
        ScreenHeader(
            "News",
            sub = if (access.canUseAdvancedNews) "MARKET STREAM" else "MARKET PREVIEW",
            subtitle = "${newsState.provider} · ${newsState.region} · ${newsState.language}",
            right = { Text("↻", style = FxTheme.typography.numberL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onRefresh)) },
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Eyebrow("SENTIMENT")
                SentimentBar(newsState.bullish, newsState.neutral, newsState.bearish)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendDot("BULLISH ${newsState.bullish}%", FxTheme.colors.up)
                    LegendDot("NEUTRAL ${newsState.neutral}%", FxTheme.colors.textGhost)
                    LegendDot("BEARISH ${newsState.bearish}%", FxTheme.colors.down)
                }
            }
        }
        SectionLabel("RECENT LINES")
        if (newsState.errorMessage != null) {
            Text("News backend unavailable · showing cached market lines", style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        visibleStories.forEach { StoryCard(it) }
        if (!access.canUseAdvancedNews) {
            ProUpsellCard(
                title = "Personalize the market stream",
                subtitle = "Pro unlocks more stories and filters by region, currencies and topics.",
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    baseCurrency: String,
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
    val baseCurrencies = SettingsBaseCurrencies.take(access.baseCurrencyLimit.cap(SettingsBaseCurrencies.size))
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
                    SettingChoiceRow(
                        title = "Sign in with Google",
                        subtitle = "Keep the same backup and restore it on a new Android phone",
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
                    subtitle = if (subscriptionState.isPremium) "Entitlement ${subscriptionState.entitlementId}" else "Upgrade for extended history, alerts and unlimited lists",
                    selected = subscriptionState.isPremium,
                    actionLabel = if (subscriptionState.isPremium) "manage" else "upgrade",
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
            }
        }
        if (baseCurrencies.size < SettingsBaseCurrencies.size) {
            ProUpsellCard(
                title = "Unlock all base currencies",
                subtitle = "Free includes ${baseCurrencies.size}; Pro unlocks every supported base currency.",
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

private val AlertDirection.label: String
    get() = when (this) {
        AlertDirection.Above -> "Above"
        AlertDirection.Below -> "Below"
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

private fun LiveRatesState.alertRates(): List<FxRate> =
    (favorites + compare + converter)
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .take(10)

private fun LiveRatesState.portfolioRates(): List<FxRate> =
    (converter + favorites + compare)
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code == baseCurrency }.thenBy { it.code })
        .take(10)

private fun amountInBase(rate: FxRate, amount: Double): Double =
    if (rate.rate == 0.0) 0.0 else amount / rate.rate

private fun buildUserBackupSnapshot(
    themeMode: ThemeMode,
    baseCurrency: String,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
): UserBackupSnapshot =
    UserBackupSnapshot(
        updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
        settings = BackupSettings(themeMode = themeMode.name, baseCurrency = baseCurrency),
        alerts = alertsState.alerts,
        watchlist = watchlistState.watchlist,
    )

private fun applyUserBackupSnapshot(
    snapshot: UserBackupSnapshot,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    liveStore: LiveRatesStore,
): ThemeMode {
    val theme = ThemeMode.entries.firstOrNull { it.name == snapshot.settings.themeMode } ?: ThemeMode.System
    AppSettingsPrefs.setThemeMode(theme)
    AppSettingsPrefs.setBaseCurrency(snapshot.settings.baseCurrency)
    liveStore.setBaseCurrency(snapshot.settings.baseCurrency)
    alertsStore.replaceAll(snapshot.alerts)
    watchlistStore.replaceFromBackup(snapshot.watchlist)
    return theme
}

private fun canCreateAlert(subscriptionState: SubscriptionState, currentCount: Int): Boolean {
    val access = subscriptionState.featureAccess()
    return access.hasUnlimitedAlerts || currentCount < access.alertLimit
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
private fun StoryCard(story: NewsStory) {
    BentoCard(padding = 12.dp) {
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
    onStart: () -> Unit = {},
    onRestore: () -> Unit = {},
) {
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onClose))
        }
        Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
        Text(subscriptionState.paywallTitle, style = FxTheme.typography.display, color = FxTheme.colors.text)
        Text(subscriptionState.paywallSubtitle, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BenefitRow("⌖", "Live to the second", "Aggregated mid-market from 14 exchanges.")
                BenefitRow("⬡", "Unlimited alerts", "Price, range, daily and weekly alerts.")
                BenefitRow("◐", "Traveler mode", "Auto-location, cheat sheets, offline rates.")
                BenefitRow("⌘", "Real fee comparator", "Wise · Revolut · banks · in one place.")
                BenefitRow("⌬", "Apple Watch + widget", "Your favorite pair always one glance away.")
                BenefitRow("∞", "Unlimited history", "Down to the minute, back to 2008.")
            }
        }
        BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Pill("RECOMMENDED", variant = PillVariant.Accent) }
                Text("Monthly", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                BigValueText(subscriptionState.productLabel.substringBefore(" /"), " / ${subscriptionState.productLabel.substringAfter("/ ", "month")}")
                Text("Cancel anytime. Billed through Google Play on Android and App Store on iOS.", style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }
        PrimaryButton(if (subscriptionState.isPremium) "Pro is active" else "Start FX/ Pro", onClick = onStart)
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(glyph, style = FxTheme.typography.numberL, color = FxTheme.colors.accent, modifier = Modifier.width(28.dp))
        Column {
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
