package com.fxalways.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast

actual object ExternalUrlOpener {
    actual fun open(url: String) {
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            AndroidAppContext.context.startActivity(intent)
        }.onFailure { error ->
            if (error is ActivityNotFoundException) {
                Toast.makeText(AndroidAppContext.context, "No browser available", Toast.LENGTH_SHORT).show()
            } else {
                throw error
            }
        }
    }
}
