package com.fxalways.app

import com.fxalways.app.data.PriceAlert

expect object AlertTestNotifier {
    fun show(alert: PriceAlert)
}
