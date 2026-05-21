package com.fxalways.app.screens.shared

import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig

internal fun privacyPolicyUrl(language: String): String = legalDocumentUrl("privacy", language)

internal fun termsOfUseUrl(language: String): String = legalDocumentUrl("terms", language)

private fun legalDocumentUrl(doc: String, language: String): String {
    val normalizedLanguage = language
        .substringBefore("-")
        .substringBefore("_")
        .lowercase()
        .ifBlank { "en" }
    return "https://fxalways.com/legal?doc=$doc&lang=$normalizedLanguage"
}

internal fun subscriptionManagementUrl(): String =
    when (PlatformConfig.platform) {
        Platform.Android -> "https://play.google.com/store/account/subscriptions"
        Platform.Ios -> "https://apps.apple.com/account/subscriptions"
    }
