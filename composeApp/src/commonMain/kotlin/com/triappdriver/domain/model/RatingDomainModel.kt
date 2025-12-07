package com.triappdriver.domain.model

import com.triappdriver.presentation.ViewState
import com.triappdriver.presentation.feature.rating.RatingStep
import kotlinx.serialization.Serializable


data class RatingDomainModel(
    val step: RatingStep = RatingStep.Rating,
    val tripId: String = "",
    val isRated: Boolean = false,

    // Dados da UI (Motorista/Corrida)
    val riderName: String = "",
    val driverPhotoUrl: String? = null,
    val tripPrice: String = "",
    val tripTime: String = "",

    val rating: Int = 0,
) : ViewState<RatingDomainModel>

@Serializable
data class RatingArgs(
    val tripId: String,
    val riderName: String,
    val driverPhotoUrl: String?,
    val price: String,
    val time: String
)