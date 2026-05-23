package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.PortfolioTransaction
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.importPortfolioCsv
import com.fxalways.app.screens.watchlist.WatchlistScreen
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserCanTrackUntilLimitThenLockedRowsOpenPaywall() {
        val harness = renderWatchlist(isPremium = false, initialWatchlist = Watchlist(codes = listOf("EUR", "GBP", "JPY")))

        compose.onNodeWithText("3/4 currencies · USD base").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_groups").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_group_tracking_only").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_group_fiat").assertIsDisplayed()
        compose.onNodeWithText("Reason: create an alert or amount next", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Reason: travel, income or savings", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("watchlist_currency_CHF").performScrollTo().performClick()
        compose.onNodeWithTag("watchlist_holding_CHF").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Track unlimited currencies").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("watchlist_currency_MXN").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
        compose.onAllNodesWithTag("watchlist_holding_MXN").assertCountEquals(0)
    }

    @Test
    fun freeUserCanRemoveTrackedCurrencyEvenAtLimit() {
        renderWatchlist(isPremium = false, initialWatchlist = Watchlist(codes = listOf("EUR", "GBP", "JPY", "CHF")))

        compose.onNodeWithText("4/4 currencies · USD base").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_currency_GBP").performScrollTo().performClick()

        compose.onAllNodesWithTag("watchlist_holding_GBP").assertCountEquals(0)
        compose.onNodeWithText("3/4 currencies · USD base").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proUserCanTrackBeyondFreeLimitWithoutUpsell() {
        renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR", "GBP", "JPY", "CHF")))

        compose.onNodeWithText("Unlimited currencies · USD base").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_group_fiat").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("watchlist_group_crypto").assertCountEquals(0)
        compose.onNodeWithTag("watchlist_currency_MXN").performScrollTo().performClick()

        compose.onNodeWithTag("watchlist_holding_MXN").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Track unlimited currencies").assertDoesNotExist()
    }

    @Test
    fun enteringHoldingUpdatesPortfolioValueAndClearingItReturnsToTrackingCopy() {
        val harness = renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR")))

        compose.onNodeWithText("1 tracked").assertIsDisplayed()
        compose.onNodeWithText("Add amounts below to value your portfolio.").assertIsDisplayed()

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("100")
        compose.runOnIdle { assertEquals(100.0, harness.holdings["EUR"]) }

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("")
        compose.runOnIdle { assertTrue("EUR" !in harness.holdings) }
        compose.onNodeWithText("Tracking live rate", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freePortfolioKeepsCostBasisAndInsightsLockedOut() {
        renderWatchlist(
            isPremium = false,
            initialWatchlist = Watchlist(codes = listOf("EUR"), holdings = mapOf("EUR" to 100.0)),
        )

        compose.onNodeWithText("holdings valued", substring = true).assertIsDisplayed()
        compose.onAllNodesWithTag("watchlist_cost_EUR").assertCountEquals(0)
        compose.onAllNodesWithTag("watchlist_portfolio_insights").assertCountEquals(0)
        compose.onAllNodesWithTag("watchlist_transactions").assertCountEquals(0)
    }

    @Test
    fun proPortfolioShowsCostBasisUnrealizedPnlAndAssetAllocation() {
        renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("EUR", "BTC"),
                holdings = mapOf("EUR" to 100.0, "BTC" to 1.0),
                holdingCosts = mapOf("EUR" to 1.0, "BTC" to 45_000.0),
            ),
        )

        compose.onNodeWithTag("watchlist_portfolio_insights").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_group_valued").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_group_crypto").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_unrealized_pnl").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_cost_basis").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_allocation").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_largest_position").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_concentration").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_scenario_down_5").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_daily_digest").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_action_plan").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_chart_range").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_portfolio_chart").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_cost_BTC").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proTransactionHistoryRecordsBuySellAndRealizedPnl() {
        val harness = renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("BTC"),
                holdings = mapOf("BTC" to 1.0),
                holdingCosts = mapOf("BTC" to 45_000.0),
            ),
        )

        compose.onNodeWithTag("watchlist_transactions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_transaction_amount").performTextReplacement("0.5")
        compose.onNodeWithTag("watchlist_transaction_price").performTextReplacement("40000")
        compose.onNodeWithTag("watchlist_transaction_record").performClick()

        compose.onNodeWithTag("watchlist_transaction_sell").performScrollTo().performClick()
        compose.onNodeWithTag("watchlist_transaction_amount").performTextReplacement("0.5")
        compose.onNodeWithTag("watchlist_transaction_price").performTextReplacement("50000")
        compose.onNodeWithTag("watchlist_transaction_record").performClick()

        compose.runOnIdle {
            assertEquals(1.0, harness.holdings["BTC"])
            assertTrue(kotlin.math.abs((harness.holdingCosts["BTC"] ?: 0.0) - 43_333.333) < 0.01)
            assertEquals(2, harness.transactions.size)
            assertTrue(kotlin.math.abs(harness.transactions.last().realizedPnlBase - 3_333.333) < 0.01)
        }
        compose.onAllNodesWithTag("watchlist_realized_pnl").assertCountEquals(1)
        compose.onNodeWithText("Sell BTC", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proTransactionInputsIgnoreInvalidZeroAndIncompleteValues() {
        val harness = renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("BTC"),
                holdings = mapOf("BTC" to 1.0),
                holdingCosts = mapOf("BTC" to 45_000.0),
            ),
        )

        compose.onNodeWithTag("watchlist_transactions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_transaction_record").performClick()
        compose.onNodeWithTag("watchlist_transaction_amount").performTextReplacement("0")
        compose.onNodeWithTag("watchlist_transaction_price").performTextReplacement("50000")
        compose.onNodeWithTag("watchlist_transaction_record").performClick()
        compose.onNodeWithTag("watchlist_transaction_amount").performTextReplacement("abc")
        compose.onNodeWithTag("watchlist_transaction_price").performTextReplacement("abc")
        compose.onNodeWithTag("watchlist_transaction_record").performClick()

        compose.runOnIdle {
            assertEquals(1.0, harness.holdings["BTC"])
            assertEquals(45_000.0, harness.holdingCosts["BTC"])
            assertEquals(0, harness.transactions.size)
        }
        compose.onNodeWithTag("watchlist_no_transactions").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proSellGreaterThanHoldingClampsTransactionAndClearsClosedPositionCostBasis() {
        val harness = renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("BTC"),
                holdings = mapOf("BTC" to 1.0),
                holdingCosts = mapOf("BTC" to 45_000.0),
            ),
        )

        compose.onNodeWithTag("watchlist_transactions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_transaction_sell").performClick()
        compose.onNodeWithTag("watchlist_transaction_amount").performTextReplacement("2")
        compose.onNodeWithTag("watchlist_transaction_price").performTextReplacement("50000")
        compose.onNodeWithTag("watchlist_transaction_record").performClick()

        compose.runOnIdle {
            assertTrue("BTC" !in harness.holdings)
            assertTrue("BTC" !in harness.holdingCosts)
            assertEquals(1, harness.transactions.size)
            assertEquals(1.0, harness.transactions.single().amount)
            assertEquals(5_000.0, harness.transactions.single().realizedPnlBase)
        }
        compose.onNodeWithText("Tracking live rate", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proTransactionAssetSelectorRecordsAgainstChosenHolding() {
        val harness = renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("BTC", "ETH"),
                holdings = mapOf("BTC" to 1.0, "ETH" to 1.0),
                holdingCosts = mapOf("BTC" to 45_000.0, "ETH" to 2_000.0),
            ),
        )

        compose.onNodeWithTag("watchlist_transactions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_transaction_asset_ETH").performScrollTo().performClick()
        compose.onNodeWithTag("watchlist_transaction_amount").performTextReplacement("2")
        compose.onNodeWithTag("watchlist_transaction_price").performTextReplacement("3000")
        compose.onNodeWithTag("watchlist_transaction_record").performClick()

        compose.runOnIdle {
            assertEquals(1.0, harness.holdings["BTC"])
            assertEquals(3.0, harness.holdings["ETH"])
            assertEquals(45_000.0, harness.holdingCosts["BTC"])
            assertTrue(kotlin.math.abs((harness.holdingCosts["ETH"] ?: 0.0) - 2_666.666) < 0.01)
            assertEquals("ETH", harness.transactions.single().code)
        }
        compose.onNodeWithText("Buy ETH", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proInitialTransactionHistoryRendersLatestRowsAndRealizedPnl() {
        renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("BTC"),
                holdings = mapOf("BTC" to 1.0),
                holdingCosts = mapOf("BTC" to 45_000.0),
                transactions = listOf(
                    PortfolioTransaction(
                        id = "old_buy",
                        code = "BTC",
                        type = PortfolioTransactionType.Buy,
                        amount = 1.0,
                        priceBase = 45_000.0,
                        createdAtMillis = 1_700_000_000_000L,
                    ),
                    PortfolioTransaction(
                        id = "latest_sell",
                        code = "BTC",
                        type = PortfolioTransactionType.Sell,
                        amount = 0.25,
                        priceBase = 50_000.0,
                        realizedPnlBase = 1_250.0,
                        createdAtMillis = 1_700_000_000_100L,
                    ),
                ),
            ),
        )

        compose.onNodeWithTag("watchlist_transactions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_transaction_latest_sell").assertIsDisplayed()
        compose.onNodeWithText("Sell BTC", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("watchlist_realized_pnl").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proCostBasisInputSanitizesAndUpdatesUnrealizedPnlWithoutMovingRows() {
        val harness = renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("EUR", "BTC"),
                holdings = mapOf("EUR" to 100.0, "BTC" to 1.0),
            ),
        )

        compose.onNodeWithTag("watchlist_cost_BTC").performScrollTo().performTextReplacement("40000abc")
        compose.onNodeWithTag("watchlist_cost_EUR").performScrollTo().performTextReplacement("1.10")

        compose.runOnIdle {
            assertEquals(40_000.0, harness.holdingCosts["BTC"])
            assertEquals(1.10, harness.holdingCosts["EUR"])
        }
        compose.onNodeWithTag("watchlist_holding_EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_holding_BTC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_portfolio_insights").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun holdingInputSanitizesNonNumericCharacters() {
        val harness = renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR")))

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("12abc,5")

        compose.runOnIdle { assertEquals(12.5, harness.holdings["EUR"]) }
    }

    @Test
    fun largeHoldingsForMultipleCurrenciesKeepRowsStableWhileEditing() {
        val harness = renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR", "GBP", "JPY")))

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("999999999")
        compose.onNodeWithTag("watchlist_amount_GBP").performScrollTo().performTextReplacement("888888888")
        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("777777777")

        compose.runOnIdle {
            assertEquals(777777777.0, harness.holdings["EUR"])
            assertEquals(888888888.0, harness.holdings["GBP"])
        }
        compose.onNodeWithTag("watchlist_holding_EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_holding_GBP").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_holding_JPY").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun tappingHoldingTitleOpensDetailForThatCurrency() {
        val harness = renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR", "GBP")))

        compose.onNodeWithTag("watchlist_detail_GBP").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(listOf("GBP"), harness.openedDetailCodes) }
    }

    @Test
    fun emptyWatchlistShowsEmptyStateAndCanStartTracking() {
        renderWatchlist(isPremium = false, initialWatchlist = Watchlist(codes = emptyList()))

        compose.onNodeWithText("Choose currencies below to start tracking.").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_currency_EUR").performScrollTo().performClick()

        compose.onNodeWithTag("watchlist_holding_EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1/4 currencies · USD base").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proPortfolioExportsCsvWithHoldingsAndTransactions() {
        renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(
                codes = listOf("EUR", "BTC"),
                holdings = mapOf("EUR" to 100.0, "BTC" to 1.0),
                holdingCosts = mapOf("EUR" to 1.05, "BTC" to 45_000.0),
                transactions = listOf(
                    PortfolioTransaction(
                        id = "btc_buy",
                        code = "BTC",
                        type = PortfolioTransactionType.Buy,
                        amount = 1.0,
                        priceBase = 45_000.0,
                        createdAtMillis = 1_700_000_000_000L,
                    ),
                ),
            ),
        )

        compose.onNodeWithTag("watchlist_import_export").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Portfolio CSV backup").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_export_summary").assertIsDisplayed()
        compose.onNodeWithText("Export CSV").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_copy_export_csv").assertIsDisplayed().performClick()
        compose.onNodeWithTag("watchlist_export_feedback").assertIsDisplayed()
        compose.onNodeWithTag("watchlist_import_summary").assertIsDisplayed()
        compose.onNodeWithText("HOLDING,BTC", substring = true).assertIsDisplayed()
        compose.onNodeWithText("TRANSACTION,BTC,BUY", substring = true).assertIsDisplayed()
    }

    @Test
    fun proPortfolioImportsCsvHoldingsAndTransactions() {
        val harness = renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR")))

        val csv = """
            record_type,code,type,amount,price_base,realized_pnl_base,created_at_millis,id
            HOLDING,BTC,,2,43000,,,
            TRANSACTION,BTC,BUY,2,43000,0,1700000000000,import_buy_btc
        """.trimIndent()
        compose.onNodeWithTag("watchlist_import_csv").performScrollTo().performTextReplacement(csv)
        compose.onNodeWithTag("watchlist_import_csv_button").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(2.0, harness.holdings["BTC"])
            assertEquals(43_000.0, harness.holdingCosts["BTC"])
            assertEquals("import_buy_btc", harness.transactions.single().id)
        }
        compose.onNodeWithTag("watchlist_import_feedback").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("watchlist_holding_BTC").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proPortfolioImportRejectsInvalidCsvWithoutChangingPortfolio() {
        val harness = renderWatchlist(
            isPremium = true,
            initialWatchlist = Watchlist(codes = listOf("BTC"), holdings = mapOf("BTC" to 1.0), holdingCosts = mapOf("BTC" to 45_000.0)),
        )

        compose.onNodeWithTag("watchlist_import_csv").performScrollTo().performTextReplacement("bad,row\nTRANSACTION,BTC,HOLD,abc,0")
        compose.onNodeWithTag("watchlist_import_csv_button").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(1.0, harness.holdings["BTC"])
            assertEquals(45_000.0, harness.holdingCosts["BTC"])
            assertEquals(0, harness.transactions.size)
        }
        compose.onNodeWithText("No valid portfolio rows found").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freePortfolioDoesNotShowCsvImportExportTools() {
        renderWatchlist(
            isPremium = false,
            initialWatchlist = Watchlist(codes = listOf("BTC"), holdings = mapOf("BTC" to 1.0)),
        )

        compose.onAllNodesWithTag("watchlist_import_export").assertCountEquals(0)
    }

    private fun renderWatchlist(
        isPremium: Boolean,
        initialWatchlist: Watchlist,
        liveState: LiveRatesState = testLiveRatesState(),
    ): WatchlistHarness {
        val harness = WatchlistHarness()
        harness.holdings = initialWatchlist.holdings
        harness.holdingCosts = initialWatchlist.holdingCosts
        harness.transactions += initialWatchlist.transactions
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var watchlist by remember { mutableStateOf(initialWatchlist) }

            FxTheme {
                WatchlistScreen(
                    liveState = liveState,
                    watchlistState = WatchlistState(watchlist),
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    onOpenPaywall = { harness.paywallClicks += 1 },
                    onToggleCurrency = { code ->
                        val selected = code in watchlist.codes
                        val canAdd = selected || isPremium || watchlist.codes.size < 4
                        if (!selected && !canAdd) {
                            harness.paywallClicks += 1
                        } else {
                            val nextCodes = if (selected) watchlist.codes.filterNot { it == code } else watchlist.codes + code
                            watchlist = watchlist.copy(
                                codes = nextCodes,
                                holdings = watchlist.holdings.filterKeys { it in nextCodes },
                                holdingCosts = watchlist.holdingCosts.filterKeys { it in nextCodes },
                                transactions = watchlist.transactions.filter { it.code in nextCodes },
                            )
                        }
                    },
                    onSetHolding = { code, amount ->
                        val nextHoldings = if (amount <= 0.0) watchlist.holdings - code else watchlist.holdings + (code to amount)
                        val nextCodes = if (code in watchlist.codes) watchlist.codes else (watchlist.codes + code).distinct()
                        watchlist = watchlist.copy(codes = nextCodes, holdings = nextHoldings)
                        harness.holdings = nextHoldings
                    },
                    onSetHoldingCost = { code, averageCost ->
                        val nextHoldingCosts = if (averageCost <= 0.0) {
                            watchlist.holdingCosts - code
                        } else {
                            watchlist.holdingCosts + (code to averageCost)
                        }
                        watchlist = watchlist.copy(holdingCosts = nextHoldingCosts)
                        harness.holdingCosts = nextHoldingCosts
                    },
                    onRecordTransaction = { code, type, amount, priceBase ->
                        val currentAmount = watchlist.holdings[code] ?: 0.0
                        val currentAverageCost = watchlist.holdingCosts[code] ?: 0.0
                        val id = "test_${harness.transactions.size}"
                        when (type) {
                            PortfolioTransactionType.Buy -> {
                                val nextAmount = currentAmount + amount
                                val nextAverageCost = ((currentAmount * currentAverageCost) + (amount * priceBase)) / nextAmount
                                val transaction = PortfolioTransaction(
                                    id = id,
                                    code = code,
                                    type = type,
                                    amount = amount,
                                    priceBase = priceBase,
                                    createdAtMillis = 1_700_000_000_000L + harness.transactions.size,
                                )
                                watchlist = watchlist.copy(
                                    codes = if (code in watchlist.codes) watchlist.codes else watchlist.codes + code,
                                    holdings = watchlist.holdings + (code to nextAmount),
                                    holdingCosts = watchlist.holdingCosts + (code to nextAverageCost),
                                    transactions = watchlist.transactions + transaction,
                                )
                                harness.holdings = watchlist.holdings
                                harness.holdingCosts = watchlist.holdingCosts
                                harness.transactions += transaction
                            }
                            PortfolioTransactionType.Sell -> {
                                val sellAmount = amount.coerceAtMost(currentAmount)
                                val realizedPnl = if (currentAverageCost > 0.0) (priceBase - currentAverageCost) * sellAmount else 0.0
                                val nextAmount = currentAmount - sellAmount
                                val transaction = PortfolioTransaction(
                                    id = id,
                                    code = code,
                                    type = type,
                                    amount = sellAmount,
                                    priceBase = priceBase,
                                    realizedPnlBase = realizedPnl,
                                    createdAtMillis = 1_700_000_000_000L + harness.transactions.size,
                                )
                                watchlist = watchlist.copy(
                                    holdings = if (nextAmount <= 0.0) watchlist.holdings - code else watchlist.holdings + (code to nextAmount),
                                    holdingCosts = if (nextAmount <= 0.0) watchlist.holdingCosts - code else watchlist.holdingCosts,
                                    transactions = watchlist.transactions + transaction,
                                )
                                harness.holdings = watchlist.holdings
                                harness.holdingCosts = watchlist.holdingCosts
                                harness.transactions += transaction
                            }
                        }
                    },
                    onImportPortfolioCsv = { csv ->
                        val result = watchlist.importPortfolioCsv(csv, nowMillis = 1_800_000_000_000L)
                        watchlist = result.watchlist
                        harness.holdings = watchlist.holdings
                        harness.holdingCosts = watchlist.holdingCosts
                        harness.transactions.clear()
                        harness.transactions += watchlist.transactions
                        result
                    },
                    onOpenDetail = { rate ->
                        harness.openedDetailCodes += rate.code
                    },
                )
            }
        }
        return harness
    }

    private fun testLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, -0.2, listOf(0.91f, 0.92f), "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.78, 0.1, listOf(0.77f, 0.78f), "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.0, 0.3, listOf(155f, 156f), "1 USD = 156.0000 JPY")
        val chf = FxRate("CHF", "Swiss Franc", "🇨🇭", CurrencyKind.Fiat, 0.83, -0.1, listOf(0.82f, 0.83f), "1 USD = 0.8300 CHF")
        val mxn = FxRate("MXN", "Mexican Peso", "🇲🇽", CurrencyKind.Fiat, 18.72, 0.2, listOf(18.6f, 18.72f), "1 USD = 18.7200 MXN")
        val btc = FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.00002, 2.0, listOf(0.000019f, 0.00002f), "1 USD = 0.00002 BTC")
        val eth = FxRate("ETH", "Ethereum", "Ξ", CurrencyKind.Crypto, 0.00025, -1.0, listOf(0.00026f, 0.00025f), "1 USD = 0.00025 ETH")
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy, chf, mxn),
            converter = listOf(usd, eur, gbp, jpy, chf, mxn),
            compare = listOf(eur, gbp, jpy, chf, mxn),
            allFiat = listOf(usd, eur, gbp, jpy, chf, mxn),
            crypto = listOf(btc, eth),
        )
    }

    private class WatchlistHarness {
        var paywallClicks = 0
        val openedDetailCodes = mutableListOf<String>()
        var holdings: Map<String, Double> = emptyMap()
        var holdingCosts: Map<String, Double> = emptyMap()
        val transactions = mutableListOf<PortfolioTransaction>()
    }
}
