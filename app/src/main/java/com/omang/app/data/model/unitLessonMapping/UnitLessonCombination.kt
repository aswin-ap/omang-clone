package com.omang.app.data.model.unitLessonMapping

import androidx.room.ColumnInfo

data class UnitLessonCombination(
    @ColumnInfo(name = "unit_id")
    val unitId: Int,

    @ColumnInfo(name = "lesson_id")
    val lessonId: Int
)