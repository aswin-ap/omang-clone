package com.omang.app.data.model.chat.joinRoom

import com.google.gson.annotations.SerializedName

data class JoinResponse(

	@field:SerializedName("clientId")
	val clientId: String,

	@field:SerializedName("joined")
	val joined: String,

	@field:SerializedName("userName")
	val userName: String,

	@field:SerializedName("userId")
	val userId: String
)
