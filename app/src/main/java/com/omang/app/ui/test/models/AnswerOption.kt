package com.omang.app.ui.test.models

data class AnswerOption(
    val answerId: String,
    val answer: String,
    val url: String,
    var isSelected: Boolean = false,
)
