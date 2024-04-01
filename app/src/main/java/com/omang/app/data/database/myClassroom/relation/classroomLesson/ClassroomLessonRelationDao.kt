package com.omang.app.data.database.myClassroom.relation.classroomLesson

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClassroomLessonRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassroomLessonMapping(mappingEntity: List<ClassroomLessonRelationEntity>)
    @Query("DELETE FROM relation_classroom_lesson WHERE classroom_id = :classroomId")
    suspend fun deleteClassroomLessonMapping(classroomId: Int)
    @Query("DELETE FROM relation_classroom_lesson WHERE classroom_id = :classroomId AND lesson_id = :lessonId")
    suspend fun deleteClassroomLessonMapping(classroomId: Int,lessonId: Int)
    @Query("SELECT COUNT(*) FROM relation_classroom_lesson WHERE lesson_id = :lessonId")
    suspend fun hasLessonMapping(lessonId: Int) : Int
    @Query("DELETE FROM relation_classroom_lesson")
    suspend fun clear()
}