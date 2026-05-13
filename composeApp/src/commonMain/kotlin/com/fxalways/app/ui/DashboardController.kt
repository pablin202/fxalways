package com.fxalways.app.ui

import com.fxalways.app.data.DashboardRepository
import com.fxalways.app.domain.DashboardState
import com.fxalways.app.subscription.PlaceholderSubscriptionGateway
import com.fxalways.app.subscription.SubscriptionGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardController(
    private val repository: DashboardRepository = DashboardRepository(),
    private val subscriptions: SubscriptionGateway = PlaceholderSubscriptionGateway(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init {
        refresh()
    }

    fun onAmountChange(value: String) {
        _state.update { it.copy(amount = value.filter { char -> char.isDigit() || char == '.' }) }
    }

    fun onBaseChange(value: String) {
        _state.update { it.copy(base = value) }
        refresh()
    }

    fun onQuoteChange(value: String) {
        _state.update { it.copy(quote = value) }
        refresh()
    }

    fun refresh() {
        val current = _state.value
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val subscription = subscriptions.currentState()
                val latest = repository.latest(current.base)
                val historyDays = if (subscription.isPremium) 1825 else 365
                val history = repository.history(current.base, current.quote, historyDays)
                val watchCards = repository.watchCards()
                _state.update {
                    it.copy(
                        latest = latest,
                        historical = history,
                        watchCards = watchCards,
                        isLoading = false,
                        isPremium = subscription.isPremium,
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "",
                    )
                }
            }
        }
    }

    fun buyMonthly() {
        scope.launch {
            val subscription = subscriptions.purchaseMonthly()
            _state.update { it.copy(isPremium = subscription.isPremium) }
            refresh()
        }
    }
}
