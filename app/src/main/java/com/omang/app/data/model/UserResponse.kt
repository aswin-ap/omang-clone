package com.omang.app.data.model

import com.google.gson.annotations.SerializedName


data class UserResponse(

    @SerializedName("page") var page: Int? = null,
    @SerializedName("per_page") var perPage: Int? = null,
    @SerializedName("total") var total: Int? = null,
    @SerializedName("total_pages") var totalPages: Int? = null,
    @SerializedName("data") var data: List<UserApiModel> = arrayListOf(),
    @SerializedName("support") var supportApiModel: SupportApiModel? = SupportApiModel()

)