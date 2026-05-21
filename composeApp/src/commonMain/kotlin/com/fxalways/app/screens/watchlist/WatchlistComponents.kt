package com.fxalways.app.screens.watchlist

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.PortfolioCsvImportResult
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.importPortfolioCsv
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.app.screens.portfolio.*
import com.fxalways.app.screens.shared.ProUpsellCard
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BigValueText
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.SparkLine
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun WatchlistScreen(
    liveState: com.fxalways.app.data.LiveRatesState,
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
                        KeyValueRow(ui("Unrealized P&L"), formatSignedMoney(portfolioUnrealizedPnl, liveState.baseCurrency), portfolioPnlPercentLabel(portfolioUnrealizedPnl, portfolioCostBasis), modifier = Modifier.testTag("watchlist_unrealized_pnl"))
                        KeyValueRow(ui("Realized P&L"), formatSignedMoney(portfolioRealizedPnl, liveState.baseCurrency), "${watchlistState.watchlist.transactions.size} ${ui("transactions")}", modifier = Modifier.testTag("watchlist_realized_pnl"))
                        KeyValueRow(ui("Total P&L"), formatSignedMoney(portfolioUnrealizedPnl + portfolioRealizedPnl, liveState.baseCurrency), ui("realized + unrealized"), modifier = Modifier.testTag("watchlist_total_pnl"))
                        KeyValueRow(ui("Cost basis"), "${liveState.baseCurrency} ${formatMoneyValue(portfolioCostBasis)}", ui("average cost per asset"), modifier = Modifier.testTag("watchlist_cost_basis"))
                        KeyValueRow(ui("Allocation"), "${ui("Fiat")} ${allocationLabel(fiatValue, portfolioValue)} · ${ui("Crypto")} ${allocationLabel(cryptoValue, portfolioValue)}", modifier = Modifier.testTag("watchlist_allocation"))
                        KeyValueRow(ui("Largest position"), largestHolding?.rate?.code ?: "—", largestHolding?.weightLabel(portfolioValue), modifier = Modifier.testTag("watchlist_largest_position"))
                        KeyValueRow(ui("Concentration"), largestHolding?.weightLabel(portfolioValue) ?: "0%", largestHolding?.let { "${it.rate.code} · ${ui("largest holding weight")}" }, modifier = Modifier.testTag("watchlist_concentration"))
                        KeyValueRow(ui("Scenario -5%"), formatSignedMoney(portfolioValue * -0.05, liveState.baseCurrency), ui("estimated portfolio shock"), modifier = Modifier.testTag("watchlist_scenario_down_5"))
                        KeyValueRow(ui("Daily digest"), largestDailyDriver?.dailyChangeLabel(liveState.baseCurrency) ?: "${liveState.baseCurrency} 0.00", largestDailyDriver?.let { "${it.rate.code} · ${ui("largest daily driver")}" }, modifier = Modifier.testTag("watchlist_daily_digest"))
                        KeyValueRow(ui("Action plan"), portfolioActionPlan(largestHolding, largestDailyDriver, portfolioDailyChange), ui("Review concentration before adding new exposure."), modifier = Modifier.testTag("watchlist_action_plan"))
                        if (portfolioSeries.size >= 2) {
                            KeyValueRow(ui("Chart range"), formatPortfolioSignedPercent(portfolioSeries.changePercent()), ui("estimated from tracked assets"), modifier = Modifier.testTag("watchlist_chart_range"))
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
