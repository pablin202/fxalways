package com.fxalways.app.data

import com.fxalways.app.WatchlistPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Watchlist(
    val id: String = "primary",
    val name: String = "Primary watchlist",
    val codes: List<String> = listOf("EUR", "GBP", "JPY"),
    val holdings: Map<String, Double> = emptyMap(),
)

data class WatchlistState(
    val watchlist: Watchlist = Watchlist(),
)

class WatchlistStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(WatchlistState(watchlist = loadWatchlist()))
    val state: StateFlow<WatchlistState> = _state

    fun toggle(code: String, canAdd: Boolean): Boolean {
        val current = _state.value.watchlist
        val nextCodes = if (code in current.codes) {
            current.codes.filterNot { it == code }
        } else {
            if (!canAdd) return false
            current.codes + code
        }
        replace(current.copy(codes = nextCodes))
        return true
    }

    fun setHolding(code: String, amount: Double) {
        val current = _state.value.watchlist
        val nextHoldings = if (amount <= 0.0) {
            current.holdings - code
        } else {
            current.holdings + (code to amount)
        }
        val nextCodes = if (code in current.codes) current.codes else current.codes + code
        replace(current.copy(codes = nextCodes, holdings = nextHoldings))
    }

    fun replaceFromBackup(watchlist: Watchlist) {
        replace(watchlist)
    }

    private fun replace(watchlist: Watchlist) {
        _state.update { it.copy(watchlist = watchlist) }
        WatchlistPrefs.setWatchlistJson(json.encodeToString(WatchlistPayload(watchlist)))
    }

    private fun loadWatchlist(): Watchlist =
        runCatching {
            WatchlistPrefs.watchlistJson()
                ?.let { json.decodeFromString<WatchlistPayload>(it).watchlist }
                ?: Watchlist()
        }.getOrElse { Watchlist() }
}

@Serializable
private data class WatchlistPayload(
    val watchlist: Watchlist = Watchlist(),
)
