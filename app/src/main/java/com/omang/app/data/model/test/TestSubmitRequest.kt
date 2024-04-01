package com.omang.app.data.model.test

import com.google.gson.annotations.SerializedName

data class TestSubmitRequest(
    @field:SerializedName("mcqs")
    var mcqs: List<Mcq> = arrayListOf()
)

data class Mcq(
    @field:SerializedName("id")
    var id: Int? = null,

   @field:SerializedName("mcqStudId")
    var mcqStudId: Int? = null,

    @field:SerializedName("classroom")
    var classroom: Int? = null,

    @field:SerializedName("unit")
    var unit: Int? = null,

    @field:SerializedName("score")
    var score: Int? = null,

    @field:SerializedName("attemptsCount")
    var attemptsCount: Int? = null,

    @field:SerializedName("correctAttempts")
    var correctAttempts: Int? = null,

    @field:SerializedName("wrongAttempts")
    var wrongAttempts: Int? = null,

    @field:SerializedName("attendedOn")
    var attendedOn: String? = null,

    @field:SerializedName("questions")
    var questions: List<AttemptedQuestion> = arrayListOf()
)

data class AttemptedQuestion(
    @field:SerializedName("question")
    var question: Int? = null,

    @field:SerializedName("selectedOptions")
    var selectedOptions: List<Int> = arrayListOf(),

    @field:SerializedName("score")
    var score: Int? = null,

    @field:SerializedName("answer")
    var answer: String? = null
)