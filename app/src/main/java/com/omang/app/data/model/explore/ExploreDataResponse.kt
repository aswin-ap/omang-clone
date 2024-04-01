package com.omang.app.data.model.explore

import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.model.summary.MyWebPlatformItem

class ExploreDataResponse(
    @field:SerializedName("statusCode")
    val statusCode: Int,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: ExploreDataItem?,

    )

data class ExploreDataItem(

    @field:SerializedName("added")
    val added: List<Added>,

    @field:SerializedName("deleted")
    val deleted: List<Int>,

    )

data class Added(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("url")
    val url: String,

    @field:SerializedName("alternateUrls")
    val alternateUrls: List<String>,

    @field:SerializedName("userAgent")
    val userAgent: Any?,

    @field:SerializedName("threshold")
    val threshold: Any?,

    @field:SerializedName("logo")
    val logo: String,

    )



fun MyWebPlatformItem.asEntity(): MyWebPlatformEntity = MyWebPlatformEntity(
    id = this.id,
    name = this.name,
    logo = this.logo,
    url = null,
    altUrl = null,
    timeStamp = null,
    type = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value
)

fun MyWebPlatformItem.asExploreEntity(): MyWebPlatformEntity = MyWebPlatformEntity(
    id = this.id,
    name = this.name,
    logo = this.logo,
    url = null,
    altUrl = null,
    timeStamp = null,
    type = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value
)


