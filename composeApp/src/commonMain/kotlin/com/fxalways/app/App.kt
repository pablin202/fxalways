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
import com.fxalways.app.screens.converter.amountBucket
import com.fxalways.app.screens.onboarding.OnboardingResult
import com.fxalways.app.screens.onboarding.OnboardingScreen
import com.fxalways.app.screens.profile.preset
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
                onComplete = { result ->
                    applyOnboardingResult(result)
                    onboardingComplete = true
                },
            )
        }
    }
    }
}

/**
 * Persists what onboarding learned so the first Home/Convert screen already shows the user's corridor
 * with a real amount (issue #10): base currency, converter/compare lists led by the destination,
 * amount, traveler destination and the inferred profile.
 */
internal fun applyOnboardingResult(result: OnboardingResult) {
    val corridor = result.corridor
    val preset = result.profile.preset()
    val converterCodes = (listOf(corridor.target) + preset.converterCodes).filter { it != corridor.base }.distinct().take(4)
    val compareCodes = (listOf(corridor.target) + preset.compareCodes).filter { it != corridor.base }.distinct().take(4)
    AppSettingsPrefs.setCorridor(corridor)
    AppSettingsPrefs.setUserProfile(result.profile)
    AppSettingsPrefs.setBaseCurrency(corridor.base)
    AppSettingsPrefs.setConverterCurrencyCodes(converterCodes)
    AppSettingsPrefs.setCompareCurrencyCodes(compareCodes)
    AppSettingsPrefs.setTravelerCurrency(corridor.target)
    AppSettingsPrefs.setConverterAmountText(corridor.amount.toLong().toString())
    OnboardingPrefs.markSeen()
    Observability.event(
        "onboarding_corridor_set",
        mapOf("base" to corridor.base, "target" to corridor.target, "amount_bucket" to amountBucket(corridor.amount), "cadence" to corridor.cadence.name.lowercase()),
    )
    Observability.event(
        "onboarding_complete",
        mapOf("profile" to result.profile.name, "profile_inferred" to "true", "notifications" to result.notificationsRequested.toString()),
    )
}
