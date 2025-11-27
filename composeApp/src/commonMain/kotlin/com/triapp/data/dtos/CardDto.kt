package com.triapp.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
    val title: String,
    val subtitle: String,
    val extraInfo: String? = null,
    val isDefault: Boolean = false,
)