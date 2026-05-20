package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.LiveRatesState
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
class ConverterScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserSeesOnlyMidMarketAndCustomFeeQuotes() {
        val harness = renderConverter(isPremium = false)

        compose.onNodeWithTag("converter_rate_trust").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Indicative mid-market rates.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("FEES · USD → EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_fee_reality_check").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Best real-world route").assertIsDisplayed()
        compose.onNodeWithTag("converter_reality_recipient").assertIsDisplayed()
        compose.onNodeWithTag("converter_reality_loss").assertIsDisplayed()
        compose.onNodeWithText("REMITTANCE PLAN · USD → EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_remittance_planner").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("remittance_family_route").assertIsDisplayed()
        compose.onNodeWithTag("remittance_recipient_estimate").assertIsDisplayed()
        compose.onNodeWithTag("remittance_cadence_Monthly").assertIsDisplayed()
        compose.onAllNodesWithTag("remittance_cadence_Biweekly").assertCountEquals(0)
        compose.onNodeWithTag("remittance_upsell").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
        compose.onNodeWithTag("fee_quote_Mid-market").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("CUSTOM COST").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Custom").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Pro unlocks the complete provider list; estimates update with your amount.")
            .performScrollTo()
            .assertIsDisplayed()

        compose.onAllNodesWithTag("fee_quote_Wise").assertCountEquals(0)
        compose.onAllNodesWithTag("fee_quote_Revolut").assertCountEquals(0)
        compose.onAllNodesWithTag("fee_quote_Card payment").assertCountEquals(0)
        compose.onAllNodesWithTag("fee_quote_ATM cash").assertCountEquals(0)
        compose.onAllNodesWithTag("fee_quote_Bank transfer").assertCountEquals(0)
        compose.onAllNodesWithTag("fee_quote_Airport exchange").assertCountEquals(0)
    }

    @Test
    fun proUserSeesCompleteProviderComparison() {
        renderConverter(isPremium = true)

        compose.onNodeWithText("FEES · USD → EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Mid-market").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_best_provider").assertIsDisplayed()
        compose.onNodeWithTag("converter_provider_savings").assertIsDisplayed()
        compose.onNodeWithTag("converter_mid_market_value").assertIsDisplayed()
        compose.onNodeWithTag("converter_best_loss").assertIsDisplayed()
        compose.onNodeWithTag("converter_best_route").assertIsDisplayed()
        compose.onNodeWithTag("converter_fee_reality_check").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_reality_provider").assertIsDisplayed()
        compose.onNodeWithText("Low cost").assertIsDisplayed()
        compose.onNodeWithTag("converter_remittance_planner").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("remittance_cadence_Biweekly").assertIsDisplayed().performClick()
        compose.onNodeWithTag("remittance_recurring_amount").assertIsDisplayed()
        compose.onNodeWithText("Before payday").assertIsDisplayed()
        compose.onAllNodesWithTag("remittance_upsell").assertCountEquals(0)
        compose.onNodeWithTag("fee_quote_Wise").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Revolut").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Card payment").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_ATM cash").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Bank transfer").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Airport exchange").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Custom").performScrollTo().assertIsDisplayed()

        compose.onAllNodesWithText("Pro unlocks the complete provider list; estimates update with your amount.")
            .assertCountEquals(0)
    }

    @Test
    fun customInputsUpdateCustomCostInFreeMode() {
        renderConverter(isPremium = false)

        compose.onNodeWithTag("fee_input_Fixed fee").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("CUSTOM COST").assertIsDisplayed()
        compose.onNodeWithText("Fixed fee").assertIsDisplayed()
        compose.onNodeWithText("Fee %").assertIsDisplayed()
        compose.onNodeWithText("FX markup").assertIsDisplayed()

        compose.onNodeWithTag("fee_input_Fixed fee").performScrollTo().performTextReplacement("10")
        compose.onNodeWithTag("fee_input_Fee %").performScrollTo().performTextReplacement("5")
        compose.onNodeWithTag("fee_input_FX markup").performScrollTo().performTextReplacement("4")

        compose.onNodeWithText("Your custom cost").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Custom").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_provider_history").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freeUserSeesSmartTimingPreviewAndUpsellOnlyForLongerRanges() {
        val harness = renderConverter(isPremium = false)

        compose.onNodeWithTag("converter_smart_timing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_score").assertIsDisplayed()
        compose.onNodeWithText("Strong rate").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_7d").assertIsDisplayed()
        compose.onAllNodesWithTag("converter_timing_30d").assertCountEquals(0)
        compose.onAllNodesWithTag("converter_timing_90d").assertCountEquals(0)
        compose.onNodeWithTag("converter_timing_travel").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_upsell").performClick()

        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
    }

    @Test
    fun freeUserTracksLocalRateNotebookAndPreviewProviderHistory() {
        val harness = renderConverter(isPremium = false)

        compose.onNodeWithText("LOCAL RATE NOTEBOOK · USD → EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_local_rate_notebook").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("local_rate_official").assertIsDisplayed()
        compose.onNodeWithTag("local_rate_spread").assertIsDisplayed()
        compose.onNodeWithTag("fee_input_Local market").performScrollTo().performTextReplacement("1.05")
        compose.onNodeWithTag("local_rate_market").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("converter_provider_history").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("provider_history_row_0").assertIsDisplayed()
        compose.onNodeWithTag("provider_history_row_1").assertIsDisplayed()
        compose.onAllNodesWithTag("provider_history_row_2").assertCountEquals(0)
        compose.onNodeWithTag("provider_history_upsell").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
    }

    @Test
    fun proUserSeesFullProviderHistoryWithoutUpsell() {
        renderConverter(isPremium = true)

        compose.onNodeWithTag("converter_provider_history").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("provider_history_row_0").assertIsDisplayed()
        compose.onNodeWithTag("provider_history_row_1").assertIsDisplayed()
        compose.onNodeWithTag("provider_history_row_2").assertIsDisplayed()
        compose.onAllNodesWithTag("provider_history_upsell").assertCountEquals(0)
    }

    @Test
    fun proUserSeesFullSmartTimingHorizonsAndUseCaseRecommendations() {
        renderConverter(isPremium = true)

        compose.onNodeWithTag("converter_smart_timing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Strong rate").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_7d").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_30d").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_90d").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_travel").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_savings").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_remit").assertIsDisplayed()
        compose.onAllNodesWithTag("converter_timing_upsell").assertCountEquals(0)
    }

    @Test
    fun smartTimingUpdatesWhenPairIsReversed() {
        renderConverter(isPremium = true)

        compose.onNodeWithText("SMART TIMING · USD → EUR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("⇄  Reverse").performScrollTo().performClick()

        compose.onNodeWithText("SMART TIMING · EUR → USD").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_smart_timing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_score").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun smartTimingShowsWaitRecommendationForWeakRecentRange() {
        renderConverter(isPremium = true, liveState = fallingLiveRatesState())

        compose.onNodeWithTag("converter_smart_timing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Wait").assertIsDisplayed()
        compose.onNodeWithText("Wait or set an alert", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_7d").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_30d").assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_90d").assertIsDisplayed()
    }

    @Test
    fun smartTimingHandlesFlatSeriesWithoutMissingScore() {
        renderConverter(isPremium = true, liveState = flatLiveRatesState())

        compose.onNodeWithTag("converter_smart_timing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_score").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_7d").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_30d").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("converter_timing_90d").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reverseSwapsPairAndKeepsFeeCalculatorForNewPair() {
        renderConverter(isPremium = true)

        compose.onNodeWithText("⇄  Reverse").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("⇄  Reverse").assertIsDisplayed()
        compose.onNodeWithText("⇄  Reverse").performClick()

        compose.onNodeWithText("FEES · EUR → USD").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Mid-market").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Custom").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freeEditListShowsOnlyCoreCryptoAndLocksWhenLimitIsFull() {
        val harness = renderConverter(isPremium = false, selectedCodes = listOf("EUR", "GBP", "JPY", "CHF"))

        compose.onNodeWithTag("converter_edit_list").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_search").performTextReplacement("SOL")

        compose.onAllNodesWithTag("currency_list_SOL").assertCountEquals(0)

        compose.onNodeWithTag("currency_list_search").performTextReplacement("BTC")
        compose.onNodeWithTag("currency_list_BTC").assertIsDisplayed().performClick()

        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
    }

    @Test
    fun proEditListSearchesCryptoAndAddsItToConverterRows() {
        val harness = renderConverter(isPremium = true, selectedCodes = listOf("EUR"))

        compose.onNodeWithTag("converter_edit_list").performScrollTo().performClick()
        compose.onNodeWithTag("currency_list_search").performTextReplacement("sol")
        compose.onNodeWithTag("currency_list_SOL").assertIsDisplayed().performClick()
        compose.onNodeWithTag("currency_list_scroll").performTouchInput { swipeDown() }

        compose.runOnIdle { assertTrue("SOL" in harness.selectedCodes) }
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("converter_row_SOL").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("converter_row_SOL").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proEditListLabelsFiatCryptoAndStablecoinSearchResults() {
        renderConverter(isPremium = true, selectedCodes = listOf("EUR"))

        compose.onNodeWithTag("converter_edit_list").performScrollTo().performClick()
        compose.onNodeWithText("Fiat · Euro").assertIsDisplayed()

        compose.onNodeWithTag("currency_list_search").performTextReplacement("tether")
        compose.onNodeWithTag("currency_list_USDT").assertIsDisplayed()
        compose.onNodeWithText("Stablecoin · Tether").assertIsDisplayed()

        compose.onNodeWithTag("currency_list_search").performTextReplacement("")
        compose.onNodeWithTag("currency_list_scroll").performScrollToNode(hasTestTag("currency_list_BTC"))
        compose.onNodeWithTag("currency_list_BTC").assertIsDisplayed()
        compose.onNodeWithText("Crypto · Bitcoin").assertIsDisplayed()
    }

    @Test
    fun loadingConverterShowsSkeletonAndTrustStatus() {
        renderConverter(
            isPremium = false,
            liveState = testLiveRatesState().copy(isLoading = true, isLive = false, updatedLabel = "loading"),
        )

        compose.onNodeWithTag("converter_rate_trust").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("rate_trust_source_loading").assertCountEquals(1)
        compose.onAllNodesWithTag("rate_trust_updated_loading").assertCountEquals(1)
        compose.onAllNodesWithTag("rate_trust_source").assertCountEquals(0)
        compose.onAllNodesWithTag("rate_trust_updated").assertCountEquals(0)
        compose.onAllNodesWithTag("converter_loading_skeleton").assertCountEquals(1)
        compose.onAllNodesWithTag("converter_fee_loading_skeleton").assertCountEquals(1)
        compose.onAllNodesWithTag("converter_best_provider").assertCountEquals(0)
    }

    private fun renderConverter(
        isPremium: Boolean,
        selectedCodes: List<String> = listOf("EUR", "GBP", "JPY"),
        liveState: LiveRatesState = testLiveRatesState(),
    ): ConverterHarness {
        val harness = ConverterHarness(selectedCodes = selectedCodes)
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var codes by remember { mutableStateOf(selectedCodes) }

            FxTheme {
                ConverterScreen(
                    liveState = liveState,
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    selectedCurrencyCodes = codes,
                    onCurrencyCodesChange = {
                        codes = it
                        harness.selectedCodes = it
                    },
                    onOpenPaywall = { harness.paywallClicks += 1 },
                )
            }
        }
        return harness
    }

    private fun testLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, List(36) { 1f }, "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, -0.2, List(36) { index -> 0.84f + index * 0.0023f }, "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.78, 0.1, List(36) { index -> 0.76f + index * 0.0006f }, "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.0, 0.3, List(36) { index -> 151f + index * 0.14f }, "1 USD = 156.0000 JPY")
        val chf = FxRate("CHF", "Swiss Franc", "🇨🇭", CurrencyKind.Fiat, 0.83, -0.1, List(36) { index -> 0.82f + index * 0.0003f }, "1 USD = 0.8300 CHF")
        val btc = FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.000015, 2.4, listOf(0.000014f, 0.000015f), "1 USD = 0.000015 BTC")
        val eth = FxRate("ETH", "Ethereum", "Ξ", CurrencyKind.Crypto, 0.00024, 1.2, listOf(0.00023f, 0.00024f), "1 USD = 0.000240 ETH")
        val usdt = FxRate("USDT", "Tether", "₮", CurrencyKind.Crypto, 1.0002, 0.01, listOf(1f, 1.0002f), "1 USD = 1.0002 USDT")
        val usdc = FxRate("USDC", "USD Coin", "$", CurrencyKind.Crypto, 0.9999, -0.01, listOf(1f, 0.9999f), "1 USD = 0.9999 USDC")
        val sol = FxRate("SOL", "Solana", "◎", CurrencyKind.Crypto, 0.00628, -1.14, listOf(0.0068f, 0.00628f), "1 USD = 0.006280 SOL")
        val fiat = listOf(usd, eur, gbp, jpy, chf)
        val crypto = listOf(btc, eth, usdt, usdc, sol)
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy, chf),
            converter = fiat,
            compare = listOf(eur, gbp, jpy, chf),
            crypto = crypto,
            allFiat = fiat,
        )
    }

    private fun fallingLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, List(36) { 1f }, "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.84, -2.1, List(36) { index -> 0.92f - index * 0.0023f }, "1 USD = 0.8400 EUR")
        return testLiveRatesState().copy(
            favorites = listOf(eur),
            converter = listOf(usd, eur),
            compare = listOf(eur),
            allFiat = listOf(usd, eur),
        )
    }

    private fun flatLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, List(36) { 1f }, "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, 0.0, List(36) { 0.92f }, "1 USD = 0.9200 EUR")
        return testLiveRatesState().copy(
            favorites = listOf(eur),
            converter = listOf(usd, eur),
            compare = listOf(eur),
            allFiat = listOf(usd, eur),
        )
    }

    private class ConverterHarness(
        var selectedCodes: List<String>,
    ) {
        var paywallClicks = 0
    }
}
