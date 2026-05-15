package com.fxalways.app.data

import kotlinx.datetime.Clock

data class PortfolioCsvImportResult(
    val watchlist: Watchlist,
    val importedHoldings: Int,
    val importedTransactions: Int,
    val skippedRows: Int,
) {
    val hasImports: Boolean get() = importedHoldings > 0 || importedTransactions > 0
}

fun Watchlist.toPortfolioCsv(): String {
    val rows = mutableListOf("record_type,code,type,amount,price_base,realized_pnl_base,created_at_millis,id")
    codes.distinct().sorted().forEach { code ->
        val amount = holdings[code] ?: 0.0
        val averageCost = holdingCosts[code] ?: 0.0
        if (amount > 0.0 || averageCost > 0.0) {
            rows += listOf(
                "HOLDING",
                code,
                "",
                amount.csvNumber(),
                averageCost.csvNumber(),
                "",
                "",
                "",
            ).joinToString(",")
        }
    }
    transactions.sortedBy { it.createdAtMillis }.forEach { transaction ->
        rows += listOf(
            "TRANSACTION",
            transaction.code,
            transaction.type.name.uppercase(),
            transaction.amount.csvNumber(),
            transaction.priceBase.csvNumber(),
            transaction.realizedPnlBase.csvNumber(),
            transaction.createdAtMillis.toString(),
            transaction.id.csvCell(),
        ).joinToString(",")
    }
    return rows.joinToString("\n")
}

fun Watchlist.importPortfolioCsv(csv: String, nowMillis: Long = Clock.System.now().toEpochMilliseconds()): PortfolioCsvImportResult {
    val lines = csv
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (lines.isEmpty()) return PortfolioCsvImportResult(this, 0, 0, 0)

    val dataLines = if (lines.first().lowercase().startsWith("record_type,")) lines.drop(1) else lines
    var importedHoldings = 0
    var importedTransactions = 0
    var skippedRows = 0
    val nextCodes = codes.toMutableList()
    val nextHoldings = holdings.toMutableMap()
    val nextHoldingCosts = holdingCosts.toMutableMap()
    val nextTransactions = transactions.toMutableList()
    val existingTransactionIds = nextTransactions.map { it.id }.toMutableSet()

    dataLines.forEachIndexed { index, line ->
        val cells = line.split(",").map { it.trim() }
        val recordType = cells.getOrNull(0)?.uppercase().orEmpty()
        val code = cells.getOrNull(1)?.uppercase().orEmpty()
        if (code.isBlank()) {
            skippedRows += 1
            return@forEachIndexed
        }
        when (recordType) {
            "HOLDING" -> {
                val amount = cells.getOrNull(3).parseCsvDouble()
                val averageCost = cells.getOrNull(4).parseCsvDouble()
                if ((amount == null || amount < 0.0) && (averageCost == null || averageCost < 0.0)) {
                    skippedRows += 1
                    return@forEachIndexed
                }
                if (code !in nextCodes) nextCodes += code
                amount?.takeIf { it > 0.0 }?.let { nextHoldings[code] = it }
                averageCost?.takeIf { it > 0.0 }?.let { nextHoldingCosts[code] = it }
                importedHoldings += 1
            }
            "TRANSACTION" -> {
                val type = when (cells.getOrNull(2)?.uppercase()) {
                    "BUY" -> PortfolioTransactionType.Buy
                    "SELL" -> PortfolioTransactionType.Sell
                    else -> null
                }
                val amount = cells.getOrNull(3).parseCsvDouble()
                val priceBase = cells.getOrNull(4).parseCsvDouble()
                if (type == null || amount == null || priceBase == null || amount <= 0.0 || priceBase <= 0.0) {
                    skippedRows += 1
                    return@forEachIndexed
                }
                if (code !in nextCodes) nextCodes += code
                val explicitId = cells.getOrNull(7)?.takeIf { it.isNotBlank() }
                val id = explicitId ?: "${code}_${type.name}_${nowMillis}_${index}"
                if (id in existingTransactionIds) {
                    skippedRows += 1
                    return@forEachIndexed
                }
                existingTransactionIds += id
                nextTransactions += PortfolioTransaction(
                    id = id,
                    code = code,
                    type = type,
                    amount = amount,
                    priceBase = priceBase,
                    realizedPnlBase = cells.getOrNull(5).parseCsvDouble() ?: 0.0,
                    createdAtMillis = cells.getOrNull(6)?.toLongOrNull() ?: (nowMillis + index),
                )
                importedTransactions += 1
            }
            else -> skippedRows += 1
        }
    }

    val nextWatchlist = copy(
        codes = nextCodes.distinct(),
        holdings = nextHoldings,
        holdingCosts = nextHoldingCosts,
        transactions = nextTransactions.sortedBy { it.createdAtMillis },
    )
    return PortfolioCsvImportResult(nextWatchlist, importedHoldings, importedTransactions, skippedRows)
}

private fun String?.parseCsvDouble(): Double? =
    this?.replace(",", ".")?.toDoubleOrNull()

private fun Double.csvNumber(): String =
    if (this == 0.0) "0" else toString()

private fun String.csvCell(): String =
    replace(",", "_").replace("\n", " ").trim()
