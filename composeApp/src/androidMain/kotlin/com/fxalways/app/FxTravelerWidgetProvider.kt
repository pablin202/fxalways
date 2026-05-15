package com.fxalways.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray
import org.json.JSONObject

class FxTravelerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildWidgetViews(context))
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, FxTravelerWidgetProvider::class.java))
            ids.forEach { appWidgetManager.updateAppWidget(it, buildWidgetViews(context)) }
        }

        private fun buildWidgetViews(context: Context): RemoteViews {
            val prefs = context.applicationContext.getSharedPreferences("fx_always_prefs", 0)
            val base = prefs.getString("base_currency", null)?.takeIf { it.isNotBlank() } ?: "USD"
            val destination = prefs.getString("traveler_currency", null)?.takeIf { it.isNotBlank() } ?: "JPY"
            val budgetBase = prefs.getFloat("traveler_budget_base", 100.0f).toDouble()
            val rawCache = context.applicationContext
                .getSharedPreferences("fx_always_live_rates_cache", 0)
                .getString("live_rates_${base.uppercase()}", null)
            val snapshot = FxTravelerWidgetSnapshotParser.fromCacheJson(
                raw = rawCache,
                selectedCurrency = destination,
                budgetBase = budgetBase,
            ) ?: FxTravelerWidgetSnapshot.empty(destination)
            return RemoteViews(context.packageName, R.layout.fx_traveler_widget).apply {
                setTextViewText(R.id.traveler_widget_status, snapshot.status)
                setTextViewText(R.id.traveler_widget_destination, snapshot.destinationLabel)
                setTextViewText(R.id.traveler_widget_budget, snapshot.localBudget)
                setTextViewText(R.id.traveler_widget_daily, snapshot.dailyBudget)
                setTextViewText(R.id.traveler_widget_cash, snapshot.cashBuffer)
                setTextViewText(R.id.traveler_widget_footer_label, snapshot.footerLabel)
                setTextViewText(R.id.traveler_widget_footer_value, snapshot.footerValue)
                setOnClickPendingIntent(R.id.traveler_widget_root, launchPendingIntent(context))
            }
        }

        private fun launchPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

data class FxTravelerWidgetSnapshot(
    val status: String,
    val destinationLabel: String,
    val localBudget: String,
    val dailyBudget: String,
    val cashBuffer: String,
    val footerLabel: String,
    val footerValue: String,
) {
    companion object {
        fun empty(destination: String): FxTravelerWidgetSnapshot =
            FxTravelerWidgetSnapshot(
                status = "OPEN",
                destinationLabel = destination,
                localBudget = "--",
                dailyBudget = "Open app",
                cashBuffer = "Waiting",
                footerLabel = "Traveler",
                footerValue = "Refresh rates",
            )
    }
}

object FxTravelerWidgetSnapshotParser {
    private const val TripDays = 3

    fun fromCacheJson(raw: String?, selectedCurrency: String, budgetBase: Double): FxTravelerWidgetSnapshot? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            val base = json.optString("baseCurrency", "USD").ifBlank { "USD" }
            val updated = json.optString("updatedLabel", "")
            val destination = travelerWidgetDestination(selectedCurrency)
            val rate = listOfRates(json.optJSONArray("favorites"))
                .plus(listOfRates(json.optJSONArray("converter")))
                .plus(listOfRates(json.optJSONArray("allFiat")))
                .firstOrNull { it.code == selectedCurrency }
                ?: return@runCatching FxTravelerWidgetSnapshot.empty(selectedCurrency)
            val localBudget = budgetBase.coerceAtLeast(0.0) * rate.rate
            val dailyBudget = localBudget / TripDays
            val cashBuffer = localBudget * destination.cashBufferPct
            FxTravelerWidgetSnapshot(
                status = if (updated.contains("cached", ignoreCase = true)) "CACHE" else "LIVE",
                destinationLabel = "${destination.city.uppercase()} · ${rate.code}",
                localBudget = "${destination.symbol}${formatWidgetAmount(localBudget)}",
                dailyBudget = "${destination.symbol}${formatWidgetAmount(dailyBudget)} / day",
                cashBuffer = "${destination.symbol}${formatWidgetAmount(cashBuffer)} cash",
                footerLabel = "Rate · ${updated.updatedShortLabel()}",
                footerValue = "1 $base = ${formatWidgetRate(rate.rate)} ${rate.code}",
            )
        }.getOrNull()
    }

    private fun listOfRates(array: JSONArray?): List<TravelerWidgetRate> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val code = item.optString("code").takeIf { it.isNotBlank() } ?: continue
                val rate = item.optDouble("rate").takeIf { it.isFinite() && it > 0.0 } ?: continue
                add(TravelerWidgetRate(code, rate))
            }
        }.distinctBy { it.code }
    }
}

private data class TravelerWidgetRate(val code: String, val rate: Double)

private data class TravelerWidgetDestination(
    val city: String,
    val symbol: String,
    val cashBufferPct: Double,
)

private fun travelerWidgetDestination(code: String): TravelerWidgetDestination =
    when (code) {
        "JPY" -> TravelerWidgetDestination("Tokyo", "¥", 0.25)
        "EUR" -> TravelerWidgetDestination("Eurozone", "€", 0.15)
        "GBP" -> TravelerWidgetDestination("London", "£", 0.10)
        "MXN" -> TravelerWidgetDestination("Mexico City", "$", 0.30)
        "BRL" -> TravelerWidgetDestination("Sao Paulo", "R$", 0.20)
        "AUD" -> TravelerWidgetDestination("Sydney", "A$", 0.10)
        "CAD" -> TravelerWidgetDestination("Toronto", "C$", 0.10)
        "CHF" -> TravelerWidgetDestination("Zurich", "Fr ", 0.10)
        else -> TravelerWidgetDestination(code, "$code ", 0.20)
    }

private fun formatWidgetAmount(value: Double): String =
    "%,.0f".format(value)

private fun formatWidgetRate(value: Double): String =
    when {
        value >= 100.0 -> "%.2f".format(value)
        value >= 1.0 -> "%.4f".format(value)
        value >= 0.0001 -> "%.6f".format(value)
        else -> "<0.0001"
    }

private fun String.updatedShortLabel(): String =
    split("·")
        .lastOrNull()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "Updated"
