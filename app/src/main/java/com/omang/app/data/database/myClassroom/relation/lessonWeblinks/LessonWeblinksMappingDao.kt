package com.omang.app.data.database.myClassroom.relation.lessonWeblinks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity

@Dao
interface LessonWeblinksMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonWeblinksMapping(lessonWeblinksMappingEntities: List<LessonWeblinksMappingEntity>)

    @Query("SELECT * FROM relation_lesson_weblinks WHERE classroomId = :classroomId AND lessonId = :lessonId")
    suspend fun getLessonWeblinksMapping(classroomId: Int, lessonId: Int): LessonWeblinksMappingEntity?
    @Query("DELETE FROM relation_lesson_weblinks WHERE classroomId = :classroomId")
    suspend fun deleteClassroomLessonWebLinkMapping(classroomId: Int)
    @Query("DELETE FROM relation_lesson_weblinks WHERE classroomId = :classroomId AND lessonId = :lessonId")
    suspend fun deleteClassroomLessonWebLinkMapping(classroomId: Int, lessonId: Int)
    @Query("DELETE FROM relation_lesson_weblinks")
    suspend fun clear()
}