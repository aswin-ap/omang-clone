package com.omang.app.data.database.myClassroom.relation.lessonWeblinks

import androidx.room.Entity
import com.omang.app.data.database.DBConstants.Companion.LESSON_WEBLINKS_RELATION_TABLE

@Entity(tableName = LESSON_WEBLINKS_RELATION_TABLE, primaryKeys = ["classroomId", "lessonId"])

class LessonWeblinksMappingEntity(
    val classroomId: Int,
    val lessonId: Int,
    val websiteIds: List<Int>,
)

