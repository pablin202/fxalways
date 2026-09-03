package com.fxalways.app.screens.dashboard

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.Corridor
import com.fxalways.app.SendCadence
import com.fxalways.app.UserProfile
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.domain.ProviderQuoteDto
import com.fxalways.app.screens.alerts.QuickAlertState
import com.fxalways.app.screens.converter.CustomFeeInput
import com.fxalways.app.screens.converter.EstimatedFeeQuote
import com.fxalways.app.screens.converter.SmartTimingInsight
import com.fxalways.app.screens.converter.estimatedFeeQuotes
import com.fxalways.app.screens.converter.smartTimingInsight
import com.fxalways.app.screens.converter.withBackendProviderQuotes
import com.fxalways.app.screens.profile.preset
import com.fxalways.app.screens.providers.FreeFeeProviderIds
import com.fxalways.app.screens.providers.FreeQuoteProviderLimit
import com.fxalways.app.screens.providers.normalizeProviderPreferenceCodes
import com.fxalways.app.screens.providers.quoteCapableProviderCodes
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

/** "How much arrives today and should I send?" — the first thing a remittance user sees (issue #11). */
internal data class TodayDecision(
    val sourceRate: FxRate,
    val targetRate: FxRate,
    val amount: Double,
    val convertedAmount: Double,
    val bestRoute: EstimatedFeeQuote?,
    val worstRoute: EstimatedFeeQuote?,
    val timing: SmartTimingInsight,
) {
    /** Target-currency amount the best real route delivers over the worst visible one. */
    val savingsVsWorst: Double
        get() = if (bestRoute != null && worstRoute != null) (worstRoute.lossTargetValue - bestRoute.lossTargetValue).coerceAtLeast(0.0) else 0.0
}

/** Corridor used when the user skipped onboarding or the profile came from Settings. */
internal fun UserProfile.defaultCorridor(baseCurrency: String): Corridor {
    val preset = preset()
    val presetTarget = preset.suggestedPair.substringAfter("->", "").trim()
    val target = presetTarget.takeIf { it.isNotBlank() && it != baseCurrency }
        ?: preset.converterCodes.firstOrNull { it != baseCurrency }
        ?: "USD"
    val amount = preset.suggestedAmount.toDoubleOrNull() ?: 500.0
    return Corridor(baseCurrency, target, amount, if (this == UserProfile.Remittances) SendCadence.Monthly else SendCadence.Once)
}

internal fun todayDecision(
    liveState: LiveRatesState,
    corridor: Corridor,
    isPremium: Boolean,
    providerPreferenceCodes: List<String>,
    backendQuotes: List<ProviderQuoteDto> = emptyList(),
): TodayDecision? {
    val base = liveState.baseCurrency
    val available = liveState.converterAvailableRates(isPremium)
    val sourceRate = available.firstOrNull { it.code == base }
        ?: FxRate(code = base, name = base, glyph = "◆", kind = CurrencyKind.Fiat, rate = 1.0, change24h = 0.0, sparkline = emptyList())
    val targetRate = available.firstOrNull { it.code == corridor.target && it.code != base } ?: return null
    val amount = corridor.amount.takeIf { it > 0.0 } ?: return null
    val codes = normalizeProviderPreferenceCodes(providerPreferenceCodes, base, targetRate.code)
    val allQuotes = estimatedFeeQuotes(sourceRate, targetRate, amount, CustomFeeInput(0.0, 0.0, 0.0), codes)
        .withBackendProviderQuotes(backendQuotes, targetRate)
    val visibleQuotes = if (isPremium) {
        allQuotes
    } else {
        val freeIds = FreeFeeProviderIds + codes.quoteCapableProviderCodes().take(FreeQuoteProviderLimit)
        allQuotes.filter { it.providerId in freeIds }
    }
    val realRoutes = visibleQuotes.filterNot { it.providerId in FreeFeeProviderIds }
    return TodayDecision(
        sourceRate = sourceRate,
        targetRate = targetRate,
        amount = amount,
        convertedAmount = convertedAmount(amount, sourceRate, targetRate),
        bestRoute = realRoutes.minByOrNull { it.lossTargetValue },
        worstRoute = realRoutes.maxByOrNull { it.lossTargetValue },
        timing = smartTimingInsight(sourceRate, targetRate),
    )
}

/** "500", "1,000", "2,500.50": whole corridor amounts read like money, not like a rate. */
internal fun formatWholeAmount(amount: Double): String {
    val whole = amount.toLong()
    if (amount != whole.toDouble()) return formatMoneyValue(amount)
    return whole.toString().reversed().chunked(3).joinToString(",").reversed()
}

@Composable
internal fun TodayDecisionCard(
    decision: TodayDecision,
    profile: UserProfile,
    isPremium: Boolean,
    alertState: QuickAlertState?,
    onCreateAlert: () -> Unit,
    onSeeProviders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(decision.sourceRate.code, decision.targetRate.code, decision.timing.signal) {
        Observability.event(
            "send_decision_viewed",
            mapOf(
                "surface" to "home",
                "base" to decision.sourceRate.code,
                "target" to decision.targetRate.code,
                "signal" to decision.timing.signal.lowercase().replace(' ', '_'),
                "plan" to if (isPremium) "pro" else "free",
            ),
        )
    }
    val signalVariant = when (decision.timing.signal) {
        "Strong rate" -> PillVariant.Up
        "Good time" -> PillVariant.Accent
        else -> PillVariant.Ghost
    }
    val alertLabel = when (alertState) {
        QuickAlertState.Active -> "Alert active"
        QuickAlertState.Paused -> "Resume alert"
        QuickAlertState.Locked -> "Unlock alerts"
        else -> "Alert me"
    }
    val reasonPrefix = if (profile == UserProfile.Freelancer) "${ui("If you cash out today")}: " else ""
    BentoCard(modifier.testTag("dashboard_today_decision"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("TODAY'S DECISION"), color = FxTheme.colors.accent)
                Pill(ui(decision.timing.signal), variant = signalVariant, modifier = Modifier.testTag("dashboard_today_signal"))
            }
            Text(
                "${decision.sourceRate.code} ${formatWholeAmount(decision.amount)} → ${formatConvertedAmount(decision.targetRate, decision.convertedAmount)}",
                style = FxTheme.typography.numberL,
                color = FxTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("dashboard_today_amount"),
            )
            val route = decision.bestRoute
            Text(
                if (route == null) {
                    ui("Mid-market reference · providers add fees")
                } else {
                    buildString {
                        append("≈ ${route.amount} ${ui("with")} ${ui(route.provider)}")
                        if (decision.savingsVsWorst > 0.0) {
                            append(" · ${decision.targetRate.code} ${formatMoneyValue(decision.savingsVsWorst)} ${ui("more than the worst visible provider")}")
                        }
                    }
                },
                style = FxTheme.typography.caption,
                color = FxTheme.colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("dashboard_today_route"),
            )
            Text(
                reasonPrefix + ui(decision.timing.action),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("dashboard_today_reason"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(
                    text = ui(alertLabel),
                    modifier = Modifier.weight(1f).testTag("dashboard_today_alert"),
                    onClick = onCreateAlert,
                )
                GhostButton(
                    text = ui("See all providers"),
                    modifier = Modifier.weight(1f).testTag("dashboard_today_providers"),
                    onClick = onSeeProviders,
                )
            }
        }
    }
}

@Composable
internal fun HomeMoreToggle(expanded: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("dashboard_more_toggle")
            .clickable(onClick = onToggle)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Eyebrow(if (expanded) ui("Show less") else ui("More on Home"))
            if (!expanded) Text(ui("Favorites, profile tips and upgrade"), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
        Text(if (expanded) "▴" else "▾", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}
