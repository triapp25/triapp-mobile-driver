package com.triapp.domain.model

import com.triapp.presentation.ViewState
import com.triapp.presentation.feature.rating.RatingStep

data class RatingDomainModel(
    val step: RatingStep = RatingStep.Rating,
    val rating: Int = 0,
    val comment: String = "",
    val selectedTags: List<String> = emptyList(),
    val selectedTip: String? = null
) : ViewState<RatingDomainModel>