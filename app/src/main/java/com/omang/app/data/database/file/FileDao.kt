package com.omang.app.data.database.file

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileEntity: FileEntity)

    @Query("SELECT * FROM ${DBConstants.FILE_TABLE}")
    suspend fun getAllFiles(): List<FileEntity>

    @Query("DELETE FROM ${DBConstants.FILE_TABLE}")
    suspend fun clear()

}