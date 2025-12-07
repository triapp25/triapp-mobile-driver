package com.triappdriver.domain.usecase

import com.triappdriver.data.repository.DataRepository
import com.triappdriver.domain.model.ProductDomainModel
import com.triappdriver.utils.getCrashlyticsService

class GetDataUseCase(
    private val dataRepository: DataRepository
) {
    suspend operator fun invoke(): Result<List<ProductDomainModel>> {
        return try {
            val products = dataRepository.fetchProducts()
            Result.success(products)
        } catch (e: Exception) {
            getCrashlyticsService().recordException(e)
            Result.failure(e)
        }
    }
}