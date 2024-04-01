package com.omang.app.ui.gallery.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaFile(
    val urlPath: String,
    val name: String,
    val createdAt: Long,
    var isSelected: Boolean = false,
    val isDeviceFile: Boolean = false
) : Parcelable