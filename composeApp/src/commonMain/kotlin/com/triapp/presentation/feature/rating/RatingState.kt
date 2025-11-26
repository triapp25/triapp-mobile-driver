package com.triapp.presentation.feature.rating

import com.triapp.presentation.SideEffect
import com.triapp.presentation.ViewIntent


sealed class RatingEffect : SideEffect<Nothing> {
    object Finished : RatingEffect()
    data class ShowToast(val message: String) : RatingEffect()
}

sealed class RatingIntent : ViewIntent<Nothing> {
    data class SelectRating(val rating: Int) : RatingIntent()
    data class UpdateComment(val comment: String) : RatingIntent()
    data class ToggleTag(val tag: String) : RatingIntent()
    data class SelectTip(val tip: String?) : RatingIntent()
    object Submit : RatingIntent()
    object Skip : RatingIntent()
}

enum class RatingStep {
    Rating,
    Finished
}