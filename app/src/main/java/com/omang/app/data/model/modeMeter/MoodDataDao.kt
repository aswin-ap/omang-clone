package com.omang.app.data.model.modeMeter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MoodDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClickedData(moodDataEntity: MoodDataEntity)

    @Query("UPDATE mode_data_table SET time = :formattedDateTime WHERE id = :modeId ")
    suspend fun setMoodMeterTime(formattedDateTime: String ,modeId:Int)

    @Query("SELECT * FROM mode_data_table")
    suspend fun getClickedData(): List<MoodDataEntity>

    @Query("DELETE FROM mode_data_table")
    suspend fun clearMoodData()

}