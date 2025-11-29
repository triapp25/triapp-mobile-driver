package com.triapp.domain.model

import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.rating.RatingStep
import kotlinx.serialization.Serializable


data class RatingDomainModel(
    val step: RatingStep = RatingStep.Rating,
    val tripId: String = "",
    val isRated: Boolean = false,

    // Dados da UI (Motorista/Corrida)
    val driverName: String = "",
    val driverCar: String = "",
    val driverPhotoUrl: String? = null,
    val tripPrice: String = "",
    val tripTime: String = "",

    val rating: Int = 0,
    val comment: String = "",
    val selectedTags: List<String> = emptyList(),
    val selectedTip: String? = null
) : ViewState<RatingDomainModel>

@Serializable
data class RatingArgs(
    val tripId: String,
    val driverName: String,
    val driverCar: String,
    val driverPhotoUrl: String?,
    val price: String,
    val time: String
)