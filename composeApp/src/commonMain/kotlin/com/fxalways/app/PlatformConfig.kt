package com.fxalways.app

enum class Platform {
    Android,
    Ios,
}

expect object PlatformConfig {
    val backendBaseUrl: String
    val revenueCatApiKey: String
    val platform: Platform
    val versionName: String
    val isDebug: Boolean
}
