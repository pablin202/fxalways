package com.fxalways.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

actual object NotificationPermissionStatus {
    actual val actionLabel: String
        get() = if (canPostNotifications()) "allowed" else "review"

    actual val subtitle: String
        get() = if (canPostNotifications()) {
            "Android can deliver local price alerts while checks run in the background"
        } else {
            "Android permission is required before local price alerts can be delivered"
        }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                AndroidAppContext.context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
}
