package com.fxalways.app.subscription

data class SubscriptionState(
    val isPremium: Boolean,
    val canPurchase: Boolean = true,
    val productLabel: String = "$2.99 / month",
    val entitlementId: String = "pro",
    val paywallTitle: String = "FX Always Pro",
    val paywallSubtitle: String = "Extended history, advanced alerts and unlimited portfolios.",
)

interface SubscriptionGateway {
    suspend fun currentState(): SubscriptionState
    suspend fun purchaseMonthly(): SubscriptionState
    suspend fun restore(): SubscriptionState
    suspend fun setDevPremium(enabled: Boolean): SubscriptionState
}

class PlaceholderSubscriptionGateway : SubscriptionGateway {
    private var devPremium = false

    override suspend fun currentState(): SubscriptionState =
        SubscriptionState(isPremium = devPremium)

    override suspend fun purchaseMonthly(): SubscriptionState {
        devPremium = true
        return currentState()
    }

    override suspend fun restore(): SubscriptionState =
        currentState()

    override suspend fun setDevPremium(enabled: Boolean): SubscriptionState {
        devPremium = enabled
        return currentState()
    }
}
