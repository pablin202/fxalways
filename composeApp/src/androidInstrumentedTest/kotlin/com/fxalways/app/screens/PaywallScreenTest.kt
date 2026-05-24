package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.UserProfile
import com.fxalways.app.screens.paywall.PaywallScreen
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionPlan
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

        compose.onNodeWithText("Travel with fewer money surprises.").assertIsDisplayed()
        compose.onNodeWithText("Pro adds live OCR", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("paywall_benefits").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_comparison").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_alerts").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_compare").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_ocr").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_crypto").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_traveler").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_feature_watchlist").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 active alert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Unlimited pairs + ranges").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Manual entry").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Live camera OCR + currency detection").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("BTC, ETH, USDT, USDC").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Search and add up to 200 crypto assets").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Lifetime").assertCountEquals(0)
        compose.onAllNodesWithText("One payment, permanent access").assertCountEquals(0)

        compose.onNodeWithTag("paywall_plan_Yearly").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_selected_plan").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_restore").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_terms").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_privacy").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_close").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(listOf(SubscriptionPlanKind.Yearly), harness.startedPlans)
            assertEquals(1, harness.restoreClicks)
            assertEquals(
                listOf(
                    "https://fxalways.com/legal?doc=terms&lang=en",
                    "https://fxalways.com/legal?doc=privacy&lang=en",
                ),
                harness.openedUrls,
            )
            assertEquals(1, harness.closeClicks)
        }
    }

    @Test
    fun paywallLegalLinksUseSelectedLanguage() {
        val harness = renderPaywall(SubscriptionState(isPremium = false), appLanguage = "es")

        compose.onNodeWithTag("paywall_terms").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_privacy").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(
                listOf(
                    "https://fxalways.com/legal?doc=terms&lang=es",
                    "https://fxalways.com/legal?doc=privacy&lang=es",
                ),
                harness.openedUrls,
            )
        }
    }

    @Test
    fun planSelectionIgnoresUnavailablePlansAndStartsAvailableRecurringPlan() {
        val harness = renderPaywall(
            SubscriptionState(
                isPremium = false,
                plans = listOf(
                    SubscriptionPlan(
                        kind = SubscriptionPlanKind.Monthly,
                        title = "Monthly",
                        priceLabel = "$2.99",
                        cadenceLabel = "Paid every month",
                        isAvailable = false,
                    ),
                    SubscriptionPlan(
                        kind = SubscriptionPlanKind.Yearly,
                        title = "Yearly",
                        priceLabel = "$29.99",
                        cadenceLabel = "Best long-term value",
                        badge = "BEST VALUE",
                        isAvailable = true,
                    ),
                ),
            ),
        )

        compose.onNodeWithTag("paywall_plan_Monthly").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_plan_Yearly").performScrollTo().performClick()
        compose.onNodeWithTag("paywall_selected_plan").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Yearly").assertCountEquals(2)
        compose.onAllNodesWithText("Not configured").assertCountEquals(1)

        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(listOf(SubscriptionPlanKind.Yearly), harness.startedPlans) }
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
        compose.onNodeWithTag("paywall_status_message").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("RevenueCat unavailable.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(emptyList(), unavailableHarness.startedPlans) }
    }

    @Test
    fun revenueCatPackageErrorIsLocalizedAndDoesNotStartUnavailablePurchase() {
        val harness = renderPaywall(
            SubscriptionState(
                isPremium = false,
                canPurchase = false,
                statusMessage = "No RevenueCat package is configured for yearly.",
            ),
        )

        compose.onNodeWithTag("paywall_status_message").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("No RevenueCat package is configured for yearly.").assertIsDisplayed()
        compose.onNodeWithTag("paywall_start_button").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(emptyList(), harness.startedPlans) }
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

    @Test
    fun paywallPersonalizesOfferForSelectedProfile() {
        renderPaywall(SubscriptionState(isPremium = false), userProfile = UserProfile.Remittances)

        compose.onNodeWithTag("paywall_profile_offer").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Send money smarter").assertIsDisplayed()
        compose.onNodeWithText("Full provider comparison + alerts").assertIsDisplayed()
        compose.onNodeWithText("USD → MXN").assertIsDisplayed()
        compose.onNodeWithText("Wise first, compare bank transfer").assertIsDisplayed()
    }

    private fun renderPaywall(
        subscriptionState: SubscriptionState,
        actionInProgress: Boolean = false,
        userProfile: UserProfile = UserProfile.Traveler,
        appLanguage: String = "en",
    ): PaywallHarness {
        val harness = PaywallHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                PaywallScreen(
                    subscriptionState = subscriptionState,
                    actionInProgress = actionInProgress,
                    userProfile = userProfile,
                    appLanguage = appLanguage,
                    onClose = { harness.closeClicks += 1 },
                    onStart = { harness.startedPlans += it },
                    onRestore = { harness.restoreClicks += 1 },
                    onOpenUrl = { harness.openedUrls += it },
                )
            }
        }
        return harness
    }

    private class PaywallHarness {
        var closeClicks = 0
        var restoreClicks = 0
        val startedPlans = mutableListOf<SubscriptionPlanKind>()
        val openedUrls = mutableListOf<String>()
    }
}
