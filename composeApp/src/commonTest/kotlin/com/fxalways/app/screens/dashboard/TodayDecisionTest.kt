package com.fxalways.app.screens.dashboard

import com.fxalways.app.Corridor
import com.fxalways.app.SendCadence
import com.fxalways.app.UserProfile
import com.fxalways.app.data.LiveRatesState
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TodayDecisionTest {
    private fun rate(code: String, value: Double, series: List<Double> = listOf(value, value)) =
        FxRate(code, code, "◆", CurrencyKind.Fiat, value, 0.0, series.map { it.toFloat() })

    private val state = LiveRatesState(
        isLoading = false,
        isLive = true,
        baseCurrency = "AUD",
        favorites = listOf(rate("ARS", 1080.0, listOf(1000.0, 1040.0, 1080.0))),
        converter = listOf(rate("AUD", 1.0), rate("ARS", 1080.0, listOf(1000.0, 1040.0, 1080.0))),
        compare = emptyList(),
        crypto = emptyList(),
        allFiat = listOf(rate("AUD", 1.0)),
    )

    @Test
    fun wholeAmountsReadLikeMoney() {
        assertEquals("500", formatWholeAmount(500.0))
        assertEquals("1,000", formatWholeAmount(1_000.0))
        assertEquals("12,345", formatWholeAmount(12_345.0))
        assertEquals("2,500.50", formatWholeAmount(2_500.5))
    }

    @Test
    fun defaultCorridorComesFromTheProfilePreset() {
        assertEquals(Corridor("USD", "MXN", 500.0, SendCadence.Monthly), UserProfile.Remittances.defaultCorridor("USD"))
        // Base equals the preset target: fall back to the first converter currency.
        assertEquals("EUR", UserProfile.Remittances.defaultCorridor("MXN").target)
        assertEquals(SendCadence.Once, UserProfile.Freelancer.defaultCorridor("USD").cadence)
    }

    @Test
    fun decisionConvertsCorridorAtTodaysRateAndRanksRealRoutes() {
        val decision = todayDecision(state, Corridor("AUD", "ARS", 500.0, SendCadence.Monthly), isPremium = true, providerPreferenceCodes = listOf("wise", "western_union"))
        assertNotNull(decision)
        assertEquals(540_000.0, decision.convertedAmount, 1e-6)
        assertEquals("Wise", decision.bestRoute?.provider)
        assertEquals("Western Union", decision.worstRoute?.provider)
        assertTrue(decision.savingsVsWorst > 0.0)
        assertEquals("Strong rate", decision.timing.signal)
    }

    @Test
    fun decisionIsNullWithoutARateForTheTarget() {
        assertNull(todayDecision(state, Corridor("AUD", "COP", 500.0, SendCadence.Once), isPremium = false, providerPreferenceCodes = emptyList()))
        assertNull(todayDecision(state, Corridor("AUD", "ARS", 0.0, SendCadence.Once), isPremium = false, providerPreferenceCodes = emptyList()))
    }
}
