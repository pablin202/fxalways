package com.fxalways.app

expect object WatchlistPrefs {
    fun watchlistJson(): String?
    fun setWatchlistJson(json: String)
}
