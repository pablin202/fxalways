package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.screens.more.MoreScreen
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoreScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun everyMoreRowIsClickableAndRoutesOnce() {
        val harness = MoreHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                MoreScreen(
                    subscriptionState = SubscriptionState(isPremium = false),
                    alertsCount = 1,
                    watchlistCount = 3,
                    onOpenAlerts = { harness.alerts += 1 },
                    onOpenWatchlist = { harness.watchlist += 1 },
                    onOpenTraveler = { harness.traveler += 1 },
                    onOpenSettings = { harness.settings += 1 },
                    onOpenNews = { harness.news += 1 },
                    onOpenPaywall = { harness.paywall += 1 },
                    onOpenCompare = { harness.compare += 1 },
                    onOpenCrypto = { harness.crypto += 1 },
                )
            }
        }

        compose.onNodeWithTag("more_traveler").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_news").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_compare").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_crypto").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_alerts").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_watchlist").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_settings").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_pro").performScrollTo().assertIsDisplayed().performClick()

        compose.runOnIdle {
            assertEquals(1, harness.traveler)
            assertEquals(1, harness.news)
            assertEquals(1, harness.alerts)
            assertEquals(1, harness.watchlist)
            assertEquals(1, harness.settings)
            assertEquals(1, harness.paywall)
            assertEquals(1, harness.compare)
            assertEquals(1, harness.crypto)
        }
    }

    private class MoreHarness {
        var compare = 0
        var crypto = 0
        var traveler = 0
        var news = 0
        var alerts = 0
        var watchlist = 0
        var settings = 0
        var paywall = 0
    }
}
