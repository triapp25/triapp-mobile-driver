package com.triapp.data.repository

import com.triapp.domain.model.RideHistoryDomainModel
import com.triapp.domain.model.toDomain
import com.triapp.domain.model.toEntity
import com.triapp.local.dao.RideDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RideRepository {
    fun getRideHistory(): Flow<List<RideHistoryDomainModel>>
    fun getRideById(id: String): Flow<RideHistoryDomainModel?>
    suspend fun saveRide(ride: RideHistoryDomainModel)
    suspend fun deleteRide(id: String)
}

class RideRepositoryImpl(
    private val dao: RideDao
) : RideRepository {

    override fun getRideHistory(): Flow<List<RideHistoryDomainModel>> {
        return dao.getRideHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRideById(id: String): Flow<RideHistoryDomainModel?> {
        return dao.getRideById(id).map { it?.toDomain() }
    }

    override suspend fun saveRide(ride: RideHistoryDomainModel) {
        dao.insertRide(ride.toEntity())
    }

    override suspend fun deleteRide(id: String) {
        dao.clearRideData(id)
    }
}