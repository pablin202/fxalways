package com.fxalways.app.screens.detail

import com.fxalways.app.screens.*
import com.fxalways.app.screens.more.MoreFeatureIcon
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
import com.fxalways.app.screens.news.StoryCard
import com.fxalways.app.screens.news.safeTestTagKey
import com.fxalways.app.screens.shared.ProUpsellCard
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
    val periodIsPro = period == Period.All
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
                        if (!effectivePremium && next == Period.All) {
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
                subtitle = ui("Free covers 1 year; Pro adds 5 years of history, full event context and deeper market overlays."),
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
