package com.triapp.data.repository

import com.triapp.data.ApiService
import com.triapp.data.dtos.RatingBody
import com.triapp.domain.model.RatingDomainModel
import com.triapp.local.dao.RatingDao
import com.triapp.local.entity.RatingEntity
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
                comment = domainModel.comment,
                selectedTags = domainModel.selectedTags,
                selectedTip = domainModel.selectedTip
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
            driverName = this.driverName,
            driverCar = this.driverCar,
            tripPrice = this.tripPrice,
            tripTime = this.tripTime,
            userRating = this.rating,
            userComment = this.comment,
            userSelectedTags = this.selectedTags.joinToString(","), // Lista -> String
            userSelectedTip = this.selectedTip,
            isRated = this.isRated,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun RatingEntity.toDomain(): RatingDomainModel {
        return RatingDomainModel(
            tripId = this.tripId,
            driverName = this.driverName,
            driverCar = this.driverCar,
            tripPrice = this.tripPrice,
            tripTime = this.tripTime,
            rating = this.userRating,
            comment = this.userComment,
            selectedTags = if (this.userSelectedTags.isEmpty()) emptyList() else this.userSelectedTags.split(
                ","
            ),
            selectedTip = this.userSelectedTip,
            isRated = this.isRated
        )
    }
}