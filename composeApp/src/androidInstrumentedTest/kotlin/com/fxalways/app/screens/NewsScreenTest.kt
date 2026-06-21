package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.screens.news.NewsScreen
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyNewsStateUsesFullWidthGuidance() {
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                NewsScreen(
                    newsState = NewsUiState(
                        isLoading = false,
                        provider = "fxalways",
                        refreshedLabel = "now",
                        selectedCurrency = "USD",
                        trackedCurrencies = listOf("USD", "EUR"),
                        stories = emptyList(),
                    ),
                    subscriptionState = SubscriptionState(isPremium = true),
                )
            }
        }

        compose.onNodeWithTag("news_empty_state")
            .performScrollTo()
            .assertIsDisplayed()
            .assertWidthIsAtLeast(300.dp)
        compose.onNodeWithText("No market stories yet").assertIsDisplayed()
        compose.onNodeWithTag("news_empty_refresh_guidance").assertIsDisplayed()
    }

    @Test
    fun newsMetadataUsesReadableRowsForLongSourceLabels() {
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                NewsScreen(
                    newsState = NewsUiState(
                        isLoading = false,
                        provider = "Frankfurter / European Central Bank / regional market stream",
                        refreshedLabel = "2026-05-20T17:29:00Z",
                        region = "Australia",
                        selectedCurrency = "AUD",
                        trackedCurrencies = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD"),
                        stories = emptyList(),
                    ),
                    subscriptionState = SubscriptionState(isPremium = true),
                )
            }
        }

        compose.onNodeWithTag("news_metadata_feed").performScrollTo().assertIsDisplayed().assertWidthIsAtLeast(300.dp)
        compose.onNodeWithTag("news_metadata_source").performScrollTo().assertIsDisplayed().assertWidthIsAtLeast(300.dp)
        compose.onNodeWithTag("news_metadata_updated").performScrollTo().assertIsDisplayed().assertWidthIsAtLeast(300.dp)
    }
}
