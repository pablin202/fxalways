package com.fxalways.app

actual object AppSettingsPrefs {
    private const val NAME = "fx_always_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_BASE_CURRENCY = "base_currency"

    actual fun themeMode(): ThemeMode {
        val raw = prefs().getString(KEY_THEME_MODE, ThemeMode.System.name)
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
    }

    actual fun setThemeMode(mode: ThemeMode) {
        prefs().edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    actual fun baseCurrency(): String =
        prefs().getString(KEY_BASE_CURRENCY, "USD") ?: "USD"

    actual fun setBaseCurrency(code: String) {
        prefs().edit().putString(KEY_BASE_CURRENCY, code).apply()
    }

    private fun prefs() = AndroidAppContext.context.getSharedPreferences(NAME, 0)
}
