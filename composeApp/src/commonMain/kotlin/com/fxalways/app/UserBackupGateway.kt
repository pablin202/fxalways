package com.fxalways.app

import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.Watchlist
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class BackupSettings(
    val themeMode: String = ThemeMode.System.name,
    val baseCurrency: String = "USD",
    val travelerCurrency: String = "JPY",
    val travelerBudgetBase: Double = 100.0,
    val converterCurrencyCodes: List<String> = emptyList(),
)

@Serializable
data class UserBackupSnapshot(
    val schemaVersion: Int = 1,
    val updatedAtMillis: Long = Clock.System.now().toEpochMilliseconds(),
    val settings: BackupSettings = BackupSettings(),
    val alerts: List<PriceAlert> = emptyList(),
    val watchlist: Watchlist = Watchlist(),
)

data class UserBackupState(
    val uid: String? = null,
    val isAnonymous: Boolean = true,
    val isAvailable: Boolean = false,
    val providerLabel: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val errorMessage: String? = null,
)

data class AccountLinkResult(
    val state: UserBackupState,
    val snapshot: UserBackupSnapshot,
)

fun UserBackupSnapshot.isDefaultLocalBackup(): Boolean =
    settings.isDefaultLocalSettings() &&
        alerts.isEmpty() &&
        watchlist == Watchlist()

private fun BackupSettings.isDefaultLocalSettings(): Boolean =
    themeMode == ThemeMode.System.name &&
        baseCurrency in setOf("USD", DeviceLocale.currencyCode) &&
        travelerCurrency == "JPY" &&
        travelerBudgetBase == 100.0 &&
        converterCurrencyCodes.isEmpty()

expect object UserBackupGateway {
    suspend fun ensureUser(): UserBackupState
    suspend fun pullSnapshot(): UserBackupSnapshot?
    suspend fun pushSnapshot(snapshot: UserBackupSnapshot)
    suspend fun linkWithGoogle(localSnapshot: UserBackupSnapshot): AccountLinkResult
    suspend fun linkWithApple(localSnapshot: UserBackupSnapshot): AccountLinkResult
    suspend fun signOutToAnonymous(localSnapshot: UserBackupSnapshot): AccountLinkResult
}

fun mergeBackupSnapshots(local: UserBackupSnapshot, remote: UserBackupSnapshot?): UserBackupSnapshot {
    if (remote == null) return local
    val alerts = (remote.alerts + local.alerts).distinctBy { it.id }
    val codes = (remote.watchlist.codes + local.watchlist.codes).distinct()
    val holdings = remote.watchlist.holdings + local.watchlist.holdings
    return local.copy(
        alerts = alerts,
        watchlist = local.watchlist.copy(
            codes = codes,
            holdings = holdings,
        ),
    )
}
