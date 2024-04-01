package com.omang.app.data.database.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(characters: UserEntity)

    @Query("SELECT * FROM USER_TABLE")
    suspend fun getUser(): UserEntity?

    @Query("SELECT firstName FROM USER_TABLE")
    suspend fun getUserFirstName(): String?

    @Query("DELETE FROM USER_TABLE")
    suspend fun clear()
}
