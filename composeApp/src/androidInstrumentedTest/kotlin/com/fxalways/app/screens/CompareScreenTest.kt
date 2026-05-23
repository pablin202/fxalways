package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.screens.compare.CompareScreen
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
class CompareScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserSeesLimitedBoardAndUpsell() {
        renderCompare(isPremium = false, selectedCodes = listOf("EUR", "GBP", "JPY", "CHF", "MXN", "BTC"))

        compose.onNodeWithTag("compare_board").assertIsDisplayed()
        compose.onAllNodesWithTag("compare_tile_EUR").assertCountEquals(1)
        compose.onAllNodesWithTag("compare_tile_GBP").assertCountEquals(1)
        compose.onAllNodesWithTag("compare_tile_JPY").assertCountEquals(1)
        compose.onAllNodesWithTag("compare_tile_CHF").assertCountEquals(1)
        compose.onAllNodesWithTag("compare_tile_MXN").assertCountEquals(0)
        compose.onAllNodesWithTag("compare_tile_BTC").assertCountEquals(0)
        compose.onNodeWithText("Compare every tracked currency").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proUserSeesFullBoardIncludingCryptoWithoutUpsell() {
        renderCompare(isPremium = true, selectedCodes = listOf("EUR", "GBP", "JPY", "CHF", "MXN", "BTC"))

        compose.onNodeWithTag("compare_board").assertIsDisplayed()
        compose.onNodeWithTag("compare_tile_BTC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 crypto", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("compare_overlay").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("compare_tile_BTC").assertCountEquals(1)
    }

    @Test
    fun sortModesReorderTilesByStrongestAndWeakestMoves() {
        val harness = renderCompare(isPremium = true, selectedCodes = listOf("EUR", "GBP", "JPY", "CHF"))

        compose.onNodeWithTag("compare_sort_Strongest").performClick()
        compose.onNodeWithTag("compare_open_strongest").performScrollTo().performClick()

        compose.onNodeWithTag("compare_sort_Weakest").performClick()
        compose.onNodeWithTag("compare_tile_CHF").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(listOf("JPY", "CHF"), harness.openedDetailCodes) }
    }

    @Test
    fun tileAndOpenStrongestNavigateToDetail() {
        val harness = renderCompare(isPremium = true, selectedCodes = listOf("EUR", "GBP", "JPY"))

        compose.onNodeWithTag("compare_tile_GBP").performScrollTo().performClick()
        compose.onNodeWithTag("compare_open_strongest").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(listOf("GBP", "JPY"), harness.openedDetailCodes) }
    }

    @Test
    fun freeEditSheetLocksAdditionalCurrenciesAndOpensPaywall() {
        val harness = renderCompare(isPremium = false, selectedCodes = listOf("EUR", "GBP", "JPY", "CHF"))

        compose.onNodeWithTag("compare_edit_button").performScrollTo().performClick()
        compose.onNodeWithText("Edit comparison").assertIsDisplayed()
        compose.onNodeWithTag("currency_list_MXN").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
    }

    @Test
    fun proEditSheetAppliesExpandedSelection() {
        val harness = renderCompare(isPremium = true, selectedCodes = listOf("EUR", "GBP"))

        compose.onNodeWithTag("compare_edit_button").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_JPY").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_apply").assertIsDisplayed().performClick()

        compose.runOnIdle { assertEquals(listOf("EUR", "GBP", "CHF", "MXN"), harness.selectedCodes) }
    }

    @Test
    fun proEditSheetAddsCryptoAndBoardShowsIt() {
        val baseState = testLiveRatesState()
        val usd = baseState.allFiat.first { it.code == "USD" }
        val eur = baseState.allFiat.first { it.code == "EUR" }
        val harness = renderCompare(
            isPremium = true,
            selectedCodes = listOf("EUR"),
            liveState = baseState.copy(
                compare = listOf(eur),
                favorites = emptyList(),
                converter = listOf(usd, eur),
                allFiat = listOf(usd, eur),
            ),
        )

        compose.onNodeWithTag("compare_edit_button").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_SOL").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_apply").assertIsDisplayed().performClick()

        compose.onNodeWithTag("compare_tile_SOL").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertTrue("SOL" in harness.selectedCodes) }
    }

    @Test
    fun proEditSheetDismissAppliesDraftSelection() {
        val harness = renderCompare(isPremium = true, selectedCodes = listOf("EUR"))

        compose.onNodeWithTag("compare_edit_button").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_SOL").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_scroll").performTouchInput { swipeDown() }

        compose.onNodeWithTag("compare_tile_SOL").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertTrue("SOL" in harness.selectedCodes) }
    }

    @Test
    fun proEditSheetCancelDiscardsDraftSelection() {
        val harness = renderCompare(isPremium = true, selectedCodes = listOf("EUR"))

        compose.onNodeWithTag("compare_edit_button").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_SOL").performScrollTo().performClick()
        compose.onNodeWithText("Cancel").performClick()

        compose.onAllNodesWithTag("compare_tile_SOL").assertCountEquals(0)
        compose.runOnIdle { assertEquals(listOf("EUR"), harness.selectedCodes) }
    }

    @Test
    fun editSheetLabelsFiatCryptoAndStablecoinRows() {
        renderCompare(isPremium = true, selectedCodes = listOf("EUR"))

        compose.onNodeWithTag("compare_edit_button").performScrollTo().performClick()

        compose.onNodeWithText("Fiat · Euro").assertIsDisplayed()
        compose.onAllNodesWithTag("currency_list_BTC").assertCountEquals(1)
        compose.onAllNodesWithText("Crypto · Bitcoin").assertCountEquals(1)
        compose.onNodeWithTag("currency_list_scroll").performScrollToNode(hasTestTag("currency_list_USDT"))
        compose.onNodeWithTag("currency_list_USDT").assertIsDisplayed()
        compose.onNodeWithText("Stablecoin · Tether").assertIsDisplayed()
    }

    @Test
    fun unavailableSavedCodesFallBackToDefaultComparison() {
        renderCompare(isPremium = false, selectedCodes = listOf("XXX", "YYY"))

        compose.onAllNodesWithTag("compare_tile_EUR").assertCountEquals(1)
    }

    private fun renderCompare(
        isPremium: Boolean,
        selectedCodes: List<String>,
        liveState: LiveRatesState = testLiveRatesState(),
    ): CompareHarness {
        val harness = CompareHarness(selectedCodes = selectedCodes)
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var codes by remember { mutableStateOf(selectedCodes) }

            FxTheme {
                CompareScreen(
                    liveState = liveState,
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    selectedCurrencyCodes = codes,
                    onCurrencyCodesChange = {
                        codes = it
                        harness.selectedCodes = it
                    },
                    onOpenPaywall = { harness.paywallClicks += 1 },
                    onOpenDetail = { rate -> harness.openedDetailCodes += rate.code },
                )
            }
        }
        return harness
    }

    private fun testLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, -0.2, listOf(0.91f, 0.92f), "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.78, 0.4, listOf(0.77f, 0.78f), "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.0, 1.8, listOf(155f, 156f), "1 USD = 156.0000 JPY")
        val chf = FxRate("CHF", "Swiss Franc", "🇨🇭", CurrencyKind.Fiat, 0.83, -1.1, listOf(0.82f, 0.83f), "1 USD = 0.8300 CHF")
        val mxn = FxRate("MXN", "Mexican Peso", "🇲🇽", CurrencyKind.Fiat, 18.72, 0.2, listOf(18.6f, 18.72f), "1 USD = 18.7200 MXN")
        val btc = FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.000015, 2.4, listOf(0.000014f, 0.000015f), "1 USD = 0.000015 BTC")
        val usdt = FxRate("USDT", "Tether", "₮", CurrencyKind.Crypto, 1.0002, 0.01, listOf(1f, 1.0002f), "1 USD = 1.0002 USDT")
        val sol = FxRate("SOL", "Solana", "◎", CurrencyKind.Crypto, 0.00628, -1.14, listOf(0.0068f, 0.00628f), "1 USD = 0.006280 SOL")
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy, chf, mxn),
            converter = listOf(usd, eur, gbp, jpy, chf, mxn),
            compare = listOf(eur, gbp, jpy, chf, mxn),
            crypto = listOf(btc, usdt, sol),
            allFiat = listOf(usd, eur, gbp, jpy, chf, mxn),
        )
    }

    private class CompareHarness(
        var selectedCodes: List<String>,
    ) {
        var paywallClicks = 0
        val openedDetailCodes = mutableListOf<String>()
    }
}
