package com.omang.app.data.model.test


import com.google.gson.annotations.SerializedName
import com.omang.app.data.model.resources.OptionItem
import com.omang.app.data.model.resources.QuestionItem

data class McqResponse(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class Data(
        @SerializedName("attendedMCQs")
        val attendedMCQs: List<AttendedMCQ>,
        @SerializedName("expiredMCQs")
        val expiredMCQs: List<ExpiredMCQ>,
        @SerializedName("notAttendedMCQs")
        val notAttendedMCQs: List<NotAttendedMCQ>
    )

    data class AttendedMCQ(
        @SerializedName("attemptsCount")
        val attemptsCount: Int,
        @SerializedName("attendedOn")
        val attendedOn: String,
        @SerializedName("classroomId")
        val classroomId: Int?,
        @SerializedName("classroomName")
        val classroomName: String,
        @SerializedName("correctAttempts")
        val correctAttempts: Int,
        @SerializedName("endTime")
        val endTime: String?,
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("parentId")
        val parentId: Int,
        @SerializedName("questions")
        val questions: List<Question>,
        @SerializedName("scoreObtained")
        val scoreObtained: Int,
        @SerializedName("startTime")
        val startTime: String?,
        @SerializedName("unitId")
        val unitId: Int,
        @SerializedName("unitName")
        val unitName: String,
        @SerializedName("wrongAttempts")
        val wrongAttempts: Int
    )

    data class Question(
        @SerializedName("answer")
        val answer: String?,
        @SerializedName("id")
        val id: Int,
        @SerializedName("options")
        val options: List<Option>,
        @SerializedName("question")
        val question: String,
        @SerializedName("questionType")
        val questionType: Int,
        @SerializedName("questionURL")
        val questionURL: String?,
        @SerializedName("score")
        val score: Int,
        @SerializedName("scoreObtained")
        val scoreObtained: Int
    )

    data class Option(
        @SerializedName("answered")
        val answered: Boolean,
        @SerializedName("id")
        val id: Int,
        @SerializedName("isAnswer")
        val isAnswer: Boolean,
        @SerializedName("option")
        val option: String,
        @SerializedName("optionURL")
        val optionURL: String?
    )


    data class ExpiredMCQ(
        @SerializedName("expiredOn")
        val expiredOn: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String
    )

    data class NotAttendedMCQ(
        @SerializedName("endTime")
        val endTime: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("mcqStudId")
        val mcqStudId: Int,
        @SerializedName("instructions")
        val instructions: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("parentId")
        val parentId: Any,
        @SerializedName("questions")
        val questions: List<QuestionItem>,
        @SerializedName("startTime")
        val startTime: String
    )
}

fun List<McqResponse.Question>.toAttemptedQuestionList() = map {
    AttemptedQuestion(
        question = it.id,
        score = it.score,
        selectedOptions = it.options.filter { opt -> opt.answered }.map { opt -> opt.id }
    )
}

fun List<McqResponse.Option>.asEntityOptions() = map {
    OptionItem(
        id = it.id,
        option = it.option,
        optionUrl = it.optionURL,
        isAnswer = it.isAnswer,
        answered = it.answered
    )
}