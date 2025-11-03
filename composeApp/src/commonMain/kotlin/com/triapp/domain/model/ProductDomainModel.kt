package com.triapp.domain.model

import com.triapp.presentation.BaseState

data class ProductDomainModel(
    val id: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val imageUrl: String
)

typealias ProductsState = BaseState<ProductDomainModel>