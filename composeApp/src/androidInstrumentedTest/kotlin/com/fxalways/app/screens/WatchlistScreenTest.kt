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
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
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
        compose.onNodeWithTag("watchlist_currency_MXN").performScrollTo().performClick()

        compose.onNodeWithTag("watchlist_holding_MXN").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Track unlimited currencies").assertDoesNotExist()
    }

    @Test
    fun enteringHoldingUpdatesPortfolioValueAndClearingItReturnsToTrackingCopy() {
        renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR")))

        compose.onNodeWithText("1 tracked").assertIsDisplayed()
        compose.onNodeWithText("Add amounts below to value your portfolio.").assertIsDisplayed()

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("100")
        compose.onNodeWithTag("watchlist_summary").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("holdings valued", substring = true).assertIsDisplayed()

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("")
        compose.onNodeWithText("Tracking live rate", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Add amounts below to value your portfolio.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun holdingInputSanitizesNonNumericCharacters() {
        val harness = renderWatchlist(isPremium = true, initialWatchlist = Watchlist(codes = listOf("EUR")))

        compose.onNodeWithTag("watchlist_amount_EUR").performScrollTo().performTextReplacement("12abc,5")

        compose.runOnIdle { assertEquals(12.5, harness.holdings["EUR"]) }
        compose.onNodeWithText("holdings valued", substring = true).assertIsDisplayed()
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

    private fun renderWatchlist(
        isPremium: Boolean,
        initialWatchlist: Watchlist,
        liveState: LiveRatesState = testLiveRatesState(),
    ): WatchlistHarness {
        val harness = WatchlistHarness()
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
                            )
                        }
                    },
                    onSetHolding = { code, amount ->
                        val nextHoldings = if (amount <= 0.0) watchlist.holdings - code else watchlist.holdings + (code to amount)
                        val nextCodes = if (code in watchlist.codes) watchlist.codes else (watchlist.codes + code).distinct()
                        watchlist = watchlist.copy(codes = nextCodes, holdings = nextHoldings)
                        harness.holdings = nextHoldings
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
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy, chf, mxn),
            converter = listOf(usd, eur, gbp, jpy, chf, mxn),
            compare = listOf(eur, gbp, jpy, chf, mxn),
            allFiat = listOf(usd, eur, gbp, jpy, chf, mxn),
        )
    }

    private class WatchlistHarness {
        var paywallClicks = 0
        val openedDetailCodes = mutableListOf<String>()
        var holdings: Map<String, Double> = emptyMap()
    }
}
