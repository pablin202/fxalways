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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.screens.traveler.TravelerScreen
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelerScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserSeesFocusedTravelerModeAndUpsell() {
        val harness = renderTraveler(isPremium = false, selectedCurrency = "JPY", budgetBase = 100.0)

        compose.onNodeWithText("TOKYO · JPY").assertIsDisplayed()
        compose.onNodeWithTag("traveler_hero").assertIsDisplayed()
        compose.onNodeWithTag("traveler_destination_JPY").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_offline_pack").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("DCC rule").assertIsDisplayed()
        compose.onNodeWithTag("traveler_cost_templates").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cost_template_0").assertIsDisplayed()
        compose.onNodeWithTag("traveler_cost_template_1").assertIsDisplayed()
        compose.onNodeWithTag("traveler_price_scanner").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("price_scanner_upsell").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("traveler_cost_template_2").assertCountEquals(0)
        compose.onNodeWithTag("traveler_cost_template_upsell").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cheat_sheet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cheat_1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cheat_20").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("traveler_cheat_50").assertCountEquals(0)
        compose.onNodeWithText("Unlock full traveler mode").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("traveler_more_destinations").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }
    }

    @Test
    fun proUserOpensDestinationPickerAndSelectsAnySupportedCurrency() {
        val harness = renderTraveler(isPremium = true, selectedCurrency = "JPY", budgetBase = 250.0)

        compose.onNodeWithTag("traveler_more_destinations").performScrollTo().performClick()
        compose.onNodeWithTag("currency_picker_AUD").performScrollTo().performClick()

        compose.onNodeWithText("SYDNEY · AUD").assertIsDisplayed()
        compose.runOnIdle { assertEquals("AUD", harness.selectedCurrency) }
    }

    @Test
    fun budgetInputParsesCommaDecimalsAndLargeValuesWithoutLosingState() {
        val harness = renderTraveler(isPremium = true, selectedCurrency = "JPY", budgetBase = 100.0)

        compose.onNodeWithTag("traveler_budget_input").performScrollTo().performTextReplacement("12,5")
        compose.runOnIdle { assertEquals(12.5, harness.budgetBase) }
        compose.onAllNodesWithText("3 days").assertCountEquals(2)
        compose.onNodeWithTag("traveler_days_increase").performScrollTo().performClick()
        compose.onAllNodesWithText("4 days").assertCountEquals(2)
        compose.onNodeWithTag("traveler_spend_plan").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("traveler_budget_input").performScrollTo().performTextReplacement("999999999")
        compose.runOnIdle { assertEquals(999999999.0, harness.budgetBase) }
        compose.onNodeWithTag("traveler_budget_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun changingDestinationKeepsBudgetAndUpdatesDestinationContext() {
        val harness = renderTraveler(isPremium = true, selectedCurrency = "EUR", budgetBase = 375.0)

        compose.onNodeWithTag("traveler_destination_GBP").performScrollTo().performClick()

        compose.onNodeWithText("LONDON · GBP").assertIsDisplayed()
        compose.onNodeWithTag("traveler_spend_plan").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals("GBP", harness.selectedCurrency)
            assertEquals(375.0, harness.budgetBase)
        }
    }

    @Test
    fun proTravelerShowsFullCheatSheetSpendPlanAndLocalContext() {
        renderTraveler(isPremium = true, selectedCurrency = "MXN", budgetBase = 500.0)

        compose.onNodeWithTag("traveler_cheat_500").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_spend_plan").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_offline_pack").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Decline conversion; pay in local currency.").assertIsDisplayed()
        compose.onNodeWithText("Local meals").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cost_templates").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cost_template_2").assertIsDisplayed()
        compose.onNodeWithTag("traveler_price_scanner").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("price_scanner_scan_button").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("price_scanner_share").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Business").assertIsDisplayed()
        compose.onAllNodesWithTag("traveler_cost_template_upsell").assertCountEquals(0)
        compose.onNodeWithTag("traveler_local_etiquette").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_payment_rails").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_price_guide").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun unsupportedDestinationStillRendersFallbackGuide() {
        renderTraveler(isPremium = true, selectedCurrency = "SGD", budgetBase = 200.0)

        compose.onNodeWithText("SGD · SGD").assertIsDisplayed()
        compose.onNodeWithTag("traveler_destination_SGD").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_price_guide").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun offlineSnapshotStillShowsTravelerTools() {
        renderTraveler(isPremium = true, selectedCurrency = "CHF", budgetBase = 150.0, liveState = testLiveRatesState(isLive = false))

        compose.onNodeWithText("Offline snapshot", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("traveler_offline_pack").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ready from cached rates").assertIsDisplayed()
        compose.onNodeWithTag("traveler_spend_plan").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("traveler_cheat_sheet").performScrollTo().assertIsDisplayed()
    }

    private fun renderTraveler(
        isPremium: Boolean,
        selectedCurrency: String,
        budgetBase: Double,
        liveState: LiveRatesState = testLiveRatesState(),
    ): TravelerHarness {
        val harness = TravelerHarness(selectedCurrency = selectedCurrency, budgetBase = budgetBase)
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var currency by remember { mutableStateOf(selectedCurrency) }
            var budget by remember { mutableStateOf(budgetBase) }

            FxTheme {
                TravelerScreen(
                    liveState = liveState,
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    selectedCurrency = currency,
                    budgetBase = budget,
                    onCurrencySelected = {
                        currency = it
                        harness.selectedCurrency = it
                    },
                    onBudgetChange = {
                        budget = it
                        harness.budgetBase = it
                    },
                    onOpenPaywall = { harness.paywallClicks += 1 },
                )
            }
        }
        return harness
    }

    private fun testLiveRatesState(isLive: Boolean = true): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, -0.2, listOf(0.91f, 0.92f), "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.78, 0.4, listOf(0.77f, 0.78f), "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.0, 1.8, listOf(155f, 156f), "1 USD = 156.0000 JPY")
        val chf = FxRate("CHF", "Swiss Franc", "🇨🇭", CurrencyKind.Fiat, 0.83, -1.1, listOf(0.82f, 0.83f), "1 USD = 0.8300 CHF")
        val mxn = FxRate("MXN", "Mexican Peso", "🇲🇽", CurrencyKind.Fiat, 18.72, 0.2, listOf(18.6f, 18.72f), "1 USD = 18.7200 MXN")
        val brl = FxRate("BRL", "Brazilian Real", "🇧🇷", CurrencyKind.Fiat, 5.12, 0.3, listOf(5.08f, 5.12f), "1 USD = 5.1200 BRL")
        val aud = FxRate("AUD", "Australian Dollar", "🇦🇺", CurrencyKind.Fiat, 1.52, 0.1, listOf(1.5f, 1.52f), "1 USD = 1.5200 AUD")
        val cad = FxRate("CAD", "Canadian Dollar", "🇨🇦", CurrencyKind.Fiat, 1.36, -0.1, listOf(1.35f, 1.36f), "1 USD = 1.3600 CAD")
        val sgd = FxRate("SGD", "Singapore Dollar", "🇸🇬", CurrencyKind.Fiat, 1.34, 0.05, listOf(1.33f, 1.34f), "1 USD = 1.3400 SGD")
        val fiat = listOf(usd, eur, gbp, jpy, chf, mxn, brl, aud, cad, sgd)
        return LiveRatesState(
            isLoading = false,
            isLive = isLive,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy, chf, mxn),
            converter = listOf(usd, eur, gbp, jpy, chf, mxn, brl, aud, cad, sgd),
            compare = listOf(eur, gbp, jpy, chf, mxn, brl, aud, cad, sgd),
            crypto = emptyList(),
            allFiat = fiat,
        )
    }

    private class TravelerHarness(
        var selectedCurrency: String,
        var budgetBase: Double,
    ) {
        var paywallClicks = 0
    }
}
