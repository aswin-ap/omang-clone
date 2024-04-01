package com.omang.app.ui.test.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NewTest(
    var id: String,
    var title: String,
    var startTime: String,
    var endTime: String,
    var instruction: String,
    var createdOn: String,
    var subjectName: String,
)