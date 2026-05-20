package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchSmokeScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun launchSmokeWalksCoreReleaseScreens() {
        var route by mutableStateOf(SmokeRoute.Home)
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                when (route) {
                    SmokeRoute.Home -> DashboardScreen(
                        liveState = smokeLiveRatesState(),
                        subscriptionState = SubscriptionState(isPremium = false),
                        trackedCurrencyCodes = listOf("EUR", "GBP", "BTC"),
                        onRefresh = {},
                        onOpenPaywall = {},
                        onOpenDetail = {},
                        onEditFavorites = {},
                        onSeeAllCrypto = {},
                    )
                    SmokeRoute.Convert -> ConverterScreen(
                        liveState = smokeLiveRatesState(),
                        subscriptionState = SubscriptionState(isPremium = false),
                        selectedCurrencyCodes = listOf("EUR", "GBP", "BTC"),
                        onOpenPaywall = {},
                    )
                    SmokeRoute.Compare -> CompareScreen(
                        liveState = smokeLiveRatesState(),
                        subscriptionState = SubscriptionState(isPremium = false),
                        selectedCurrencyCodes = listOf("EUR", "GBP", "BTC"),
                        onOpenPaywall = {},
                        onOpenDetail = {},
                    )
                    SmokeRoute.Alerts -> AlertsScreen(
                        liveState = smokeLiveRatesState(),
                        alertsState = AlertsState(),
                        subscriptionState = SubscriptionState(isPremium = false),
                    )
                    SmokeRoute.More -> MoreScreen(
                        subscriptionState = SubscriptionState(isPremium = false),
                        alertsCount = 0,
                        watchlistCount = 3,
                        onOpenAlerts = {},
                        onOpenWatchlist = {},
                        onOpenTraveler = {},
                        onOpenSettings = {},
                        onOpenNews = {},
                        onOpenPaywall = {},
                    )
                    SmokeRoute.Paywall -> PaywallScreen(
                        subscriptionState = SubscriptionState(isPremium = false),
                    )
                }
            }
        }

        compose.onNodeWithText("Rates").assertIsDisplayed()
        compose.onNodeWithTag("dashboard_rate_trust").performScrollTo().assertIsDisplayed()

        compose.runOnIdle { route = SmokeRoute.Convert }
        compose.onNodeWithText("Convert").assertIsDisplayed()
        compose.onNodeWithTag("converter_rate_trust").performScrollTo().assertIsDisplayed()

        compose.runOnIdle { route = SmokeRoute.Compare }
        compose.onNodeWithText("Compare").assertIsDisplayed()
        compose.onNodeWithTag("compare_board").performScrollTo().assertIsDisplayed()

        compose.runOnIdle { route = SmokeRoute.Alerts }
        compose.onNodeWithText("Alerts").assertIsDisplayed()
        compose.onNodeWithText("SMART ALERTS").performScrollTo().assertIsDisplayed()

        compose.runOnIdle { route = SmokeRoute.More }
        compose.onNodeWithText("More").assertIsDisplayed()
        compose.onNodeWithText("Traveler").assertIsDisplayed()
        compose.onAllNodesWithText("COMING NEXT").assertCountEquals(0)

        compose.runOnIdle { route = SmokeRoute.Paywall }
        compose.onNodeWithTag("paywall_plan_Monthly").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_plan_Yearly").performScrollTo().assertIsDisplayed()
    }

    private enum class SmokeRoute {
        Home,
        Convert,
        Compare,
        Alerts,
        More,
        Paywall,
    }

    private fun smokeLiveRatesState(): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "USD", CurrencyKind.Fiat, 1.0, 0.0, List(36) { 1f }, "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "EUR", CurrencyKind.Fiat, 0.92, -0.2, List(36) { index -> 0.84f + index * 0.0023f }, "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "GBP", CurrencyKind.Fiat, 0.78, 0.1, List(36) { index -> 0.76f + index * 0.0006f }, "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "JPY", CurrencyKind.Fiat, 156.0, 0.3, List(36) { index -> 151f + index * 0.14f }, "1 USD = 156.0000 JPY")
        val btc = FxRate("BTC", "Bitcoin", "BTC", CurrencyKind.Crypto, 0.000015, 2.4, listOf(0.000014f, 0.000015f), "1 USD = 0.000015 BTC")
        val eth = FxRate("ETH", "Ethereum", "ETH", CurrencyKind.Crypto, 0.00024, 1.2, listOf(0.00023f, 0.00024f), "1 USD = 0.000240 ETH")
        val fiat = listOf(usd, eur, gbp, jpy)
        val crypto = listOf(btc, eth)
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy),
            converter = fiat + crypto,
            compare = listOf(eur, gbp, jpy, btc),
            crypto = crypto,
            allFiat = fiat,
        )
    }
}
