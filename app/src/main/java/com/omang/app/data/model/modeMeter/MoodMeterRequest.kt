package com.omang.app.data.model.modeMeter

import com.google.gson.annotations.SerializedName
import com.omang.app.data.model.test.Mcq

data class MoodMeterRequest(
    @field:SerializedName("moods")
    var moods: List<MoodsList> = arrayListOf()
)

data class MoodsList (

    @field:SerializedName("mood")
    var mood: Int? = null,

    @field:SerializedName("date")
    var date: String? = null
)
