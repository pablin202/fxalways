package com.fxalways.app.screens.settings

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fxalways.app.DeviceLocale
import com.fxalways.app.PlatformConfig
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.SettingsBaseCurrencies
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.providers.ProviderPreferencesCard
import com.fxalways.app.screens.providers.normalizeProviderPreferenceCodes
import com.fxalways.app.ui.SupportedLanguages
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    userProfile: UserProfile = UserProfile.Traveler,
    availableBaseCurrencies: List<FxRate> = SettingsBaseCurrencies,
    backupState: UserBackupState,
    backupSyncing: Boolean,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
    providerPreferenceCodes: List<String> = emptyList(),
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRestorePurchase: () -> Unit,
    onSyncNow: () -> Unit,
    onLinkGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDevPremiumChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onProviderPreferenceCodesChange: (List<String>) -> Unit = {},
    onUserProfileChange: (UserProfile) -> Unit = {},
) {
    val copy = settingsCopy(appLanguage)
    val activeLanguage = SupportedLanguages.firstOrNull { it.code == appLanguage }
        ?: SupportedLanguages.first()
    val access = subscriptionState.featureAccess()
    val fullBaseCurrencies = availableBaseCurrencies.ifEmpty { SettingsBaseCurrencies }
    val canUseAllBaseCurrencies = access.hasUnlimitedBaseCurrencies
    val baseCurrencyLimit = if (canUseAllBaseCurrencies) 12 else access.baseCurrencyLimit.cap(fullBaseCurrencies.size)
    val baseCurrencies = remember(fullBaseCurrencies, baseCurrency, baseCurrencyLimit) {
        compactCurrencyChoices(fullBaseCurrencies, baseCurrency, baseCurrencyLimit)
    }
    var showBaseCurrencyPicker by remember { mutableStateOf(false) }
    var linkIdentityPending by remember { mutableStateOf(false) }
    LaunchedEffect(backupSyncing, backupState.isAnonymous) {
        if (!backupSyncing || !backupState.isAnonymous) {
            linkIdentityPending = false
        }
    }
    if (showBaseCurrencyPicker) {
        CurrencyPickerSheet(
            title = ui("Choose base currency"),
            subtitle = "${fullBaseCurrencies.size} ${ui("supported live currencies")}",
            currencies = fullBaseCurrencies,
            selectedCode = baseCurrency,
            onDismiss = { showBaseCurrencyPicker = false },
            onSelect = { code ->
                showBaseCurrencyPicker = false
                onBaseCurrencyChange(code)
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = copy.more, onClick = onBack)
        }
        ScreenHeader(copy.title, sub = copy.sub, subtitle = "${copy.activeLanguage}: ${activeLanguage.label} · ${copy.deviceLanguage}: ${DeviceLocale.language.uppercase()}")

        BackupSettingsSection(
            copy = copy,
            backupState = backupState,
            lastSyncedAtMillis = lastSyncedAtMillis,
            backupSyncing = backupSyncing,
            linkIdentityPending = linkIdentityPending,
            onSyncNow = onSyncNow,
            onLinkAccount = {
                linkIdentityPending = true
                onLinkGoogle()
            },
            onSignOut = onSignOut,
        )

        SubscriptionSettingsSection(
            copy = copy,
            subscriptionState = subscriptionState,
            onOpenPaywall = onOpenPaywall,
            onRestorePurchase = onRestorePurchase,
            onOpenUrl = onOpenUrl,
        )

        SectionLabel(ui("Provider preferences"))
        ProviderPreferencesCard(
            baseCurrency = baseCurrency,
            selectedProviderCodes = normalizeProviderPreferenceCodes(providerPreferenceCodes, baseCurrency),
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
            onProviderPreferenceCodesChange = onProviderPreferenceCodesChange,
        )

        NotificationSettingsSection(copy)

        SectionLabel(ui("WIDGET SETUP"))
        WidgetQuickSetupCard(
            baseCurrency = baseCurrency,
            availableCurrencies = fullBaseCurrencies,
        )

        if (PlatformConfig.isDebug) {
            SectionLabel(ui("RELEASE READINESS"))
            ReleaseReadinessCard(
                appLanguage = appLanguage,
                baseCurrency = baseCurrency,
                backupState = backupState,
                lastSyncedAtMillis = lastSyncedAtMillis,
                subscriptionState = subscriptionState,
            )

            SectionLabel(ui("INTERNAL TEST PLAN"))
            InternalTestPlanCard(
                appLanguage = appLanguage,
                baseCurrency = baseCurrency,
                subscriptionState = subscriptionState,
            )

            SectionLabel(ui("STORE LISTING KIT"))
            StoreListingKitCard(
                appLanguage = appLanguage,
                subscriptionState = subscriptionState,
            )
        }

        ProfileSettingsSection(
            userProfile = userProfile,
            onUserProfileChange = onUserProfileChange,
        )

        ThemeSettingsSection(
            copy = copy,
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
        )

        LanguageSettingsSection(
            copy = copy,
            appLanguage = appLanguage,
            activeLanguageLabel = activeLanguage.label,
            onLanguageChange = onLanguageChange,
        )

        BaseCurrencySettingsSection(
            copy = copy,
            baseCurrency = baseCurrency,
            baseCurrencies = baseCurrencies,
            fullBaseCurrencies = fullBaseCurrencies,
            onBaseCurrencyChange = onBaseCurrencyChange,
            onMoreCurrencies = { showBaseCurrencyPicker = true },
        )

        if (PlatformConfig.isDebug) {
            DevSettingsSection(
                subscriptionState = subscriptionState,
                onDevPremiumChange = onDevPremiumChange,
            )
        }

        LegalSettingsSection(
            copy = copy,
            appLanguage = appLanguage,
            onOpenUrl = onOpenUrl,
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "${ui("Version")} ${PlatformConfig.versionName}",
            style = FxTheme.typography.captionMono,
            color = FxTheme.colors.textFaint,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}
