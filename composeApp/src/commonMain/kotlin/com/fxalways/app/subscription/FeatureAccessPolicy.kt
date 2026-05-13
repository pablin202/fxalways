package com.fxalways.app.subscription

data class FeatureAccessPolicy(
    val favoriteLimit: Int,
    val compareLimit: Int,
    val newsStoryLimit: Int,
    val feeQuoteLimit: Int,
    val travelerCheatSheetLimit: Int,
    val baseCurrencyLimit: Int,
    val alertLimit: Int,
    val watchlistCurrencyLimit: Int,
    val historyLabel: String,
    val canUseAdvancedTraveler: Boolean,
    val canUseAdvancedNews: Boolean,
    val canUseFullFeeComparison: Boolean,
) {
    val hasUnlimitedAlerts: Boolean get() = alertLimit == Int.MAX_VALUE
    val hasUnlimitedWatchlistCurrencies: Boolean get() = watchlistCurrencyLimit == Int.MAX_VALUE
}

fun SubscriptionState.featureAccess(): FeatureAccessPolicy =
    if (isPremium) {
        FeatureAccessPolicy(
            favoriteLimit = Int.MAX_VALUE,
            compareLimit = Int.MAX_VALUE,
            newsStoryLimit = Int.MAX_VALUE,
            feeQuoteLimit = Int.MAX_VALUE,
            travelerCheatSheetLimit = Int.MAX_VALUE,
            baseCurrencyLimit = Int.MAX_VALUE,
            alertLimit = Int.MAX_VALUE,
            watchlistCurrencyLimit = Int.MAX_VALUE,
            historyLabel = "5 years",
            canUseAdvancedTraveler = true,
            canUseAdvancedNews = true,
            canUseFullFeeComparison = true,
        )
    } else {
        FeatureAccessPolicy(
            favoriteLimit = 3,
            compareLimit = 4,
            newsStoryLimit = 3,
            feeQuoteLimit = 2,
            travelerCheatSheetLimit = 4,
            baseCurrencyLimit = 8,
            alertLimit = 1,
            watchlistCurrencyLimit = 4,
            historyLabel = "30 days",
            canUseAdvancedTraveler = false,
            canUseAdvancedNews = false,
            canUseFullFeeComparison = false,
        )
    }

fun Int.cap(total: Int): Int =
    if (this == Int.MAX_VALUE) total else minOf(total, this)
