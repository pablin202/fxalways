package com.fxalways.app

import platform.Foundation.NSUserDefaults

actual object AlertsPrefs {
    private const val KEY_ALERTS_JSON = "alerts_json"

    actual fun alertsJson(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_ALERTS_JSON)

    actual fun setAlertsJson(json: String) {
        NSUserDefaults.standardUserDefaults.setObject(json, KEY_ALERTS_JSON)
    }
}
