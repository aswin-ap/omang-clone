package com.omang.app.ui.admin.model

import com.google.gson.annotations.SerializedName
import com.omang.app.data.model.appupdate.AppUpdateResponseData

data class DiagnosisResponse(
    @SerializedName("statusCode")
    val statusCode: Int?,
    @SerializedName("message")
    val message: String?,
)

