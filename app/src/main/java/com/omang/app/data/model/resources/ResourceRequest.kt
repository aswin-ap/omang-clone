package com.omang.app.data.model.resources

import com.google.gson.annotations.SerializedName

data class ResourceRequest(

	@field:SerializedName("webPlatforms")
	val webPlatforms: List<Int>? = null,

	@field:SerializedName("resourceId")
	var resourceId: Int? = null,

	@field:SerializedName("books")
	val books: List<Int>? = null,

	@field:SerializedName("videos")
	val videos: List<Int>? = null,

	@field:SerializedName("units")
	val units: List<Int>? = null,

	@field:SerializedName("category")
	var category: String? = null,

	@field:SerializedName("resourceIds")
	var resourceIds: List<Int> = emptyList()
)
