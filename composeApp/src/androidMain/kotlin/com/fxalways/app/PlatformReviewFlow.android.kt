package com.fxalways.app

import com.google.android.play.core.review.ReviewManagerFactory

actual object PlatformReviewFlow {
    actual fun request(onResult: (shown: Boolean) -> Unit) {
        val activity = AndroidAppContext.activity ?: return onResult(false)
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) return@addOnCompleteListener onResult(false)
            val current = AndroidAppContext.activity ?: return@addOnCompleteListener onResult(false)
            manager.launchReviewFlow(current, request.result).addOnCompleteListener { onResult(true) }
        }
    }
}
