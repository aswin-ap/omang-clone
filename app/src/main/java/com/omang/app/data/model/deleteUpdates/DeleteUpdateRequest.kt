package com.omang.app.data.model.deleteUpdates

import com.google.gson.annotations.SerializedName

data class DeleteUpdateRequest(

	@field:SerializedName("ids")
	val ids: MutableList<String> = ArrayList()
)
