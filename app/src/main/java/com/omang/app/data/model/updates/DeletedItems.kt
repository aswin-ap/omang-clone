package com.omang.app.data.model.updates

import com.google.gson.annotations.SerializedName


data class DeletedItems(

	@field:SerializedName("details")
	val details: List<DetailsItem>,

	@field:SerializedName("updateType")
	val updateType: Int
)


