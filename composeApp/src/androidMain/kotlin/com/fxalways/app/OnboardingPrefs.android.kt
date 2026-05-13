package com.fxalways.app

actual object OnboardingPrefs {
    private const val NAME = "fx_always_prefs"
    private const val KEY_SEEN_ONBOARDING = "seen_onboarding"

    actual fun hasSeenOnboarding(): Boolean =
        AndroidAppContext.context
            .getSharedPreferences(NAME, 0)
            .getBoolean(KEY_SEEN_ONBOARDING, false)

    actual fun markSeen() {
        AndroidAppContext.context
            .getSharedPreferences(NAME, 0)
            .edit()
            .putBoolean(KEY_SEEN_ONBOARDING, true)
            .apply()
    }
}
