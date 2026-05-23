package com.fxalways.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.fxalways.observability.Observability
import com.fxalways.observability.installFirebaseObservability
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installFirebaseObservability(this)
        intent?.trackWidgetOpen()
        AndroidAppContext.init(this)
        registerPushToken()
        AndroidAlertScheduler.schedule(this)
        FxAlwaysWidgetProvider.updateAll(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.trackWidgetOpen()
    }

    @Deprecated("Used by GoogleSignInClient until Credential Manager is wired.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GoogleSignInBridge.onActivityResult(requestCode, data)
    }

    private fun Intent.trackWidgetOpen() {
        getStringExtra(EXTRA_WIDGET_SOURCE)?.let { source ->
            Observability.event("widget_used", mapOf("source" to source))
            removeExtra(EXTRA_WIDGET_SOURCE)
        }
    }

    private fun registerPushToken() {
        lifecycleScope.launch {
            val userState = UserBackupGateway.ensureUser()
            PushTokenGateway.registerForUser(userState.uid)
        }
    }

    companion object {
        const val EXTRA_WIDGET_SOURCE = "com.fxalways.app.extra.WIDGET_SOURCE"
    }
}
