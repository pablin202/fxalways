package com.fxalways.app.subscription

/**
 * Single source of truth for what Free and Pro include. README ("Free vs Pro"), the paywall
 * comparison and the store listing must match this table:
 *
 * | Capability                     | Free                         | Pro                                      |
 * |--------------------------------|------------------------------|------------------------------------------|
 * | Converter, favorites, compare  | unlimited currencies         | same                                     |
 * | Real transfer cost (providers) | your corridor, 3 providers   | all providers + provider history         |
 * | Rate alerts                    | 2 active                     | unlimited + best-moment / cheapest alerts|
 * | History                        | 1 year                       | 5 years (all-time)                       |
 * | Traveler, OCR, portfolio, news | basics                       | full                                     |
 *
 * Free is generous on access; Pro sells depth and automation.
 */
data class FeatureAccessPolicy(
    val favoriteLimit: Int,
    val compareLimit: Int,
    val newsStoryLimit: Int,
    val feeQuoteLimit: Int,
    val travelerCheatSheetLimit: Int,
    val converterCurrencyLimit: Int,
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
    val hasUnlimitedCompareCurrencies: Boolean get() = compareLimit == Int.MAX_VALUE
    val hasUnlimitedConverterCurrencies: Boolean get() = converterCurrencyLimit == Int.MAX_VALUE
    val hasUnlimitedBaseCurrencies: Boolean get() = baseCurrencyLimit == Int.MAX_VALUE
}

fun SubscriptionState.featureAccess(): FeatureAccessPolicy =
    if (isPremium) {
        FeatureAccessPolicy(
            favoriteLimit = Int.MAX_VALUE,
            compareLimit = Int.MAX_VALUE,
            newsStoryLimit = Int.MAX_VALUE,
            feeQuoteLimit = Int.MAX_VALUE,
            travelerCheatSheetLimit = Int.MAX_VALUE,
            converterCurrencyLimit = Int.MAX_VALUE,
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
            favoriteLimit = Int.MAX_VALUE,
            compareLimit = Int.MAX_VALUE,
            newsStoryLimit = 3,
            feeQuoteLimit = FREE_QUOTE_PROVIDER_LIMIT,
            travelerCheatSheetLimit = 4,
            converterCurrencyLimit = Int.MAX_VALUE,
            baseCurrencyLimit = Int.MAX_VALUE,
            alertLimit = FREE_ALERT_LIMIT,
            watchlistCurrencyLimit = Int.MAX_VALUE,
            historyLabel = "1 year",
            canUseAdvancedTraveler = false,
            canUseAdvancedNews = false,
            canUseFullFeeComparison = false,
        )
    }

const val FREE_ALERT_LIMIT = 2
const val FREE_QUOTE_PROVIDER_LIMIT = 3

fun Int.cap(total: Int): Int =
    if (this == Int.MAX_VALUE) total else minOf(total, this)
