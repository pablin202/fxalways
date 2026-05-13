package com.fxalways.app

actual object WatchlistPrefs {
    private const val NAME = "fx_always_prefs"
    private const val KEY_WATCHLIST_JSON = "watchlist_json"

    actual fun watchlistJson(): String? =
        prefs().getString(KEY_WATCHLIST_JSON, null)

    actual fun setWatchlistJson(json: String) {
        prefs().edit().putString(KEY_WATCHLIST_JSON, json).apply()
    }

    private fun prefs() = AndroidAppContext.context.getSharedPreferences(NAME, 0)
}
