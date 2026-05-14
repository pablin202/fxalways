package com.fxalways.observability

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseObservabilitySink(
    context: Context,
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
) : ObservabilitySink {
    private val analytics = FirebaseAnalytics.getInstance(context.applicationContext)

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId.orEmpty())
    }

    override fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
        crashlytics.setCustomKey(name, value.orEmpty())
    }

    override fun trackScreen(screenName: String, properties: Map<String, String>) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            properties.toBundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            },
        )
        crashlytics.log("screen:$screenName")
    }

    override fun trackEvent(name: String, properties: Map<String, String>) {
        analytics.logEvent(name, properties.toBundle())
        crashlytics.log("event:$name ${properties.toLogSuffix()}")
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable, properties: Map<String, String>) {
        properties.forEach { (key, value) -> crashlytics.setCustomKey(key, value) }
        crashlytics.recordException(throwable)
    }
}

fun installFirebaseObservability(context: Context) {
    Observability.install(FirebaseObservabilitySink(context))
}

private fun Map<String, String>.toBundle(): Bundle =
    Bundle().apply {
        forEach { (key, value) -> putString(key, value) }
    }

private fun Map<String, String>.toLogSuffix(): String =
    entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "$key=$value" }
