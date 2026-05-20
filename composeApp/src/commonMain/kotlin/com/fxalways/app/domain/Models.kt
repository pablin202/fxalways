package com.fxalways.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyRate(
    val code: String,
    val value: Double,
)

@Serializable
data class LatestRates(
    val base: String,
    val date: String,
    val rates: List<CurrencyRate>,
    val provider: String,
    val refreshedAt: String,
)

@Serializable
data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String = "",
    val flag: String = "◆",
    val country: String = "",
    val region: String = "",
    val isPopular: Boolean = false,
)

@Serializable
data class SupportedCurrenciesResponse(
    val provider: String,
    val refreshedAt: String,
    val currencies: List<CurrencyInfo>,
)

@Serializable
data class HistoricalPoint(
    val date: String,
    val value: Double,
)

@Serializable
data class HistoricalSeries(
    val base: String,
    val quote: String,
    val points: List<HistoricalPoint>,
    val provider: String,
)

@Serializable
data class CryptoMarketAssetDto(
    val code: String,
    val name: String,
    val glyph: String = "◆",
    val stable: Boolean = false,
    val rank: Int? = null,
    val priceUsd: Double = 0.0,
    val priceBase: Double = 0.0,
    val value: Double = 0.0,
    val change24h: Double = 0.0,
    val marketCapUsd: Double? = null,
    val volume24hUsd: Double? = null,
    val sparkline: List<Double> = emptyList(),
)

@Serializable
data class CryptoMarketsResponse(
    val base: String,
    val provider: String,
    val refreshedAt: String,
    val assets: List<CryptoMarketAssetDto>,
)

@Serializable
data class NewsMove(
    val code: String,
    val change: Double,
)

@Serializable
data class NewsItemDto(
    val id: String,
    val tag: String,
    val impact: String,
    val title: String,
    val summary: String,
    val source: String,
    val sourceUrl: String,
    val publishedAt: String,
    val ageLabel: String,
    val language: String,
    val countries: List<String>,
    val currencies: List<String>,
    val topics: List<String>,
    val sentiment: String,
    val moves: List<NewsMove>,
)

@Serializable
data class NewsSentimentDto(
    val bullish: Int,
    val neutral: Int,
    val bearish: Int,
)

@Serializable
data class NewsFeedDto(
    val feedKey: String,
    val language: String,
    val region: String,
    val currencies: List<String>,
    val provider: String,
    val refreshedAt: String,
    val sentiment: NewsSentimentDto,
    val items: List<NewsItemDto>,
)

@Serializable
data class ProviderCatalogItemDto(
    val id: String,
    val label: String,
    val category: String,
    val quoteMode: String,
    val markets: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
    val quoteCapable: Boolean = false,
    val priority: Int = 0,
    val subtitle: String = "",
)

@Serializable
data class ProviderCatalogDto(
    val provider: String,
    val refreshedAt: String,
    val region: String,
    val baseCurrency: String,
    val primary: List<ProviderCatalogItemDto> = emptyList(),
    val other: List<ProviderCatalogItemDto> = emptyList(),
)

data class WatchPair(
    val base: String,
    val quote: String,
)

data class RateCard(
    val pair: WatchPair,
    val rate: Double,
    val dailyChangePct: Double?,
    val sparkline: List<HistoricalPoint>,
)

data class DashboardState(
    val base: String = "USD",
    val quote: String = "EUR",
    val amount: String = "100",
    val latest: LatestRates? = null,
    val historical: HistoricalSeries? = null,
    val watchCards: List<RateCard> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isPremium: Boolean = false,
) {
    val convertedAmount: Double?
        get() {
            val amountValue = amount.toDoubleOrNull() ?: return null
            val rate = latest?.rates?.firstOrNull { it.code == quote }?.value ?: return null
            return amountValue * rate
        }
}

val DefaultPairs = listOf(
    WatchPair("USD", "EUR"),
    WatchPair("EUR", "USD"),
    WatchPair("USD", "JPY"),
    WatchPair("GBP", "USD"),
    WatchPair("AUD", "USD"),
    WatchPair("USD", "BRL"),
)

val MajorCurrencies = listOf(
    "USD",
    "EUR",
    "GBP",
    "JPY",
    "AUD",
    "CAD",
    "CHF",
    "CNY",
    "BRL",
    "MXN",
    "NZD",
    "SGD",
)
