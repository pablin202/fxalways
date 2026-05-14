package com.fxalways.app.data

import com.fxalways.app.DeviceLocale
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.domain.NewsFeedDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class NewsUiState(
    val isLoading: Boolean = true,
    val provider: String = "loading",
    val refreshedAt: String = "",
    val refreshedLabel: String = "loading",
    val region: String = DeviceLocale.region,
    val language: String = DeviceLocale.language,
    val selectedCurrency: String = DeviceLocale.currencyCode,
    val trackedCurrencies: List<String> = defaultCurrencies(DeviceLocale.currencyCode),
    val bullish: Int = 46,
    val neutral: Int = 20,
    val bearish: Int = 34,
    val stories: List<NewsStory> = emptyList(),
    val errorMessage: String? = null,
)

class NewsStore(
    private val api: ExchangeApi = ExchangeApi(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh(
        language: String = _state.value.language.takeIf { it.isNotBlank() } ?: DeviceLocale.language,
        region: String = _state.value.region.takeIf { it.isNotBlank() } ?: DeviceLocale.region,
        currencies: List<String> = _state.value.trackedCurrencies.ifEmpty { defaultCurrencies(_state.value.selectedCurrency) },
    ) {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            val selectedCurrency = currencies.firstOrNull().orEmpty().ifBlank { _state.value.selectedCurrency }
            val normalizedCurrencies = currencies.map { code -> code.uppercase() }.distinct()
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    language = language,
                    region = region.uppercase(),
                    selectedCurrency = selectedCurrency.uppercase(),
                    trackedCurrencies = normalizedCurrencies,
                )
            }
            runCatching {
                api.newsFeed(language, region, normalizedCurrencies)
            }.onSuccess { feed ->
                _state.value = feed.toUiState(
                    selectedCurrency = selectedCurrency,
                    trackedCurrencies = normalizedCurrencies,
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
        refreshedAt = refreshedAt,
        refreshedLabel = refreshedAt.toNewsRefreshLabel(),
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
                source = item.source,
                sourceUrl = item.sourceUrl,
            )
        },
    )

private fun defaultCurrencies(primary: String): List<String> =
    listOf(primary.ifBlank { DeviceLocale.currencyCode }, "USD", "EUR", "JPY", "GBP", "BTC")
        .map { it.uppercase() }
        .distinct()

private fun String.toNewsRefreshLabel(): String {
    val instant = runCatching { Instant.parse(this) }.getOrNull() ?: return "updated just now"
    val ageMs = (Clock.System.now() - instant).inWholeMilliseconds.coerceAtLeast(0)
    val minutes = ageMs / 60_000
    return when {
        minutes < 1 -> "updated just now"
        minutes < 60 -> "updated ${minutes}m ago"
        minutes < 1_440 -> "updated ${minutes / 60}h ago"
        else -> "updated ${minutes / 1_440}d ago"
    }
}
