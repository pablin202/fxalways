package com.fxalways.app.screens.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoreListingCopyTest {
    private val listings = listOf(StoreListingEN, StoreListingES, StoreListingPT)

    @Test
    fun titlesCarryAnIntentKeywordWithinPlayLimits() {
        listings.forEach { listing ->
            assertTrue(listing.title.length in 28..30, "${listing.locale} title ${listing.title.length} chars")
            assertTrue(listing.title.startsWith("FX Always: "), listing.locale)
            assertTrue(listing.shortDescription.length <= 80, "${listing.locale} short ${listing.shortDescription.length}")
            assertTrue(listing.longDescription.length <= 4000, "${listing.locale} long ${listing.longDescription.length}")
        }
    }

    @Test
    fun firstParagraphMentionsFeesAlertsAndTiming() {
        listings.forEach { listing ->
            val first = listing.longDescription.substringBefore("\n\n").lowercase()
            assertTrue("wise" in first && "western union" in first, listing.locale)
            assertTrue("alert" in first, listing.locale)
        }
        assertTrue(StoreListingEN.longDescription.endsWith("cash exchange rates."))
    }

    @Test
    fun appLanguageMapsToTheClosestListing() {
        assertEquals("es-419", storeListingFor("es").locale)
        assertEquals("pt-BR", storeListingFor("pt-BR").locale)
        assertEquals("en-US", storeListingFor("hi").locale)
        assertEquals("en-US", storeListingFor("en").locale)
    }
}
