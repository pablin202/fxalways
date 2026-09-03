package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.fxalways.app.screens.i18n.localizedProviderCatalogText
import com.fxalways.app.screens.i18n.uiProviderPreferenceTranslations
import com.fxalways.app.screens.i18n.uiProviderQuoteTranslations
import com.fxalways.app.screens.i18n.uiP9ClosedTestTranslations
import com.fxalways.app.screens.i18n.uiRateFreshnessTranslations
import com.fxalways.app.screens.i18n.uiSupplementalTranslations
import com.fxalways.app.screens.i18n.uiLaunchLanguageTranslations
import com.fxalways.app.screens.i18n.uiOnboardingTranslations
import com.fxalways.app.screens.i18n.uiNavigationTranslations
import com.fxalways.app.screens.i18n.uiTodayDecisionTranslations
import com.fxalways.app.screens.i18n.uiTranslations

internal val LocalAppLanguage = staticCompositionLocalOf { "en" }

@Composable
internal fun ui(text: String): String = localizedUiText(LocalAppLanguage.current, text)

internal fun localizedUiText(language: String, text: String): String {
    val normalized = language.lowercase().substringBefore("-").substringBefore("_")
    return uiTranslations[normalized]?.get(text)
        ?: uiSupplementalTranslations[normalized]?.get(text)
        ?: uiRateFreshnessTranslations[normalized]?.get(text)
        ?: uiP9ClosedTestTranslations[normalized]?.get(text)
        ?: uiProviderPreferenceTranslations[normalized]?.get(text)
        ?: localizedProviderCatalogText(normalized, text)
        ?: uiProviderQuoteTranslations[normalized]?.get(text)
        ?: uiLaunchLanguageTranslations[normalized]?.get(text)
        ?: uiOnboardingTranslations[normalized]?.get(text)
        ?: uiTodayDecisionTranslations[normalized]?.get(text)
        ?: uiNavigationTranslations[normalized]?.get(text)
        ?: uiTranslations["en"]?.get(text)
        ?: text
}
