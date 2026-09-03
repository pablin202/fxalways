package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.fxalways.app.PlatformBackHandler
import com.fxalways.app.UserBackupState
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.UserProfile
import com.fxalways.observability.Observability

@Composable
internal fun FxAppScreenTrackingEffect(
    selectedTab: FxTab,
    moreRoute: MoreRoute,
    detailRateVisible: Boolean,
    detailNewsStoryVisible: Boolean,
    showPaywall: Boolean,
    startupReady: Boolean,
    baseCurrency: String,
    appLanguage: String,
) {
    LaunchedEffect(selectedTab, moreRoute, detailRateVisible, detailNewsStoryVisible, showPaywall, startupReady) {
        if (startupReady) {
            val screenName = when {
                showPaywall -> "paywall"
                detailNewsStoryVisible -> "news_detail"
                detailRateVisible -> "currency_detail"
                selectedTab == FxTab.More -> moreRoute.analyticsName
                else -> selectedTab.label
            }
            Observability.screen(
                screenName,
                mapOf(
                    "tab" to selectedTab.label,
                    "base_currency" to baseCurrency,
                    "language" to appLanguage,
                ),
            )
        }
    }
}

@Composable
internal fun FxAppUserTrackingEffect(
    subscriptionState: SubscriptionState,
    backupState: UserBackupState,
    userProfile: UserProfile,
    baseCurrency: String,
) {
    LaunchedEffect(subscriptionState.isPremium, backupState.uid, userProfile, baseCurrency) {
        Observability.setUserId(backupState.uid)
        Observability.setUserProperty("premium", subscriptionState.isPremium.toString())
        Observability.setUserProperty("plan", if (subscriptionState.isPremium) "pro" else "free")
        Observability.setUserProperty("profile", userProfile.name.lowercase())
        Observability.setUserProperty("base_currency", baseCurrency)
    }
}

@Composable
internal fun FxAppBackHandler(
    showPaywall: Boolean,
    detailNewsStoryVisible: Boolean,
    detailRateVisible: Boolean,
    selectedTab: FxTab,
    moreRoute: MoreRoute,
    onClosePaywall: () -> Unit,
    onCloseNewsStory: () -> Unit,
    onCloseDetail: () -> Unit,
    onCloseMoreRoute: () -> Unit,
) {
    PlatformBackHandler(enabled = showPaywall || detailNewsStoryVisible || detailRateVisible || selectedTab == FxTab.More && moreRoute != MoreRoute.Menu) {
        when {
            showPaywall -> onClosePaywall()
            detailNewsStoryVisible -> onCloseNewsStory()
            detailRateVisible -> onCloseDetail()
            selectedTab == FxTab.More && moreRoute != MoreRoute.Menu -> onCloseMoreRoute()
        }
    }
}
