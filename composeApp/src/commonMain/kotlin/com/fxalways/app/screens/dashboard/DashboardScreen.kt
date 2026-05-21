package com.fxalways.app.screens.dashboard

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fxalways.app.UserProfile
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.alerts.QuickAlertState
import com.fxalways.app.screens.alerts.canCreateAlert
import com.fxalways.app.screens.alerts.findMatchingAlert
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.RateTrustCard
import com.fxalways.app.screens.detail.RateTrustDetailsCard
import com.fxalways.app.screens.detail.compactRuntimeLabel
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.app.screens.profile.ProfileActionCard
import com.fxalways.app.screens.profile.ProfileInsightCard
import com.fxalways.app.screens.profile.ProfileWorkflowCard
import com.fxalways.app.screens.profile.preset
import com.fxalways.app.screens.shared.ProUpsellCard
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyRow
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.LiveDot
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

internal data class ProfileAlertSuggestion(
    val rate: FxRate,
    val target: Double,
    val direction: AlertDirection,
    val kind: AlertKind,
)

internal fun suggestedProfileAlert(
    profile: UserProfile,
    liveState: LiveRatesState,
    isPremium: Boolean,
): ProfileAlertSuggestion? {
    val quote = profile.preset().suggestedPair.substringAfter("->", "").trim()
    val rate = liveState.alertRates(isPremium).firstOrNull { it.code == quote } ?: return null
    return when (profile) {
        UserProfile.Traveler -> ProfileAlertSuggestion(rate, rate.rate * 1.005, AlertDirection.Above, AlertKind.Target)
        UserProfile.CryptoHolder -> ProfileAlertSuggestion(rate, 3.0, AlertDirection.Above, AlertKind.DailyChange)
        UserProfile.Remittances -> ProfileAlertSuggestion(rate, rate.rate * 1.01, AlertDirection.Above, AlertKind.Target)
        UserProfile.Freelancer -> ProfileAlertSuggestion(rate, 1.0, AlertDirection.Above, AlertKind.DailyChange)
        UserProfile.Savings -> ProfileAlertSuggestion(rate, 5.0, AlertDirection.Above, AlertKind.DailyChange)
    }
}

internal fun suggestedProfileAlertState(
    profile: UserProfile,
    liveState: LiveRatesState,
    isPremium: Boolean,
    alerts: List<PriceAlert>,
): QuickAlertState? {
    val suggestion = suggestedProfileAlert(profile, liveState, isPremium) ?: return null
    val existing = alerts.findMatchingAlert(
        baseCurrency = liveState.baseCurrency,
        quote = suggestion.rate.code,
        target = suggestion.target,
        direction = suggestion.direction,
        kind = suggestion.kind,
    )
    return when {
        existing?.enabled == true -> QuickAlertState.Active
        existing != null -> QuickAlertState.Paused
        canCreateAlert(SubscriptionState(isPremium = isPremium), alerts.size) -> QuickAlertState.Create
        else -> QuickAlertState.Locked
    }
}

@Composable
fun DashboardScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    trackedCurrencyCodes: List<String> = emptyList(),
    userProfile: UserProfile = UserProfile.Traveler,
    suggestedProfileAlertState: QuickAlertState? = null,
    onRefresh: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
    onEditFavorites: () -> Unit,
    onSeeAllCrypto: () -> Unit,
    onCreateSuggestedAlert: () -> Unit = {},
    onOpenConverter: () -> Unit = {},
    onOpenTraveler: () -> Unit = {},
    onOpenWatchlist: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val preset = userProfile.preset()
    val profileFavorites = remember(liveState.favorites, userProfile, access.favoriteLimit) {
        val ordered = liveState.favorites.sortedWith(compareBy<FxRate> {
            val index = preset.watchlistCodes.indexOf(it.code)
            if (index == -1) Int.MAX_VALUE else index
        }.thenBy { it.code })
        ordered.take(access.favoriteLimit.cap(ordered.size))
    }
    val visibleFavorites = profileFavorites
    val visibleCrypto = liveState.visibleDashboardCryptoRates(subscriptionState.isPremium, trackedCurrencyCodes)
    val cryptoAverageMove = visibleCrypto.takeIf { it.isNotEmpty() }?.map { it.change24h }?.average() ?: 0.0
    val strongestCrypto = visibleCrypto.maxByOrNull { it.change24h }
    val stablecoinCount = visibleCrypto.count { it.code in StablecoinCodes }
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LiveDot(Modifier.size(9.dp))
                Eyebrow(if (liveState.isLive) ui("LIVE") else ui("CACHED"), color = FxTheme.colors.accent)
            }
            Text(compactRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, textAlign = TextAlign.End)
        }
        ScreenHeader(
            title = ui("Rates"),
            subtitle = "${ui("base")} · ${liveState.baseCurrency}  ·  ${visibleFavorites.size}/${liveState.favorites.size} ${ui("favorites")} · ${localizedRuntimeLabel(liveState.autoRefreshLabel)}",
            right = { Text("↻", style = FxTheme.typography.numberL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onRefresh)) },
        )
        RateTrustCard(
            liveState = liveState,
            modifier = Modifier.testTag("dashboard_rate_trust"),
        )
        RateTrustDetailsCard(
            liveState = liveState,
            modifier = Modifier.testTag("dashboard_trust_details"),
        )
        if (liveState.errorMessage != null) {
            Text(ui("Live backend unavailable · using cached UI data"), style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Loading rates"),
                rows = 4,
                modifier = Modifier.testTag("dashboard_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing market cards"),
                rows = 5,
                modifier = Modifier.testTag("dashboard_market_loading_skeleton"),
            )
        } else {
            ProfileInsightCard(
                profile = userProfile,
                isPremium = subscriptionState.isPremium,
                suggestedAlertState = suggestedProfileAlertState,
                modifier = Modifier.testTag("dashboard_profile_card"),
                onCreateSuggestedAlert = onCreateSuggestedAlert,
            )
            ProfileActionCard(
                profile = userProfile,
                onCreateSuggestedAlert = onCreateSuggestedAlert,
                onOpenConverter = onOpenConverter,
                onOpenTraveler = onOpenTraveler,
                onOpenWatchlist = onOpenWatchlist,
            )
            ProfileWorkflowCard(
                profile = userProfile,
                isPremium = subscriptionState.isPremium,
            )
            HeroRateCard(visibleFavorites.firstOrNull() ?: FavoriteRates.first(), liveState.baseCurrency)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(ui("VOLATILITY · 24H"), "0.42%", null, Modifier.weight(1f).height(76.dp))
                liveState.favorites.firstOrNull { it.code == "GBP" }?.let { MetricTile("GBP · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                liveState.favorites.firstOrNull { it.code == "JPY" }?.let { MetricTile("JPY · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
                liveState.favorites.firstOrNull { it.code == "MXN" }?.let { MetricTile("MXN · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
            }
            SectionLabel(
                "${ui("FAVORITES")} · ${visibleFavorites.size}",
                right = if (subscriptionState.isPremium) ui("Edit") else ui("Pro"),
                onRightClick = onEditFavorites,
            )
            BentoCard(padding = 0.dp) {
                Column {
                    visibleFavorites.forEach { rate ->
                        CurrencyRow(localizedRate(rate), dense = true, onClick = { onOpenDetail(rate) })
                    }
                }
            }
            if (!subscriptionState.isPremium) {
                ProUpsellCard(
                    title = ui("Unlock full watchlists"),
                    subtitle = ui("Pro adds more favorites, extended history, alerts and complete fee comparison."),
                    onClick = onOpenPaywall,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).testTag("dashboard_crypto_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Eyebrow(ui("CRYPTO MARKET"))
                Text(
                    ui("See all"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier.testTag("dashboard_crypto_see_all").clickable(onClick = onSeeAllCrypto),
                )
            }
            if (visibleCrypto.isEmpty()) {
                BentoCard(Modifier.testTag("dashboard_crypto_empty"), padding = 12.dp) {
                    Text(ui("No crypto rates yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
                }
            } else {
                BentoCard(Modifier.testTag("dashboard_crypto_snapshot"), padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CryptoMetricTile(ui("Crypto"), "${visibleCrypto.size}", ui("major crypto assets"), Modifier.weight(1f).testTag("dashboard_crypto_count"))
                            CryptoMetricTile(ui("24H avg"), formatChange(cryptoAverageMove), strongestCrypto?.code, Modifier.weight(1f).testTag("dashboard_crypto_avg"))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CryptoMetricTile(ui("Stablecoins"), "$stablecoinCount", "USDT / USDC", Modifier.weight(1f).testTag("dashboard_crypto_stablecoins"))
                            CryptoMetricTile(ui("Strongest"), strongestCrypto?.code ?: "--", strongestCrypto?.let { formatChange(it.change24h) }, Modifier.weight(1f).testTag("dashboard_crypto_strongest"))
                        }
                        Text(ui("live crypto movers"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                    }
                }
                BentoCard(Modifier.testTag("dashboard_crypto_list"), padding = 0.dp) {
                    Column {
                        visibleCrypto.forEach { rate ->
                            CryptoAssetRow(rate, liveState.baseCurrency, onClick = { onOpenDetail(rate) })
                        }
                    }
                }
                if (!subscriptionState.isPremium && liveState.crypto.size > visibleCrypto.size) {
                    Box(Modifier.testTag("dashboard_crypto_upsell")) {
                        ProUpsellCard(
                            title = ui("Unlock full watchlists"),
                            subtitle = ui("Pro shows the full crypto board across compare, alerts and portfolio."),
                            onClick = onOpenPaywall,
                        )
                    }
                }
            }
        }
    }
}
