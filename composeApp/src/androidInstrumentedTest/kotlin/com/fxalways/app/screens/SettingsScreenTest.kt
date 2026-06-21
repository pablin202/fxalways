package com.fxalways.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fxalways.app.AndroidAppContext
import com.fxalways.app.ThemeMode
import com.fxalways.app.UserBackupState
import com.fxalways.app.UserProfile
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.screens.dashboard.DashboardScreen
import com.fxalways.app.screens.settings.SettingsScreen
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun anonymousFreeUserCanSyncLinkRestoreAndOpenSubscriptionActions() {
        val harness = renderSettings(
            subscriptionState = SubscriptionState(isPremium = false),
            backupState = UserBackupState(uid = "anon", isAnonymous = true, isAvailable = true),
        )

        compose.onNodeWithTag("settings_backup_card").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("settings_sync_now").performScrollTo().performClick()
        compose.onNodeWithTag("settings_link_account").performScrollTo().performClick()
        compose.onNodeWithTag("settings_subscription").performScrollTo().performClick()
        compose.onNodeWithTag("settings_restore_purchase").performScrollTo().performClick()
        compose.onNodeWithTag("settings_manage_subscription").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(2, harness.syncClicks)
            assertEquals(1, harness.linkClicks)
            assertEquals(1, harness.paywallClicks)
            assertEquals(1, harness.restoreClicks)
            assertEquals(1, harness.openedUrls.size)
        }
    }

    @Test
    fun signedInPremiumUserCanSignOutAndChangePreferences() {
        val harness = renderSettings(
            subscriptionState = SubscriptionState(isPremium = true, activePlanLabel = "Yearly"),
            backupState = UserBackupState(
                uid = "user",
                isAnonymous = false,
                isAvailable = true,
                providerLabel = "Google",
                email = "pablo@example.com",
            ),
            lastSyncedAtMillis = 1_700_000_000_000L,
        )

        compose.onNodeWithText("Signed in with Google").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_sign_out").performScrollTo().performClick()
        compose.onNodeWithTag("settings_theme_Dark").performScrollTo().performClick()
        compose.onNodeWithTag("settings_language_es").performScrollTo().performClick()
        compose.onNodeWithTag("settings_dev_premium").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(1, harness.signOutClicks)
            assertEquals(ThemeMode.Dark, harness.themeMode)
            assertEquals("es", harness.language)
            assertEquals(listOf(false), harness.devPremiumChanges)
        }
    }

    @Test
    fun googleLinkShowsPendingFeedbackAfterTap() {
        val harness = renderSettings(
            backupState = UserBackupState(uid = "anon", isAnonymous = true, isAvailable = true),
        )

        compose.onNodeWithTag("settings_link_account").performScrollTo().performClick()

        compose.onNodeWithTag("settings_link_account_loading").assertIsDisplayed()
        compose.onNodeWithText("connecting").assertIsDisplayed()
        compose.onNodeWithText("Connecting your account securely. Please wait.").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, harness.linkClicks)
        }
    }

    @Test
    fun legalLinksUseCurrentLanguage() {
        val harness = renderSettings(subscriptionState = SubscriptionState(isPremium = true))

        compose.onNodeWithTag("settings_language_es").performScrollTo().performClick()
        compose.onNodeWithTag("settings_privacy_policy").performScrollTo().performClick()
        compose.onNodeWithTag("settings_terms_of_use").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(
                listOf(
                    "https://fxalways.com/legal?doc=privacy&lang=es",
                    "https://fxalways.com/legal?doc=terms&lang=es",
                ),
                harness.openedUrls,
            )
        }
    }

    @Test
    fun releaseReadinessShowsTesterContextAndCopiesSupportSnapshot() {
        renderSettings(
            subscriptionState = SubscriptionState(isPremium = true, activePlanLabel = "Yearly"),
            backupState = UserBackupState(
                uid = "user",
                isAnonymous = false,
                isAvailable = true,
                providerLabel = "Google",
                email = "pablo@example.com",
            ),
            lastSyncedAtMillis = 1_700_000_000_000L,
        )

        compose.onNodeWithTag("settings_release_readiness").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("release_ready_build").assertIsDisplayed()
        compose.onNodeWithTag("release_ready_backup").assertIsDisplayed()
        compose.onNodeWithTag("release_ready_legal").assertIsDisplayed()
        compose.onNodeWithText("Tester context includes plan, base, language and backup state.").assertIsDisplayed()
        compose.onNodeWithTag("release_support_snapshot_copy").performScrollTo().performClick()
        compose.onNodeWithText("Copied support snapshot").assertIsDisplayed()
    }

    @Test
    fun internalTestPlanShowsManualChecklistAndCopiesPlan() {
        renderSettings(subscriptionState = SubscriptionState(isPremium = false))

        compose.onNodeWithTag("settings_internal_test_plan").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("internal_test_plan_summary").assertIsDisplayed()
        compose.onNodeWithTag("internal_test_plan_row_0").assertIsDisplayed()
        compose.onNodeWithTag("internal_test_plan_row_1").assertIsDisplayed()
        compose.onNodeWithTag("internal_test_plan_row_2").assertIsDisplayed()
        compose.onNodeWithTag("internal_test_plan_row_3").assertIsDisplayed()
        compose.onNodeWithText("Validate limits, previews and upsells.").assertIsDisplayed()
        compose.onNodeWithTag("internal_test_plan_copy").performScrollTo().performClick()
        compose.onNodeWithText("Copied test plan").assertIsDisplayed()
    }

    @Test
    fun storeListingKitShowsCopyReadyReleaseMetadata() {
        renderSettings(subscriptionState = SubscriptionState(isPremium = true))

        compose.onNodeWithTag("settings_store_listing_kit").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("store_listing_title").assertIsDisplayed()
        compose.onNodeWithTag("store_listing_short_description").assertIsDisplayed()
        compose.onNodeWithTag("store_listing_keywords").assertIsDisplayed()
        compose.onNodeWithTag("store_listing_disclaimer").assertIsDisplayed()
        compose.onNodeWithText("Live currency converter, alerts, travel tools and portfolio tracking.").assertIsDisplayed()
        compose.onNodeWithTag("store_listing_copy").performScrollTo().performClick()
        compose.onNodeWithText("Copied store listing").assertIsDisplayed()
    }

    @Test
    fun widgetQuickSetupPinsRatesAndTravelerWidgets() {
        renderSettings(subscriptionState = SubscriptionState(isPremium = true))

        compose.onNodeWithTag("settings_widget_setup").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("widget_setup_rates_pair").assertIsDisplayed()
        compose.onNodeWithTag("widget_setup_rate_EUR").performScrollTo().performClick()
        compose.onNodeWithText("Widgets refreshed").assertIsDisplayed()
        compose.onNodeWithTag("widget_setup_traveler_destination").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("widget_setup_traveler_JPY").performScrollTo().performClick()
        compose.onNodeWithText("Widgets refreshed").assertIsDisplayed()
    }

    @Test
    fun freeBaseCurrencyMoreOpensFullPickerWithoutPaywall() {
        val harness = renderSettings(subscriptionState = SubscriptionState(isPremium = false))

        compose.onNodeWithTag("settings_base_EUR").performScrollTo().performClick()
        compose.onNodeWithTag("settings_more_base_currencies").performScrollTo().performClick()
        compose.onNodeWithTag("currency_picker_search").performTextReplacement("mex")
        compose.onNodeWithTag("currency_picker_MXN").assertIsDisplayed().performClick()

        compose.runOnIdle {
            assertEquals("MXN", harness.baseCurrency)
            assertEquals(0, harness.paywallClicks)
        }
    }

    @Test
    fun premiumBaseCurrencyPickerSearchesAndAppliesSupportedFiat() {
        val harness = renderSettings(subscriptionState = SubscriptionState(isPremium = true))

        compose.onNodeWithTag("settings_more_base_currencies").performScrollTo().performClick()
        compose.onNodeWithTag("currency_picker_search").performTextReplacement("mex")
        compose.onNodeWithTag("currency_picker_MXN").assertIsDisplayed().performClick()

        compose.runOnIdle { assertEquals("MXN", harness.baseCurrency) }
    }

    @Test
    fun unavailableBackupShowsErrorAndStillAllowsLocalSettings() {
        val harness = renderSettings(
            backupState = UserBackupState(isAvailable = false, errorMessage = "Offline backup"),
        )

        compose.onNodeWithText("Backup unavailable").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Offline backup").assertIsDisplayed()
        compose.onNodeWithTag("settings_theme_Light").performScrollTo().performClick()

        compose.runOnIdle { assertEquals(ThemeMode.Light, harness.themeMode) }
    }

    @Test
    fun userCanChangePersonalizedProfileFromSettings() {
        val harness = renderSettings(subscriptionState = SubscriptionState(isPremium = true))

        compose.onNodeWithTag("settings_profile_CryptoHolder").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_profile_Remittances").performScrollTo().performClick()
        compose.onNodeWithTag("settings_profile_Savings").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(UserProfile.Savings, harness.userProfile)
            assertEquals(listOf(UserProfile.Remittances, UserProfile.Savings), harness.profileChanges)
        }
    }

    @Test
    fun profileChangeFromSettingsUpdatesHomeAfterReturning() {
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var route by remember { mutableStateOf("settings") }
            var userProfile by remember { mutableStateOf(UserProfile.Traveler) }

            FxTheme {
                if (route == "settings") {
                    SettingsScreen(
                        themeMode = ThemeMode.System,
                        appLanguage = "en",
                        baseCurrency = "USD",
                        userProfile = userProfile,
                        availableBaseCurrencies = testBaseCurrencies(),
                        backupState = UserBackupState(uid = "anon", isAnonymous = true, isAvailable = true),
                        backupSyncing = false,
                        lastSyncedAtMillis = null,
                        subscriptionState = SubscriptionState(isPremium = true),
                        providerPreferenceCodes = listOf("wise", "revolut"),
                        onBack = { route = "home" },
                        onOpenPaywall = {},
                        onOpenUrl = {},
                        onRestorePurchase = {},
                        onSyncNow = {},
                        onLinkGoogle = {},
                        onSignOut = {},
                        onDevPremiumChange = {},
                        onThemeModeChange = {},
                        onLanguageChange = {},
                        onBaseCurrencyChange = {},
                        onProviderPreferenceCodesChange = {},
                        onUserProfileChange = { userProfile = it },
                    )
                } else {
                    DashboardScreen(
                        liveState = testSettingsLiveRatesState(),
                        subscriptionState = SubscriptionState(isPremium = true),
                        userProfile = userProfile,
                        onRefresh = {},
                        onOpenPaywall = {},
                        onOpenDetail = {},
                        onEditFavorites = {},
                        onSeeAllCrypto = {},
                    )
                }
            }
        }

        compose.onNodeWithTag("settings_profile_Remittances").performScrollTo().performClick()
        compose.onNodeWithText("More").performScrollTo().performClick()

        compose.onNodeWithTag("dashboard_profile_priority").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("dashboard_profile_priority_title").assertTextContains("Review transfer cost")
    }

    @Test
    fun userCanChooseLocalAndLatinAmericaProviderPreferences() {
        val harness = renderSettings(
            subscriptionState = SubscriptionState(isPremium = true),
            initialProviderCodes = listOf("wise", "revolut"),
        )

        compose.onNodeWithTag("settings_provider_preferences").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_provider_market").assertIsDisplayed()
        compose.onNodeWithTag("settings_provider_wise").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_provider_other_mercado_pago").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithTag("settings_provider_other_nequi").performScrollTo().performClick()
        compose.onNodeWithTag("settings_provider_use_primary").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(true, "mercado_pago" in harness.providerPreferenceChanges.first())
            assertEquals(true, "nequi" in harness.providerPreferenceChanges[1])
            assertEquals(true, harness.providerPreferenceChanges.last().contains("wise"))
        }
    }

    @Test
    fun freeProviderPreferencesLimitQuoteProvidersButAllowWalletMethods() {
        val harness = renderSettings(
            subscriptionState = SubscriptionState(isPremium = false),
            initialProviderCodes = listOf("wise", "revolut"),
        )

        compose.onNodeWithTag("settings_provider_preferences").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings_provider_moneygram").performScrollTo().performClick()
        compose.onNodeWithTag("settings_provider_other_mercado_pago").performScrollTo().performClick()
        compose.onNodeWithTag("settings_provider_select_all").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(2, harness.paywallClicks)
            assertEquals(true, harness.providerPreferenceChanges.single().contains("mercado_pago"))
            assertEquals(false, harness.providerPreferenceChanges.single().contains("moneygram"))
        }
    }

    @Test
    fun spanishProviderCatalogCopyIsLocalized() {
        renderSettings(
            subscriptionState = SubscriptionState(isPremium = true),
            initialAppLanguage = "es",
            initialProviderCodes = listOf("wise", "revolut"),
        )

        compose.onNodeWithTag("settings_provider_preferences").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Proveedor de envio", substring = true).onFirst().performScrollTo().assertIsDisplayed()
    }

    private fun renderSettings(
        subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
        backupState: UserBackupState = UserBackupState(uid = "anon", isAnonymous = true, isAvailable = true),
        lastSyncedAtMillis: Long? = null,
        initialUserProfile: UserProfile = UserProfile.Traveler,
        initialProviderCodes: List<String> = listOf("wise", "revolut"),
        initialAppLanguage: String = "en",
    ): SettingsHarness {
        val harness = SettingsHarness()
        AndroidAppContext.init(compose.activity)
        compose.setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.System) }
            var appLanguage by remember { mutableStateOf(initialAppLanguage) }
            var baseCurrency by remember { mutableStateOf("USD") }
            var userProfile by remember { mutableStateOf(initialUserProfile) }

            FxTheme {
                SettingsScreen(
                    themeMode = themeMode,
                    appLanguage = appLanguage,
                    baseCurrency = baseCurrency,
                    userProfile = userProfile,
                    availableBaseCurrencies = testBaseCurrencies(),
                    backupState = backupState,
                    backupSyncing = false,
                    lastSyncedAtMillis = lastSyncedAtMillis,
                    subscriptionState = subscriptionState,
                    providerPreferenceCodes = initialProviderCodes,
                    onOpenPaywall = { harness.paywallClicks += 1 },
                    onOpenUrl = { harness.openedUrls += it },
                    onRestorePurchase = { harness.restoreClicks += 1 },
                    onSyncNow = { harness.syncClicks += 1 },
                    onLinkGoogle = { harness.linkClicks += 1 },
                    onSignOut = { harness.signOutClicks += 1 },
                    onDevPremiumChange = { harness.devPremiumChanges += it },
                    onThemeModeChange = {
                        themeMode = it
                        harness.themeMode = it
                    },
                    onLanguageChange = {
                        appLanguage = it
                        harness.language = it
                    },
                    onBaseCurrencyChange = {
                        baseCurrency = it
                        harness.baseCurrency = it
                    },
                    onProviderPreferenceCodesChange = {
                        harness.providerPreferenceChanges += it
                    },
                    onUserProfileChange = {
                        userProfile = it
                        harness.userProfile = it
                        harness.profileChanges += it
                    },
                )
            }
        }
        return harness
    }

    private fun testBaseCurrencies(): List<FxRate> =
        listOf(
            FxRate("USD", "US Dollar", "$", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f), "1 USD = 1.0000 USD"),
            FxRate("EUR", "Euro", "€", CurrencyKind.Fiat, 0.92, -0.2, listOf(0.91f, 0.92f), "1 USD = 0.9200 EUR"),
            FxRate("GBP", "British Pound", "£", CurrencyKind.Fiat, 0.78, 0.1, listOf(0.77f, 0.78f), "1 USD = 0.7800 GBP"),
            FxRate("JPY", "Japanese Yen", "¥", CurrencyKind.Fiat, 156.0, 0.3, listOf(155f, 156f), "1 USD = 156.0000 JPY"),
            FxRate("AUD", "Australian Dollar", "A$", CurrencyKind.Fiat, 1.52, 0.1, listOf(1.51f, 1.52f), "1 USD = 1.5200 AUD"),
            FxRate("CAD", "Canadian Dollar", "C$", CurrencyKind.Fiat, 1.36, 0.1, listOf(1.35f, 1.36f), "1 USD = 1.3600 CAD"),
            FxRate("CHF", "Swiss Franc", "Fr", CurrencyKind.Fiat, 0.83, -0.1, listOf(0.82f, 0.83f), "1 USD = 0.8300 CHF"),
            FxRate("CNY", "Chinese Yuan", "¥", CurrencyKind.Fiat, 7.2, 0.0, listOf(7.1f, 7.2f), "1 USD = 7.2000 CNY"),
            FxRate("MXN", "Mexican Peso", "$", CurrencyKind.Fiat, 18.72, 0.2, listOf(18.6f, 18.72f), "1 USD = 18.7200 MXN"),
        )

    private fun testSettingsLiveRatesState(): LiveRatesState {
        val rates = testBaseCurrencies()
        val crypto = listOf(
            FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.000015, 2.4, listOf(0.000014f, 0.000015f), "1 USD = 0.000015 BTC"),
            FxRate("ETH", "Ethereum", "Ξ", CurrencyKind.Crypto, 0.00024, 1.2, listOf(0.00023f, 0.00024f), "1 USD = 0.000240 ETH"),
            FxRate("USDT", "Tether", "₮", CurrencyKind.Crypto, 1.0002, 0.01, listOf(1f, 1.0002f), "1 USD = 1.0002 USDT"),
            FxRate("USDC", "USD Coin", "$", CurrencyKind.Crypto, 0.9999, -0.01, listOf(1f, 0.9999f), "1 USD = 0.9999 USDC"),
        )
        return LiveRatesState(
            isLoading = false,
            isLive = true,
            baseCurrency = "USD",
            updatedLabel = "2026-05-14 · test · refreshed 12:00",
            favorites = rates.filterNot { it.code == "USD" },
            converter = rates,
            compare = rates.filterNot { it.code == "USD" },
            crypto = crypto,
            allFiat = rates,
        )
    }

    private class SettingsHarness {
        var syncClicks = 0
        var linkClicks = 0
        var signOutClicks = 0
        var paywallClicks = 0
        var restoreClicks = 0
        var themeMode = ThemeMode.System
        var language = "en"
        var baseCurrency = "USD"
        var userProfile = UserProfile.Traveler
        val openedUrls = mutableListOf<String>()
        val devPremiumChanges = mutableListOf<Boolean>()
        val profileChanges = mutableListOf<UserProfile>()
        val providerPreferenceChanges = mutableListOf<List<String>>()
    }
}
