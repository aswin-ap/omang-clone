package com.omang.app.data.model.resources

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class WebsiteResourceResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)

@Parcelize
data class DataItem(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("logo")
	val logo: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("alternateUrls")
	val alternateUrls: List<String>? = null,

	@field:SerializedName("url")
	val url: String
) : Parcelable
