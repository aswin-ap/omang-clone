package com.omang.app.data.model.rating

import com.google.gson.annotations.SerializedName

data class RatingRequest(
    @field:SerializedName("type")
    var type: String? = null,

    @field:SerializedName("rating")
    var rating: Float? = 0.0F,

    @field:SerializedName("entityId")
    var entityId: Int = 0
)

