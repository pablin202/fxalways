package com.fxalways.app

import com.fxalways.app.data.AlertDirection

val AlertDirection.label: String
    get() = when (this) {
        AlertDirection.Above -> "Above"
        AlertDirection.Below -> "Below"
    }

fun formatRate(value: Double): String =
    when {
        value >= 100 -> value.toString().take(8)
        value >= 1 -> value.toString().take(7)
        else -> value.toString().take(9)
    }
