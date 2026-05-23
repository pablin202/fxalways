package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.UserProfile
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.screens.alerts.QuickAlertState
import com.fxalways.app.screens.dashboard.DashboardScreen
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeDashboardShowsCryptoSnapshotAndFocusedAssetList() {
        val harness = renderDashboard(isPremium = false)

        compose.onNodeWithTag("dashboard_crypto_header").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_rate_trust").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_trust_details").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_best_next_action").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_best_action_convert").assertIsDisplayed()
        compose.onNodeWithTag("dashboard_best_action_alert").assertIsDisplayed()
        compose.onNodeWithTag("dashboard_best_action_providers").assertIsDisplayed()
        compose.onNodeWithTag("dashboard_retention_loop").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("retention_loop_watch").assertIsDisplayed()
        compose.onNodeWithTag("retention_loop_proof").assertIsDisplayed()
        compose.onNodeWithTag("retention_loop_reason").assertIsDisplayed()
        compose.onNodeWithText("Indicative mid-market rates.", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_snapshot").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_stablecoins").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_avg").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_BTC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_ETH").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_USDT").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_USDC").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("dashboard_crypto_SOL").assertCountEquals(0)
        compose.onNodeWithTag("dashboard_crypto_upsell").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("dashboard_crypto_see_all").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, harness.seeAllCryptoClicks) }
    }

    @Test
    fun proDashboardShowsStablecoinsAndOpensCryptoDetail() {
        val harness = renderDashboard(isPremium = true)

        compose.onNodeWithTag("dashboard_crypto_USDT").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_USDC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_ETH").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(listOf("ETH"), harness.openedDetailCodes) }
    }

    @Test
    fun proDashboardAddsTrackedCryptoBeyondCoreList() {
        renderDashboard(isPremium = true, trackedCurrencyCodes = listOf("SOL"))

        compose.onNodeWithTag("dashboard_crypto_BTC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_crypto_SOL").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freeDashboardKeepsTrackedCryptoLockedToCoreList() {
        renderDashboard(isPremium = false, trackedCurrencyCodes = listOf("SOL"))

        compose.onNodeWithTag("dashboard_crypto_BTC").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("dashboard_crypto_SOL").assertCountEquals(0)
    }

    @Test
    fun emptyCryptoStateShowsExplicitEmptyCard() {
        renderDashboard(isPremium = true, liveState = testLiveRatesState(crypto = emptyList()))

        compose.onNodeWithTag("dashboard_crypto_empty").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("dashboard_crypto_BTC").assertCountEquals(0)
    }

    @Test
    fun loadingDashboardShowsSkeletonAndTrustStatus() {
        renderDashboard(
            isPremium = false,
            liveState = testLiveRatesState().copy(isLoading = true, isLive = false, updatedLabel = "loading"),
        )

        compose.onNodeWithTag("dashboard_rate_trust").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithTag("rate_trust_source_loading").assertCountEquals(1)
        compose.onAllNodesWithTag("rate_trust_updated_loading").assertCountEquals(1)
        compose.onAllNodesWithTag("rate_trust_source").assertCountEquals(0)
        compose.onAllNodesWithTag("rate_trust_updated").assertCountEquals(0)
        compose.onAllNodesWithTag("trust_details_loading_skeleton").assertCountEquals(1)
        compose.onAllNodesWithTag("trust_decision_grade").assertCountEquals(0)
        compose.onAllNodesWithTag("trust_provider_disclaimer").assertCountEquals(0)
        compose.onAllNodesWithTag("dashboard_loading_skeleton").assertCountEquals(1)
        compose.onAllNodesWithTag("dashboard_market_loading_skeleton").assertCountEquals(1)
        compose.onAllNodesWithTag("dashboard_profile_card").assertCountEquals(0)
        compose.onAllNodesWithTag("dashboard_crypto_snapshot").assertCountEquals(0)
    }

    @Test
    fun freeDashboardShowsPersonalizedProfileCard() {
        val harness = renderDashboard(isPremium = false, userProfile = UserProfile.Remittances)

        compose.onNodeWithText("Send money smarter").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_free_focus").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_pro_focus").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_pair").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_alert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_action").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_workflow").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_workflow_primary").assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_alert_action").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Mid-market + custom cost").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Full provider comparison + alerts").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("USD → MXN").assertCountEquals(3)
        compose.onNodeWithText("Target rate above last 7d average").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Create suggested alert").assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_alert_action").performClick()

        compose.runOnIdle { assertEquals(1, harness.suggestedAlertClicks) }
    }

    @Test
    fun travelerProfileActionRoutesToTravelerWorkflow() {
        val travelerHarness = renderDashboard(isPremium = true, userProfile = UserProfile.Traveler)
        compose.onNodeWithTag("dashboard_profile_action_button").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, travelerHarness.travelerClicks) }
    }

    @Test
    fun providerProfileActionRoutesToConverterWorkflow() {
        val harness = renderDashboard(isPremium = true, userProfile = UserProfile.Remittances)
        compose.onNodeWithTag("dashboard_profile_action_button").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, harness.converterClicks)
            assertEquals(0, harness.watchlistClicks)
        }
    }

    @Test
    fun proDashboardShowsProProfileState() {
        val harness = renderDashboard(isPremium = true, userProfile = UserProfile.CryptoHolder, suggestedProfileAlertState = QuickAlertState.Active)

        compose.onNodeWithText("Crypto portfolio focus").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_free_focus").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_pro_focus").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_pair").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_alert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("BTC, ETH, USDT, USDC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Expanded crypto catalog + holdings").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("USD → BTC").assertCountEquals(2)
        compose.onNodeWithText("BTC/ETH daily move above 3%").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Suggested alert active").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_alert_action").performScrollTo().performClick()
        compose.onAllNodesWithText("Pro").assertCountEquals(2)

        compose.runOnIdle { assertEquals(1, harness.suggestedAlertClicks) }
    }

    @Test
    fun profileSuggestedAlertCanShowLockedState() {
        val harness = renderDashboard(
            isPremium = false,
            userProfile = UserProfile.CryptoHolder,
            suggestedProfileAlertState = QuickAlertState.Locked,
        )

        compose.onNodeWithText("Crypto portfolio focus").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Unlock suggested alert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_alert_action").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(1, harness.suggestedAlertClicks) }
    }

    private fun renderDashboard(
        isPremium: Boolean,
        trackedCurrencyCodes: List<String> = emptyList(),
        userProfile: UserProfile = UserProfile.Traveler,
        suggestedProfileAlertState: QuickAlertState? = QuickAlertState.Create,
        liveState: LiveRatesState = testLiveRatesState(),
    ): DashboardHarness {
        val harness = DashboardHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                DashboardScreen(
                    liveState = liveState,
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    trackedCurrencyCodes = trackedCurrencyCodes,
                    userProfile = userProfile,
                    suggestedProfileAlertState = suggestedProfileAlertState,
                    onRefresh = { harness.refreshClicks += 1 },
                    onOpenPaywall = { harness.paywallClicks += 1 },
                    onOpenDetail = { harness.openedDetailCodes += it.code },
                    onEditFavorites = { harness.editFavoritesClicks += 1 },
                    onSeeAllCrypto = { harness.seeAllCryptoClicks += 1 },
                    onCreateSuggestedAlert = { harness.suggestedAlertClicks += 1 },
                    onOpenConverter = { harness.converterClicks += 1 },
                    onOpenTraveler = { harness.travelerClicks += 1 },
                    onOpenWatchlist = { harness.watchlistClicks += 1 },
                )
            }
        }
        return harness
    }

    private fun testLiveRatesState(
        crypto: List<FxRate> = testCryptoRates(),
    ): LiveRatesState {
        val usd = FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 USD = 1.0000 USD")
        val eur = FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.92, -0.2, listOf(0.91f, 0.92f), "1 USD = 0.9200 EUR")
        val gbp = FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.78, 0.4, listOf(0.77f, 0.78f), "1 USD = 0.7800 GBP")
        val jpy = FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.0, 1.8, listOf(155f, 156f), "1 USD = 156.0000 JPY")
        val chf = FxRate("CHF", "Swiss Franc", "🇨🇭", CurrencyKind.Fiat, 0.83, -1.1, listOf(0.82f, 0.83f), "1 USD = 0.8300 CHF")
        val mxn = FxRate("MXN", "Mexican Peso", "🇲🇽", CurrencyKind.Fiat, 18.72, 0.2, listOf(18.6f, 18.72f), "1 USD = 18.7200 MXN")
        val fiat = listOf(usd, eur, gbp, jpy, chf, mxn)
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = listOf(eur, gbp, jpy, chf, mxn),
            converter = fiat + crypto.take(2),
            compare = listOf(eur, gbp, jpy, chf, mxn),
            crypto = crypto,
            allFiat = fiat,
        )
    }

    private fun testCryptoRates(): List<FxRate> =
        listOf(
            FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.0000154, 2.84, listOf(0.000014f, 0.000015f), "1 USD = 0.000015 BTC"),
            FxRate("ETH", "Ethereum", "Ξ", CurrencyKind.Crypto, 0.000243, 1.92, listOf(0.00022f, 0.00024f), "1 USD = 0.000243 ETH"),
            FxRate("SOL", "Solana", "◎", CurrencyKind.Crypto, 0.00628, -1.14, listOf(0.0068f, 0.00628f), "1 USD = 0.006280 SOL"),
            FxRate("USDT", "Tether", "₮", CurrencyKind.Crypto, 1.0002, 0.01, listOf(1f, 1.0002f), "1 USD = 1.0002 USDT"),
            FxRate("USDC", "USD Coin", "$", CurrencyKind.Crypto, 0.9999, -0.01, listOf(1f, 0.9999f), "1 USD = 0.9999 USDC"),
        )

    private class DashboardHarness {
        var refreshClicks = 0
        var paywallClicks = 0
        var editFavoritesClicks = 0
        var seeAllCryptoClicks = 0
        var suggestedAlertClicks = 0
        var converterClicks = 0
        var travelerClicks = 0
        var watchlistClicks = 0
        val openedDetailCodes = mutableListOf<String>()
    }
}
