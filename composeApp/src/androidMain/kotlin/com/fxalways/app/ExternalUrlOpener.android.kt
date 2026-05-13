package com.fxalways.app

import android.content.Intent
import android.net.Uri

actual object ExternalUrlOpener {
    actual fun open(url: String) {
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        AndroidAppContext.context.startActivity(intent)
    }
}
