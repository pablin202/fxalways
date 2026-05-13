package com.fxalways.app

import platform.Foundation.NSUserDefaults

actual object OnboardingPrefs {
    private const val KEY_SEEN_ONBOARDING = "seen_onboarding"

    actual fun hasSeenOnboarding(): Boolean =
        NSUserDefaults.standardUserDefaults.boolForKey(KEY_SEEN_ONBOARDING)

    actual fun markSeen() {
        NSUserDefaults.standardUserDefaults.setBool(true, KEY_SEEN_ONBOARDING)
    }
}
