package com.omang.app.data.repository

import com.omang.app.csdk.CSDKHandler
import javax.inject.Inject

class CSDKRepository @Inject constructor(private val csdkHandler: CSDKHandler) {
    fun getCSDKHandler(): CSDKHandler {
        return csdkHandler
    }
}