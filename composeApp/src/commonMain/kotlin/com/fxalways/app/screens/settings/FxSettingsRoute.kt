package com.fxalways.app.screens.settings

import com.fxalways.app.screens.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupGateway
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.profile.preset
import com.fxalways.app.screens.providers.defaultProviderPreferenceCodes
import com.fxalways.app.screens.providers.normalizeProviderPreferenceCodes
import com.fxalways.observability.Observability
import kotlinx.coroutines.launch

@Composable
internal fun FxSettingsRoute(
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    providerPreferenceCodes: List<String>,
    userProfile: UserProfile,
    liveState: LiveRatesState,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
    backupState: UserBackupState,
    backupSyncing: Boolean,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
    subscriptionGateway: SubscriptionGateway,
    liveStore: LiveRatesStore,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    newsStore: NewsStore,
    onBack: () -> Unit,
    onOpenPaywall: (String) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onTravelerCurrencyChange: (String) -> Unit,
    onTravelerBudgetBaseChange: (Double) -> Unit,
    onConverterCurrencyCodesChange: (List<String>) -> Unit,
    onCompareCurrencyCodesChange: (List<String>) -> Unit,
    onProviderPreferenceCodesChange: (List<String>) -> Unit,
    onUserProfileChange: (UserProfile) -> Unit,
    onSubscriptionStateChange: (SubscriptionState) -> Unit,
    onSubscriptionReadyChange: (Boolean) -> Unit,
    onBackupStateChange: (UserBackupState) -> Unit,
    onBackupReadyChange: (Boolean) -> Unit,
    onBackupSyncingChange: (Boolean) -> Unit,
    onLastSyncedAtMillisChange: (Long?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SettingsScreen(
        themeMode = themeMode,
        appLanguage = appLanguage,
        baseCurrency = baseCurrency,
        userProfile = userProfile,
        availableBaseCurrencies = liveState.allFiat,
        backupState = backupState,
        backupSyncing = backupSyncing,
        lastSyncedAtMillis = lastSyncedAtMillis,
        subscriptionState = subscriptionState,
        providerPreferenceCodes = providerPreferenceCodes,
        onBack = onBack,
        onOpenPaywall = { onOpenPaywall("settings") },
        onOpenUrl = ExternalUrlOpener::open,
        onRestorePurchase = {
            scope.launch {
                Observability.event("purchase_restore_started", mapOf("source" to "settings"))
                val restoredState = subscriptionGateway.restore()
                onSubscriptionStateChange(restoredState)
                AppSettingsPrefs.setCachedPremium(restoredState.isPremium)
                onSubscriptionReadyChange(true)
                Observability.event("purchase_restore_finished", mapOf("premium" to restoredState.isPremium.toString()))
            }
        },
        onSyncNow = {
            scope.launch {
                onBackupSyncingChange(true)
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
                    onBackupStateChange(UserBackupGateway.ensureUser())
                    onLastSyncedAtMillisChange(snapshot.updatedAtMillis)
                }.onFailure { error ->
                    Observability.recordException(error, mapOf("flow" to "manual_backup_sync"))
                    onBackupStateChange(backupState.copy(errorMessage = error.message))
                }
                onBackupSyncingChange(false)
            }
        },
        onLinkGoogle = {
            scope.launch {
                onBackupSyncingChange(true)
                onBackupReadyChange(false)
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
                    val result = when (PlatformConfig.platform) {
                        Platform.Android -> UserBackupGateway.linkWithGoogle(snapshot)
                        Platform.Ios -> UserBackupGateway.linkWithApple(snapshot)
                    }
                    onBackupStateChange(result.state)
                    val appliedTheme = applyUserBackupSnapshot(
                        snapshot = result.snapshot,
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
                    onBaseCurrencyChange(result.snapshot.settings.baseCurrency)
                    onLastSyncedAtMillisChange(result.snapshot.updatedAtMillis)
                    onBackupReadyChange(true)
                }.onFailure { error ->
                    Observability.recordException(error, mapOf("flow" to "link_backup_identity"))
                    onBackupStateChange(backupState.copy(errorMessage = error.message))
                    onBackupReadyChange(backupState.isAvailable)
                }
                onBackupSyncingChange(false)
            }
        },
        onSignOut = {
            scope.launch {
                onBackupSyncingChange(true)
                onBackupReadyChange(false)
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
                    val result = UserBackupGateway.signOutToAnonymous(snapshot)
                    onBackupStateChange(result.state)
                    onLastSyncedAtMillisChange(result.snapshot.updatedAtMillis)
                    onBackupReadyChange(true)
                }.onFailure { error ->
                    Observability.recordException(error, mapOf("flow" to "sign_out_to_anonymous"))
                    onBackupStateChange(backupState.copy(errorMessage = error.message))
                    onBackupReadyChange(backupState.isAvailable)
                }
                onBackupSyncingChange(false)
            }
        },
        onDevPremiumChange = { enabled ->
            scope.launch {
                val updatedState = subscriptionGateway.setDevPremium(enabled)
                onSubscriptionStateChange(updatedState)
                AppSettingsPrefs.setCachedPremium(updatedState.isPremium)
                onSubscriptionReadyChange(true)
            }
        },
        onThemeModeChange = { mode ->
            Observability.event("theme_changed", mapOf("theme" to mode.name))
            onThemeModeChange(mode)
            AppSettingsPrefs.setThemeMode(mode)
        },
        onLanguageChange = { code ->
            Observability.event("language_changed", mapOf("language" to code))
            onLanguageChange(code)
            AppSettingsPrefs.setLanguage(code)
            newsStore.setLanguage(code)
        },
        onBaseCurrencyChange = { code ->
            Observability.event("base_currency_changed", mapOf("currency" to code))
            onBaseCurrencyChange(code)
            AppSettingsPrefs.setBaseCurrency(code)
            liveStore.setBaseCurrency(code)
        },
        onUserProfileChange = { profile ->
            Observability.event("profile_changed", mapOf("profile" to profile.name))
            val preset = profile.preset()
            onUserProfileChange(profile)
            AppSettingsPrefs.setUserProfile(profile)
            onConverterCurrencyCodesChange(preset.converterCodes)
            onCompareCurrencyCodesChange(preset.compareCodes)
            onTravelerCurrencyChange(preset.travelerCurrency)
            AppSettingsPrefs.setConverterCurrencyCodes(preset.converterCodes)
            AppSettingsPrefs.setCompareCurrencyCodes(preset.compareCodes)
            AppSettingsPrefs.setTravelerCurrency(preset.travelerCurrency)
            AppSettingsPrefs.setConverterAmountText(preset.suggestedAmount)
            if (watchlistState.watchlist.holdings.isEmpty() && watchlistState.watchlist.transactions.isEmpty()) {
                watchlistStore.replaceFromBackup(Watchlist(codes = preset.watchlistCodes))
            }
        },
        onProviderPreferenceCodesChange = { codes ->
            val normalized = normalizeProviderPreferenceCodes(codes, baseCurrency)
            Observability.event("provider_preferences_changed", mapOf("count" to normalized.size.toString(), "base_currency" to baseCurrency))
            onProviderPreferenceCodesChange(normalized)
            AppSettingsPrefs.setProviderPreferenceCodes(normalized)
        },
    )
}
