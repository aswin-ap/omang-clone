package com.omang.app.data.database.test

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questionEntity: List<QuestionEntity>)

    @Query("SELECT * FROM questions_table WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Int): QuestionEntity

    @Query("SELECT * FROM questions_table WHERE id IN (:idList)")
    suspend fun getQuestionsById(idList: List<Int>): List<QuestionEntity>

    @Query("DELETE FROM questions_table")
    suspend fun clear()


}
