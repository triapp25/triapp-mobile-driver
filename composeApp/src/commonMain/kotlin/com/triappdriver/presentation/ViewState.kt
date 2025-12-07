package com.triappdriver.presentation

import androidx.compose.runtime.Immutable

@Immutable
interface ViewState<out T>

@Immutable
interface ViewIntent<out T>

@Immutable
interface SideEffect<out T>

// Classe Base de Estado: Implementa ViewState<T>
data class BaseState<out T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
) : ViewState<T>// A classe base implementa a interface genérica