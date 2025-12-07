package com.triappdriver.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_ratings")
data class RatingEntity(
    @PrimaryKey
    val tripId: String,

    // Dados para exibir na UI (Header)
    val riderName: String,
    val tripPrice: String,
    val tripTime: String,

    // Estado da Avaliação (Draft do usuário)
    val userRating: Int,

    // Controle de fluxo
    val isRated: Boolean, // false = pendente, true = enviado
    val timestamp: Long
)