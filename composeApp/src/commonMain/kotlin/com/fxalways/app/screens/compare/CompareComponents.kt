package com.fxalways.app.screens.compare

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.detail.EmptyDetailSection
import com.fxalways.app.screens.detail.LegendDot
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.OverlayChart
import com.fxalways.app.screens.detail.compareOverlayColor
import com.fxalways.app.screens.detail.compactRuntimeLabel
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.app.screens.shared.ProUpsellCard
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BentoTile
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SparkLine
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

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
                    Modifier
                        .weight(1f)
                        .height(76.dp)
                        .testTag("compare_metric_strongest")
                        .clickable(enabled = bestRate != null) { bestRate?.let(onOpenDetail) },
                )
                MetricTile(
                    ui("WEAKEST"),
                    weakestRate?.code ?: "--",
                    weakestRate?.let { formatChange(it.change24h) } ?: ui("No data"),
                    Modifier
                        .weight(1f)
                        .height(76.dp)
                        .testTag("compare_metric_weakest")
                        .clickable(enabled = weakestRate != null) { weakestRate?.let(onOpenDetail) },
                )
            }
            BentoCard(
                Modifier
                    .fillMaxWidth()
                    .testTag("compare_board"),
                padding = 12.dp,
            ) {
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
                    title = ui("Unlock the full crypto catalog"),
                    subtitle = ui("Free compares every fiat currency and core crypto; Pro adds up to 200 crypto assets and advanced overlays."),
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

private fun compareTargetCodes(
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
