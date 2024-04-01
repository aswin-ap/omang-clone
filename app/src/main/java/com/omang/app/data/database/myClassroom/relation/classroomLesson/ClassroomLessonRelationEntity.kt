package com.omang.app.data.database.myClassroom.relation.classroomLesson

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.omang.app.data.database.DBConstants.Companion.CLASSROOM_LESSON_RELATION_TABLE
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity

@Entity(
    tableName = CLASSROOM_LESSON_RELATION_TABLE,
    primaryKeys = ["classroom_id", "lesson_id"],
    foreignKeys = [
        ForeignKey(
            entity = MyClassroomEntity::class,
            parentColumns = ["classroom_id"],
            childColumns = ["classroom_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = LessonsEntity::class,
            parentColumns = ["lesson_id"],
            childColumns = ["lesson_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
class ClassroomLessonRelationEntity(
    @ColumnInfo(name = "classroom_id")
    val classroomId: Int,
    @ColumnInfo(name = "lesson_id")
    val lessonId: Int
)