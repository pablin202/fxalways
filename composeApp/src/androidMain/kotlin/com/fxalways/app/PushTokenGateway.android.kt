package com.fxalways.app

import android.provider.Settings
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

actual object PushTokenGateway {
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_PUSH_TOKENS = "push_tokens"
    private const val PREFS_NAME = "fx_always_push_prefs"
    private const val KEY_LAST_REGISTERED = "last_registered"

    actual suspend fun registerForUser(uid: String?) {
        if (uid.isNullOrBlank()) {
            Log.d(TAG, "FCM token registration skipped: no uid")
            return
        }
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            registerToken(uid, token)
        }.onFailure { error ->
            Log.w(TAG, "FCM token registration failed", error)
        }
    }

    suspend fun registerTokenForCurrentUser(token: String) {
        runCatching {
            val state = UserBackupGateway.ensureUser()
            registerToken(state.uid, token)
        }.onFailure { error ->
            Log.w(TAG, "FCM token refresh registration failed", error)
        }
    }

    suspend fun unregisterForUser(uid: String?, reason: String = "account_changed") {
        if (uid.isNullOrBlank()) return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            unregisterToken(uid, token, reason)
        }.onFailure { error ->
            Log.w(TAG, "FCM token unregister failed", error)
        }
    }

    private suspend fun registerToken(uid: String?, token: String) {
        if (uid.isNullOrBlank() || token.isBlank()) return
        val registrationKey = "$uid:${token.stableTokenId()}"
        if (lastRegisteredKey() == registrationKey) {
            Log.d(TAG, "FCM token registration skipped: unchanged")
            return
        }
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_PUSH_TOKENS)
            .document(token.stableTokenId())
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "deviceId" to deviceId(),
                    "enabled" to true,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
        setLastRegisteredKey(registrationKey)
        Log.i(TAG, "FCM token registered for user ${uid.take(8)}")
    }

    private suspend fun unregisterToken(uid: String, token: String, reason: String) {
        if (token.isBlank()) return
        val tokenId = token.stableTokenId()
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_PUSH_TOKENS)
            .document(tokenId)
            .set(
                mapOf(
                    "enabled" to false,
                    "disabledAt" to FieldValue.serverTimestamp(),
                    "disabledReason" to reason,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
        clearLastRegisteredKeyFor(uid, tokenId)
        Log.i(TAG, "FCM token disabled for user ${uid.take(8)}")
    }

    private fun deviceId(): String =
        runCatching {
            Settings.Secure.getString(AndroidAppContext.context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull().orEmpty()

    private fun String.stableTokenId(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32)
    }

    private fun lastRegisteredKey(): String? =
        AndroidAppContext.context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString(KEY_LAST_REGISTERED, null)

    private fun setLastRegisteredKey(key: String) {
        AndroidAppContext.context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_REGISTERED, key)
            .apply()
    }

    private fun clearLastRegisteredKeyFor(uid: String, tokenId: String) {
        val key = "$uid:$tokenId"
        if (lastRegisteredKey() != key) return
        AndroidAppContext.context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_REGISTERED)
            .apply()
    }

    private const val TAG = "FxPushToken"
}
