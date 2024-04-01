package com.omang.app.data.database.myClassroom.relation.unitLesson

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.omang.app.data.database.DBConstants.Companion.UNIT_LESSON_RELATION_TABLE
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.myClassroom.unit.UnitEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity

@Entity(
    tableName = UNIT_LESSON_RELATION_TABLE,
    primaryKeys = [
        "classroom_id",
        "unit_id",
        "lesson_id"
    ],
    foreignKeys = [
        ForeignKey(
            entity = MyClassroomEntity::class,
            parentColumns = ["classroom_id"],
            childColumns = ["classroom_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["unit_id"],
            childColumns = ["unit_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = LessonsEntity::class,
            parentColumns = ["lesson_id"],
            childColumns = ["lesson_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
class UnitLessonRelationEntity(
    @ColumnInfo(name = "classroom_id")
    val classroomId: Int,

    @ColumnInfo(name = "unit_id")
    val unitId: Int,

    @ColumnInfo(name = "lesson_id")
    val lessonId: Int,
)