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
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertsScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserCreatesOneAlertThenNewAlertsOpenPaywall() {
        val harness = renderAlerts(isPremium = false)

        compose.onNodeWithText("0/1 alerts · USD base").assertIsDisplayed()
        compose.onNodeWithTag("alert_target_input").performScrollTo().performTextReplacement("0.95")
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()

        compose.onNodeWithTag("alert_feedback").assertIsDisplayed()
        compose.onNodeWithTag("alert_card_manual_0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Create unlimited alerts").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("alert_target_input").performScrollTo().performTextReplacement("0.96")
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, harness.paywallClicks) }

        compose.onNodeWithTag("alert_quick_GBP").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, harness.paywallClicks) }
    }

    @Test
    fun proUserCreatesTargetAndDailyMoveAlerts() {
        renderAlerts(isPremium = true)

        compose.onNodeWithText("Unlimited alerts · USD base").assertIsDisplayed()
        compose.onNodeWithTag("alert_currency_GBP").performScrollTo().performClick()
        compose.onNodeWithTag("alert_direction_Below").performScrollTo().performClick()
        compose.onNodeWithTag("alert_target_input").performScrollTo().performTextReplacement("0.75")
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()

        compose.onNodeWithTag("alert_card_manual_0").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("alert_kind_DailyChange").performScrollTo().performClick()
        compose.onNodeWithTag("alert_preset_+1%").performScrollTo().performClick()
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()

        compose.onNodeWithTag("alert_card_manual_1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("24H MOVE").assertIsDisplayed()
        compose.onNodeWithText("Create unlimited alerts").assertDoesNotExist()
    }

    @Test
    fun invalidCustomTargetShowsValidationAndDoesNotCreateAlert() {
        renderAlerts(isPremium = false)

        compose.onNodeWithTag("alert_target_input").performScrollTo().performTextReplacement("")
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()

        compose.onNodeWithTag("alert_target_error").assertIsDisplayed()
        compose.onNodeWithText("Enter a target above 0").assertIsDisplayed()
        compose.onAllNodesWithTag("alert_card_manual_0").assertCountEquals(0)
    }

    @Test
    fun freeAlertPairPickerShowsCoreCryptoButLocksExpandedCryptoCatalog() {
        renderAlerts(isPremium = false)

        compose.onNodeWithTag("alert_choose_pair").performScrollTo().performClick()
        compose.onNodeWithTag("currency_picker_search").performTextReplacement("SOL")

        compose.onAllNodesWithTag("currency_picker_SOL").assertCountEquals(0)

        compose.onNodeWithTag("currency_picker_search").performTextReplacement("BTC")
        compose.onNodeWithTag("currency_picker_BTC").assertIsDisplayed()
        compose.onNodeWithText("Crypto · Bitcoin").assertIsDisplayed()
        compose.onNodeWithTag("currency_picker_BTC").performClick()
        compose.onNodeWithTag("alert_currency_BTC").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun proAlertPairPickerSearchesExpandedCryptoAndCreatesAlert() {
        renderAlerts(isPremium = true)

        compose.onNodeWithTag("alert_choose_pair").performScrollTo().performClick()
        compose.onNodeWithTag("currency_picker_search").performTextReplacement("sol")
        compose.onNodeWithTag("currency_picker_SOL").assertIsDisplayed().performClick()

        compose.onNodeWithTag("alert_currency_SOL").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("alert_target_input").performScrollTo().performTextReplacement("0.0065")
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()

        compose.onNodeWithTag("alert_card_manual_0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("USD / SOL").assertIsDisplayed()
    }

    @Test
    fun activeAlertSupportsPauseTestAndDelete() {
        val harness = renderAlerts(
            isPremium = true,
            initialAlerts = listOf(
                PriceAlert(
                    id = "eur_target",
                    base = "USD",
                    quote = "EUR",
                    target = 0.95,
                    direction = AlertDirection.Above,
                    kind = AlertKind.Target,
                    enabled = true,
                    createdAtMillis = 1L,
                ),
            ),
        )

        compose.onNodeWithTag("alert_card_eur_target").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("alert_toggle_eur_target").performScrollTo().performClick()
        compose.onAllNodesWithText("paused").assertCountEquals(2)

        compose.onNodeWithTag("alert_test_eur_target").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("eur_target"), harness.testedAlertIds) }

        compose.onNodeWithTag("alert_delete_eur_target").performScrollTo().performClick()
        compose.onAllNodesWithTag("alert_card_eur_target").assertCountEquals(0)
        compose.onNodeWithText("NO ALERTS YET").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun pausedExistingQuickAlertCanBeResumedEvenWhenFreeLimitReached() {
        renderAlerts(
            isPremium = false,
            initialAlerts = listOf(
                PriceAlert(
                    id = "eur_quick",
                    base = "USD",
                    quote = "EUR",
                    target = 0.9292,
                    direction = AlertDirection.Above,
                    kind = AlertKind.Target,
                    enabled = false,
                    createdAtMillis = 1L,
                ),
            ),
        )

        compose.onNodeWithText("1/1 alerts · USD base").assertIsDisplayed()
        compose.onNodeWithTag("alert_quick_EUR").performScrollTo().performClick()

        compose.onNodeWithTag("alert_card_eur_quick").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("on").assertIsDisplayed()
    }

    @Test
    fun freeUserReactivatesMatchingPausedCustomAlertWithoutPaywallAtLimit() {
        val harness = renderAlerts(
            isPremium = false,
            initialAlerts = listOf(
                PriceAlert(
                    id = "eur_custom",
                    base = "USD",
                    quote = "EUR",
                    target = 0.9292,
                    direction = AlertDirection.Above,
                    kind = AlertKind.Target,
                    enabled = false,
                    createdAtMillis = 1L,
                ),
            ),
        )

        compose.onNodeWithText("1/1 alerts · USD base").assertIsDisplayed()
        compose.onNodeWithTag("alert_currency_EUR").performScrollTo().performClick()
        compose.onNodeWithText("Reactivate existing alert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("alert_create_button").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(0, harness.paywallClicks)
            assertEquals(1, harness.manualCreateCalls)
        }
        compose.onNodeWithTag("alert_card_eur_custom").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Existing alert reactivated USD/EUR.").assertIsDisplayed()
        compose.onNodeWithText("on").assertIsDisplayed()
    }

    @Test
    fun alertWithMissingLiveRateShowsWaitingState() {
        renderAlerts(
            isPremium = true,
            initialAlerts = listOf(
                PriceAlert(
                    id = "cad_missing",
                    base = "USD",
                    quote = "CAD",
                    target = 1.4,
                    direction = AlertDirection.Above,
                    kind = AlertKind.Target,
                    enabled = true,
                    createdAtMillis = 1L,
                ),
            ),
        )

        compose.onNodeWithTag("alert_card_cad_missing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("waiting for live rate", substring = true).assertIsDisplayed()
    }

    @Test
    fun activeAlertsShowHitAndDistanceStatesForTargetAndDailyMove() {
        renderAlerts(
            isPremium = true,
            initialAlerts = listOf(
                PriceAlert(
                    id = "eur_hit",
                    base = "USD",
                    quote = "EUR",
                    target = 0.90,
                    direction = AlertDirection.Above,
                    kind = AlertKind.Target,
                    enabled = true,
                    createdAtMillis = 1L,
                ),
                PriceAlert(
                    id = "gbp_move",
                    base = "USD",
                    quote = "GBP",
                    target = 0.5,
                    direction = AlertDirection.Above,
                    kind = AlertKind.DailyChange,
                    enabled = true,
                    createdAtMillis = 2L,
                    lastTriggeredAtMillis = 1_700_000_000_000L,
                ),
            ),
        )

        compose.onAllNodesWithTag("alert_card_eur_hit").assertCountEquals(1)
        compose.onAllNodesWithText("target hit", substring = true).assertCountEquals(1)
        compose.onAllNodesWithText("target reached").assertCountEquals(1)

        compose.onAllNodesWithTag("alert_card_gbp_move").assertCountEquals(1)
        compose.onAllNodesWithText("24H MOVE").assertCountEquals(1)
        compose.onAllNodesWithText("0.4 pts away", substring = true).assertCountEquals(1)
        compose.onAllNodesWithText("0.4 pts to move").assertCountEquals(1)
    }

    private fun renderAlerts(
        isPremium: Boolean,
        initialAlerts: List<PriceAlert> = emptyList(),
        liveState: LiveRatesState = testLiveRatesState(),
    ): AlertsHarness {
        val harness = AlertsHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var alerts by remember { mutableStateOf(initialAlerts) }
            var nextAlertIndex by remember { mutableStateOf(0) }

            FxTheme {
                AlertsScreen(
                    liveState = liveState,
                    alertsState = AlertsState(alerts),
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    onOpenPaywall = { harness.paywallClicks += 1 },
                    onCreateAlert = { rate ->
                        val id = "quick_${nextAlertIndex++}"
                        alerts = alerts + PriceAlert(
                            id = id,
                            base = liveState.baseCurrency,
                            quote = rate.code,
                            target = rate.rate * 1.01,
                            direction = AlertDirection.Above,
                            kind = AlertKind.Target,
                            enabled = true,
                            createdAtMillis = nextAlertIndex.toLong(),
                        )
                    },
                    onCreateManualAlert = { rate, direction, target, kind ->
                        harness.manualCreateCalls += 1
                        val existing = alerts.firstOrNull {
                            it.matchesDefinition(
                                base = liveState.baseCurrency,
                                quote = rate.code,
                                target = target,
                                direction = direction,
                                kind = kind,
                            )
                        }
                        if (existing != null) {
                            alerts = alerts.map {
                                if (it.id == existing.id) {
                                    it.copy(enabled = true, createdAtMillis = nextAlertIndex.toLong())
                                } else {
                                    it
                                }
                            }
                        } else {
                            val id = "manual_${nextAlertIndex++}"
                            alerts = alerts + PriceAlert(
                                id = id,
                                base = liveState.baseCurrency,
                                quote = rate.code,
                                target = target,
                                direction = direction,
                                kind = kind,
                                enabled = true,
                                createdAtMillis = nextAlertIndex.toLong(),
                            )
                        }
                    },
                    onResumeAlert = { id ->
                        alerts = alerts.map { if (it.id == id) it.copy(enabled = true) else it }
                    },
                    onToggleAlert = { id ->
                        alerts = alerts.map { if (it.id == id) it.copy(enabled = !it.enabled) else it }
                    },
                    onDeleteAlert = { id ->
                        alerts = alerts.filterNot { it.id == id }
                    },
                    onTestAlert = { alert ->
                        harness.testedAlertIds += alert.id
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
        val btc = FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.000015, 2.4, listOf(0.000014f, 0.000015f), "1 USD = 0.000015 BTC")
        val eth = FxRate("ETH", "Ethereum", "Ξ", CurrencyKind.Crypto, 0.00024, 1.2, listOf(0.00023f, 0.00024f), "1 USD = 0.000240 ETH")
        val usdt = FxRate("USDT", "Tether", "₮", CurrencyKind.Crypto, 1.0002, 0.01, listOf(1f, 1.0002f), "1 USD = 1.0002 USDT")
        val usdc = FxRate("USDC", "USD Coin", "$", CurrencyKind.Crypto, 0.9999, -0.01, listOf(1f, 0.9999f), "1 USD = 0.9999 USDC")
        val sol = FxRate("SOL", "Solana", "◎", CurrencyKind.Crypto, 0.00628, -1.14, listOf(0.0068f, 0.00628f), "1 USD = 0.006280 SOL")
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy),
            converter = listOf(usd, eur, gbp, jpy),
            compare = listOf(eur, gbp, jpy),
            crypto = listOf(btc, eth, usdt, usdc, sol),
            allFiat = listOf(usd, eur, gbp, jpy),
        )
    }

    private class AlertsHarness {
        var paywallClicks = 0
        var manualCreateCalls = 0
        val testedAlertIds = mutableListOf<String>()
    }
}

private fun PriceAlert.matchesDefinition(
    base: String,
    quote: String,
    target: Double,
    direction: AlertDirection,
    kind: AlertKind,
): Boolean =
    this.base == base &&
        this.quote == quote &&
        this.kind == kind &&
        this.direction == direction &&
        abs(this.target - target) < when (kind) {
            AlertKind.Target -> 0.0000001
            AlertKind.DailyChange -> 0.01
        }
