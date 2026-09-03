package com.fxalways.app

actual object PlatformReviewFlow {
    actual fun request(onResult: (shown: Boolean) -> Unit) = onResult(false)
}
