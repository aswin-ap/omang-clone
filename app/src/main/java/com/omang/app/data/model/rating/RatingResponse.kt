package com.omang.app.data.model.rating

data class RatingResponse(
    val data: RatingData,
    val message: String,
    val statusCode: Int
)
