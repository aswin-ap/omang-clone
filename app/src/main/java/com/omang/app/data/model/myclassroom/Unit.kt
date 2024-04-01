package com.omang.app.data.model.myclassroom

import com.google.gson.annotations.SerializedName

data class Unit(
    @field:SerializedName("id")
    var id: Int? = null,

    @field:SerializedName("lessons")
    var lessons: List<Int>? = null,

    @field:SerializedName("mcqs")
    var mcqs: List<Int>? = null
)