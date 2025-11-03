package com.triapp.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val rideId: String,   // ID único da corrida vindo do backend
    val tenantId: String,          // Para isolamento Multi-Tenant
    val userId: String,            // Opcional, mas útil para o contexto
    val status: String,            // Ex: "PENDING", "ACTIVE", "COMPLETED", "CANCELED"
    val originAddress: String,
    val destinationAddress: String,
    val driverName: String?,
    val driverRating: Double?,
    val price: Double?,            // Preço final ou estimado
    val scheduledTime: Long        // Hora de agendamento/início
)