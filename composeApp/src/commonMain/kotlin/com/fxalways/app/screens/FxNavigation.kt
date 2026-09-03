package com.fxalways.app.screens

/** Bottom tabs (issue #12): Today · Convert · Send · Alerts · More. */
enum class FxTab(val label: String) {
    Today("Today"),
    Convert("Convert"),
    Send("Send"),
    Alerts("Alerts"),
    More("More"),
}

internal enum class MoreRoute {
    Menu,
    Alerts,
    Watchlist,
    Traveler,
    Settings,
    Compare,
    News,
    Crypto,
}

internal val MoreRoute.analyticsName: String
    get() = when (this) {
        MoreRoute.Menu -> "more"
        MoreRoute.Alerts -> "alerts"
        MoreRoute.Watchlist -> "watchlist"
        MoreRoute.Traveler -> "traveler"
        MoreRoute.Settings -> "settings"
        MoreRoute.Compare -> "compare"
        MoreRoute.News -> "news"
        MoreRoute.Crypto -> "crypto"
    }

/** Where a widget tap or notification should land; consumed by the shell. */
internal fun widgetSourceDestination(source: String): Pair<FxTab, MoreRoute> = when (source) {
    "convert" -> FxTab.Convert to MoreRoute.Menu
    "send" -> FxTab.Send to MoreRoute.Menu
    "alerts" -> FxTab.Alerts to MoreRoute.Menu
    "watchlist" -> FxTab.More to MoreRoute.Watchlist
    "traveler" -> FxTab.More to MoreRoute.Traveler
    else -> FxTab.Today to MoreRoute.Menu
}
