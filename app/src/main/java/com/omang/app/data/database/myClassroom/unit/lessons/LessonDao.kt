package com.omang.app.data.database.myClassroom.unit.lessons

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessonsEntity: List<LessonsEntity>)

    @Query("SELECT * FROM lesson_table WHERE lesson_id = :lessonId")
    suspend fun getLessonById(lessonId: Int): LessonsEntity?

    @Query("DELETE FROM lesson_table")
    suspend fun clear()

    @Query("UPDATE lesson_table SET lesson_rate = :rating WHERE lesson_id = :lessonId")
    suspend fun updateLessonRating(lessonId: Int, rating: Float)

}