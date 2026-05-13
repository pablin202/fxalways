package com.fxalways.app

actual object PlatformConfig {
    actual val backendBaseUrl: String = BuildConfig.FX_BACKEND_URL
    actual val revenueCatApiKey: String = BuildConfig.REVENUECAT_API_KEY
    actual val platform: Platform = Platform.Android
    actual val versionName: String = BuildConfig.VERSION_NAME
}
