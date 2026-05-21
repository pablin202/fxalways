package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.fxalways.app.screens.i18n.uiProviderPreferenceTranslations
import com.fxalways.app.screens.i18n.uiSupplementalTranslations
import com.fxalways.app.screens.i18n.uiTranslations

internal val LocalAppLanguage = staticCompositionLocalOf { "en" }

@Composable
internal fun ui(text: String): String = localizedUiText(LocalAppLanguage.current, text)

internal fun localizedUiText(language: String, text: String): String {
    val normalized = language.lowercase().substringBefore("-").substringBefore("_")
    return uiTranslations[normalized]?.get(text)
        ?: uiSupplementalTranslations[normalized]?.get(text)
        ?: uiProviderPreferenceTranslations[normalized]?.get(text)
        ?: uiTranslations["en"]?.get(text)
        ?: text
}
