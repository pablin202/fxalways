package com.fxalways.app.subscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the Free/Pro table. If a number here changes, README, the paywall comparison
 * and the store listing have to change with it.
 */
class FeatureAccessPolicyTest {
    private val free = SubscriptionState(isPremium = false).featureAccess()
    private val pro = SubscriptionState(isPremium = true).featureAccess()

    @Test
    fun freeHasNoCurrencyCaps() {
        assertTrue(free.hasUnlimitedConverterCurrencies)
        assertTrue(free.hasUnlimitedCompareCurrencies)
        assertTrue(free.hasUnlimitedWatchlistCurrencies)
        assertTrue(free.hasUnlimitedBaseCurrencies)
        assertEquals(Int.MAX_VALUE, free.favoriteLimit)
    }

    @Test
    fun freeSellsDepthNotAccess() {
        assertEquals(2, free.alertLimit)
        assertEquals(3, free.feeQuoteLimit)
        assertEquals("1 year", free.historyLabel)
        assertEquals(3, free.newsStoryLimit)
        assertFalse(free.canUseFullFeeComparison)
        assertFalse(free.canUseAdvancedTraveler)
        assertFalse(free.canUseAdvancedNews)
    }

    @Test
    fun proIsUnlimitedEverywhere() {
        assertTrue(pro.hasUnlimitedAlerts)
        assertEquals("5 years", pro.historyLabel)
        assertTrue(pro.canUseFullFeeComparison)
        assertTrue(pro.canUseAdvancedTraveler)
        assertTrue(pro.canUseAdvancedNews)
        assertEquals(Int.MAX_VALUE, pro.feeQuoteLimit)
    }

    @Test
    fun capNeverExceedsWhatIsAvailable() {
        assertEquals(5, Int.MAX_VALUE.cap(5))
        assertEquals(2, 2.cap(5))
        assertEquals(0, 2.cap(0))
    }
}
