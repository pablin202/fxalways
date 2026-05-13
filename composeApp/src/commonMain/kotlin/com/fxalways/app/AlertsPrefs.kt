package com.fxalways.app

expect object AlertsPrefs {
    fun alertsJson(): String?
    fun setAlertsJson(json: String)
}
