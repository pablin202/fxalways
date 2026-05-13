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
    fun travelerCurrency(): String
    fun setTravelerCurrency(code: String)
    fun travelerBudgetBase(): Double
    fun setTravelerBudgetBase(amount: Double)
    fun cachedPremium(): Boolean?
    fun setCachedPremium(enabled: Boolean)
}
