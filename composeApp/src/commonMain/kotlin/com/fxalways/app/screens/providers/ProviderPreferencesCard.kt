package com.fxalways.app.screens.providers

import com.fxalways.app.screens.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun ProviderPreferencesCard(
    baseCurrency: String,
    selectedProviderCodes: List<String>,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
    onProviderPreferenceCodesChange: (List<String>) -> Unit,
) {
    val effectiveSelectedCodes = selectedProviderCodes.cappedProviderPreferenceCodesForPlan(isPremium)
    val selected = effectiveSelectedCodes.toSet()
    val primary = remember(baseCurrency) { primaryProviderOptions(baseCurrency) }
    val other = remember(baseCurrency) { otherProviderOptions(baseCurrency) }
    val primaryQuoteCodes = primary.filter { it.quoteCapable }.map { it.id }
    val allCodes = ProviderCatalog.map { it.id }
    val selectedQuoteCount = effectiveSelectedCodes.quoteCapableProviderCodes().size
    fun toggle(provider: ProviderOption) {
        val next = if (provider.id in selected) {
            effectiveSelectedCodes - provider.id
        } else {
            if (!isPremium && provider.quoteCapable && selectedQuoteCount >= FreeQuoteProviderLimit) {
                onOpenPaywall()
                return
            }
            effectiveSelectedCodes + provider.id
        }
        onProviderPreferenceCodesChange(next.cappedProviderPreferenceCodesForPlan(isPremium))
    }
    BentoCard(Modifier.testTag("settings_provider_preferences"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProviderMarketSummary(
                baseCurrency = baseCurrency,
                isPremium = isPremium,
                selectedQuoteCount = selectedQuoteCount,
                modifier = Modifier.testTag("settings_provider_market"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderPreferenceActionButton(
                    text = ui("Use local"),
                    modifier = Modifier.weight(1f).testTag("settings_provider_use_primary"),
                    onClick = { onProviderPreferenceCodesChange(primaryQuoteCodes.cappedProviderPreferenceCodesForPlan(isPremium)) },
                )
                ProviderPreferenceActionButton(
                    text = ui("Select all"),
                    modifier = Modifier.weight(1f).testTag("settings_provider_select_all"),
                    onClick = {
                        if (isPremium) {
                            onProviderPreferenceCodesChange(allCodes)
                        } else {
                            onOpenPaywall()
                        }
                    },
                )
            }
            Eyebrow(ui("Main providers"))
            primary.forEach { provider ->
                ProviderPreferenceRow(
                    provider = provider,
                    selected = provider.id in selected,
                    modifier = Modifier.testTag("settings_provider_${provider.id}"),
                    onClick = { toggle(provider) },
                )
            }
            Eyebrow(ui("Other markets"))
            other.forEach { provider ->
                ProviderPreferenceRow(
                    provider = provider,
                    selected = provider.id in selected,
                    modifier = Modifier.testTag("settings_provider_other_${provider.id}"),
                    onClick = { toggle(provider) },
                )
            }
        }
    }
}

@Composable
private fun ProviderMarketSummary(
    baseCurrency: String,
    isPremium: Boolean,
    selectedQuoteCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(ui("Provider market"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Pill("${baseCurrency.uppercase()} · ${ui(marketForCurrency(baseCurrency).providerMarketLabel())}", variant = PillVariant.Accent)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(ui("quote providers"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Pill(
                if (isPremium) ui("Unlimited quote providers") else "${selectedQuoteCount.coerceAtMost(FreeQuoteProviderLimit)}/$FreeQuoteProviderLimit ${ui("quote providers")}",
                variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost,
            )
        }
        Text(
            if (isPremium) {
                ui("Main options are local to your base currency; other markets stay selectable.")
            } else {
                "${selectedQuoteCount.coerceAtMost(FreeQuoteProviderLimit)}/$FreeQuoteProviderLimit · ${ui("Wallet-only methods do not count toward the Free provider limit.")}"
            },
            style = FxTheme.typography.caption,
            color = FxTheme.colors.textDim,
        )
    }
}

@Composable
private fun ProviderPreferenceActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(44.dp)
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = FxTheme.typography.bodyStrong,
            color = FxTheme.colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProviderPreferenceRow(
    provider: ProviderOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SettingChoiceRow(
        title = ui(provider.label),
        subtitle = "${ui(provider.category)} · ${ui(provider.quoteMode)}",
        selected = selected,
        actionLabel = if (selected) ui("active") else ui("add"),
        modifier = modifier,
        onClick = onClick,
    )
}
