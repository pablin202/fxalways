package com.fxalways.observability

interface ObservabilitySink {
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
    fun trackScreen(screenName: String, properties: Map<String, String> = emptyMap())
    fun trackEvent(name: String, properties: Map<String, String> = emptyMap())
    fun log(message: String)
    fun recordException(throwable: Throwable, properties: Map<String, String> = emptyMap())
}

object NoOpObservabilitySink : ObservabilitySink {
    override fun setUserId(userId: String?) = Unit
    override fun setUserProperty(name: String, value: String?) = Unit
    override fun trackScreen(screenName: String, properties: Map<String, String>) = Unit
    override fun trackEvent(name: String, properties: Map<String, String>) = Unit
    override fun log(message: String) = Unit
    override fun recordException(throwable: Throwable, properties: Map<String, String>) = Unit
}

object Observability {
    private var sink: ObservabilitySink = NoOpObservabilitySink

    fun install(sink: ObservabilitySink) {
        this.sink = sink
    }

    fun setUserId(userId: String?) {
        sink.setUserId(userId)
    }

    fun setUserProperty(name: String, value: String?) {
        sink.setUserProperty(name.normalizeAnalyticsName(), value)
    }

    fun screen(screenName: String, properties: Map<String, String> = emptyMap()) {
        val normalizedScreen = screenName.normalizeAnalyticsName()
        sink.trackScreen(normalizedScreen, properties.normalizedAnalyticsProperties())
    }

    fun event(name: String, properties: Map<String, String> = emptyMap()) {
        sink.trackEvent(name.normalizeAnalyticsName(), properties.normalizedAnalyticsProperties())
    }

    fun log(message: String) {
        sink.log(message)
    }

    fun recordException(throwable: Throwable, properties: Map<String, String> = emptyMap()) {
        sink.recordException(throwable, properties.normalizedAnalyticsProperties())
    }
}

fun String.normalizeAnalyticsName(): String =
    lowercase()
        .replace(Regex("[^a-z0-9_]+"), "_")
        .trim('_')
        .take(40)
        .ifBlank { "unknown" }

private fun Map<String, String>.normalizedAnalyticsProperties(): Map<String, String> =
    entries.associate { (key, value) ->
        key.normalizeAnalyticsName() to value.take(100)
    }
