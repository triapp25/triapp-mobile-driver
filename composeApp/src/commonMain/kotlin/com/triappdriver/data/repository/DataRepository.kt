package com.triappdriver.data.repository

import androidx.annotation.Discouraged
import com.triappdriver.data.ApiIngestionService
import com.triappdriver.data.ApiService
import com.triappdriver.data.dtos.CardBody
import com.triappdriver.data.dtos.LocationDTO
import com.triappdriver.data.dtos.RideRequestDTO
import com.triappdriver.data.dtos.RideStatusRequestAcceptedDTO
import com.triappdriver.data.dtos.RideStatusRequestCompletedDTO
import com.triappdriver.data.dtos.RideStatusRequestOnGoingDTO
import com.triappdriver.data.dtos.RideStatusRequestRejectedDTO
import com.triappdriver.domain.model.Coordinate
import com.triappdriver.domain.model.ProductDomainModel
import com.triappdriver.domain.model.WalletDomainModel
import com.triappdriver.local.AppPreferences
import com.triappdriver.utils.FirebaseAuthManager

interface DataRepository {
    suspend fun fetchProducts(): List<ProductDomainModel>
    suspend fun rideRequest(
        riderId: String,
        pickup: Coordinate,
        pickupAddress: String,
        dropoff: Coordinate,
        dropoffAddress: String,
        riderNote: String
    ): String

    suspend fun cardOptions(model: WalletDomainModel): WalletDomainModel

    suspend fun rideRejected(tripId: String)
    suspend fun rideAccepted(tripId: String, location: LocationDTO)
    suspend fun rideOnGoing(tripId: String, location: LocationDTO)
    suspend fun rideCompleted(
        tripId: String,
        location: LocationDTO,
        distanceKm: Double,
        durationMin: Double
    )
}


class DataRepositoryImpl(
    private val apiService: ApiService,
    private val authManager: FirebaseAuthManager,
) : DataRepository {

    override suspend fun fetchProducts(): List<ProductDomainModel> {
        return apiService.getProducts().map { productDto ->
            ProductDomainModel(
                id = productDto.id,
                name = productDto.name,
                description = productDto.description.orEmpty(),
                formattedPrice = productDto.price.toString(),
                imageUrl = productDto.imageUrl ?: ""
            )
        }
    }

    override suspend fun rideRequest(
        riderId: String,
        pickup: Coordinate,
        pickupAddress: String,
        dropoff: Coordinate,
        dropoffAddress: String,
        riderNote: String,

        ): String {
        return apiService.rideRequest(
            RideRequestDTO(
                riderId, LocationDTO(
                    pickup.latitude,
                    pickup.longitude,
                    pickupAddress
                ),
                LocationDTO(
                    dropoff.latitude,
                    dropoff.longitude,
                    dropoffAddress
                ), riderNote
            )
        ).tripId
    }

    override suspend fun rideAccepted(
        tripId: String,
        location: LocationDTO
    ) {
        apiService.rideAccepted(
            tripId,
            RideStatusRequestAcceptedDTO(
                tripId = tripId,
                driverId = authManager.getCurrentUser()?.userId.orEmpty(),
                currentLocation = location
            )
        )
    }

    override suspend fun rideRejected(
        tripId: String
    ) {
        apiService.rideRejected(
            tripId,
            RideStatusRequestRejectedDTO(
                tripId = tripId,
                driverId = authManager.getCurrentUser()?.userId.orEmpty(),
                status = "REJECTED",
                reason = "Sem disponibilidade"
            )
        )
    }

    override suspend fun rideOnGoing(
        tripId: String,
        location: LocationDTO
    ) {
        apiService.rideOnGoing(
            tripId,
            RideStatusRequestOnGoingDTO(
                tripId = tripId,
                driverId = authManager.getCurrentUser()?.userId.orEmpty(),
                pickupLocation = location
            )
        )
    }

    override suspend fun rideCompleted(
        tripId: String,
        location: LocationDTO,
        distanceKm: Double,
        durationMin: Double
    ) {
        apiService.rideCompleted(
            tripId,
            RideStatusRequestCompletedDTO(
                tripId = tripId,
                driverId = authManager.getCurrentUser()?.userId.orEmpty(),
                dropoffLocation = location,
                distanceKm = distanceKm,
                durationMin = durationMin,
            )
        )
    }

    override suspend fun cardOptions(model: WalletDomainModel): WalletDomainModel {
        val body = CardBody(
            model.title,
            model.subtitle,
            model.extraInfo.orEmpty(),
            model.isDefault
        )

        when (model.cardOption) {
            WalletDomainModel.CardOption.CREATE -> apiService.createCard(body)
            WalletDomainModel.CardOption.UPDATED -> apiService.updateCard(body)
            WalletDomainModel.CardOption.DELETE -> apiService.deleteCard(body)
            WalletDomainModel.CardOption.ANOTHER -> apiService.getCards()
        }

        return model
    }
}