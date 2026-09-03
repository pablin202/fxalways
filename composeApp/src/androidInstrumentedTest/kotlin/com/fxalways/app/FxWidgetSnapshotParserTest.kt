package com.fxalways.app

import android.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FxWidgetSnapshotParserTest {
    @Test
    fun parserBuildsPrimaryPairAndCryptoTilesFromCachePayload() {
        val snapshot = FxWidgetSnapshotParser.fromCacheJson(
            """
            {
              "baseCurrency": "USD",
              "updatedLabel": "2026-05-15 · live",
              "favorites": [
                {"code":"EUR","rate":0.92,"change24h":-0.2}
              ],
              "converter": [],
              "crypto": [
                {"code":"BTC","rate":0.000015,"change24h":2.4},
                {"code":"ETH","rate":0.00024,"change24h":-1.2}
              ]
            }
            """.trimIndent(),
        )

        assertNotNull(snapshot)
        assertEquals("DAILY", snapshot.status)
        assertEquals("USD / EUR", snapshot.primaryPair)
        assertEquals("0.920000", snapshot.primaryValue)
        assertTrue(snapshot.tileOneLabel.startsWith("BTC"))
        assertEquals("+2.40%", snapshot.tileOneValue)
        assertTrue(snapshot.tileTwoLabel.startsWith("ETH"))
        assertEquals("-1.20%", snapshot.tileTwoValue)
        assertEquals(Color.rgb(248, 113, 113), snapshot.tileTwoColor)
        assertEquals("Best mover · live", snapshot.footerLabel)
        assertEquals("BTC +2.40%", snapshot.footerValue)
    }

    @Test
    fun parserUsesCacheStatusAndFallbackWhenCryptoMissing() {
        val snapshot = FxWidgetSnapshotParser.fromCacheJson(
            """
            {
              "baseCurrency": "AUD",
              "updatedLabel": "2026-05-15 · cached 4m",
              "favorites": [],
              "converter": [
                {"code":"AUD","rate":1.0,"change24h":0.0},
                {"code":"JPY","rate":101.25,"change24h":0.4}
              ],
              "crypto": []
            }
            """.trimIndent(),
        )

        assertNotNull(snapshot)
        assertEquals("CACHE 4m", snapshot.status)
        assertEquals("AUD / JPY", snapshot.primaryPair)
        assertEquals("101.25", snapshot.primaryValue)
        assertEquals("BTC", snapshot.tileOneLabel)
        assertEquals("Waiting", snapshot.tileOneValue)
        assertEquals("Best mover · cached 4m", snapshot.footerLabel)
        assertEquals("JPY +0.40%", snapshot.footerValue)
    }

    @Test
    fun parserReturnsNullForBlankOrMalformedPayload() {
        assertNull(FxWidgetSnapshotParser.fromCacheJson(null))
        assertNull(FxWidgetSnapshotParser.fromCacheJson(""))
        assertNull(FxWidgetSnapshotParser.fromCacheJson("{"))
    }
}
