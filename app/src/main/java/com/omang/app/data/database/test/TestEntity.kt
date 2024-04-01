package com.omang.app.data.database.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.TEST_TABLE
import com.omang.app.data.model.test.AttemptedQuestion

//Entity to save the MCQ tests
@Entity(tableName = TEST_TABLE)
class TestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "generalMcqId")
    val generalMcqId: Int? = null,

    @ColumnInfo(name = "classroomId")
    var classroomId: Int? = null,

    @ColumnInfo(name = "unitId")
    var unitId: Int? = null,

    @ColumnInfo(name = "startTime")
    val startTime: String,

    @ColumnInfo(name = "endTime")
    val endTime: String,

    @ColumnInfo(name = "createdOn")
    var createdOn: String,

    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "instructions")
    val instructions: String,

    @ColumnInfo(name = "mcq_type")
    val mcqType: Int, // UNIT/ MCQ

    @ColumnInfo(name = "type")
    var type: Int? = null, // NEW,ATTEMPTED,EXPIRED

    @ColumnInfo(name = "question_id")
    val questionsId: List<Int>,

    @ColumnInfo(name = "score")
    var score: Float = 0f,

    @ColumnInfo(name = "percentage")
    var percentage: Float = 0f,

    @ColumnInfo(name = "attemptsCount")
    var attemptsCount: Int = 0,

    @ColumnInfo(name = "correctAttempts")
    var correctAttempts: Int = 0,

    @ColumnInfo(name = "wrongAttempts")
    var wrongAttempts: Int = 0,

    @ColumnInfo(name = "isSentToApi")
    var isSentToApi: Int,

    @ColumnInfo(name = "questions")
    var questions: List<AttemptedQuestion> = listOf(),
) {
    @Ignore
    var isOneTestRemains : Boolean = false
}