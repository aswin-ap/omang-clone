package com.omang.app.data.database.myClassroom.unit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(unitEntities: List<UnitEntity>)

    @Query("SELECT * FROM unit_table WHERE unit_id = :unitId")
    suspend fun getUnitById(unitId: Int): UnitEntity?

    @Query("DELETE FROM unit_table")
    suspend fun clear()

}