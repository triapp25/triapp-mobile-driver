package com.triapp.presentation.feature.taxi

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.triapp.domain.model.TaxiState

@Composable
fun TaxiScreen(viewModel: TaxiViewModel) {
    val state = viewModel.state.collectAsState()

    when (state.value) {

        is TaxiState.Idle -> {
            Button(onClick = { viewModel.startTaxi() }) {
                Text("Chamar Táxi")
            }
        }

        is TaxiState.Started -> {
            Text("Corrida iniciada… aguardando taxista…")
        }

        is TaxiState.Update -> {
            val update = state as TaxiState.Update
            Text("Status: ${update.status}")
        }

        is TaxiState.Completed -> {
            Text("Corrida concluída!")
        }

        is TaxiState.Error -> {
            Text("Erro: ${(state as TaxiState.Error).message}")
        }
    }
}
