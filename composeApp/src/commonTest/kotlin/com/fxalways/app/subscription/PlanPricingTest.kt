package com.fxalways.app.subscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlanPricingTest {
    @Test
    fun monthlyEquivalentDividesTheYearlyPriceByTwelve() {
        assertEquals("USD 1.67", monthlyEquivalentLabel(19_990_000L, "USD"))
        assertEquals("EUR 2.50", monthlyEquivalentLabel(29_990_000L, "EUR"))
        assertEquals("ARS 833.33", monthlyEquivalentLabel(9_999_990_000L, "ARS"))
    }

    @Test
    fun savingsComparesYearlyAgainstTwelveMonths() {
        assertEquals(44, yearlySavingsPercent(19_990_000L, 2_990_000L))
        assertNull(yearlySavingsPercent(19_990_000L, null))
        assertNull(yearlySavingsPercent(40_000_000L, 2_990_000L))
    }

    @Test
    fun unpricedPlansCarryNoPriceAndAreNotPurchasable() {
        val plans = unpricedSubscriptionPlans()
        assertEquals(2, plans.size)
        assertTrue(plans.all { it.priceLabel.isEmpty() && !it.isAvailable })
    }
}
