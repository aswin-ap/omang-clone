package com.omang.app.ui.test.models

data class Question(
    var id: String?,
    var question: String,
    var questionType: String?,
    var correctOption: String?,
    var weightage: Int?,
    var options: List<String>,
    var types: String?,
    var answer: String?,
    var score: Int?,
    var testId: String?,
    var imagePath: String?,
)