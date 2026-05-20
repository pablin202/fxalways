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
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.DetailUiState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.domain.HistoricalPoint
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.Period
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun freeUserSeesPreviewNewsAndLongRangeHistoryOpensPaywall() {
        val harness = renderDetail(isPremium = false)

        compose.onNodeWithText("USD / EUR").assertIsDisplayed()
        compose.onNodeWithTag("detail_rate_trust").assertIsDisplayed()
        compose.onNodeWithText("Indicative mid-market rates.", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("detail_share_rate_card").assertIsDisplayed()
        compose.onNodeWithTag("detail_share_rate_source").assertIsDisplayed()
        compose.onNodeWithTag("detail_share_disclaimer").assertIsDisplayed()
        compose.onNodeWithTag("detail_share_copy").performClick()
        compose.onNodeWithText("Copied rate card").assertIsDisplayed()
        compose.onNodeWithText("HISTORY · 1M").assertIsDisplayed()
        compose.onAllNodesWithText("Preview").assertCountEquals(2)
        compose.onNodeWithTag("detail_economic_calendar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("detail_calendar_event_0").assertIsDisplayed()
        compose.onNodeWithTag("detail_calendar_event_1").assertIsDisplayed()
        compose.onAllNodesWithText("EUR jobs update", substring = true).assertCountEquals(0)
        compose.onNodeWithTag("detail_calendar_upsell").assertIsDisplayed()
        compose.onNodeWithTag("detail_story_ECBfirstEURevent").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("detail_story_ECBsecondEURevent").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("ECB third EUR event").assertCountEquals(0)

        compose.onNodeWithTag("period_OneYear").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(1, harness.paywallClicks)
            assertEquals(listOf(Period.OneMonth), harness.loadedPeriods)
        }
    }

    @Test
    fun proUserLoadsLongRangeHistoryAndCanOpenAllRelatedStoriesAndEvents() {
        val harness = renderDetail(isPremium = true)

        compose.onNodeWithTag("period_OneYear").performScrollTo().performClick()

        compose.onNodeWithText("HISTORY · 1Y").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("ExchangeApi · 4 pts · updated test").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("STATISTICS · 1Y").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("detail_economic_calendar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("EUR jobs update", substring = true).assertIsDisplayed()
        compose.onAllNodesWithText("Pro unlocks the full calendar and impact filters.").assertCountEquals(0)
        compose.onNodeWithTag("detail_story_ECBthirdEURevent").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("detail_story_ECBthirdEURevent").performScrollTo().performClick()
        compose.onNodeWithTag("detail_event_ECBthirdEURevent").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(listOf(Period.OneMonth, Period.OneYear), harness.loadedPeriods)
            assertEquals(listOf("ECB third EUR event"), harness.openedStories.map { it.title })
            assertEquals(listOf("https://example.com/eur-3"), harness.openedUrls)
            assertEquals(0, harness.paywallClicks)
        }
    }

    @Test
    fun loadingAndErrorStatesKeepCachedPreviewVisible() {
        renderDetail(
            isPremium = true,
            detailState = DetailUiState(
                isLoading = true,
                base = "USD",
                quote = "EUR",
                period = Period.OneMonth,
                series = listOf(0.91f, 0.92f, 0.93f),
                errorMessage = "Network down",
            ),
        )

        compose.onNodeWithText("LOADING HISTORY").assertIsDisplayed()
        compose.onNodeWithText("History unavailable · using cached preview").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("detail_statistics").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun alertCtaUsesExistingPairCountAndCreatesAlertForSelectedRate() {
        val harness = renderDetail(
            isPremium = false,
            alertsState = AlertsState(
                listOf(
                    PriceAlert(
                        id = "eur_active",
                        base = "USD",
                        quote = "EUR",
                        target = 0.94,
                        direction = AlertDirection.Above,
                        enabled = true,
                    ),
                ),
            ),
        )

        compose.onAllNodesWithText("1/1 active", substring = true).assertCountEquals(1)
        compose.onNodeWithTag("detail_alert_cta").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(listOf("EUR"), harness.createdAlertCodes) }
    }

    private fun renderDetail(
        isPremium: Boolean,
        alertsState: AlertsState = AlertsState(),
        detailState: DetailUiState = loadedDetailState(),
    ): DetailHarness {
        val harness = DetailHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            FxTheme {
                DetailScreen(
                    liveState = testLiveRatesState(),
                    alertsState = alertsState,
                    subscriptionState = SubscriptionState(isPremium = isPremium),
                    detailState = detailState,
                    newsState = NewsUiState(isLoading = false, stories = relatedStories()),
                    rate = testRate(),
                    onOpenPaywall = { harness.paywallClicks += 1 },
                    onLoadHistory = { _, _, period, _ -> harness.loadedPeriods += period },
                    onOpenUrl = { harness.openedUrls += it },
                    onOpenStory = { harness.openedStories += it },
                    onCreateAlert = { harness.createdAlertCodes += it.code },
                )
            }
        }
        return harness
    }

    private fun testLiveRatesState(): LiveRatesState =
        LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "updated test",
            favorites = listOf(testRate()),
            converter = listOf(testRate()),
            compare = listOf(testRate()),
            allFiat = listOf(testRate()),
            detailSeries = listOf(0.91f, 0.92f, 0.93f, 0.94f),
        )

    private fun testRate(): FxRate =
        FxRate(
            code = "EUR",
            name = "Euro",
            glyph = "EUR",
            kind = CurrencyKind.Fiat,
            rate = 0.92,
            change24h = -0.2,
            sparkline = listOf(0.91f, 0.92f, 0.93f, 0.94f),
            caption = "1 USD = 0.9200 EUR",
        )

    private fun loadedDetailState(): DetailUiState =
        DetailUiState(
            isLoading = false,
            base = "USD",
            quote = "EUR",
            period = Period.OneYear,
            provider = "ExchangeApi",
            updatedLabel = "updated test",
            series = listOf(0.90f, 0.91f, 0.92f, 0.93f),
            points = listOf(
                HistoricalPoint("2026-01-01", 0.90),
                HistoricalPoint("2026-02-01", 0.91),
                HistoricalPoint("2026-03-01", 0.92),
                HistoricalPoint("2026-04-01", 0.93),
            ),
        )

    private fun relatedStories(): List<NewsStory> =
        listOf(
            NewsStory("EUR", "HIGH IMPACT", "10m ago", "ECB first EUR event", "First move.", listOf("EUR" to -0.2), sourceUrl = "https://example.com/eur-1"),
            NewsStory("ECB", "MED IMPACT", "20m ago", "ECB second EUR event", "Second move.", listOf("EUR" to 0.1), sourceUrl = "https://example.com/eur-2"),
            NewsStory("ECB", "MED IMPACT", "30m ago", "ECB third EUR event", "Third move.", listOf("EUR" to 0.3), sourceUrl = "https://example.com/eur-3"),
            NewsStory("BTC", "MED IMPACT", "40m ago", "BTC unrelated event", "Crypto move.", listOf("BTC" to 2.0), sourceUrl = "https://example.com/btc"),
        )

    private class DetailHarness {
        var paywallClicks = 0
        val loadedPeriods = mutableListOf<Period>()
        val openedStories = mutableListOf<NewsStory>()
        val openedUrls = mutableListOf<String>()
        val createdAlertCodes = mutableListOf<String>()
    }
}
