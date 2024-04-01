package com.omang.app.data.model.token

import com.google.gson.annotations.SerializedName

data class AccessToken(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)

data class Data(

	@field:SerializedName("accessToken")
	val accessToken: String? = null
)
