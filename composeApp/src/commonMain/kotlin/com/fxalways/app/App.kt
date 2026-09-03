package com.fxalways.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fxalways.app.screens.FxAppShell
import com.fxalways.app.screens.onboarding.OnboardingScreen
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
fun App() {
    var onboardingComplete by remember { mutableStateOf(OnboardingPrefs.hasSeenOnboarding()) }
    val resetGeneration by AccountLifecycle.resetGeneration.collectAsState()
    LaunchedEffect(Unit) { ReviewPrompter.onAppStart() }
    LaunchedEffect(resetGeneration) {
        if (resetGeneration > 0) onboardingComplete = OnboardingPrefs.hasSeenOnboarding()
    }
    key(resetGeneration) {
    if (onboardingComplete) {
        FxAppShell()
    } else {
        FxTheme(dark = true) {
            OnboardingScreen(
                onComplete = {
                    AppSettingsPrefs.setUserProfile(it)
                    OnboardingPrefs.markSeen()
                    Observability.event("onboarding_complete", mapOf("profile" to it.name))
                    onboardingComplete = true
                },
            )
        }
    }
    }
}
