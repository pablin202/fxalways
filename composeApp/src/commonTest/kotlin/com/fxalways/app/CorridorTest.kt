package com.fxalways.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CorridorTest {
    @Test
    fun encodeDecodeRoundTrip() {
        val corridor = Corridor("AUD", "ARS", 500.0, SendCadence.Monthly)
        assertEquals(corridor, Corridor.decode(corridor.encode()))
        assertNull(Corridor.decode(null))
        assertNull(Corridor.decode("AUD,ARS"))
        assertNull(Corridor.decode("AUD,ARS,abc,Monthly"))
        assertNull(Corridor.decode("AUD,ARS,500,Weekly"))
    }

    @Test
    fun monthlyOrWhenItPaysMeansRemittances() {
        assertEquals(UserProfile.Remittances, inferProfile(Corridor("AUD", "ARS", 500.0, SendCadence.Monthly), localCurrency = "AUD"))
        assertEquals(UserProfile.Remittances, inferProfile(Corridor("USD", "MXN", 1_000.0, SendCadence.WhenItPays), localCurrency = "USD"))
        assertEquals(UserProfile.Remittances, inferProfile(Corridor("AUD", "JPY", 2_000.0, SendCadence.Monthly), localCurrency = "AUD"))
    }

    @Test
    fun oneOffToTouristDestinationMeansTraveler() {
        assertEquals(UserProfile.Traveler, inferProfile(Corridor("AUD", "JPY", 2_000.0, SendCadence.Once), localCurrency = "AUD"))
        assertEquals(UserProfile.Traveler, inferProfile(Corridor("USD", "EUR", 500.0, SendCadence.Once), localCurrency = "USD"))
        assertEquals(UserProfile.Remittances, inferProfile(Corridor("AUD", "COP", 500.0, SendCadence.Once), localCurrency = "AUD"))
    }

    @Test
    fun paidFromAbroadIntoLocalCurrencyMeansFreelancer() {
        assertEquals(UserProfile.Freelancer, inferProfile(Corridor("USD", "ARS", 2_000.0, SendCadence.Monthly), localCurrency = "ARS"))
        assertEquals(UserProfile.Freelancer, inferProfile(Corridor("EUR", "COP", 1_000.0, SendCadence.Once), localCurrency = "COP"))
        // Living in the US and sending USD abroad is not freelancing.
        assertEquals(UserProfile.Remittances, inferProfile(Corridor("USD", "MXN", 500.0, SendCadence.Monthly), localCurrency = "USD"))
    }

    @Test
    fun suggestedTargetsNeverIncludeTheBase() {
        assertFalse(suggestedTargets("AUD").contains("AUD"))
        assertFalse(suggestedTargets("USD").contains("USD"))
        assertEquals("ARS", suggestedTargets("AUD").first())
        assertEquals("MXN", suggestedTargets("USD").first())
        assertEquals(8, suggestedTargets("XXX").size)
    }
}
