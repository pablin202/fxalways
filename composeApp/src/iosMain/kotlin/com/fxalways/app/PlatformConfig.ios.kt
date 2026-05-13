package com.fxalways.app

actual object PlatformConfig {
    actual val backendBaseUrl: String = "https://us-central1-moneytrackerpro-8ff64.cloudfunctions.net"
    actual val revenueCatApiKey: String = "test_aDOfCCMYLDGOStPsXdDkPJFanUC"
    actual val platform: Platform = Platform.Ios
    actual val versionName: String = "1.0.0"
}
