package com.omang.app.data.database.myClassroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContentContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contentCountEntity: ContentCountEntity)

    @Query("DELETE FROM contentcountentity")
    suspend fun clear()
}