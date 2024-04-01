package com.omang.app.utils

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.lang.Exception

object FcmTokenFetcher {
    private var fcmToken: String? = null

    suspend fun fetchFcmToken(): String {
        if (fcmToken.isNullOrBlank()) {
            fcmToken = try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                // Handle the exception
                e.message
            }
        }
        return fcmToken.orEmpty()
    }
}