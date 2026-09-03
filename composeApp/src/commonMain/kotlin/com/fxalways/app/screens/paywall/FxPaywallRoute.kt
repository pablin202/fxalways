package com.fxalways.app.screens.paywall

import com.fxalways.app.screens.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.UserProfile
import com.fxalways.app.subscription.SubscriptionGateway
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.observability.Observability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun FxPaywallRoute(
    subscriptionState: SubscriptionState,
    actionInProgress: Boolean,
    userProfile: UserProfile,
    subscriptionGateway: SubscriptionGateway,
    onClose: () -> Unit,
    onSubscriptionStateChange: (SubscriptionState) -> Unit,
    onSubscriptionReadyChange: (Boolean) -> Unit,
    onActionInProgressChange: (Boolean) -> Unit,
    onPaywallVisibleChange: (Boolean) -> Unit,
    source: String = "unknown",
) {
    val scope = rememberCoroutineScope()

    PaywallScreen(
        subscriptionState = subscriptionState,
        actionInProgress = actionInProgress,
        userProfile = userProfile,
        onClose = onClose,
        onStart = { planKind: SubscriptionPlanKind ->
            scope.launch {
                onActionInProgressChange(true)
                try {
                    Observability.event("purchase_started", mapOf("plan" to planKind.name, "source" to source))
                    val updatedState = subscriptionGateway.purchasePlan(planKind)
                    onSubscriptionStateChange(updatedState)
                    AppSettingsPrefs.setCachedPremium(updatedState.isPremium)
                    onSubscriptionReadyChange(true)
                    onPaywallVisibleChange(!updatedState.isPremium)
                    if (updatedState.isPremium) {
                        Observability.event("purchase_success", mapOf("plan" to planKind.name, "source" to source))
                    }
                    Observability.event("purchase_finished", mapOf("premium" to updatedState.isPremium.toString()))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Observability.recordException(error, mapOf("flow" to "purchase", "plan" to planKind.name))
                } finally {
                    onActionInProgressChange(false)
                }
            }
        },
        onRestore = {
            scope.launch {
                onActionInProgressChange(true)
                try {
                    Observability.event("purchase_restore_started", mapOf("source" to "paywall"))
                    val restoredState = subscriptionGateway.restore()
                    onSubscriptionStateChange(restoredState)
                    AppSettingsPrefs.setCachedPremium(restoredState.isPremium)
                    onSubscriptionReadyChange(true)
                    onPaywallVisibleChange(!restoredState.isPremium)
                    Observability.event("purchase_restore_finished", mapOf("premium" to restoredState.isPremium.toString()))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Observability.recordException(error, mapOf("flow" to "purchase_restore", "source" to "paywall"))
                } finally {
                    onActionInProgressChange(false)
                }
            }
        },
        onOpenUrl = ExternalUrlOpener::open,
        onRetryPrices = {
            scope.launch {
                onActionInProgressChange(true)
                try {
                    Observability.event("paywall_prices_retry", mapOf("source" to source))
                    val refreshed = subscriptionGateway.currentState()
                    onSubscriptionStateChange(refreshed)
                    onSubscriptionReadyChange(true)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Observability.recordException(error, mapOf("flow" to "paywall_prices_retry"))
                } finally {
                    onActionInProgressChange(false)
                }
            }
        },
    )
}
