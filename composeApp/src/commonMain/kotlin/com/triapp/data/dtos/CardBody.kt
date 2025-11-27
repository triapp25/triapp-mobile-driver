package com.triapp.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CardBody(
    val title: String,
    val subtitle: String,
    val extraInfo: String,
    val isDefault: Boolean,
)

