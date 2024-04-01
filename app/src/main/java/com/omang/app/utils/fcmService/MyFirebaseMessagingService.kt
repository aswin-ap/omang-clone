package com.omang.app.utils.fcmService

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.omang.app.data.model.fcm.Payload
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.dataStore.DataStoreKeys.FCM_TOKEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dataManagerRepository: DataManagerRepository

    override fun onNewToken(token: String) {
        val job = Job()
        CoroutineScope(job + Dispatchers.Main).launch {
            dataManagerRepository.saveToDataStore(FCM_TOKEN, token)

        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.v("onMessageReceived ${message.data}")

        val payload: String? = message.data["payload"]
        val contentBody: String? = message.data["body"]
        val contentTitle: String? = message.data["title"]
        val classroom: Int? = message.data["classroom"]?.toInt()
        val gson = Gson()
        val payloadData: Payload = gson.fromJson(payload, Payload::class.java).also { payLoad ->
            contentTitle?.let {
                payLoad.title = it

            }
            contentBody?.let { it ->
                payLoad.body = it
            }
            classroom?.let {
                payLoad.classroom = classroom
            }
        }

        Timber.e("payload : update type --> " + payloadData.updateType)
        EventBus.getDefault().post(payloadData)

    }
}

