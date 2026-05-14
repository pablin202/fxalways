package com.fxalways.app

import platform.Foundation.NSBundle

actual object PlatformConfig {
    actual val backendBaseUrl: String = infoString(
        key = "FXAlwaysBackendUrl",
        fallback = "https://us-central1-moneytrackerpro-8ff64.cloudfunctions.net",
    )
    actual val revenueCatApiKey: String = infoString("FXAlwaysRevenueCatApiKey")
    actual val platform: Platform = Platform.Ios
    actual val versionName: String = infoString(key = "CFBundleShortVersionString", fallback = "1.0.0")
    actual val isDebug: Boolean = infoString("FXAlwaysIsDebug").equals("YES", ignoreCase = true)

    private fun infoString(key: String, fallback: String = ""): String {
        val value = NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String
        return value
            ?.takeUnless { it.isBlank() || it.startsWith("$(") }
            ?: fallback
    }
}
