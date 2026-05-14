package com.fxalways.app.data.mock

import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate

data class FeeQuote(
    val provider: String,
    val badge: String?,
    val amount: String,
    val fee: String,
    val isHighFee: Boolean = false,
)

data class NewsStory(
    val tag: String,
    val impact: String,
    val age: String,
    val title: String,
    val summary: String,
    val moves: List<Pair<String, Double>>,
    val source: String = "Demo",
    val sourceUrl: String = "",
    val topics: List<String> = emptyList(),
)

data class EventItem(
    val date: String,
    val tag: String,
    val headline: String,
)

val FavoriteRates = listOf(
    FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.9182, -0.34, listOf(0.923f, 0.921f, 0.919f, 0.922f, 0.918f, 0.917f, 0.9182f)),
    FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.7841, 0.12, listOf(0.776f, 0.778f, 0.781f, 0.780f, 0.783f, 0.7841f)),
    FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.42, 0.68, listOf(154.9f, 155.4f, 155.8f, 156.0f, 155.7f, 156.42f)),
    FxRate("CHF", "Swiss Franc", "🇨🇭", CurrencyKind.Fiat, 0.8292, -0.08, listOf(0.835f, 0.833f, 0.832f, 0.830f, 0.8292f)),
    FxRate("MXN", "Mexican Peso", "🇲🇽", CurrencyKind.Fiat, 18.72, 0.21, listOf(18.52f, 18.62f, 18.59f, 18.71f, 18.72f)),
)

val CryptoRates = listOf(
    FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.0000154, 2.84, listOf(0.0000141f, 0.0000146f, 0.0000144f, 0.0000152f, 0.0000154f)),
    FxRate("ETH", "Ethereum", "Ξ", CurrencyKind.Crypto, 0.000243, 1.92, listOf(0.000225f, 0.000231f, 0.000236f, 0.000229f, 0.000243f)),
    FxRate("SOL", "Solana", "◎", CurrencyKind.Crypto, 0.00628, -1.14, listOf(0.0068f, 0.0067f, 0.0064f, 0.0065f, 0.00628f)),
    FxRate("USDT", "Tether", "₮", CurrencyKind.Crypto, 1.0002, 0.01, listOf(1.0f, 1.0001f, 0.9998f, 1.0002f)),
    FxRate("USDC", "USD Coin", "$", CurrencyKind.Crypto, 0.9999, -0.01, listOf(1.0f, 0.9999f, 1.0001f, 0.9999f)),
)

val ConverterRates = listOf(
    FxRate("USD", "US Dollar", "🇺🇸", CurrencyKind.Fiat, 1.0, 0.0, listOf(1f, 1f)),
    FxRate("EUR", "Euro", "🇪🇺", CurrencyKind.Fiat, 0.9182, -0.34, FavoriteRates[0].sparkline),
    FxRate("GBP", "British Pound", "🇬🇧", CurrencyKind.Fiat, 0.7841, 0.12, FavoriteRates[1].sparkline),
    FxRate("JPY", "Japanese Yen", "🇯🇵", CurrencyKind.Fiat, 156.42, 0.68, FavoriteRates[2].sparkline),
    FxRate("BTC", "Bitcoin", "₿", CurrencyKind.Crypto, 0.0000154, 2.84, CryptoRates[0].sparkline),
)

val CompareRates = FavoriteRates + CryptoRates.take(1) + listOf(
    FxRate("CAD", "Canadian Dollar", "🇨🇦", CurrencyKind.Fiat, 1.372, -0.18, listOf(1.39f, 1.386f, 1.38f, 1.376f, 1.372f)),
    FxRate("AUD", "Australian Dollar", "🇦🇺", CurrencyKind.Fiat, 1.3838, 0.31, listOf(1.35f, 1.36f, 1.37f, 1.365f, 1.3838f)),
)

val DetailSeries = listOf(
    0.904f, 0.908f, 0.912f, 0.909f, 0.915f, 0.919f, 0.916f, 0.921f, 0.9241f, 0.9203f, 0.917f, 0.9182f,
)

val FeeQuotes = listOf(
    FeeQuote("Mid-market", "BEST", "€918.20", "—"),
    FeeQuote("Wise", null, "€914.66", "$3.85"),
    FeeQuote("Revolut", null, "€913.41", "$5.20"),
    FeeQuote("Chase Bank", "HIGH FEE", "€889.10", "$31.66", true),
)

val Events = listOf(
    EventItem("May 09", "ECB", "Rates held at 3.50% — markets unmoved"),
    EventItem("Apr 24", "CPI", "Eurozone inflation eases to 2.4% YoY"),
    EventItem("Apr 11", "FED", "Dovish minutes lift EUR by 0.6%"),
)

val NewsStories = listOf(
    NewsStory(
        tag = "ECB",
        impact = "HIGH IMPACT",
        age = "37m ago",
        title = "ECB holds key rate at 3.50%, signals patience",
        summary = "Lagarde says inflation path \"consistent\" but cites services pressure. EUR ticks down 0.34%.",
        moves = listOf("EUR" to -0.34, "CHF" to -0.08),
    ),
    NewsStory(
        tag = "BTC",
        impact = "MED IMPACT",
        age = "1h ago",
        title = "Crypto liquidity improves as dollar softens",
        summary = "BTC and ETH move higher during the London session while fiat majors stay range-bound.",
        moves = listOf("BTC" to 2.84, "ETH" to 1.92),
    ),
)
