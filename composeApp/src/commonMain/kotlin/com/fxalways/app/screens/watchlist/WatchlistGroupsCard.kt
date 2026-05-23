package com.fxalways.app.screens.watchlist

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.screens.portfolio.PortfolioHolding
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow

@Composable
internal fun WatchlistGroupsCard(
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
                        "${ui(group.subtitle)} · ${ui(group.reason)}",
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
    val reason: String,
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
            WatchlistDynamicGroup("valued", "Valued holdings", "Currencies with entered amounts", "Reason: portfolio impact", it)
        },
        trackingOnlyCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("tracking_only", "Tracking only", "No amount entered yet", "Reason: create an alert or amount next", it)
        },
        fiatCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("fiat", "Fiat exposure", "Tracked government currencies", "Reason: travel, income or savings", it)
        },
        cryptoCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("crypto", "Crypto exposure", "Tracked crypto assets and stablecoins", "Reason: volatility and allocation", it)
        },
        moverCodes.takeIf { it.isNotEmpty() }?.let {
            WatchlistDynamicGroup("movers", "Largest movers", "Sorted by absolute 24h move", "Reason: today's action trigger", it)
        },
    )
}
