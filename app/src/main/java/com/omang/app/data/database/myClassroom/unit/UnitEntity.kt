package com.omang.app.data.database.myClassroom.unit

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.DBConstants.Companion.UNIT_TABLE
import com.omang.app.data.ui.DownloadStatus

@Entity(tableName = UNIT_TABLE)
class UnitEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unit_id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "objective")
    val objective: String,

) {
    @Ignore
    var isExpanded: Boolean = false

    @Ignore
    var progress: Int = 0

    @Ignore
    var downloadStatus: DownloadStatus = DownloadStatus.NOT_START

    @Ignore
    var totalFiles: Int = 0

    @Ignore
    var downloadedFiles: Int = 0

    @Ignore
    var hasInternetConnection = true

/*    @Ignore
    var isTestPassed = false   */

    @Ignore
    var isTestPassed = DBConstants.TestStatus.NOT_ATTEMPTED
}