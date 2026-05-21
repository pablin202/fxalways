package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.UserProfile
import com.fxalways.app.screens.onboarding.OnboardingScreen
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun userCanSelectProfileAndSkipWithThatProfile() {
        val completedProfiles = mutableListOf<UserProfile>()
        renderOnboarding { completedProfiles += it }

        compose.onNodeWithTag("onboarding_profile_picker").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_profile_CryptoHolder").performClick()
        compose.onNodeWithText("Skip").performClick()

        compose.runOnIdle { assertEquals(listOf(UserProfile.CryptoHolder), completedProfiles) }
    }

    @Test
    fun getStartedCompletesSelectedProfileAfterAllPages() {
        val completedProfiles = mutableListOf<UserProfile>()
        renderOnboarding { completedProfiles += it }

        compose.onNodeWithTag("onboarding_profile_Remittances").performClick()
        repeat(3) {
            compose.onNodeWithText("Next  →").performClick()
        }
        compose.onNodeWithText("Get started").performClick()

        compose.runOnIdle { assertEquals(listOf(UserProfile.Remittances), completedProfiles) }
    }

    private fun renderOnboarding(onComplete: (UserProfile) -> Unit) {
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme(dark = true) {
                OnboardingScreen(onComplete = onComplete)
            }
        }
    }
}
