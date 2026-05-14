package com.fxalways.app

actual object LiveRatesCachePrefs {
    private const val NAME = "fx_always_live_rates_cache"

    actual fun cacheJson(baseCurrency: String): String? =
        prefs().getString(key(baseCurrency), null)

    actual fun setCacheJson(baseCurrency: String, json: String) {
        prefs().edit().putString(key(baseCurrency), json).apply()
    }

    private fun key(baseCurrency: String): String = "live_rates_${baseCurrency.uppercase()}"

    private fun prefs() = AndroidAppContext.context.getSharedPreferences(NAME, 0)
}
