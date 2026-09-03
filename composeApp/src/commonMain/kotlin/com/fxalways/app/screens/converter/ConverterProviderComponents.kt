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
internal fun ProviderComparisonHistoryCard(
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

internal data class ProviderHistoryItem(
    val amountLabel: String,
    val provider: String,
    val recipientAmount: String,
    val loss: String,
    val effectiveRate: String,
)

internal fun providerComparisonHistory(
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
internal fun ProviderRecommendationCard(
    quote: EstimatedFeeQuote?,
    potentialSavings: Double,
    isPremium: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onOpenPaywall: () -> Unit,
) {
    val badge = when {
        isLoading -> "Refreshing"
        quote == null -> "Needs amount"
        quote.sourceStatus == "live" -> "Live quote"
        quote.sourceStatus == "comparison" -> "Market comparison"
        quote.sourceStatus == "partner_setup" -> "Partner setup"
        quote.sourceStatus == "unavailable" -> "Unsupported"
        else -> "Best estimate"
    }
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("PROVIDER RECOMMENDATION"), color = FxTheme.colors.accent)
                Pill(ui(badge), variant = if (quote?.sourceStatus == "live") PillVariant.Up else PillVariant.Ghost)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Best value"),
                    quote?.let { ui(it.provider) } ?: ui("None yet"),
                    quote?.amount ?: ui("Enter amount"),
                    Modifier.weight(1f).testTag("provider_reco_best_value"),
                )
                MetricTile(
                    ui("Potential savings"),
                    quote?.let { quote.amount.substringBefore(" ") + " " + formatMoneyValue(potentialSavings) } ?: "--",
                    quote?.let { ui(it.sourceStatusLabel()) },
                    Modifier.weight(1f).testTag("provider_reco_savings"),
                )
            }
            KeyValueRow(
                ui("Why this route"),
                quote?.let { ui(it.bestFor) } ?: ui("Enter an amount to compare real routes."),
                quote?.let { "${ui("Delivery")} ${ui(it.deliverySpeed)} · ${ui("Payment")} ${ui(it.paymentMethod)}" }
                    ?: ui("Provider rates can differ"),
                modifier = Modifier.testTag("provider_reco_reason"),
            )
            KeyValueRow(
                ui("Quote completeness"),
                quote?.let { ui(it.sourceStatusLabel()) } ?: ui("Needs amount"),
                quote?.let { ui(it.sourceMessage) } ?: ui("Compare providers before moving money."),
                modifier = Modifier.testTag("provider_reco_completeness"),
            )
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks every provider route and quote status."),
                    modifier = Modifier.fillMaxWidth().testTag("provider_reco_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
internal fun ProviderMatrixCard(
    quotes: List<EstimatedFeeQuote>,
    isLoading: Boolean,
    errorMessage: String?,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
    base: String = "",
    target: String = "",
    amountValue: Double = 0.0,
) {
    val visibleQuotes = quotes.take(if (isPremium) 6 else 2)
    val hasQuotes = visibleQuotes.isNotEmpty()
    LaunchedEffect(base, target, hasQuotes) {
        if (hasQuotes && base.isNotBlank() && target.isNotBlank()) {
            Observability.event(
                "provider_compare_viewed",
                mapOf("base" to base, "target" to target, "amount_bucket" to amountBucket(amountValue), "plan" to if (isPremium) "pro" else "free"),
            )
        }
    }
    BentoCard(Modifier.testTag("converter_provider_matrix"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            when {
                isLoading -> Text(ui("Refreshing provider quotes..."), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent, modifier = Modifier.testTag("provider_quotes_loading"))
                errorMessage != null -> Text(ui("Provider quotes unavailable; showing estimates."), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, modifier = Modifier.testTag("provider_quotes_error"))
            }
            if (visibleQuotes.isEmpty()) {
                Text(ui("Enter an amount to compare real routes."), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            visibleQuotes.forEachIndexed { index, quote ->
                KeyValueRow(
                    ui(quote.provider),
                    "${ui("Delivery")} ${ui(quote.deliverySpeed)} · ${ui(quote.sourceStatusLabel())}",
                    "${ui("Payment")} ${ui(quote.paymentMethod)} · ${ui("Source")} ${ui(quote.sourceTrustLabel())}",
                    modifier = Modifier.testTag("provider_matrix_row_$index"),
                )
                Text(
                    ui(quote.sourceMessage),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                    modifier = Modifier.fillMaxWidth().testTag("provider_matrix_source_note_$index"),
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
