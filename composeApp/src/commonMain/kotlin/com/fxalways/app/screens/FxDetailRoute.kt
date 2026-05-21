package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.DetailUiState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.screens.alerts.canCreateAlert
import com.fxalways.app.screens.alerts.findQuickAlert
import com.fxalways.app.screens.detail.DetailScreen
import com.fxalways.designsystem.components.FxRate

@Composable
internal fun FxDetailRoute(
    rate: FxRate,
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState,
    subscriptionReady: Boolean,
    detailState: DetailUiState,
    newsState: NewsUiState,
    alertsStore: AlertsStore,
    detailStore: DetailStore,
    onBack: () -> Unit,
    onOpenPaywall: (String) -> Unit,
    onOpenStory: (NewsStory, String) -> Unit,
) {
    DetailScreen(
        liveState = liveState,
        alertsState = alertsState,
        subscriptionState = subscriptionState,
        subscriptionReady = subscriptionReady,
        detailState = detailState,
        newsState = newsState,
        rate = rate,
        onBack = onBack,
        onOpenPaywall = { onOpenPaywall("currency_detail") },
        onLoadHistory = detailStore::load,
        onOpenUrl = ExternalUrlOpener::open,
        onOpenStory = { onOpenStory(it, "currency_detail") },
        onCreateAlert = { alertRate ->
            if (
                canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                alertsState.alerts.findQuickAlert(liveState.baseCurrency, alertRate) != null
            ) {
                alertsStore.addQuickAlert(liveState.baseCurrency, alertRate)
            } else {
                onOpenPaywall("currency_detail_alert_limit")
            }
        },
    )
}
