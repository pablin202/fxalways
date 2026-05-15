package com.fxalways.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.fxalways.observability.installFirebaseObservability

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installFirebaseObservability(this)
        AndroidAppContext.init(this)
        AndroidAlertScheduler.schedule(this)
        FxAlwaysWidgetProvider.updateAll(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { App() }
    }

    @Deprecated("Used by GoogleSignInClient until Credential Manager is wired.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GoogleSignInBridge.onActivityResult(requestCode, data)
    }

}
