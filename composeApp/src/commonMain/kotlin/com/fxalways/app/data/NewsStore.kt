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
    val region: String = DeviceLocale.region,
    val language: String = DeviceLocale.language,
    val selectedCurrency: String = "USD",
    val trackedCurrencies: List<String> = listOf("USD", "EUR", "JPY", "GBP", "BTC"),
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
        language: String = _state.value.language.takeIf { it.isNotBlank() } ?: DeviceLocale.language,
        region: String = _state.value.region.takeIf { it.isNotBlank() } ?: DeviceLocale.region,
        currencies: List<String> = _state.value.trackedCurrencies.ifEmpty { defaultCurrencies(_state.value.selectedCurrency) },
    ) {
        scope.launch {
            val selectedCurrency = currencies.firstOrNull().orEmpty().ifBlank { _state.value.selectedCurrency }
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    language = language,
                    region = region.uppercase(),
                    selectedCurrency = selectedCurrency.uppercase(),
                    trackedCurrencies = currencies.map { code -> code.uppercase() }.distinct(),
                )
            }
            runCatching {
                api.newsFeed(language, region, currencies)
            }.onSuccess { feed ->
                _state.value = feed.toUiState(
                    selectedCurrency = selectedCurrency,
                    trackedCurrencies = currencies,
                )
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

    fun setRegion(region: String) {
        refresh(region = region, currencies = _state.value.trackedCurrencies)
    }

    fun setCurrency(code: String) {
        refresh(currencies = defaultCurrencies(code))
    }
}

private fun NewsFeedDto.toUiState(
    selectedCurrency: String,
    trackedCurrencies: List<String>,
): NewsUiState =
    NewsUiState(
        isLoading = false,
        provider = provider,
        region = region,
        language = language,
        selectedCurrency = selectedCurrency.uppercase(),
        trackedCurrencies = trackedCurrencies.map { it.uppercase() }.distinct(),
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

private fun defaultCurrencies(primary: String): List<String> =
    listOf(primary, "USD", "EUR", "JPY", "GBP", "BTC")
        .map { it.uppercase() }
        .distinct()
