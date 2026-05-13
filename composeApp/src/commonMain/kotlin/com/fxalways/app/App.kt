package com.fxalways.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fxalways.app.screens.FxAppShell
import com.fxalways.app.screens.OnboardingScreen
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun App() {
    var onboardingComplete by remember { mutableStateOf(OnboardingPrefs.hasSeenOnboarding()) }
    if (onboardingComplete) {
        FxAppShell()
    } else {
        FxTheme(dark = true) {
            OnboardingScreen(
                onComplete = {
                    OnboardingPrefs.markSeen()
                    onboardingComplete = true
                },
            )
        }
    }
}
