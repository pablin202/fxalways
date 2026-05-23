package com.fxalways.app

import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.PriceAlert

data class AlertNotificationText(
    val channelName: String,
    val channelDescription: String,
    val title: String,
    val body: String,
    val expandedBody: String,
)

data class AlertNotificationCopy(
    val priceAlerts: String,
    val channelDescription: String,
    val alertHit: String,
    val testAlert: String,
    val above: String,
    val below: String,
    val up: String,
    val down: String,
    val now: String,
    val dailyMove: String,
    val twentyFourHour: String,
)

fun localizedAlertNotificationText(
    alert: PriceAlert,
    currentRate: Double?,
    currentDailyChange: Double?,
    language: String = AppSettingsPrefs.language(),
    isTest: Boolean = false,
): AlertNotificationText {
    val copy = alertNotificationCopy(language)
    val pair = "${alert.base}/${alert.quote}"
    val title = if (isTest) {
        "${copy.testAlert} · $pair"
    } else {
        "$pair ${copy.alertHit}"
    }
    val body = when (alert.kind) {
        AlertKind.Target -> "${alert.direction.targetLabel(copy)} ${formatRate(alert.target)} · ${copy.now} ${currentRate?.let(::formatRate) ?: "--"}"
        AlertKind.DailyChange -> "${alert.direction.dailyLabel(copy)} ${formatPercentValue(alert.target)}% · ${copy.twentyFourHour} ${currentDailyChange?.let(::formatSignedPercent) ?: "--"}"
    }
    return AlertNotificationText(
        channelName = copy.priceAlerts,
        channelDescription = copy.channelDescription,
        title = title,
        body = body,
        expandedBody = "$pair: $body",
    )
}

fun localizedTestAlertBody(alert: PriceAlert, language: String = AppSettingsPrefs.language()): String {
    val copy = alertNotificationCopy(language)
    return when (alert.kind) {
        AlertKind.Target -> "${alert.direction.targetLabel(copy)} ${formatRate(alert.target)}"
        AlertKind.DailyChange -> "${alert.direction.dailyLabel(copy)} ${formatPercentValue(alert.target)}% ${copy.dailyMove}"
    }
}

fun alertNotificationCopy(language: String): AlertNotificationCopy =
    when (language.lowercase().substringBefore("-").substringBefore("_")) {
        "es" -> AlertNotificationCopy("Alertas de precio", "Alertas de pares de divisas locales y del servidor", "alcanzó la alerta", "Alerta de prueba", "Por encima de", "Por debajo de", "Sube", "Baja", "ahora", "movimiento diario", "24 h")
        "pt" -> AlertNotificationCopy("Alertas de preço", "Alertas locais e do servidor para pares de moedas", "atingiu o alerta", "Alerta de teste", "Acima de", "Abaixo de", "Sobe", "Cai", "agora", "movimento diário", "24 h")
        "zh" -> AlertNotificationCopy("价格提醒", "服务器和本地货币对提醒", "已触发提醒", "测试提醒", "高于", "低于", "上涨", "下跌", "当前", "日内波动", "24小时")
        "hi" -> AlertNotificationCopy("मूल्य अलर्ट", "सर्वर और स्थानीय मुद्रा जोड़ी अलर्ट", "अलर्ट चालू हुआ", "टेस्ट अलर्ट", "ऊपर", "नीचे", "ऊपर", "नीचे", "अभी", "दैनिक बदलाव", "24घं")
        "fr" -> AlertNotificationCopy("Alertes de prix", "Alertes serveur et locales sur les paires de devises", "a déclenché l'alerte", "Alerte de test", "Au-dessus de", "Sous", "Hausse", "Baisse", "maintenant", "variation quotidienne", "24 h")
        "ar" -> AlertNotificationCopy("تنبيهات السعر", "تنبيهات أزواج العملات من الخادم والمحلية", "تم تشغيل التنبيه", "تنبيه اختبار", "فوق", "تحت", "صعود", "هبوط", "الآن", "حركة يومية", "24س")
        "bn" -> AlertNotificationCopy("দাম অ্যালার্ট", "সার্ভার ও স্থানীয় মুদ্রা জোড়া অ্যালার্ট", "অ্যালার্ট চালু হয়েছে", "টেস্ট অ্যালার্ট", "উপরে", "নিচে", "উপরে", "নিচে", "এখন", "দৈনিক পরিবর্তন", "২৪ঘ")
        "ru" -> AlertNotificationCopy("Оповещения о цене", "Серверные и локальные оповещения по валютным парам", "сработало", "Тестовое оповещение", "Выше", "Ниже", "Рост", "Падение", "сейчас", "дневное движение", "24 ч")
        "ur" -> AlertNotificationCopy("قیمت الرٹس", "سرور اور مقامی کرنسی جوڑی الرٹس", "الرٹ چل گیا", "ٹیسٹ الرٹ", "اوپر", "نیچے", "اوپر", "نیچے", "اب", "روزانہ حرکت", "24گھنٹے")
        "id" -> AlertNotificationCopy("Peringatan harga", "Peringatan pasangan mata uang server dan lokal", "memicu peringatan", "Peringatan tes", "Di atas", "Di bawah", "Naik", "Turun", "sekarang", "pergerakan harian", "24 jam")
        "de" -> AlertNotificationCopy("Preisalarme", "Server- und lokale Waehrungspaar-Alarme", "Alarm ausgelöst", "Testalarm", "Über", "Unter", "Steigt", "Fällt", "jetzt", "Tagesbewegung", "24 h")
        "ja" -> AlertNotificationCopy("価格アラート", "サーバーとローカルの通貨ペアアラート", "アラート発火", "テストアラート", "上回る", "下回る", "上昇", "下落", "現在", "日次変動", "24時間")
        else -> AlertNotificationCopy("Price alerts", "Server and local currency pair alerts", "alert hit", "Test alert", "Above", "Below", "Up", "Down", "now", "daily move", "24h")
    }

private fun AlertDirection.targetLabel(copy: AlertNotificationCopy): String =
    when (this) {
        AlertDirection.Above -> copy.above
        AlertDirection.Below -> copy.below
    }

private fun AlertDirection.dailyLabel(copy: AlertNotificationCopy): String =
    when (this) {
        AlertDirection.Above -> copy.up
        AlertDirection.Below -> copy.down
    }

fun formatRate(value: Double): String =
    when {
        value >= 100 -> value.toString().take(8)
        value >= 1 -> value.toString().take(7)
        else -> value.toString().take(9)
    }

private fun formatPercentValue(value: Double): String =
    ((value * 10.0).toInt() / 10.0).toString()

private fun formatSignedPercent(value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return "$sign${formatPercentValue(kotlin.math.abs(value))}%"
}
