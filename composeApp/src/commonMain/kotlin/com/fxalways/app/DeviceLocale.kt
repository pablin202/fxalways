package com.fxalways.app

expect object DeviceLocale {
    val language: String
    val region: String
    val currencyCode: String
}

fun fallbackCurrencyForRegion(region: String): String =
    when (region.uppercase()) {
        "AD", "AT", "BE", "CY", "DE", "EE", "ES", "FI", "FR", "GR", "HR", "IE", "IT", "LT", "LU", "LV", "MC", "MT", "NL", "PT", "SI", "SK", "SM", "VA" -> "EUR"
        "GB", "GG", "IM", "JE" -> "GBP"
        "AU", "CX", "CC", "HM", "KI", "NR", "NF", "TV" -> "AUD"
        "CA" -> "CAD"
        "CH", "LI" -> "CHF"
        "CN" -> "CNY"
        "JP" -> "JPY"
        "BR" -> "BRL"
        "MX" -> "MXN"
        "NZ", "CK", "NU", "PN", "TK" -> "NZD"
        "SG" -> "SGD"
        "AR" -> "ARS"
        "CL" -> "CLP"
        "CO" -> "COP"
        "PE" -> "PEN"
        "UY" -> "UYU"
        "PY" -> "PYG"
        "BO" -> "BOB"
        "ZA" -> "ZAR"
        "IN" -> "INR"
        "KR" -> "KRW"
        "ID" -> "IDR"
        "TH" -> "THB"
        "MY" -> "MYR"
        "PH" -> "PHP"
        "VN" -> "VND"
        "TR" -> "TRY"
        "SE" -> "SEK"
        "NO", "SJ" -> "NOK"
        "DK", "FO", "GL" -> "DKK"
        "PL" -> "PLN"
        "CZ" -> "CZK"
        "HU" -> "HUF"
        "RO" -> "RON"
        "BG" -> "BGN"
        "IL" -> "ILS"
        "AE" -> "AED"
        "SA" -> "SAR"
        "EG" -> "EGP"
        "HK" -> "HKD"
        "TW" -> "TWD"
        else -> "USD"
    }
