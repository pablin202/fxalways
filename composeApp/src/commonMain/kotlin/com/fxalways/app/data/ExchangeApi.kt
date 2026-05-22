package com.fxalways.app.data

import com.fxalways.app.PlatformConfig
import com.fxalways.app.domain.CryptoMarketsResponse
import com.fxalways.app.domain.HistoricalSeries
import com.fxalways.app.domain.LatestRates
import com.fxalways.app.domain.NewsFeedDto
import com.fxalways.app.domain.ProviderCatalogDto
import com.fxalways.app.domain.ProviderQuotesDto
import com.fxalways.app.domain.SupportedCurrenciesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExchangeApi(
    private val baseUrl: String = PlatformConfig.backendBaseUrl.trimEnd('/'),
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    },
) {
    suspend fun latest(base: String): LatestRates =
        client.get("$baseUrl/latestRates") {
            parameter("base", base)
        }.body()

    suspend fun supportedCurrencies(): SupportedCurrenciesResponse =
        client.get("$baseUrl/supportedCurrencies").body()

    suspend fun providerCatalog(base: String): ProviderCatalogDto =
        client.get("$baseUrl/providerCatalog") {
            parameter("base", base)
        }.body()

    suspend fun providerQuotes(
        base: String,
        target: String,
        amount: Double,
        providers: List<String>,
        isPremium: Boolean,
    ): ProviderQuotesDto =
        client.get("$baseUrl/providerQuotes") {
            parameter("base", base)
            parameter("target", target)
            parameter("amount", amount)
            parameter("providers", providers.joinToString(","))
            parameter("plan", if (isPremium) "pro" else "free")
        }.body()

    suspend fun history(base: String, quote: String, days: Int = 365): HistoricalSeries =
        client.get("$baseUrl/historicalRates") {
            parameter("base", base)
            parameter("quote", quote)
            parameter("days", days)
        }.body()

    suspend fun cryptoMarkets(base: String, limit: Int = 200): CryptoMarketsResponse =
        client.get("$baseUrl/cryptoMarkets") {
            parameter("base", base)
            parameter("limit", limit)
        }.body()

    suspend fun newsFeed(
        language: String = "en",
        region: String = "US",
        currencies: List<String> = listOf("USD", "EUR", "JPY", "GBP", "BTC"),
    ): NewsFeedDto =
        client.get("$baseUrl/newsFeed") {
            parameter("language", language)
            parameter("region", region)
            parameter("currencies", currencies.joinToString(","))
        }.body()
}
