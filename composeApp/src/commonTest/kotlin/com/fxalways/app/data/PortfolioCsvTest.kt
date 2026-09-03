package com.fxalways.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortfolioCsvTest {
    private val sample = Watchlist(
        codes = listOf("BTC", "ETH"),
        holdings = mapOf("BTC" to 0.5, "ETH" to 4.0),
        holdingCosts = mapOf("BTC" to 30_000.0),
        transactions = listOf(
            PortfolioTransaction("t2", "ETH", PortfolioTransactionType.Sell, 1.0, 2_100.0, realizedPnlBase = 300.0, createdAtMillis = 2_000L),
            PortfolioTransaction("t1", "BTC", PortfolioTransactionType.Buy, 0.5, 30_000.0, createdAtMillis = 1_000L),
        ),
    )

    @Test
    fun exportThenImportRoundTrips() {
        val csv = sample.toPortfolioCsv()
        assertTrue(csv.startsWith("record_type,code,type,amount,price_base,realized_pnl_base,created_at_millis,id"))
        val result = Watchlist(codes = emptyList()).importPortfolioCsv(csv, nowMillis = 9_999L)
        assertEquals(2, result.importedHoldings)
        assertEquals(2, result.importedTransactions)
        assertEquals(0, result.skippedRows)
        assertTrue(result.hasImports)
        assertEquals(sample.holdings, result.watchlist.holdings)
        assertEquals(sample.holdingCosts, result.watchlist.holdingCosts)
        assertEquals(listOf("t1", "t2"), result.watchlist.transactions.map { it.id })
        assertEquals(sample.transactions.sortedBy { it.createdAtMillis }, result.watchlist.transactions)
        assertEquals(listOf("BTC", "ETH"), result.watchlist.codes)
    }

    @Test
    fun invalidRowsAreSkippedAndCounted() {
        val csv = """
            record_type,code,type,amount,price_base,realized_pnl_base,created_at_millis,id
            HOLDING,,,1,2,,,
            HOLDING,BTC,,-1,-1,,,
            TRANSACTION,BTC,SWAP,1,100,,,x1
            TRANSACTION,BTC,BUY,0,100,,,x2
            TRANSACTION,BTC,BUY,1,0,,,x3
            DIVIDEND,BTC,,1,1,,,
            HOLDING,ETH,,2,,,,
        """.trimIndent()
        val result = Watchlist(codes = emptyList()).importPortfolioCsv(csv, nowMillis = 1L)
        assertEquals(6, result.skippedRows)
        assertEquals(1, result.importedHoldings)
        assertEquals(0, result.importedTransactions)
        assertEquals(mapOf("ETH" to 2.0), result.watchlist.holdings)
    }

    @Test
    fun duplicateTransactionIdsAreNotImportedTwice() {
        val csv = sample.toPortfolioCsv()
        val once = Watchlist(codes = emptyList()).importPortfolioCsv(csv, nowMillis = 1L)
        val twice = once.watchlist.importPortfolioCsv(csv, nowMillis = 2L)
        assertEquals(0, twice.importedTransactions)
        assertEquals(2, twice.skippedRows)
        assertEquals(2, twice.watchlist.transactions.size)
    }

    @Test
    fun headerIsOptionalAndDecimalCommaIsAccepted() {
        val result = Watchlist(codes = emptyList()).importPortfolioCsv("HOLDING,btc,,0,5,,,,", nowMillis = 1L)
        // "0,5" is split by the CSV comma, so amount=0 and price=5 → cost basis only.
        assertEquals(1, result.importedHoldings)
        assertEquals(mapOf("BTC" to 5.0), result.watchlist.holdingCosts)
        assertTrue(result.watchlist.holdings.isEmpty())
        assertEquals(listOf("BTC"), result.watchlist.codes)
    }

    @Test
    fun emptyCsvChangesNothing() {
        val result = sample.importPortfolioCsv("   \n  ")
        assertEquals(sample, result.watchlist)
        assertFalse(result.hasImports)
    }

    @Test
    fun generatedTransactionIdsAreUniquePerRow() {
        val csv = "TRANSACTION,BTC,BUY,1,100,,,\nTRANSACTION,BTC,BUY,2,100,,,"
        val result = Watchlist(codes = emptyList()).importPortfolioCsv(csv, nowMillis = 42L)
        assertEquals(2, result.importedTransactions)
        assertEquals(2, result.watchlist.transactions.map { it.id }.distinct().size)
    }
}
