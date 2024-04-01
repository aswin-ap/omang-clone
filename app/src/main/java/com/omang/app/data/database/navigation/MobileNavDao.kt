package com.omang.app.data.database.navigation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants

@Dao
interface MobileNavDao {
    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insert(mobileNavigationEntity: MobileNavigationEntity)

    @Query("SELECT * FROM ${DBConstants.MOBILE_NAV_TABLE}")
    suspend fun getAllNavigation(): List<MobileNavigationEntity>

    @Query("DELETE FROM ${DBConstants.MOBILE_NAV_TABLE}")
    suspend fun deleteAll()

    @Query("SELECT * FROM ${DBConstants.MOBILE_NAV_TABLE} ORDER BY id limit 10")
    suspend fun getLastTenNavigation(): List<MobileNavigationEntity>
    @Query("DELETE FROM mobile_navigation_table")
    suspend fun clear()
}