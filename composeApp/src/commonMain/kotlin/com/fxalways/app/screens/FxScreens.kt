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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.AlertTestNotifier
import com.fxalways.app.BackupSettings
import com.fxalways.app.DeviceLocale
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.NotificationPermissionStatus
import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig
import com.fxalways.app.ThemeMode
import com.fxalways.app.PlatformBackHandler
import com.fxalways.app.UserProfile
import com.fxalways.app.UserBackupGateway
import com.fxalways.app.UserBackupSnapshot
import com.fxalways.app.UserBackupState
import com.fxalways.app.isDefaultLocalBackup
import com.fxalways.app.refreshFxWidgets
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
import com.fxalways.app.data.PortfolioCsvImportResult
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.PortfolioTransaction
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.app.data.SettingsBaseCurrencies
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.data.importPortfolioCsv
import com.fxalways.app.data.matchesDefinition
import com.fxalways.app.data.toPortfolioCsv
import com.fxalways.app.subscription.SubscriptionPlan
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.createSubscriptionGateway
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.ui.SupportedLanguages
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
import com.fxalways.observability.Observability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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
    val scope = rememberCoroutineScope()
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
        Column(
            Modifier
                .fillMaxSize()
                .background(FxTheme.colors.bg)
                .safeContentPadding(),
        ) {
            Box(Modifier.weight(1f)) {
                if (!startupReady) {
                    StartupLoadingScreen(baseCurrency, appLanguage)
                } else if (showPaywall) {
                    PaywallScreen(
                        subscriptionState = subscriptionState,
                        actionInProgress = subscriptionActionInProgress,
                        userProfile = userProfile,
                        onClose = { showPaywall = false },
                        onStart = { planKind ->
                            scope.launch {
                                subscriptionActionInProgress = true
                                try {
                                    Observability.event("purchase_started", mapOf("plan" to planKind.name))
                                    subscriptionState = subscriptionGateway.purchasePlan(planKind)
                                    AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                    subscriptionReady = true
                                    showPaywall = !subscriptionState.isPremium
                                    if (subscriptionState.isPremium) {
                                        Observability.event("purchase_success", mapOf("plan" to planKind.name))
                                    }
                                    Observability.event("purchase_finished", mapOf("premium" to subscriptionState.isPremium.toString()))
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    Observability.recordException(error, mapOf("flow" to "purchase", "plan" to planKind.name))
                                } finally {
                                    subscriptionActionInProgress = false
                                }
                            }
                        },
                        onRestore = {
                            scope.launch {
                                subscriptionActionInProgress = true
                                try {
                                    Observability.event("purchase_restore_started", mapOf("source" to "paywall"))
                                    subscriptionState = subscriptionGateway.restore()
                                    AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                    subscriptionReady = true
                                    showPaywall = !subscriptionState.isPremium
                                    Observability.event("purchase_restore_finished", mapOf("premium" to subscriptionState.isPremium.toString()))
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    Observability.recordException(error, mapOf("flow" to "purchase_restore", "source" to "paywall"))
                                } finally {
                                    subscriptionActionInProgress = false
                                }
                            }
                        },
                        onOpenUrl = ExternalUrlOpener::open,
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
                        onOpenPaywall = { openPaywall("currency_detail") },
                        onLoadHistory = detailStore::load,
                        onOpenUrl = ExternalUrlOpener::open,
                        onOpenStory = { openStory(it, "currency_detail") },
                        onCreateAlert = { rate ->
                            if (
                                canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                                alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate) != null
                            ) {
                                alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                            } else {
                                openPaywall("currency_detail_alert_limit")
                            }
                        },
                    )
                } else {
                    when (selectedTab) {
                        FxTab.Rates -> {
                            if (liveState.errorMessage != null && !liveState.isLive) {
                                OfflineScreen(
                                    liveState,
                                    onRefresh = {
                                        Observability.event("rates_refresh", mapOf("source" to "offline"))
                                        liveStore.refresh()
                                    },
                                )
                            } else {
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
                                    ),
                                    onRefresh = {
                                        Observability.event("rates_refresh", mapOf("source" to "dashboard"))
                                        liveStore.refresh()
                                    },
                                    onOpenPaywall = { openPaywall("dashboard") },
                                    onOpenDetail = { openDetail(it, "dashboard") },
                                    onEditFavorites = {
                                        if (subscriptionState.isPremium) {
                                            selectTab(FxTab.More)
                                            openMoreRoute(MoreRoute.Watchlist)
                                        } else {
                                            openPaywall("dashboard_favorites")
                                        }
                                    },
                                    onSeeAllCrypto = {
                                        val cryptoCodes = liveState.visibleDashboardCryptoRates(subscriptionState.isPremium, compareCurrencyCodes).map { it.code }
                                        if (cryptoCodes.isNotEmpty()) {
                                            Observability.event("dashboard_crypto_see_all", mapOf("count" to cryptoCodes.size.toString()))
                                            compareCurrencyCodes = cryptoCodes
                                            AppSettingsPrefs.setCompareCurrencyCodes(cryptoCodes)
                                            selectTab(FxTab.Compare)
                                        }
                                    },
                                    onCreateSuggestedAlert = {
                                        val suggestion = suggestedProfileAlert(
                                            profile = userProfile,
                                            liveState = liveState,
                                            isPremium = subscriptionState.isPremium,
                                        )
                                        if (suggestion == null) {
                                            openMoreRoute(MoreRoute.Alerts)
                                            selectTab(FxTab.More)
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
                                                    selectTab(FxTab.More)
                                                    openMoreRoute(MoreRoute.Alerts)
                                                }
                                                canCreateAlert(subscriptionState, alertsState.alerts.size) -> {
                                                    Observability.event("profile_alert_created", mapOf("profile" to userProfile.name, "currency" to suggestion.rate.code, "kind" to suggestion.kind.name))
                                                    alertsStore.addAlert(liveState.baseCurrency, suggestion.rate.code, suggestion.target, suggestion.direction, suggestion.kind)
                                                    selectTab(FxTab.More)
                                                    openMoreRoute(MoreRoute.Alerts)
                                                }
                                                else -> openPaywall("dashboard_profile_alert_limit")
                                            }
                                        }
                                    },
                                    onOpenConverter = { selectTab(FxTab.Convert) },
                                    onOpenTraveler = {
                                        selectTab(FxTab.More)
                                        openMoreRoute(MoreRoute.Traveler)
                                    },
                                    onOpenWatchlist = {
                                        selectTab(FxTab.More)
                                        openMoreRoute(MoreRoute.Watchlist)
                                    },
                                )
                            }
                        }
                        FxTab.Convert -> ConverterScreen(
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
                                converterCurrencyCodes = codes
                                AppSettingsPrefs.setConverterCurrencyCodes(codes)
                            },
                            onOpenPaywall = { openPaywall("converter") },
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
                                    else -> openPaywall("converter_transfer_alert_limit")
                                }
                            },
                            onOpenProviderUrl = ExternalUrlOpener::open,
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
                                compareCurrencyCodes = codes
                                AppSettingsPrefs.setCompareCurrencyCodes(codes)
                            },
                            onOpenPaywall = { openPaywall("compare") },
                            onOpenDetail = { openDetail(it, "compare") },
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
                            onOpenStory = { openStory(it, "news") },
                            onOpenPaywall = { openPaywall("news") },
                        )
                        FxTab.More -> when (moreRoute) {
                            MoreRoute.Menu -> MoreScreen(
                                subscriptionState = subscriptionState,
                                alertsCount = alertsState.activeCount,
                                watchlistCount = watchlistState.watchlist.codes.size,
                                onOpenAlerts = { openMoreRoute(MoreRoute.Alerts) },
                                onOpenWatchlist = { openMoreRoute(MoreRoute.Watchlist) },
                                onOpenTraveler = { openMoreRoute(MoreRoute.Traveler) },
                                onOpenSettings = { openMoreRoute(MoreRoute.Settings) },
                                onOpenNews = { selectTab(FxTab.News) },
                                onOpenPaywall = { openPaywall("more") },
                            )
                            MoreRoute.Alerts -> AlertsScreen(
                                liveState = liveState,
                                alertsState = alertsState,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { openPaywall("alerts") },
                                onCreateAlert = { rate ->
                                    if (
                                        canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                                        alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate) != null
                                    ) {
                                        Observability.event("alert_created", mapOf("type" to "quick", "currency" to rate.code))
                                        alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                                    } else {
                                        openPaywall("alert_limit")
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
                                        openPaywall("alert_limit")
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
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { openPaywall("watchlist") },
                                onToggleCurrency = { code ->
                                    val selected = code in watchlistState.watchlist.codes
                                    val canAdd = selected ||
                                        subscriptionState.featureAccess().hasUnlimitedWatchlistCurrencies ||
                                        watchlistState.watchlist.codes.size < subscriptionState.featureAccess().watchlistCurrencyLimit
                                    if (!watchlistStore.toggle(code, canAdd)) {
                                        openPaywall("watchlist_limit")
                                    } else {
                                        Observability.event("watchlist_toggle", mapOf("currency" to code))
                                    }
                                },
                                onSetHolding = watchlistStore::setHolding,
                                onSetHoldingCost = watchlistStore::setHoldingCost,
                                onRecordTransaction = watchlistStore::recordTransaction,
                                onImportPortfolioCsv = watchlistStore::importPortfolioCsv,
                                onOpenDetail = { openDetail(it, "watchlist") },
                            )
                            MoreRoute.Traveler -> TravelerScreen(
                                liveState = liveState,
                                subscriptionState = subscriptionState,
                                selectedCurrency = travelerCurrency,
                                budgetBase = travelerBudgetBase,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onCurrencySelected = { code ->
                                    Observability.event("traveler_currency_changed", mapOf("currency" to code))
                                    travelerCurrency = code
                                    AppSettingsPrefs.setTravelerCurrency(code)
                                },
                                onBudgetChange = { amount ->
                                    Observability.event("traveler_budget_changed")
                                    travelerBudgetBase = amount
                                    AppSettingsPrefs.setTravelerBudgetBase(amount)
                                },
                                onOpenPaywall = { openPaywall("traveler") },
                            )
                            MoreRoute.Settings -> SettingsScreen(
                                themeMode = themeMode,
                                appLanguage = appLanguage,
                                baseCurrency = baseCurrency,
                                userProfile = userProfile,
                                availableBaseCurrencies = liveState.allFiat,
                                backupState = backupState,
                                backupSyncing = backupSyncing,
                                lastSyncedAtMillis = lastSyncedAtMillis,
                                subscriptionState = subscriptionState,
                                providerPreferenceCodes = providerPreferenceCodes,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { openPaywall("settings") },
                                onOpenUrl = ExternalUrlOpener::open,
                                onRestorePurchase = {
                                    scope.launch {
                                        Observability.event("purchase_restore_started", mapOf("source" to "settings"))
                                        subscriptionState = subscriptionGateway.restore()
                                        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                        subscriptionReady = true
                                        Observability.event("purchase_restore_finished", mapOf("premium" to subscriptionState.isPremium.toString()))
                                    }
                                },
                                onSyncNow = {
                                    scope.launch {
                                        backupSyncing = true
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
                                            backupState = UserBackupGateway.ensureUser()
                                            lastSyncedAtMillis = snapshot.updatedAtMillis
                                        }.onFailure { error ->
                                            Observability.recordException(error, mapOf("flow" to "manual_backup_sync"))
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
                                            themeMode = appliedTheme
                                            baseCurrency = result.snapshot.settings.baseCurrency
                                            lastSyncedAtMillis = result.snapshot.updatedAtMillis
                                            backupReady = true
                                        }.onFailure { error ->
                                            Observability.recordException(error, mapOf("flow" to "link_backup_identity"))
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
                                            val result = UserBackupGateway.signOutToAnonymous(snapshot)
                                            backupState = result.state
                                            lastSyncedAtMillis = result.snapshot.updatedAtMillis
                                            backupReady = true
                                        }.onFailure { error ->
                                            Observability.recordException(error, mapOf("flow" to "sign_out_to_anonymous"))
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
                                    Observability.event("theme_changed", mapOf("theme" to mode.name))
                                    themeMode = mode
                                    AppSettingsPrefs.setThemeMode(mode)
                                },
                                onLanguageChange = { code ->
                                    Observability.event("language_changed", mapOf("language" to code))
                                    appLanguage = code
                                    AppSettingsPrefs.setLanguage(code)
                                    newsStore.setLanguage(code)
                                },
                                onBaseCurrencyChange = { code ->
                                    Observability.event("base_currency_changed", mapOf("currency" to code))
                                    baseCurrency = code
                                    AppSettingsPrefs.setBaseCurrency(code)
                                    liveStore.setBaseCurrency(code)
                                },
                                onUserProfileChange = { profile ->
                                    Observability.event("profile_changed", mapOf("profile" to profile.name))
                                    val preset = profile.preset()
                                    userProfile = profile
                                    AppSettingsPrefs.setUserProfile(profile)
                                    converterCurrencyCodes = preset.converterCodes
                                    compareCurrencyCodes = preset.compareCodes
                                    travelerCurrency = preset.travelerCurrency
                                    AppSettingsPrefs.setConverterCurrencyCodes(converterCurrencyCodes)
                                    AppSettingsPrefs.setCompareCurrencyCodes(compareCurrencyCodes)
                                    AppSettingsPrefs.setTravelerCurrency(travelerCurrency)
                                    AppSettingsPrefs.setConverterAmountText(preset.suggestedAmount)
                                    if (watchlistState.watchlist.holdings.isEmpty() && watchlistState.watchlist.transactions.isEmpty()) {
                                        watchlistStore.replaceFromBackup(Watchlist(codes = preset.watchlistCodes))
                                    }
                                },
                                onProviderPreferenceCodesChange = { codes ->
                                    val normalized = normalizeProviderPreferenceCodes(codes, baseCurrency)
                                    Observability.event("provider_preferences_changed", mapOf("count" to normalized.size.toString(), "base_currency" to baseCurrency))
                                    providerPreferenceCodes = normalized
                                    AppSettingsPrefs.setProviderPreferenceCodes(normalized)
                                },
                            )
                        }
                    }
                }
            }
            if (startupReady) {
                FxBottomBar(
                    tabs = FxTab.entries.map { ui(it.label) },
                    selectedIndex = selectedTab.ordinal,
                    onSelect = {
                        selectTab(FxTab.entries[it])
                    },
                    iconKeys = FxTab.entries.map { it.label },
                )
            }
        }
    }
}
}

private data class ProfileCopy(
    val title: String,
    val label: String,
    val subtitle: String,
    val freeFocus: String,
    val proFocus: String,
)

private fun UserProfile.copy(): ProfileCopy =
    when (this) {
        UserProfile.Traveler -> ProfileCopy(
            title = "Travel money setup",
            label = "Traveler",
            subtitle = "Trip budget, local cash buffer and destination rates stay near the top.",
            freeFocus = "Budget + core destinations",
            proFocus = "Full cheat sheet + all destinations",
        )
        UserProfile.CryptoHolder -> ProfileCopy(
            title = "Crypto portfolio focus",
            label = "Crypto holder",
            subtitle = "Crypto board, stablecoins and holdings get priority across Home and Portfolio.",
            freeFocus = "BTC, ETH, USDT, USDC",
            proFocus = "Expanded crypto catalog + holdings",
        )
        UserProfile.Remittances -> ProfileCopy(
            title = "Send money smarter",
            label = "Remittances",
            subtitle = "Provider cost, timing and alerts stay visible for repeat transfers.",
            freeFocus = "Mid-market + custom cost",
            proFocus = "Full provider comparison + alerts",
        )
        UserProfile.Freelancer -> ProfileCopy(
            title = "Multi-currency income",
            label = "Freelancer",
            subtitle = "Converter, base currency and income pairs are tuned for cross-border work.",
            freeFocus = "Converter + saved pairs",
            proFocus = "Timing + portfolio + alerts",
        )
        UserProfile.Savings -> ProfileCopy(
            title = "Savings and allocation",
            label = "Savings",
            subtitle = "Portfolio allocation, long-range context and alerts are treated as the main workflow.",
            freeFocus = "Portfolio snapshot",
            proFocus = "P&L, allocation and long history",
        )
    }

private data class ProfilePreset(
    val initialTab: FxTab,
    val moreRoute: MoreRoute = MoreRoute.Menu,
    val converterCodes: List<String>,
    val compareCodes: List<String>,
    val watchlistCodes: List<String>,
    val travelerCurrency: String,
    val suggestedAmount: String,
    val suggestedPair: String,
    val suggestedProvider: String,
    val suggestedAlert: String,
    val suggestedHolding: String,
)

private fun UserProfile.preset(): ProfilePreset =
    when (this) {
        UserProfile.Traveler -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Traveler,
            converterCodes = listOf("EUR", "GBP", "JPY"),
            compareCodes = listOf("EUR", "GBP", "JPY", "MXN"),
            watchlistCodes = listOf("EUR", "GBP", "JPY", "MXN"),
            travelerCurrency = "JPY",
            suggestedAmount = "1000",
            suggestedPair = "USD -> JPY",
            suggestedProvider = "Wise / Revolut",
            suggestedAlert = "Destination rate near 30d high",
            suggestedHolding = "Trip cash budget",
        )
        UserProfile.CryptoHolder -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Watchlist,
            converterCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            compareCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            watchlistCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            travelerCurrency = "EUR",
            suggestedAmount = "1000",
            suggestedPair = "USD -> BTC",
            suggestedProvider = "Mid-market crypto rate",
            suggestedAlert = "BTC/ETH daily move above 3%",
            suggestedHolding = "BTC, ETH and stablecoins",
        )
        UserProfile.Remittances -> ProfilePreset(
            initialTab = FxTab.Convert,
            converterCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            compareCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            watchlistCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            travelerCurrency = "MXN",
            suggestedAmount = "500",
            suggestedPair = "USD -> MXN",
            suggestedProvider = "Wise first, compare bank transfer",
            suggestedAlert = "Target rate above last 7d average",
            suggestedHolding = "Receiver currency balance",
        )
        UserProfile.Freelancer -> ProfilePreset(
            initialTab = FxTab.Convert,
            converterCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            compareCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            watchlistCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            travelerCurrency = "EUR",
            suggestedAmount = "2500",
            suggestedPair = "USD -> EUR",
            suggestedProvider = "Wise / bank transfer",
            suggestedAlert = "Invoice pair moves 1% in a day",
            suggestedHolding = "Client payment currencies",
        )
        UserProfile.Savings -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Watchlist,
            converterCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            compareCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            watchlistCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            travelerCurrency = "CHF",
            suggestedAmount = "1000",
            suggestedPair = "USD -> CHF",
            suggestedProvider = "Mid-market baseline",
            suggestedAlert = "Portfolio allocation drift above 5%",
            suggestedHolding = "Core savings currencies",
        )
    }

private data class ProfileAlertSuggestion(
    val rate: FxRate,
    val target: Double,
    val direction: AlertDirection,
    val kind: AlertKind,
)

private data class ProfileAction(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
)

private data class ProfileWorkflow(
    val primary: String,
    val nextStep: String,
    val proFit: String,
)

private fun UserProfile.workflowCopy(): ProfileWorkflow =
    when (this) {
        UserProfile.Traveler -> ProfileWorkflow("Trip wallet + scanner", "Scan local price before paying", "Saved trips, OCR and full cheat sheet")
        UserProfile.CryptoHolder -> ProfileWorkflow("Crypto watch + breakouts", "Create movement alert", "Expanded crypto catalog and advanced alerts")
        UserProfile.Remittances -> ProfileWorkflow("Repeat transfer decision", "Compare provider route now", "Provider matrix, recurring plan and unlimited alerts")
        UserProfile.Freelancer -> ProfileWorkflow("Invoice currency control", "Check invoice amount and timing", "Timing horizons and saved working pairs")
        UserProfile.Savings -> ProfileWorkflow("Allocation watch", "Review drift and set alert", "Long-range history and portfolio alerts")
    }

private fun UserProfile.nextActionCopy(): ProfileAction =
    when (this) {
        UserProfile.Traveler -> ProfileAction(
            title = "Scan a local price",
            subtitle = "Open your travel price scanner and compare against the live mid-market rate.",
            actionLabel = "Scan price",
        )
        UserProfile.CryptoHolder -> ProfileAction(
            title = "Create a movement alert",
            subtitle = "Turn your profile signal into an alert before the rate moves away.",
            actionLabel = "Create suggested alert",
        )
        UserProfile.Remittances -> ProfileAction(
            title = "Review transfer cost",
            subtitle = "Check provider loss and hidden markup before sending money.",
            actionLabel = "Review transfer cost",
        )
        UserProfile.Freelancer -> ProfileAction(
            title = "Check invoice currency",
            subtitle = "Keep your working pair, fees and timing visible.",
            actionLabel = "Convert",
        )
        UserProfile.Savings -> ProfileAction(
            title = "Review allocation drift",
            subtitle = "Track savings currencies and long-range movement from one place.",
            actionLabel = "Watchlist",
        )
    }

private fun suggestedProfileAlert(
    profile: UserProfile,
    liveState: LiveRatesState,
    isPremium: Boolean,
): ProfileAlertSuggestion? {
    val quote = profile.preset().suggestedPair.substringAfter("->", "").trim()
    val rate = liveState.alertRates(isPremium).firstOrNull { it.code == quote } ?: return null
    return when (profile) {
        UserProfile.Traveler -> ProfileAlertSuggestion(rate, rate.rate * 1.005, AlertDirection.Above, AlertKind.Target)
        UserProfile.CryptoHolder -> ProfileAlertSuggestion(rate, 3.0, AlertDirection.Above, AlertKind.DailyChange)
        UserProfile.Remittances -> ProfileAlertSuggestion(rate, rate.rate * 1.01, AlertDirection.Above, AlertKind.Target)
        UserProfile.Freelancer -> ProfileAlertSuggestion(rate, 1.0, AlertDirection.Above, AlertKind.DailyChange)
        UserProfile.Savings -> ProfileAlertSuggestion(rate, 5.0, AlertDirection.Above, AlertKind.DailyChange)
    }
}

private fun suggestedProfileAlertState(
    profile: UserProfile,
    liveState: LiveRatesState,
    isPremium: Boolean,
    alerts: List<PriceAlert>,
): QuickAlertState? {
    val suggestion = suggestedProfileAlert(profile, liveState, isPremium) ?: return null
    val existing = alerts.findMatchingAlert(
        baseCurrency = liveState.baseCurrency,
        quote = suggestion.rate.code,
        target = suggestion.target,
        direction = suggestion.direction,
        kind = suggestion.kind,
    )
    return when {
        existing?.enabled == true -> QuickAlertState.Active
        existing != null -> QuickAlertState.Paused
        canCreateAlert(SubscriptionState(isPremium = isPremium), alerts.size) -> QuickAlertState.Create
        else -> QuickAlertState.Locked
    }
}

@Composable
private fun ProfileInsightCard(
    profile: UserProfile,
    isPremium: Boolean,
    suggestedAlertState: QuickAlertState?,
    modifier: Modifier = Modifier,
    onCreateSuggestedAlert: () -> Unit,
) {
    val copy = profile.copy()
    val preset = profile.preset()
    BentoCard(modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("${ui("FOR YOU")} · ${ui(copy.label)}", color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Free"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            Text(ui(copy.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui(copy.subtitle), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileMetricTile(ui("Free focus"), ui(copy.freeFocus), null, Modifier.weight(1f).testTag("dashboard_profile_free_focus"))
                ProfileMetricTile(ui("Pro focus"), ui(copy.proFocus), null, Modifier.weight(1f).testTag("dashboard_profile_pro_focus"))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileMetricTile(ui("Suggested pair"), preset.suggestedPair, preset.suggestedProvider, Modifier.weight(1f).testTag("dashboard_profile_pair"))
                ProfileMetricTile(ui("Suggested alert"), ui(preset.suggestedAlert), ui(preset.suggestedHolding), Modifier.weight(1f).testTag("dashboard_profile_alert"))
            }
            suggestedAlertState?.let { state ->
                PrimaryButton(
                    text = ui(state.profileAlertActionLabel),
                    modifier = Modifier.fillMaxWidth().testTag("dashboard_profile_alert_action"),
                    onClick = onCreateSuggestedAlert,
                )
            }
        }
    }
}

@Composable
private fun ProfileActionCard(
    profile: UserProfile,
    onCreateSuggestedAlert: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenTraveler: () -> Unit,
    onOpenWatchlist: () -> Unit,
) {
    val action = profile.nextActionCopy()
    BentoCard(Modifier.testTag("dashboard_profile_action"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(ui("PROFILE ACTION"), color = FxTheme.colors.accent)
            Text(ui(action.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui(action.subtitle), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            GhostButton(
                text = ui(action.actionLabel),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_profile_action_button"),
                onClick = {
                    Observability.event("profile_action_clicked", mapOf("profile" to profile.name, "action" to action.title))
                    when (profile) {
                        UserProfile.Traveler -> onOpenTraveler()
                        UserProfile.CryptoHolder -> onCreateSuggestedAlert()
                        UserProfile.Remittances,
                        UserProfile.Freelancer -> onOpenConverter()
                        UserProfile.Savings -> onOpenWatchlist()
                    }
                },
            )
        }
    }
}

@Composable
private fun ProfileWorkflowCard(profile: UserProfile, isPremium: Boolean) {
    val workflow = profile.workflowCopy()
    BentoCard(Modifier.testTag("dashboard_profile_workflow"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("PROFILE WORKFLOW"), color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Free"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            KeyValueRow(ui("Primary workflow"), ui(workflow.primary), null, modifier = Modifier.testTag("dashboard_profile_workflow_primary"))
            KeyValueRow(ui("Recommended next step"), ui(workflow.nextStep), null, modifier = Modifier.testTag("dashboard_profile_workflow_next"))
            KeyValueRow(ui("Monetization fit"), ui(workflow.proFit), null, modifier = Modifier.testTag("dashboard_profile_workflow_pro"))
        }
    }
}

private val QuickAlertState.profileAlertActionLabel: String
    get() = when (this) {
        QuickAlertState.Create -> "Create suggested alert"
        QuickAlertState.Active -> "Suggested alert active"
        QuickAlertState.Paused -> "Reactivate suggested alert"
        QuickAlertState.Locked -> "Unlock suggested alert"
    }

@Composable
private fun ProfileMetricTile(
    label: String,
    value: String,
    sub: String?,
    modifier: Modifier = Modifier,
) {
    BentoTile(
        modifier = modifier.heightIn(min = 108.dp),
        padding = 13.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                label.uppercase(),
                style = FxTheme.typography.eyebrow,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )
            sub?.let {
                Text(
                    it,
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    trackedCurrencyCodes: List<String> = emptyList(),
    userProfile: UserProfile = UserProfile.Traveler,
    suggestedProfileAlertState: QuickAlertState? = null,
    onRefresh: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
    onEditFavorites: () -> Unit,
    onSeeAllCrypto: () -> Unit,
    onCreateSuggestedAlert: () -> Unit = {},
    onOpenConverter: () -> Unit = {},
    onOpenTraveler: () -> Unit = {},
    onOpenWatchlist: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val preset = userProfile.preset()
    val profileFavorites = remember(liveState.favorites, userProfile, access.favoriteLimit) {
        val ordered = liveState.favorites.sortedWith(compareBy<FxRate> {
            val index = preset.watchlistCodes.indexOf(it.code)
            if (index == -1) Int.MAX_VALUE else index
        }.thenBy { it.code })
        ordered.take(access.favoriteLimit.cap(ordered.size))
    }
    val visibleFavorites = profileFavorites
    val visibleCrypto = liveState.visibleDashboardCryptoRates(subscriptionState.isPremium, trackedCurrencyCodes)
    val cryptoAverageMove = visibleCrypto.takeIf { it.isNotEmpty() }?.map { it.change24h }?.average() ?: 0.0
    val strongestCrypto = visibleCrypto.maxByOrNull { it.change24h }
    val stablecoinCount = visibleCrypto.count { it.code in StablecoinCodes }
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LiveDot(Modifier.size(9.dp))
                Eyebrow(if (liveState.isLive) ui("LIVE") else ui("CACHED"), color = FxTheme.colors.accent)
            }
            Text(compactRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, textAlign = TextAlign.End)
        }
        ScreenHeader(
            title = ui("Rates"),
            subtitle = "${ui("base")} · ${liveState.baseCurrency}  ·  ${visibleFavorites.size}/${liveState.favorites.size} ${ui("favorites")} · ${localizedRuntimeLabel(liveState.autoRefreshLabel)}",
            right = { Text("↻", style = FxTheme.typography.numberL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onRefresh)) },
        )
        RateTrustCard(
            liveState = liveState,
            modifier = Modifier.testTag("dashboard_rate_trust"),
        )
        RateTrustDetailsCard(
            liveState = liveState,
            modifier = Modifier.testTag("dashboard_trust_details"),
        )
        if (liveState.errorMessage != null) {
            Text(ui("Live backend unavailable · using cached UI data"), style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Loading rates"),
                rows = 4,
                modifier = Modifier.testTag("dashboard_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing market cards"),
                rows = 5,
                modifier = Modifier.testTag("dashboard_market_loading_skeleton"),
            )
        } else {
            ProfileInsightCard(
                profile = userProfile,
                isPremium = subscriptionState.isPremium,
                suggestedAlertState = suggestedProfileAlertState,
                modifier = Modifier.testTag("dashboard_profile_card"),
                onCreateSuggestedAlert = onCreateSuggestedAlert,
            )
            ProfileActionCard(
                profile = userProfile,
                onCreateSuggestedAlert = onCreateSuggestedAlert,
                onOpenConverter = onOpenConverter,
                onOpenTraveler = onOpenTraveler,
                onOpenWatchlist = onOpenWatchlist,
            )
            ProfileWorkflowCard(
                profile = userProfile,
                isPremium = subscriptionState.isPremium,
            )
            HeroRateCard(visibleFavorites.firstOrNull() ?: FavoriteRates.first(), liveState.baseCurrency)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(ui("VOLATILITY · 24H"), "0.42%", null, Modifier.weight(1f).height(76.dp))
                liveState.favorites.firstOrNull { it.code == "GBP" }?.let { MetricTile("GBP · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                liveState.favorites.firstOrNull { it.code == "JPY" }?.let { MetricTile("JPY · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
                liveState.favorites.firstOrNull { it.code == "MXN" }?.let { MetricTile("MXN · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
            }
            SectionLabel(
                "${ui("FAVORITES")} · ${visibleFavorites.size}",
                right = if (subscriptionState.isPremium) ui("Edit") else ui("Pro"),
                onRightClick = onEditFavorites,
            )
            BentoCard(padding = 0.dp) {
                Column {
                    visibleFavorites.forEach { rate ->
                        CurrencyRow(localizedRate(rate), dense = true, onClick = { onOpenDetail(rate) })
                    }
                }
            }
            if (!subscriptionState.isPremium) {
                ProUpsellCard(
                    title = ui("Unlock full watchlists"),
                    subtitle = ui("Pro adds more favorites, extended history, alerts and complete fee comparison."),
                    onClick = onOpenPaywall,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).testTag("dashboard_crypto_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Eyebrow(ui("CRYPTO MARKET"))
                Text(
                    ui("See all"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier.testTag("dashboard_crypto_see_all").clickable(onClick = onSeeAllCrypto),
                )
            }
            if (visibleCrypto.isEmpty()) {
                BentoCard(Modifier.testTag("dashboard_crypto_empty"), padding = 12.dp) {
                    Text(ui("No crypto rates yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
                }
            } else {
                BentoCard(Modifier.testTag("dashboard_crypto_snapshot"), padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CryptoMetricTile(ui("Crypto"), "${visibleCrypto.size}", ui("major crypto assets"), Modifier.weight(1f).testTag("dashboard_crypto_count"))
                            CryptoMetricTile(ui("24H avg"), formatChange(cryptoAverageMove), strongestCrypto?.code, Modifier.weight(1f).testTag("dashboard_crypto_avg"))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CryptoMetricTile(ui("Stablecoins"), "$stablecoinCount", "USDT / USDC", Modifier.weight(1f).testTag("dashboard_crypto_stablecoins"))
                            CryptoMetricTile(ui("Strongest"), strongestCrypto?.code ?: "--", strongestCrypto?.let { formatChange(it.change24h) }, Modifier.weight(1f).testTag("dashboard_crypto_strongest"))
                        }
                        Text(ui("live crypto movers"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                    }
                }
                BentoCard(Modifier.testTag("dashboard_crypto_list"), padding = 0.dp) {
                    Column {
                        visibleCrypto.forEach { rate ->
                            CryptoAssetRow(rate, liveState.baseCurrency, onClick = { onOpenDetail(rate) })
                        }
                    }
                }
                if (!subscriptionState.isPremium && liveState.crypto.size > visibleCrypto.size) {
                    Box(Modifier.testTag("dashboard_crypto_upsell")) {
                        ProUpsellCard(
                            title = ui("Unlock full watchlists"),
                            subtitle = ui("Pro shows the full crypto board across compare, alerts and portfolio."),
                            onClick = onOpenPaywall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RateTrustCard(
    liveState: LiveRatesState,
    modifier: Modifier = Modifier,
    providerOverride: String? = null,
    updatedOverride: String? = null,
) {
    val loading = liveState.isInitialRateLoading()
    val source = providerOverride?.takeIf { it.isNotBlank() } ?: liveState.rateProviderLabel()
    val updated = updatedOverride?.takeIf { it.isNotBlank() } ?: liveState.updatedLabel
    val status = when {
        loading -> ui("Loading")
        liveState.isOfflineCache -> ui("Cached")
        liveState.isLive -> ui("Live")
        else -> ui("Preview")
    }
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("RATE TRUST"), color = FxTheme.colors.accent)
                Pill(status, variant = if (liveState.isLive) PillVariant.Accent else PillVariant.Ghost)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (loading) {
                    TrustMetricSkeleton(ui("Source"), Modifier.weight(1f).testTag("rate_trust_source_loading"))
                    TrustMetricSkeleton(ui("Updated"), Modifier.weight(1f).testTag("rate_trust_updated_loading"))
                } else {
                    TrustMetric(ui("Source"), compactProviderLabel(source), Modifier.weight(1f).testTag("rate_trust_source"))
                    TrustMetric(ui("Updated"), compactRuntimeLabel(updated), Modifier.weight(1f).testTag("rate_trust_updated"))
                }
            }
            Text(
                ui("Indicative mid-market rates. Final transfer or card rates can include provider fees and markups."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
        }
    }
}

@Composable
private fun TrustMetricSkeleton(label: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "trustSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "trustSkeletonAlpha",
    )
    Column(
        modifier = modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.6f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(label.uppercase(), style = FxTheme.typography.eyebrow, color = FxTheme.colors.textFaint, maxLines = 1)
        Box(
            Modifier
                .fillMaxWidth(0.84f)
                .height(14.dp)
                .clip(FxTheme.shapes.field)
                .background(FxTheme.colors.surface3.copy(alpha = alpha)),
        )
        Box(
            Modifier
                .fillMaxWidth(0.58f)
                .height(10.dp)
                .clip(FxTheme.shapes.field)
                .background(FxTheme.colors.surface3.copy(alpha = alpha * 0.78f)),
        )
    }
}

@Composable
private fun TrustMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.6f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label.uppercase(), style = FxTheme.typography.eyebrow, color = FxTheme.colors.textFaint, maxLines = 1)
        Text(value, style = FxTheme.typography.captionMono, color = FxTheme.colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RateTrustDetailsCard(
    liveState: LiveRatesState,
    modifier: Modifier = Modifier,
) {
    val loading = liveState.isInitialRateLoading()
    val decisionGrade = when {
        loading -> "Loading"
        liveState.isLive -> "Live"
        else -> "Cached"
    }
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("TRUST DETAILS"), color = FxTheme.colors.accent)
                Pill(ui(decisionGrade), variant = if (liveState.isLive) PillVariant.Up else PillVariant.Ghost)
            }
            if (loading) {
                InlineSkeletonRows(
                    rows = 4,
                    modifier = Modifier.testTag("trust_details_loading_skeleton"),
                )
            } else {
                KeyValueRow(
                    ui("Decision grade"),
                    ui(decisionGrade),
                    "${ui("Source")} ${compactProviderLabel(liveState.rateProviderLabel())} · ${ui("Updated")} ${compactRuntimeLabel(liveState.updatedLabel)}",
                    modifier = Modifier.testTag("trust_decision_grade"),
                )
                KeyValueRow(
                    ui("Provider rates can differ"),
                    ui("Fees + spread"),
                    ui("We use mid-market rates for intelligence; providers can add fees, spread, delivery limits and card/cash markups."),
                    modifier = Modifier.testTag("trust_provider_disclaimer"),
                )
            }
        }
    }
}

@Composable
private fun LoadingSkeletonCard(title: String, rows: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(title, color = FxTheme.colors.accent)
            repeat(rows) { index ->
                Box(
                    Modifier
                        .fillMaxWidth(if (index % 2 == 0) 1f else 0.78f)
                        .height(14.dp)
                        .clip(FxTheme.shapes.field)
                        .background(FxTheme.colors.surface3.copy(alpha = alpha)),
                )
            }
        }
    }
}

@Composable
private fun InlineSkeletonRows(rows: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "inlineSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "inlineSkeletonAlpha",
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { index ->
            Box(
                Modifier
                    .fillMaxWidth(if (index % 2 == 0) 1f else 0.68f)
                    .height(14.dp)
                    .clip(FxTheme.shapes.field)
                    .background(FxTheme.colors.surface3.copy(alpha = alpha)),
            )
        }
    }
}

private fun LiveRatesState.rateProviderLabel(): String {
    val parts = updatedLabel.split("·").map { it.trim() }.filter { it.isNotBlank() }
    return parts.getOrNull(1)
        ?.takeUnless { it.contains("refreshed", ignoreCase = true) || it.contains("cached", ignoreCase = true) }
        ?: if (crypto.isNotEmpty()) "FX backend / CoinPaprika" else "FX backend"
}

private fun compactProviderLabel(label: String): String =
    when {
        label.contains("Frankfurter", ignoreCase = true) ||
            label.contains("European Central Bank", ignoreCase = true) -> "ECB / Frankfurter"
        label.contains("CoinPaprika", ignoreCase = true) -> "FX / CoinPaprika"
        label.length > 24 -> label.take(21).trimEnd() + "..."
        else -> label
    }

@Composable
private fun compactRuntimeLabel(label: String): String {
    val localized = localizedRuntimeLabel(label)
    return when {
        localized == ui("loading") -> ui("loading")
        label.contains("Frankfurter", ignoreCase = true) ||
            label.contains("European Central Bank", ignoreCase = true) -> {
            val date = label.substringBefore("·").trim().takeIf { it.isNotBlank() }
            val refreshed = label.substringAfterLast("refreshed", "").trim()
            listOfNotNull(date, refreshed.takeIf { it.isNotBlank() }?.let { "${ui("refreshed")} $it" })
                .joinToString(" · ")
                .ifBlank { "ECB" }
        }
        localized.length > 34 -> localized.take(31).trimEnd() + "..."
        else -> localized
    }
}

private fun LiveRatesState.isInitialRateLoading(): Boolean =
    isLoading && !isLive && !isOfflineCache

@Composable
private fun CryptoMetricTile(
    label: String,
    value: String,
    sub: String?,
    modifier: Modifier = Modifier,
) {
    BentoTile(
        modifier = modifier.heightIn(min = 98.dp),
        padding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label.uppercase(),
                style = FxTheme.typography.eyebrow,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    value,
                    style = FxTheme.typography.numberBody,
                    color = FxTheme.colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    sub.orEmpty(),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun CryptoAssetRow(rate: FxRate, baseCurrency: String, onClick: () -> Unit) {
    val inversePrice = if (rate.rate > 0.0) 1.0 / rate.rate else 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_crypto_${rate.code}")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 34.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(rate.name, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("$baseCurrency ${formatMoneyValue(inversePrice)} · ${ui("per coin")}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        }
        SparkLine(rate.sparkline, Modifier.size(64.dp, 26.dp), color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down, showLastDot = true)
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 64.dp)) {
            Text(formatCryptoAmount(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.text)
            Spacer(Modifier.height(2.dp))
            Text(formatChange(rate.change24h), style = FxTheme.typography.captionMono, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
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
                Pill(ui("pinned"), variant = PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(formatRate(rate.rate), style = FxTheme.typography.numberXL.copy(fontSize = 44.sp, lineHeight = 44.sp), color = FxTheme.colors.text)
                Text(formatChange(rate.change24h), style = FxTheme.typography.numberBody, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Eyebrow(ui("24H RANGE"))
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
    alertsState: AlertsState = AlertsState(),
    subscriptionState: SubscriptionState,
    selectedCurrencyCodes: List<String> = emptyList(),
    selectedProviderCodes: List<String> = emptyList(),
    onCurrencyCodesChange: (List<String>) -> Unit = {},
    onOpenPaywall: () -> Unit,
    onCreateTransferAlert: (FxRate, FxRate, Double) -> Unit = { _, _, _ -> },
    onOpenProviderUrl: (String) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val focusManager = LocalFocusManager.current
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val availableRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat, liveState.crypto, subscriptionState.isPremium) {
        liveState.converterAvailableRates(subscriptionState.isPremium)
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
    var amountText by remember { mutableStateOf(sanitizeAmountInput(AppSettingsPrefs.converterAmountText())) }
    var amountFocused by remember { mutableStateOf(false) }
    var customFixedFeeText by remember { mutableStateOf("0") }
    var customFeePercentText by remember { mutableStateOf("1.00") }
    var customMarkupPercentText by remember { mutableStateOf("2.50") }
    var remittanceCadence by remember { mutableStateOf("Monthly") }
    var transferPurpose by remember { mutableStateOf("Family") }
    var transferDecisionHistory by remember { mutableStateOf(emptyList<TransferDecision>()) }
    var scannedPriceText by remember { mutableStateOf("25") }
    var priceScannerHistory by remember { mutableStateOf(emptyList<PriceScannerHistoryEntry>()) }
    val sourceRate = rates.firstOrNull { it.code == sourceCode }
        ?: rates.firstOrNull { it.code == liveState.baseCurrency }
        ?: rates.first()
    val targetRate = rates.firstOrNull { it.code == targetCode && it.code != sourceRate.code }
        ?: rates.firstOrNull { it.code != sourceRate.code }
        ?: sourceRate
    var localMarketRateText by remember(sourceRate.code, targetRate.code) { mutableStateOf(formatRate(targetRate.rate * 1.08)) }
    val amountValue = parseAmountInput(amountText)
    val localMarketRate = parseAmountInput(localMarketRateText).takeIf { it > 0.0 } ?: targetRate.rate
    val customFee = CustomFeeInput(
        fixedFee = parseAmountInput(customFixedFeeText),
        feePercent = parseAmountInput(customFeePercentText),
        markupPercent = parseAmountInput(customMarkupPercentText),
    )
    val providerCodes = remember(selectedProviderCodes, sourceRate.code, targetRate.code) {
        normalizeProviderPreferenceCodes(selectedProviderCodes, sourceRate.code, targetRate.code)
    }
    val allFeeQuotes = estimatedFeeQuotes(sourceRate, targetRate, amountValue, customFee, providerCodes)
    val feeQuotes = if (access.canUseFullFeeComparison) {
        allFeeQuotes.take(EstimatedFeeQuoteCount)
    } else {
        val freeProviderIds = FreeFeeProviderIds + providerCodes.quoteCapableProviderCodes().take(FreeQuoteProviderLimit)
        allFeeQuotes.filter { it.providerId in freeProviderIds }
    }
    val bestQuote = feeQuotes.minByOrNull { it.lossTargetValue }
    val bestRealWorldQuote = feeQuotes
        .filterNot { it.provider == "Mid-market" }
        .minByOrNull { it.lossTargetValue }
    val worstQuote = feeQuotes.maxByOrNull { it.lossTargetValue }
    val customQuote = feeQuotes.firstOrNull { it.provider == "Custom" }
    val potentialSavings = bestQuote?.let { best ->
        worstQuote?.let { worst -> (worst.lossTargetValue - best.lossTargetValue).coerceAtLeast(0.0) }
    } ?: 0.0
    val timingInsight = remember(sourceRate, targetRate) { smartTimingInsight(sourceRate, targetRate) }
    if (showCurrencyPicker) {
        CurrencyListPickerSheet(
            title = ui("Edit converter list"),
            lockedSubtitle = ui("Pro unlocks more converter currencies"),
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
            Eyebrow(ui("MID"), color = FxTheme.colors.accent)
            Text(
                compactRuntimeLabel(liveState.updatedLabel),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ScreenHeader(ui("Convert"), subtitle = ui("Multi-currency · live to 4 decimals"))
        RateTrustCard(
            liveState = liveState,
            modifier = Modifier.testTag("converter_rate_trust"),
        )
        RateTrustDetailsCard(
            liveState = liveState,
            modifier = Modifier.testTag("converter_trust_details"),
        )
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing converter rates"),
                rows = 4,
                modifier = Modifier.testTag("converter_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing fee estimates"),
                rows = 5,
                modifier = Modifier.testTag("converter_fee_loading_skeleton"),
            )
        } else {
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(ui("YOU SEND"))
                    Pill(sourceRate.code, variant = PillVariant.Accent)
                }
                BasicTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = sanitizeAmountInput(raw)
                        AppSettingsPrefs.setConverterAmountText(amountText)
                    },
                    singleLine = true,
                    textStyle = FxTheme.typography.numberXL.copy(color = FxTheme.colors.text, fontSize = 38.sp, lineHeight = 40.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .testTag("converter_amount_input")
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
                    Text("${ui("Converted to")} ${targetRate.code}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
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
                                Observability.event(
                                    "converter_target_selected",
                                    mapOf("source" to sourceRate.code, "target" to rate.code),
                                )
                                focusManager.clearFocus()
                            }
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton(
                "⇄  ${ui("Reverse")}",
                Modifier.weight(1f),
                onClick = {
                    val previousSource = sourceRate
                    val previousTarget = targetRate
                    sourceCode = previousTarget.code
                    targetCode = previousSource.code
                    amountText = formatInputAmount(convertedAmount(amountValue, previousSource, previousTarget))
                    AppSettingsPrefs.setConverterAmountText(amountText)
                    Observability.event(
                        "converter_reversed",
                        mapOf("source" to previousSource.code, "target" to previousTarget.code),
                    )
                    focusManager.clearFocus()
                },
            )
            GhostButton("≡  ${ui("Edit list")}", Modifier.weight(1f).testTag("converter_edit_list"), onClick = { showCurrencyPicker = true })
        }
        SectionLabel("${ui("SMART TIMING")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        SmartTimingCard(
            insight = timingInsight,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )
        SectionLabel("${ui("LOCAL RATE NOTEBOOK")} · ${sourceRate.code} → ${targetRate.code}")
        LocalRateNotebookCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            localMarketRateText = localMarketRateText,
            localMarketRate = localMarketRate,
            onLocalMarketRateChange = { localMarketRateText = sanitizeAmountInput(it) },
        )
        SectionLabel("${ui("PRICE SCANNER")} · ${targetRate.code} → ${sourceRate.code}", right = if (subscriptionState.isPremium) ui("OCR beta") else ui("Preview"))
        PriceScannerCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            scannedPriceText = scannedPriceText,
            localMarketRate = localMarketRate,
            isPremium = subscriptionState.isPremium,
            onScannedPriceChange = { scannedPriceText = sanitizeAmountInput(it) },
            onScannedPriceDetected = { amount, detectedCurrency ->
                scannedPriceText = sanitizeAmountInput(amount)
                val normalizedCurrency = detectedCurrency?.uppercase()
                val detectedTargetRate = rates.firstOrNull { it.code == normalizedCurrency && it.code != sourceRate.code } ?: targetRate
                if (normalizedCurrency != null && normalizedCurrency != targetRate.code && rates.any { it.code == normalizedCurrency && it.code != sourceRate.code }) {
                    targetCode = normalizedCurrency
                }
                Observability.event(
                    "price_scanner_result_used",
                    mapOf("source" to sourceRate.code, "target" to (normalizedCurrency ?: targetRate.code)),
                )
                priceScannerHistory = (listOf(
                    PriceScannerHistoryEntry(
                        amountText = sanitizeAmountInput(amount),
                        targetCode = detectedTargetRate.code,
                        sourceCode = sourceRate.code,
                        liveSourceCost = liveSourceCostFor(parseAmountInput(amount), detectedTargetRate),
                        hiddenCost = hiddenCostFor(parseAmountInput(amount), detectedTargetRate, localMarketRate),
                    ),
                ) + priceScannerHistory).take(4)
            },
            history = priceScannerHistory,
            onOpenPaywall = onOpenPaywall,
        )
        SectionLabel("${ui("FEES")} · ${sourceRate.code} → ${targetRate.code}", right = if (access.canUseFullFeeComparison) ui("Estimated") else ui("Preview"))
        FeeRealityCheckCard(
            quote = bestRealWorldQuote ?: bestQuote,
        )
        SectionLabel("${ui("REMITTANCE PLAN")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        RemittancePlannerCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            quote = bestRealWorldQuote ?: bestQuote,
            cadence = remittanceCadence,
            isPremium = subscriptionState.isPremium,
            onCadenceChange = { remittanceCadence = it },
            onOpenPaywall = onOpenPaywall,
        )
        SectionLabel("${ui("TRANSFER INTENT")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        TransferIntentCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            quote = bestRealWorldQuote ?: bestQuote,
            purpose = transferPurpose,
            history = transferDecisionHistory,
            matchingAlert = alertsState.alerts.findMatchingAlert(
                baseCurrency = sourceRate.code,
                quote = targetRate.code,
                target = transferAlertTarget(sourceRate, targetRate),
                direction = AlertDirection.Above,
                kind = AlertKind.Target,
            ),
            isPremium = subscriptionState.isPremium,
            onPurposeChange = { transferPurpose = it },
            onDecisionSaved = { decision ->
                transferDecisionHistory = (listOf(decision) + transferDecisionHistory).take(5)
            },
            onCreateAlert = { onCreateTransferAlert(sourceRate, targetRate, transferAlertTarget(sourceRate, targetRate)) },
            onOpenProviderUrl = onOpenProviderUrl,
            onOpenPaywall = onOpenPaywall,
        )
        SectionLabel(ui("PROVIDER MATRIX"), right = if (access.canUseFullFeeComparison) ui("Estimated") else ui("Preview"))
        ProviderMatrixCard(
            quotes = feeQuotes.filterNot { it.provider == "Mid-market" },
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(
                        ui("Best provider"),
                        bestQuote?.provider?.let { ui(it) } ?: "--",
                        bestQuote?.let { "${ui("Recipient gets")} ${it.amount}" },
                        Modifier.weight(1f).testTag("converter_best_provider"),
                    )
                    MetricTile(
                        ui("Potential savings"),
                        formatConvertedAmount(targetRate, potentialSavings),
                        ui("vs worst visible provider"),
                        Modifier.weight(1f).testTag("converter_provider_savings"),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(
                        ui("Mid-market value"),
                        formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)),
                        ui("before fees and markup"),
                        Modifier.weight(1f).testTag("converter_mid_market_value"),
                    )
                    MetricTile(
                        ui("Best loss"),
                        bestQuote?.loss ?: "${targetRate.code} 0.00",
                        bestQuote?.provider?.let { ui(it) },
                        Modifier.weight(1f).testTag("converter_best_loss"),
                    )
                }
                bestQuote?.let {
                    KeyValueRow(
                        ui("Best route"),
                        "${ui(it.provider)} · ${ui("Recipient gets")} ${it.amount}",
                        "${ui("Loss vs mid-market")} ${it.loss} (${it.lossPercent})",
                        modifier = Modifier.testTag("converter_best_route"),
                    )
                }
                customQuote?.let {
                    KeyValueRow(ui("Your custom cost"), it.loss, "${ui("Effective rate")} ${it.effectiveRate}")
                }
            }
        }
        ProviderComparisonHistoryCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            customFee = customFee,
            selectedProviderCodes = providerCodes,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Eyebrow(ui("CUSTOM COST"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeeInputField(
                        label = ui("Fixed fee"),
                        value = customFixedFeeText,
                        suffix = sourceRate.code,
                        modifier = Modifier.weight(1f),
                        onValueChange = { customFixedFeeText = sanitizeAmountInput(it) },
                    )
                    FeeInputField(
                        label = ui("Fee %"),
                        value = customFeePercentText,
                        suffix = "%",
                        modifier = Modifier.weight(1f),
                        onValueChange = { customFeePercentText = sanitizeAmountInput(it) },
                    )
                    FeeInputField(
                        label = ui("FX markup"),
                        value = customMarkupPercentText,
                        suffix = "%",
                        modifier = Modifier.weight(1f),
                        onValueChange = { customMarkupPercentText = sanitizeAmountInput(it) },
                    )
                }
            }
        }
        BentoCard(padding = 0.dp) {
            Column { feeQuotes.forEachIndexed { index, quote -> FeeComparisonRow(quote, rank = index + 1) } }
        }
        if (!access.canUseFullFeeComparison) {
            ProUpsellCard(
                title = ui("See the real transfer cost"),
                subtitle = ui("Pro unlocks the complete provider list; estimates update with your amount."),
                modifier = Modifier.testTag("converter_fee_upsell"),
                onClick = onOpenPaywall,
            )
        }
        }
    }
}

@Composable
private fun LocalRateNotebookCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    localMarketRateText: String,
    localMarketRate: Double,
    onLocalMarketRateChange: (String) -> Unit,
) {
    val localSpreadPct = if (targetRate.rate > 0.0) ((localMarketRate / targetRate.rate) - 1.0) * 100.0 else 0.0
    BentoCard(Modifier.testTag("converter_local_rate_notebook"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Official mid-market"),
                    formatRate(targetRate.rate),
                    "${sourceRate.code}/${targetRate.code}",
                    Modifier.weight(1f).testTag("local_rate_official"),
                )
                MetricTile(
                    ui("Local spread"),
                    formatSignedPercent(localSpreadPct),
                    ui("vs mid-market"),
                    Modifier.weight(1f).testTag("local_rate_spread"),
                )
            }
            FeeInputField(
                label = ui("Local market"),
                value = localMarketRateText,
                suffix = targetRate.code,
                modifier = Modifier.fillMaxWidth().testTag("local_rate_input"),
                onValueChange = onLocalMarketRateChange,
            )
            KeyValueRow(
                ui("Local market"),
                "${formatRate(localMarketRate)} ${targetRate.code}",
                ui("Track official vs informal rates before exchanging cash."),
                modifier = Modifier.testTag("local_rate_market"),
            )
        }
    }
}

@Composable
private fun PriceScannerCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    scannedPriceText: String,
    localMarketRate: Double,
    isPremium: Boolean,
    onScannedPriceChange: (String) -> Unit,
    onScannedPriceDetected: (amount: String, currencyCode: String?) -> Unit,
    history: List<PriceScannerHistoryEntry> = emptyList(),
    onOpenPaywall: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(scannedPriceText, sourceRate.code, targetRate.code, localMarketRate) { mutableStateOf(false) }
    val scannedPrice = parseAmountInput(scannedPriceText)
    val liveSourceCost = liveSourceCostFor(scannedPrice, targetRate)
    val localSourceCost = if (localMarketRate > 0.0) scannedPrice / localMarketRate else liveSourceCost
    val hiddenCost = localSourceCost - liveSourceCost
    val shareText = remember(scannedPriceText, sourceRate.code, targetRate.code, liveSourceCost, localSourceCost, hiddenCost) {
        buildString {
            append("FX Always price check\n")
            append("Price: ${targetRate.code} $scannedPriceText\n")
            append("At live rate: ${sourceRate.code} ${formatRate(liveSourceCost)}\n")
            append("With local rate: ${sourceRate.code} ${formatRate(localSourceCost)}\n")
            append("Potential hidden cost: ${formatSignedAmount(sourceRate.code, hiddenCost)}")
        }
    }
    BentoCard(Modifier.testTag("converter_price_scanner"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("Camera-ready price check"))
                Pill(if (isPremium) ui("OCR beta") else ui("Preview"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            Text(
                ui("Compare a shop, cash desk or card terminal price against the live mid-market rate."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            FeeInputField(
                label = ui("Scanned price"),
                value = scannedPriceText,
                suffix = targetRate.code,
                modifier = Modifier.fillMaxWidth().testTag("price_scanner_input"),
                onValueChange = onScannedPriceChange,
            )
            if (isPremium) {
                PriceOcrScannerAction(
                    scanLabel = ui("Scan price"),
                    readingLabel = ui("Reading price"),
                    detectedLabel = ui("Detected"),
                    unavailableLabel = ui("No price found. Center one price and try again."),
                    liveTitleLabel = ui("Live price scanner"),
                    liveHintLabel = ui("Point at one price. We'll detect the amount before filling it."),
                    useDetectedLabel = ui("Use detected price"),
                    closeLabel = ui("Close"),
                    currentCurrencyLabel = ui("Current scanner currency"),
                    switchingCurrencyLabel = ui("Switching scanner to detected currency"),
                    targetCurrency = targetRate.code,
                    modifier = Modifier.fillMaxWidth(),
                    onPriceDetected = onScannedPriceDetected,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("At live rate"),
                    "${sourceRate.code} ${formatRate(liveSourceCost)}",
                    "${targetRate.code} ${formatRate(scannedPrice)}",
                    Modifier.weight(1f).testTag("price_scanner_live_cost"),
                )
                MetricTile(
                    ui("With local rate"),
                    "${sourceRate.code} ${formatRate(localSourceCost)}",
                    "${formatRate(localMarketRate)} ${targetRate.code}",
                    Modifier.weight(1f).testTag("price_scanner_local_cost"),
                )
            }
            KeyValueRow(
                ui("Potential hidden cost"),
                formatSignedAmount(sourceRate.code, hiddenCost),
                ui("Type, paste or scan a shelf price; OCR fills this same check automatically."),
                modifier = Modifier.testTag("price_scanner_hidden_cost"),
            )
            if (isPremium) {
                GhostButton(
                    text = if (copied) ui("Copied price check") else ui("Copy price check"),
                    modifier = Modifier.fillMaxWidth().testTag("price_scanner_share"),
                    onClick = {
                        clipboard.setText(AnnotatedString(shareText))
                        copied = true
                        Observability.event("price_check_copied", mapOf("source" to sourceRate.code, "target" to targetRate.code))
                    },
                )
            }
            if (history.isNotEmpty()) {
                SectionLabel(ui("PRICE CHECK HISTORY"), right = ui("Last scanned checks"))
                Column(Modifier.testTag("price_scanner_history"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    history.forEachIndexed { index, item ->
                        KeyValueRow(
                            "${item.targetCode} ${item.amountText}",
                            "${item.sourceCode} ${formatRate(item.liveSourceCost)}",
                            "${ui("Potential hidden cost")} ${formatSignedAmount(item.sourceCode, item.hiddenCost)}",
                            modifier = Modifier.testTag("price_scanner_history_$index"),
                        )
                    }
                }
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks the complete provider list; estimates update with your amount."),
                    modifier = Modifier.fillMaxWidth().testTag("price_scanner_upsell"),
                    onClick = {
                        Observability.event("paywall_opened", mapOf("source" to "price_scanner"))
                        onOpenPaywall()
                    },
                )
            }
        }
    }
}

@Composable
private fun ProviderComparisonHistoryCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    customFee: CustomFeeInput,
    selectedProviderCodes: List<String>,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val history = remember(sourceRate, targetRate, amountValue, customFee, selectedProviderCodes, isPremium) {
        providerComparisonHistory(sourceRate, targetRate, amountValue, customFee, selectedProviderCodes, isPremium)
    }
    BentoCard(Modifier.testTag("converter_provider_history"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow("${ui("PROVIDER HISTORY")} · ${ui("route history")}")
            history.forEachIndexed { index, item ->
                KeyValueRow(
                    item.amountLabel,
                    "${ui(item.provider)} · ${item.recipientAmount}",
                    "${ui("Lost")} ${item.loss} · ${ui("Effective rate")} ${item.effectiveRate}",
                    modifier = Modifier.testTag("provider_history_row_$index"),
                )
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro shows more route history by amount."),
                    modifier = Modifier.fillMaxWidth().testTag("provider_history_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

private data class ProviderHistoryItem(
    val amountLabel: String,
    val provider: String,
    val recipientAmount: String,
    val loss: String,
    val effectiveRate: String,
)

private fun providerComparisonHistory(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    customFee: CustomFeeInput,
    selectedProviderCodes: List<String>,
    isPremium: Boolean,
): List<ProviderHistoryItem> {
    val baseAmount = amountValue.takeIf { it > 0.0 } ?: 100.0
    return listOf(baseAmount * 0.5, baseAmount, baseAmount * 2.0)
        .take(if (isPremium) 3 else 2)
        .map { amount ->
            val quote = estimatedFeeQuotes(sourceRate, targetRate, amount, customFee, selectedProviderCodes)
                .filterNot { it.provider == "Mid-market" }
                .minByOrNull { it.lossTargetValue }
                ?: estimatedFeeQuotes(sourceRate, targetRate, amount, customFee, selectedProviderCodes).first()
            ProviderHistoryItem(
                amountLabel = "${sourceRate.code} ${formatMoneyValue(amount)}",
                provider = quote.provider,
                recipientAmount = quote.amount,
                loss = quote.loss,
                effectiveRate = quote.effectiveRate,
            )
        }
}

@Composable
private fun SmartTimingCard(
    insight: SmartTimingInsight,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    BentoCard(Modifier.testTag("converter_smart_timing"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ui(insight.signal), style = FxTheme.typography.bodyStrong, color = insight.color(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(insight.action, style = FxTheme.typography.caption, color = FxTheme.colors.textDim, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.testTag("converter_timing_action"))
                }
                Box(
                    Modifier
                        .testTag("converter_timing_score")
                        .clip(FxTheme.shapes.field)
                        .background(FxTheme.colors.surface2)
                        .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${insight.score}/100", style = FxTheme.typography.numberBody, color = insight.color(), textAlign = TextAlign.End)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                insight.horizons.take(if (isPremium) insight.horizons.size else 1).forEach { horizon ->
                    SmartTimingHorizonRow(horizon)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimingUseCaseTile(ui("Travel"), insight.travelAdvice, Modifier.weight(1f).testTag("converter_timing_travel"))
                TimingUseCaseTile(ui("Savings"), insight.savingsAdvice, Modifier.weight(1f).testTag("converter_timing_savings"))
                TimingUseCaseTile(ui("Remit"), insight.remittanceAdvice, Modifier.weight(1f).testTag("converter_timing_remit"))
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Unlock 30d and 90d timing"),
                    modifier = Modifier.fillMaxWidth().testTag("converter_timing_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
private fun SmartTimingHorizonRow(horizon: TimingHorizon) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("converter_timing_${horizon.label.lowercase()}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(horizon.label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
            Text(horizon.rangeLabel, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(horizon.positionLabel, style = FxTheme.typography.captionMono, color = FxTheme.colors.text)
            Text("${ui("Trend")} ${horizon.trendLabel} · ${ui("Vol")} ${horizon.volatilityLabel}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun TimingUseCaseTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = FxTheme.typography.caption, color = FxTheme.colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SmartTimingInsight.color(): Color =
    when (signal) {
        "Strong rate" -> FxTheme.colors.up
        "Good time" -> FxTheme.colors.accent
        else -> FxTheme.colors.textDim
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
            .testTag("converter_row_${rate.code}")
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
                if (source) ui("Base currency · source amount") else if (selected) ui("Selected destination") else localizedCurrencyName(rate.name),
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
private fun FeeComparisonRow(quote: EstimatedFeeQuote, rank: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .testTag("fee_quote_${quote.provider}")
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("#$rank", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ui(quote.provider), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    quote.badge?.let { Pill(ui(it), variant = if (quote.isHighFee) PillVariant.Down else PillVariant.Up) }
                }
                Text(
                    "${ui("Recipient gets")} ${quote.amount}",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${ui("Fee")} ${quote.fee} · ${ui("Markup")} ${quote.markup}",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${ui("Delivery")} ${ui(quote.deliverySpeed)} · ${ui("Payment")} ${ui(quote.paymentMethod)}",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(quote.amount, style = FxTheme.typography.numberBody, color = FxTheme.colors.text, textAlign = TextAlign.End)
                Text("${ui("Lost")} ${quote.loss}", style = FxTheme.typography.captionMono, color = if (quote.lossTargetValue > 0.0) FxTheme.colors.down else FxTheme.colors.up)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${ui("Effective rate")} ${quote.effectiveRate}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
            Text("${ui("Loss vs mid-market")} ${quote.lossPercent}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
private fun FeeRealityCheckCard(quote: EstimatedFeeQuote?) {
    if (quote == null) {
        return
    }
    val verdict = quote.realityVerdict()
    BentoCard(Modifier.testTag("converter_fee_reality_check"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(ui("REALITY CHECK"), color = FxTheme.colors.accent)
                    Text(ui("Best real-world route"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
                        "${ui(quote.provider)} · ${ui("This estimate helps you spot hidden fees before you convert.")}",
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("converter_reality_provider"),
                    )
                }
                Pill(ui(verdict.label), variant = verdict.variant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Recipient should get"),
                    quote.amount,
                    ui(quote.provider),
                    Modifier.weight(1f).testTag("converter_reality_recipient"),
                )
                MetricTile(
                    ui("Estimated loss"),
                    quote.loss,
                    "${quote.lossPercent} ${ui("against mid-market")}",
                    Modifier.weight(1f).testTag("converter_reality_loss"),
                )
            }
        }
    }
}

@Composable
private fun TransferIntentCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    quote: EstimatedFeeQuote?,
    purpose: String,
    history: List<TransferDecision>,
    matchingAlert: PriceAlert?,
    isPremium: Boolean,
    onPurposeChange: (String) -> Unit,
    onDecisionSaved: (TransferDecision) -> Unit,
    onCreateAlert: () -> Unit,
    onOpenProviderUrl: (String) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val purposes = if (isPremium) listOf("Family", "Travel", "Invoice", "Savings") else listOf("Family", "Travel")
    val hasAmount = amountValue > 0.0
    var selectedDecision by remember { mutableStateOf<TransferDecision?>(null) }
    var copied by remember { mutableStateOf(false) }
    val currentDecision = remember(sourceRate, targetRate, amountValue, quote, purpose) {
        transferDecision(sourceRate, targetRate, amountValue, quote, purpose)
    }
    if (selectedDecision != null) {
        TransferDecisionSheet(
            decision = selectedDecision ?: currentDecision,
            copied = copied,
            onCopy = {
                clipboard.setText(AnnotatedString((selectedDecision ?: currentDecision).shareText()))
                copied = true
                Observability.event("transfer_decision_copied", mapOf("provider" to (selectedDecision ?: currentDecision).provider))
            },
            onOpenProvider = {
                val url = providerExternalUrl((selectedDecision ?: currentDecision).provider)
                if (url != null) {
                    onOpenProviderUrl(url)
                    Observability.event("transfer_provider_opened", mapOf("provider" to (selectedDecision ?: currentDecision).provider))
                }
            },
            onDismiss = { selectedDecision = null },
        )
    }
    BentoCard(Modifier.testTag("converter_transfer_intent"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("TRANSFER INTENT"), color = FxTheme.colors.accent)
                Pill(if (hasAmount) ui("Ready") else ui("Preview"), variant = if (hasAmount) PillVariant.Up else PillVariant.Ghost)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                purposes.forEach { option ->
                    Pill(
                        text = ui(option),
                        variant = if (purpose == option) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("transfer_purpose_${option.lowercase()}")
                            .clickable {
                                onPurposeChange(option)
                                Observability.event("transfer_intent_purpose_selected", mapOf("purpose" to option))
                            },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(ui("You send"), "${sourceRate.code} ${formatMoneyValue(amountValue)}", ui("Purpose") + " · " + ui(purpose), Modifier.weight(1f).testTag("transfer_intent_send"))
                MetricTile(ui("Receiver gets"), quote?.amount ?: formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)), quote?.provider?.let { ui(it) } ?: ui("Best route now"), Modifier.weight(1f).testTag("transfer_intent_receive"))
            }
            KeyValueRow(
                ui("Best route now"),
                quote?.provider?.let { ui(it) } ?: "--",
                if (hasAmount) {
                    "${ui("Delivery")} ${quote?.deliverySpeed?.let { ui(it) } ?: "--"} · ${ui("Risk")} ${quote?.riskLabel?.let { ui(it) } ?: "--"}"
                } else {
                    ui("Enter an amount to compare real routes.")
                },
                modifier = Modifier.testTag("transfer_intent_best_route"),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton(
                    text = ui("Use this route"),
                    modifier = Modifier.weight(1f).testTag("transfer_intent_use_route"),
                    onClick = {
                        val decision = currentDecision
                        copied = false
                        selectedDecision = decision
                        onDecisionSaved(decision)
                        Observability.event("transfer_intent_route_used", mapOf("provider" to decision.provider))
                    },
                )
                GhostButton(
                    text = if (matchingAlert != null) ui("Better-rate alert active") else ui("Set better-rate alert"),
                    modifier = Modifier.weight(1f).testTag("transfer_intent_set_alert"),
                    onClick = {
                        if (isPremium) {
                            onCreateAlert()
                            Observability.event("transfer_intent_alert_requested", mapOf("target" to targetRate.code))
                        } else {
                            onOpenPaywall()
                        }
                    },
                )
            }
            if (history.isNotEmpty()) {
                SectionLabel(ui("TRANSFER HISTORY"), right = ui("Last decisions"))
                Column(Modifier.testTag("transfer_decision_history"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    history.take(if (isPremium) 5 else 2).forEachIndexed { index, decision ->
                        KeyValueRow(
                            "${decision.sourceCode} ${formatMoneyValue(decision.amountValue)} → ${decision.targetCode}",
                            ui(decision.provider),
                            "${ui("Receiver gets")} ${decision.receiverGets} · ${ui("Risk")} ${ui(decision.riskLabel)}",
                            modifier = Modifier
                                .testTag("transfer_decision_history_$index")
                                .clickable {
                                    copied = false
                                    selectedDecision = decision
                                },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferDecisionSheet(
    decision: TransferDecision,
    copied: Boolean,
    onCopy: () -> Unit,
    onOpenProvider: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val providerUrl = providerExternalUrl(decision.provider)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FxTheme.colors.surface1,
        contentColor = FxTheme.colors.text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .testTag("transfer_decision_sheet")
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(ui("TRANSFER DECISION"), color = FxTheme.colors.accent)
                    Text(ui(decision.provider), style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                }
                Pill(ui(decision.purpose), variant = PillVariant.Accent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(ui("You send"), "${decision.sourceCode} ${formatMoneyValue(decision.amountValue)}", ui("Purpose") + " · " + ui(decision.purpose), Modifier.weight(1f))
                MetricTile(ui("Receiver gets"), decision.receiverGets, ui(decision.provider), Modifier.weight(1f))
            }
            KeyValueRow(ui("Estimated loss"), decision.loss, "${ui("Effective rate")} ${decision.effectiveRate}")
            KeyValueRow(ui("Delivery"), ui(decision.deliverySpeed), "${ui("Payment")} ${ui(decision.paymentMethod)} · ${ui("Risk")} ${ui(decision.riskLabel)}")
            KeyValueRow(ui("External provider"), if (providerUrl == null) ui("Not connected yet") else ui("Open provider"), ui("Provider links can be enabled later with affiliate or deep-link URLs."))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton(
                    text = if (copied) ui("Copied decision") else ui("Copy decision"),
                    modifier = Modifier.weight(1f).testTag("transfer_decision_copy"),
                    onClick = onCopy,
                )
                GhostButton(
                    text = if (providerUrl == null) ui("Provider link pending") else ui("Open provider"),
                    modifier = Modifier.weight(1f).testTag("transfer_decision_provider"),
                    onClick = {
                        if (providerUrl != null) {
                            onOpenProvider()
                        }
                    },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ProviderMatrixCard(
    quotes: List<EstimatedFeeQuote>,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val visibleQuotes = quotes.take(if (isPremium) 6 else 2)
    BentoCard(Modifier.testTag("converter_provider_matrix"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (visibleQuotes.isEmpty()) {
                Text(ui("Enter an amount to compare real routes."), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            visibleQuotes.forEachIndexed { index, quote ->
                KeyValueRow(
                    ui(quote.provider),
                    "${ui("Delivery")} ${ui(quote.deliverySpeed)} · ${ui("Risk")} ${ui(quote.riskLabel)}",
                    "${ui("Payment")} ${ui(quote.paymentMethod)} · ${ui("Best for")} ${ui(quote.bestFor)}",
                    modifier = Modifier.testTag("provider_matrix_row_$index"),
                )
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks the complete provider list; estimates update with your amount."),
                    modifier = Modifier.fillMaxWidth().testTag("provider_matrix_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

private data class FeeRealityVerdict(
    val label: String,
    val variant: PillVariant,
)

private data class TransferDecision(
    val id: String,
    val sourceCode: String,
    val targetCode: String,
    val amountValue: Double,
    val purpose: String,
    val provider: String,
    val receiverGets: String,
    val loss: String,
    val effectiveRate: String,
    val deliverySpeed: String,
    val paymentMethod: String,
    val riskLabel: String,
    val createdAtMillis: Long,
)

private fun transferDecision(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    quote: EstimatedFeeQuote?,
    purpose: String,
): TransferDecision {
    val fallbackAmount = formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate))
    val provider = quote?.provider ?: "Mid-market"
    val createdAt = Clock.System.now().toEpochMilliseconds()
    return TransferDecision(
        id = "${sourceRate.code}-${targetRate.code}-$createdAt",
        sourceCode = sourceRate.code,
        targetCode = targetRate.code,
        amountValue = amountValue,
        purpose = purpose,
        provider = provider,
        receiverGets = quote?.amount ?: fallbackAmount,
        loss = quote?.loss ?: "${targetRate.code} 0.00",
        effectiveRate = quote?.effectiveRate ?: "${formatRate(if (sourceRate.rate == 0.0) 0.0 else targetRate.rate / sourceRate.rate)} ${targetRate.code}",
        deliverySpeed = quote?.deliverySpeed ?: "Instant",
        paymentMethod = quote?.paymentMethod ?: "Debit/bank",
        riskLabel = quote?.riskLabel ?: "Low",
        createdAtMillis = createdAt,
    )
}

private fun transferAlertTarget(sourceRate: FxRate, targetRate: FxRate): Double {
    val currentPairRate = if (sourceRate.rate == 0.0) targetRate.rate else targetRate.rate / sourceRate.rate
    return currentPairRate * 1.01
}

private fun providerExternalUrl(provider: String): String? =
    when (provider) {
        // Keep these disabled until real affiliate/deep-link URLs are configured.
        else -> null
    }

private fun TransferDecision.shareText(): String = buildString {
    append("FX Always transfer decision\n")
    append("Route: $provider\n")
    append("Pair: $sourceCode/$targetCode\n")
    append("You send: $sourceCode ${formatMoneyValue(amountValue)}\n")
    append("Receiver gets: $receiverGets\n")
    append("Estimated loss: $loss\n")
    append("Effective rate: $effectiveRate\n")
    append("Delivery: $deliverySpeed · Payment: $paymentMethod · Risk: $riskLabel")
}

private fun EstimatedFeeQuote.realityVerdict(): FeeRealityVerdict =
    when {
        lossPercentValue <= 0.01 -> FeeRealityVerdict("No markup", PillVariant.Up)
        lossPercentValue < 1.50 -> FeeRealityVerdict("Low cost", PillVariant.Up)
        lossPercentValue < 4.00 -> FeeRealityVerdict("Expensive", PillVariant.Accent)
        else -> FeeRealityVerdict("Avoid", PillVariant.Down)
    }

@Composable
private fun RemittancePlannerCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    quote: EstimatedFeeQuote?,
    cadence: String,
    isPremium: Boolean,
    onCadenceChange: (String) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val cadenceMultiplier = when (cadence) {
        "Biweekly" -> 26
        "One-time" -> 1
        else -> 12
    }
    val yearlyLoss = (quote?.lossTargetValue ?: 0.0) * cadenceMultiplier
    val nextSendWindow = when (cadence) {
        "Biweekly" -> "Next 14 days"
        "One-time" -> "This week"
        else -> "Before payday"
    }
    val planConfidence = when {
        quote == null || amountValue <= 0.0 -> "Needs amount"
        quote.lossPercentValue < 1.0 -> "Good route"
        quote.lossPercentValue < 3.0 -> "Watch fees"
        else -> "Avoid route"
    }
    val cadenceOptions = if (isPremium) {
        listOf("One-time", "Monthly", "Biweekly")
    } else {
        listOf("One-time", "Monthly")
    }
    BentoCard(Modifier.testTag("converter_remittance_planner"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Family route"),
                    "${sourceRate.code} → ${targetRate.code}",
                    quote?.provider?.let { ui(it) } ?: ui("based on current best route"),
                    Modifier.weight(1f).testTag("remittance_family_route"),
                )
                MetricTile(
                    ui("Recipient estimate"),
                    quote?.amount ?: formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)),
                    ui("based on current best route"),
                    Modifier.weight(1f).testTag("remittance_recipient_estimate"),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cadenceOptions.forEach { option ->
                    Pill(
                        ui(option),
                        variant = if (cadence == option) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("remittance_cadence_$option")
                            .clickable { onCadenceChange(option) },
                    )
                }
            }
            KeyValueRow(
                ui("Recurring amount"),
                "${sourceRate.code} ${formatMoneyValue(amountValue * cadenceMultiplier)} / year",
                "${ui(cadence)} · ${quote?.loss ?: "${targetRate.code} 0.00"} ${ui("vs mid-market")}",
                modifier = Modifier.testTag("remittance_recurring_amount"),
            )
            KeyValueRow(
                ui("Reminder cadence"),
                if (isPremium) ui("Before payday") else ui("Monthly"),
                if (isPremium) "${ui("Family route")} · ${sourceRate.code}/${targetRate.code}" else ui("Pro unlocks reminder planning and extra cadences."),
                modifier = Modifier.testTag("remittance_reminder_cadence"),
            )
            KeyValueRow(
                ui("Next send window"),
                ui(nextSendWindow),
                "${ui("Plan confidence")} · ${ui(planConfidence)}",
                modifier = Modifier.testTag("remittance_next_window"),
            )
            KeyValueRow(
                ui("Annual fee drag"),
                "${targetRate.code} ${formatMoneyValue(yearlyLoss)}",
                "${cadenceMultiplier} ${ui("planned sends")} · ${ui("estimated from current route")}",
                modifier = Modifier.testTag("remittance_annual_fee_drag"),
            )
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks reminder planning and extra cadences."),
                    modifier = Modifier.fillMaxWidth().testTag("remittance_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
private fun FeeInputField(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fee_input_$label")
                .clip(FxTheme.shapes.field)
                .background(if (focused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                .border(1.dp, if (focused) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text("0", style = FxTheme.typography.numberBody, color = FxTheme.colors.textGhost)
                        }
                        innerTextField()
                    }
                    Text(suffix, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                }
            },
        )
    }
}

private const val EstimatedFeeQuoteCount = 8

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
        "${detailState.provider} · ${detailState.points.size} pts · ${localizedRuntimeLabel(detailState.updatedLabel)}"
    } else {
        ui("cached preview")
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
                Pill(if (activeForPair > 0) "🔔 $activeForPair ${ui("alert")}" else "★ ${ui("Watching")}")
                Pill(if (effectivePremium) ui("Pro") else ui("Free"))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FlagDot(selected.glyph, selected.kind, size = 36.dp)
            Column {
                Text("${liveState.baseCurrency} / ${selected.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(localizedCurrencyName(selected.name), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(formatRate(selected.rate), style = FxTheme.typography.numberXL, color = FxTheme.colors.text)
            Text(formatChange(selected.change24h), style = FxTheme.typography.numberBody, color = if (selected.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down, modifier = Modifier.padding(bottom = 7.dp))
        }
        Text("${selected.caption?.let { ui(it) } ?: ui("mid-market")} · ${localizedRuntimeLabel(liveState.updatedLabel)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        RateTrustCard(
            liveState = liveState,
            providerOverride = if (detailState.provider.isNotBlank()) detailState.provider else null,
            updatedOverride = if (detailState.updatedLabel.isNotBlank()) detailState.updatedLabel else null,
            modifier = Modifier.testTag("detail_rate_trust"),
        )
        ShareRateCard(
            baseCurrency = liveState.baseCurrency,
            rate = selected,
            provider = if (detailState.provider.isNotBlank()) detailState.provider else liveState.rateProviderLabel(),
            updatedLabel = if (detailState.updatedLabel.isNotBlank()) detailState.updatedLabel else liveState.updatedLabel,
        )
        BentoCard(Modifier.testTag("detail_history_card")) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(if (detailState.isLoading && detailMatches) ui("LOADING HISTORY") else "${ui("HISTORY")} · ${period.label}")
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
                    Text(ui("History unavailable · using cached preview"), style = FxTheme.typography.caption, color = FxTheme.colors.down)
                }
            }
        }
        if (periodIsPro && !effectivePremium) {
            ProUpsellCard(
                title = ui("Unlock long-range history"),
                subtitle = ui("Pro adds 1Y and all-time detail, full event context and deeper market overlays."),
                modifier = Modifier.testTag("detail_history_upsell"),
                onClick = onOpenPaywall,
            )
        }
        SectionLabel("${ui("STATISTICS")} · ${period.label}")
        BentoCard(Modifier.testTag("detail_statistics")) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KeyValueRow(ui("Open"), formatRate(stats.open))
                KeyValueRow(ui("High"), formatRate(stats.high))
                KeyValueRow(ui("Low"), formatRate(stats.low))
                KeyValueRow(ui("Range"), "${formatRate(stats.low)} - ${formatRate(stats.high)}")
                KeyValueRow(ui("Volatility"), "${formatRate(stats.volatilityPct)}%")
                KeyValueRow(ui("Average"), formatRate(stats.average))
            }
        }
        SectionLabel(ui("ECONOMIC CALENDAR"), right = ui("Next 7 days"))
        EconomicCalendarCard(
            rate = selected,
            isPremium = effectivePremium,
            onOpenPaywall = onOpenPaywall,
        )
        SectionLabel(ui("RELATED NEWS"), right = if (newsState.isLoading) ui("Loading") else if (effectivePremium) ui("Live") else ui("Preview"))
        if (relatedStories.isEmpty()) {
            EmptyDetailSection(
                title = if (newsState.isLoading) ui("Loading related news") else ui("No related news"),
                subtitle = if (newsState.isLoading) {
                    "${ui("Fetching market headlines")} ${selected.code}"
                } else {
                    "${ui("No live headlines are currently tied to")} ${selected.code}."
                },
                modifier = Modifier.testTag("detail_news_empty"),
            )
        } else {
            relatedStories.take(if (effectivePremium) relatedStories.size else 2).forEach { story ->
                StoryCard(story, modifier = Modifier.testTag("detail_story_${story.safeTestTagKey()}"), onClick = { onOpenStory(story) })
            }
        }
        SectionLabel(ui("EVENTS · ANNOTATED"), right = if (effectivePremium) ui("Derived") else ui("Preview"))
        if (relatedStories.isEmpty()) {
            EmptyDetailSection(
                title = ui("No annotated events"),
                subtitle = "${ui("Events will appear here when stories include")} ${selected.code}.",
                modifier = Modifier.testTag("detail_events_empty"),
            )
        } else {
            BentoCard(padding = 0.dp) {
                Column {
                    relatedStories.take(if (effectivePremium) relatedStories.size else 2).forEach { story ->
                        DetailEventRow(story, modifier = Modifier.testTag("detail_event_${story.safeTestTagKey()}"), onOpenUrl = onOpenUrl)
                    }
                }
            }
        }
        GhostIconButton(
            icon = MoreFeatureIcon.Alerts,
            text = if (activeForPair > 0) "${ui("Add another alert")} ${selected.code} · $alertLabel" else "${ui("Alert me above")} ${formatRate(selected.rate * 1.01)} · $alertLabel",
            modifier = Modifier.fillMaxWidth().testTag("detail_alert_cta"),
            onClick = { onCreateAlert(selected) },
        )
    }
}

@Composable
private fun ShareRateCard(
    baseCurrency: String,
    rate: FxRate,
    provider: String,
    updatedLabel: String,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(baseCurrency, rate.code, rate.rate, provider, updatedLabel) { mutableStateOf(false) }
    val updatedForDisplay = compactRuntimeLabel(updatedLabel)
    val shareText = remember(baseCurrency, rate, provider, updatedForDisplay) {
        buildString {
            append("FX Always rate card\n")
            append("$baseCurrency / ${rate.code}: ${formatRate(rate.rate)}\n")
            append("24h: ${formatChange(rate.change24h)}\n")
            append("Source: ${compactProviderLabel(provider)}\n")
            append("Updated: $updatedForDisplay\n")
            append("Disclaimer: Indicative only. Check provider fees before sending money.")
        }
    }
    BentoCard(Modifier.testTag("detail_share_rate_card"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(ui("SHARE RATE CARD"), color = FxTheme.colors.accent)
                    Text("$baseCurrency / ${rate.code} · ${formatRate(rate.rate)}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
                        "${ui("Source")} ${compactProviderLabel(provider)} · ${ui("Updated")} ${compactRuntimeLabel(updatedLabel)}",
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("detail_share_rate_source"),
                    )
                }
                Pill(formatChange(rate.change24h), variant = if (rate.change24h >= 0.0) PillVariant.Up else PillVariant.Down)
            }
            Text(
                ui("Indicative only. Check provider fees before sending money."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
                modifier = Modifier.testTag("detail_share_disclaimer"),
            )
            GhostButton(
                text = if (copied) ui("Copied rate card") else ui("Copy rate card"),
                modifier = Modifier.fillMaxWidth().testTag("detail_share_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(shareText))
                    copied = true
                },
            )
        }
    }
}

@Composable
private fun EconomicCalendarCard(
    rate: FxRate,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val events = remember(rate.code, rate.kind) { economicCalendarEvents(rate) }
    var impactFilter by remember(rate.code, rate.kind) { mutableStateOf("All") }
    val filteredEvents = remember(events, impactFilter, isPremium) {
        if (!isPremium || impactFilter == "All") {
            events
        } else {
            events.filter { it.impact == impactFilter }
        }
    }
    BentoCard(Modifier.testTag("detail_economic_calendar"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "High", "Medium").forEach { option ->
                    val locked = option != "All" && !isPremium
                    Pill(
                        text = if (locked) "${ui(option)} · Pro" else ui(option),
                        variant = if (impactFilter == option && !locked) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_calendar_filter_${option.lowercase()}")
                            .clickable {
                                if (locked) {
                                    onOpenPaywall()
                                } else {
                                    impactFilter = option
                                }
                            },
                    )
                }
            }
            filteredEvents.take(if (isPremium) filteredEvents.size else 2).forEachIndexed { index, event ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .testTag("detail_calendar_event_$index")
                        .clip(FxTheme.shapes.field)
                        .background(FxTheme.colors.surface2)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(ui(event.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${event.day} · ${ui(event.topic)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                    }
                    Pill("${ui("Impact")} ${ui(event.impact)}", variant = event.impactVariant)
                }
            }
            KeyValueRow(
                ui("Calendar plan"),
                if (filteredEvents.any { it.impact == "High" }) ui("Watch high-impact windows") else ui("Low event risk"),
                "${filteredEvents.size} ${ui("events")} · ${ui("Next 7 days")}",
                modifier = Modifier.testTag("detail_calendar_plan"),
            )
            if (!isPremium && events.size > 2) {
                GhostButton(
                    text = ui("Pro unlocks the full calendar and impact filters."),
                    modifier = Modifier.fillMaxWidth().testTag("detail_calendar_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

private data class EconomicCalendarEvent(
    val day: String,
    val title: String,
    val topic: String,
    val impact: String,
    val impactVariant: PillVariant,
)

private fun economicCalendarEvents(rate: FxRate): List<EconomicCalendarEvent> =
    if (rate.kind == CurrencyKind.Crypto) {
        listOf(
            EconomicCalendarEvent("Mon", "${rate.code} liquidity watch", "Liquidity", "Medium", PillVariant.Accent),
            EconomicCalendarEvent("Wed", "Network activity pulse", "Network", "Medium", PillVariant.Accent),
            EconomicCalendarEvent("Fri", "Protocol market update", "Protocol", "Low", PillVariant.Ghost),
        )
    } else {
        listOf(
            EconomicCalendarEvent("Tue", "${rate.code} central bank speaker", "Central bank", "High", PillVariant.Down),
            EconomicCalendarEvent("Wed", "${rate.code} inflation print", "Inflation", "High", PillVariant.Down),
            EconomicCalendarEvent("Thu", "${rate.code} jobs update", "Jobs", "Medium", PillVariant.Accent),
            EconomicCalendarEvent("Fri", "${rate.code} growth tracker", "Growth", "Low", PillVariant.Ghost),
        )
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
private fun DetailEventRow(story: NewsStory, modifier: Modifier = Modifier, onOpenUrl: (String) -> Unit) {
    Row(
        modifier
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
private fun EmptyDetailSection(title: String, subtitle: String, modifier: Modifier = Modifier) {
    BentoCard(modifier.fillMaxWidth(), padding = 12.dp) {
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
    selectedCurrencyCodes: List<String> = emptyList(),
    onCurrencyCodesChange: (List<String>) -> Unit = {},
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
) {
    val access = subscriptionState.featureAccess()
    var sortMode by remember { mutableStateOf(CompareSortMode.Movers) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val availableRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat, liveState.crypto, subscriptionState.isPremium) {
        liveState.compareAvailableRates(subscriptionState.isPremium)
    }
    val selectedCodes = remember(liveState.baseCurrency, selectedCurrencyCodes, availableRates, access.compareLimit) {
        compareTargetCodes(selectedCurrencyCodes, availableRates, liveState.baseCurrency, access.compareLimit)
    }
    val compareRates = remember(selectedCodes, availableRates, sortMode) {
        val byCode = availableRates.associateBy { it.code }
        selectedCodes.mapNotNull { byCode[it] }.sortedForCompare(sortMode)
    }
    val bestRate = compareRates.maxByOrNull { it.change24h }
    val weakestRate = compareRates.minByOrNull { it.change24h }
    val averageAbsMove = if (compareRates.isEmpty()) 0.0 else compareRates.sumOf { kotlin.math.abs(it.change24h) } / compareRates.size
    val momentumSpread = if (bestRate != null && weakestRate != null) bestRate.change24h - weakestRate.change24h else 0.0
    val cryptoCount = compareRates.count { it.kind == CurrencyKind.Crypto }
    if (showCurrencyPicker) {
        CurrencyListPickerSheet(
            title = ui("Edit comparison"),
            lockedSubtitle = ui("Pro unlocks more comparison currencies"),
            currencies = availableRates.filterNot { it.code == liveState.baseCurrency },
            selectedCodes = selectedCodes,
            limit = access.compareLimit,
            isPremium = subscriptionState.isPremium,
            onDismiss = { showCurrencyPicker = false },
            onOpenPaywall = {
                showCurrencyPicker = false
                onOpenPaywall()
            },
            onApply = { codes ->
                showCurrencyPicker = false
                onCurrencyCodesChange(codes)
            },
        )
    }
    ScreenScaffold {
        ScreenHeader(
            ui("Compare"),
            sub = "${liveState.baseCurrency} ${ui("BASE")}",
            subtitle = "${compareRates.size} ${ui("currencies")} · ${ui(sortMode.label).lowercase()} · ${compactRuntimeLabel(liveState.updatedLabel)}",
        )
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing comparison board"),
                rows = 5,
                modifier = Modifier.testTag("compare_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing market cards"),
                rows = 6,
                modifier = Modifier.testTag("compare_tiles_loading_skeleton"),
            )
        } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompareSortMode.entries.forEach { mode ->
                Pill(
                    ui(mode.label),
                    variant = if (mode == sortMode) PillVariant.Accent else PillVariant.Ghost,
                    modifier = Modifier
                        .testTag("compare_sort_${mode.name}")
                        .clickable { sortMode = mode },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                ui("STRONGEST"),
                bestRate?.code ?: "--",
                bestRate?.let { formatChange(it.change24h) } ?: ui("No data"),
                Modifier.weight(1f).height(76.dp),
            )
            MetricTile(
                ui("WEAKEST"),
                weakestRate?.code ?: "--",
                weakestRate?.let { formatChange(it.change24h) } ?: ui("No data"),
                Modifier.weight(1f).height(76.dp),
            )
        }
        BentoCard(Modifier.fillMaxWidth().testTag("compare_board"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Eyebrow(ui("COMPARE BOARD"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(ui("Average move"), formatPercentValue(averageAbsMove) + "%", ui(sortMode.label), Modifier.weight(1f).height(76.dp))
                    MetricTile(ui("Momentum spread"), formatPercentValue(momentumSpread) + "%", "${bestRate?.code ?: "--"} / ${weakestRate?.code ?: "--"}", Modifier.weight(1f).height(76.dp))
                }
                KeyValueRow(ui("Asset mix"), "${compareRates.size} ${ui("currencies")} · $cryptoCount ${ui("crypto")}")
            }
        }
        if (compareRates.isEmpty()) {
            EmptyDetailSection(
                title = ui("No comparison currencies"),
                subtitle = "${ui("The saved list is unavailable for")} ${liveState.baseCurrency}. ${ui("Edit the comparison set to choose active currencies.")}",
            )
        } else {
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
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton("≡  ${ui("Edit comparison")}", Modifier.weight(1f).testTag("compare_edit_button"), onClick = { showCurrencyPicker = true })
            GhostButton("↗  ${ui("Open strongest")}", Modifier.weight(1f).testTag("compare_open_strongest"), onClick = { bestRate?.let(onOpenDetail) })
        }
        if (!subscriptionState.isPremium) {
            ProUpsellCard(
                title = ui("Compare every tracked currency"),
                subtitle = "${ui("Free compares")} ${access.compareLimit}; ${ui("Pro unlocks the full board and advanced overlays.")}",
                onClick = onOpenPaywall,
            )
        }
        if (compareRates.isNotEmpty()) {
            BentoCard(modifier = Modifier.testTag("compare_overlay"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Eyebrow(ui("OVERLAY · 1M"))
                    OverlayChart(compareRates.take(4))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        compareRates.take(4).forEachIndexed { index, rate ->
                            LegendDot(rate.code, compareOverlayColor(index, rate.kind))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun CompareTile(rate: FxRate, baseCurrency: String, onOpenDetail: (FxRate) -> Unit, modifier: Modifier = Modifier) {
    BentoTile(
        modifier = modifier
            .testTag("compare_tile_${rate.code}")
            .clickable { onOpenDetail(rate) },
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
            Text("${ui("per 1")} $baseCurrency", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
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
    var tripDays by remember { mutableStateOf(3) }
    val budgetLocal = budgetBase * selectedRate.rate
    val dailyBudgetLocal = budgetLocal / tripDays.coerceAtLeast(1).toDouble()
    val cashBufferLocal = budgetLocal * destination.cashBufferPct
    val cardSpendLocal = (budgetLocal - cashBufferLocal).coerceAtLeast(0.0)
    val anchorPrice = destination.priceGuide.firstOrNull { item ->
        val label = item.label.lowercase()
        label.contains("meal") || label.contains("lunch") || label.contains("ramen") || label.contains("tacos") || label.contains("pub")
    } ?: destination.priceGuide.firstOrNull()
    val anchorPurchases = anchorPrice?.localAmount?.takeIf { it > 0.0 }?.let { budgetLocal / it } ?: 0.0
    val cheatAmounts = listOf(1, 5, 10, 20, 50, 100, 250, 500).take(access.travelerCheatSheetLimit.cap(8))
    val baseDefinition = liveState.allFiat.firstOrNull { it.code == liveState.baseCurrency }
        ?: SettingsBaseCurrencies.firstOrNull { it.code == liveState.baseCurrency }
    val baseRate = remember(liveState.baseCurrency, baseDefinition) {
        FxRate(
            code = liveState.baseCurrency,
            name = baseDefinition?.name ?: liveState.baseCurrency,
            glyph = baseDefinition?.glyph ?: "◆",
            rate = 1.0,
            change24h = 0.0,
            sparkline = listOf(1f, 1f, 1f),
        )
    }
    var budgetText by remember { mutableStateOf(if (budgetBase > 0.0) formatMoneyValue(budgetBase) else "") }
    var travelerScannedPriceText by remember(selectedRate.code) { mutableStateOf(destination.priceGuide.firstOrNull()?.localAmount?.let(::formatMoneyValue) ?: "25") }
    var travelerPriceHistory by remember(selectedRate.code) { mutableStateOf(emptyList<PriceScannerHistoryEntry>()) }
    var showDestinationPicker by remember { mutableStateOf(false) }
    if (showDestinationPicker) {
        CurrencyPickerSheet(
            title = ui("Choose destination"),
            subtitle = "${travelRates.size} ${ui("live currencies")} · ${liveState.baseCurrency} ${ui("base")}",
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
            BackNavButton(label = ui("More"), onClick = onBack)
        }
        ScreenHeader(
            ui("Traveler"),
            sub = "${destination.city.uppercase()} · ${selectedRate.code}",
            subtitle = if (liveState.isLive) "${ui("Live")} ${liveState.baseCurrency} ${ui("rates")} · ${compactRuntimeLabel(liveState.updatedLabel)}" else "${ui("Offline snapshot")} · ${liveState.baseCurrency} ${ui("base")}",
        )
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing traveler rates"),
                rows = 5,
                modifier = Modifier.testTag("traveler_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing destination tools"),
                rows = 6,
                modifier = Modifier.testTag("traveler_destination_loading_skeleton"),
            )
        } else {
        BentoCard(Modifier.fillMaxWidth().height(156.dp).testTag("traveler_hero"), padding = 14.dp) {
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
                Text("${formatChange(selectedRate.change24h)} ${ui("today")} · ${ui("mid-market")}", style = FxTheme.typography.captionMono, color = if (selectedRate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
        }

        SectionLabel(ui("DESTINATION"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleDestinations.chunked(4).forEach { rowRates ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowRates.forEach { rate ->
                            val item = travelerDestination(rate.code)
                            Pill(
                                "${item.flag} ${rate.code}",
                                variant = if (rate.code == selectedRate.code) PillVariant.Accent else PillVariant.Ghost,
                                modifier = Modifier
                                    .testTag("traveler_destination_${rate.code}")
                                    .clickable { onCurrencySelected(rate.code) },
                            )
                        }
                    }
                }
                SettingChoiceRow(
                    title = ui("More destinations"),
                    subtitle = if (access.canUseAdvancedTraveler) {
                        "${ui("Search")} ${travelRates.size} ${ui("supported live currencies")}"
                    } else {
                        "${ui("Free shows")} ${visibleDestinations.size}; ${ui("Pro unlocks every supported currency")}"
                    },
                    selected = false,
                    actionLabel = ui("more +"),
                    modifier = Modifier.testTag("traveler_more_destinations"),
                    onClick = {
                        if (access.canUseAdvancedTraveler) showDestinationPicker = true else onOpenPaywall()
                    },
                )
                if (!access.canUseAdvancedTraveler && travelRates.size > visibleDestinations.size) {
                    Text(ui("Free keeps the destination picker focused on the most common travel currencies."), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                }
            }
        }

        SectionLabel(ui("TRIP BUDGET"))
        BentoCard(Modifier.testTag("traveler_budget_card"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("${ui("BUDGET")} · ${liveState.baseCurrency}")
                        BasicTextField(
                            value = budgetText,
                            onValueChange = { raw ->
                                val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                                budgetText = next
                                onBudgetChange(parseAmountInput(next))
                            },
                            singleLine = true,
                            textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("traveler_budget_input"),
                        )
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Eyebrow(ui("LOCAL"))
                        Text("${destination.symbol}${formatMoneyValue(budgetLocal)}", style = FxTheme.typography.numberL, color = FxTheme.colors.text)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().testTag("traveler_days_control"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(ui("Trip days"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                        Text(ui("Daily budget = local budget / days"), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill("-", modifier = Modifier.testTag("traveler_days_decrease").clickable { tripDays = (tripDays - 1).coerceAtLeast(1) })
                        Pill("$tripDays ${ui("days")}", variant = PillVariant.Accent, modifier = Modifier.testTag("traveler_days_value"))
                        Pill("+", modifier = Modifier.testTag("traveler_days_increase").clickable { tripDays = (tripDays + 1).coerceAtMost(30) })
                    }
                }
                KeyValueRow(ui("Local budget"), "${destination.symbol}${formatMoneyValue(budgetLocal)}")
                KeyValueRow(ui("Daily budget"), "${destination.symbol}${formatMoneyValue(dailyBudgetLocal)} · $tripDays ${ui("days")}")
                KeyValueRow(ui("Cash buffer"), "${destination.symbol}${formatMoneyValue(cashBufferLocal)} · ${(destination.cashBufferPct * 100).toInt()}% ${ui("of local budget")}")
            }
        }

        SectionLabel(ui("SPEND PLAN"))
        BentoCard(Modifier.testTag("traveler_spend_plan"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricTile(ui("Daily budget"), "${destination.symbol}${formatMoneyValue(dailyBudgetLocal)}", "$tripDays ${ui("days")}", Modifier.weight(1f).height(76.dp))
                    MetricTile(ui("Card spend"), "${destination.symbol}${formatMoneyValue(cardSpendLocal)}", ui("after cash buffer"), Modifier.weight(1f).height(76.dp))
                }
                KeyValueRow(ui("Cash buffer"), "${destination.symbol}${formatMoneyValue(cashBufferLocal)} · ${(destination.cashBufferPct * 100).toInt()}%")
                if (anchorPrice != null) {
                    KeyValueRow(ui("Local meals"), "${formatMoneyValue(anchorPurchases)}x ${ui(anchorPrice.label)} · ${ui("guide estimate")}")
                }
                KeyValueRow(ui("Formula"), "${ui("Cash buffer")} = ${ui("Local budget")} x ${(destination.cashBufferPct * 100).toInt()}%")
            }
        }

        SectionLabel(ui("COST TEMPLATES"), right = destination.city)
        TravelerCostTemplatesCard(
            destination = destination,
            dailyBudgetLocal = dailyBudgetLocal,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )

        SectionLabel(ui("Scan traveler price"), right = if (subscriptionState.isPremium) ui("OCR beta") else ui("Preview"))
        Box(Modifier.testTag("traveler_price_scanner")) {
            PriceScannerCard(
                sourceRate = baseRate,
                targetRate = selectedRate,
                scannedPriceText = travelerScannedPriceText,
                localMarketRate = selectedRate.rate,
                isPremium = subscriptionState.isPremium,
                onScannedPriceChange = { travelerScannedPriceText = sanitizeAmountInput(it) },
                onScannedPriceDetected = { amount, detectedCurrency ->
                    travelerScannedPriceText = sanitizeAmountInput(amount)
                    detectedCurrency?.uppercase()?.takeIf { it == selectedRate.code }?.let {
                        Observability.event("traveler_price_scanned", mapOf("currency" to it))
                    }
                    travelerPriceHistory = (listOf(
                        PriceScannerHistoryEntry(
                            amountText = sanitizeAmountInput(amount),
                            targetCode = selectedRate.code,
                            sourceCode = liveState.baseCurrency,
                            liveSourceCost = liveSourceCostFor(parseAmountInput(amount), selectedRate),
                            hiddenCost = 0.0,
                        ),
                    ) + travelerPriceHistory).take(3)
                },
                history = travelerPriceHistory,
                onOpenPaywall = onOpenPaywall,
            )
        }

        SectionLabel(ui("OFFLINE PACK"), right = if (liveState.isLive) ui("Live") else ui("CACHED"))
        BentoCard(Modifier.testTag("traveler_offline_pack"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KeyValueRow(
                    if (liveState.isLive) ui("Saved snapshot") else ui("Ready from cached rates"),
                    compactRuntimeLabel(liveState.updatedLabel),
                )
                KeyValueRow(
                    ui("Rate snapshot"),
                    "1 ${liveState.baseCurrency} = ${formatRate(selectedRate.rate)} ${selectedRate.code}",
                )
                KeyValueRow(
                    ui("ATM cash target"),
                    "${destination.symbol}${formatMoneyValue(cashBufferLocal)} · ${(destination.cashBufferPct * 100).toInt()}%",
                )
                KeyValueRow(ui("DCC rule"), ui("Decline conversion; pay in local currency."))
                KeyValueRow(ui("Receipt check"), ui("Compare terminal rate against this mid-market snapshot."))
            }
        }

        SectionLabel(ui("CHEAT SHEET"))
        BentoCard(Modifier.testTag("traveler_cheat_sheet"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cheatAmounts.forEach { amount ->
                    Box(Modifier.testTag("traveler_cheat_$amount")) {
                        KeyValueRow("$amount ${liveState.baseCurrency}", "${destination.symbol}${formatMoneyValue(amount * selectedRate.rate)}")
                    }
                }
            }
        }
        if (!access.canUseAdvancedTraveler) {
            ProUpsellCard(
                title = ui("Unlock full traveler mode"),
                subtitle = ui("Pro adds complete cheat sheets, offline context and more local money tips."),
                onClick = onOpenPaywall,
            )
        }
        SectionLabel(ui("LOCAL ETIQUETTE"))
        Row(Modifier.testTag("traveler_local_etiquette"), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(ui("TIPPING"), destination.tipping, ui(destination.tippingNote), Modifier.weight(1f))
            MetricTile(ui("TAX"), ui(destination.tax), ui(destination.taxNote), Modifier.weight(1f))
        }
        BentoTile(Modifier.fillMaxWidth().testTag("traveler_payment_rails")) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Eyebrow(ui("CARDS ACCEPTED"))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        destination.paymentRails.forEach { Pill(it) }
                    }
                }
                Text(ui(destination.cashNote), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
        }
        SectionLabel(ui("LOCAL PRICE GUIDE"), right = ui("Estimates"))
        BentoCard(Modifier.testTag("traveler_price_guide"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                destination.priceGuide.forEach { item ->
                    val basePrice = item.localAmount / selectedRate.rate
                    KeyValueRow(ui(item.label), "${destination.symbol}${formatMoneyValue(item.localAmount)} · ${liveState.baseCurrency} ${formatMoneyValue(basePrice)}")
                }
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

@Composable
private fun TravelerCostTemplatesCard(
    destination: TravelerDestination,
    dailyBudgetLocal: Double,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val templates = remember(destination.code, dailyBudgetLocal) {
        travelerCostTemplates(destination, dailyBudgetLocal)
    }
    BentoCard(Modifier.testTag("traveler_cost_templates"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            templates.take(if (isPremium) templates.size else 2).forEachIndexed { index, template ->
                KeyValueRow(
                    ui(template.label),
                    "${destination.symbol}${formatMoneyValue(template.dailyTotal)}",
                    "${ui("daily template")} · ${template.detail}",
                    modifier = Modifier.testTag("traveler_cost_template_$index"),
                )
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks premium city templates."),
                    modifier = Modifier.fillMaxWidth().testTag("traveler_cost_template_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

private data class TravelerCostTemplate(
    val label: String,
    val dailyTotal: Double,
    val detail: String,
)

private fun travelerCostTemplates(
    destination: TravelerDestination,
    dailyBudgetLocal: Double,
): List<TravelerCostTemplate> {
    val meal = destination.priceGuide.firstOrNull { it.label.contains("meal", ignoreCase = true) || it.label.contains("ramen", ignoreCase = true) || it.label.contains("tacos", ignoreCase = true) }
        ?: destination.priceGuide.firstOrNull()
    val transit = destination.priceGuide.firstOrNull { it.label.contains("metro", ignoreCase = true) || it.label.contains("transit", ignoreCase = true) || it.label.contains("tube", ignoreCase = true) }
    val coffee = destination.priceGuide.firstOrNull { it.label.contains("coffee", ignoreCase = true) }
    val mealAmount = meal?.localAmount ?: dailyBudgetLocal * 0.35
    val transitAmount = transit?.localAmount ?: dailyBudgetLocal * 0.10
    val coffeeAmount = coffee?.localAmount ?: dailyBudgetLocal * 0.05
    return listOf(
        TravelerCostTemplate("Backpacker", mealAmount + transitAmount + coffeeAmount, "${meal?.label ?: "Meal"} + ${transit?.label ?: "Transit"}"),
        TravelerCostTemplate("Comfort", mealAmount * 2.0 + transitAmount * 2.0 + coffeeAmount, "${meal?.label ?: "Meal"} x2 + ${transit?.label ?: "Transit"}"),
        TravelerCostTemplate("Business", mealAmount * 3.0 + transitAmount * 2.0 + coffeeAmount * 2.0, "${meal?.label ?: "Meal"} x3 + taxiTemplateLabel(destination)"),
    )
}

private fun taxiTemplateLabel(destination: TravelerDestination): String =
    destination.priceGuide.firstOrNull { it.label.contains("taxi", ignoreCase = true) }?.label ?: "Taxi"

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
        ScreenHeader(ui("More"), sub = ui("TOOLS"), subtitle = ui("Travel, preferences and account"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MoreRow(
                    icon = MoreFeatureIcon.Traveler,
                    title = ui("Traveler"),
                    subtitle = ui("Local cheat sheets and offline rates"),
                    onClick = onOpenTraveler,
                )
                MoreRow(
                    icon = MoreFeatureIcon.News,
                    title = ui("News"),
                    subtitle = ui("Market stream and sentiment"),
                    onClick = onOpenNews,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Alerts,
                    title = ui("Alerts"),
                    subtitle = "$alertsCount ${ui("active")} · ${ui("price targets and breakouts")}",
                    onClick = onOpenAlerts,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Watchlist,
                    title = ui("Watchlist"),
                    subtitle = "$watchlistCount ${ui("currencies")} · ${ui("custom tracking")}",
                    onClick = onOpenWatchlist,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Settings,
                    title = ui("Settings"),
                    subtitle = ui("Theme mode, base currency and version"),
                    onClick = onOpenSettings,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Pro,
                    title = if (subscriptionState.isPremium) ui("FX/ Pro active") else ui("Upgrade to Pro"),
                    subtitle = subscriptionState.localizedProStatusLabel(),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
fun AlertsScreen(
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    showTestAction: Boolean = PlatformConfig.isDebug,
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = { NotificationPermissionStatus.requestIfNeeded() },
    onCreateAlert: (FxRate) -> Unit = {},
    onCreateManualAlert: (FxRate, AlertDirection, Double, AlertKind) -> Unit = { _, _, _, _ -> },
    onResumeAlert: (String) -> Unit = {},
    onToggleAlert: (String) -> Unit = {},
    onDeleteAlert: (String) -> Unit = {},
    onMarkAlertTriggered: (String) -> Unit = {},
    onTestAlert: (PriceAlert) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val canCreate = canCreateAlert(subscriptionState, alertsState.alerts.size)
	    val limitLabel = if (access.hasUnlimitedAlerts) ui("Unlimited") else "${alertsState.alerts.size}/${access.alertLimit}"
    val alertRates = remember(
        liveState.baseCurrency,
        liveState.favorites,
        liveState.compare,
        liveState.converter,
        liveState.allFiat,
        liveState.crypto,
        subscriptionState.isPremium,
    ) {
        liveState.alertRates(subscriptionState.isPremium)
    }
    val currentRatesByCode = remember(liveState.baseCurrency, alertRates) {
        alertRates.associateBy { it.code }
    }
    val digestDriver = remember(alertRates) {
        alertRates.maxByOrNull { kotlin.math.abs(it.change24h) }
    }
    val triggeredAlerts = remember(alertsState.alerts) {
        alertsState.alerts
            .filter { it.lastTriggeredAtMillis != null }
            .sortedByDescending { it.lastTriggeredAtMillis }
            .take(4)
    }
    val smartSuggestions = remember(liveState.baseCurrency, alertRates, subscriptionState.isPremium) {
        smartAlertSuggestions(alertRates, subscriptionState.isPremium)
    }
    var selectedRateCode by remember(liveState.baseCurrency) { mutableStateOf(alertRates.firstOrNull()?.code ?: "EUR") }
    val selectedRate = alertRates.firstOrNull { it.code == selectedRateCode } ?: alertRates.firstOrNull() ?: FavoriteRates.first()
    val visibleAlertRates = remember(alertRates, selectedRate.code, subscriptionState.isPremium) {
        compactCurrencyChoices(alertRates, selectedRate.code, if (subscriptionState.isPremium) 8 else 4)
    }
    var showAlertCurrencyPicker by remember { mutableStateOf(false) }
    var selectedKind by remember { mutableStateOf(AlertKind.Target) }
    var selectedDirection by remember { mutableStateOf(AlertDirection.Above) }
    var digestCadence by remember { mutableStateOf("Daily") }
    var targetText by remember(selectedRate.code, selectedDirection, selectedKind) {
        mutableStateOf(defaultAlertInput(selectedRate, selectedDirection, selectedKind))
    }
    val targetValue = parseAmountInput(targetText)
    val selectedDailyChange = selectedRate.change24h
    val matchingCustomAlert = alertsState.alerts.findMatchingAlert(
        baseCurrency = liveState.baseCurrency,
        quote = selectedRate.code,
        target = targetValue,
        direction = selectedDirection,
        kind = selectedKind,
    )
    val canCreateOrUpdate = canCreate || matchingCustomAlert != null
	    var customAlertFeedback by remember { mutableStateOf<String?>(null) }
    var customAlertError by remember { mutableStateOf<String?>(null) }
	    val existingAlertReactivatedCopy = ui("Existing alert reactivated")
	    val alertCreatedCopy = ui("alert created")
    val invalidTargetCopy = ui("Enter a target above 0")
	    LaunchedEffect(liveState.baseCurrency, selectedRate.code, selectedDirection, selectedKind, targetText) {
	        customAlertFeedback = null
        customAlertError = null
	    }
    if (showAlertCurrencyPicker) {
        CurrencyPickerSheet(
            title = ui("Choose alert pair"),
            subtitle = "${alertRates.size} ${ui("currencies")} · ${liveState.baseCurrency} ${ui("base")}",
            currencies = alertRates,
            selectedCode = selectedRate.code,
            onDismiss = { showAlertCurrencyPicker = false },
            onSelect = { code ->
                showAlertCurrencyPicker = false
                selectedRateCode = code
                alertRates.firstOrNull { it.code == code }?.let { rate ->
                    targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
                }
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
	            BackNavButton(label = ui("More"), onClick = onBack)
        }
	        ScreenHeader(ui("Alerts"), sub = ui("PRICE TARGETS"), subtitle = "$limitLabel ${ui("alerts")} · ${liveState.baseCurrency} ${ui("base")}")

        BentoCard(Modifier.fillMaxWidth().heightIn(min = 144.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
	                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
	                    Pill("${alertsState.activeCount} ${ui("active")}", variant = if (alertsState.activeCount > 0) PillVariant.Up else PillVariant.Ghost)
                }
	                Text(ui("Watch breakouts without watching charts."), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
	                    ui("Android checks every 15 min when online. iOS saves alerts now; push delivery is next."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing smart alerts"),
                rows = 5,
                modifier = Modifier.testTag("alerts_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing alert builder"),
                rows = 6,
                modifier = Modifier.testTag("alerts_builder_loading_skeleton"),
            )
        } else {
        SectionLabel(ui("NOTIFICATION DIGEST"), right = if (subscriptionState.isPremium) "FX/ PRO" else ui("Preview"))
        AlertDigestCard(
            activeCount = alertsState.activeCount,
            triggeredCount = triggeredAlerts.size,
            driver = digestDriver,
            cadence = digestCadence,
            isPremium = subscriptionState.isPremium,
            onCadenceSelected = { cadence ->
                if (cadence == "Weekly" && !subscriptionState.isPremium) {
                    onOpenPaywall()
                } else {
                    digestCadence = cadence
                    onRequestNotificationPermission()
                }
            },
            onOpenPaywall = onOpenPaywall,
        )
        AlertActionCenterCard(
            alerts = alertsState.alerts,
            currentRatesByCode = currentRatesByCode,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )

        SectionLabel(ui("SMART ALERTS"), right = if (subscriptionState.isPremium) "FX/ PRO" else ui("Preview"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (smartSuggestions.isEmpty()) {
                    Text(
                        ui("No smart alert signals yet"),
                        modifier = Modifier.fillMaxWidth().testTag("alert_smart_empty").padding(horizontal = 12.dp, vertical = 10.dp),
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textFaint,
                    )
                } else {
                    smartSuggestions.forEach { suggestion ->
                        val existingSmartAlert = alertsState.alerts.findMatchingAlert(
                            baseCurrency = liveState.baseCurrency,
                            quote = suggestion.rate.code,
                            target = suggestion.target,
                            direction = suggestion.direction,
                            kind = suggestion.kind,
                        )
                        val canUseSuggestion = existingSmartAlert != null || canCreate
                        SmartAlertRow(
                            baseCurrency = liveState.baseCurrency,
                            suggestion = suggestion,
                            state = when {
                                existingSmartAlert?.enabled == true -> QuickAlertState.Active
                                existingSmartAlert != null -> QuickAlertState.Paused
                                canCreate -> QuickAlertState.Create
                                else -> QuickAlertState.Locked
                            },
                            enabled = canUseSuggestion,
                            onCreate = {
                                onRequestNotificationPermission()
                                if (existingSmartAlert != null) {
                                    onResumeAlert(existingSmartAlert.id)
                                } else {
                                    onCreateManualAlert(suggestion.rate, suggestion.direction, suggestion.target, suggestion.kind)
                                }
                            },
                            onLocked = onOpenPaywall,
                        )
                    }
                }
            }
        }

        SectionLabel(ui("ALERT TEMPLATES"))
        BentoCard(Modifier.testTag("alert_templates"), padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                alertTemplates.forEachIndexed { index, template ->
                    SettingChoiceRow(
                        title = ui(template.title),
                        subtitle = ui(template.subtitle),
                        selected = selectedKind == template.kind && selectedDirection == template.direction,
                        actionLabel = ui("Apply"),
                        modifier = Modifier.testTag("alert_template_$index"),
                        onClick = {
                            selectedKind = template.kind
                            selectedDirection = template.direction
                            targetText = template.targetText(selectedRate)
                            Observability.event(
                                "alert_template_selected",
                                mapOf("template" to template.id, "currency" to selectedRate.code),
                            )
                        },
                    )
                }
            }
        }

        SectionLabel(ui("CUSTOM ALERT"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
	                Eyebrow("${liveState.baseCurrency} ${ui("PAIR")}")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleAlertRates.chunked(2).forEach { rowRates ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowRates.forEach { rate ->
	                                AlertCurrencyChoice(
	                                    rate = rate,
	                                    selected = rate.code == selectedRate.code,
	                                    modifier = Modifier.clickable {
	                                        selectedRateCode = rate.code
	                                        targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
	                                    }.weight(1f),
	                                )
                            }
                            if (rowRates.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                GhostButton(
                    text = "≡  ${ui("Choose alert pair")}",
                    modifier = Modifier.fillMaxWidth().testTag("alert_choose_pair"),
                    onClick = { showAlertCurrencyPicker = true },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertKind.entries.forEach { kind ->
                        Pill(
	                            text = ui(kind.label),
	                            variant = if (kind == selectedKind) PillVariant.Accent else PillVariant.Ghost,
	                            modifier = Modifier
                                    .testTag("alert_kind_${kind.name}")
                                    .clickable {
	                                selectedKind = kind
	                                targetText = defaultAlertInput(selectedRate, selectedDirection, kind)
	                            },
	                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertDirection.entries.forEach { direction ->
                        Pill(
		                            text = ui(direction.label(selectedKind)),
	                            variant = if (direction == selectedDirection) PillVariant.Accent else PillVariant.Ghost,
	                            modifier = Modifier
                                    .testTag("alert_direction_${direction.name}")
                                    .clickable {
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
	                            modifier = Modifier
                                    .testTag("alert_preset_${preset.label}")
                                    .clickable {
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
		                    label = if (selectedKind == AlertKind.Target) ui("Target rate") else ui("Daily move %"),
	                )
	                PrimaryButton(
                    text = when {
	                        matchingCustomAlert?.enabled == true -> ui("Keep existing alert active")
	                        matchingCustomAlert != null -> ui("Reactivate existing alert")
	                        canCreate -> "${ui("Create")} ${ui(selectedDirection.label(selectedKind)).lowercase()} ${ui("alert")}"
		                        else -> ui("Unlock custom alerts")
	                    },
                    modifier = Modifier.fillMaxWidth().testTag("alert_create_button"),
	                    onClick = {
	                        if (!canCreateOrUpdate) {
	                            onOpenPaywall()
	                        } else if (targetValue > 0.0) {
	                            onRequestNotificationPermission()
	                            onCreateManualAlert(selectedRate, selectedDirection, targetValue, selectedKind)
	                            customAlertFeedback = if (matchingCustomAlert != null) {
		                                "$existingAlertReactivatedCopy ${liveState.baseCurrency}/${selectedRate.code}."
		                            } else {
			                                "${liveState.baseCurrency}/${selectedRate.code} $alertCreatedCopy."
		                            }
                                customAlertError = null
	                        } else {
                                customAlertError = invalidTargetCopy
	                        }
	                    },
	                )
                customAlertError?.let { error ->
                    Text(
                        error,
                        modifier = Modifier.testTag("alert_target_error"),
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.down,
                    )
                }
	                customAlertFeedback?.let { feedback ->
	                    Text(
	                        feedback,
                            modifier = Modifier.testTag("alert_feedback"),
	                        style = FxTheme.typography.captionMono,
	                        color = FxTheme.colors.accent,
                    )
                }
                Text(
                    localizedAlertSummaryLine(selectedKind, selectedRate, selectedDirection, targetValue, selectedDailyChange),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }

	        SectionLabel(ui("QUICK CREATE"))
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
                            onRequestNotificationPermission()
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
	                title = ui("Create unlimited alerts"),
	                subtitle = "${ui("Free includes")} ${access.alertLimit}; ${ui("Pro unlocks every pair, range and breakout alert.")}",
                onClick = onOpenPaywall,
            )
        }

	        SectionLabel(ui("ACTIVE ALERTS"))
        if (alertsState.alerts.isEmpty()) {
            BentoCard(padding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
	                    Eyebrow(ui("NO ALERTS YET"))
	                    Text(ui("Create one from a favorite currency or from any detail screen."), style = FxTheme.typography.body, color = FxTheme.colors.textDim)
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
                        showTestAction = showTestAction,
                        onTest = {
                            onRequestNotificationPermission()
                            onTestAlert(it)
                            onMarkAlertTriggered(it.id)
                        },
                    )
                }
            }
        }
        SectionLabel(ui("TRIGGER HISTORY"))
        AlertTriggerHistoryCard(
            alerts = triggeredAlerts,
            currentRatesByCode = currentRatesByCode,
            baseCurrency = liveState.baseCurrency,
        )
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
    onSetHoldingCost: (String, Double) -> Unit = { _, _ -> },
    onRecordTransaction: (String, PortfolioTransactionType, Double, Double) -> Unit = { _, _, _, _ -> },
    onImportPortfolioCsv: (String) -> PortfolioCsvImportResult = { watchlistState.watchlist.importPortfolioCsv(it) },
    onOpenDetail: (FxRate) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val allRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.crypto, subscriptionState.isPremium) {
        liveState.portfolioRates(subscriptionState.isPremium)
    }
	    val limitLabel = if (access.hasUnlimitedWatchlistCurrencies) ui("Unlimited") else "${watchlistState.watchlist.codes.size}/${access.watchlistCurrencyLimit}"
	    val holdings = remember(liveState.baseCurrency, allRates, watchlistState.watchlist) {
	        watchlistState.watchlist.codes.mapNotNull { code ->
	            val rate = allRates.firstOrNull { it.code == code } ?: return@mapNotNull null
	            PortfolioHolding(
	                rate = rate,
	                amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0,
	                averageCostBase = watchlistState.watchlist.holdingCosts[rate.code] ?: 0.0,
	            )
	        }
	    }
    val valuedHoldings = holdings.filter { it.amount > 0.0 }
    val portfolioValue = valuedHoldings.sumOf { it.baseValue }
    val portfolioDailyChange = valuedHoldings.sumOf { it.dailyChangeInBase }
    val portfolioCostBasis = valuedHoldings.sumOf { it.costBasisBase }
    val portfolioUnrealizedPnl = valuedHoldings.sumOf { it.unrealizedPnlBase }
    val portfolioRealizedPnl = watchlistState.watchlist.transactions.sumOf { it.realizedPnlBase }
    val fiatValue = valuedHoldings.filter { it.rate.kind == CurrencyKind.Fiat }.sumOf { it.baseValue }
    val cryptoValue = valuedHoldings.filter { it.rate.kind == CurrencyKind.Crypto }.sumOf { it.baseValue }
    val largestHolding = valuedHoldings.maxByOrNull { it.baseValue }
    val largestDailyDriver = valuedHoldings.maxByOrNull { kotlin.math.abs(it.dailyChangeInBase) }
    val portfolioSeries = remember(valuedHoldings) { valuedHoldings.portfolioValueSeries() }
    val nonZeroHoldings = holdings.count { it.amount > 0.0 }
    ScreenScaffold {
        if (onBack != null) {
	            BackNavButton(label = ui("More"), onClick = onBack)
        }
	        ScreenHeader(ui("Watchlist"), sub = ui("CUSTOM TRACKING"), subtitle = "$limitLabel ${ui("currencies")} · ${liveState.baseCurrency} ${ui("base")}")

        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing watchlist"),
                rows = 5,
                modifier = Modifier.testTag("watchlist_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing portfolio rows"),
                rows = 6,
                modifier = Modifier.testTag("watchlist_holdings_loading_skeleton"),
            )
        } else {
        BentoCard(Modifier.fillMaxWidth().heightIn(min = 148.dp).testTag("watchlist_summary"), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
	                    Pill("${holdings.size} ${ui("tracked")}", variant = if (holdings.isNotEmpty()) PillVariant.Accent else PillVariant.Ghost)
                }
	                Text(ui("Tracked currencies"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                if (nonZeroHoldings == 0) {
	                    BigValueText("${holdings.size}", " ${ui("tracked")}")
                    Text(
	                        ui("Add amounts below to value your portfolio."),
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    BigValueText("${liveState.baseCurrency} ${formatMoneyValue(portfolioValue)}")
                    Text(
	                        "${formatPortfolioChange(portfolioDailyChange, liveState.baseCurrency)} ${ui("today")} · $nonZeroHoldings ${ui("holdings valued")}",
                        style = FxTheme.typography.caption,
                        color = if (portfolioDailyChange >= 0.0) FxTheme.colors.up else FxTheme.colors.down,
                    )
                    if (portfolioSeries.size >= 2) {
                        SparkLine(
                            portfolioSeries,
                            Modifier.fillMaxWidth().height(38.dp).testTag("watchlist_portfolio_chart"),
                            color = if ((portfolioSeries.last() - portfolioSeries.first()) >= 0f) FxTheme.colors.up else FxTheme.colors.down,
                            showLastDot = true,
                        )
                    }
                }
            }
        }

        SectionLabel(ui("WATCHLIST GROUPS"))
        WatchlistGroupsCard(
            codes = watchlistState.watchlist.codes,
            allRates = allRates,
            holdings = holdings,
        )

        if (subscriptionState.isPremium && nonZeroHoldings > 0) {
            SectionLabel(ui("PORTFOLIO INSIGHTS"))
            BentoCard(Modifier.testTag("watchlist_portfolio_insights"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    KeyValueRow(
                        ui("Unrealized P&L"),
                        formatSignedMoney(portfolioUnrealizedPnl, liveState.baseCurrency),
                        portfolioPnlPercentLabel(portfolioUnrealizedPnl, portfolioCostBasis),
                        modifier = Modifier.testTag("watchlist_unrealized_pnl"),
                    )
                    KeyValueRow(
                        ui("Realized P&L"),
                        formatSignedMoney(portfolioRealizedPnl, liveState.baseCurrency),
                        "${watchlistState.watchlist.transactions.size} ${ui("transactions")}",
                        modifier = Modifier.testTag("watchlist_realized_pnl"),
                    )
                    KeyValueRow(
                        ui("Total P&L"),
                        formatSignedMoney(portfolioUnrealizedPnl + portfolioRealizedPnl, liveState.baseCurrency),
                        ui("realized + unrealized"),
                        modifier = Modifier.testTag("watchlist_total_pnl"),
                    )
                    KeyValueRow(
                        ui("Cost basis"),
                        "${liveState.baseCurrency} ${formatMoneyValue(portfolioCostBasis)}",
                        ui("average cost per asset"),
                        modifier = Modifier.testTag("watchlist_cost_basis"),
                    )
                    KeyValueRow(
                        ui("Allocation"),
                        "${ui("Fiat")} ${allocationLabel(fiatValue, portfolioValue)} · ${ui("Crypto")} ${allocationLabel(cryptoValue, portfolioValue)}",
                        modifier = Modifier.testTag("watchlist_allocation"),
                    )
                    KeyValueRow(
                        ui("Largest position"),
                        largestHolding?.rate?.code ?: "—",
                        largestHolding?.weightLabel(portfolioValue),
                        modifier = Modifier.testTag("watchlist_largest_position"),
                    )
                    KeyValueRow(
                        ui("Concentration"),
                        largestHolding?.weightLabel(portfolioValue) ?: "0%",
                        largestHolding?.let { "${it.rate.code} · ${ui("largest holding weight")}" },
                        modifier = Modifier.testTag("watchlist_concentration"),
                    )
                    KeyValueRow(
                        ui("Scenario -5%"),
                        formatSignedMoney(portfolioValue * -0.05, liveState.baseCurrency),
                        ui("estimated portfolio shock"),
                        modifier = Modifier.testTag("watchlist_scenario_down_5"),
                    )
                    KeyValueRow(
                        ui("Daily digest"),
                        largestDailyDriver?.dailyChangeLabel(liveState.baseCurrency) ?: "${liveState.baseCurrency} 0.00",
                        largestDailyDriver?.let { "${it.rate.code} · ${ui("largest daily driver")}" },
                        modifier = Modifier.testTag("watchlist_daily_digest"),
                    )
                    KeyValueRow(
                        ui("Action plan"),
                        portfolioActionPlan(largestHolding, largestDailyDriver, portfolioDailyChange),
                        ui("Review concentration before adding new exposure."),
                        modifier = Modifier.testTag("watchlist_action_plan"),
                    )
                    if (portfolioSeries.size >= 2) {
                        KeyValueRow(
                            ui("Chart range"),
                            formatPortfolioSignedPercent(portfolioSeries.changePercent()),
                            ui("estimated from tracked assets"),
                            modifier = Modifier.testTag("watchlist_chart_range"),
                        )
                    }
                }
            }
        }

        if (subscriptionState.isPremium && holdings.isNotEmpty()) {
            SectionLabel(ui("TRANSACTION HISTORY"))
            PortfolioTransactionsCard(
                baseCurrency = liveState.baseCurrency,
                holdings = holdings,
                transactions = watchlistState.watchlist.transactions,
                onRecordTransaction = onRecordTransaction,
            )
        }

        if (subscriptionState.isPremium) {
            SectionLabel(ui("IMPORT / EXPORT"))
            PortfolioImportExportCard(
                watchlist = watchlistState.watchlist,
                onImportPortfolioCsv = onImportPortfolioCsv,
            )
        }

	        SectionLabel(ui("PORTFOLIO HOLDINGS"))
        if (holdings.isEmpty()) {
            BentoCard(padding = 14.dp) {
	                Text(ui("Choose currencies below to start tracking."), style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        } else {
            if (nonZeroHoldings == 0) {
                BentoCard(padding = 12.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
	                        Eyebrow(ui("HOW IT WORKS"))
                        Text(
	                            ui("Watchlist follows rates. Portfolio value appears after you enter how much you hold."),
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
                            canEditCostBasis = subscriptionState.isPremium,
                            onAmountChange = { amount -> onSetHolding(holding.rate.code, amount) },
                            onCostChange = { averageCost -> onSetHoldingCost(holding.rate.code, averageCost) },
                            onOpenDetail = { onOpenDetail(holding.rate) },
                        )
                    }
                }
            }
        }

	        SectionLabel(ui("ADD OR REMOVE"))
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
	                title = ui("Track unlimited currencies"),
	                subtitle = "${ui("Free includes")} ${access.watchlistCurrencyLimit}; ${ui("Pro unlocks bigger watchlists across rates, alerts and portfolio tracking.")}",
                onClick = onOpenPaywall,
            )
        }
    }
    }
}

@Composable
private fun WatchlistGroupsCard(
    codes: List<String>,
    allRates: List<FxRate>,
    holdings: List<PortfolioHolding>,
) {
    val ratesByCode = remember(allRates) { allRates.associateBy { it.code } }
    val trackedRates = remember(codes, ratesByCode) { codes.mapNotNull { ratesByCode[it] } }
    val valuedHoldingCodes = remember(holdings) { holdings.filter { it.amount > 0.0 }.map { it.rate.code }.toSet() }
    val dynamicGroups = remember(trackedRates, valuedHoldingCodes) {
        watchlistDynamicGroups(trackedRates, valuedHoldingCodes)
    }
    BentoCard(Modifier.testTag("watchlist_groups"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (dynamicGroups.isEmpty()) {
                KeyValueRow(
                    ui("No tracked groups yet"),
                    ui("Add currencies to build groups from your actual watchlist."),
                    ui("No preset buckets"),
                    modifier = Modifier.testTag("watchlist_group_empty"),
                )
            } else {
                dynamicGroups.forEach { group ->
                    KeyValueRow(
                        ui(group.label),
                        group.codes.joinToString(" · "),
                        ui(group.subtitle),
                        modifier = Modifier.testTag("watchlist_group_${group.id}"),
                    )
                }
            }
        }
    }
}

private data class WatchlistDynamicGroup(
    val id: String,
    val label: String,
    val subtitle: String,
    val codes: List<String>,
)

private fun watchlistDynamicGroups(
    trackedRates: List<FxRate>,
    valuedHoldingCodes: Set<String>,
): List<WatchlistDynamicGroup> {
    val fiatCodes = trackedRates.filter { it.kind == CurrencyKind.Fiat }.map { it.code }
    val cryptoCodes = trackedRates.filter { it.kind == CurrencyKind.Crypto }.map { it.code }
    val valuedCodes = trackedRates.filter { it.code in valuedHoldingCodes }.map { it.code }
    val trackingOnlyCodes = trackedRates.filter { it.code !in valuedHoldingCodes }.map { it.code }
    val moverCodes = trackedRates
        .sortedByDescending { kotlin.math.abs(it.change24h) }
        .take(4)
        .filter { kotlin.math.abs(it.change24h) > 0.0 }
        .map { it.code }

    return listOfNotNull(
        valuedCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("valued", "Valued holdings", "Currencies with entered amounts", it)
        },
        trackingOnlyCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("tracking_only", "Tracking only", "No amount entered yet", it)
        },
        fiatCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("fiat", "Fiat exposure", "Tracked government currencies", it)
        },
        cryptoCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("crypto", "Crypto exposure", "Tracked crypto assets and stablecoins", it)
        },
        moverCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("movers", "Largest movers", "Sorted by absolute 24h move", it)
        },
    )
}

@Composable
private fun PortfolioTransactionsCard(
    baseCurrency: String,
    holdings: List<PortfolioHolding>,
    transactions: List<PortfolioTransaction>,
    onRecordTransaction: (String, PortfolioTransactionType, Double, Double) -> Unit,
) {
    val codes = remember(holdings) { holdings.map { it.rate.code }.distinct() }
    var selectedCode by remember(codes) { mutableStateOf(codes.firstOrNull().orEmpty()) }
    if (selectedCode !in codes) selectedCode = codes.firstOrNull().orEmpty()
    var selectedType by remember { mutableStateOf(PortfolioTransactionType.Buy) }
    var amountText by remember(selectedCode) { mutableStateOf("") }
    var priceText by remember(selectedCode) { mutableStateOf("") }
    val amount = parseAmountInput(amountText)
    val price = parseAmountInput(priceText)
    val latestTransactions = remember(transactions) { transactions.sortedByDescending { it.createdAtMillis }.take(5) }

    BentoCard(Modifier.testTag("watchlist_transactions"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                codes.forEach { code ->
                    TransactionChip(
                        label = code,
                        selected = selectedCode == code,
                        modifier = Modifier.testTag("watchlist_transaction_asset_$code"),
                        onClick = { selectedCode = code },
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionChip(
                    label = ui("Buy"),
                    selected = selectedType == PortfolioTransactionType.Buy,
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_buy"),
                    onClick = { selectedType = PortfolioTransactionType.Buy },
                )
                TransactionChip(
                    label = ui("Sell"),
                    selected = selectedType == PortfolioTransactionType.Sell,
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_sell"),
                    onClick = { selectedType = PortfolioTransactionType.Sell },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionInputField(
                    value = amountText,
                    placeholder = ui("amount"),
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_amount"),
                    onValueChange = { amountText = it },
                )
                TransactionInputField(
                    value = priceText,
                    placeholder = "${ui("price")} $baseCurrency",
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_price"),
                    onValueChange = { priceText = it },
                )
            }
            PrimaryButton(
                text = ui("Record transaction"),
                modifier = Modifier.fillMaxWidth().testTag("watchlist_transaction_record"),
                onClick = {
                    if (selectedCode.isNotBlank() && amount > 0.0 && price > 0.0) {
                        onRecordTransaction(selectedCode, selectedType, amount, price)
                        amountText = ""
                        priceText = ""
                    }
                },
            )
            if (latestTransactions.isEmpty()) {
                Text(
                    ui("No transactions yet"),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    modifier = Modifier.testTag("watchlist_no_transactions"),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    latestTransactions.forEach { transaction ->
                        PortfolioTransactionRow(baseCurrency = baseCurrency, transaction = transaction)
                    }
                }
            }
        }
        }
    }

@Composable
private fun PortfolioImportExportCard(
    watchlist: com.fxalways.app.data.Watchlist,
    onImportPortfolioCsv: (String) -> PortfolioCsvImportResult,
) {
    val clipboardManager = LocalClipboardManager.current
    var importText by remember { mutableStateOf("") }
    var importFeedback by remember { mutableStateOf<String?>(null) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }
    val exportCsv = remember(watchlist) { watchlist.toPortfolioCsv() }
    val exportHoldingCount = watchlist.holdings.count { it.value > 0.0 || (watchlist.holdingCosts[it.key] ?: 0.0) > 0.0 }
    val exportTransactionCount = watchlist.transactions.size
    val holdingsCopy = ui("holdings")
    val transactionsCopy = ui("transactions")
    val skippedCopy = ui("skipped")
    val noValidRowsCopy = ui("No valid portfolio rows found")
    val exportCopiedCopy = ui("Export copied")

    BentoCard(Modifier.testTag("watchlist_import_export"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                ui("Portfolio CSV backup"),
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                ui("Copy a manual backup or paste one back in to restore portfolio data."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            KeyValueRow(
                ui("Export CSV"),
                "$exportHoldingCount $holdingsCopy · $exportTransactionCount $transactionsCopy",
                ui("manual backup"),
                modifier = Modifier.testTag("watchlist_export_summary"),
            )
            PrimaryButton(
                text = ui("Copy export CSV"),
                modifier = Modifier.fillMaxWidth().testTag("watchlist_copy_export_csv"),
                onClick = {
                    clipboardManager.setText(AnnotatedString(exportCsv))
                    exportFeedback = exportCopiedCopy
                },
            )
            exportFeedback?.let {
                Text(
                    it,
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier.testTag("watchlist_export_feedback"),
                )
            }
            CsvTextBox(
                value = exportCsv,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.testTag("watchlist_export_csv"),
            )
            KeyValueRow(
                ui("Import CSV"),
                ui("Paste rows below"),
                ui("merge safe"),
                modifier = Modifier.testTag("watchlist_import_summary"),
            )
            CsvTextBox(
                value = importText,
                onValueChange = { importText = it.take(4_000) },
                readOnly = false,
                placeholder = ui("Paste portfolio CSV"),
                modifier = Modifier.testTag("watchlist_import_csv"),
            )
            PrimaryButton(
                text = ui("Import CSV"),
                modifier = Modifier.fillMaxWidth().testTag("watchlist_import_csv_button"),
                onClick = {
                    val result = onImportPortfolioCsv(importText)
                    importFeedback = if (result.hasImports) {
                        "${result.importedHoldings} $holdingsCopy · ${result.importedTransactions} $transactionsCopy · ${result.skippedRows} $skippedCopy"
                    } else {
                        noValidRowsCopy
                    }
                    if (result.hasImports) importText = ""
                },
            )
            importFeedback?.let {
                Text(
                    it,
                    style = FxTheme.typography.captionMono,
                    color = if (it == noValidRowsCopy) FxTheme.colors.down else FxTheme.colors.accent,
                    modifier = Modifier.testTag("watchlist_import_feedback"),
                )
            }
        }
    }
}

@Composable
private fun CsvTextBox(
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        minLines = 4,
        maxLines = 6,
        textStyle = FxTheme.typography.captionMono.copy(color = FxTheme.colors.text),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        decorationBox = { innerTextField ->
            if (value.isBlank() && placeholder.isNotBlank()) {
                Text(placeholder, style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            }
            innerTextField()
        },
    )
}

@Composable
private fun TransactionChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(1.dp, if (selected) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = FxTheme.typography.captionMono, color = if (selected) FxTheme.colors.accent else FxTheme.colors.textDim)
    }
}

@Composable
private fun TransactionInputField(value: String, placeholder: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)) },
        singleLine = true,
        textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        modifier = modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        decorationBox = { innerTextField ->
            if (value.isBlank()) {
                Text(
                    placeholder,
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textGhost,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun PortfolioTransactionRow(baseCurrency: String, transaction: PortfolioTransaction) {
    val pnl = transaction.realizedPnlBase
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("watchlist_transaction_${transaction.id}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${ui(transaction.type.name)} ${transaction.code} ${formatMoneyValue(transaction.amount)}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                "${baseCurrency} ${formatMoneyValue(transaction.priceBase)} · ${localizedShortAgeLabel(transaction.createdAtMillis)}",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
            )
        }
        Text(
            if (transaction.type == PortfolioTransactionType.Sell) formatSignedMoney(pnl, baseCurrency) else ui("cost basis"),
            style = FxTheme.typography.captionMono,
            color = when {
                transaction.type == PortfolioTransactionType.Buy -> FxTheme.colors.textDim
                pnl >= 0.0 -> FxTheme.colors.up
                else -> FxTheme.colors.down
            },
        )
    }
}

@Composable
private fun PortfolioHoldingRow(
    baseCurrency: String,
    holding: PortfolioHolding,
    portfolioValue: Double,
    canEditCostBasis: Boolean,
    onAmountChange: (Double) -> Unit,
    onCostChange: (Double) -> Unit,
    onOpenDetail: () -> Unit,
) {
    val rate = holding.rate
    val amount = holding.amount
    val focusManager = LocalFocusManager.current
    var amountText by remember(rate.code) { mutableStateOf(if (amount > 0.0) formatRate(amount) else "") }
    var amountFocused by remember(rate.code) { mutableStateOf(false) }
    var costText by remember(rate.code) { mutableStateOf(if (holding.averageCostBase > 0.0) formatRate(holding.averageCostBase) else "") }
    var costFocused by remember(rate.code) { mutableStateOf(false) }
    LaunchedEffect(amount, amountFocused) {
        if (!amountFocused) amountText = if (amount > 0.0) formatRate(amount) else ""
    }
    LaunchedEffect(holding.averageCostBase, costFocused) {
        if (!costFocused) costText = if (holding.averageCostBase > 0.0) formatRate(holding.averageCostBase) else ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("watchlist_holding_${rate.code}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp, modifier = Modifier.clickable(onClick = onOpenDetail))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "${rate.code} ${ui("holding")}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
                modifier = Modifier
                    .testTag("watchlist_detail_${rate.code}")
                    .clickable(onClick = onOpenDetail),
            )
            val holdingSubtitle = if (amount <= 0.0) {
                "${ui("Tracking live rate")} ${formatRate(rate.rate)} · ${ui("enter amount held")}"
            } else if (canEditCostBasis && holding.hasCostBasis) {
                "${formatSignedMoney(holding.unrealizedPnlBase, baseCurrency)} ${ui("unrealized")} · ${holding.weightLabel(portfolioValue)} · ${holding.dailyChangeLabel(baseCurrency)}"
            } else {
                "$baseCurrency ${formatMoneyValue(holding.baseValue)} · ${holding.weightLabel(portfolioValue)} · ${holding.dailyChangeLabel(baseCurrency)}"
            }
            Text(
                holdingSubtitle,
                style = FxTheme.typography.captionMono,
                color = if (amount <= 0.0) {
                    FxTheme.colors.textFaint
                } else if (canEditCostBasis && holding.hasCostBasis) {
                    if (holding.unrealizedPnlBase >= 0.0) FxTheme.colors.up else FxTheme.colors.down
                } else if (holding.dailyChangeInBase >= 0.0) {
                    FxTheme.colors.up
                } else {
                    FxTheme.colors.down
                },
            )
        }
        Column(
            modifier = Modifier.width(112.dp),
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
                    .testTag("watchlist_amount_${rate.code}")
                    .clip(FxTheme.shapes.field)
                    .background(if (amountFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                    .border(1.dp, if (amountFocused) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .onFocusChanged { amountFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    if (amountText.isBlank()) {
                        Text(
	                            ui("amount"),
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
	                    ui("done"),
	                    style = FxTheme.typography.captionMono,
	                    color = FxTheme.colors.accent,
	                    modifier = Modifier
                            .testTag("watchlist_amount_done_${rate.code}")
                            .clickable { focusManager.clearFocus() },
	                )
            }
            if (canEditCostBasis) {
                BasicTextField(
                    value = costText,
                    onValueChange = { raw ->
                        val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                        costText = next
                        onCostChange(parseAmountInput(next))
                    },
                    singleLine = true,
                    textStyle = FxTheme.typography.captionMono.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watchlist_cost_${rate.code}")
                        .clip(FxTheme.shapes.field)
                        .background(if (costFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface1)
                        .border(1.dp, if (costFocused) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .onFocusChanged { costFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        if (costText.isBlank()) {
                            Text(
                                ui("avg cost"),
                                style = FxTheme.typography.captionMono,
                                color = FxTheme.colors.textGhost,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                            )
                        }
                        innerTextField()
                    },
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
            .testTag("watchlist_currency_${rate.code}")
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
	                if (amount > 0.0) "${formatRate(amount)} ${ui("held")} · ${localizedCurrencyName(rate.name)}" else localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
            )
        }
        Text(formatRate(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.textDim)
        Pill(
            text = when {
	                selected -> ui("tracked")
	                locked -> ui("pro")
	                else -> ui("add")
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
                modifier = Modifier.testTag("alert_target_input"),
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
private fun AlertCurrencyChoice(
    rate: FxRate,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .testTag("alert_currency_${rate.code}")
            .heightIn(min = 54.dp)
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(
                1.dp,
                if (selected) FxTheme.colors.accentLine else FxTheme.colors.border,
                FxTheme.shapes.field,
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 26.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = if (selected) FxTheme.colors.accent else FxTheme.colors.text)
            Text(
                localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Text("✓", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.accent)
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
            .testTag("alert_quick_${rate.code}")
            .clip(FxTheme.shapes.field)
            .clickable(onClick = if (enabled) onCreate else onLocked)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("$baseCurrency / ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
	            Text("${ui("Above")} ${formatRate(rate.rate * 1.01)} · ${ui("current")} ${formatRate(rate.rate)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        Pill(ui(state.label), variant = state.variant)
    }
}

@Composable
private fun SmartAlertRow(
    baseCurrency: String,
    suggestion: SmartAlertSuggestion,
    state: QuickAlertState,
    enabled: Boolean,
    onCreate: () -> Unit,
    onLocked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_smart_${suggestion.rate.code}")
            .clip(FxTheme.shapes.field)
            .clickable(onClick = if (enabled) onCreate else onLocked)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(suggestion.rate.glyph, suggestion.rate.kind, 30.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "$baseCurrency / ${suggestion.rate.code}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                ui(suggestion.title),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${ui(suggestion.direction.label(suggestion.kind))} ${formatRate(suggestion.target)} · ${ui(suggestion.subtitle)}",
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Pill(ui(state.label), variant = state.variant)
            Text(suggestion.strengthLabel, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun AlertCard(
    alert: PriceAlert,
    currentRate: Double?,
    currentChangePct: Double?,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    showTestAction: Boolean,
    onTest: (PriceAlert) -> Unit,
) {
    val isHit = alert.isHit(currentRate, currentChangePct)
    BentoCard(modifier = Modifier.testTag("alert_card_${alert.id}"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                FlagDot(if (alert.kind == AlertKind.Target) "◎" else "%", CurrencyKind.Fiat, 32.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
	                        "${ui(alert.direction.label(alert.kind))} ${alert.targetLabel()} · ${localizedAlertStatusLabel(alert, currentRate, currentChangePct)}",
                        style = FxTheme.typography.captionMono,
                        color = if (isHit) FxTheme.colors.up else FxTheme.colors.textFaint,
                    )
                }
	                Pill(if (alert.enabled) ui("on") else ui("paused"), variant = if (alert.enabled) PillVariant.Up else PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetricTile(
	                    if (alert.kind == AlertKind.Target) ui("CURRENT") else ui("24H MOVE"),
                    if (alert.kind == AlertKind.Target) currentRate?.let(::formatRate) ?: "--" else currentChangePct?.let(::formatSignedPercent) ?: "--",
                    localizedAlertDistanceLabel(alert, currentRate, currentChangePct),
                    Modifier.weight(1f).height(72.dp),
                )
                MetricTile(
	                    ui("LAST HIT"),
	                    alert.lastTriggeredAtMillis?.let { localizedShortAgeLabel(it) } ?: ui("Never"),
	                    if (alert.enabled) ui("monitoring") else ui("paused"),
                    Modifier.weight(1f).height(72.dp),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(
		                    if (alert.enabled) ui("pause") else ui("resume"),
	                    style = FxTheme.typography.captionMono,
	                    color = FxTheme.colors.textDim,
	                    modifier = Modifier
                            .testTag("alert_toggle_${alert.id}")
                            .clickable { onToggle(alert.id) },
	                )
                Spacer(Modifier.width(14.dp))
                if (showTestAction) {
                    Text(
		                        ui("test"),
	                        style = FxTheme.typography.captionMono,
	                        color = FxTheme.colors.accent,
	                        modifier = Modifier
                                .testTag("alert_test_${alert.id}")
                                .clickable { onTest(alert) },
	                    )
	                    Spacer(Modifier.width(14.dp))
                }
	                Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textFaint, modifier = Modifier
                        .testTag("alert_delete_${alert.id}")
                        .clickable { onDelete(alert.id) })
            }
        }
    }
}

@Composable
private fun AlertActionCenterCard(
    alerts: List<PriceAlert>,
    currentRatesByCode: Map<String, FxRate>,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val decisionAlert = alerts
        .sortedWith(compareByDescending<PriceAlert> { it.lastTriggeredAtMillis ?: 0L }.thenByDescending { it.enabled })
        .firstOrNull()
    val currentRate = decisionAlert?.let { currentRatesByCode[it.quote]?.rate }
    val currentChange = decisionAlert?.let { currentRatesByCode[it.quote]?.change24h }
    val isHit = decisionAlert?.isHit(currentRate, currentChange) == true
    BentoCard(Modifier.testTag("alert_action_center"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("ACTION CENTER"), color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Preview"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            if (decisionAlert == null) {
                KeyValueRow(
                    ui("No alert has fired yet"),
                    ui("Set next alert"),
                    ui("Create alerts first; fired alerts will become concrete decisions here."),
                    modifier = Modifier.testTag("alert_action_empty"),
                )
            } else {
                KeyValueRow(
                    if (isHit) ui("Alert fired") else ui("Recommended next step"),
                    "${decisionAlert.base}/${decisionAlert.quote} · ${ui(decisionAlert.direction.label(decisionAlert.kind))} ${decisionAlert.targetLabel()}",
                    if (isHit) ui("Review provider cost before moving money.") else localizedAlertDistanceLabel(decisionAlert, currentRate, currentChange),
                    modifier = Modifier.testTag("alert_action_decision"),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostButton(
                        text = ui("Convert now"),
                        modifier = Modifier.weight(1f).testTag("alert_action_convert"),
                        onClick = {
                            if (isPremium) {
                                Observability.event("alert_action_convert", mapOf("quote" to decisionAlert.quote))
                            } else {
                                onOpenPaywall()
                            }
                        },
                    )
                    GhostButton(
                        text = ui("Set next alert"),
                        modifier = Modifier.weight(1f).testTag("alert_action_next"),
                        onClick = { Observability.event("alert_action_next_alert", mapOf("quote" to decisionAlert.quote)) },
                    )
                }
                GhostButton(
                    text = ui("Share decision"),
                    modifier = Modifier.fillMaxWidth().testTag("alert_action_share"),
                    onClick = { Observability.event("alert_action_share", mapOf("quote" to decisionAlert.quote, "hit" to isHit.toString())) },
                )
            }
        }
    }
}

@Composable
private fun AlertDigestCard(
    activeCount: Int,
    triggeredCount: Int,
    driver: FxRate?,
    cadence: String,
    isPremium: Boolean,
    onCadenceSelected: (String) -> Unit,
    onOpenPaywall: () -> Unit,
) {
    BentoCard(Modifier.testTag("alert_digest"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily", "Weekly").forEach { option ->
                    val locked = option == "Weekly" && !isPremium
                    Pill(
                        text = if (locked) "${ui(option)} · Pro" else ui(option),
                        variant = if (cadence == option && !locked) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .testTag("alert_digest_${option.lowercase()}")
                            .clickable { onCadenceSelected(option) },
                    )
                }
            }
            KeyValueRow(
                ui("Digest includes"),
                ui("Active alerts and recent hits"),
                "${activeCount} ${ui("active")} · $triggeredCount ${ui("Last hit").lowercase()}",
                modifier = Modifier.testTag("alert_digest_includes"),
            )
            KeyValueRow(
                if (cadence == "Weekly") ui("Weekly digest") else ui("Daily digest"),
                driver?.let { "${it.code} ${formatSignedPercent(it.change24h)}" } ?: "--",
                if (cadence == "Weekly") ui("Weekly digest groups active alerts, hits and largest watched move.") else ui("Daily digest highlights active alerts, latest hits and today's largest move."),
                modifier = Modifier.testTag("alert_digest_driver"),
            )
            KeyValueRow(
                ui("Next reminder"),
                if (cadence == "Weekly") ui("Monday morning") else ui("Tomorrow morning"),
                driver?.let { "${ui("Watch")} ${it.code} · ${formatSignedPercent(it.change24h)}" } ?: ui("Add alerts to personalize digest."),
                modifier = Modifier.testTag("alert_digest_next_reminder"),
            )
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks weekly digest."),
                    modifier = Modifier.fillMaxWidth().testTag("alert_digest_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
private fun AlertTriggerHistoryCard(
    alerts: List<PriceAlert>,
    currentRatesByCode: Map<String, FxRate>,
    baseCurrency: String,
) {
    BentoCard(Modifier.testTag("alert_trigger_history"), padding = 8.dp) {
        if (alerts.isEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(ui("No alert hits yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    ui("Triggered alerts will appear here after Android checks rates."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                alerts.forEach { alert ->
                    AlertHistoryRow(
                        alert = alert,
                        currentRate = currentRatesByCode[alert.quote]?.rate.takeIf { alert.base == baseCurrency },
                        currentChangePct = currentRatesByCode[alert.quote]?.change24h.takeIf { alert.base == baseCurrency },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryRow(
    alert: PriceAlert,
    currentRate: Double?,
    currentChangePct: Double?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("alert_history_${alert.id}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(if (alert.kind == AlertKind.Target) "◎" else "%", CurrencyKind.Fiat, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(
                "${ui("Alert triggered")} · ${ui(alert.direction.label(alert.kind))} ${alert.targetLabel()}",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                localizedAlertDistanceLabel(alert, currentRate, currentChangePct),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(ui("Last hit"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Text(
                alert.lastTriggeredAtMillis?.let { localizedShortAgeLabel(it) } ?: ui("Never"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.text,
            )
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
    var selectedTopic by remember { mutableStateOf("ALL") }
    val normalizedQuery = query.trim()
    val topicOptions = remember(newsState.stories) {
        (listOf("ALL") + newsState.stories.flatMap { story -> story.topics }.filter { it.isNotBlank() })
            .distinct()
            .take(8)
    }
    val filteredStories = remember(newsState.stories, query, newsState.selectedCurrency, selectedTopic) {
        newsState.stories.filter { story ->
            val matchesQuery = normalizedQuery.isBlank() ||
                story.title.contains(normalizedQuery, ignoreCase = true) ||
                story.summary.contains(normalizedQuery, ignoreCase = true) ||
                story.tag.contains(normalizedQuery, ignoreCase = true) ||
                story.topics.any { it.contains(normalizedQuery, ignoreCase = true) } ||
                story.moves.any { it.first.contains(normalizedQuery, ignoreCase = true) }
            val matchesCurrency = newsState.selectedCurrency.isBlank() ||
                newsState.selectedCurrency == "USD" ||
                story.moves.any { it.first == newsState.selectedCurrency } ||
                story.tag == newsState.selectedCurrency
            val matchesTopic = selectedTopic == "ALL" || story.topics.any { it == selectedTopic }
            matchesQuery && matchesCurrency && matchesTopic
        }
    }
    val visibleStories = filteredStories.take(access.newsStoryLimit.cap(filteredStories.size))
    val regionOptions = listOf("US", "AU", "GB", "EU", "BR", "MX", "JP")
    val currencyOptions = (newsState.trackedCurrencies + listOf("USD", "EUR", "GBP", "JPY", "AUD", "BTC")).distinct()
    val emptyCopy = newsEmptyCopy(
        hasBackendStories = newsState.stories.isNotEmpty(),
        hasQuery = normalizedQuery.isNotBlank(),
        topic = selectedTopic,
    )
    ScreenScaffold {
        ScreenHeader(
	            ui("News"),
	            sub = if (access.canUseAdvancedNews) ui("MARKET STREAM") else ui("MARKET PREVIEW"),
	            subtitle = if (newsState.isLoading && newsState.stories.isEmpty()) {
                "${ui("Loading market stream")} · ${newsState.selectedCurrency} ${ui("focus")}"
            } else {
                "${compactProviderLabel(newsState.provider)} · ${newsState.region} · ${newsState.selectedCurrency} ${ui("focus")} · ${compactRuntimeLabel(newsState.refreshedLabel)}"
            },
            right = {
                Text(
                    if (newsState.isLoading) "…" else "↻",
                    style = FxTheme.typography.numberL,
                    color = if (newsState.isLoading) FxTheme.colors.accent else FxTheme.colors.textDim,
                    modifier = Modifier.clickable(enabled = !newsState.isLoading, onClick = onRefresh),
                )
            },
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
	                    Eyebrow(ui("SENTIMENT"))
                    if (newsState.isLoading) {
	                        Eyebrow(ui("REFRESHING"), color = FxTheme.colors.accent)
                    }
                }
                if (newsState.isLoading && newsState.stories.isEmpty()) {
                    InlineSkeletonRows(rows = 4, modifier = Modifier.testTag("news_sentiment_loading"))
                } else {
                    SentimentBar(newsState.bullish, newsState.neutral, newsState.bearish)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
	                        LegendDot("${ui("BULLISH")} ${newsState.bullish}%", FxTheme.colors.up)
	                        LegendDot("${ui("NEUTRAL")} ${newsState.neutral}%", FxTheme.colors.textGhost)
	                        LegendDot("${ui("BEARISH")} ${newsState.bearish}%", FxTheme.colors.down)
                    }
	                    KeyValueRow(ui("Feed"), "${newsState.language.uppercase()} · ${newsState.trackedCurrencies.joinToString(", ")}")
	                    KeyValueRow(ui("Updated"), "${compactProviderLabel(newsState.provider)} · ${compactRuntimeLabel(newsState.refreshedLabel)}")
                }
            }
        }
        if (newsState.isLoading && newsState.stories.isEmpty()) {
            LoadingSkeletonCard(
                title = ui("Loading market stream"),
                rows = 4,
                modifier = Modifier.testTag("news_loading_skeleton"),
            )
        }
        BentoCard(padding = 10.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NewsSearchField(query = query, onQueryChange = { query = it })
                NewsFilterRow(
	                    label = ui("REGION"),
                    options = regionOptions,
                    selected = newsState.region,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { region ->
                        if (access.canUseAdvancedNews) onRegionSelected(region) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
	                    label = ui("CURRENCY"),
                    options = currencyOptions,
                    selected = newsState.selectedCurrency,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { code ->
                        if (access.canUseAdvancedNews) onCurrencySelected(code) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
	                    label = ui("TOPIC"),
                    options = topicOptions,
                    selected = selectedTopic,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { topic ->
                        if (access.canUseAdvancedNews || topic == selectedTopic) {
                            selectedTopic = topic
                        } else {
                            onOpenPaywall()
                        }
                    },
                )
            }
        }
	        SectionLabel("${ui("RECENT LINES")} · ${filteredStories.size}")
        if (newsState.errorMessage != null && newsState.stories.isEmpty()) {
            BentoCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ui("Market stream unavailable"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(userFriendlyNetworkError(newsState.errorMessage), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                }
            }
        } else if (visibleStories.isEmpty() && !(newsState.isLoading && newsState.stories.isEmpty())) {
            BentoCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
	                    Text(ui(emptyCopy.first), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
	                    Text(ui(emptyCopy.second), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                    if (newsState.isLoading) {
	                        Text(ui("Refreshing market stream…"), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
                    }
                }
            }
        }
        visibleStories.forEach { story ->
            StoryCard(story, onClick = { onOpenStory(story) })
        }
        if ((!access.canUseAdvancedNews || visibleStories.size < filteredStories.size) && !(newsState.isLoading && newsState.stories.isEmpty())) {
            ProUpsellCard(
	                title = ui("Personalize the market stream"),
                subtitle = if (visibleStories.size < filteredStories.size) {
	                    "${ui("Showing")} ${visibleStories.size}/${filteredStories.size} ${ui("stories")}. ${ui("Pro unlocks the full regional stream.")}"
                } else {
	                    ui("Pro unlocks more stories and filters by region, currencies and topics.")
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
	        title = ui("Market update"),
	        summary = ui("Latest currency market context."),
        moves = emptyList(),
        source = "FX Always",
        sourceUrl = "",
    )
    val impactColor = if (selected.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent
    ScreenScaffold {
	        BackNavButton(label = ui("News"), onClick = onBack)
        ScreenHeader(
	            ui("News detail"),
            sub = "${selected.tag} · ${selected.impact}",
	            subtitle = "${selected.source.ifBlank { ui("Market source") }} · ${selected.age}",
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
	        SectionLabel(ui("MARKET MOVES"))
        BentoCard(padding = 12.dp) {
            if (selected.moves.isEmpty()) {
	                Text(ui("No direct currency move was detected for this story."), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    selected.moves.forEach { (code, change) ->
                        KeyValueRow(code, formatChange(change))
                    }
                }
            }
        }
	        SectionLabel(ui("SOURCE"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
	                KeyValueRow(ui("Publisher"), selected.source.ifBlank { ui("Market source") })
	                KeyValueRow(ui("Published"), selected.age)
                if (selected.sourceUrl.isNotBlank()) {
	                    GhostButton(ui("Open original source"), onClick = { onOpenUrl(selected.sourceUrl) })
                } else {
	                    Text(ui("This item is generated from the fallback market brief, so there is no external article link."), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    userProfile: UserProfile = UserProfile.Traveler,
    availableBaseCurrencies: List<FxRate> = SettingsBaseCurrencies,
    backupState: UserBackupState,
    backupSyncing: Boolean,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
    providerPreferenceCodes: List<String> = emptyList(),
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRestorePurchase: () -> Unit,
    onSyncNow: () -> Unit,
    onLinkGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDevPremiumChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onProviderPreferenceCodesChange: (List<String>) -> Unit = {},
    onUserProfileChange: (UserProfile) -> Unit = {},
) {
    val copy = settingsCopy(appLanguage)
    val activeLanguage = SupportedLanguages.firstOrNull { it.code == appLanguage }
        ?: SupportedLanguages.first()
    val access = subscriptionState.featureAccess()
    val fullBaseCurrencies = availableBaseCurrencies.ifEmpty { SettingsBaseCurrencies }
    val canUseAllBaseCurrencies = access.hasUnlimitedBaseCurrencies
    val baseCurrencyLimit = if (canUseAllBaseCurrencies) 12 else access.baseCurrencyLimit.cap(fullBaseCurrencies.size)
    val baseCurrencies = remember(fullBaseCurrencies, baseCurrency, baseCurrencyLimit) {
        compactCurrencyChoices(fullBaseCurrencies, baseCurrency, baseCurrencyLimit)
    }
    var showBaseCurrencyPicker by remember { mutableStateOf(false) }
    var linkIdentityPending by remember { mutableStateOf(false) }
    LaunchedEffect(backupSyncing, backupState.isAnonymous) {
        if (!backupSyncing || !backupState.isAnonymous) {
            linkIdentityPending = false
        }
    }
    if (showBaseCurrencyPicker) {
        CurrencyPickerSheet(
	            title = ui("Choose base currency"),
	            subtitle = "${fullBaseCurrencies.size} ${ui("supported live currencies")}",
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
            BackNavButton(label = copy.more, onClick = onBack)
        }
        ScreenHeader(copy.title, sub = copy.sub, subtitle = "${copy.activeLanguage}: ${activeLanguage.label} · ${copy.deviceLanguage}: ${DeviceLocale.language.uppercase()}")

        SectionLabel(copy.backup)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AccountBackupCard(
                    backupState = backupState,
                    lastSyncedAtMillis = lastSyncedAtMillis,
                    backupSyncing = backupSyncing,
                    modifier = Modifier.testTag("settings_backup_card"),
                    onClick = onSyncNow,
                )
                SettingChoiceRow(
                    title = copy.syncNow,
                    subtitle = copy.syncNowSubtitle,
                    selected = false,
                    actionLabel = if (backupSyncing) copy.syncing else copy.sync,
                    modifier = Modifier.testTag("settings_sync_now"),
                    enabled = !backupSyncing,
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
                        title = "${copy.signInWith} $providerLabel",
                        subtitle = if (linkIdentityPending) copy.signInProgressSubtitle else "${copy.signInSubtitle} $deviceLabel",
                        selected = false,
                        actionLabel = if (linkIdentityPending) copy.connecting else copy.connect,
                        modifier = Modifier.testTag("settings_link_account"),
                        enabled = !backupSyncing && !linkIdentityPending,
                        isLoading = linkIdentityPending,
                        onClick = {
                            linkIdentityPending = true
                            onLinkGoogle()
                        },
                    )
                } else {
                    SettingChoiceRow(
                        title = copy.signOut,
                        subtitle = copy.signOutSubtitle,
                        selected = false,
                        actionLabel = copy.signOutAction,
                        modifier = Modifier.testTag("settings_sign_out"),
                        onClick = onSignOut,
                    )
                }
            }
        }

        SectionLabel(copy.subscription)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = if (subscriptionState.isPremium) ui("FX/ Pro active") else ui("FX/ Free"),
                    subtitle = subscriptionState.statusMessage?.let { localizedSubscriptionMessage(it) } ?: subscriptionState.localizedProStatusLabel(),
                    selected = subscriptionState.isPremium,
                    actionLabel = if (subscriptionState.isPremium) copy.view else copy.upgrade,
                    modifier = Modifier.testTag("settings_subscription"),
                    onClick = onOpenPaywall,
                )
                SettingChoiceRow(
                    title = copy.restorePurchase,
                    subtitle = copy.restorePurchaseSubtitle,
                    selected = false,
                    actionLabel = copy.restore,
                    modifier = Modifier.testTag("settings_restore_purchase"),
                    onClick = onRestorePurchase,
                )
                SettingChoiceRow(
                    title = copy.manageSubscription,
                    subtitle = copy.manageSubscriptionSubtitle,
                    selected = false,
                    actionLabel = copy.open,
                    modifier = Modifier.testTag("settings_manage_subscription"),
                    onClick = { onOpenUrl(subscriptionManagementUrl()) },
                )
            }
        }

        SectionLabel(ui("Provider preferences"))
        ProviderPreferencesCard(
            baseCurrency = baseCurrency,
            selectedProviderCodes = normalizeProviderPreferenceCodes(providerPreferenceCodes, baseCurrency),
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
            onProviderPreferenceCodesChange = onProviderPreferenceCodesChange,
        )

        SectionLabel(copy.notifications)
        BentoCard(padding = 8.dp) {
            SettingChoiceRow(
                title = copy.priceAlertNotifications,
                subtitle = ui(NotificationPermissionStatus.subtitle),
                selected = false,
                actionLabel = ui(NotificationPermissionStatus.actionLabel),
                modifier = Modifier.testTag("settings_notifications"),
                onClick = {},
            )
        }

        SectionLabel(ui("WIDGET SETUP"))
        WidgetQuickSetupCard(
            baseCurrency = baseCurrency,
            availableCurrencies = fullBaseCurrencies,
        )

        if (PlatformConfig.isDebug) {
            SectionLabel(ui("RELEASE READINESS"))
            ReleaseReadinessCard(
                appLanguage = appLanguage,
                baseCurrency = baseCurrency,
                backupState = backupState,
                lastSyncedAtMillis = lastSyncedAtMillis,
                subscriptionState = subscriptionState,
            )

            SectionLabel(ui("INTERNAL TEST PLAN"))
            InternalTestPlanCard(
                appLanguage = appLanguage,
                baseCurrency = baseCurrency,
                subscriptionState = subscriptionState,
            )

            SectionLabel(ui("STORE LISTING KIT"))
            StoreListingKitCard(
                appLanguage = appLanguage,
                subscriptionState = subscriptionState,
            )
        }

        SectionLabel(ui("Profile"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                UserProfile.entries.forEach { profile ->
                    val profileCopy = profile.copy()
                    SettingChoiceRow(
                        title = ui(profileCopy.label),
                        subtitle = ui(profileCopy.subtitle),
                        selected = userProfile == profile,
                        modifier = Modifier.testTag("settings_profile_${profile.name}"),
                        onClick = { onUserProfileChange(profile) },
                    )
                }
            }
        }

        SectionLabel(copy.themeMode)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ThemeMode.entries.forEach { mode ->
                    SettingChoiceRow(
	                        title = ui(mode.label),
	                        subtitle = ui(mode.subtitle),
                        selected = themeMode == mode,
                        modifier = Modifier.testTag("settings_theme_${mode.name}"),
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
        }

        SectionLabel(copy.language)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = copy.activeLanguage,
                    subtitle = "${activeLanguage.label} · ${copy.languageApplied}",
                    selected = true,
                    actionLabel = appLanguage.uppercase(),
                    onClick = {},
                )
                SupportedLanguages.forEach { language ->
                    SettingChoiceRow(
                        title = language.label,
                        subtitle = if (language.code == DeviceLocale.language) copy.deviceLanguage else language.code.uppercase(),
                        selected = appLanguage == language.code,
                        modifier = Modifier.testTag("settings_language_${language.code}"),
                        onClick = { onLanguageChange(language.code) },
                    )
                }
            }
        }

        SectionLabel(copy.baseCurrency)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                baseCurrencies.forEach { currency ->
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = localizedCurrencyName(currency.name),
                        selected = baseCurrency == currency.code,
                        modifier = Modifier.testTag("settings_base_${currency.code}"),
                        onClick = { onBaseCurrencyChange(currency.code) },
                    )
                }
                SettingChoiceRow(
                    title = copy.moreCurrencies,
                    subtitle = if (canUseAllBaseCurrencies) {
                        "${copy.search} ${fullBaseCurrencies.size}"
                    } else {
                        "${copy.freeIncludes} ${baseCurrencies.size}; Pro ${copy.unlocks} ${fullBaseCurrencies.size}"
                    },
                    selected = false,
	                    actionLabel = ui("more +"),
                    modifier = Modifier.testTag("settings_more_base_currencies"),
                    onClick = {
                        if (canUseAllBaseCurrencies) showBaseCurrencyPicker = true else onOpenPaywall()
                    },
                )
            }
        }
        if (!canUseAllBaseCurrencies && baseCurrencies.size < fullBaseCurrencies.size) {
            ProUpsellCard(
                title = copy.unlockAllBaseCurrencies,
                subtitle = "${copy.freeIncludes} ${baseCurrencies.size}; Pro ${copy.unlocks} ${fullBaseCurrencies.size} ${copy.supportedBaseCurrencies}.",
                onClick = onOpenPaywall,
            )
        }

        if (PlatformConfig.isDebug) {
	            SectionLabel(ui("DEV"))
            BentoCard(padding = 8.dp) {
                SettingChoiceRow(
	                    title = "${ui("Simulate")} ${if (subscriptionState.isPremium) ui("Free") else ui("Pro")}",
	                    subtitle = ui("Debug-only local gate override"),
                    selected = subscriptionState.isPremium,
	                    actionLabel = if (subscriptionState.isPremium) ui("set free") else ui("set pro"),
                    modifier = Modifier.testTag("settings_dev_premium"),
                    onClick = { onDevPremiumChange(!subscriptionState.isPremium) },
                )
            }
        }

        SectionLabel(copy.legal)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = copy.privacyPolicy,
                    subtitle = copy.privacyPolicySubtitle,
                    selected = false,
                    actionLabel = copy.open,
                    modifier = Modifier.testTag("settings_privacy_policy"),
                    onClick = { onOpenUrl(privacyPolicyUrl(appLanguage)) },
                )
                SettingChoiceRow(
                    title = copy.termsOfUse,
                    subtitle = copy.termsOfUseSubtitle,
                    selected = false,
                    actionLabel = copy.open,
                    modifier = Modifier.testTag("settings_terms_of_use"),
                    onClick = { onOpenUrl(termsOfUseUrl(appLanguage)) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
	            "${ui("Version")} ${PlatformConfig.versionName}",
            style = FxTheme.typography.captionMono,
            color = FxTheme.colors.textFaint,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StoreListingKitCard(
    appLanguage: String,
    subscriptionState: SubscriptionState,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(appLanguage, subscriptionState.isPremium) { mutableStateOf(false) }
    val planLabel = if (subscriptionState.isPremium) "Pro" else "Free"
    val listingTitle = "FX Always"
    val shortDescription = ui("Live currency converter, alerts, travel tools and portfolio tracking.")
    val keywords = ui("currency converter, exchange rates, travel money, rate alerts")
    val disclaimer = ui("Rates are indicative and may differ from provider, card or cash exchange rates.")
    val listingText = remember(appLanguage, planLabel, shortDescription, keywords, disclaimer) {
        buildString {
            append("FX Always store listing kit\n")
            append("Language: $appLanguage\n")
            append("Plan context: $planLabel\n")
            append("Title: $listingTitle\n")
            append("Short description: $shortDescription\n")
            append("Keywords: $keywords\n")
            append("Disclaimer: $disclaimer")
        }
    }
    BentoCard(Modifier.testTag("settings_store_listing_kit"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValueRow(
                ui("Listing draft"),
                listingTitle,
                "$appLanguage · $planLabel",
                modifier = Modifier.testTag("store_listing_title"),
            )
            KeyValueRow(
                ui("Short description"),
                shortDescription,
                modifier = Modifier.testTag("store_listing_short_description"),
            )
            KeyValueRow(
                ui("Keywords"),
                keywords,
                modifier = Modifier.testTag("store_listing_keywords"),
            )
            KeyValueRow(
                ui("Store disclaimer"),
                disclaimer,
                modifier = Modifier.testTag("store_listing_disclaimer"),
            )
            GhostButton(
                text = if (copied) ui("Copied store listing") else ui("Copy store listing"),
                modifier = Modifier.fillMaxWidth().testTag("store_listing_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(listingText))
                    copied = true
                },
            )
        }
    }
}

@Composable
private fun WidgetQuickSetupCard(
    baseCurrency: String,
    availableCurrencies: List<FxRate>,
) {
    val candidates = remember(baseCurrency, availableCurrencies) {
        val byCode = availableCurrencies.associateBy { it.code }
        (listOf("EUR", "JPY", "GBP", "MXN", "BRL", "AUD", "CAD", "CHF") + PopularCurrencyCodes)
            .filter { it != baseCurrency }
            .distinct()
            .mapNotNull { byCode[it] }
            .take(6)
            .ifEmpty { availableCurrencies.filterNot { it.code == baseCurrency }.take(6) }
    }
    var widgetTarget by remember { mutableStateOf(AppSettingsPrefs.converterCurrencyCodes().firstOrNull() ?: candidates.firstOrNull()?.code.orEmpty()) }
    var travelerTarget by remember { mutableStateOf(AppSettingsPrefs.travelerCurrency()) }
    var feedback by remember { mutableStateOf<String?>(null) }
    BentoCard(Modifier.testTag("settings_widget_setup"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("Widget quick setup"))
                feedback?.let { Pill(ui(it), variant = PillVariant.Accent) }
            }
            Text(
                ui("Tap to pin this currency to widgets and refresh Android home screen cards."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            KeyValueRow(
                ui("Rates widget pair"),
                "$baseCurrency → ${widgetTarget.ifBlank { "--" }}",
                ui("Converted to"),
                modifier = Modifier.testTag("widget_setup_rates_pair"),
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                candidates.forEach { rate ->
                    TransactionChip(
                        label = rate.code,
                        selected = widgetTarget == rate.code,
                        modifier = Modifier.testTag("widget_setup_rate_${rate.code}"),
                        onClick = {
                            widgetTarget = rate.code
                            AppSettingsPrefs.setConverterCurrencyCodes(
                                (listOf(rate.code) + AppSettingsPrefs.converterCurrencyCodes())
                                    .filter { it != baseCurrency }
                                    .distinct()
                                    .take(4),
                            )
                            refreshFxWidgets()
                            feedback = "Widgets refreshed"
                        },
                    )
                }
            }
            KeyValueRow(
                ui("Traveler widget destination"),
                travelerTarget.ifBlank { "--" },
                travelerDestination(travelerTarget.ifBlank { "JPY" }).city,
                modifier = Modifier.testTag("widget_setup_traveler_destination"),
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                candidates.forEach { rate ->
                    TransactionChip(
                        label = rate.code,
                        selected = travelerTarget == rate.code,
                        modifier = Modifier.testTag("widget_setup_traveler_${rate.code}"),
                        onClick = {
                            travelerTarget = rate.code
                            AppSettingsPrefs.setTravelerCurrency(rate.code)
                            refreshFxWidgets()
                            feedback = "Widgets refreshed"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InternalTestPlanCard(
    appLanguage: String,
    baseCurrency: String,
    subscriptionState: SubscriptionState,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(appLanguage, baseCurrency, subscriptionState.isPremium) { mutableStateOf(false) }
    val planLabel = if (subscriptionState.isPremium) "Pro" else "Free"
    val checklist = listOf(
        "Free limits" to "Validate limits, previews and upsells.",
        "Pro unlocks" to "Validate expanded calendars, histories and portfolios.",
        "Offline/cache" to "Validate cached rates and traveler offline pack.",
        "Paywall/legal" to "Validate restore, manage subscription, terms and privacy.",
    )
    val testPlanText = remember(appLanguage, baseCurrency, planLabel) {
        buildString {
            append("FX Always internal test plan\n")
            append("Plan: $planLabel\n")
            append("Base: $baseCurrency\n")
            append("Language: $appLanguage\n")
            checklist.forEach { (title, detail) ->
                append("- $title: $detail\n")
            }
        }.trim()
    }
    BentoCard(Modifier.testTag("settings_internal_test_plan"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValueRow(
                ui("Manual QA checklist"),
                "$planLabel · $baseCurrency · ${appLanguage.uppercase()}",
                ui("Cover before each internal build."),
                modifier = Modifier.testTag("internal_test_plan_summary"),
            )
            checklist.forEachIndexed { index, item ->
                KeyValueRow(
                    ui(item.first),
                    ui(item.second),
                    modifier = Modifier.testTag("internal_test_plan_row_$index"),
                )
            }
            GhostButton(
                text = if (copied) ui("Copied test plan") else ui("Copy test plan"),
                modifier = Modifier.fillMaxWidth().testTag("internal_test_plan_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(testPlanText))
                    copied = true
                },
            )
        }
    }
}

@Composable
private fun ReleaseReadinessCard(
    appLanguage: String,
    baseCurrency: String,
    backupState: UserBackupState,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(appLanguage, baseCurrency, backupState.uid, subscriptionState.isPremium) { mutableStateOf(false) }
    val backupLabel = when {
        backupState.isAvailable && backupState.isAnonymous -> ui("guest")
        backupState.isAvailable -> ui("signed in")
        else -> ui("offline")
    }
    val planLabel = if (subscriptionState.isPremium) "Pro" else "Free"
    val syncLabel = if (lastSyncedAtMillis != null) localizedShortAgeLabel(lastSyncedAtMillis) else ui("Never")
    val supportSnapshot = remember(appLanguage, baseCurrency, backupLabel, planLabel, syncLabel) {
        buildString {
            append("FX Always support snapshot\n")
            append("Version: ${PlatformConfig.versionName}\n")
            append("Plan: $planLabel\n")
            append("Base: $baseCurrency\n")
            append("Language: $appLanguage\n")
            append("Backup: $backupLabel\n")
            append("Last sync: $syncLabel")
        }
    }
    BentoCard(Modifier.testTag("settings_release_readiness"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Build"),
                    PlatformConfig.versionName,
                    ui("Ready for tester reports"),
                    Modifier.weight(1f).testTag("release_ready_build"),
                )
                MetricTile(
                    ui("Backup"),
                    backupLabel,
                    syncLabel,
                    Modifier.weight(1f).testTag("release_ready_backup"),
                )
            }
            KeyValueRow(
                ui("Legal"),
                ui("Policies linked"),
                "Terms · Privacy",
                modifier = Modifier.testTag("release_ready_legal"),
            )
            Text(
                ui("Tester context includes plan, base, language and backup state."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            GhostButton(
                text = if (copied) ui("Copied support snapshot") else ui("Copy support snapshot"),
                modifier = Modifier.fillMaxWidth().testTag("release_support_snapshot_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(supportSnapshot))
                    copied = true
                },
            )
        }
    }
}

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

@Composable
private fun localizedRuntimeLabel(label: String): String =
    when {
        label == "cached · mock" -> "${ui("cached")} · mock"
        label == "Auto-refresh off" -> ui("Auto-refresh off")
        label.startsWith("Auto-refresh every ") -> "${ui("Auto-refresh every")} ${label.substringAfter("every ").substringBefore(" min")} ${ui("min")}"
        label == "loading" -> ui("loading")
        label == "updated just now" -> ui("updated just now")
        label.startsWith("updated ") && label.endsWith("m ago") -> "${ui("updated")} ${label.removePrefix("updated ").removeSuffix("m ago")}m ${ui("ago")}"
        label.startsWith("updated ") -> "${ui("updated")} ${label.removePrefix("updated ")}"
        label.startsWith("refreshed ") -> "${ui("refreshed")} ${label.removePrefix("refreshed ")}"
        else -> ui(label)
    }

@Composable
internal fun localizedCurrencyName(name: String): String = ui(name)

@Composable
private fun localizedRate(rate: FxRate): FxRate =
    rate.copy(
        name = localizedCurrencyName(rate.name),
        caption = if (rate.caption == "cached · mock") localizedRuntimeLabel(rate.caption) else ui(rate.caption),
    )

@Composable
private fun localizedSubscriptionMessage(message: String): String =
    when {
        message.startsWith("No RevenueCat package is configured for ") -> {
            val plan = message.removePrefix("No RevenueCat package is configured for ").removeSuffix(".")
            "${ui("No RevenueCat package is configured for")} ${ui(plan)}."
        }
        message.startsWith("Pro active") -> message.replace("Pro active", ui("Pro active"))
        message == "RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases." -> ui("RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases.")
        message == "RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases." -> ui("RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases.")
        message == "RevenueCat key missing. Restore is not connected yet." -> ui("RevenueCat key missing. Restore is not connected yet.")
        message == "RevenueCat unavailable." -> ui("RevenueCat unavailable.")
        message == "No offering packages are configured in RevenueCat." -> ui("No offering packages are configured in RevenueCat.")
        message == "Purchase did not complete." -> ui("Purchase did not complete.")
        message == "Restore failed." -> ui("Restore failed.")
        message == "Dev override only affects local debug gating." -> ui("Dev override only affects local debug gating.")
        else -> ui(message)
    }

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

enum class QuickAlertState(
    val label: String,
    val variant: PillVariant,
) {
    Create("create", PillVariant.Ghost),
    Active("active", PillVariant.Up),
    Paused("resume", PillVariant.Ghost),
    Locked("pro", PillVariant.Accent),
}

private data class SmartAlertSuggestion(
    val rate: FxRate,
    val title: String,
    val subtitle: String,
    val target: Double,
    val direction: AlertDirection,
    val kind: AlertKind = AlertKind.Target,
    val strength: Double,
) {
    val strengthLabel: String
        get() = "${(strength * 100.0).toInt()}%"
}

private fun smartAlertSuggestions(rates: List<FxRate>, isPremium: Boolean): List<SmartAlertSuggestion> =
    rates
        .mapNotNull(::smartAlertSuggestion)
        .sortedWith(compareByDescending<SmartAlertSuggestion> { it.strength }.thenBy { it.rate.code })
        .take(if (isPremium) 4 else 2)

private fun smartAlertSuggestion(rate: FxRate): SmartAlertSuggestion? {
    val points = (rate.sparkline + rate.rate.toFloat())
        .filter { it.isFinite() && it > 0f }
        .map { it.toDouble() }
    if (points.size < 3) return null
    val low = points.minOrNull() ?: return null
    val high = points.maxOrNull() ?: return null
    val range = high - low
    if (range <= 0.0) return null
    val position = ((rate.rate - low) / range).coerceIn(0.0, 1.0)
    return when {
        position >= 0.74 -> SmartAlertSuggestion(
            rate = rate,
            title = "Near recent high",
            subtitle = "30d range signal",
            target = rate.rate * 1.002,
            direction = AlertDirection.Above,
            strength = position,
        )
        position <= 0.26 -> SmartAlertSuggestion(
            rate = rate,
            title = "Near recent low",
            subtitle = "30d range signal",
            target = rate.rate * 0.998,
            direction = AlertDirection.Below,
            strength = 1.0 - position,
        )
        else -> null
    }
}

private data class AlertPreset(
    val label: String,
    val percent: Double,
)

private data class AlertTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: AlertKind,
    val direction: AlertDirection,
    val targetText: (FxRate) -> String,
)

private val alertTemplates = listOf(
    AlertTemplate(
        id = "travel_good_rate",
        title = "Good travel rate",
        subtitle = "Alert when the destination rate improves for a trip.",
        kind = AlertKind.Target,
        direction = AlertDirection.Above,
        targetText = { rate -> formatRate(rate.rate * 1.01) },
    ),
    AlertTemplate(
        id = "daily_breakout",
        title = "Daily breakout",
        subtitle = "Alert when a pair moves sharply in one day.",
        kind = AlertKind.DailyChange,
        direction = AlertDirection.Above,
        targetText = { "2.0" },
    ),
    AlertTemplate(
        id = "remittance_window",
        title = "Better remittance window",
        subtitle = "Alert before a repeat transfer window improves.",
        kind = AlertKind.Target,
        direction = AlertDirection.Below,
        targetText = { rate -> formatRate(rate.rate * 0.99) },
    ),
)

private val alertPresets = listOf(
    AlertPreset("-1%", -1.0),
    AlertPreset("-0.5%", -0.5),
    AlertPreset("+0.5%", 0.5),
    AlertPreset("+1%", 1.0),
)

private fun List<PriceAlert>.findQuickAlert(baseCurrency: String, rate: FxRate): PriceAlert? {
    val target = quickAlertTarget(rate)
    return findMatchingAlert(
        baseCurrency = baseCurrency,
        quote = rate.code,
        target = target,
        direction = AlertDirection.Above,
        kind = AlertKind.Target,
    )
}

private fun quickAlertTarget(rate: FxRate): Double =
    rate.rate * 1.01

private fun List<PriceAlert>.findMatchingAlert(
    baseCurrency: String,
    quote: String,
    target: Double,
    direction: AlertDirection,
    kind: AlertKind,
): PriceAlert? =
    firstOrNull {
        it.matchesDefinition(
            base = baseCurrency,
            quote = quote,
            target = target,
            direction = direction,
            kind = kind,
        )
    }

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

@Composable
private fun localizedAlertStatusLabel(alert: PriceAlert, currentRate: Double?, currentChangePct: Double?): String =
    when {
        alert.kind == AlertKind.Target && currentRate == null -> "${ui("waiting for live rate")} · ${alert.base}"
        alert.kind == AlertKind.DailyChange && currentChangePct == null -> ui("waiting for 24h change")
        alert.isHit(currentRate, currentChangePct) -> ui("target hit")
        alert.kind == AlertKind.Target && currentRate != null -> "${alert.distancePercent(currentRate)}% ${ui("away")}"
        alert.kind == AlertKind.DailyChange && currentChangePct != null -> "${alert.dailyChangeDistancePercent(currentChangePct)} ${ui("pts away")}"
        else -> ui("waiting")
    }

@Composable
private fun localizedAlertDistanceLabel(alert: PriceAlert, currentRate: Double?, currentChangePct: Double?): String =
    when {
        alert.kind == AlertKind.Target && currentRate == null -> ui("base changed")
        alert.kind == AlertKind.DailyChange && currentChangePct == null -> ui("waiting")
        alert.isHit(currentRate, currentChangePct) -> ui("target reached")
        alert.kind == AlertKind.Target && currentRate != null -> "${alert.distancePercent(currentRate)}% ${ui("to target")}"
        alert.kind == AlertKind.DailyChange && currentChangePct != null -> "${alert.dailyChangeDistancePercent(currentChangePct)} ${ui("pts to move")}"
        else -> ui("waiting")
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

@Composable
private fun localizedAlertSummaryLine(
    kind: AlertKind,
    rate: FxRate,
    direction: AlertDirection,
    targetValue: Double,
    currentChangePct: Double,
): String =
    when (kind) {
        AlertKind.Target -> "${ui("Current")} ${formatRate(rate.rate)} · ${ui("target")} ${if (targetValue > 0.0) formatRate(targetValue) else "--"}"
        AlertKind.DailyChange -> {
            val threshold = if (targetValue > 0.0) {
                "${ui(direction.label(kind)).lowercase()} ${formatPercentValue(targetValue)}%"
            } else {
                "--"
            }
            "24h ${formatSignedPercent(currentChangePct)} · ${ui("alert at")} $threshold"
        }
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

@Composable
private fun localizedShortAgeLabel(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 60 -> ui("Now")
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ${ui("ago")}"
        elapsedSeconds < 86_400 -> "${elapsedSeconds / 3600}h ${ui("ago")}"
        else -> "${elapsedSeconds / 86_400}d ${ui("ago")}"
    }
}

private fun LiveRatesState.alertRates(isPremium: Boolean): List<FxRate> =
    (favorites + compare + converter + allFiat + availableCryptoRates(isPremium))
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.portfolioRates(isPremium: Boolean = false): List<FxRate> =
    (converter + favorites + compare + allFiat + availableCryptoRates(isPremium))
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code == baseCurrency || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.defaultCryptoRates(): List<FxRate> {
    val byCode = crypto.associateBy { it.code }
    return DefaultCryptoCodes.mapNotNull { byCode[it] }
}

private fun LiveRatesState.visibleDashboardCryptoRates(isPremium: Boolean, trackedCurrencyCodes: List<String>): List<FxRate> {
    val byCode = crypto.associateBy { it.code }
    val trackedCrypto = if (isPremium) {
        trackedCurrencyCodes
            .filter { it !in DefaultCryptoCodes }
            .mapNotNull { byCode[it] }
    } else {
        emptyList()
    }
    return (defaultCryptoRates() + trackedCrypto).distinctBy { it.code }
}

private fun LiveRatesState.availableCryptoRates(isPremium: Boolean): List<FxRate> =
    if (isPremium) {
        crypto
    } else {
        defaultCryptoRates()
    }

private fun LiveRatesState.converterAvailableRates(isPremium: Boolean): List<FxRate> =
    (allFiat + favorites + compare + converter + availableCryptoRates(isPremium))
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.compareAvailableRates(isPremium: Boolean): List<FxRate> =
    (compare + favorites + converter + allFiat + availableCryptoRates(isPremium))
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

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

private fun compareTargetCodes(
    selectedCurrencyCodes: List<String>,
    availableRates: List<FxRate>,
    baseCurrency: String,
    limit: Int,
): List<String> =
    converterTargetCodes(
        selectedCurrencyCodes = selectedCurrencyCodes,
        availableRates = availableRates,
        baseCurrency = baseCurrency,
        limit = limit,
    )

private enum class CompareSortMode(val label: String) {
    Movers("Movers"),
    Strongest("Strongest"),
    Weakest("Weakest"),
}

private fun List<FxRate>.sortedForCompare(sortMode: CompareSortMode): List<FxRate> =
    when (sortMode) {
        CompareSortMode.Movers -> sortedByDescending { kotlin.math.abs(it.change24h) }
        CompareSortMode.Strongest -> sortedByDescending { it.change24h }
        CompareSortMode.Weakest -> sortedBy { it.change24h }
    }

private data class PortfolioHolding(
    val rate: FxRate,
    val amount: Double,
    val averageCostBase: Double,
) {
    val baseValue: Double = amountInBase(rate, amount)
    val dailyChangeInBase: Double = if (rate.rate == 0.0) 0.0 else baseValue * rate.change24h / 100.0
    val hasCostBasis: Boolean = averageCostBase > 0.0 && amount > 0.0
    val costBasisBase: Double = if (hasCostBasis) averageCostBase * amount else 0.0
    val unrealizedPnlBase: Double = if (hasCostBasis) baseValue - costBasisBase else 0.0
}

private fun amountInBase(rate: FxRate, amount: Double): Double =
    if (rate.rate == 0.0) 0.0 else amount / rate.rate

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

private fun List<PortfolioHolding>.portfolioValueSeries(): List<Float> {
    if (isEmpty()) return emptyList()
    val pointCount = minOf(24, map { it.rate.sparkline.size }.filter { it > 0 }.minOrNull() ?: return emptyList())
    return List(pointCount) { index ->
        sumOf { holding ->
            val point = holding.rate.sparkline.getOrNull(index)?.toDouble() ?: holding.rate.rate
            if (point <= 0.0) 0.0 else holding.amount / point
        }.toFloat()
    }
}

private fun List<Float>.changePercent(): Double =
    if (size < 2 || first() == 0f) 0.0 else (last() - first()) / first() * 100.0

private fun formatSignedMoney(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}

private fun formatPortfolioSignedPercent(change: Double): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign${formatRate(kotlin.math.abs(change))}%"
}

private fun portfolioPnlPercentLabel(pnl: Double, costBasis: Double): String =
    if (costBasis <= 0.0) {
        "Add average cost"
    } else {
        val sign = if (pnl >= 0.0) "+" else "-"
        "$sign${formatRate(kotlin.math.abs(pnl / costBasis * 100.0))}%"
    }

private fun allocationLabel(value: Double, portfolioValue: Double): String =
    if (portfolioValue <= 0.0 || value <= 0.0) "0%" else "${((value / portfolioValue) * 100.0).toInt()}%"

@Composable
private fun portfolioActionPlan(
    largestHolding: PortfolioHolding?,
    largestDailyDriver: PortfolioHolding?,
    portfolioDailyChange: Double,
): String =
    when {
        largestHolding == null -> ui("Add amounts to activate portfolio guidance.")
        largestHolding.baseValue > 0.0 && largestDailyDriver?.rate?.code == largestHolding.rate.code && portfolioDailyChange < 0.0 ->
            "${ui("Review")} ${largestHolding.rate.code} ${ui("before adding more exposure")}"
        largestHolding.baseValue > 0.0 ->
            "${ui("Keep")} ${largestHolding.rate.code} ${ui("below concentration target")}"
        else -> ui("Review concentration before adding new exposure.")
    }

private fun formatPortfolioChange(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}

private fun buildUserBackupSnapshot(
    themeMode: ThemeMode,
    language: String,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    userProfile: UserProfile,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
): UserBackupSnapshot =
    UserBackupSnapshot(
        updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
        settings = BackupSettings(
            themeMode = themeMode.name,
            language = language,
            baseCurrency = baseCurrency,
            travelerCurrency = travelerCurrency,
            travelerBudgetBase = travelerBudgetBase,
            converterCurrencyCodes = converterCurrencyCodes,
            compareCurrencyCodes = compareCurrencyCodes,
            providerPreferenceCodes = providerPreferenceCodes,
            userProfile = userProfile.name,
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
    onCompareCurrencyCodes: (List<String>) -> Unit,
    onProviderPreferenceCodes: (List<String>) -> Unit,
    onTravelerCurrency: (String) -> Unit,
    onTravelerBudgetBase: (Double) -> Unit,
    onUserProfile: (UserProfile) -> Unit,
    onLanguage: (String) -> Unit,
): ThemeMode {
    val theme = ThemeMode.entries.firstOrNull { it.name == snapshot.settings.themeMode } ?: ThemeMode.System
    val language = snapshot.settings.language.ifBlank { DeviceLocale.language }
    val profile = UserProfile.entries.firstOrNull { it.name == snapshot.settings.userProfile } ?: UserProfile.Traveler
    AppSettingsPrefs.setThemeMode(theme)
    AppSettingsPrefs.setLanguage(language)
    AppSettingsPrefs.setBaseCurrency(snapshot.settings.baseCurrency)
    AppSettingsPrefs.setTravelerCurrency(snapshot.settings.travelerCurrency)
    AppSettingsPrefs.setTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    AppSettingsPrefs.setConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    AppSettingsPrefs.setCompareCurrencyCodes(snapshot.settings.compareCurrencyCodes)
    AppSettingsPrefs.setProviderPreferenceCodes(snapshot.settings.providerPreferenceCodes)
    AppSettingsPrefs.setUserProfile(profile)
    liveStore.setBaseCurrency(snapshot.settings.baseCurrency)
    onLanguage(language)
    onConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    onCompareCurrencyCodes(snapshot.settings.compareCurrencyCodes)
    onProviderPreferenceCodes(snapshot.settings.providerPreferenceCodes)
    onTravelerCurrency(snapshot.settings.travelerCurrency)
    onTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    onUserProfile(profile)
    alertsStore.replaceAll(snapshot.alerts)
    watchlistStore.replaceFromBackup(snapshot.watchlist)
    return theme
}

private fun canCreateAlert(subscriptionState: SubscriptionState, currentCount: Int): Boolean {
    val access = subscriptionState.featureAccess()
    return access.hasUnlimitedAlerts || currentCount < access.alertLimit
}

@Composable
private fun StoryCard(story: NewsStory, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    BentoCard(
        padding = 12.dp,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(story.tag, variant = PillVariant.Ghost)
                    Eyebrow(ui(story.impact), color = if (story.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent)
                }
                Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            Text(story.title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(story.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            if (story.source.isNotBlank()) {
                Text(
                    if (story.sourceUrl.isNotBlank()) "${story.source} · ${ui("tap for details")}" else story.source,
                    style = FxTheme.typography.captionMono,
                    color = if (story.sourceUrl.isNotBlank()) FxTheme.colors.accent else FxTheme.colors.textFaint,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("MOVES"))
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
                    Text(ui("Search headlines, tags or currencies"), style = FxTheme.typography.caption, color = FxTheme.colors.textGhost)
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

private fun newsEmptyCopy(
    hasBackendStories: Boolean,
    hasQuery: Boolean,
    topic: String,
): Pair<String, String> =
    when {
        !hasBackendStories -> "No market stories yet" to "No live market stories have arrived yet."
        hasQuery -> "No search matches" to "No live stories match this search."
        topic != "ALL" -> "No topic stories" to "Try a broader filter or refresh the feed."
        else -> "No currency stories" to "Try a broader filter or refresh the feed."
    }

private fun privacyPolicyUrl(language: String): String = legalDocumentUrl("privacy", language)

private fun termsOfUseUrl(language: String): String = legalDocumentUrl("terms", language)

private fun legalDocumentUrl(doc: String, language: String): String {
    val normalizedLanguage = language
        .substringBefore("-")
        .substringBefore("_")
        .lowercase()
        .ifBlank { "en" }
    return "https://fxalways.com/legal?doc=$doc&lang=$normalizedLanguage"
}

private fun subscriptionManagementUrl(): String =
    when (PlatformConfig.platform) {
        Platform.Android -> "https://play.google.com/store/account/subscriptions"
        Platform.Ios -> "https://apps.apple.com/account/subscriptions"
    }

private data class SettingsCopy(
    val title: String,
    val sub: String,
    val more: String,
    val backup: String,
    val syncNow: String,
    val syncNowSubtitle: String,
    val syncing: String,
    val sync: String,
    val signInWith: String,
    val signInSubtitle: String,
    val signInProgressSubtitle: String,
    val connect: String,
    val connecting: String,
    val signOut: String,
    val signOutSubtitle: String,
    val signOutAction: String,
    val subscription: String,
    val view: String,
    val upgrade: String,
    val restorePurchase: String,
    val restorePurchaseSubtitle: String,
    val restore: String,
    val manageSubscription: String,
    val manageSubscriptionSubtitle: String,
    val open: String,
    val notifications: String,
    val priceAlertNotifications: String,
    val themeMode: String,
    val language: String,
    val activeLanguage: String,
    val deviceLanguage: String,
    val languageApplied: String,
    val baseCurrency: String,
    val moreCurrencies: String,
    val search: String,
    val freeIncludes: String,
    val unlocks: String,
    val unlockAllBaseCurrencies: String,
    val supportedBaseCurrencies: String,
    val legal: String,
    val privacyPolicy: String,
    val privacyPolicySubtitle: String,
    val termsOfUse: String,
    val termsOfUseSubtitle: String,
)

private fun settingsCopy(language: String): SettingsCopy =
    when (language.lowercase()) {
        "es" -> SettingsCopy(
            title = "Ajustes",
            sub = "PREFERENCIAS",
            more = "Más",
            backup = "BACKUP",
            syncNow = "Sincronizar ahora",
            syncNowSubtitle = "Guarda ajustes, alertas y watchlist en Firebase",
            syncing = "sincronizando",
            sync = "sincronizar",
            signInWith = "Iniciar sesión con",
            signInSubtitle = "Mantén el mismo backup y restáuralo en un nuevo",
            signInProgressSubtitle = "Conectando la cuenta de forma segura. Espera un momento.",
            connect = "conectar",
            connecting = "conectando",
            signOut = "Cerrar sesión",
            signOutSubtitle = "Mantén los datos locales y continúa con backup invitado",
            signOutAction = "salir",
            subscription = "SUSCRIPCIÓN",
            view = "ver",
            upgrade = "pro",
            restorePurchase = "Restaurar compra",
            restorePurchaseSubtitle = "Recupera una suscripción existente de Play/App Store",
            restore = "restaurar",
            manageSubscription = "Gestionar suscripción",
            manageSubscriptionSubtitle = "Abre el centro de suscripciones de la tienda",
            open = "abrir",
            notifications = "NOTIFICACIONES",
            priceAlertNotifications = "Notificaciones de alertas",
            themeMode = "TEMA",
            language = "IDIOMA",
            activeLanguage = "Idioma activo",
            deviceLanguage = "Idioma del dispositivo",
            languageApplied = "aplicado",
            baseCurrency = "MONEDA BASE",
            moreCurrencies = "Más monedas",
            search = "Buscar monedas soportadas",
            freeIncludes = "Free incluye",
            unlocks = "desbloquea",
            unlockAllBaseCurrencies = "Desbloquear todas las monedas base",
            supportedBaseCurrencies = "monedas base soportadas",
            legal = "LEGAL",
            privacyPolicy = "Política de privacidad",
            privacyPolicySubtitle = "Cómo FX Always maneja cuenta, rates y datos",
            termsOfUse = "Términos de uso",
            termsOfUseSubtitle = "Suscripción, disclaimers y uso aceptable",
        )
        "pt" -> SettingsCopy(
            title = "Ajustes",
            sub = "PREFERÊNCIAS",
            more = "Mais",
            backup = "BACKUP",
            syncNow = "Sincronizar agora",
            syncNowSubtitle = "Salva ajustes, alertas e watchlist no Firebase",
            syncing = "sincronizando",
            sync = "sincronizar",
            signInWith = "Entrar com",
            signInSubtitle = "Mantenha o mesmo backup e restaure em um novo",
            signInProgressSubtitle = "Conectando a conta com segurança. Aguarde um momento.",
            connect = "conectar",
            connecting = "conectando",
            signOut = "Sair",
            signOutSubtitle = "Mantém dados locais e continua com backup convidado",
            signOutAction = "sair",
            subscription = "ASSINATURA",
            view = "ver",
            upgrade = "pro",
            restorePurchase = "Restaurar compra",
            restorePurchaseSubtitle = "Recupera uma assinatura existente da Play/App Store",
            restore = "restaurar",
            manageSubscription = "Gerenciar assinatura",
            manageSubscriptionSubtitle = "Abre o centro de assinaturas da loja",
            open = "abrir",
            notifications = "NOTIFICAÇÕES",
            priceAlertNotifications = "Notificações de alertas",
            themeMode = "TEMA",
            language = "IDIOMA",
            activeLanguage = "Idioma ativo",
            deviceLanguage = "Idioma do dispositivo",
            languageApplied = "aplicado",
            baseCurrency = "MOEDA BASE",
            moreCurrencies = "Mais moedas",
            search = "Buscar moedas suportadas",
            freeIncludes = "Free inclui",
            unlocks = "desbloqueia",
            unlockAllBaseCurrencies = "Desbloquear todas as moedas base",
            supportedBaseCurrencies = "moedas base suportadas",
            legal = "LEGAL",
            privacyPolicy = "Política de privacidade",
            privacyPolicySubtitle = "Como FX Always lida com conta, rates e dados",
            termsOfUse = "Termos de uso",
            termsOfUseSubtitle = "Assinatura, disclaimers e uso aceitável",
        )
        else -> {
            fun t(key: String): String = localizedUiText(language, key)
            SettingsCopy(
                title = t("Settings"),
                sub = t("APP PREFERENCES"),
                more = t("More"),
                backup = t("BACKUP"),
                syncNow = t("Sync now"),
                syncNowSubtitle = t("Push the latest settings, alerts and watchlist to Firebase"),
                syncing = t("syncing"),
                sync = t("sync"),
                signInWith = t("Sign in with"),
                signInSubtitle = t("Keep the same backup and restore it on a new"),
                signInProgressSubtitle = t("Connecting your account securely. Please wait."),
                connect = t("connect"),
                connecting = t("connecting"),
                signOut = t("Sign out"),
                signOutSubtitle = t("Keep local data and continue with a new guest backup"),
                signOutAction = t("sign out"),
                subscription = t("SUBSCRIPTION"),
                view = t("view"),
                upgrade = t("upgrade"),
                restorePurchase = t("Restore purchase"),
                restorePurchaseSubtitle = t("Recover an existing Play/App Store subscription"),
                restore = t("restore"),
                manageSubscription = t("Manage subscription"),
                manageSubscriptionSubtitle = t("Open the store subscription center for billing changes"),
                open = t("open"),
                notifications = t("NOTIFICATIONS"),
                priceAlertNotifications = t("Price alert notifications"),
                themeMode = t("THEME MODE"),
                language = t("LANGUAGE"),
                activeLanguage = t("Active language"),
                deviceLanguage = t("Device language"),
                languageApplied = t("applied"),
                baseCurrency = t("BASE CURRENCY"),
                moreCurrencies = t("More currencies"),
                search = t("Search supported base currencies"),
                freeIncludes = t("Free includes"),
                unlocks = t("unlocks"),
                unlockAllBaseCurrencies = t("Unlock all base currencies"),
                supportedBaseCurrencies = t("supported base currencies"),
                legal = t("LEGAL"),
                privacyPolicy = t("Privacy Policy"),
                privacyPolicySubtitle = t("How FX Always handles account, rates and analytics data"),
                termsOfUse = t("Terms of Use"),
                termsOfUseSubtitle = t("Subscription terms, disclaimers and acceptable use"),
            )
        }
    }

@Composable
private fun ProUpsellCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    BentoCard(
        modifier = modifier
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

private fun NewsStory.safeTestTagKey(): String =
    title
        .filter { it.isLetterOrDigit() }
        .take(18)
        .ifBlank { tag }

@Composable
fun PaywallScreen(
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    actionInProgress: Boolean = false,
    userProfile: UserProfile = UserProfile.Traveler,
    appLanguage: String = LocalAppLanguage.current,
    onClose: () -> Unit = {},
    onStart: (SubscriptionPlanKind) -> Unit = {},
    onRestore: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    var selectedKind by remember { mutableStateOf(SubscriptionPlanKind.Monthly) }
    val selectedPlan = subscriptionState.plans.firstOrNull { it.kind == selectedKind && it.isAvailable }
        ?: subscriptionState.plans.firstOrNull { it.isAvailable }
        ?: subscriptionState.plans.first()
    val profileCopy = userProfile.copy()
    val profilePreset = userProfile.preset()
    LaunchedEffect(selectedPlan.kind) {
        selectedKind = selectedPlan.kind
    }

    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textDim, modifier = Modifier.testTag("paywall_close").clickable(onClick = onClose))
        }
        Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
	        Text(ui("The full picture.\nMore rates. More context."), style = FxTheme.typography.display, color = FxTheme.colors.text)
        Text(
	            ui("Monthly or annual Pro unlocks unlimited alerts, deeper history, expanded comparisons, traveler tools and watchlists."),
            style = FxTheme.typography.body,
            color = FxTheme.colors.textDim,
        )
        Text(
            ui("Built for people who move money, travel, track currencies or need alerts before rates move away."),
            style = FxTheme.typography.caption,
            color = FxTheme.colors.textFaint,
        )
        BentoCard(Modifier.testTag("paywall_profile_offer"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Eyebrow("${ui("FOR YOU")} · ${ui(profileCopy.label)}", color = FxTheme.colors.accent)
                Text(ui(profileCopy.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(ui(profileCopy.proFocus), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                PaywallProfileSignal(
                    label = ui("Suggested pair"),
                    value = profilePreset.suggestedPair,
                    detail = profilePreset.suggestedProvider,
                )
                PaywallProfileSignal(
                    label = ui("Suggested alert"),
                    value = ui(profilePreset.suggestedAlert),
                    detail = ui(profilePreset.suggestedHolding),
                )
            }
        }
        if (subscriptionState.isPremium) {
            ProActiveCard(subscriptionState = subscriptionState)
        }
        SectionLabel(ui("PRO UNLOCKS"))
        BentoCard(Modifier.testTag("paywall_benefits"), padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
	                BenefitRow("FX", ui("Fresh market rates"), ui("Backend-backed mid-market rates with automatic refresh."))
	                BenefitRow("AL", ui("Unlimited alerts"), ui("Price, range, daily and weekly targets."))
	                BenefitRow("TR", ui("Traveler mode"), ui("Auto-location, cheat sheets and offline rates."))
	                BenefitRow("%", ui("Fee comparison"), ui("Expanded provider estimates by amount and currency pair."))
	                BenefitRow("OCR", ui("OCR price scanner"), ui("Camera scanner fills the hidden-cost check from shelf, receipt or cash-desk prices."))
	                BenefitRow("WL", ui("Bigger watchlists"), ui("Track more currencies across converter, compare and portfolio."))
	                BenefitRow("1Y", ui("Long-range history"), ui("Unlock 1Y and all-time detail views where history is available."))
            }
        }
        SectionLabel(ui("FREE VS PRO"))
        BentoCard(Modifier.testTag("paywall_comparison"), padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaywallComparisonRow("alerts", ui("Custom alerts"), ui("1 active alert"), ui("Unlimited pairs + ranges"))
                PaywallComparisonRow("compare", ui("Compare board"), ui("4 currencies"), ui("Every tracked currency"))
                PaywallComparisonRow("ocr", ui("OCR price scanner"), ui("Manual entry"), ui("Live camera OCR + currency detection"))
                PaywallComparisonRow("crypto", ui("Crypto catalog"), ui("BTC, ETH, USDT, USDC"), ui("Search and add up to 200 crypto assets"))
                PaywallComparisonRow("traveler", ui("Traveler"), ui("Focused destinations"), ui("All destinations + full cheat sheet"))
                PaywallComparisonRow("watchlist", ui("Watchlist"), ui("4 tracked currencies"), ui("Unlimited portfolio tracking"))
                PaywallComparisonRow("news", ui("News"), ui("Top stories only"), ui("Full regional stream"))
                PaywallComparisonRow("history", ui("History"), ui("30 days"), ui("1Y + all-time where available"))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subscriptionState.plans.forEach { plan ->
                PlanOption(
                    plan = plan,
                    selected = plan.kind == selectedPlan.kind,
                    modifier = Modifier.testTag("paywall_plan_${plan.kind.name}"),
                    onSelect = {
                        if (plan.isAvailable) {
                            Observability.event("plan_selected", mapOf("plan" to plan.kind.name))
                            selectedKind = plan.kind
                        }
                    },
                )
            }
        }
        BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card).testTag("paywall_selected_plan"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    selectedPlan.badge?.let { Pill(ui(it), variant = PillVariant.Accent) }
                }
                Text(ui(selectedPlan.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                BigValueText(selectedPlan.priceLabel, ui(selectedPlan.cadenceLabel))
                Text(
	                    ui("Recurring subscription billed through Google Play on Android and App Store on iOS."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                )
            }
        }
        subscriptionState.statusMessage?.let {
            Text(
                localizedSubscriptionMessage(it),
                modifier = Modifier.testTag("paywall_status_message"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.down,
            )
        }
        PrimaryButton(
            when {
	                actionInProgress -> ui("Processing...")
	                subscriptionState.isPremium -> ui("Continue")
	                !subscriptionState.canPurchase -> ui("Purchases unavailable")
	                else -> ui("Start FX/ Pro")
            },
            enabled = !actionInProgress && (subscriptionState.isPremium || subscriptionState.canPurchase),
            isLoading = actionInProgress,
            onClick = {
                if (actionInProgress) {
                    return@PrimaryButton
                } else if (subscriptionState.isPremium) {
                    onClose()
                } else if (subscriptionState.canPurchase) {
                    onStart(selectedPlan.kind)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("paywall_start_button"),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
	                ui("Restore purchase"),
                style = FxTheme.typography.captionMono,
                color = if (actionInProgress) FxTheme.colors.textGhost else FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_restore").clickable(enabled = !actionInProgress, onClick = onRestore),
            )
            Text("  ·  ", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            Text(
                ui("Terms"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_terms").clickable { onOpenUrl(termsOfUseUrl(appLanguage)) },
            )
            Text("  ·  ", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            Text(
                ui("Privacy"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_privacy").clickable { onOpenUrl(privacyPolicyUrl(appLanguage)) },
            )
        }
    }
}

@Composable
private fun PaywallProfileSignal(label: String, value: String, detail: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(label, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        Text(value, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
        Text(detail, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}

@Composable
private fun PaywallComparisonRow(id: String, feature: String, freeValue: String, proValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("paywall_feature_$id")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.54f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(0.92f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(feature, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui("Free"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Text(freeValue, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.End) {
            Text(ui("Pro unlock"), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
            Text(proValue, style = FxTheme.typography.caption, color = FxTheme.colors.text)
        }
    }
}

@Composable
private fun ProActiveCard(subscriptionState: SubscriptionState) {
    BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card).testTag("paywall_active_card"), padding = 12.dp) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot("✓", CurrencyKind.Fiat, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
	                Eyebrow(ui("ACTIVE"), color = FxTheme.colors.accent)
	                Text(ui("FX/ Pro is active"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subscriptionState.localizedProStatusLabel(), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }
    }
}

@Composable
private fun PlanOption(
    plan: SubscriptionPlan,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val borderColor = if (selected) FxTheme.colors.accentLine else FxTheme.colors.border
    val contentAlpha = if (plan.isAvailable) 1f else 0.46f
    BentoCard(
        modifier = modifier
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
            FlagDot(planGlyph(plan.kind), CurrencyKind.Fiat, 40.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ui(plan.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    plan.badge?.let { Pill(ui(it), variant = PillVariant.Accent) }
                }
                Text(ui(plan.cadenceLabel), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(plan.priceLabel, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
	                    if (plan.isAvailable) ui("Available") else ui("Not configured"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }
    }
}

private fun planGlyph(kind: SubscriptionPlanKind): String =
    when (kind) {
        SubscriptionPlanKind.Monthly -> "1M"
        SubscriptionPlanKind.Yearly -> "1Y"
    }

private fun SubscriptionState.proStatusLabel(): String =
    if (isPremium) {
        activePlanLabel?.let { "Active plan: $it · Entitlement $entitlementId" }
            ?: "Entitlement $entitlementId is active"
    } else {
        "Alerts, extended history and unlimited watchlists"
    }

@Composable
private fun SubscriptionState.localizedProStatusLabel(): String =
    if (isPremium) {
        activePlanLabel?.let { "${ui("Active plan")}: $it · $entitlementId" }
            ?: "${ui("Entitlement is active")} · $entitlementId"
    } else {
        ui("Alerts, extended history and unlimited watchlists")
    }

@Composable
fun OfflineScreen(
    liveState: LiveRatesState = LiveRatesState(),
    onRefresh: () -> Unit = {},
) {
    val primaryRate = liveState.favorites.firstOrNull()
        ?: liveState.converter.firstOrNull { it.code != liveState.baseCurrency }
        ?: liveState.compare.firstOrNull()
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot(color = FxTheme.colors.down)
	            Eyebrow(ui("OFFLINE"), color = FxTheme.colors.down)
	            Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
	        ScreenHeader(
            ui("No connection"),
            subtitle = if (liveState.isOfflineCache) {
                ui("Showing rates from your last sync")
            } else {
                ui("Connect once to save rates for offline use")
            },
        )
        if (primaryRate != null) {
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
	                Eyebrow("${ui("LAST KNOWN")} · ${liveState.baseCurrency} → ${primaryRate.code}", color = FxTheme.colors.down)
                    Text(formatRate(primaryRate.rate), style = FxTheme.typography.numberXL, color = FxTheme.colors.textDim)
                    Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                }
            }
        } else {
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Eyebrow(ui("LAST KNOWN"), color = FxTheme.colors.down)
                    Text(ui("No saved rates yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
                }
            }
        }
	        PrimaryButton("↻  ${ui("Retry connection")}", onClick = onRefresh)
	        SectionLabel(ui("CACHED FAVORITES"))
        BentoCard(padding = 0.dp) {
            Column {
                liveState.favorites.take(4).forEach { CurrencyRow(localizedRate(it), dense = true, enabled = false) }
            }
        }
        Text("╌╌╌  ${ui("saved locally")}  ╌╌╌", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

private data class OnboardingStep(
    val tag: String,
    val title: String,
    val body: String,
    val glyph: String,
    val signal: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: (UserProfile) -> Unit = {}) {
    val localCurrency = remember { DeviceLocale.currencyCode }
    val localRegion = remember { DeviceLocale.region.uppercase() }
    val localLanguage = remember { DeviceLocale.language.uppercase() }
    var selectedProfile by remember { mutableStateOf(UserProfile.Traveler) }
	    val steps = listOf(
            OnboardingStep(
	                tag = ui("STEP 01 · LIVE RATES"),
	                title = ui("Fresh rates.\nAlways ready."),
	                body = ui("The app starts with your local base currency and keeps rates refreshed from the backend."),
                glyph = "⌖",
	                signal = "${ui("Local base")} · $localCurrency",
            ),
            OnboardingStep(
	                tag = ui("STEP 02 · FEES THAT MATTER"),
	                title = ui("See the cost\nbefore you send."),
	                body = ui("Compare estimated provider fees by amount and currency pair, then unlock deeper comparisons with Pro."),
                glyph = "⬢",
	                signal = "${ui("Converter")} · ${ui("fees")} · Pro",
            ),
            OnboardingStep(
	                tag = ui("STEP 03 · TRAVEL READY"),
	                title = ui("Your wallet\nfollows the map."),
	                body = ui("Auto-detect local currency on landing. Offline-safe last rates. Per-country tipping built in."),
                glyph = "◐",
	                signal = "${ui("Region")} · $localRegion",
            ),
            OnboardingStep(
	                tag = ui("STEP 04 · BACKUP"),
	                title = ui("Start private.\nRestore later."),
	                body = ui("A guest backup is created silently. You can connect Google on Android or Apple on iOS when you want portability."),
                glyph = "∞",
	                signal = "${ui("Language")} · $localLanguage",
            ),
	    )
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(localCurrency, variant = PillVariant.Ghost)
                    Text(
                        ui("Skip"),
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        modifier = Modifier
                            .clip(FxTheme.shapes.field)
                            .clickable(onClick = { onComplete(selectedProfile) })
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                key = { it },
            ) { page ->
                OnboardingPage(step = steps[page])
            }

            OnboardingProfilePicker(
                selectedProfile = selectedProfile,
                onProfileSelected = { selectedProfile = it },
            )

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
	                    text = if (pagerState.currentPage == steps.lastIndex) ui("Get started") else ui("Next  →"),
                    modifier = Modifier.width(if (pagerState.currentPage == steps.lastIndex) 154.dp else 126.dp),
                ) {
                    if (pagerState.currentPage == steps.lastIndex) {
                        onComplete(selectedProfile)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingProfilePicker(
    selectedProfile: UserProfile,
    onProfileSelected: (UserProfile) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FxTheme.colors.bg)
            .testTag("onboarding_profile_picker")
            .padding(top = 8.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(ui("Choose your focus"), color = FxTheme.colors.accent)
            Pill(ui(selectedProfile.copy().label), variant = PillVariant.Accent)
        }
        val rows = listOf(
            listOf(UserProfile.Traveler, UserProfile.CryptoHolder, UserProfile.Remittances),
            listOf(UserProfile.Freelancer, UserProfile.Savings),
        )
        rows.forEach { rowProfiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowProfiles.forEach { profile ->
                    val copy = profile.copy()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("onboarding_profile_${profile.name}")
                            .clip(FxTheme.shapes.field)
                            .background(if (selectedProfile == profile) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                            .border(
                                if (selectedProfile == profile) 1.dp else 0.dp,
                                if (selectedProfile == profile) FxTheme.colors.accentLine else Color.Transparent,
                                FxTheme.shapes.field,
                            )
                            .clickable { onProfileSelected(profile) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            ui(copy.label),
                            style = FxTheme.typography.caption,
                            color = if (selectedProfile == profile) FxTheme.colors.accent else FxTheme.colors.textDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (rowProfiles.size < 3) {
                    Spacer(Modifier.weight(1f))
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
        Spacer(Modifier.height(18.dp))
        OnboardingSignal(step.signal)
        Spacer(Modifier.weight(0.22f))
    }
}

@Composable
private fun OnboardingSignal(text: String) {
    Row(
        modifier = Modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveDot(Modifier.size(8.dp))
        Text(text, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
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
private fun GhostIconButton(
    icon: MoreFeatureIcon,
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit = {},
) {
    Row(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoreFeatureIconView(icon)
        Spacer(Modifier.width(8.dp))
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FxTheme.colors.accentSoft)
                .border(1.dp, FxTheme.colors.accentLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(glyph, style = FxTheme.typography.captionMono, color = FxTheme.colors.accent, textAlign = TextAlign.Center)
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
private fun OverlayChart(rates: List<FxRate>) {
    val border = FxTheme.colors.border
    val series = rates.map { rate -> rate.sparkline.normalizedPercentSeries() }
    val colors = rates.mapIndexed { index, rate -> compareOverlayColor(index, rate.kind) }
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
                val denominator = values.lastIndex.coerceAtLeast(1)
                val point = Offset(
                    x = (index.toFloat() / denominator) * size.width,
                    y = (1f - (value - min) / range) * size.height,
                )
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            drawPath(path, colors[seriesIndex], style = Stroke(width = 1.5f))
        }
    }
}

private fun List<Float>.normalizedPercentSeries(): List<Float> {
    val first = firstOrNull()?.takeIf { kotlin.math.abs(it) > 0.0000001f } ?: return this
    return map { ((it - first) / first) * 100f }
}

@Composable
private fun compareOverlayColor(index: Int, kind: CurrencyKind?): Color {
    val colors = listOf(FxTheme.colors.accent, FxTheme.colors.up, FxTheme.colors.down, FxTheme.colors.textDim)
    return if (kind == CurrencyKind.Crypto) FxTheme.colors.crypto else colors[index % colors.size]
}
