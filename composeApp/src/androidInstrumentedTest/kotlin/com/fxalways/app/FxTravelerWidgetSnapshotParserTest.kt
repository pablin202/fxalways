package com.fxalways.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FxTravelerWidgetSnapshotParserTest {
    @Test
    fun parserBuildsTravelerBudgetFromSelectedDestinationAndCacheRate() {
        val snapshot = FxTravelerWidgetSnapshotParser.fromCacheJson(
            raw = """
            {
              "baseCurrency": "USD",
              "updatedLabel": "2026-05-15 · live",
              "favorites": [
                {"code":"JPY","rate":156.0,"change24h":1.8}
              ],
              "converter": [],
              "allFiat": []
            }
            """.trimIndent(),
            selectedCurrency = "JPY",
            budgetBase = 100.0,
        )

        assertNotNull(snapshot)
        assertEquals("DAILY", snapshot.status)
        assertEquals("TOKYO · JPY", snapshot.destinationLabel)
        assertEquals("¥15,600", snapshot.localBudget)
        assertEquals("¥5,200 / day", snapshot.dailyBudget)
        assertEquals("¥3,900 cash", snapshot.cashBuffer)
        assertEquals("Rate · live", snapshot.footerLabel)
        assertEquals("1 USD = 156.00 JPY", snapshot.footerValue)
    }

    @Test
    fun parserUsesFallbackDestinationForUnsupportedCurrency() {
        val snapshot = FxTravelerWidgetSnapshotParser.fromCacheJson(
            raw = """
            {
              "baseCurrency": "USD",
              "updatedLabel": "2026-05-15 · cached 4m",
              "favorites": [],
              "converter": [
                {"code":"SGD","rate":1.34,"change24h":0.05}
              ],
              "allFiat": []
            }
            """.trimIndent(),
            selectedCurrency = "SGD",
            budgetBase = 200.0,
        )

        assertNotNull(snapshot)
        assertEquals("CACHE", snapshot.status)
        assertEquals("SGD · SGD", snapshot.destinationLabel)
        assertEquals("SGD 268", snapshot.localBudget)
        assertEquals("SGD 89 / day", snapshot.dailyBudget)
        assertEquals("SGD 54 cash", snapshot.cashBuffer)
    }

    @Test
    fun parserReturnsNullForBlankOrMalformedPayload() {
        assertNull(FxTravelerWidgetSnapshotParser.fromCacheJson(null, "JPY", 100.0))
        assertNull(FxTravelerWidgetSnapshotParser.fromCacheJson("", "JPY", 100.0))
        assertNull(FxTravelerWidgetSnapshotParser.fromCacheJson("{", "JPY", 100.0))
    }
}
