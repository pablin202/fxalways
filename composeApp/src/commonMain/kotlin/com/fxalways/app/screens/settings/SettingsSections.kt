package com.fxalways.app.screens.settings

import com.fxalways.app.screens.*
import com.fxalways.app.screens.paywall.localizedProStatusLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.DeviceLocale
import com.fxalways.app.NotificationPermissionStatus
import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.screens.profile.copy
import com.fxalways.app.screens.shared.privacyPolicyUrl
import com.fxalways.app.screens.shared.subscriptionManagementUrl
import com.fxalways.app.screens.shared.termsOfUseUrl
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.SectionLabel

@Composable
internal fun BackupSettingsSection(
    copy: SettingsCopy,
    backupState: UserBackupState,
    lastSyncedAtMillis: Long?,
    backupSyncing: Boolean,
    linkIdentityPending: Boolean,
    onSyncNow: () -> Unit,
    onLinkAccount: () -> Unit,
    onSignOut: () -> Unit,
) {
    SectionLabel(copy.backup)
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AccountBackupCard(
                backupState = backupState,
                lastSyncedAtMillis = lastSyncedAtMillis,
                backupSyncing = backupSyncing,
                modifier = Modifier.testTag("settings_backup_card"),
                onClick = onSyncNow,
            )
            SettingChoiceRow(
                title = copy.syncNow,
                subtitle = copy.syncNowSubtitle,
                selected = false,
                actionLabel = if (backupSyncing) copy.syncing else copy.sync,
                modifier = Modifier.testTag("settings_sync_now"),
                enabled = !backupSyncing,
                onClick = onSyncNow,
            )
            if (backupState.isAnonymous) {
                val providerLabel = when (PlatformConfig.platform) {
                    Platform.Android -> "Google"
                    Platform.Ios -> "Apple"
                }
                val deviceLabel = when (PlatformConfig.platform) {
                    Platform.Android -> "Android phone"
                    Platform.Ios -> "iPhone"
                }
                SettingChoiceRow(
                    title = "${copy.signInWith} $providerLabel",
                    subtitle = if (linkIdentityPending) copy.signInProgressSubtitle else "${copy.signInSubtitle} $deviceLabel",
                    selected = false,
                    actionLabel = if (linkIdentityPending) copy.connecting else copy.connect,
                    modifier = Modifier.testTag("settings_link_account"),
                    enabled = !backupSyncing && !linkIdentityPending,
                    isLoading = linkIdentityPending,
                    onClick = onLinkAccount,
                )
            } else {
                SettingChoiceRow(
                    title = copy.signOut,
                    subtitle = copy.signOutSubtitle,
                    selected = false,
                    actionLabel = copy.signOutAction,
                    modifier = Modifier.testTag("settings_sign_out"),
                    onClick = onSignOut,
                )
            }
        }
    }
}

@Composable
internal fun SubscriptionSettingsSection(
    copy: SettingsCopy,
    subscriptionState: com.fxalways.app.subscription.SubscriptionState,
    onOpenPaywall: () -> Unit,
    onRestorePurchase: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    SectionLabel(copy.subscription)
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingChoiceRow(
                title = if (subscriptionState.isPremium) ui("FX/ Pro active") else ui("FX/ Free"),
                subtitle = subscriptionState.statusMessage?.let { localizedSubscriptionMessage(it) } ?: subscriptionState.localizedProStatusLabel(),
                selected = subscriptionState.isPremium,
                actionLabel = if (subscriptionState.isPremium) copy.view else copy.upgrade,
                modifier = Modifier.testTag("settings_subscription"),
                onClick = onOpenPaywall,
            )
            SettingChoiceRow(
                title = copy.restorePurchase,
                subtitle = copy.restorePurchaseSubtitle,
                selected = false,
                actionLabel = copy.restore,
                modifier = Modifier.testTag("settings_restore_purchase"),
                onClick = onRestorePurchase,
            )
            SettingChoiceRow(
                title = copy.manageSubscription,
                subtitle = copy.manageSubscriptionSubtitle,
                selected = false,
                actionLabel = copy.open,
                modifier = Modifier.testTag("settings_manage_subscription"),
                onClick = { onOpenUrl(subscriptionManagementUrl()) },
            )
        }
    }
}

@Composable
internal fun NotificationSettingsSection(copy: SettingsCopy) {
    SectionLabel(copy.notifications)
    BentoCard(padding = 8.dp) {
        SettingChoiceRow(
            title = copy.priceAlertNotifications,
            subtitle = ui(NotificationPermissionStatus.subtitle),
            selected = false,
            actionLabel = ui(NotificationPermissionStatus.actionLabel),
            modifier = Modifier.testTag("settings_notifications"),
            onClick = {},
        )
    }
}

@Composable
internal fun ProfileSettingsSection(
    userProfile: UserProfile,
    onUserProfileChange: (UserProfile) -> Unit,
) {
    SectionLabel(ui("Profile"))
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UserProfile.entries.forEach { profile ->
                val profileCopy = profile.copy()
                SettingChoiceRow(
                    title = ui(profileCopy.label),
                    subtitle = ui(profileCopy.subtitle),
                    selected = userProfile == profile,
                    modifier = Modifier.testTag("settings_profile_${profile.name}"),
                    onClick = { onUserProfileChange(profile) },
                )
            }
        }
    }
}

@Composable
internal fun ThemeSettingsSection(
    copy: SettingsCopy,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    SectionLabel(copy.themeMode)
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemeMode.entries.forEach { mode ->
                SettingChoiceRow(
                    title = ui(mode.label),
                    subtitle = ui(mode.subtitle),
                    selected = themeMode == mode,
                    modifier = Modifier.testTag("settings_theme_${mode.name}"),
                    onClick = { onThemeModeChange(mode) },
                )
            }
        }
    }
}

@Composable
internal fun LanguageSettingsSection(
    copy: SettingsCopy,
    appLanguage: String,
    activeLanguageLabel: String,
    onLanguageChange: (String) -> Unit,
) {
    SectionLabel(copy.language)
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingChoiceRow(
                title = copy.activeLanguage,
                subtitle = "$activeLanguageLabel · ${copy.languageApplied}",
                selected = true,
                actionLabel = appLanguage.uppercase(),
                onClick = {},
            )
            com.fxalways.app.ui.SupportedLanguages.forEach { language ->
                SettingChoiceRow(
                    title = language.label,
                    subtitle = if (language.code == DeviceLocale.language) copy.deviceLanguage else language.code.uppercase(),
                    selected = appLanguage == language.code,
                    modifier = Modifier.testTag("settings_language_${language.code}"),
                    onClick = { onLanguageChange(language.code) },
                )
            }
        }
    }
}

@Composable
internal fun BaseCurrencySettingsSection(
    copy: SettingsCopy,
    baseCurrency: String,
    baseCurrencies: List<FxRate>,
    fullBaseCurrencies: List<FxRate>,
    onBaseCurrencyChange: (String) -> Unit,
    onMoreCurrencies: () -> Unit,
) {
    SectionLabel(copy.baseCurrency)
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            baseCurrencies.forEach { currency ->
                SettingChoiceRow(
                    title = "${currency.glyph}  ${currency.code}",
                    subtitle = localizedCurrencyName(currency.name),
                    selected = baseCurrency == currency.code,
                    modifier = Modifier.testTag("settings_base_${currency.code}"),
                    onClick = { onBaseCurrencyChange(currency.code) },
                )
            }
            SettingChoiceRow(
                title = copy.moreCurrencies,
                subtitle = "${copy.search} ${fullBaseCurrencies.size}",
                selected = false,
                actionLabel = ui("more +"),
                modifier = Modifier.testTag("settings_more_base_currencies"),
                onClick = onMoreCurrencies,
            )
        }
    }
}

@Composable
internal fun DevSettingsSection(
    subscriptionState: com.fxalways.app.subscription.SubscriptionState,
    onDevPremiumChange: (Boolean) -> Unit,
) {
    SectionLabel(ui("DEV"))
    BentoCard(padding = 8.dp) {
        SettingChoiceRow(
            title = "${ui("Simulate")} ${if (subscriptionState.isPremium) ui("Free") else ui("Pro")}",
            subtitle = ui("Debug-only local gate override"),
            selected = subscriptionState.isPremium,
            actionLabel = if (subscriptionState.isPremium) ui("set free") else ui("set pro"),
            modifier = Modifier.testTag("settings_dev_premium"),
            onClick = { onDevPremiumChange(!subscriptionState.isPremium) },
        )
    }
}

@Composable
internal fun LegalSettingsSection(
    copy: SettingsCopy,
    appLanguage: String,
    onOpenUrl: (String) -> Unit,
) {
    SectionLabel(copy.legal)
    BentoCard(padding = 8.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingChoiceRow(
                title = copy.privacyPolicy,
                subtitle = copy.privacyPolicySubtitle,
                selected = false,
                actionLabel = copy.open,
                modifier = Modifier.testTag("settings_privacy_policy"),
                onClick = { onOpenUrl(privacyPolicyUrl(appLanguage)) },
            )
            SettingChoiceRow(
                title = copy.termsOfUse,
                subtitle = copy.termsOfUseSubtitle,
                selected = false,
                actionLabel = copy.open,
                modifier = Modifier.testTag("settings_terms_of_use"),
                onClick = { onOpenUrl(termsOfUseUrl(appLanguage)) },
            )
        }
    }
}

internal val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "System"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
    }

internal val ThemeMode.subtitle: String
    get() = when (this) {
        ThemeMode.System -> "Follow device appearance"
        ThemeMode.Light -> "Use the bright interface"
        ThemeMode.Dark -> "Use the dark trading interface"
    }
