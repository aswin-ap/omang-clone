package com.omang.app.data.model.unitWithLessons

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.omang.app.data.database.myClassroom.relation.unitLesson.UnitLessonRelationEntity
import com.omang.app.data.database.myClassroom.unit.UnitEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity

data class UnitWithLessons(
    @Embedded
    val unit: UnitEntity,

    @Relation(
        parentColumn = "unit_id",
        entityColumn = "lesson_id",
        associateBy = Junction(
            UnitLessonRelationEntity::class,
            parentColumn = "unit_id",
            entityColumn = "lesson_id"
        )
    )
    val lessons: List<LessonsEntity>

)