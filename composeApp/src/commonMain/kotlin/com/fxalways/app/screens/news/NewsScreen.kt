package com.fxalways.app.screens.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.ScreenScaffold
import com.fxalways.app.screens.detail.InlineSkeletonRows
import com.fxalways.app.screens.detail.LegendDot
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.SentimentBar
import com.fxalways.app.screens.detail.compactProviderLabel
import com.fxalways.app.screens.detail.compactRuntimeLabel
import com.fxalways.app.screens.shared.ProUpsellCard
import com.fxalways.app.screens.ui
import com.fxalways.app.screens.userFriendlyNetworkError
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun NewsScreen(
    newsState: NewsUiState = NewsUiState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onRefresh: () -> Unit = {},
    onRegionSelected: (String) -> Unit = {},
    onCurrencySelected: (String) -> Unit = {},
    onOpenStory: (NewsStory) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    var query by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf("ALL") }
    val normalizedQuery = query.trim()
    val topicOptions = remember(newsState.stories) {
        (listOf("ALL") + newsState.stories.flatMap { story -> story.topics }.filter { it.isNotBlank() })
            .distinct()
            .take(8)
    }
    val filteredStories = remember(newsState.stories, query, newsState.selectedCurrency, selectedTopic) {
        newsState.stories.filter { story ->
            val matchesQuery = normalizedQuery.isBlank() ||
                story.title.contains(normalizedQuery, ignoreCase = true) ||
                story.summary.contains(normalizedQuery, ignoreCase = true) ||
                story.tag.contains(normalizedQuery, ignoreCase = true) ||
                story.topics.any { it.contains(normalizedQuery, ignoreCase = true) } ||
                story.moves.any { it.first.contains(normalizedQuery, ignoreCase = true) }
            val matchesCurrency = newsState.selectedCurrency.isBlank() ||
                newsState.selectedCurrency == "USD" ||
                story.moves.any { it.first == newsState.selectedCurrency } ||
                story.tag == newsState.selectedCurrency
            val matchesTopic = selectedTopic == "ALL" || story.topics.any { it == selectedTopic }
            matchesQuery && matchesCurrency && matchesTopic
        }
    }
    val visibleStories = filteredStories.take(access.newsStoryLimit.cap(filteredStories.size))
    val regionOptions = listOf("US", "AU", "GB", "EU", "BR", "MX", "JP")
    val currencyOptions = (newsState.trackedCurrencies + listOf("USD", "EUR", "GBP", "JPY", "AUD", "BTC")).distinct()
    val emptyCopy = newsEmptyCopy(
        hasBackendStories = newsState.stories.isNotEmpty(),
        hasQuery = normalizedQuery.isNotBlank(),
        topic = selectedTopic,
    )
    val providerLabel = compactProviderLabel(newsState.provider)
    val updatedLabel = compactRuntimeLabel(newsState.refreshedLabel)
    ScreenScaffold {
        ScreenHeader(
            ui("News"),
            sub = if (access.canUseAdvancedNews) ui("MARKET STREAM") else ui("MARKET PREVIEW"),
            subtitle = if (newsState.isLoading && newsState.stories.isEmpty()) {
                "${ui("Loading market stream")} · ${newsState.selectedCurrency} ${ui("focus")}"
            } else {
                "${newsState.region} · ${newsState.selectedCurrency} ${ui("focus")} · $updatedLabel"
            },
            right = {
                Text(
                    if (newsState.isLoading) "…" else "↻",
                    style = FxTheme.typography.numberL,
                    color = if (newsState.isLoading) FxTheme.colors.accent else FxTheme.colors.textDim,
                    modifier = Modifier.clickable(enabled = !newsState.isLoading, onClick = onRefresh),
                )
            },
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(ui("SENTIMENT"))
                    if (newsState.isLoading) {
                        Eyebrow(ui("REFRESHING"), color = FxTheme.colors.accent)
                    }
                }
                if (newsState.isLoading && newsState.stories.isEmpty()) {
                    InlineSkeletonRows(rows = 4, modifier = Modifier.testTag("news_sentiment_loading"))
                } else {
                    SentimentBar(newsState.bullish, newsState.neutral, newsState.bearish)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LegendDot("${ui("BULLISH")} ${newsState.bullish}%", FxTheme.colors.up)
                        LegendDot("${ui("NEUTRAL")} ${newsState.neutral}%", FxTheme.colors.textGhost)
                        LegendDot("${ui("BEARISH")} ${newsState.bearish}%", FxTheme.colors.down)
                    }
                    KeyValueRow(
                        ui("Feed"),
                        "${newsState.language.uppercase()} · ${compactTrackedCurrencies(newsState.trackedCurrencies)}",
                        modifier = Modifier.testTag("news_metadata_feed"),
                    )
                    KeyValueRow(
                        ui("Source"),
                        providerLabel,
                        newsState.region,
                        modifier = Modifier.testTag("news_metadata_source"),
                    )
                    KeyValueRow(
                        ui("Updated"),
                        updatedLabel,
                        "${newsState.selectedCurrency} ${ui("focus")}",
                        modifier = Modifier.testTag("news_metadata_updated"),
                    )
                }
            }
        }
        if (newsState.isLoading && newsState.stories.isEmpty()) {
            LoadingSkeletonCard(
                title = ui("Loading market stream"),
                rows = 4,
                modifier = Modifier.testTag("news_loading_skeleton"),
            )
        }
        BentoCard(padding = 10.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NewsSearchField(query = query, onQueryChange = { query = it })
                NewsFilterRow(
                    label = ui("REGION"),
                    options = regionOptions,
                    selected = newsState.region,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { region ->
                        if (access.canUseAdvancedNews) onRegionSelected(region) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
                    label = ui("CURRENCY"),
                    options = currencyOptions,
                    selected = newsState.selectedCurrency,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { code ->
                        if (access.canUseAdvancedNews) onCurrencySelected(code) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
                    label = ui("TOPIC"),
                    options = topicOptions,
                    selected = selectedTopic,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { topic ->
                        if (access.canUseAdvancedNews || topic == selectedTopic) {
                            selectedTopic = topic
                        } else {
                            onOpenPaywall()
                        }
                    },
                )
            }
        }
        SectionLabel("${ui("RECENT LINES")} · ${filteredStories.size}")
        if (newsState.errorMessage != null && newsState.stories.isEmpty()) {
            BentoCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ui("Market stream unavailable"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(userFriendlyNetworkError(newsState.errorMessage), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                }
            }
        } else if (visibleStories.isEmpty() && !(newsState.isLoading && newsState.stories.isEmpty())) {
            BentoCard(modifier = Modifier.fillMaxWidth().testTag("news_empty_state"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ui(emptyCopy.first), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(ui(emptyCopy.second), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                    Text(
                        ui("Try a broader filter or refresh the feed."),
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textGhost,
                        modifier = Modifier.testTag("news_empty_refresh_guidance"),
                    )
                    if (newsState.isLoading) {
                        Text(ui("Refreshing market stream…"), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
                    }
                }
            }
        }
        visibleStories.forEach { story ->
            StoryCard(story, onClick = { onOpenStory(story) })
        }
        if ((!access.canUseAdvancedNews || visibleStories.size < filteredStories.size) && !(newsState.isLoading && newsState.stories.isEmpty())) {
            ProUpsellCard(
                title = ui("Personalize the market stream"),
                subtitle = if (visibleStories.size < filteredStories.size) {
                    "${ui("Showing")} ${visibleStories.size}/${filteredStories.size} ${ui("stories")}. ${ui("Pro unlocks the full regional stream.")}"
                } else {
                    ui("Pro unlocks more stories and filters by region, currencies and topics.")
                },
                onClick = onOpenPaywall,
            )
        }
    }
}

internal fun newsEmptyCopy(
    hasBackendStories: Boolean,
    hasQuery: Boolean,
    topic: String,
): Pair<String, String> =
    when {
        !hasBackendStories -> "No market stories yet" to "No live market stories have arrived yet."
        hasQuery -> "No search matches" to "No live stories match this search."
        topic != "ALL" -> "No topic stories" to "Try a broader filter or refresh the feed."
        else -> "No currency stories" to "Try a broader filter or refresh the feed."
    }

private fun compactTrackedCurrencies(codes: List<String>): String {
    val visible = codes.filter { it.isNotBlank() }.distinct().take(4)
    val extra = (codes.distinct().size - visible.size).coerceAtLeast(0)
    return if (extra > 0) {
        "${visible.joinToString(", ")} +$extra"
    } else {
        visible.joinToString(", ")
    }
}
