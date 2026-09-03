package com.fxalways.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

class FxAlwaysWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildWidgetViews(context))
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, FxAlwaysWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                ids.forEach { appWidgetManager.updateAppWidget(it, buildWidgetViews(context)) }
            }
        }

        private fun buildWidgetViews(context: Context): RemoteViews {
            val snapshot = FxWidgetSnapshotParser.fromCacheJson(widgetCacheJson(context), widgetCorridor(context)) ?: FxWidgetSnapshot.empty()
            return RemoteViews(context.packageName, R.layout.fx_always_widget).apply {
                setTextViewText(R.id.widget_status, snapshot.status)
                setTextViewText(R.id.widget_primary_pair, snapshot.primaryPair)
                setTextViewText(R.id.widget_primary_value, snapshot.primaryValue)
                setTextViewText(R.id.widget_tile_one_label, snapshot.tileOneLabel)
                setTextViewText(R.id.widget_tile_one_value, snapshot.tileOneValue)
                setTextColor(R.id.widget_tile_one_value, snapshot.tileOneColor)
                setTextViewText(R.id.widget_tile_two_label, snapshot.tileTwoLabel)
                setTextViewText(R.id.widget_tile_two_value, snapshot.tileTwoValue)
                setTextColor(R.id.widget_tile_two_value, snapshot.tileTwoColor)
                setTextViewText(R.id.widget_footer_label, snapshot.footerLabel)
                setTextViewText(R.id.widget_footer_value, snapshot.footerValue)
                setTextColor(R.id.widget_footer_value, snapshot.footerColor)
                setOnClickPendingIntent(R.id.widget_root, launchPendingIntent(context, "rates", 0))
                setOnClickPendingIntent(R.id.widget_primary_pair, launchPendingIntent(context, "convert", 1))
                setOnClickPendingIntent(R.id.widget_primary_value, launchPendingIntent(context, "convert", 2))
                setOnClickPendingIntent(R.id.widget_tile_one, launchPendingIntent(context, "watchlist", 3))
                setOnClickPendingIntent(R.id.widget_tile_two, launchPendingIntent(context, "watchlist", 4))
                setOnClickPendingIntent(R.id.widget_footer_tile, launchPendingIntent(context, "rates", 5))
            }
        }

        private fun launchPendingIntent(context: Context, source: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_WIDGET_SOURCE, source)
            }
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun widgetCorridor(context: Context): String? =
            context.applicationContext.getSharedPreferences("fx_always_prefs", 0).getString("corridor", null)

        private fun widgetCacheJson(context: Context): String? {
            val prefs = context.applicationContext.getSharedPreferences("fx_always_prefs", 0)
            val base = prefs.getString("base_currency", null)?.takeIf { it.isNotBlank() } ?: "USD"
            return context.applicationContext
                .getSharedPreferences("fx_always_live_rates_cache", 0)
                .getString("live_rates_${base.uppercase()}", null)
        }
    }
}

actual fun refreshFxWidgets() {
    runCatching {
        FxAlwaysWidgetProvider.updateAll(AndroidAppContext.context)
        FxTravelerWidgetProvider.updateAll(AndroidAppContext.context)
    }
}

data class FxWidgetSnapshot(
    val status: String,
    val primaryPair: String,
    val primaryValue: String,
    val tileOneLabel: String,
    val tileOneValue: String,
    val tileOneColor: Int,
    val tileTwoLabel: String,
    val tileTwoValue: String,
    val tileTwoColor: Int,
    val footerLabel: String,
    val footerValue: String,
    val footerColor: Int,
) {
    companion object {
        fun empty(): FxWidgetSnapshot =
            FxWidgetSnapshot(
                status = "OPEN",
                primaryPair = "Open FX Always",
                primaryValue = "--",
                tileOneLabel = "BTC",
                tileOneValue = "Waiting",
                tileOneColor = WidgetTextDim,
                tileTwoLabel = "ETH",
                tileTwoValue = "Waiting",
                tileTwoColor = WidgetTextDim,
                footerLabel = "Open app",
                footerValue = "Refresh rates",
                footerColor = WidgetTextDim,
            )
    }
}

object FxWidgetSnapshotParser {
    fun fromCacheJson(raw: String?, corridor: String? = null): FxWidgetSnapshot? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            val base = json.optString("baseCurrency", "USD").ifBlank { "USD" }
            val updated = json.optString("updatedLabel", "")
            val rates = listOfRates(json.optJSONArray("favorites")) +
                listOfRates(json.optJSONArray("converter")) +
                listOfRates(json.optJSONArray("crypto"))
            val primary = rates.firstOrNull { it.code != base } ?: return@runCatching FxWidgetSnapshot.empty()
            // Today's decision variant (issue #11): the user's corridor amount converted at today's rate.
            val corridorInfo = Corridor.decode(corridor)?.takeIf { it.target != base }
            val corridorRate = corridorInfo?.let { info -> rates.firstOrNull { it.code == info.target } }
            val btc = rates.firstOrNull { it.code == "BTC" }
            val eth = rates.firstOrNull { it.code == "ETH" }
            val bestMover = rates
                .filter { it.code != base }
                .maxByOrNull { abs(it.change24h) }
            FxWidgetSnapshot(
                status = if (updated.contains("cached", ignoreCase = true)) "CACHE ${updated.cacheAgeLabel()}" else "DAILY",
                primaryPair = if (corridorInfo != null && corridorRate != null) "$base ${formatWidgetAmount(corridorInfo.amount)} → ${corridorInfo.target}" else "$base / ${primary.code}",
                primaryValue = if (corridorInfo != null && corridorRate != null) formatWidgetAmount(corridorInfo.amount * corridorRate.rate) else formatWidgetRate(primary.rate),
                tileOneLabel = btc?.let { "BTC ${formatWidgetRate(it.rate)}" } ?: "BTC",
                tileOneValue = btc?.changeLabel() ?: "Waiting",
                tileOneColor = btc?.changeColor() ?: WidgetTextDim,
                tileTwoLabel = eth?.let { "ETH ${formatWidgetRate(it.rate)}" } ?: "ETH",
                tileTwoValue = eth?.changeLabel() ?: "Waiting",
                tileTwoColor = eth?.changeColor() ?: WidgetTextDim,
                footerLabel = bestMover?.let { "Best mover · ${updated.updatedShortLabel()}" } ?: "Updated",
                footerValue = bestMover?.let { "${it.code} ${it.changeLabel()}" } ?: updated.updatedShortLabel(),
                footerColor = bestMover?.changeColor() ?: WidgetTextDim,
            )
        }.getOrNull()
    }

    private fun listOfRates(array: JSONArray?): List<WidgetRate> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val code = item.optString("code").takeIf { it.isNotBlank() } ?: continue
                val rate = item.optDouble("rate").takeIf { it.isFinite() && it > 0.0 } ?: continue
                add(WidgetRate(code = code, rate = rate, change24h = item.optDouble("change24h").takeIf { it.isFinite() } ?: 0.0))
            }
        }.distinctBy { it.code }
    }
}

private data class WidgetRate(
    val code: String,
    val rate: Double,
    val change24h: Double,
)

private fun WidgetRate.changeLabel(): String {
    val sign = if (change24h >= 0.0) "+" else "-"
    return "$sign${formatWidgetNumber(abs(change24h))}%"
}

private fun WidgetRate.changeColor(): Int =
    if (change24h >= 0.0) WidgetUp else WidgetDown

private fun formatWidgetRate(value: Double): String =
    when {
        value >= 100.0 -> formatWidgetNumber(value, decimals = 2)
        value >= 1.0 -> formatWidgetNumber(value, decimals = 4)
        value >= 0.0001 -> formatWidgetNumber(value, decimals = 6)
        else -> "<0.0001"
    }

private fun formatWidgetAmount(value: Double): String =
    if (value >= 1_000.0) "%,.0f".format(value) else formatWidgetNumber(value, decimals = if (value >= 100.0) 0 else 2)

private fun formatWidgetNumber(value: Double, decimals: Int = 2): String =
    "%.${decimals}f".format(value)

private fun String.updatedShortLabel(): String =
    split("·")
        .lastOrNull()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "Updated"

private fun String.cacheAgeLabel(): String =
    updatedShortLabel()
        .removePrefix("cached")
        .trim()
        .ifBlank { "saved" }

private val WidgetUp = Color.rgb(94, 234, 212)
private val WidgetDown = Color.rgb(248, 113, 113)
private val WidgetTextDim = Color.rgb(155, 168, 186)
