package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupGateway
import com.fxalways.app.UserBackupState
import com.fxalways.app.isDefaultLocalBackup
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.screens.profile.ProfilePreset
import com.fxalways.app.screens.providers.defaultProviderPreferenceCodes
import com.fxalways.app.screens.settings.applyUserBackupSnapshot
import com.fxalways.app.screens.settings.buildUserBackupSnapshot
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.observability.Observability

@Composable
internal fun FxAppPresetDefaultsEffect(
    initialPreset: ProfilePreset,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    travelerCurrency: String,
    watchlistState: WatchlistState,
    watchlistStore: WatchlistStore,
    onConverterCurrencyCodesChange: (List<String>) -> Unit,
    onCompareCurrencyCodesChange: (List<String>) -> Unit,
    onTravelerCurrencyChange: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        if (converterCurrencyCodes.isEmpty()) {
            onConverterCurrencyCodesChange(initialPreset.converterCodes)
            AppSettingsPrefs.setConverterCurrencyCodes(initialPreset.converterCodes)
        }
        if (compareCurrencyCodes.isEmpty()) {
            onCompareCurrencyCodesChange(initialPreset.compareCodes)
            AppSettingsPrefs.setCompareCurrencyCodes(initialPreset.compareCodes)
        }
        if (watchlistState.watchlist == Watchlist()) {
            watchlistStore.replaceFromBackup(Watchlist(codes = initialPreset.watchlistCodes))
        }
        if (travelerCurrency == "JPY" && initialPreset.travelerCurrency != "JPY") {
            onTravelerCurrencyChange(initialPreset.travelerCurrency)
            AppSettingsPrefs.setTravelerCurrency(initialPreset.travelerCurrency)
        }
        if (AppSettingsPrefs.converterAmountText() == "1000" && initialPreset.suggestedAmount != "1000") {
            AppSettingsPrefs.setConverterAmountText(initialPreset.suggestedAmount)
        }
        if (AppSettingsPrefs.providerPreferenceCodes().isEmpty()) {
            AppSettingsPrefs.setProviderPreferenceCodes(providerPreferenceCodes)
        }
    }
}

@Composable
internal fun FxAppRateRefreshEffect(liveStore: LiveRatesStore) {
    LaunchedEffect(liveStore) {
        liveStore.startAutoRefresh()
    }
}

@Composable
internal fun FxAppNewsCurrencyEffect(startupReady: Boolean, baseCurrency: String, newsStore: NewsStore) {
    LaunchedEffect(startupReady, baseCurrency) {
        if (startupReady) {
            newsStore.setCurrency(baseCurrency)
        }
    }
}

@Composable
internal fun FxAppStartupBackupEffect(
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    userProfile: com.fxalways.app.UserProfile,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
    subscriptionGateway: SubscriptionGateway,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    liveStore: LiveRatesStore,
    newsStore: NewsStore,
    onSubscriptionStateChange: (SubscriptionState) -> Unit,
    onSubscriptionReadyChange: (Boolean) -> Unit,
    onBackupStateChange: (UserBackupState) -> Unit,
    onBackupReadyChange: (Boolean) -> Unit,
    onStartupReadyChange: (Boolean) -> Unit,
    onLastSyncedAtMillisChange: (Long?) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onConverterCurrencyCodesChange: (List<String>) -> Unit,
    onCompareCurrencyCodesChange: (List<String>) -> Unit,
    onProviderPreferenceCodesChange: (List<String>) -> Unit,
    onTravelerCurrencyChange: (String) -> Unit,
    onTravelerBudgetBaseChange: (Double) -> Unit,
    onUserProfileChange: (com.fxalways.app.UserProfile) -> Unit,
) {
    LaunchedEffect(Unit) {
        val subscriptionState = subscriptionGateway.currentState()
        onSubscriptionStateChange(subscriptionState)
        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
        onSubscriptionReadyChange(true)
        var backupState = UserBackupGateway.ensureUser()
        onBackupStateChange(backupState)
        if (backupState.isAvailable) {
            runCatching {
                val localSnapshot = buildUserBackupSnapshot(
                    themeMode,
                    appLanguage,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    compareCurrencyCodes,
                    providerPreferenceCodes,
                    userProfile,
                    alertsState,
                    watchlistState,
                )
                val remoteSnapshot = UserBackupGateway.pullSnapshot()
                if (remoteSnapshot != null && localSnapshot.isDefaultLocalBackup()) {
                    val appliedTheme = applyUserBackupSnapshot(
                        snapshot = remoteSnapshot,
                        alertsStore = alertsStore,
                        watchlistStore = watchlistStore,
                        liveStore = liveStore,
                        onConverterCurrencyCodes = onConverterCurrencyCodesChange,
                        onCompareCurrencyCodes = onCompareCurrencyCodesChange,
                        onProviderPreferenceCodes = { onProviderPreferenceCodesChange(it.ifEmpty { defaultProviderPreferenceCodes(baseCurrency) }) },
                        onTravelerCurrency = onTravelerCurrencyChange,
                        onTravelerBudgetBase = onTravelerBudgetBaseChange,
                        onUserProfile = onUserProfileChange,
                        onLanguage = {
                            onLanguageChange(it)
                            newsStore.setLanguage(it)
                        },
                    )
                    onThemeModeChange(appliedTheme)
                    onBaseCurrencyChange(remoteSnapshot.settings.baseCurrency)
                } else if (remoteSnapshot == null) {
                    UserBackupGateway.pushSnapshot(localSnapshot)
                    onLastSyncedAtMillisChange(localSnapshot.updatedAtMillis)
                } else {
                    onLastSyncedAtMillisChange(remoteSnapshot.updatedAtMillis)
                }
            }.onFailure { error ->
                Observability.recordException(error, mapOf("flow" to "startup_backup"))
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
                onBackupStateChange(backupState)
            }
        }
        onBackupReadyChange(backupState.isAvailable)
        onStartupReadyChange(true)
    }
}

@Composable
internal fun FxAppAutoBackupEffect(
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    userProfile: com.fxalways.app.UserProfile,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
    backupReady: Boolean,
    backupState: UserBackupState,
    onBackupStateChange: (UserBackupState) -> Unit,
    onBackupReadyChange: (Boolean) -> Unit,
    onLastSyncedAtMillisChange: (Long?) -> Unit,
) {
    LaunchedEffect(themeMode, appLanguage, baseCurrency, travelerCurrency, travelerBudgetBase, converterCurrencyCodes, compareCurrencyCodes, providerPreferenceCodes, userProfile, alertsState, watchlistState, backupReady) {
        if (backupReady) {
            runCatching {
                val snapshot = buildUserBackupSnapshot(
                    themeMode,
                    appLanguage,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    compareCurrencyCodes,
                    providerPreferenceCodes,
                    userProfile,
                    alertsState,
                    watchlistState,
                )
                UserBackupGateway.pushSnapshot(snapshot)
                onLastSyncedAtMillisChange(snapshot.updatedAtMillis)
            }.onFailure { error ->
                Observability.recordException(error, mapOf("flow" to "auto_backup_sync"))
                onBackupStateChange(backupState.copy(isAvailable = false, errorMessage = error.message))
                onBackupReadyChange(false)
            }
        }
    }
}
