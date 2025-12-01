package com.triapp.data.repository

import com.triapp.data.ApiService
import com.triapp.data.dtos.CardBody
import com.triapp.data.dtos.LocationDTO
import com.triapp.data.dtos.RideRequestDTO
import com.triapp.domain.model.Coordinate
import com.triapp.domain.model.ProductDomainModel
import com.triapp.domain.model.WalletDomainModel

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
}


class DataRepositoryImpl(
    private val apiService: ApiService
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