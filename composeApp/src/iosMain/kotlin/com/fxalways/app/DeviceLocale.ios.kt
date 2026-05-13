package com.fxalways.app

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual object DeviceLocale {
    actual val language: String
        get() = NSLocale.currentLocale.languageCode?.takeIf { it.length == 2 } ?: "en"

    actual val region: String
        get() = NSLocale.currentLocale.countryCode?.takeIf { it.length == 2 } ?: "US"
}
