package com.fxalways.app

import java.util.Locale
import java.util.Currency

actual object DeviceLocale {
    actual val language: String
        get() = Locale.getDefault().language.takeIf { it.length == 2 } ?: "en"

    actual val region: String
        get() = Locale.getDefault().country.takeIf { it.length == 2 } ?: "US"

    actual val currencyCode: String
        get() = runCatching {
            Currency.getInstance(Locale("", region)).currencyCode
        }.getOrNull()?.takeIf { it.length == 3 } ?: fallbackCurrencyForRegion(region)
}
