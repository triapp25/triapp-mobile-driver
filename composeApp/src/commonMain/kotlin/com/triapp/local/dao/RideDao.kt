package com.triapp.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.triapp.local.entity.RideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {

    /**
     * Insere ou atualiza uma corrida, útil para sincronizar o status.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    /**
     * Retorna a lista de corridas concluídas de um Tenant, ordenadas pela mais recente.
     */
    @Query("SELECT * FROM rides WHERE tenantId = :tenantId AND status = 'COMPLETED' ORDER BY scheduledTime DESC")
    fun getRideHistory(tenantId: String): Flow<List<RideEntity>>

    /**
     * Retorna uma corrida específica pelo ID, útil para rastreamento.
     */
    @Query("SELECT * FROM rides WHERE tenantId = :tenantId AND rideId = :rideId LIMIT 1")
    fun getRideById(tenantId: String, rideId: String): Flow<RideEntity?>

    /**
     * Limpa todos os dados de corrida para o Tenant especificado.
     */
    @Query("DELETE FROM rides WHERE tenantId = :tenantId")
    suspend fun clearRideData(tenantId: String)
}