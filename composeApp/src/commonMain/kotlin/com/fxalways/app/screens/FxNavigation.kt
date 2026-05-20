package com.fxalways.app.screens

enum class FxTab(val label: String) {
    Rates("Rates"),
    Convert("Convert"),
    Compare("Compare"),
    News("News"),
    More("More"),
}

internal enum class MoreRoute {
    Menu,
    Alerts,
    Watchlist,
    Traveler,
    Settings,
}

internal val MoreRoute.analyticsName: String
    get() = when (this) {
        MoreRoute.Menu -> "more"
        MoreRoute.Alerts -> "alerts"
        MoreRoute.Watchlist -> "watchlist"
        MoreRoute.Traveler -> "traveler"
        MoreRoute.Settings -> "settings"
    }
