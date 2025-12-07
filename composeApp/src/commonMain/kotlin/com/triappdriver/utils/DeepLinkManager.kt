package com.triappdriver.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Chamado pelo Swift quando o app abre via URL
object DeepLinkManager {
    private val _currentDeepLink = MutableStateFlow<String?>(null)
    val currentDeepLink = _currentDeepLink.asStateFlow()

    fun handleDeepLink(url: String) {
        val route = url.replace("triapp://", "")
        _currentDeepLink.value = route
    }

    fun consumeDeepLink() {
        _currentDeepLink.value = null
    }
}