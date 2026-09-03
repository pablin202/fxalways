package com.fxalways.app.screens

import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormattingTest {
    private fun rate(code: String, value: Double, kind: CurrencyKind = CurrencyKind.Fiat) =
        FxRate(code = code, name = code, glyph = code.take(1), kind = kind, rate = value, change24h = 0.0, sparkline = emptyList())

    private val usd = rate("USD", 1.0)
    private val mxn = rate("MXN", 17.0)
    private val btc = rate("BTC", 0.00002, CurrencyKind.Crypto)

    @Test
    fun amountInputAcceptsDecimalCommaAndPoint() {
        // es/pt keyboards type "12,5"; en/hi keyboards type "12.5"; both parse to the same value.
        assertEquals(12.5, parseAmountInput("12,5"))
        assertEquals(12.5, parseAmountInput("12.5"))
        assertEquals(1234.5, parseAmountInput("1,234.5"))
        assertEquals(0.0, parseAmountInput(""))
        assertEquals(0.0, parseAmountInput("abc"))
    }

    @Test
    fun sanitizeKeepsDigitsAndASingleDecimalSeparator() {
        assertEquals("12.3", sanitizeAmountInput("abc1.2.3"))
        assertEquals("12,5", sanitizeAmountInput("12,5"))
        assertEquals("1234.5", sanitizeAmountInput("1,234.5"))
        assertEquals("", sanitizeAmountInput("$€"))
        assertEquals(14, sanitizeAmountInput("123456789012345678").length)
    }

    @Test
    fun moneyValuesUseFixedDecimalsAndGroupingRegardlessOfLocale() {
        assertEquals("0.00", formatMoneyValue(0.0))
        assertEquals("<0.01", formatMoneyValue(0.004))
        assertEquals("1,234.50", formatMoneyValue(1234.5))
        assertEquals("12.5000", formatMoneyValue(12.5))
        assertEquals("MXN 1,700.00", formatConvertedAmount(mxn, 1700.0))
    }

    @Test
    fun cryptoAmountsKeepPrecisionForSmallValues() {
        assertEquals("0", formatCryptoAmount(0.0))
        assertEquals("<0.000001", formatCryptoAmount(0.0000001))
        assertEquals("0.5000", formatCryptoAmount(0.5))
        assertEquals("BTC 2.0000", formatConvertedAmount(btc, 2.0))
    }

    @Test
    fun signedPercentAndAmountCarryTheirSign() {
        assertEquals("+1.2%", formatSignedPercent(1.26))
        assertEquals("-3.4%", formatSignedPercent(-3.44))
        assertEquals("+USD 12.5000", formatSignedAmount("USD", 12.5))
        assertEquals("-USD 0.2500", formatSignedAmount("USD", -0.25))
    }

    @Test
    fun conversionAndHiddenCostMath() {
        assertEquals(1700.0, convertedAmount(100.0, usd, mxn), 1e-9)
        assertEquals(0.0, convertedAmount(100.0, rate("ZZZ", 0.0), mxn))
        assertEquals(100.0, liveSourceCostFor(1700.0, mxn), 1e-9)
        // Local market pays 20 MXN per USD: the shelf price costs 85 USD locally vs 100 at mid-market.
        assertEquals(-15.0, hiddenCostFor(1700.0, mxn, localMarketRate = 20.0), 1e-9)
        assertEquals(0.0, hiddenCostFor(1700.0, mxn, localMarketRate = 0.0), 1e-9)
    }

    @Test
    fun inputAmountFormattingDropsGroupingForLargeValues() {
        assertEquals("", formatInputAmount(0.0))
        assertEquals("1234.50", formatInputAmount(1234.5))
        assertEquals("12.0000", formatInputAmount(12.0))
    }
}
