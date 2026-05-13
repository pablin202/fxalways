package com.fxalways.app

import com.fxalways.app.data.PriceAlert

actual object AlertTestNotifier {
    actual fun show(alert: PriceAlert) = Unit
}
