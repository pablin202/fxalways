package com.fxalways.app

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

object GoogleSignInBridge {
    private const val REQUEST_CODE = 8021

    private var pending: ((Result<GoogleSignInAccount>) -> Unit)? = null

    suspend fun requestAccount(): GoogleSignInAccount =
        suspendCancellableCoroutine { continuation ->
            val activity = AndroidAppContext.activity
            if (activity == null) {
                continuation.resumeWithException(IllegalStateException("Activity is not available"))
                return@suspendCancellableCoroutine
            }
            if (pending != null) {
                continuation.resumeWithException(IllegalStateException("Google Sign-In is already in progress"))
                return@suspendCancellableCoroutine
            }

            pending = { result ->
                result
                    .onSuccess { continuation.resume(it) }
                    .onFailure { continuation.resumeWithException(it) }
            }
            continuation.invokeOnCancellation { pending = null }

            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(activity, options)
            activity.startActivityForResult(client.signInIntent, REQUEST_CODE)
        }

    fun onActivityResult(requestCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE) return
        val callback = pending ?: return
        pending = null
        val result = runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        }
        callback(result)
    }

    suspend fun signOut() {
        val activity = AndroidAppContext.activity ?: return
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, options).signOut().await()
    }
}
