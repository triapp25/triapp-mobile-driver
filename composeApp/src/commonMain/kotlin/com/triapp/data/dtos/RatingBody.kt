package com.triapp.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RatingBody(
    val tripId: String,
    val rating: Int,
    val comment: String,
    val selectedTags: List<String>,
    val selectedTip: String?
)