package com.fxalways.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Deep-link request from a widget or notification, consumed once by the app shell. */
object PendingNavigation {
    private val _source = MutableStateFlow<String?>(null)
    val source: StateFlow<String?> = _source

    fun request(source: String) {
        _source.value = source
    }

    fun consume() {
        _source.value = null
    }
}
