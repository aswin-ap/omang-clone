package com.omang.app.data.model.analytics

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnalyticsData(
    @field:SerializedName("analytics")
    val analytics: List<AnalyticItem>,
) : Serializable