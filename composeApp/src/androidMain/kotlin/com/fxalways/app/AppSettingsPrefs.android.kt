package com.fxalways.app

actual object AppSettingsPrefs {
    private const val NAME = "fx_always_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_BASE_CURRENCY = "base_currency"
    private const val KEY_TRAVELER_CURRENCY = "traveler_currency"
    private const val KEY_TRAVELER_BUDGET_BASE = "traveler_budget_base"
    private const val KEY_CONVERTER_AMOUNT_TEXT = "converter_amount_text"
    private const val KEY_CONVERTER_CURRENCY_CODES = "converter_currency_codes"
    private const val KEY_COMPARE_CURRENCY_CODES = "compare_currency_codes"
    private const val KEY_PROVIDER_PREFERENCE_CODES = "provider_preference_codes"
    private const val KEY_USER_PROFILE = "user_profile"
    private const val KEY_CACHED_PREMIUM = "cached_premium"
    private const val KEY_FIRST_CONVERSION_TRACKED = "first_conversion_tracked"

    actual fun themeMode(): ThemeMode {
        val raw = prefs().getString(KEY_THEME_MODE, ThemeMode.System.name)
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
    }

    actual fun setThemeMode(mode: ThemeMode) {
        prefs().edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    actual fun language(): String =
        prefs().getString(KEY_LANGUAGE, null)?.takeIf { it.isNotBlank() } ?: DeviceLocale.language

    actual fun setLanguage(code: String) {
        prefs().edit().putString(KEY_LANGUAGE, code.lowercase()).apply()
    }

    actual fun baseCurrency(): String =
        prefs().getString(KEY_BASE_CURRENCY, null) ?: DeviceLocale.currencyCode

    actual fun setBaseCurrency(code: String) {
        prefs().edit().putString(KEY_BASE_CURRENCY, code).apply()
    }

    actual fun travelerCurrency(): String =
        prefs().getString(KEY_TRAVELER_CURRENCY, "JPY") ?: "JPY"

    actual fun setTravelerCurrency(code: String) {
        prefs().edit().putString(KEY_TRAVELER_CURRENCY, code).apply()
    }

    actual fun travelerBudgetBase(): Double =
        prefs().getFloat(KEY_TRAVELER_BUDGET_BASE, 100.0f).toDouble()

    actual fun setTravelerBudgetBase(amount: Double) {
        prefs().edit().putFloat(KEY_TRAVELER_BUDGET_BASE, amount.toFloat()).apply()
    }

    actual fun converterAmountText(): String =
        prefs().getString(KEY_CONVERTER_AMOUNT_TEXT, null)?.takeIf { it.isNotBlank() } ?: "1000"

    actual fun setConverterAmountText(amount: String) {
        prefs().edit().putString(KEY_CONVERTER_AMOUNT_TEXT, amount).apply()
    }

    actual fun converterCurrencyCodes(): List<String> =
        prefs().getString(KEY_CONVERTER_CURRENCY_CODES, null)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    actual fun setConverterCurrencyCodes(codes: List<String>) {
        prefs().edit().putString(KEY_CONVERTER_CURRENCY_CODES, codes.distinct().joinToString(",")).apply()
    }

    actual fun compareCurrencyCodes(): List<String> =
        prefs().getString(KEY_COMPARE_CURRENCY_CODES, null)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    actual fun setCompareCurrencyCodes(codes: List<String>) {
        prefs().edit().putString(KEY_COMPARE_CURRENCY_CODES, codes.distinct().joinToString(",")).apply()
    }

    actual fun providerPreferenceCodes(): List<String> =
        prefs().getString(KEY_PROVIDER_PREFERENCE_CODES, null)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    actual fun setProviderPreferenceCodes(codes: List<String>) {
        prefs().edit().putString(KEY_PROVIDER_PREFERENCE_CODES, codes.distinct().joinToString(",")).apply()
    }

    actual fun userProfile(): UserProfile {
        val raw = prefs().getString(KEY_USER_PROFILE, UserProfile.Traveler.name)
        return UserProfile.entries.firstOrNull { it.name == raw } ?: UserProfile.Traveler
    }

    actual fun setUserProfile(profile: UserProfile) {
        prefs().edit().putString(KEY_USER_PROFILE, profile.name).apply()
    }

    actual fun firstConversionTracked(): Boolean = prefs().getBoolean(KEY_FIRST_CONVERSION_TRACKED, false)

    actual fun setFirstConversionTracked() {
        prefs().edit().putBoolean(KEY_FIRST_CONVERSION_TRACKED, true).apply()
    }

    actual fun cachedPremium(): Boolean? =
        if (prefs().contains(KEY_CACHED_PREMIUM)) prefs().getBoolean(KEY_CACHED_PREMIUM, false) else null

    actual fun setCachedPremium(enabled: Boolean) {
        prefs().edit().putBoolean(KEY_CACHED_PREMIUM, enabled).apply()
    }

    private fun prefs() = AndroidAppContext.context.getSharedPreferences(NAME, 0)
}
