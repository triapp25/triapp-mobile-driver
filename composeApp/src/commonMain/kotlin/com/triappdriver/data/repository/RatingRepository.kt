package com.triappdriver.data.repository

import com.triappdriver.data.ApiService
import com.triappdriver.data.dtos.RatingBody
import com.triappdriver.domain.model.RatingDomainModel
import com.triappdriver.local.dao.RatingDao
import com.triappdriver.local.entity.RatingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

interface RatingRepository {
    suspend fun saveOrUpdateLocalRating(domainModel: RatingDomainModel)
    suspend fun sendRatingToApi(domainModel: RatingDomainModel)
    suspend fun markAsRatedInLocalDb(tripId: String)
    suspend fun getLastPendingRating(): RatingDomainModel?
}

class RatingRepositoryImpl(
    private val dao: RatingDao,
    private val api: ApiService
) : RatingRepository {

    override suspend fun saveOrUpdateLocalRating(domainModel: RatingDomainModel) {
        withContext(Dispatchers.IO) {
            val entity = domainModel.toEntity()
            dao.insertOrUpdate(entity)
        }
    }

    override suspend fun sendRatingToApi(domainModel: RatingDomainModel) {
        withContext(Dispatchers.IO) {
            val request = RatingBody(
                tripId = domainModel.tripId,
                rating = domainModel.rating,
            )
            api.sendRating(request)
        }
    }

    override suspend fun markAsRatedInLocalDb(tripId: String) {
        withContext(Dispatchers.IO) {
            dao.markAsRated(tripId)
        }
    }

    override suspend fun getLastPendingRating(): RatingDomainModel? {
        return withContext(Dispatchers.IO) {
            dao.getLastPendingRating()?.toDomain()
        }
    }

    private fun RatingDomainModel.toEntity(): RatingEntity {
        return RatingEntity(
            tripId = this.tripId,
            riderName = this.riderName,
            tripPrice = this.tripPrice,
            tripTime = this.tripTime,
            userRating = this.rating,
            isRated = this.isRated,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun RatingEntity.toDomain(): RatingDomainModel {
        return RatingDomainModel(
            tripId = this.tripId,
            riderName = this.riderName,
            tripPrice = this.tripPrice,
            tripTime = this.tripTime,
            rating = this.userRating,
            isRated = this.isRated
        )
    }
}