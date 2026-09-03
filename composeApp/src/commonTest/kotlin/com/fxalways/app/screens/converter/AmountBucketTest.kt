package com.fxalways.app.screens.converter

import kotlin.test.Test
import kotlin.test.assertEquals

class AmountBucketTest {
    @Test
    fun bucketsAreStableAndExclusive() {
        assertEquals("lt_100", amountBucket(0.0))
        assertEquals("lt_100", amountBucket(99.99))
        assertEquals("100_1k", amountBucket(100.0))
        assertEquals("100_1k", amountBucket(999.0))
        assertEquals("1k_10k", amountBucket(1_000.0))
        assertEquals("1k_10k", amountBucket(9_999.0))
        assertEquals("gte_10k", amountBucket(10_000.0))
        assertEquals("gte_10k", amountBucket(250_000.0))
    }
}
