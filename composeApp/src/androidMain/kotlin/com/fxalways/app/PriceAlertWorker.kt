package com.fxalways.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertsCodec
import com.fxalways.app.data.ExchangeApi
import com.fxalways.app.data.PriceAlert
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class PriceAlertWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alerts = AlertsCodec.decode(prefs.getString(KEY_ALERTS_JSON, null), json)
        val activeAlerts = alerts.filter { it.enabled }
        if (activeAlerts.isEmpty()) return Result.success()

        return runCatching {
            val api = ExchangeApi()
            val ratesByBase = activeAlerts
                .map { it.base }
                .distinct()
                .associateWith { base -> api.latest(base) }
            val now = Clock.System.now().toEpochMilliseconds()
            val updatedAlerts = alerts.toMutableTriggeredCopy(now, ratesByBase)

            prefs.edit()
                .putString(KEY_ALERTS_JSON, AlertsCodec.encode(updatedAlerts, json))
                .apply()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun List<PriceAlert>.toMutableTriggeredCopy(now: Long, ratesByBase: Map<String, com.fxalways.app.domain.LatestRates>): List<PriceAlert> =
        map { alert ->
            val currentRate = ratesByBase[alert.base]?.rates?.firstOrNull { it.code == alert.quote }?.value
            if (currentRate != null && alert.shouldTrigger(currentRate, now)) {
                notifyTriggered(alert, currentRate)
                alert.copy(lastTriggeredAtMillis = now)
            } else {
                alert
            }
        }

    private fun PriceAlert.shouldTrigger(currentRate: Double, now: Long): Boolean {
        val crossed = when (direction) {
            AlertDirection.Above -> currentRate >= target
            AlertDirection.Below -> currentRate <= target
        }
        val outsideCooldown = lastTriggeredAtMillis?.let { now - it >= TRIGGER_COOLDOWN_MILLIS } ?: true
        return crossed && outsideCooldown
    }

    private fun notifyTriggered(alert: PriceAlert, currentRate: Double) {
        if (!canPostNotifications()) return
        ensureChannel()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${alert.base}/${alert.quote} alert hit")
            .setContentText("${alert.direction.label} ${formatRate(alert.target)} · now ${formatRate(currentRate)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${alert.base}/${alert.quote} is ${formatRate(currentRate)}. Your target was ${alert.direction.label.lowercase()} ${formatRate(alert.target)}."),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(alert.id.hashCode(), notification)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Price alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Currency pair target alerts"
        }
        manager.createNotificationChannel(channel)
    }

    private val AlertDirection.label: String
        get() = when (this) {
            AlertDirection.Above -> "Above"
            AlertDirection.Below -> "Below"
        }

    private companion object {
        const val PREFS_NAME = "fx_always_prefs"
        const val KEY_ALERTS_JSON = "alerts_json"
        const val CHANNEL_ID = "price_alerts"
        const val TRIGGER_COOLDOWN_MILLIS = 6 * 60 * 60 * 1000L
    }
}
