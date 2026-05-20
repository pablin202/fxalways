package com.fxalways.app

import platform.Foundation.NSUserDefaults

actual object AppSettingsPrefs {
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

    actual fun themeMode(): ThemeMode {
        val raw = NSUserDefaults.standardUserDefaults.stringForKey(KEY_THEME_MODE)
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
    }

    actual fun setThemeMode(mode: ThemeMode) {
        NSUserDefaults.standardUserDefaults.setObject(mode.name, KEY_THEME_MODE)
    }

    actual fun language(): String =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_LANGUAGE)?.takeIf { it.isNotBlank() } ?: DeviceLocale.language

    actual fun setLanguage(code: String) {
        NSUserDefaults.standardUserDefaults.setObject(code.lowercase(), KEY_LANGUAGE)
    }

    actual fun baseCurrency(): String =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_BASE_CURRENCY) ?: DeviceLocale.currencyCode

    actual fun setBaseCurrency(code: String) {
        NSUserDefaults.standardUserDefaults.setObject(code, KEY_BASE_CURRENCY)
    }

    actual fun travelerCurrency(): String =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_TRAVELER_CURRENCY) ?: "JPY"

    actual fun setTravelerCurrency(code: String) {
        NSUserDefaults.standardUserDefaults.setObject(code, KEY_TRAVELER_CURRENCY)
    }

    actual fun travelerBudgetBase(): Double {
        val value = NSUserDefaults.standardUserDefaults.doubleForKey(KEY_TRAVELER_BUDGET_BASE)
        return if (value > 0.0) value else 100.0
    }

    actual fun setTravelerBudgetBase(amount: Double) {
        NSUserDefaults.standardUserDefaults.setDouble(amount, KEY_TRAVELER_BUDGET_BASE)
    }

    actual fun converterAmountText(): String =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_CONVERTER_AMOUNT_TEXT)?.takeIf { it.isNotBlank() } ?: "1000"

    actual fun setConverterAmountText(amount: String) {
        NSUserDefaults.standardUserDefaults.setObject(amount, KEY_CONVERTER_AMOUNT_TEXT)
    }

    actual fun converterCurrencyCodes(): List<String> =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_CONVERTER_CURRENCY_CODES)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    actual fun setConverterCurrencyCodes(codes: List<String>) {
        NSUserDefaults.standardUserDefaults.setObject(codes.distinct().joinToString(","), KEY_CONVERTER_CURRENCY_CODES)
    }

    actual fun compareCurrencyCodes(): List<String> =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_COMPARE_CURRENCY_CODES)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    actual fun setCompareCurrencyCodes(codes: List<String>) {
        NSUserDefaults.standardUserDefaults.setObject(codes.distinct().joinToString(","), KEY_COMPARE_CURRENCY_CODES)
    }

    actual fun providerPreferenceCodes(): List<String> =
        NSUserDefaults.standardUserDefaults.stringForKey(KEY_PROVIDER_PREFERENCE_CODES)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    actual fun setProviderPreferenceCodes(codes: List<String>) {
        NSUserDefaults.standardUserDefaults.setObject(codes.distinct().joinToString(","), KEY_PROVIDER_PREFERENCE_CODES)
    }

    actual fun userProfile(): UserProfile {
        val raw = NSUserDefaults.standardUserDefaults.stringForKey(KEY_USER_PROFILE) ?: UserProfile.Traveler.name
        return UserProfile.entries.firstOrNull { it.name == raw } ?: UserProfile.Traveler
    }

    actual fun setUserProfile(profile: UserProfile) {
        NSUserDefaults.standardUserDefaults.setObject(profile.name, KEY_USER_PROFILE)
    }

    actual fun cachedPremium(): Boolean? =
        if (NSUserDefaults.standardUserDefaults.objectForKey(KEY_CACHED_PREMIUM) != null) {
            NSUserDefaults.standardUserDefaults.boolForKey(KEY_CACHED_PREMIUM)
        } else {
            null
        }

    actual fun setCachedPremium(enabled: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(enabled, KEY_CACHED_PREMIUM)
    }
}
