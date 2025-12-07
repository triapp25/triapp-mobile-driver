package com.triappdriver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
/**
 * Classe base para ViewModels MVI.
 * @param S O estado CONCRETO da feature (ex: ProductsState, que é BaseListState<...>)
 * @param I A Intent CONCRETA da feature (ex: ProductsIntent)
 * @param E O Effect CONCRETO da feature (ex: ProductsEffect)
 */
abstract class BaseViewModel<S, I, E>(
    initialState: S
) : ViewModel()
        where S : ViewState<*>,
              I : ViewIntent<*>,
              E : SideEffect<*> {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>()
    val effect = _effect.receiveAsFlow()

    protected fun updateState(block: (S) -> S) {
        _state.update(block)
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun processIntent(intent: I)
}