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
internal fun ConverterRow(
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
internal fun FeeComparisonRow(quote: EstimatedFeeQuote, rank: Int) {
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
                Text(
                    "${ui("Source")} ${ui(quote.sourceLabel)}",
                    style = FxTheme.typography.captionMono,
                    color = when (quote.sourceStatus) {
                        "live" -> FxTheme.colors.up
                        "partner_setup", "estimated" -> FxTheme.colors.accent
                        else -> FxTheme.colors.textFaint
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("fee_quote_source_${quote.provider}"),
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
internal fun FeeRealityCheckCard(quote: EstimatedFeeQuote?) {
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

internal data class FeeRealityVerdict(
    val label: String,
    val variant: PillVariant,
)

internal fun EstimatedFeeQuote.realityVerdict(): FeeRealityVerdict =
    when {
        lossPercentValue <= 0.01 -> FeeRealityVerdict("No markup", PillVariant.Up)
        lossPercentValue < 1.50 -> FeeRealityVerdict("Low cost", PillVariant.Up)
        lossPercentValue < 4.00 -> FeeRealityVerdict("Expensive", PillVariant.Accent)
        else -> FeeRealityVerdict("Avoid", PillVariant.Down)
    }
