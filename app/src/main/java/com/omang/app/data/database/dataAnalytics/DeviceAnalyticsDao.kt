package com.omang.app.data.database.dataAnalytics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants
import timber.log.Timber

@Dao
interface DeviceAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analyticsEntity: AnalyticsEntity)

    @Query("SELECT * FROM ${DBConstants.DATA_ANALYTICS}")
    suspend fun getAllAnalytics(): List<AnalyticsEntity>

    @Query("DELETE FROM ${DBConstants.DATA_ANALYTICS}")
    suspend fun clear()

}
