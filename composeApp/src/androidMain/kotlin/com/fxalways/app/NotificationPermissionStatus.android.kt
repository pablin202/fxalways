package com.fxalways.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

actual object NotificationPermissionStatus {
    actual val actionLabel: String
        get() = if (canPostNotifications()) "allowed" else "review"

    actual val subtitle: String
        get() = if (canPostNotifications()) {
            "Server push alerts are active; Android keeps local alert checks as fallback"
        } else {
            "Android permission is required before server or local price alerts can be delivered"
        }

    actual fun requestIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (canPostNotifications()) return
        val activity = AndroidAppContext.activity ?: return
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST,
        )
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                AndroidAppContext.context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private const val NOTIFICATION_PERMISSION_REQUEST = 1001
}
