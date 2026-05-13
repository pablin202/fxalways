package com.fxalways.app

import java.util.Locale

actual object DeviceLocale {
    actual val language: String
        get() = Locale.getDefault().language.takeIf { it.length == 2 } ?: "en"

    actual val region: String
        get() = Locale.getDefault().country.takeIf { it.length == 2 } ?: "US"
}
