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
enum class AlertKind {
    Target,
    DailyChange,
}

@Serializable
data class PriceAlert(
    val id: String,
    val base: String,
    val quote: String,
    val target: Double,
    val direction: AlertDirection,
    val kind: AlertKind = AlertKind.Target,
    val enabled: Boolean = true,
    val createdAtMillis: Long = 0L,
    val lastTriggeredAtMillis: Long? = null,
)

data class AlertsState(
    val alerts: List<PriceAlert> = emptyList(),
) {
    val activeCount: Int get() = alerts.count { it.enabled }
}

fun PriceAlert.matchesDefinition(
    base: String,
    quote: String,
    target: Double,
    direction: AlertDirection,
    kind: AlertKind,
): Boolean =
    this.base == base &&
        this.quote == quote &&
        this.kind == kind &&
        this.direction == direction &&
        kotlin.math.abs(this.target - target) < duplicateTolerance(kind)

fun duplicateTolerance(kind: AlertKind): Double =
    when (kind) {
        AlertKind.Target -> DUPLICATE_TARGET_TOLERANCE
        AlertKind.DailyChange -> DUPLICATE_PERCENT_TOLERANCE
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
            kind = AlertKind.Target,
        )
    }

    fun addAlert(base: String, quote: String, target: Double, direction: AlertDirection, kind: AlertKind = AlertKind.Target) {
        val now = Clock.System.now().toEpochMilliseconds()
        val alert = PriceAlert(
            id = "$base-$quote-$now",
            base = base,
            quote = quote,
            target = target,
            direction = direction,
            kind = kind,
            createdAtMillis = now,
        )
        val existing = _state.value.alerts.firstOrNull {
            it.matchesDefinition(
                base = base,
                quote = quote,
                target = target,
                direction = direction,
                kind = kind,
            )
        }
        val nextAlerts = if (existing != null) {
            _state.value.alerts.map {
                if (it.id == existing.id) {
                    it.copy(enabled = true, createdAtMillis = now)
                } else {
                    it
                }
            }
        } else {
            _state.value.alerts + alert
        }
        replaceAlerts(nextAlerts)
    }

    fun toggleAlert(id: String) {
        replaceAlerts(_state.value.alerts.map { if (it.id == id) it.copy(enabled = !it.enabled) else it })
    }

    fun resumeAlert(id: String) {
        replaceAlerts(_state.value.alerts.map { if (it.id == id) it.copy(enabled = true) else it })
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
        val sortedAlerts = alerts.sortedWith(
            compareByDescending<PriceAlert> { it.enabled }
                .thenByDescending { it.lastTriggeredAtMillis ?: 0L }
                .thenByDescending { it.createdAtMillis },
        )
        _state.update { it.copy(alerts = sortedAlerts) }
        persist(sortedAlerts)
    }

    private fun loadAlerts(): List<PriceAlert> =
        AlertsCodec.decode(AlertsPrefs.alertsJson(), json)

    private fun persist(alerts: List<PriceAlert>) {
        AlertsPrefs.setAlertsJson(AlertsCodec.encode(alerts, json))
    }

}

private const val DUPLICATE_TARGET_TOLERANCE = 0.0000001
private const val DUPLICATE_PERCENT_TOLERANCE = 0.01

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
