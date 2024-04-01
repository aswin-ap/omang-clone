package com.omang.app.ui.survey.models
import com.omang.app.ui.test.models.Question

data class Survey(
    var id: String,
    var title: String,
    var instruction: String,
    var count: String,
    var starttime: String,
    var endtime: String,
    var questions: List<Question> = ArrayList(),
    var downloadId: String,
    var survey_status: Int
)