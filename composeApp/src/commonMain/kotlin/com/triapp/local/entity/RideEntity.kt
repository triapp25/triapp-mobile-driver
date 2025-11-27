package com.triapp.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val rideId: String,
    val date: String,
    val origin: String,
    val destination: String,
    val driverName: String,
    val price: String,
    val paymentMethod: String,
    val distanceTime: String
)