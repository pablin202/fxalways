package com.fxalways.app.screens.paywall

import com.fxalways.app.screens.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.UserProfile
import com.fxalways.app.subscription.SubscriptionPlan
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.profile.copy
import com.fxalways.app.screens.profile.formatProfilePair
import com.fxalways.app.screens.profile.preset
import com.fxalways.app.screens.shared.privacyPolicyUrl
import com.fxalways.app.screens.shared.termsOfUseUrl
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BigValueText
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
fun PaywallScreen(
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    actionInProgress: Boolean = false,
    userProfile: UserProfile = UserProfile.Traveler,
    appLanguage: String = LocalAppLanguage.current,
    onClose: () -> Unit = {},
    onStart: (SubscriptionPlanKind) -> Unit = {},
    onRestore: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    var selectedKind by remember { mutableStateOf(SubscriptionPlanKind.Monthly) }
    val selectedPlan = subscriptionState.plans.firstOrNull { it.kind == selectedKind && it.isAvailable }
        ?: subscriptionState.plans.firstOrNull { it.isAvailable }
        ?: subscriptionState.plans.first()
    val profileCopy = userProfile.copy()
    val profilePreset = userProfile.preset()
    val heroTitle = when (userProfile) {
        UserProfile.Traveler -> "Travel with fewer money surprises."
        UserProfile.Remittances -> "Send money with clearer costs."
        UserProfile.Freelancer -> "Protect every cross-border payment."
        UserProfile.CryptoHolder -> "Track currency moves before they matter."
        UserProfile.Savings -> "Keep long-term currency risk visible."
    }
    val heroBody = when (userProfile) {
        UserProfile.Traveler -> "Pro adds live OCR, full traveler tools, unlimited alerts and deeper provider comparison."
        UserProfile.Remittances -> "Pro expands provider routes, recurring alerts and delivery context before you send."
        UserProfile.Freelancer -> "Pro keeps invoice currency, provider loss, alerts and longer history visible."
        UserProfile.CryptoHolder -> "Pro unlocks more tracked assets, alerts, comparison and longer history."
        UserProfile.Savings -> "Pro adds unlimited watchlists, longer history and alerts across every currency you track."
    }
    LaunchedEffect(selectedPlan.kind) {
        selectedKind = selectedPlan.kind
    }

    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textDim, modifier = Modifier.testTag("paywall_close").clickable(onClick = onClose))
        }
        Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
        Text(ui(heroTitle), style = FxTheme.typography.display, color = FxTheme.colors.text)
        Text(
            ui(heroBody),
            style = FxTheme.typography.body,
            color = FxTheme.colors.textDim,
        )
        Text(
            ui("Built for people who move money, travel, track currencies or need alerts before rates move away."),
            style = FxTheme.typography.caption,
            color = FxTheme.colors.textFaint,
        )
        BentoCard(Modifier.testTag("paywall_profile_offer"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Eyebrow("${ui("FOR YOU")} · ${ui(profileCopy.label)}", color = FxTheme.colors.accent)
                Text(ui(profileCopy.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(ui(profileCopy.proFocus), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                PaywallProfileSignal(
                    label = ui("Suggested pair"),
                    value = formatProfilePair(profilePreset.suggestedPair),
                    detail = profilePreset.suggestedProvider,
                )
                PaywallProfileSignal(
                    label = ui("Suggested alert"),
                    value = ui(profilePreset.suggestedAlert),
                    detail = ui(profilePreset.suggestedHolding),
                )
            }
        }
        if (subscriptionState.isPremium) {
            ProActiveCard(subscriptionState = subscriptionState)
        }
        SectionLabel(ui("PRO UNLOCKS"))
        BentoCard(Modifier.testTag("paywall_benefits"), padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BenefitRow("FX", ui("Fresh market rates"), ui("Backend-backed mid-market rates with automatic refresh."))
                BenefitRow("AL", ui("Unlimited alerts"), ui("Price, range, daily and weekly targets."))
                BenefitRow("TR", ui("Traveler mode"), ui("Auto-location, cheat sheets and offline rates."))
                BenefitRow("%", ui("Fee comparison"), ui("Expanded provider estimates by amount and currency pair."))
                BenefitRow("OCR", ui("OCR price scanner"), ui("Camera scanner fills the hidden-cost check from shelf, receipt or cash-desk prices."))
                BenefitRow("WL", ui("Bigger watchlists"), ui("Track more currencies across converter, compare and portfolio."))
                BenefitRow("1Y", ui("Long-range history"), ui("Unlock 1Y and all-time detail views where history is available."))
            }
        }
        SectionLabel(ui("FREE VS PRO"))
        BentoCard(Modifier.testTag("paywall_comparison"), padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaywallComparisonRow("alerts", ui("Custom alerts"), ui("1 active alert"), ui("Unlimited pairs + ranges"))
                PaywallComparisonRow("compare", ui("Compare board"), ui("4 currencies"), ui("Every tracked currency"))
                PaywallComparisonRow("ocr", ui("OCR price scanner"), ui("Manual entry"), ui("Live camera OCR + currency detection"))
                PaywallComparisonRow("crypto", ui("Crypto catalog"), ui("BTC, ETH, USDT, USDC"), ui("Search and add up to 200 crypto assets"))
                PaywallComparisonRow("traveler", ui("Traveler"), ui("Focused destinations"), ui("All destinations + full cheat sheet"))
                PaywallComparisonRow("watchlist", ui("Watchlist"), ui("4 tracked currencies"), ui("Unlimited portfolio tracking"))
                PaywallComparisonRow("news", ui("News"), ui("Top stories only"), ui("Full regional stream"))
                PaywallComparisonRow("history", ui("History"), ui("30 days"), ui("1Y + all-time where available"))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subscriptionState.plans.forEach { plan ->
                PlanOption(
                    plan = plan,
                    selected = plan.kind == selectedPlan.kind,
                    modifier = Modifier.testTag("paywall_plan_${plan.kind.name}"),
                    onSelect = {
                        if (plan.isAvailable) {
                            Observability.event("plan_selected", mapOf("plan" to plan.kind.name))
                            selectedKind = plan.kind
                        }
                    },
                )
            }
        }
        BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card).testTag("paywall_selected_plan"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    selectedPlan.badge?.let { Pill(ui(it), variant = PillVariant.Accent) }
                }
                Text(ui(selectedPlan.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                BigValueText(selectedPlan.priceLabel, ui(selectedPlan.cadenceLabel))
                Text(
                    ui("Recurring subscription billed through Google Play on Android and App Store on iOS."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                )
            }
        }
        subscriptionState.statusMessage?.let {
            Text(
                localizedSubscriptionMessage(it),
                modifier = Modifier.testTag("paywall_status_message"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.down,
            )
        }
        PrimaryButton(
            when {
                actionInProgress -> ui("Processing...")
                subscriptionState.isPremium -> ui("Continue")
                !subscriptionState.canPurchase -> ui("Purchases unavailable")
                else -> ui("Start FX/ Pro")
            },
            enabled = !actionInProgress && (subscriptionState.isPremium || subscriptionState.canPurchase),
            isLoading = actionInProgress,
            onClick = {
                if (actionInProgress) {
                    return@PrimaryButton
                } else if (subscriptionState.isPremium) {
                    onClose()
                } else if (subscriptionState.canPurchase) {
                    onStart(selectedPlan.kind)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("paywall_start_button"),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
                ui("Restore purchase"),
                style = FxTheme.typography.captionMono,
                color = if (actionInProgress) FxTheme.colors.textGhost else FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_restore").clickable(enabled = !actionInProgress, onClick = onRestore),
            )
            Text("  ·  ", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            Text(
                ui("Terms"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_terms").clickable { onOpenUrl(termsOfUseUrl(appLanguage)) },
            )
            Text("  ·  ", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            Text(
                ui("Privacy"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_privacy").clickable { onOpenUrl(privacyPolicyUrl(appLanguage)) },
            )
        }
    }
}

@Composable
private fun PaywallProfileSignal(label: String, value: String, detail: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(label, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        Text(value, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
        Text(detail, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}

@Composable
private fun PaywallComparisonRow(id: String, feature: String, freeValue: String, proValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("paywall_feature_$id")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.54f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(0.92f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(feature, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui("Free"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Text(freeValue, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.End) {
            Text(ui("Pro unlock"), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
            Text(proValue, style = FxTheme.typography.caption, color = FxTheme.colors.text)
        }
    }
}

@Composable
private fun ProActiveCard(subscriptionState: SubscriptionState) {
    BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card).testTag("paywall_active_card"), padding = 12.dp) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot("✓", CurrencyKind.Fiat, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow(ui("ACTIVE"), color = FxTheme.colors.accent)
                Text(ui("FX/ Pro is active"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subscriptionState.localizedProStatusLabel(), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }
    }
}

@Composable
private fun PlanOption(
    plan: SubscriptionPlan,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val borderColor = if (selected) FxTheme.colors.accentLine else FxTheme.colors.border
    val contentAlpha = if (plan.isAvailable) 1f else 0.46f
    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, FxTheme.shapes.card)
            .alpha(contentAlpha)
            .clickable(onClick = onSelect),
        padding = 12.dp,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot(planGlyph(plan.kind), CurrencyKind.Fiat, 40.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ui(plan.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    plan.badge?.let { Pill(ui(it), variant = PillVariant.Accent) }
                }
                Text(ui(plan.cadenceLabel), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(plan.priceLabel, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
                    if (plan.isAvailable) ui("Available") else ui("Not configured"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }
    }
}

private fun planGlyph(kind: SubscriptionPlanKind): String =
    when (kind) {
        SubscriptionPlanKind.Monthly -> "1M"
        SubscriptionPlanKind.Yearly -> "1Y"
    }

@Composable
internal fun SubscriptionState.localizedProStatusLabel(): String =
    if (isPremium) {
        activePlanLabel?.let { "${ui("Active plan")}: $it · $entitlementId" }
            ?: "${ui("Entitlement is active")} · $entitlementId"
    } else {
        ui("Alerts, extended history and unlimited watchlists")
    }

@Composable
private fun BenefitRow(glyph: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(glyph, CurrencyKind.Fiat, 30.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(body, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
}
