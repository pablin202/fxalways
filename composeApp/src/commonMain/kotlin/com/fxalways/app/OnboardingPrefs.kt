package com.fxalways.app

expect object OnboardingPrefs {
    fun hasSeenOnboarding(): Boolean
    fun markSeen()
}
