package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
                )
            }
        }

        compose.onNodeWithTag("more_traveler").assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_news").assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_alerts").assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_watchlist").assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_settings").assertIsDisplayed().performClick()
        compose.onNodeWithTag("more_pro").assertIsDisplayed().performClick()

        compose.runOnIdle {
            assertEquals(1, harness.traveler)
            assertEquals(1, harness.news)
            assertEquals(1, harness.alerts)
            assertEquals(1, harness.watchlist)
            assertEquals(1, harness.settings)
            assertEquals(1, harness.paywall)
        }
    }

    private class MoreHarness {
        var traveler = 0
        var news = 0
        var alerts = 0
        var watchlist = 0
        var settings = 0
        var paywall = 0
    }
}
