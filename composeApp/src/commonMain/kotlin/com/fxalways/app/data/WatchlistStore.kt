package com.fxalways.app.data

import com.fxalways.app.WatchlistPrefs
import kotlinx.datetime.Clock
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
    val holdingCosts: Map<String, Double> = emptyMap(),
    val transactions: List<PortfolioTransaction> = emptyList(),
)

@Serializable
enum class PortfolioTransactionType {
    Buy,
    Sell,
}

@Serializable
data class PortfolioTransaction(
    val id: String,
    val code: String,
    val type: PortfolioTransactionType,
    val amount: Double,
    val priceBase: Double,
    val realizedPnlBase: Double = 0.0,
    val createdAtMillis: Long = Clock.System.now().toEpochMilliseconds(),
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
        val nextHoldings = current.holdings.filterKeys { it in nextCodes }
        val nextHoldingCosts = current.holdingCosts.filterKeys { it in nextCodes }
        val nextTransactions = current.transactions.filter { it.code in nextCodes }
        replace(current.copy(codes = nextCodes, holdings = nextHoldings, holdingCosts = nextHoldingCosts, transactions = nextTransactions))
        return true
    }

    fun setHolding(code: String, amount: Double) {
        val current = _state.value.watchlist
        val nextHoldings = if (amount <= 0.0) {
            current.holdings - code
        } else {
            current.holdings + (code to amount)
        }
        val nextCodes = if (code in current.codes) current.codes else (current.codes + code).distinct()
        replace(current.copy(codes = nextCodes, holdings = nextHoldings))
    }

    fun setHoldingCost(code: String, averageCostBase: Double) {
        val current = _state.value.watchlist
        val nextHoldingCosts = if (averageCostBase <= 0.0) {
            current.holdingCosts - code
        } else {
            current.holdingCosts + (code to averageCostBase)
        }
        replace(current.copy(holdingCosts = nextHoldingCosts))
    }

    fun recordTransaction(code: String, type: PortfolioTransactionType, amount: Double, priceBase: Double) {
        if (amount <= 0.0 || priceBase <= 0.0) return
        val current = _state.value.watchlist
        val currentAmount = current.holdings[code] ?: 0.0
        val currentAverageCost = current.holdingCosts[code] ?: 0.0
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val transactionId = "${code}_${type.name}_${timestamp}_${current.transactions.size}"
        when (type) {
            PortfolioTransactionType.Buy -> {
                val nextAmount = currentAmount + amount
                val currentCost = currentAmount * currentAverageCost
                val nextAverageCost = (currentCost + amount * priceBase) / nextAmount
                val nextCodes = if (code in current.codes) current.codes else (current.codes + code).distinct()
                replace(
                    current.copy(
                        codes = nextCodes,
                        holdings = current.holdings + (code to nextAmount),
                        holdingCosts = current.holdingCosts + (code to nextAverageCost),
                        transactions = current.transactions + PortfolioTransaction(
                            id = transactionId,
                            code = code,
                            type = type,
                            amount = amount,
                            priceBase = priceBase,
                            createdAtMillis = timestamp,
                        ),
                    ),
                )
                return
            }
            PortfolioTransactionType.Sell -> {
                val sellAmount = amount.coerceAtMost(currentAmount)
                if (sellAmount <= 0.0) return
                val nextAmount = currentAmount - sellAmount
                val realizedPnl = if (currentAverageCost > 0.0) {
                    (priceBase - currentAverageCost) * sellAmount
                } else {
                    0.0
                }
                val nextHoldings = if (nextAmount <= 0.0) current.holdings - code else current.holdings + (code to nextAmount)
                val nextCosts = if (nextAmount <= 0.0) current.holdingCosts - code else current.holdingCosts
                PortfolioTransaction(
                    id = transactionId,
                    code = code,
                    type = type,
                    amount = sellAmount,
                    priceBase = priceBase,
                    realizedPnlBase = realizedPnl,
                    createdAtMillis = timestamp,
                ).also {
                    replace(
                        current.copy(
                            holdings = nextHoldings,
                            holdingCosts = nextCosts,
                            transactions = current.transactions + it,
                        ),
                    )
                }
                return
            }
        }
    }

    fun replaceFromBackup(watchlist: Watchlist) {
        replace(watchlist)
    }

    fun importPortfolioCsv(csv: String): PortfolioCsvImportResult {
        val result = _state.value.watchlist.importPortfolioCsv(csv)
        replace(result.watchlist)
        return result
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
