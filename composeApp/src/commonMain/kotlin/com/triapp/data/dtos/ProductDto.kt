package com.triapp.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String? = null
)