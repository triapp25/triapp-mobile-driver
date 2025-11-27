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
    @Query("SELECT * FROM rides")
    fun getRideHistory(): Flow<List<RideEntity>>

    /**
     * Retorna uma corrida específica pelo ID, útil para rastreamento.
     */
    @Query("SELECT * FROM rides WHERE rideId = :rideId LIMIT 1")
    fun getRideById(rideId: String): Flow<RideEntity?>

    /**
     * Limpa todos os dados de corrida para o Tenant especificado.
     */
    @Query("DELETE FROM rides WHERE rideId = :rideId")
    suspend fun clearRideData(rideId: String)
}