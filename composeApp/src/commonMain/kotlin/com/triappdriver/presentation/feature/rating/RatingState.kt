package com.triappdriver.presentation.feature.rating

import com.triappdriver.presentation.SideEffect
import com.triappdriver.presentation.ViewIntent


sealed class RatingEffect : SideEffect<Nothing> {
    object Finished : RatingEffect()
    data class ShowToast(val message: String) : RatingEffect()
}

sealed class RatingIntent : ViewIntent<Nothing> {
    data class SelectRating(val rating: Int) : RatingIntent()
    object Submit : RatingIntent()
    object Skip : RatingIntent()
}

enum class RatingStep {
    Rating,
    Finished
}