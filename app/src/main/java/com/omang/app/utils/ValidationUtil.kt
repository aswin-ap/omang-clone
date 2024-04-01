package com.omang.app.utils

object ValidationUtil {
    fun isNotNullOrEmpty(text: String?) : Boolean{
        return !text.isNullOrEmpty()
    }
}