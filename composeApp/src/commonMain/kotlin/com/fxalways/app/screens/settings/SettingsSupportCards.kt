package com.fxalways.app.screens.settings

import com.fxalways.app.screens.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.PlatformConfig
import com.fxalways.app.UserBackupState
import com.fxalways.app.refreshFxWidgets
import com.fxalways.app.screens.traveler.travelerDestination
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun StoreListingKitCard(
    appLanguage: String,
    subscriptionState: SubscriptionState,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(appLanguage, subscriptionState.isPremium) { mutableStateOf(false) }
    val planLabel = if (subscriptionState.isPremium) "Pro" else "Free"
    // Listing copy lives in StoreListingCopy.kt (issue #14): title with an intent keyword, wedge-first description.
    val listing = remember(appLanguage) { storeListingFor(appLanguage) }
    val listingTitle = listing.title
    val shortDescription = listing.shortDescription
    val keywords = listing.keywords
    val disclaimer = ui("Rates are indicative and may differ from provider, card or cash exchange rates.")
    val listingText = remember(appLanguage, planLabel, listing, disclaimer) {
        buildString {
            append("FX Always store listing kit\n")
            append("Locale: ${listing.locale} (app language $appLanguage)\n")
            append("Plan context: $planLabel\n")
            append("Title (${listing.title.length}/30): ${listing.title}\n")
            append("Short description (${listing.shortDescription.length}/80): ${listing.shortDescription}\n")
            append("Keywords: ${listing.keywords}\n")
            append("Disclaimer: $disclaimer\n\n")
            append("Full description:\n")
            append(listing.longDescription)
        }
    }
    BentoCard(Modifier.testTag("settings_store_listing_kit"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValueRow(
                ui("Listing draft"),
                listingTitle,
                "${listing.locale} · $planLabel",
                modifier = Modifier.testTag("store_listing_title"),
            )
            KeyValueRow(
                ui("Short description"),
                shortDescription,
                modifier = Modifier.testTag("store_listing_short_description"),
            )
            KeyValueRow(
                ui("Keywords"),
                keywords,
                modifier = Modifier.testTag("store_listing_keywords"),
            )
            KeyValueRow(
                ui("Store disclaimer"),
                disclaimer,
                modifier = Modifier.testTag("store_listing_disclaimer"),
            )
            GhostButton(
                text = if (copied) ui("Copied store listing") else ui("Copy store listing"),
                modifier = Modifier.fillMaxWidth().testTag("store_listing_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(listingText))
                    copied = true
                },
            )
        }
    }
}

@Composable
internal fun WidgetQuickSetupCard(
    baseCurrency: String,
    availableCurrencies: List<FxRate>,
) {
    val candidates = remember(baseCurrency, availableCurrencies) {
        val byCode = availableCurrencies.associateBy { it.code }
        (listOf("EUR", "JPY", "GBP", "MXN", "BRL", "AUD", "CAD", "CHF") + PopularCurrencyCodes)
            .filter { it != baseCurrency }
            .distinct()
            .mapNotNull { byCode[it] }
            .take(6)
            .ifEmpty { availableCurrencies.filterNot { it.code == baseCurrency }.take(6) }
    }
    var widgetTarget by remember { mutableStateOf(AppSettingsPrefs.converterCurrencyCodes().firstOrNull() ?: candidates.firstOrNull()?.code.orEmpty()) }
    var travelerTarget by remember { mutableStateOf(AppSettingsPrefs.travelerCurrency()) }
    var feedback by remember { mutableStateOf<String?>(null) }
    BentoCard(Modifier.testTag("settings_widget_setup"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("Widget quick setup"))
                feedback?.let { Pill(ui(it), variant = PillVariant.Accent) }
            }
            Text(
                ui("Tap to pin this currency to widgets and refresh Android home screen cards."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            KeyValueRow(
                ui("Rates widget pair"),
                "$baseCurrency → ${widgetTarget.ifBlank { "--" }}",
                ui("Converted to"),
                modifier = Modifier.testTag("widget_setup_rates_pair"),
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                candidates.forEach { rate ->
                    TransactionChip(
                        label = rate.code,
                        selected = widgetTarget == rate.code,
                        modifier = Modifier.testTag("widget_setup_rate_${rate.code}"),
                        onClick = {
                            widgetTarget = rate.code
                            AppSettingsPrefs.setConverterCurrencyCodes(
                                (listOf(rate.code) + AppSettingsPrefs.converterCurrencyCodes())
                                    .filter { it != baseCurrency }
                                    .distinct()
                                    .take(4),
                            )
                            refreshFxWidgets()
                            feedback = "Widgets refreshed"
                        },
                    )
                }
            }
            KeyValueRow(
                ui("Traveler widget destination"),
                travelerTarget.ifBlank { "--" },
                travelerDestination(travelerTarget.ifBlank { "JPY" }).city,
                modifier = Modifier.testTag("widget_setup_traveler_destination"),
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                candidates.forEach { rate ->
                    TransactionChip(
                        label = rate.code,
                        selected = travelerTarget == rate.code,
                        modifier = Modifier.testTag("widget_setup_traveler_${rate.code}"),
                        onClick = {
                            travelerTarget = rate.code
                            AppSettingsPrefs.setTravelerCurrency(rate.code)
                            refreshFxWidgets()
                            feedback = "Widgets refreshed"
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun InternalTestPlanCard(
    appLanguage: String,
    baseCurrency: String,
    subscriptionState: SubscriptionState,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(appLanguage, baseCurrency, subscriptionState.isPremium) { mutableStateOf(false) }
    val planLabel = if (subscriptionState.isPremium) "Pro" else "Free"
    val checklist = listOf(
        "Free limits" to "Validate limits, previews and upsells.",
        "Pro unlocks" to "Validate expanded calendars, histories and portfolios.",
        "Offline/cache" to "Validate cached rates and traveler offline pack.",
        "Paywall/legal" to "Validate restore, manage subscription, terms and privacy.",
    )
    val testPlanText = remember(appLanguage, baseCurrency, planLabel) {
        buildString {
            append("FX Always internal test plan\n")
            append("Plan: $planLabel\n")
            append("Base: $baseCurrency\n")
            append("Language: $appLanguage\n")
            checklist.forEach { (title, detail) ->
                append("- $title: $detail\n")
            }
        }.trim()
    }
    BentoCard(Modifier.testTag("settings_internal_test_plan"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValueRow(
                ui("Manual QA checklist"),
                "$planLabel · $baseCurrency · ${appLanguage.uppercase()}",
                ui("Cover before each internal build."),
                modifier = Modifier.testTag("internal_test_plan_summary"),
            )
            checklist.forEachIndexed { index, item ->
                KeyValueRow(
                    ui(item.first),
                    ui(item.second),
                    modifier = Modifier.testTag("internal_test_plan_row_$index"),
                )
            }
            GhostButton(
                text = if (copied) ui("Copied test plan") else ui("Copy test plan"),
                modifier = Modifier.fillMaxWidth().testTag("internal_test_plan_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(testPlanText))
                    copied = true
                },
            )
        }
    }
}

@Composable
internal fun ReleaseReadinessCard(
    appLanguage: String,
    baseCurrency: String,
    backupState: UserBackupState,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(appLanguage, baseCurrency, backupState.uid, subscriptionState.isPremium) { mutableStateOf(false) }
    val backupLabel = when {
        backupState.isAvailable && backupState.isAnonymous -> ui("guest")
        backupState.isAvailable -> ui("signed in")
        else -> ui("offline")
    }
    val planLabel = if (subscriptionState.isPremium) "Pro" else "Free"
    val syncLabel = if (lastSyncedAtMillis != null) localizedShortAgeLabel(lastSyncedAtMillis) else ui("Never")
    val supportSnapshot = remember(appLanguage, baseCurrency, backupLabel, planLabel, syncLabel) {
        buildString {
            append("FX Always support snapshot\n")
            append("Version: ${PlatformConfig.versionName}\n")
            append("Plan: $planLabel\n")
            append("Base: $baseCurrency\n")
            append("Language: $appLanguage\n")
            append("Backup: $backupLabel\n")
            append("Last sync: $syncLabel")
        }
    }
    BentoCard(Modifier.testTag("settings_release_readiness"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Build"),
                    PlatformConfig.versionName,
                    ui("Ready for tester reports"),
                    Modifier.weight(1f).testTag("release_ready_build"),
                )
                MetricTile(
                    ui("Backup"),
                    backupLabel,
                    syncLabel,
                    Modifier.weight(1f).testTag("release_ready_backup"),
                )
            }
            KeyValueRow(
                ui("Legal"),
                ui("Policies linked"),
                "Terms · Privacy",
                modifier = Modifier.testTag("release_ready_legal"),
            )
            Text(
                ui("Tester context includes plan, base, language and backup state."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            GhostButton(
                text = if (copied) ui("Copied support snapshot") else ui("Copy support snapshot"),
                modifier = Modifier.fillMaxWidth().testTag("release_support_snapshot_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(supportSnapshot))
                    copied = true
                },
            )
        }
    }
}
