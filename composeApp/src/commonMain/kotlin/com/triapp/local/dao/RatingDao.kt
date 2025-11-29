package com.triapp.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.triapp.local.entity.RatingEntity

@Dao
interface RatingDao {

    // Salva ou atualiza o rascunho da avaliação
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rating: RatingEntity)

    // Marca como avaliado (sucesso na API)
    @Query("UPDATE trip_ratings SET isRated = 1 WHERE tripId = :tripId")
    suspend fun markAsRated(tripId: String)

    // Busca a última avaliação PENDENTE (isRated = 0)
    // Usado pela Home para saber se deve mostrar o modal
    @Query("SELECT * FROM trip_ratings WHERE isRated = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPendingRating(): RatingEntity?

    // Opcional: Limpar avaliações antigas já enviadas
    @Query("DELETE FROM trip_ratings WHERE isRated = 1")
    suspend fun clearCompletedRatings()
}