package com.omang.app.data.database.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.LOCATION

@Entity(tableName = LOCATION)
class LocationEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "lat")
    val lat: String,
    @ColumnInfo(name = "long")
    val lon: String,
    @ColumnInfo(name = "time")
    val time: Long = System.currentTimeMillis()
)