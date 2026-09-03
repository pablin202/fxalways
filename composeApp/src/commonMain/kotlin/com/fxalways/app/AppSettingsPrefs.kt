package com.fxalways.app

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class UserProfile {
    Traveler,
    CryptoHolder,
    Remittances,
    Freelancer,
    Savings,
}

expect object AppSettingsPrefs {
    fun themeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun language(): String
    fun setLanguage(code: String)
    fun baseCurrency(): String
    fun setBaseCurrency(code: String)
    fun travelerCurrency(): String
    fun setTravelerCurrency(code: String)
    fun travelerBudgetBase(): Double
    fun setTravelerBudgetBase(amount: Double)
    fun converterAmountText(): String
    fun setConverterAmountText(amount: String)
    fun converterCurrencyCodes(): List<String>
    fun setConverterCurrencyCodes(codes: List<String>)
    fun compareCurrencyCodes(): List<String>
    fun setCompareCurrencyCodes(codes: List<String>)
    fun providerPreferenceCodes(): List<String>
    fun setProviderPreferenceCodes(codes: List<String>)
    fun userProfile(): UserProfile
    fun setUserProfile(profile: UserProfile)
    fun firstConversionTracked(): Boolean
    fun setFirstConversionTracked()
    fun sessionCount(): Int
    fun incrementSessionCount(): Int
    fun reviewPromptShown(): Boolean
    fun setReviewPromptShown()
    fun cachedPremium(): Boolean?
    fun setCachedPremium(enabled: Boolean)
}
