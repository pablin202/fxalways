package com.fxalways.app.screens.profile

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.app.UserProfile
import com.fxalways.app.screens.alerts.QuickAlertState
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BentoTile
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

internal data class ProfileCopy(
    val title: String,
    val label: String,
    val subtitle: String,
    val freeFocus: String,
    val proFocus: String,
)

internal fun UserProfile.copy(): ProfileCopy =
    when (this) {
        UserProfile.Traveler -> ProfileCopy(
            title = "Travel money setup",
            label = "Traveler",
            subtitle = "Trip budget, local cash buffer and destination rates stay near the top.",
            freeFocus = "Budget + core destinations",
            proFocus = "Full cheat sheet + all destinations",
        )
        UserProfile.CryptoHolder -> ProfileCopy(
            title = "Crypto portfolio focus",
            label = "Crypto holder",
            subtitle = "Crypto board, stablecoins and holdings get priority across Home and Portfolio.",
            freeFocus = "BTC, ETH, USDT, USDC",
            proFocus = "Expanded crypto catalog + holdings",
        )
        UserProfile.Remittances -> ProfileCopy(
            title = "Send money smarter",
            label = "Remittances",
            subtitle = "Provider cost, timing and alerts stay visible for repeat transfers.",
            freeFocus = "Mid-market + custom cost",
            proFocus = "Full provider comparison + alerts",
        )
        UserProfile.Freelancer -> ProfileCopy(
            title = "Multi-currency income",
            label = "Freelancer",
            subtitle = "Converter, base currency and income pairs are tuned for cross-border work.",
            freeFocus = "Converter + saved pairs",
            proFocus = "Timing + portfolio + alerts",
        )
        UserProfile.Savings -> ProfileCopy(
            title = "Savings and allocation",
            label = "Savings",
            subtitle = "Portfolio allocation, long-range context and alerts are treated as the main workflow.",
            freeFocus = "Portfolio snapshot",
            proFocus = "P&L, allocation and long history",
        )
    }

internal data class ProfilePreset(
    val initialTab: FxTab,
    val moreRoute: MoreRoute = MoreRoute.Menu,
    val converterCodes: List<String>,
    val compareCodes: List<String>,
    val watchlistCodes: List<String>,
    val travelerCurrency: String,
    val suggestedAmount: String,
    val suggestedPair: String,
    val suggestedProvider: String,
    val suggestedAlert: String,
    val suggestedHolding: String,
)

internal fun UserProfile.preset(): ProfilePreset =
    when (this) {
        UserProfile.Traveler -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Traveler,
            converterCodes = listOf("EUR", "GBP", "JPY"),
            compareCodes = listOf("EUR", "GBP", "JPY", "MXN"),
            watchlistCodes = listOf("EUR", "GBP", "JPY", "MXN"),
            travelerCurrency = "JPY",
            suggestedAmount = "1000",
            suggestedPair = "USD -> JPY",
            suggestedProvider = "Wise / Revolut",
            suggestedAlert = "Destination rate near 30d high",
            suggestedHolding = "Trip cash budget",
        )
        UserProfile.CryptoHolder -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Watchlist,
            converterCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            compareCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            watchlistCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            travelerCurrency = "EUR",
            suggestedAmount = "1000",
            suggestedPair = "USD -> BTC",
            suggestedProvider = "Mid-market crypto rate",
            suggestedAlert = "BTC/ETH daily move above 3%",
            suggestedHolding = "BTC, ETH and stablecoins",
        )
        UserProfile.Remittances -> ProfilePreset(
            initialTab = FxTab.Convert,
            converterCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            compareCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            watchlistCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            travelerCurrency = "MXN",
            suggestedAmount = "500",
            suggestedPair = "USD -> MXN",
            suggestedProvider = "Wise first, compare bank transfer",
            suggestedAlert = "Target rate above last 7d average",
            suggestedHolding = "Receiver currency balance",
        )
        UserProfile.Freelancer -> ProfilePreset(
            initialTab = FxTab.Convert,
            converterCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            compareCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            watchlistCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            travelerCurrency = "EUR",
            suggestedAmount = "2500",
            suggestedPair = "USD -> EUR",
            suggestedProvider = "Wise / bank transfer",
            suggestedAlert = "Invoice pair moves 1% in a day",
            suggestedHolding = "Client payment currencies",
        )
        UserProfile.Savings -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Watchlist,
            converterCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            compareCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            watchlistCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            travelerCurrency = "CHF",
            suggestedAmount = "1000",
            suggestedPair = "USD -> CHF",
            suggestedProvider = "Mid-market baseline",
            suggestedAlert = "Portfolio allocation drift above 5%",
            suggestedHolding = "Core savings currencies",
        )
    }

private data class ProfileAction(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
)

private data class ProfileWorkflow(
    val primary: String,
    val nextStep: String,
    val proFit: String,
)

private fun UserProfile.workflowCopy(): ProfileWorkflow =
    when (this) {
        UserProfile.Traveler -> ProfileWorkflow("Trip wallet + scanner", "Scan local price before paying", "Saved trips, OCR and full cheat sheet")
        UserProfile.CryptoHolder -> ProfileWorkflow("Crypto watch + breakouts", "Create movement alert", "Expanded crypto catalog and advanced alerts")
        UserProfile.Remittances -> ProfileWorkflow("Repeat transfer decision", "Compare provider route now", "Provider matrix, recurring plan and unlimited alerts")
        UserProfile.Freelancer -> ProfileWorkflow("Invoice currency control", "Check invoice amount and timing", "Timing horizons and saved working pairs")
        UserProfile.Savings -> ProfileWorkflow("Allocation watch", "Review drift and set alert", "Long-range history and portfolio alerts")
    }

private fun UserProfile.nextActionCopy(): ProfileAction =
    when (this) {
        UserProfile.Traveler -> ProfileAction(
            title = "Scan a local price",
            subtitle = "Open your travel price scanner and compare against the live mid-market rate.",
            actionLabel = "Scan price",
        )
        UserProfile.CryptoHolder -> ProfileAction(
            title = "Create a movement alert",
            subtitle = "Turn your profile signal into an alert before the rate moves away.",
            actionLabel = "Create suggested alert",
        )
        UserProfile.Remittances -> ProfileAction(
            title = "Review transfer cost",
            subtitle = "Check provider loss and hidden markup before sending money.",
            actionLabel = "Review transfer cost",
        )
        UserProfile.Freelancer -> ProfileAction(
            title = "Check invoice currency",
            subtitle = "Keep your working pair, fees and timing visible.",
            actionLabel = "Convert",
        )
        UserProfile.Savings -> ProfileAction(
            title = "Review allocation drift",
            subtitle = "Track savings currencies and long-range movement from one place.",
            actionLabel = "Watchlist",
        )
    }

@Composable
internal fun ProfileInsightCard(
    profile: UserProfile,
    isPremium: Boolean,
    suggestedAlertState: QuickAlertState?,
    modifier: Modifier = Modifier,
    onCreateSuggestedAlert: () -> Unit,
) {
    val copy = profile.copy()
    val preset = profile.preset()
    BentoCard(modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("${ui("FOR YOU")} · ${ui(copy.label)}", color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Free"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            Text(ui(copy.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui(copy.subtitle), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileMetricTile(ui("Free focus"), ui(copy.freeFocus), null, Modifier.weight(1f).testTag("dashboard_profile_free_focus"))
                ProfileMetricTile(ui("Pro focus"), ui(copy.proFocus), null, Modifier.weight(1f).testTag("dashboard_profile_pro_focus"))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileMetricTile(ui("Suggested pair"), preset.suggestedPair, preset.suggestedProvider, Modifier.weight(1f).testTag("dashboard_profile_pair"))
                ProfileMetricTile(ui("Suggested alert"), ui(preset.suggestedAlert), ui(preset.suggestedHolding), Modifier.weight(1f).testTag("dashboard_profile_alert"))
            }
            suggestedAlertState?.let { state ->
                PrimaryButton(
                    text = ui(state.profileAlertActionLabel),
                    modifier = Modifier.fillMaxWidth().testTag("dashboard_profile_alert_action"),
                    onClick = onCreateSuggestedAlert,
                )
            }
        }
    }
}

@Composable
internal fun ProfileActionCard(
    profile: UserProfile,
    onCreateSuggestedAlert: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenTraveler: () -> Unit,
    onOpenWatchlist: () -> Unit,
) {
    val action = profile.nextActionCopy()
    BentoCard(Modifier.testTag("dashboard_profile_action"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(ui("PROFILE ACTION"), color = FxTheme.colors.accent)
            Text(ui(action.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui(action.subtitle), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            GhostButton(
                text = ui(action.actionLabel),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_profile_action_button"),
                onClick = {
                    Observability.event("profile_action_clicked", mapOf("profile" to profile.name, "action" to action.title))
                    when (profile) {
                        UserProfile.Traveler -> onOpenTraveler()
                        UserProfile.CryptoHolder -> onCreateSuggestedAlert()
                        UserProfile.Remittances,
                        UserProfile.Freelancer -> onOpenConverter()
                        UserProfile.Savings -> onOpenWatchlist()
                    }
                },
            )
        }
    }
}

@Composable
internal fun ProfileWorkflowCard(profile: UserProfile, isPremium: Boolean) {
    val workflow = profile.workflowCopy()
    BentoCard(Modifier.testTag("dashboard_profile_workflow"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("PROFILE WORKFLOW"), color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Free"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            KeyValueRow(ui("Primary workflow"), ui(workflow.primary), null, modifier = Modifier.testTag("dashboard_profile_workflow_primary"))
            KeyValueRow(ui("Recommended next step"), ui(workflow.nextStep), null, modifier = Modifier.testTag("dashboard_profile_workflow_next"))
            KeyValueRow(ui("Monetization fit"), ui(workflow.proFit), null, modifier = Modifier.testTag("dashboard_profile_workflow_pro"))
        }
    }
}

private val QuickAlertState.profileAlertActionLabel: String
    get() = when (this) {
        QuickAlertState.Create -> "Create suggested alert"
        QuickAlertState.Active -> "Suggested alert active"
        QuickAlertState.Paused -> "Reactivate suggested alert"
        QuickAlertState.Locked -> "Unlock suggested alert"
    }

@Composable
private fun ProfileMetricTile(
    label: String,
    value: String,
    sub: String?,
    modifier: Modifier = Modifier,
) {
    BentoTile(
        modifier = modifier.heightIn(min = 108.dp),
        padding = 13.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                label.uppercase(),
                style = FxTheme.typography.eyebrow,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )
            sub?.let {
                Text(
                    it,
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}
