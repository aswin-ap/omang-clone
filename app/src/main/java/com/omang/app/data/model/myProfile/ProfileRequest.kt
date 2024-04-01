package com.omang.app.data.model.myProfile

import com.google.gson.annotations.SerializedName

data class ProfileRequest(
    @field:SerializedName("avatar")
    var avatar: Int? = null
)
