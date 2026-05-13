package com.fxalways.app.data

import com.fxalways.app.AlertsPrefs
import com.fxalways.designsystem.components.FxRate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class AlertDirection {
    Above,
    Below,
}

@Serializable
data class PriceAlert(
    val id: String,
    val base: String,
    val quote: String,
    val target: Double,
    val direction: AlertDirection,
    val enabled: Boolean = true,
    val createdAtMillis: Long = 0L,
    val lastTriggeredAtMillis: Long? = null,
)

data class AlertsState(
    val alerts: List<PriceAlert> = emptyList(),
) {
    val activeCount: Int get() = alerts.count { it.enabled }
}

class AlertsStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(AlertsState(alerts = loadAlerts()))
    val state: StateFlow<AlertsState> = _state

    fun addQuickAlert(base: String, rate: FxRate) {
        addAlert(
            base = base,
            quote = rate.code,
            target = rate.rate * 1.01,
            direction = AlertDirection.Above,
        )
    }

    fun addAlert(base: String, quote: String, target: Double, direction: AlertDirection) {
        val now = Clock.System.now().toEpochMilliseconds()
        val alert = PriceAlert(
            id = "$base-$quote-$now",
            base = base,
            quote = quote,
            target = target,
            direction = direction,
            createdAtMillis = now,
        )
        replaceAlerts(_state.value.alerts + alert)
    }

    fun toggleAlert(id: String) {
        replaceAlerts(_state.value.alerts.map { if (it.id == id) it.copy(enabled = !it.enabled) else it })
    }

    fun deleteAlert(id: String) {
        replaceAlerts(_state.value.alerts.filterNot { it.id == id })
    }

    fun markTriggered(id: String, triggeredAtMillis: Long = Clock.System.now().toEpochMilliseconds()) {
        replaceAlerts(_state.value.alerts.map { if (it.id == id) it.copy(lastTriggeredAtMillis = triggeredAtMillis) else it })
    }

    fun replaceAll(alerts: List<PriceAlert>) {
        replaceAlerts(alerts)
    }

    private fun replaceAlerts(alerts: List<PriceAlert>) {
        _state.update { it.copy(alerts = alerts) }
        persist(alerts)
    }

    private fun loadAlerts(): List<PriceAlert> =
        AlertsCodec.decode(AlertsPrefs.alertsJson(), json)

    private fun persist(alerts: List<PriceAlert>) {
        AlertsPrefs.setAlertsJson(AlertsCodec.encode(alerts, json))
    }
}

object AlertsCodec {
    fun decode(raw: String?, json: Json = Json { ignoreUnknownKeys = true }): List<PriceAlert> =
        runCatching {
            raw
                ?.let { json.decodeFromString<AlertsPayload>(it).alerts }
                ?: emptyList()
        }.getOrElse { emptyList() }

    fun encode(alerts: List<PriceAlert>, json: Json = Json { ignoreUnknownKeys = true }): String =
        json.encodeToString(AlertsPayload(alerts))
}

@Serializable
data class AlertsPayload(
    val alerts: List<PriceAlert> = emptyList(),
)
