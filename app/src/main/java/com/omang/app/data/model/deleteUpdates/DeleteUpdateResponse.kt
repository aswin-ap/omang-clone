package com.omang.app.data.model.deleteUpdates

import com.google.gson.annotations.SerializedName

data class DeleteUpdateResponse(

	@field:SerializedName("data")
	val data: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)
