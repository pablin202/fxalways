package com.fxalways.app

import platform.Foundation.NSUserDefaults

actual object LiveRatesCachePrefs {
    actual fun cacheJson(baseCurrency: String): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(key(baseCurrency))

    actual fun setCacheJson(baseCurrency: String, json: String) {
        NSUserDefaults.standardUserDefaults.setObject(json, key(baseCurrency))
    }

    private fun key(baseCurrency: String): String = "live_rates_${baseCurrency.uppercase()}"
}
