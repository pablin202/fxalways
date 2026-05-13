package com.fxalways.app

import platform.Foundation.NSUserDefaults

actual object WatchlistPrefs {
    private const val KEY_WATCHLIST_JSON = "watchlist_json"

    actual fun watchlistJson(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_WATCHLIST_JSON)

    actual fun setWatchlistJson(json: String) {
        NSUserDefaults.standardUserDefaults.setObject(json, KEY_WATCHLIST_JSON)
    }
}
