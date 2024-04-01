package com.omang.app.data.model.updates

import com.google.gson.annotations.SerializedName


data class UpdatedItem(
    @field:SerializedName("updateType")
    val updateType: Int? = null,

    @field:SerializedName("details")
    val details: List<DetailsItem?>? = null
)
