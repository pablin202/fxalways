package com.fxalways.app.data

import com.fxalways.app.data.mock.DetailSeries
import com.fxalways.app.domain.HistoricalPoint
import com.fxalways.designsystem.components.Period
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DetailUiState(
    val isLoading: Boolean = false,
    val base: String = "USD",
    val quote: String = "EUR",
    val period: Period = Period.OneMonth,
    val provider: String = "cached preview",
    val updatedLabel: String = "",
    val series: List<Float> = DetailSeries,
    val points: List<HistoricalPoint> = emptyList(),
    val errorMessage: String? = null,
)

class DetailStore(
    private val api: ExchangeApi = ExchangeApi(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state
    private var loadJob: Job? = null

    fun load(base: String, quote: String, period: Period, fallback: List<Float>) {
        val current = _state.value
        if (current.base == base && current.quote == quote && current.period == period && current.points.isNotEmpty() && !current.isLoading) {
            return
        }
        if (current.base == base && current.quote == quote && current.points.size >= period.requiredCachedPoints && !current.isLoading) {
            _state.update {
                it.copy(
                    isLoading = false,
                    period = period,
                    series = current.points.toDetailSeries(period, fallback),
                    errorMessage = null,
                )
            }
            return
        }
        loadJob?.cancel()
        loadJob = scope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    base = base,
                    quote = quote,
                    period = period,
                    series = fallback.seriesForFallback(),
                    points = emptyList(),
                    provider = "cached preview",
                    updatedLabel = "",
                    errorMessage = null,
                )
            }
            runCatching {
                api.history(base, quote, days = period.requestDays)
            }.onSuccess { history ->
                val points = history.points.sortedBy { it.date }
                _state.update {
                    it.copy(
                        isLoading = false,
                        base = history.base,
                        quote = history.quote,
                        period = period,
                        provider = history.provider,
                        updatedLabel = "updated ${detailRefreshTimeLabel()}",
                        series = points.toDetailSeries(period, fallback),
                        points = points,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                _state.update {
                    it.copy(
                        isLoading = false,
                        base = base,
                        quote = quote,
                        period = period,
                        provider = "cached preview",
                        updatedLabel = "",
                        series = fallback.seriesForFallback(),
                        points = emptyList(),
                        errorMessage = throwable.message ?: "History unavailable",
                    )
                }
            }
        }
    }
}

private val Period.requestDays: Int
    get() = when (this) {
        Period.OneDay -> 45
        Period.OneWeek -> 45
        Period.OneMonth -> 45
        Period.OneYear -> 365
        Period.All -> 1_825
    }

private val Period.requiredCachedPoints: Int
    get() = when (this) {
        Period.OneDay -> 45
        Period.OneWeek -> 45
        Period.OneMonth -> 45
        Period.OneYear -> 365
        Period.All -> 1_825
    }

private val Period.visiblePoints: Int
    get() = when (this) {
        Period.OneDay -> 2
        Period.OneWeek -> 7
        Period.OneMonth -> 45
        Period.OneYear -> 365
        Period.All -> 1_825
    }

private fun List<HistoricalPoint>.toDetailSeries(period: Period, fallback: List<Float>): List<Float> {
    val values = takeLast(period.visiblePoints)
        .map { it.value.toFloat() }
        .takeIf { it.size >= 2 }
        ?: return fallback.seriesForFallback()
    if (values.size <= MAX_DETAIL_POINTS) return values
    val step = (values.size / MAX_DETAIL_POINTS).coerceAtLeast(1)
    return values.filterIndexed { index, _ -> index % step == 0 || index == values.lastIndex }
}

private fun List<Float>.seriesForFallback(): List<Float> =
    takeIf { it.size >= 2 } ?: DetailSeries

private fun detailRefreshTimeLabel(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = now.hour.toString().padStart(2, '0')
    val minute = now.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

private const val MAX_DETAIL_POINTS = 90
