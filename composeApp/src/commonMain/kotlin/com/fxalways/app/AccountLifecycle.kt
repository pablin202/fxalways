package com.fxalways.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * App-wide signal that the local identity was wiped (account deleted).
 * [App] keys the whole shell on [resetGeneration], so every remembered store,
 * cache and screen state is rebuilt from scratch and the user lands on onboarding.
 */
object AccountLifecycle {
    private val _resetGeneration = MutableStateFlow(0)
    val resetGeneration: StateFlow<Int> = _resetGeneration

    fun signalReset() {
        _resetGeneration.value += 1
    }
}

/** Clears every locally persisted preference and cache owned by the app. */
expect object LocalDataReset {
    fun clearAll()
}
