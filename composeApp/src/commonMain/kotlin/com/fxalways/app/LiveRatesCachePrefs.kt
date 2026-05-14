package com.fxalways.app

expect object LiveRatesCachePrefs {
    fun cacheJson(baseCurrency: String): String?
    fun setCacheJson(baseCurrency: String, json: String)
}
