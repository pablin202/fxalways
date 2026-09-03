package com.fxalways.app.screens.converter

import com.fxalways.app.subscription.FREE_QUOTE_PROVIDER_LIMIT
import com.fxalways.designsystem.components.FxRate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeeQuotesTest {
    private fun rate(code: String, value: Double) =
        FxRate(code = code, name = code, glyph = code.take(1), rate = value, change24h = 0.0, sparkline = emptyList())

    private val usd = rate("USD", 1.0)
    private val mxn = rate("MXN", 17.0)
    private val noCustomFee = CustomFeeInput(fixedFee = 0.0, feePercent = 0.0, markupPercent = 0.0)

    private fun quotes(amount: Double = 1_000.0) =
        estimatedFeeQuotes(usd, mxn, amount, noCustomFee, selectedProviderCodes = listOf("wise", "western_union", "airport_exchange"))

    @Test
    fun midMarketIsTheZeroLossBaselineAndQuotesSortByLoss() {
        val result = quotes()
        val mid = result.first { it.providerId == "mid_market" }
        assertEquals(0.0, mid.lossTargetValue, 1e-9)
        assertEquals("best", mid.badge)
        assertEquals(0.0, result.first().lossTargetValue, 1e-9)
        assertEquals(result.map { it.lossTargetValue }, result.map { it.lossTargetValue }.sorted())
        // A zero-fee custom provider ties with mid-market and is listed first so the user's own route stays visible.
        assertEquals("Custom", result.first().provider)
    }

    @Test
    fun wiseLossEqualsItsFeesConvertedAtMidMarket() {
        val wise = quotes().first { it.providerId == "wise" }
        // 0.35 fixed + 0.45% of 1000 = 4.85 USD; the rest converts at 17.0 with no markup.
        val expectedLoss = 4.85 * 17.0
        assertEquals(expectedLoss, wise.lossTargetValue, 1e-6)
        assertEquals(expectedLoss / 17_000.0 * 100.0, wise.lossPercentValue, 1e-6)
        assertNull(wise.badge)
        assertFalse(wise.isHighFee)
        assertEquals("USD 4.8500", wise.fee)
    }

    @Test
    fun airportExchangeIsFlaggedAvoidAndHighFee() {
        val airport = quotes().first { it.providerId == "airport_exchange" }
        assertEquals("avoid", airport.badge)
        assertTrue(airport.isHighFee)
        assertEquals(8.5, airport.lossPercentValue, 1e-6)
        assertEquals(quotes().last().providerId, "airport_exchange")
    }

    @Test
    fun onlySelectedProvidersPlusBaselineAndCustomAreVisible() {
        val ids = quotes().map { it.providerId }.toSet()
        assertEquals(setOf("mid_market", "custom", "wise", "western_union", "airport_exchange"), ids)
    }

    @Test
    fun zeroOrNegativeAmountProducesZeroLossQuotes() {
        val result = estimatedFeeQuotes(usd, mxn, -5.0, noCustomFee, listOf("wise"))
        assertTrue(result.all { it.lossTargetValue == 0.0 })
        assertTrue(result.all { it.lossPercentValue == 0.0 })
    }

    @Test
    fun customFeeInputDrivesTheCustomQuote() {
        val custom = estimatedFeeQuotes(usd, mxn, 1_000.0, CustomFeeInput(fixedFee = 10.0, feePercent = 1.0, markupPercent = 2.0), listOf("wise"))
            .first { it.providerId == "custom" }
        // (1000 - 10 - 10) * 17 * 0.98 = 16326.8 received → 673.2 lost
        assertEquals(673.2, custom.lossTargetValue, 1e-6)
        assertTrue(custom.isHighFee)
        assertEquals("high fee", custom.badge)
    }

    @Test
    fun backendQuotesLeaveLocalQuotesUntouchedWhenEmpty() {
        val local = quotes()
        assertEquals(local, local.withBackendProviderQuotes(emptyList(), mxn))
    }

    @Test
    fun freePlanSeesThreeQuoteProviders() {
        assertEquals(3, FREE_QUOTE_PROVIDER_LIMIT)
    }
}
