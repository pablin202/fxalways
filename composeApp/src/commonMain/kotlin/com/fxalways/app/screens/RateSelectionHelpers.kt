package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.subscription.cap
import com.fxalways.designsystem.components.FxRate
import kotlinx.datetime.Clock

internal fun compactCurrencyChoices(
    currencies: List<FxRate>,
    selectedCode: String,
    limit: Int,
): List<FxRate> {
    val distinct = currencies.distinctBy { it.code }
    val byCode = distinct.associateBy { it.code }
    val selected = byCode[selectedCode]
    val popular = PopularCurrencyCodes.mapNotNull { byCode[it] }
    return (listOfNotNull(selected) + popular.filterNot { it.code == selectedCode })
        .take(limit)
        .ifEmpty { distinct.take(limit) }
}

@Composable
internal fun localizedRuntimeLabel(label: String): String =
    when {
        label == "cached · mock" -> "${ui("cached")} · mock"
        label == "Auto-refresh off" -> ui("Auto-refresh off")
        label.startsWith("Auto-refresh every ") -> "${ui("Auto-refresh every")} ${label.substringAfter("every ").substringBefore(" min")} ${ui("min")}"
        label == "loading" -> ui("loading")
        label == "updated just now" -> ui("updated just now")
        label.startsWith("updated ") && label.endsWith("m ago") -> "${ui("updated")} ${label.removePrefix("updated ").removeSuffix("m ago")}m ${ui("ago")}"
        label.startsWith("updated ") -> "${ui("updated")} ${label.removePrefix("updated ")}"
        label.startsWith("refreshed ") -> "${ui("refreshed")} ${label.removePrefix("refreshed ")}"
        else -> ui(label)
    }

@Composable
internal fun localizedCurrencyName(name: String): String = ui(name)

@Composable
internal fun localizedRate(rate: FxRate): FxRate =
    rate.copy(
        name = localizedCurrencyName(rate.name),
        caption = if (rate.caption == "cached · mock") localizedRuntimeLabel(rate.caption) else ui(rate.caption),
    )

@Composable
internal fun localizedSubscriptionMessage(message: String): String =
    when {
        message.startsWith("No RevenueCat package is configured for ") -> {
            val plan = message.removePrefix("No RevenueCat package is configured for ").removeSuffix(".")
            "${ui("No RevenueCat package is configured for")} ${ui(plan)}."
        }
        message.startsWith("Pro active") -> message.replace("Pro active", ui("Pro active"))
        message == "RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases." -> ui("RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases.")
        message == "RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases." -> ui("RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases.")
        message == "RevenueCat key missing. Restore is not connected yet." -> ui("RevenueCat key missing. Restore is not connected yet.")
        message == "RevenueCat unavailable." -> ui("RevenueCat unavailable.")
        message == "No offering packages are configured in RevenueCat." -> ui("No offering packages are configured in RevenueCat.")
        message == "Purchase did not complete." -> ui("Purchase did not complete.")
        message == "Restore failed." -> ui("Restore failed.")
        message == "Dev override only affects local debug gating." -> ui("Dev override only affects local debug gating.")
        else -> ui(message)
    }

internal fun shortAgeLabel(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 60 -> "Now"
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ago"
        elapsedSeconds < 86_400 -> "${elapsedSeconds / 3600}h ago"
        else -> "${elapsedSeconds / 86_400}d ago"
    }
}

@Composable
internal fun localizedShortAgeLabel(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 60 -> ui("Now")
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ${ui("ago")}"
        elapsedSeconds < 86_400 -> "${elapsedSeconds / 3600}h ${ui("ago")}"
        else -> "${elapsedSeconds / 86_400}d ${ui("ago")}"
    }
}

internal fun LiveRatesState.alertRates(isPremium: Boolean): List<FxRate> =
    (favorites + compare + converter + allFiat + availableCryptoRates(isPremium))
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

internal fun LiveRatesState.portfolioRates(isPremium: Boolean = false): List<FxRate> =
    (converter + favorites + compare + allFiat + availableCryptoRates(isPremium))
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code == baseCurrency || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.defaultCryptoRates(): List<FxRate> {
    val byCode = crypto.associateBy { it.code }
    return DefaultCryptoCodes.mapNotNull { byCode[it] }
}

internal fun LiveRatesState.visibleDashboardCryptoRates(isPremium: Boolean, trackedCurrencyCodes: List<String>): List<FxRate> {
    val byCode = crypto.associateBy { it.code }
    val trackedCrypto = if (isPremium) {
        trackedCurrencyCodes
            .filter { it !in DefaultCryptoCodes }
            .mapNotNull { byCode[it] }
    } else {
        emptyList()
    }
    return (defaultCryptoRates() + trackedCrypto).distinctBy { it.code }
}

private fun LiveRatesState.availableCryptoRates(isPremium: Boolean): List<FxRate> =
    if (isPremium) {
        crypto
    } else {
        defaultCryptoRates()
    }

internal fun LiveRatesState.converterAvailableRates(isPremium: Boolean): List<FxRate> =
    (allFiat + favorites + compare + converter + availableCryptoRates(isPremium))
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

internal fun LiveRatesState.compareAvailableRates(isPremium: Boolean): List<FxRate> =
    (compare + favorites + converter + allFiat + availableCryptoRates(isPremium))
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

internal fun converterTargetCodes(
    selectedCurrencyCodes: List<String>,
    availableRates: List<FxRate>,
    baseCurrency: String,
    limit: Int,
): List<String> {
    val availableCodes = availableRates.map { it.code }.toSet()
    val selected = selectedCurrencyCodes
        .filter { it != baseCurrency && it in availableCodes }
        .distinct()
    val defaults = PopularCurrencyCodes
        .filter { it != baseCurrency && it in availableCodes && it !in selected }
    val targetLimit = limit.cap(availableRates.size).coerceAtLeast(1)
    return (selected + defaults)
        .take(targetLimit)
        .ifEmpty {
            availableRates
                .map { it.code }
                .filter { it != baseCurrency }
                .take(targetLimit)
        }
}
