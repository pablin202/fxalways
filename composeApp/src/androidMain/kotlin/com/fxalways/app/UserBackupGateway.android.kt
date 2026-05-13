package com.fxalways.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual object UserBackupGateway {
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_BACKUPS = "backups"
    private const val DOCUMENT_DEFAULT = "default"
    private const val FIELD_PAYLOAD_JSON = "payloadJson"
    private const val FIELD_SCHEMA_VERSION = "schemaVersion"
    private const val FIELD_UPDATED_AT = "updatedAtMillis"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    actual suspend fun ensureUser(): UserBackupState =
        runCatching {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            UserBackupState(
                uid = user?.uid,
                isAnonymous = user?.isAnonymous != false,
                isAvailable = user != null,
                providerLabel = user?.providerData
                    ?.firstOrNull { it.providerId != "firebase" && it.providerId != "password" }
                    ?.providerId
                    ?.toProviderLabel(),
                email = user?.email,
                displayName = user?.displayName,
            )
        }.getOrElse { error ->
            UserBackupState(isAvailable = false, errorMessage = error.message)
        }

    actual suspend fun pullSnapshot(): UserBackupSnapshot? {
        val uid = requireUid()
        val document = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_BACKUPS)
            .document(DOCUMENT_DEFAULT)
            .get()
            .await()

        val payload = document.getString(FIELD_PAYLOAD_JSON) ?: return null
        return runCatching { json.decodeFromString<UserBackupSnapshot>(payload) }.getOrNull()
    }

    actual suspend fun pushSnapshot(snapshot: UserBackupSnapshot) {
        val uid = requireUid()
        val payload = json.encodeToString(snapshot)
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_BACKUPS)
            .document(DOCUMENT_DEFAULT)
            .set(
                mapOf(
                    FIELD_PAYLOAD_JSON to payload,
                    FIELD_SCHEMA_VERSION to snapshot.schemaVersion,
                    FIELD_UPDATED_AT to snapshot.updatedAtMillis,
                ),
                SetOptions.merge(),
            )
            .await()
    }

    actual suspend fun linkWithGoogle(localSnapshot: UserBackupSnapshot): AccountLinkResult {
        val account = GoogleSignInBridge.requestAccount()
        val idToken = account.idToken ?: error("Google did not return an ID token")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: auth.signInAnonymously().await().user ?: error("Firebase user is unavailable")

        val finalUser = try {
            currentUser.linkWithCredential(credential).await().user
        } catch (collision: FirebaseAuthUserCollisionException) {
            auth.signInWithCredential(credential).await().user
        } ?: error("Google account link failed")

        val remoteSnapshot = pullSnapshot()
        val merged = mergeBackupSnapshots(localSnapshot, remoteSnapshot)
        pushSnapshot(merged)

        return AccountLinkResult(
            state = UserBackupState(
                uid = finalUser.uid,
                isAnonymous = finalUser.isAnonymous,
                isAvailable = true,
                providerLabel = "Google",
                email = finalUser.email ?: account.email,
                displayName = finalUser.displayName ?: account.displayName,
            ),
            snapshot = merged,
        )
    }

    actual suspend fun linkWithApple(localSnapshot: UserBackupSnapshot): AccountLinkResult =
        AccountLinkResult(
            state = ensureUser().copy(errorMessage = "Apple Sign-In is only available on iOS"),
            snapshot = localSnapshot,
        )

    actual suspend fun signOutToAnonymous(localSnapshot: UserBackupSnapshot): AccountLinkResult {
        GoogleSignInBridge.signOut()
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        val user = auth.signInAnonymously().await().user ?: error("Anonymous Firebase sign-in failed")
        val state = UserBackupState(
            uid = user.uid,
            isAnonymous = true,
            isAvailable = true,
        )
        pushSnapshot(localSnapshot)
        return AccountLinkResult(state = state, snapshot = localSnapshot)
    }

    private suspend fun requireUid(): String {
        val state = ensureUser()
        return state.uid ?: error(state.errorMessage ?: "Firebase user is unavailable")
    }

    private fun String.toProviderLabel(): String =
        when (this) {
            GoogleAuthProvider.PROVIDER_ID -> "Google"
            else -> this
        }
}
