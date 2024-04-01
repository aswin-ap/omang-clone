package com.omang.app.ui.test.models

data class Results(
    var score: String,
    var wrongCount: String,
    var correctCount: String,
    var attemptedCount: String,
    var totalMarks: String,
    var totalMarksObtained: String,
    var percentageObtained: String,
    var startTime: String,
    var questions: List<Question>,
)