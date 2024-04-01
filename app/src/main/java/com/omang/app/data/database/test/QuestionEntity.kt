package com.omang.app.data.database.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.QUESTIONS_TABLE
import com.omang.app.data.model.resources.OptionItem

//Entity to save the MCQ tests
@Entity(tableName = QUESTIONS_TABLE)
class QuestionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "questionType")
    val questionType: Int,

    @ColumnInfo(name = "questionUrl")
    val questionUrl: String?,

    @ColumnInfo(name = "options")
    val options: List<OptionItem>,
) {
    @Ignore
    var questionNUmber: Int = 0

    @Ignore
    var isMultiQuestion: Boolean = false

    @Ignore
    val selectedOptions = mutableListOf<OptionItem>()
}