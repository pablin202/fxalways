package com.fxalways.app

actual object LocalDataReset {
    /** Every SharedPreferences file the app writes; keep in sync with the *Prefs.android.kt objects. */
    private val preferenceFiles = listOf(
        "fx_always_prefs",
        "fx_always_push_prefs",
        "fx_always_live_rates_cache",
    )

    actual fun clearAll() {
        val context = AndroidAppContext.context
        preferenceFiles.forEach { name ->
            context.getSharedPreferences(name, 0).edit().clear().commit()
        }
    }
}
