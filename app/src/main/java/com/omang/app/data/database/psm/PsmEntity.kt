package com.omang.app.data.database.psm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.MOBILE_NAV_TABLE
import com.omang.app.data.database.DBConstants.Companion.PSM_TABLE
import com.omang.app.utils.DeviceUtil
import java.sql.Date

@Entity(tableName = PSM_TABLE)
class PsmEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo("psmId")
    val psmId: Int = 0,

    @ColumnInfo(name = "psmResponse")
    val psmResponse: String,
)