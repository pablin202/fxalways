package com.fxalways.app.subscription

import com.fxalways.app.PlatformConfig
import com.fxalways.app.UserBackupGateway
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package

enum class SubscriptionPlanKind {
    Monthly,
    Yearly,
}

data class SubscriptionPlan(
    val kind: SubscriptionPlanKind,
    val title: String,
    val priceLabel: String,
    val cadenceLabel: String,
    val badge: String? = null,
    val isAvailable: Boolean = true,
)

data class SubscriptionState(
    val isPremium: Boolean,
    val canPurchase: Boolean = true,
    val productLabel: String = "$2.99 / month",
    val plans: List<SubscriptionPlan> = defaultSubscriptionPlans(),
    val activePlanLabel: String? = null,
    val entitlementId: String = "pro",
    val paywallTitle: String = "FX Always Pro",
    val paywallSubtitle: String = "Extended history, advanced alerts and unlimited portfolios.",
    val statusMessage: String? = null,
)

interface SubscriptionGateway {
    suspend fun currentState(): SubscriptionState
    suspend fun purchaseMonthly(): SubscriptionState
    suspend fun purchasePlan(kind: SubscriptionPlanKind): SubscriptionState
    suspend fun restore(): SubscriptionState
    suspend fun setDevPremium(enabled: Boolean): SubscriptionState
}

class PlaceholderSubscriptionGateway : SubscriptionGateway {
    private var devPremium = false

    override suspend fun currentState(): SubscriptionState =
        SubscriptionState(isPremium = devPremium)

    override suspend fun purchaseMonthly(): SubscriptionState =
        purchasePlan(SubscriptionPlanKind.Monthly)

    override suspend fun purchasePlan(kind: SubscriptionPlanKind): SubscriptionState {
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

fun createSubscriptionGateway(): SubscriptionGateway =
    RevenueCatSubscriptionGateway()

private class RevenueCatSubscriptionGateway : SubscriptionGateway {
    private val fallback = PlaceholderSubscriptionGateway()
    private var configuredUserId: String? = null
    private var packagesByKind: Map<SubscriptionPlanKind, Package> = emptyMap()

    override suspend fun currentState(): SubscriptionState {
        if (!hasApiKey()) {
            return unavailableState("RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases.")
        }

        val purchases = ensureConfigured()
        return runCatching {
            val offerings = purchases.awaitOfferings()
            packagesByKind = offerings.current?.toPackagesByKind().orEmpty()
            purchases.awaitCustomerInfo().toSubscriptionState()
        }.getOrElse { error ->
            unavailableState(error.message ?: "RevenueCat unavailable.")
        }
    }

    override suspend fun purchaseMonthly(): SubscriptionState =
        purchasePlan(SubscriptionPlanKind.Monthly)

    override suspend fun purchasePlan(kind: SubscriptionPlanKind): SubscriptionState {
        if (!hasApiKey()) {
            return unavailableState("RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases.")
        }

        val purchases = ensureConfigured()
        if (packagesByKind.isEmpty()) {
            packagesByKind = runCatching {
                purchases.awaitOfferings().current?.toPackagesByKind().orEmpty()
            }.getOrDefault(emptyMap())
        }
        val packageToPurchase = packagesByKind[kind]
            ?: packagesByKind[SubscriptionPlanKind.Monthly]
            ?: packagesByKind.values.firstOrNull()
        if (packageToPurchase == null) {
            return currentState().copy(
                canPurchase = false,
                statusMessage = "No RevenueCat package is configured for ${kind.label}.",
            )
        }

        return runCatching {
            purchases.awaitPurchase(packageToPurchase).customerInfo.toSubscriptionState(selectedKind = kind)
        }.getOrElse { error ->
            currentState().copy(statusMessage = error.message ?: "Purchase did not complete.")
        }
    }

    override suspend fun restore(): SubscriptionState {
        if (!hasApiKey()) {
            return unavailableState("RevenueCat key missing. Restore is not connected yet.")
        }

        return runCatching {
            ensureConfigured().awaitRestore().toSubscriptionState()
        }.getOrElse { error ->
            currentState().copy(statusMessage = error.message ?: "Restore failed.")
        }
    }

    override suspend fun setDevPremium(enabled: Boolean): SubscriptionState =
        fallback.setDevPremium(enabled).copy(
            statusMessage = if (hasApiKey()) "Dev override only affects local debug gating." else null,
        )

    private suspend fun ensureConfigured(): Purchases {
        val account = UserBackupGateway.ensureUser()
        val uid = account.uid ?: error("Firebase user unavailable for RevenueCat")

        if (!Purchases.isConfigured) {
            Purchases.logLevel = LogLevel.DEBUG
            configuredUserId = uid
            return Purchases.configure(apiKey = PlatformConfig.revenueCatApiKey) {
                appUserId = uid
            }
        }

        val purchases = Purchases.sharedInstance
        if (configuredUserId != uid || purchases.appUserID != uid) {
            purchases.awaitLogIn(uid)
            configuredUserId = uid
        }
        return purchases
    }

    private fun hasApiKey(): Boolean =
        PlatformConfig.revenueCatApiKey.isNotBlank() &&
            !PlatformConfig.revenueCatApiKey.contains("YOUR_REVENUECAT", ignoreCase = true)

    private suspend fun unavailableState(message: String): SubscriptionState =
        fallback.currentState().copy(
            canPurchase = false,
            statusMessage = message,
        )

    private fun CustomerInfo.toSubscriptionState(
        selectedKind: SubscriptionPlanKind = SubscriptionPlanKind.Monthly,
    ): SubscriptionState {
        val activeEntitlementId = PRO_ENTITLEMENTS.firstOrNull { entitlements.active.containsKey(it) }
        val activeKind = activePlanKind() ?: selectedKind.takeIf { activeEntitlementId != null }
        val selectedPlan = packagesByKind[selectedKind]
        val plans = packagesByKind.toSubscriptionPlans()
        val activePlan = plans.firstOrNull { it.kind == activeKind }
        val price = selectedPlan?.storeProduct?.price?.formatted?.toReadablePrice()?.let {
            "$it / ${selectedKind.periodLabel}"
        } ?: plans.firstOrNull { it.kind == SubscriptionPlanKind.Monthly }?.priceLabel ?: "$2.99 / month"
        return SubscriptionState(
            isPremium = activeEntitlementId != null,
            canPurchase = plans.any { it.isAvailable },
            productLabel = price,
            plans = plans,
            activePlanLabel = activePlan?.title,
            entitlementId = activeEntitlementId ?: PRO_ENTITLEMENTS.first(),
            statusMessage = when {
                activeEntitlementId != null -> "Pro active${activePlan?.let { " · ${it.title}" }.orEmpty()}."
                plans.none { it.isAvailable } -> "No offering packages are configured in RevenueCat."
                else -> null
            },
        )
    }

    private fun CustomerInfo.activePlanKind(): SubscriptionPlanKind? =
        packagesByKind.entries.firstOrNull { (_, packageForPlan) ->
            activeSubscriptions.any { productId ->
                productId == packageForPlan.storeProduct.id ||
                    productId.startsWith("${packageForPlan.storeProduct.id}:") ||
                    packageForPlan.storeProduct.id.startsWith("$productId:")
            }
        }?.key

    private fun String.toReadablePrice(): String =
        replace(Regex("^([A-Z]{3})(\\d)"), "$1 $2")

    private fun Offering.toPackagesByKind(): Map<SubscriptionPlanKind, Package> =
        buildMap {
            monthly?.let { put(SubscriptionPlanKind.Monthly, it) }
            annual?.let { put(SubscriptionPlanKind.Yearly, it) }
        }

    private fun Map<SubscriptionPlanKind, Package>.toSubscriptionPlans(): List<SubscriptionPlan> =
        defaultSubscriptionPlans().map { fallbackPlan ->
            val packageForPlan = get(fallbackPlan.kind) ?: return@map fallbackPlan.copy(isAvailable = false)
            fallbackPlan.copy(
                priceLabel = packageForPlan.storeProduct.price.formatted.toReadablePrice(),
                isAvailable = true,
            )
        }

    private companion object {
        val PRO_ENTITLEMENTS = setOf("pro", "FX Always Pro")
    }
}

fun defaultSubscriptionPlans(): List<SubscriptionPlan> =
    listOf(
        SubscriptionPlan(
            kind = SubscriptionPlanKind.Monthly,
            title = "Monthly",
            priceLabel = "$2.99",
            cadenceLabel = "Paid every month",
        ),
        SubscriptionPlan(
            kind = SubscriptionPlanKind.Yearly,
            title = "Yearly",
            priceLabel = "$29.99",
            cadenceLabel = "Best long-term value",
            badge = "BEST VALUE",
        ),
    )

private val SubscriptionPlanKind.label: String
    get() = when (this) {
        SubscriptionPlanKind.Monthly -> "monthly"
        SubscriptionPlanKind.Yearly -> "yearly"
    }

private val SubscriptionPlanKind.periodLabel: String
    get() = when (this) {
        SubscriptionPlanKind.Monthly -> "month"
        SubscriptionPlanKind.Yearly -> "year"
    }
