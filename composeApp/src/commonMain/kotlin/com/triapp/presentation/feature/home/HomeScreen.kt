package com.triapp.presentation.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.triapp.domain.model.HomeDomainModel
import com.triapp.domain.model.RideOption
import com.triapp.utils.Coordinate
import com.triapp.utils.NativeMap
import com.triapp.utils.PlatformMap
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun HomeScreen() {
    val mapController = koinInject<MapController>()
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()

    var platformMap: PlatformMap? by remember { mutableStateOf(null) }

    Box(Modifier.fillMaxSize()) {
        NativeMap(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                mapController.attachMap(map)
                map.moveCamera(Coordinate(40.4168, -3.7038), 13.0)
            },
            mapController = mapController
        )

        Column(Modifier.matchParentSize()) {

            LaunchedEffect(state.driverPosition) {
                state.driverPosition?.let { pos ->
                    mapController.moveDriver(pos.lat, pos.lng)
                }
            }

            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.weight(1f))

                Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp) {
                    when (state.step) {
                        HomeStep.Idle -> IdleView { viewModel.processIntent(HomeIntent.EnterDestination) }
                        HomeStep.SelectingDestination -> DestinationView(
                            state,
                            viewModel::processIntent
                        )

                        HomeStep.ChoosingRide -> RideOptionsView(state, viewModel::processIntent)
                        HomeStep.InProgress -> InProgressView(state, viewModel::processIntent)
                    }
                }
            }
        }
    }
}


@Composable
fun IdleView(onEnter: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("Where to?"); Spacer(Modifier.height(8.dp)); Button(
        onClick = onEnter
    ) { Text("Enter destination") }
    }
}


@Composable
fun DestinationView(state: HomeDomainModel, send: (HomeIntent) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = state.pickup,
            onValueChange = { send(HomeIntent.UpdatePickup(it)) },
            label = { Text("Pickup") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.dropoff,
            onValueChange = { send(HomeIntent.UpdateDropoff(it)) },
            label = { Text("Dropoff") })
        Spacer(Modifier.height(12.dp))
        Button(onClick = { /* compute options then select first for demo */ send(
            HomeIntent.SelectOption(
                RideOption("1", "Standard", "R$ 15.90", "1 min")
            )
        )
        }) { Text("Find rides") }
    }
}


@Composable
fun RideOptionsView(state: HomeDomainModel, send: (HomeIntent) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("Choose your ride")
        Spacer(Modifier.height(8.dp))
        state.availableOptions.ifEmpty {
// show mock options
            listOf(
                RideOption("1", "Economy", "R$ 12.50", "2 min"),
                RideOption("2", "Standard", "R$ 15.90", "1 min"),
                RideOption("3", "Comfort", "R$ 22.40", "3 min")
            )
        }.forEach { option ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                onClick = { send(HomeIntent.SelectOption(option)) }) {
                Row(Modifier.padding(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(option.name); Text(
                        option.eta,
                        style = MaterialTheme.typography.bodySmall
                    )
                    }
                    Text(option.price)
                }
            }
        }


        Spacer(Modifier.height(12.dp))
        Button(onClick = { send(HomeIntent.ConfirmRequest) }) { Text("Confirm ride") }
    }
}


@Composable
fun InProgressView(state: HomeDomainModel, send: (HomeIntent) -> Unit) {
    val ride = state.ride ?: return
    Column(Modifier.padding(16.dp)) {
        Text("In progress • ${ride.etaMinutes} min • ${ride.remainingKm} km remaining")
        Spacer(Modifier.height(8.dp))
        Text("Driver: ${ride.driver.name} • ${ride.driver.car}")
        Spacer(Modifier.height(12.dp))
        Row { Button(onClick = { send(HomeIntent.SimulateArrival) }) { Text("Simulate arrival") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = { send(HomeIntent.CancelRide) }) { Text("Cancel") } }
    }
}