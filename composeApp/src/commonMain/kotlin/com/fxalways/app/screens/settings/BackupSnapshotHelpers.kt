package com.fxalways.app.screens.settings

import com.fxalways.app.screens.*
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.BackupSettings
import com.fxalways.app.Corridor
import com.fxalways.app.DeviceLocale
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupSnapshot
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import kotlinx.datetime.Clock

internal fun buildUserBackupSnapshot(
    themeMode: ThemeMode,
    language: String,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    userProfile: UserProfile,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
): UserBackupSnapshot =
    UserBackupSnapshot(
        updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
        settings = BackupSettings(
            themeMode = themeMode.name,
            language = language,
            baseCurrency = baseCurrency,
            travelerCurrency = travelerCurrency,
            travelerBudgetBase = travelerBudgetBase,
            converterCurrencyCodes = converterCurrencyCodes,
            compareCurrencyCodes = compareCurrencyCodes,
            providerPreferenceCodes = providerPreferenceCodes,
            userProfile = userProfile.name,
            corridor = AppSettingsPrefs.corridor()?.encode().orEmpty(),
        ),
        alerts = alertsState.alerts,
        watchlist = watchlistState.watchlist,
    )

internal fun applyUserBackupSnapshot(
    snapshot: UserBackupSnapshot,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    liveStore: LiveRatesStore,
    onConverterCurrencyCodes: (List<String>) -> Unit,
    onCompareCurrencyCodes: (List<String>) -> Unit,
    onProviderPreferenceCodes: (List<String>) -> Unit,
    onTravelerCurrency: (String) -> Unit,
    onTravelerBudgetBase: (Double) -> Unit,
    onUserProfile: (UserProfile) -> Unit,
    onLanguage: (String) -> Unit,
): ThemeMode {
    val theme = ThemeMode.entries.firstOrNull { it.name == snapshot.settings.themeMode } ?: ThemeMode.System
    val language = snapshot.settings.language.ifBlank { DeviceLocale.language }
    val profile = UserProfile.entries.firstOrNull { it.name == snapshot.settings.userProfile } ?: UserProfile.Traveler
    AppSettingsPrefs.setThemeMode(theme)
    AppSettingsPrefs.setLanguage(language)
    AppSettingsPrefs.setBaseCurrency(snapshot.settings.baseCurrency)
    AppSettingsPrefs.setTravelerCurrency(snapshot.settings.travelerCurrency)
    AppSettingsPrefs.setTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    AppSettingsPrefs.setConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    AppSettingsPrefs.setCompareCurrencyCodes(snapshot.settings.compareCurrencyCodes)
    AppSettingsPrefs.setProviderPreferenceCodes(snapshot.settings.providerPreferenceCodes)
    AppSettingsPrefs.setUserProfile(profile)
    Corridor.decode(snapshot.settings.corridor)?.let { AppSettingsPrefs.setCorridor(it) }
    liveStore.setBaseCurrency(snapshot.settings.baseCurrency)
    onLanguage(language)
    onConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    onCompareCurrencyCodes(snapshot.settings.compareCurrencyCodes)
    onProviderPreferenceCodes(snapshot.settings.providerPreferenceCodes)
    onTravelerCurrency(snapshot.settings.travelerCurrency)
    onTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    onUserProfile(profile)
    alertsStore.replaceAll(snapshot.alerts)
    watchlistStore.replaceFromBackup(snapshot.watchlist)
    return theme
}
