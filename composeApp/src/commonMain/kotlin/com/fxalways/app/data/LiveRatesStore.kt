package com.fxalways.app.data

import com.fxalways.app.data.mock.CompareRates
import com.fxalways.app.data.mock.ConverterRates
import com.fxalways.app.data.mock.CryptoRates
import com.fxalways.app.data.mock.DetailSeries
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.domain.HistoricalPoint
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class LiveRatesState(
    val isLoading: Boolean = true,
    val isLive: Boolean = false,
    val errorMessage: String? = null,
    val baseCurrency: String = "USD",
    val updatedLabel: String = "cached · mock",
    val autoRefreshLabel: String = "Auto-refresh off",
    val favorites: List<FxRate> = FavoriteRates,
    val crypto: List<FxRate> = CryptoRates,
    val converter: List<FxRate> = ConverterRates,
    val compare: List<FxRate> = CompareRates,
    val detailSeries: List<Float> = DetailSeries,
)

class LiveRatesStore(
    private val api: ExchangeApi = ExchangeApi(),
    initialBaseCurrency: String = "USD",
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(LiveRatesState(baseCurrency = initialBaseCurrency))
    val state: StateFlow<LiveRatesState> = _state
    private var autoRefreshJob: Job? = null
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun setBaseCurrency(code: String) {
        if (_state.value.baseCurrency == code) return
        _state.update { it.copy(baseCurrency = code) }
        refresh(forceLoading = true)
    }

    fun startAutoRefresh(intervalMillis: Long = AUTO_REFRESH_INTERVAL_MILLIS) {
        if (autoRefreshJob?.isActive == true) return
        _state.update { it.copy(autoRefreshLabel = "Auto-refresh every ${intervalMillis / 60_000} min") }
        autoRefreshJob = scope.launch {
            while (isActive) {
                delay(intervalMillis)
                refresh(forceLoading = false)
            }
        }
    }

    fun refresh(forceLoading: Boolean = true) {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            if (forceLoading) {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _state.update { it.copy(errorMessage = null) }
            }
            runCatching {
                val base = _state.value.baseCurrency
                val targets = targetDefinitions(base)
                val latest = api.latest(base)
                val histories = targets.map { definition ->
                    async {
                        val history = api.history(base, definition.code, days = 45)
                        definition to history.points
                    }
                }.awaitAll().toMap()

                val liveRates = targets.map { definition ->
                    val history = histories.getValue(definition)
                    val rate = latest.rates.firstOrNull { it.code == definition.code }?.value ?: history.lastOrNull()?.value ?: definition.fallbackRate
                    val change = history.dailyChangePct() ?: definition.fallbackChange
                    FxRate(
                        code = definition.code,
                        name = definition.name,
                        glyph = definition.glyph,
                        kind = CurrencyKind.Fiat,
                        rate = rate,
                        change24h = change,
                        sparkline = history.toSparkline(definition.fallbackSparkline),
                        caption = "1 $base = ${formatCaptionRate(rate)} ${definition.code}",
                    )
                }

                val favoriteRates = basePriority(base, favoriteCodes).mapNotNull { code -> liveRates.firstOrNull { it.code == code } }.take(5)
                val converterRates = buildList {
                    currencyDefinition(base)?.let { definition ->
                        add(FxRate(base, definition.name, definition.glyph, CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 $base = 1.0000 $base"))
                    }
                    basePriority(base, converterCodes).mapNotNull { code -> liveRates.firstOrNull { it.code == code } }.forEach(::add)
                    addAll(CryptoRates.take(1))
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isLive = true,
                        baseCurrency = base,
                        updatedLabel = "${latest.date} · ${latest.provider} · ${refreshTimeLabel()}",
                        autoRefreshLabel = "Auto-refresh every ${AUTO_REFRESH_INTERVAL_MILLIS / 60_000} min",
                        favorites = favoriteRates,
                        converter = converterRates,
                        compare = basePriority(base, compareCodes).mapNotNull { code -> liveRates.firstOrNull { it.code == code } }.take(8),
                        detailSeries = histories.values.firstOrNull()?.toSparkline(DetailSeries) ?: DetailSeries,
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLive = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    private companion object {
        const val AUTO_REFRESH_INTERVAL_MILLIS = 60_000L
    }
}

private data class LiveRateDefinition(
    val code: String,
    val name: String,
    val glyph: String,
    val fallbackRate: Double,
    val fallbackChange: Double,
    val fallbackSparkline: List<Float>,
)

private val favoriteCodes = listOf("EUR", "GBP", "JPY", "CHF", "MXN", "USD")
private val converterCodes = listOf("EUR", "GBP", "JPY", "USD")
private val compareCodes = listOf("EUR", "GBP", "JPY", "CHF", "MXN", "BRL", "CAD", "AUD", "USD")

private val liveDefinitions = listOf(
    LiveRateDefinition("USD", "US Dollar", "🇺🇸", 1.0, 0.0, listOf(1f, 1f)),
    LiveRateDefinition("EUR", "Euro", "🇪🇺", 0.9182, -0.34, FavoriteRates[0].sparkline),
    LiveRateDefinition("GBP", "British Pound", "🇬🇧", 0.7841, 0.12, FavoriteRates[1].sparkline),
    LiveRateDefinition("JPY", "Japanese Yen", "🇯🇵", 156.42, 0.68, FavoriteRates[2].sparkline),
    LiveRateDefinition("CHF", "Swiss Franc", "🇨🇭", 0.8292, -0.08, FavoriteRates[3].sparkline),
    LiveRateDefinition("MXN", "Mexican Peso", "🇲🇽", 18.72, 0.21, FavoriteRates[4].sparkline),
    LiveRateDefinition("BRL", "Brazilian Real", "🇧🇷", 5.692, 0.23, listOf(5.55f, 5.62f, 5.59f, 5.65f, 5.692f)),
    LiveRateDefinition("CAD", "Canadian Dollar", "🇨🇦", 1.372, -0.18, listOf(1.39f, 1.386f, 1.38f, 1.376f, 1.372f)),
    LiveRateDefinition("AUD", "Australian Dollar", "🇦🇺", 1.3838, 0.31, listOf(1.35f, 1.36f, 1.37f, 1.365f, 1.3838f)),
)

val SettingsBaseCurrencies: List<FxRate> = liveDefinitions.map {
    FxRate(it.code, it.name, it.glyph, CurrencyKind.Fiat, it.fallbackRate, it.fallbackChange, it.fallbackSparkline)
}

private fun targetDefinitions(base: String): List<LiveRateDefinition> =
    liveDefinitions.filterNot { it.code == base }

private fun currencyDefinition(code: String): LiveRateDefinition? =
    liveDefinitions.firstOrNull { it.code == code }

private fun basePriority(base: String, codes: List<String>): List<String> =
    codes.filterNot { it == base } + liveDefinitions.map { it.code }.filterNot { it == base || it in codes }

private fun formatCaptionRate(rate: Double): String =
    when {
        rate >= 100 -> rate.toString().take(8)
        rate >= 1 -> rate.toString().take(7)
        else -> rate.toString().take(9)
    }

private fun List<HistoricalPoint>.dailyChangePct(): Double? {
    if (size < 2) return null
    val previous = this[size - 2].value
    if (previous == 0.0) return null
    return ((last().value - previous) / previous) * 100.0
}

private fun List<HistoricalPoint>.toSparkline(fallback: List<Float>): List<Float> =
    takeLast(18)
        .map { it.value.toFloat() }
        .takeIf { it.size >= 2 }
        ?: fallback

private fun refreshTimeLabel(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = now.hour.toString().padStart(2, '0')
    val minute = now.minute.toString().padStart(2, '0')
    return "refreshed $hour:$minute"
}
