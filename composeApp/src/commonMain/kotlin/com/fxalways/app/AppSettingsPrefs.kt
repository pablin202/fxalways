package com.fxalways.app

enum class ThemeMode {
    System,
    Light,
    Dark,
}

expect object AppSettingsPrefs {
    fun themeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun baseCurrency(): String
    fun setBaseCurrency(code: String)
}
