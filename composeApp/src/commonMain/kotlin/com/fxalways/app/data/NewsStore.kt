package com.fxalways.app.data

import com.fxalways.app.DeviceLocale
import com.fxalways.app.data.mock.NewsStories
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.domain.NewsFeedDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = true,
    val provider: String = "mock",
    val region: String = "US",
    val language: String = "en",
    val bullish: Int = 46,
    val neutral: Int = 20,
    val bearish: Int = 34,
    val stories: List<NewsStory> = NewsStories,
    val errorMessage: String? = null,
)

class NewsStore(
    private val api: ExchangeApi = ExchangeApi(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state

    init {
        refresh()
    }

    fun refresh(
        language: String = DeviceLocale.language,
        region: String = DeviceLocale.region,
        currencies: List<String> = listOf("USD", "EUR", "JPY", "GBP", "BTC"),
    ) {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                api.newsFeed(language, region, currencies)
            }.onSuccess { feed ->
                _state.value = feed.toUiState()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }
}

private fun NewsFeedDto.toUiState(): NewsUiState =
    NewsUiState(
        isLoading = false,
        provider = provider,
        region = region,
        language = language,
        bullish = sentiment.bullish,
        neutral = sentiment.neutral,
        bearish = sentiment.bearish,
        stories = items.map { item ->
            NewsStory(
                tag = item.tag,
                impact = item.impact.uppercase(),
                age = item.ageLabel,
                title = item.title,
                summary = item.summary,
                moves = item.moves.map { it.code to it.change },
            )
        },
    )
