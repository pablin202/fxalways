package com.fxalways.app.subscription

import com.fxalways.app.PlatformConfig
import com.fxalways.app.UserBackupGateway
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Offerings
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
    /** Yearly only: the store price divided by 12, e.g. "US$1.67". */
    val monthlyEquivalentLabel: String? = null,
    /** Yearly only: % saved versus paying monthly for a year, when both prices are known. */
    val savingsPercent: Int? = null,
    /** Free-trial length reported by the store, e.g. "7 days". */
    val trialLabel: String? = null,
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
    /**
     * False when [plans] are placeholders (store offerings not loaded): the paywall must show a
     * retry state instead of any price (issue #13).
     */
    val pricesLoaded: Boolean = true,
)

interface SubscriptionGateway {
    suspend fun currentState(): SubscriptionState
    suspend fun purchaseMonthly(): SubscriptionState
    suspend fun purchasePlan(kind: SubscriptionPlanKind): SubscriptionState
    suspend fun restore(): SubscriptionState
    suspend fun setDevPremium(enabled: Boolean): SubscriptionState

    /** Called after the Firebase account was deleted; drops any store identity tied to it. */
    suspend fun onAccountDeleted() = Unit
}

class PlaceholderSubscriptionGateway : SubscriptionGateway {
    private var devPremium = false

    override suspend fun currentState(): SubscriptionState =
        SubscriptionState(isPremium = devPremium, canPurchase = false, plans = unpricedSubscriptionPlans(), pricesLoaded = false)

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
            packagesByKind = offerings.proOffering()?.toPackagesByKind().orEmpty()
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
                purchases.awaitOfferings().proOffering()?.toPackagesByKind().orEmpty()
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

    override suspend fun onAccountDeleted() {
        if (!Purchases.isConfigured) return
        runCatching { Purchases.sharedInstance.awaitLogOut() }
        configuredUserId = null
    }

    private suspend fun ensureConfigured(): Purchases {
        val account = UserBackupGateway.ensureUser()
        val uid = account.uid

        if (!Purchases.isConfigured) {
            Purchases.logLevel = LogLevel.DEBUG
            return if (uid.isNullOrBlank()) {
                Purchases.configure(apiKey = PlatformConfig.revenueCatApiKey)
            } else {
                configuredUserId = uid
                Purchases.configure(apiKey = PlatformConfig.revenueCatApiKey) {
                    appUserId = uid
                }
            }
        }

        val purchases = Purchases.sharedInstance
        if (uid.isNullOrBlank()) {
            return purchases
        }
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
            plans = unpricedSubscriptionPlans(),
            pricesLoaded = false,
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
        } ?: plans.firstOrNull { it.kind == SubscriptionPlanKind.Monthly && it.isAvailable }?.let { "${it.priceLabel} / month" } ?: "FX/ Pro"
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
            pricesLoaded = plans.any { it.isAvailable },
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

    private fun Offerings.proOffering(): Offering? =
        getOffering(PRO_OFFERING_ID)
            ?: getOffering(DEFAULT_OFFERING_ID)
            ?: current

    private fun Offering.toPackagesByKind(): Map<SubscriptionPlanKind, Package> =
        buildMap {
            monthly?.let { put(SubscriptionPlanKind.Monthly, it) }
            annual?.let { put(SubscriptionPlanKind.Yearly, it) }
        }

    private fun Map<SubscriptionPlanKind, Package>.toSubscriptionPlans(): List<SubscriptionPlan> {
        val monthlyMicros = get(SubscriptionPlanKind.Monthly)?.storeProduct?.price?.amountMicros
        return unpricedSubscriptionPlans().map { fallbackPlan ->
            val packageForPlan = get(fallbackPlan.kind) ?: return@map fallbackPlan
            val price = packageForPlan.storeProduct.price
            val yearly = fallbackPlan.kind == SubscriptionPlanKind.Yearly
            fallbackPlan.copy(
                priceLabel = price.formatted.toReadablePrice(),
                isAvailable = true,
                monthlyEquivalentLabel = if (yearly) monthlyEquivalentLabel(price.amountMicros, price.currencyCode) else null,
                savingsPercent = if (yearly) yearlySavingsPercent(price.amountMicros, monthlyMicros) else null,
            )
        }
    }

    private companion object {
        const val PRO_OFFERING_ID = "pro"
        const val DEFAULT_OFFERING_ID = "default"
        val PRO_ENTITLEMENTS = setOf("pro")
    }
}

/** Plans without a price: what the paywall gets until Google Play offerings load (issue #13). */
fun unpricedSubscriptionPlans(): List<SubscriptionPlan> =
    defaultSubscriptionPlans().map { it.copy(priceLabel = "", isAvailable = false) }

/** "US$1.67" for a yearly price of 19.99 USD; currency symbol is the ISO code, prefixed like the store label. */
fun monthlyEquivalentLabel(yearlyAmountMicros: Long, currencyCode: String): String {
    val cents = (yearlyAmountMicros / 12.0 / 10_000.0).let { kotlin.math.round(it) }.toLong()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$currencyCode $whole.$fraction"
}

fun yearlySavingsPercent(yearlyAmountMicros: Long, monthlyAmountMicros: Long?): Int? {
    if (monthlyAmountMicros == null || monthlyAmountMicros <= 0L) return null
    val yearOfMonthly = monthlyAmountMicros * 12.0
    val saved = (1.0 - yearlyAmountMicros / yearOfMonthly) * 100.0
    return saved.toInt().takeIf { it > 0 }
}

/** Placeholder prices for previews and tests only; production states come from the store (see [unpricedSubscriptionPlans]). */
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
