package com.fxalways.app

actual object NotificationPermissionStatus {
    actual val actionLabel: String = "saved"
    actual val subtitle: String = "Alerts sync with your account; iOS push delivery is next"
    actual fun requestIfNeeded() = Unit
}
