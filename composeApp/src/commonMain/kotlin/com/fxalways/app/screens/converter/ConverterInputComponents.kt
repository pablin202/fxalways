package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
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
internal fun LocalRateNotebookCard(
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
internal fun PriceScannerCard(
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
internal fun FeeInputField(
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
