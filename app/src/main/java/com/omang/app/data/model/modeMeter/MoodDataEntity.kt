package com.omang.app.data.model.modeMeter

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = DBConstants.MODE_DATA_TABLE
)

@Parcelize
data class MoodDataEntity(

    @PrimaryKey
    @ColumnInfo(name = "columnId")
    val columnId: String,

    @ColumnInfo(name = "id")
    val id: Int?,

    @ColumnInfo(name = "time")
    val time: String?,

    @ColumnInfo(name = "displayName")
    val displayName: String?,

    ) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    var hasInternetConnection: Boolean = true
}

