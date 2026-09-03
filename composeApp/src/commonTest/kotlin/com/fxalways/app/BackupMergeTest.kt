package com.fxalways.app

import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.PortfolioTransaction
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.Watchlist
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class BackupMergeTest {
    private fun alert(id: String, target: Double = 1.0) =
        PriceAlert(id = id, base = "USD", quote = "MXN", target = target, direction = AlertDirection.Above)

    private fun tx(id: String, at: Long) =
        PortfolioTransaction(id, "BTC", PortfolioTransactionType.Buy, 1.0, 100.0, createdAtMillis = at)

    private val customSettings = BackupSettings(themeMode = ThemeMode.Dark.name, language = "es", baseCurrency = "AUD")

    @Test
    fun withoutRemoteTheLocalSnapshotIsReturnedAsIs() {
        val local = UserBackupSnapshot(alerts = listOf(alert("a")))
        assertSame(local, mergeBackupSnapshots(local, null))
    }

    @Test
    fun alertsAreUnionedWithoutDuplicatesAndRemoteWinsOnConflict() {
        val local = UserBackupSnapshot(settings = customSettings, alerts = listOf(alert("a", 1.0), alert("b")))
        val remote = UserBackupSnapshot(alerts = listOf(alert("a", 2.0), alert("c")))
        val merged = mergeBackupSnapshots(local, remote)
        assertEquals(listOf("a", "c", "b"), merged.alerts.map { it.id })
        assertEquals(2.0, merged.alerts.first { it.id == "a" }.target)
    }

    @Test
    fun transactionsAreDedupedAndSortedByTime() {
        val local = UserBackupSnapshot(settings = customSettings, watchlist = Watchlist(transactions = listOf(tx("t3", 30), tx("t1", 10))))
        val remote = UserBackupSnapshot(watchlist = Watchlist(transactions = listOf(tx("t2", 20), tx("t1", 10))))
        val merged = mergeBackupSnapshots(local, remote)
        assertEquals(listOf("t1", "t2", "t3"), merged.watchlist.transactions.map { it.id })
    }

    @Test
    fun watchlistCodesAndHoldingsAreUnionedWithLocalHoldingsWinning() {
        val local = UserBackupSnapshot(
            settings = customSettings,
            watchlist = Watchlist(codes = listOf("EUR", "BTC"), holdings = mapOf("BTC" to 1.0), holdingCosts = mapOf("BTC" to 20_000.0)),
        )
        val remote = UserBackupSnapshot(
            watchlist = Watchlist(codes = listOf("GBP", "EUR"), holdings = mapOf("BTC" to 0.5, "ETH" to 3.0), holdingCosts = mapOf("ETH" to 1_500.0)),
        )
        val merged = mergeBackupSnapshots(local, remote)
        assertEquals(listOf("GBP", "EUR", "BTC"), merged.watchlist.codes)
        assertEquals(mapOf("BTC" to 1.0, "ETH" to 3.0), merged.watchlist.holdings)
        assertEquals(mapOf("BTC" to 20_000.0, "ETH" to 1_500.0), merged.watchlist.holdingCosts)
    }

    @Test
    fun customizedLocalSettingsWinOverRemote() {
        val local = UserBackupSnapshot(settings = customSettings)
        val remote = UserBackupSnapshot(settings = BackupSettings(themeMode = ThemeMode.Light.name, baseCurrency = "EUR"))
        assertEquals(customSettings, mergeBackupSnapshots(local, remote).settings)
    }

    @Test
    fun untouchedLocalInstallAdoptsRemoteSettings() {
        val local = UserBackupSnapshot() // default settings, no alerts, default watchlist
        val remoteSettings = BackupSettings(themeMode = ThemeMode.Light.name, language = "pt", baseCurrency = "BRL")
        val remote = UserBackupSnapshot(settings = remoteSettings, alerts = listOf(alert("r")))
        val merged = mergeBackupSnapshots(local, remote)
        assertEquals(remoteSettings, merged.settings)
        assertEquals(listOf("r"), merged.alerts.map { it.id })
    }
}
