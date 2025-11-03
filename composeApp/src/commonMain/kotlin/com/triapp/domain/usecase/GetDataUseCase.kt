package com.triapp.domain.usecase

import com.triapp.data.repository.DataRepository
import com.triapp.domain.model.ProductDomainModel
import com.triapp.utils.getCrashlyticsService

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