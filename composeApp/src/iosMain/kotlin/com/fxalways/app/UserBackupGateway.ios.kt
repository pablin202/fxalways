package com.fxalways.app

actual object UserBackupGateway {
    actual suspend fun ensureUser(): UserBackupState =
        UserBackupState(isAvailable = false, errorMessage = "iOS Firebase Auth is not connected yet")

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
            state = UserBackupState(isAvailable = false, errorMessage = "iOS Firebase Auth is not connected yet"),
            snapshot = localSnapshot,
        )
}
