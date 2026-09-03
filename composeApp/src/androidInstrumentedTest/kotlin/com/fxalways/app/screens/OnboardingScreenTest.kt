package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.Corridor
import com.fxalways.app.SendCadence
import com.fxalways.app.UserProfile
import com.fxalways.app.screens.onboarding.OnboardingResult
import com.fxalways.app.screens.onboarding.OnboardingScreen
import com.fxalways.app.suggestedTargets
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val results = mutableListOf<OnboardingResult>()
    private var notificationRequests = 0

    @Test
    fun monthlyCorridorInfersRemittancesAndCompletesWithoutNotifications() {
        renderOnboarding(localCurrency = "AUD")

        compose.onNodeWithTag("onboarding_next").assertIsNotEnabled()
        compose.onNodeWithTag("onboarding_base_AUD").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_target_ARS").performScrollTo().performClick()
        compose.onNodeWithText("AUD → ARS").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").assertIsEnabled().performClick()

        compose.onNodeWithTag("onboarding_amount_1000").performScrollTo().performClick()
        compose.onNodeWithTag("onboarding_cadence_Monthly").performScrollTo().performClick()
        compose.onNodeWithText("Remittances").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()

        compose.onNodeWithText("Enable alerts").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("onboarding_not_now").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(listOf(OnboardingResult(Corridor("AUD", "ARS", 1_000.0, SendCadence.Monthly), UserProfile.Remittances, false)), results)
            assertEquals(0, notificationRequests)
        }
    }

    @Test
    fun oneOffTripSearchedByNameInfersTravelerAndRequestsNotifications() {
        renderOnboarding(localCurrency = "USD")

        compose.onNodeWithTag("onboarding_target_search").performTextInput("jap")
        compose.onNodeWithTag("onboarding_target_JPY").performScrollTo().performClick()
        compose.onNodeWithTag("onboarding_next").performClick()

        compose.onNodeWithTag("onboarding_amount_2000").performScrollTo().performClick()
        compose.onNodeWithTag("onboarding_cadence_Once").performScrollTo().performClick()
        compose.onNodeWithText("Traveler").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("onboarding_enable_alerts").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(Corridor("USD", "JPY", 2_000.0, SendCadence.Once), results.single().corridor)
            assertEquals(UserProfile.Traveler, results.single().profile)
            assertEquals(true, results.single().notificationsRequested)
            assertEquals(1, notificationRequests)
        }
    }

    @Test
    fun switchingBaseClearsAConflictingTargetAndOffersNewSuggestions() {
        renderOnboarding(localCurrency = "AUD")

        compose.onNodeWithTag("onboarding_target_USD").performScrollTo().performClick()
        compose.onNodeWithTag("onboarding_base_USD").performClick()
        compose.onNodeWithTag("onboarding_next").assertIsNotEnabled()
        compose.onNodeWithTag("onboarding_target_MXN").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun skipCompletesWithTheFirstSuggestedCorridor() {
        renderOnboarding(localCurrency = "AUD")

        compose.onNodeWithTag("onboarding_skip").performClick()

        compose.runOnIdle {
            val result = results.single()
            assertEquals(Corridor("AUD", suggestedTargets("AUD").first(), 500.0, SendCadence.Once), result.corridor)
            assertEquals(UserProfile.Remittances, result.profile)
            assertEquals(false, result.notificationsRequested)
        }
    }

    private fun renderOnboarding(localCurrency: String) {
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme(dark = true) {
                OnboardingScreen(
                    onComplete = { results += it },
                    localCurrency = localCurrency,
                    onRequestNotifications = { notificationRequests += 1 },
                )
            }
        }
    }
}
