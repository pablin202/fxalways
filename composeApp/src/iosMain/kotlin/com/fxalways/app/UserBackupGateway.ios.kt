package com.fxalways.app

import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults

actual object UserBackupGateway {
    private const val KEY_ANONYMOUS_UID = "anonymous_uid"

    actual suspend fun ensureUser(): UserBackupState =
        UserBackupState(
            uid = anonymousUid(),
            isAnonymous = true,
            isAvailable = true,
            providerLabel = "Anonymous",
        )

    actual suspend fun pullSnapshot(): UserBackupSnapshot? = null

    actual suspend fun pushSnapshot(snapshot: UserBackupSnapshot) = Unit

    actual suspend fun linkWithGoogle(localSnapshot: UserBackupSnapshot): AccountLinkResult =
        AccountLinkResult(
            state = UserBackupState(isAvailable = false, errorMessage = "Google Sign-In is Android-only here; iOS will use Apple Sign-In"),
            snapshot = localSnapshot,
        )

    actual suspend fun linkWithApple(localSnapshot: UserBackupSnapshot): AccountLinkResult =
        AccountLinkResult(
            state = UserBackupState(isAvailable = false, errorMessage = "Apple Sign-In is not connected yet"),
            snapshot = localSnapshot,
        )

    actual suspend fun signOutToAnonymous(localSnapshot: UserBackupSnapshot): AccountLinkResult =
        AccountLinkResult(
            state = ensureUser(),
            snapshot = localSnapshot,
        )

    actual suspend fun deleteAccount() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_ANONYMOUS_UID)
    }

    private fun anonymousUid(): String {
        val defaults = NSUserDefaults.standardUserDefaults
        val existing = defaults.stringForKey(KEY_ANONYMOUS_UID)
        if (!existing.isNullOrBlank()) return existing

        val uid = "ios-anon-${NSUUID().UUIDString}"
        defaults.setObject(uid, KEY_ANONYMOUS_UID)
        return uid
    }
}
