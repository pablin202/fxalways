package com.fxalways.app.screens

internal const val FreeQuoteProviderLimit = 2
internal val FreeFeeProviderIds = setOf("mid_market", "custom")

internal data class ProviderOption(
    val id: String,
    val label: String,
    val category: String,
    val quoteMode: String,
    val markets: Set<String>,
    val currencies: Set<String>,
    val quoteCapable: Boolean,
    val priority: Int,
    val subtitle: String,
)

internal val ProviderCatalog = listOf(
    ProviderOption("wise", "Wise", "Transfer provider", "Live quote ready", setOf("global", "oceania", "latam", "europe", "asia"), setOf("AUD", "USD", "EUR", "GBP", "ARS", "BRL", "MXN", "COP", "JPY"), true, 100, "Official quote API path; first candidate for live pricing."),
    ProviderOption("revolut", "Revolut", "Transfer provider", "Partner API required", setOf("global", "oceania", "europe", "asia"), setOf("AUD", "USD", "EUR", "GBP", "JPY", "SGD"), true, 92, "Strong travel and multi-currency account route."),
    ProviderOption("moneygram", "MoneyGram", "Transfer provider", "Live quote ready", setOf("global", "latam", "asia", "africa"), setOf("USD", "AUD", "ARS", "BRL", "MXN", "COP", "PEN", "CLP"), true, 88, "Quote API supports cash, bank, wallet and card receive options."),
    ProviderOption("western_union", "Western Union", "Transfer provider", "Partner API required", setOf("global", "latam", "africa", "asia"), setOf("USD", "AUD", "ARS", "BRL", "MXN", "COP", "PEN", "CLP"), true, 82, "Cash-pickup and bank fallback for broad corridors."),
    ProviderOption("remitly", "Remitly", "Transfer provider", "Partner API required", setOf("global", "latam", "asia", "africa"), setOf("USD", "AUD", "MXN", "COP", "PEN", "BRL"), true, 80, "Popular remittance option where official access requires partnership."),
    ProviderOption("paypal_xoom", "PayPal / Xoom", "Transfer provider", "Partner API required", setOf("global", "latam", "north_america"), setOf("USD", "AUD", "MXN", "ARS", "BRL", "COP", "PEN"), true, 74, "Useful where PayPal identity or Xoom corridors matter."),
    ProviderOption("remessa_online", "Remessa Online", "Transfer provider", "Partner API required", setOf("latam"), setOf("BRL", "USD", "EUR", "GBP"), true, 86, "Brazil-focused FX and international transfer provider."),
    ProviderOption("global66", "Global66", "Transfer provider", "Estimated", setOf("latam"), setOf("CLP", "ARS", "COP", "PEN", "MXN", "USD", "EUR"), true, 78, "LatAm app for wallet, card and international transfers."),
    ProviderOption("dolarapp", "DolarApp", "Digital dollar", "Estimated", setOf("latam"), setOf("ARS", "MXN", "COP", "BRL", "USD"), true, 70, "Digital-dollar account route for LatAm users."),
    ProviderOption("airtm", "Airtm", "Digital dollar", "Estimated", setOf("latam", "emerging"), setOf("ARS", "VES", "COP", "PEN", "USD"), true, 62, "Useful in high-friction emerging-market corridors."),
    ProviderOption("card_payment", "Card payment", "Local rail", "Estimated", setOf("global", "oceania", "latam", "europe", "asia"), setOf("AUD", "USD", "EUR", "ARS", "BRL", "MXN"), true, 58, "Emergency card route with markup visibility."),
    ProviderOption("atm_cash", "ATM cash", "Local rail", "Estimated", setOf("global", "oceania", "latam", "europe", "asia"), setOf("AUD", "USD", "EUR", "ARS", "BRL", "MXN"), true, 52, "Cash-access route with fee and spread estimates."),
    ProviderOption("bank_transfer", "Bank transfer", "Local rail", "Estimated", setOf("global", "oceania", "latam", "europe", "asia"), setOf("AUD", "USD", "EUR", "ARS", "BRL", "MXN"), true, 48, "Bank fallback when fintech providers are unavailable."),
    ProviderOption("airport_exchange", "Airport exchange", "Local rail", "Estimated", setOf("global", "oceania", "latam", "europe", "asia"), setOf("AUD", "USD", "EUR", "ARS", "BRL", "MXN"), true, 20, "Last-resort cash route; kept visible for avoidance decisions."),
    ProviderOption("paypal", "PayPal", "Wallet / payout", "Wallet only", setOf("global", "north_america", "latam", "oceania"), setOf("USD", "AUD", "MXN", "BRL", "ARS"), false, 45, "Wallet and payout method, not a direct FX quote source."),
    ProviderOption("venmo", "Venmo", "Wallet / payout", "Wallet only", setOf("north_america"), setOf("USD"), false, 38, "US wallet/payout method through PayPal rails."),
    ProviderOption("paypay", "PayPay", "Wallet / payout", "Wallet only", setOf("asia"), setOf("JPY"), false, 36, "Japan wallet/payment rail."),
    ProviderOption("mercado_pago", "Mercado Pago", "Wallet / payout", "Wallet only", setOf("latam"), setOf("ARS", "BRL", "MXN", "CLP", "COP", "PEN", "UYU"), false, 44, "LatAm wallet and local payment rail."),
    ProviderOption("pix", "Pix", "Local rail", "Wallet only", setOf("latam"), setOf("BRL"), false, 42, "Brazil instant payment rail."),
    ProviderOption("picpay", "PicPay", "Wallet / payout", "Wallet only", setOf("latam"), setOf("BRL"), false, 35, "Brazil wallet/Pix acceptance option."),
    ProviderOption("nequi", "Nequi", "Wallet / payout", "Wallet only", setOf("latam"), setOf("COP"), false, 34, "Colombia wallet and business payment API surface."),
    ProviderOption("yape", "Yape", "Wallet / payout", "Wallet only", setOf("latam"), setOf("PEN"), false, 32, "Peru wallet and local receive method."),
    ProviderOption("uala", "Uala", "Wallet / payout", "Wallet only", setOf("latam"), setOf("ARS", "MXN", "COP"), false, 31, "LatAm wallet/card route."),
)

internal fun marketForCurrency(currencyCode: String): String =
    when (currencyCode.uppercase()) {
        "AUD", "NZD" -> "oceania"
        "ARS", "BRL", "MXN", "CLP", "COP", "PEN", "UYU", "VES", "BOB", "PYG" -> "latam"
        "USD", "CAD" -> "north_america"
        "EUR", "GBP", "CHF", "DKK", "NOK", "SEK", "PLN" -> "europe"
        "JPY", "CNY", "SGD", "HKD", "INR", "PHP", "THB", "IDR" -> "asia"
        else -> "global"
    }

private fun providerMatchesMarket(provider: ProviderOption, market: String, codes: Set<String>): Boolean =
    "global" in provider.markets || market in provider.markets || provider.currencies.any { it in codes }

internal fun primaryProviderOptions(baseCurrency: String, targetCurrency: String? = null): List<ProviderOption> {
    val market = marketForCurrency(baseCurrency)
    val codes = setOfNotNull(baseCurrency.uppercase(), targetCurrency?.uppercase())
    return ProviderCatalog
        .filter { providerMatchesMarket(it, market, codes) }
        .sortedWith(compareByDescending<ProviderOption> { it.priority }.thenBy { it.label })
}

internal fun otherProviderOptions(baseCurrency: String, targetCurrency: String? = null): List<ProviderOption> {
    val primaryIds = primaryProviderOptions(baseCurrency, targetCurrency).map { it.id }.toSet()
    return ProviderCatalog
        .filterNot { it.id in primaryIds }
        .sortedWith(compareByDescending<ProviderOption> { it.category == "Transfer provider" }.thenByDescending { it.priority }.thenBy { it.label })
}

internal fun defaultProviderPreferenceCodes(baseCurrency: String): List<String> =
    primaryProviderOptions(baseCurrency)
        .filter { it.quoteCapable }
        .take(7)
        .map { it.id }

internal fun normalizeProviderPreferenceCodes(
    codes: List<String>,
    baseCurrency: String,
    targetCurrency: String? = null,
): List<String> {
    val validIds = ProviderCatalog.map { it.id }.toSet()
    val selected = codes.filter { it in validIds }.distinct()
    return selected.ifEmpty { defaultProviderPreferenceCodes(baseCurrency) }
}

internal fun List<String>.quoteCapableProviderCodes(): List<String> {
    val quoteCapableIds = ProviderCatalog.filter { it.quoteCapable }.map { it.id }.toSet()
    return filter { it in quoteCapableIds }
}

internal fun List<String>.cappedProviderPreferenceCodesForPlan(isPremium: Boolean): List<String> {
    if (isPremium) return distinct()
    var quoteCount = 0
    val byId = ProviderCatalog.associateBy { it.id }
    return distinct().filter { id ->
        val provider = byId[id] ?: return@filter false
        if (!provider.quoteCapable) {
            true
        } else if (quoteCount < FreeQuoteProviderLimit) {
            quoteCount += 1
            true
        } else {
            false
        }
    }
}

internal fun String.providerMarketLabel(): String =
    when (this) {
        "oceania" -> "Australia / Oceania"
        "latam" -> "Latin America"
        "north_america" -> "North America"
        "europe" -> "Europe"
        "asia" -> "Asia"
        else -> "Global"
    }
