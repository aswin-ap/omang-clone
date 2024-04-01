package com.omang.app.data.model.modeMeter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeMeterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModeMeterData(modeMeterEntity: List<ModeMeterEntity>)

    @Query("SELECT * FROM mode_meter_table")
     fun getMoodMeterItems(): List<ModeMeterEntity>

    @Query("SELECT COUNT(*) FROM mode_meter_table")
    suspend fun getRowCount():Int


}