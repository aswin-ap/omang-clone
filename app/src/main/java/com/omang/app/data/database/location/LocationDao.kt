package com.omang.app.data.database.location

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationEntity: LocationEntity)

    @Query("DELETE FROM app_location")
    suspend fun clear()

}