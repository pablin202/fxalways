package com.fxalways.app.data

import com.fxalways.app.domain.DefaultPairs
import com.fxalways.app.domain.HistoricalPoint
import com.fxalways.app.domain.RateCard
import com.fxalways.app.domain.WatchPair
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DashboardRepository(
    private val api: ExchangeApi = ExchangeApi(),
) {
    suspend fun latest(base: String) = api.latest(base)

    suspend fun history(base: String, quote: String, days: Int) = api.history(base, quote, days)

    suspend fun watchCards(pairs: List<WatchPair> = DefaultPairs): List<RateCard> = coroutineScope {
        pairs.map { pair ->
            async {
                val latest = api.latest(pair.base)
                val history = api.history(pair.base, pair.quote, days = 30)
                val rate = latest.rates.firstOrNull { it.code == pair.quote }?.value ?: history.points.lastOrNull()?.value ?: 0.0
                RateCard(
                    pair = pair,
                    rate = rate,
                    dailyChangePct = history.points.dailyChangePct(),
                    sparkline = history.points.takeLast(14),
                )
            }
        }.awaitAll()
    }
}

private fun List<HistoricalPoint>.dailyChangePct(): Double? {
    if (size < 2) return null
    val previous = this[size - 2].value
    if (previous == 0.0) return null
    return ((last().value - previous) / previous) * 100.0
}
