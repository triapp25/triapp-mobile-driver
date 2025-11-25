package com.triapp.data.repository

import com.triapp.data.ApiService
import com.triapp.data.dtos.StartTaxiBody
import com.triapp.domain.model.LatLng
import com.triapp.domain.model.ProductDomainModel

interface DataRepository {
    suspend fun fetchProducts(): List<ProductDomainModel>
    suspend fun startTaxi(): String
}


class DataRepositoryImpl(
    private val apiService: ApiService
) : DataRepository {

    override suspend fun fetchProducts(): List<ProductDomainModel> {
        // Mapeamento de DTO para Domínio
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

    override suspend fun startTaxi(): String {
        return apiService.startTaxi(
            StartTaxiBody(
                latLngInit = LatLng(1.0, 1.0),
                latLngEnd = LatLng(1.0, 1.0)
            )
        )
    }
}