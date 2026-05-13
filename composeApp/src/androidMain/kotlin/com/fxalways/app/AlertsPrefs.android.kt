package com.fxalways.app

actual object AlertsPrefs {
    private const val NAME = "fx_always_prefs"
    private const val KEY_ALERTS_JSON = "alerts_json"

    actual fun alertsJson(): String? =
        prefs().getString(KEY_ALERTS_JSON, null)

    actual fun setAlertsJson(json: String) {
        prefs().edit().putString(KEY_ALERTS_JSON, json).apply()
    }

    private fun prefs() = AndroidAppContext.context.getSharedPreferences(NAME, 0)
}
