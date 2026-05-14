package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserSeesOnlyMidMarketAndCustomFeeQuotes() {
        renderConverter(isPremium = false)

        compose.onNodeWithText("FEES · USD → EUR").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Mid-market").assertCountEquals(2)
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
        compose.onAllNodesWithText("Mid-market").assertCountEquals(2)
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

        compose.onNodeWithText("CUSTOM COST").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fixed fee").assertIsDisplayed()
        compose.onNodeWithText("Fee %").assertIsDisplayed()
        compose.onNodeWithText("FX markup").assertIsDisplayed()

        compose.onNodeWithTag("fee_input_Fixed fee").performScrollTo().performTextReplacement("10")
        compose.onNodeWithTag("fee_input_Fee %").performScrollTo().performTextReplacement("5")
        compose.onNodeWithTag("fee_input_FX markup").performScrollTo().performTextReplacement("4")

        compose.onNodeWithText("Your custom cost").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("fee_quote_Custom").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Lost", substring = true).assertCountEquals(2)
    }

    @Test
    fun reverseSwapsPairAndKeepsFeeCalculatorForNewPair() {
        renderConverter(isPremium = true)

        compose.onNodeWithText("⇄  Reverse").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("⇄  Reverse").assertIsDisplayed()
        compose.onNodeWithText("⇄  Reverse").performClick()

        compose.onNodeWithText("FEES · EUR → USD").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Mid-market").assertCountEquals(2)
        compose.onNodeWithTag("fee_quote_Custom").performScrollTo().assertIsDisplayed()
    }

    private fun renderConverter(isPremium: Boolean) {
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                ConverterScreen(
                    liveState = testLiveRatesState(),
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    selectedCurrencyCodes = listOf("EUR", "GBP", "JPY"),
                    onCurrencyCodesChange = {},
                    onOpenPaywall = {},
                )
            }
        }
    }

    private fun testLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, -0.2, listOf(0.91f, 0.92f), "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.78, 0.1, listOf(0.77f, 0.78f), "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.0, 0.3, listOf(155f, 156f), "1 USD = 156.0000 JPY")
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy),
            converter = listOf(usd, eur, gbp, jpy),
            compare = listOf(eur, gbp, jpy),
            allFiat = listOf(usd, eur, gbp, jpy),
        )
    }
}
