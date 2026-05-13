package com.fxalways.app

import platform.Foundation.NSUserDefaults

actual object AppSettingsPrefs {
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_BASE_CURRENCY = "base_currency"

    actual fun themeMode(): ThemeMode {
        val raw = NSUserDefaults.standardUserDefaults.stringForKey(KEY_THEME_MODE)
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
    }

    actual fun setThemeMode(mode: ThemeMode) {
        NSUserDefaults.standardUserDefaults.setObject(mode.name, KEY_THEME_MODE)
    }

    actual fun baseCurrency(): String =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_BASE_CURRENCY) ?: "USD"

    actual fun setBaseCurrency(code: String) {
        NSUserDefaults.standardUserDefaults.setObject(code, KEY_BASE_CURRENCY)
    }
}
