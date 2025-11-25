package com.triapp.presentation.feature.taxi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triapp.domain.model.TaxiState
import com.triapp.domain.usecase.TaxiUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaxiViewModel(
    private val taxiUseCase: TaxiUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<TaxiState>(TaxiState.Idle)
    val state: StateFlow<TaxiState> = _state

    fun startTaxi() {
        viewModelScope.launch {
            taxiUseCase.startTaxiFlow().collect { uiState ->
                _state.value = uiState
            }
        }
    }
}
