package com.fxalways.app.screens.converter

import com.fxalways.designsystem.components.FxRate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmartTimingTest {
    private fun rate(code: String, value: Double, series: List<Double>) =
        FxRate(code = code, name = code, glyph = code.take(1), rate = value, change24h = 0.0, sparkline = series.map { it.toFloat() })

    private val usd = rate("USD", 1.0, List(8) { 1.0 })

    @Test
    fun pairAtTopOfRisingRangeIsStrong() {
        val mxn = rate("MXN", 1.10, listOf(1.00, 1.02, 1.04, 1.05, 1.07, 1.08, 1.09, 1.10))
        val insight = smartTimingInsight(usd, mxn)
        assertEquals("Strong rate", insight.signal)
        assertEquals("Send now", insight.remittanceAdvice)
        assertTrue(insight.score >= 82, "score=${insight.score}")
    }

    @Test
    fun pairAtBottomOfFallingRangeIsWait() {
        val mxn = rate("MXN", 1.00, listOf(1.10, 1.09, 1.08, 1.07, 1.05, 1.04, 1.02, 1.00))
        val insight = smartTimingInsight(usd, mxn)
        assertEquals("Wait", insight.signal)
        assertEquals("Delay if flexible", insight.remittanceAdvice)
        assertEquals("Use alerts", insight.savingsAdvice)
        assertTrue(insight.score < 58, "score=${insight.score}")
    }

    @Test
    fun midRangeLowVolatilityIsGoodTime() {
        val mxn = rate("MXN", 1.00, listOf(1.00, 1.02, 0.98, 1.00))
        val insight = smartTimingInsight(usd, mxn)
        assertEquals("Good time", insight.signal)
        assertEquals("Send staged", insight.remittanceAdvice)
        assertTrue(insight.score in 58..81, "score=${insight.score}")
    }

    @Test
    fun horizonsCover7d30dAnd90dOfThePairSeries() {
        val mxn = rate("MXN", 1.10, listOf(1.00, 1.02, 1.04, 1.05, 1.07, 1.08, 1.09, 1.10))
        val insight = smartTimingInsight(usd, mxn)
        assertEquals(listOf("7D", "30D", "90D"), insight.horizons.map { it.label })
        val week = insight.horizons.first()
        assertEquals(1.0, week.position, 1e-9)
        assertTrue(week.trendPct > 0.0)
    }

    @Test
    fun pairSeriesDividesTargetBySource() {
        // EUR/USD rising while USD is flat: the pair position follows the target series.
        val eur = rate("EUR", 0.95, listOf(0.90, 0.92, 0.95))
        val insight = smartTimingInsight(usd, eur)
        assertEquals(1.0, insight.horizons.first().position, 1e-9)
        // Same target series but the source strengthens even faster: the pair falls.
        val strongSource = rate("USD", 2.0, listOf(1.0, 1.5, 2.0))
        val falling = smartTimingInsight(strongSource, eur)
        assertEquals(0.0, falling.horizons.first().position, 1e-9)
    }

    @Test
    fun emptySparklinesFallBackToSpotRateWithoutCrashing() {
        val insight = smartTimingInsight(rate("USD", 1.0, emptyList()), rate("MXN", 17.0, emptyList()))
        assertEquals(3, insight.horizons.size)
        assertEquals(0.5, insight.horizons.first().position, 1e-9)
    }
}
