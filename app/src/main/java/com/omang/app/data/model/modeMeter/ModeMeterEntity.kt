package com.omang.app.data.model.modeMeter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants


@Entity(tableName = DBConstants.MODE_METER_TABLE)
data class ModeMeterEntity(
    @PrimaryKey(autoGenerate = false)

    @ColumnInfo(name = "id")
    val id: Int?,

    @ColumnInfo(name = "emoji")
    val emoji: String?,

    @ColumnInfo(name = "displayName")
    val displayName: String?,

    @ColumnInfo(name = "systemName")
    val systemName: String?,

    @ColumnInfo(name = "colour")
    val colour: String?

) {
    @Ignore
    var hasInternetConnection: Boolean = true
}
