package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaywallScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freePaywallShowsConcreteFreeVsProValueAndStartsSelectedPlan() {
        val harness = renderPaywall(SubscriptionState(isPremium = false))

        compose.onNodeWithText("The full picture.", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("paywall_benefits").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_comparison").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_alerts").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_compare").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_crypto").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_traveler").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_watchlist").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 active alert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Unlimited pairs + ranges").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("BTC, ETH, USDT, USDC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Search and add up to 200 crypto assets").performScrollTo().assertIsDisplayed()

        compose.onNodeWithTag("paywall_plan_Yearly").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_selected_plan").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_restore").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(listOf(SubscriptionPlanKind.Yearly), harness.startedPlans)
            assertEquals(1, harness.restoreClicks)
        }
    }

    @Test
    fun premiumPaywallShowsActiveStateAndContinueCloses() {
        val harness = renderPaywall(
            SubscriptionState(
                isPremium = true,
                activePlanLabel = "Yearly",
                entitlementId = "pro",
            ),
        )

        compose.onNodeWithTag("paywall_active_card").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("FX/ Pro is active").assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(1, harness.closeClicks)
            assertEquals(emptyList(), harness.startedPlans)
        }
    }

    @Test
    fun unavailableStateDoesNotStartPurchases() {
        val unavailableHarness = renderPaywall(
            SubscriptionState(
                isPremium = false,
                canPurchase = false,
                statusMessage = "RevenueCat unavailable.",
            ),
        )

        compose.onNodeWithText("Purchases unavailable").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(emptyList(), unavailableHarness.startedPlans) }
    }

    @Test
    fun processingStateDisablesStartAndRestore() {
        val processingHarness = renderPaywall(SubscriptionState(isPremium = false), actionInProgress = true)

        compose.onNodeWithText("Processing...").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_restore").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(emptyList(), processingHarness.startedPlans)
            assertEquals(0, processingHarness.restoreClicks)
        }
    }

    private fun renderPaywall(
        subscriptionState: SubscriptionState,
        actionInProgress: Boolean = false,
    ): PaywallHarness {
        val harness = PaywallHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                PaywallScreen(
                    subscriptionState = subscriptionState,
                    actionInProgress = actionInProgress,
                    onClose = { harness.closeClicks += 1 },
                    onStart = { harness.startedPlans += it },
                    onRestore = { harness.restoreClicks += 1 },
                )
            }
        }
        return harness
    }

    private class PaywallHarness {
        var closeClicks = 0
        var restoreClicks = 0
        val startedPlans = mutableListOf<SubscriptionPlanKind>()
    }
}
