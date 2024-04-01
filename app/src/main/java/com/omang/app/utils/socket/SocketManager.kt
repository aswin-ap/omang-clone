package com.omang.app.utils.socket

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.omang.app.BuildConfig
import com.omang.app.data.model.chat.joinRoom.JoinResponse
import com.omang.app.data.model.chat.message.Message
import com.omang.app.data.model.chat.message.MessageResponse
import com.omang.app.data.model.chat.newNotification.NewNotificationResponse
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.dataStore.SharedPref
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.SocketIOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class SocketManager @Inject constructor(/*lifecycle: Lifecycle,*/ var sharedPref: SharedPref) :
    DefaultLifecycleObserver {

    var TAG = "WebSocket"
    private var socket: Socket? = null

    private val connectionStatusLiveData = MutableLiveData<SocketStatus>()

    // FIXME:  lifecycleowner

    /*    init {
            lifecycle.addObserver(this)
        }*/

    /*    override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            init()
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            socketReconnect()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            socketDisconnect()
        }*/

    fun init() {
        connectionStatusLiveData.value = SocketStatus.Connecting

        Timber.e("socket " + (socket == null))

//        if (socket == null) {

        val token = runBlocking {
            sharedPref.accessToken
        }

        val options = IO.Options()
        options.auth = mapOf("token" to token.toString())
        options.reconnectionDelay = 5000 // sec
        options.reconnectionAttempts = 3 // times
        options.query = "__sails_io_sdk_version=1.2.1"
        options.transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)

        Timber.tag(TAG).v(
            "Base url     : ${
                BuildConfig.BASE_URL.replace(
                    "/api/app/",
                    ""
                ) + "/messaging"
            }"
        )

        Timber.tag(TAG).v("Socket Token : $token")

        socket = IO.socket(
            URI.create(
                BuildConfig.BASE_URL.replace(
                    "/api/app/",
                    ""
                ) + "/messaging"
            ), options
        )

        listenToConnection()
        socket?.connect()

//        }
    }

    fun socketReconnect() {
        connectionStatusLiveData.value = SocketStatus.Connecting
        socket?.connect()
    }

    fun socketDisconnect() {
        socket?.disconnect()
    }

    fun joinRoom(roomId: Int, callback: (Boolean) -> Unit) {
        Timber.tag(TAG).e("join room $roomId")
        val data = JSONObject()
        data.put("roomId", roomId)

        val datas = arrayOf(data)

        socket?.emit("join", datas) { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                Timber.tag(TAG).v("join callback : $response")
                val apiResponse = Gson().fromJson(response.toString(), ApiResponse::class.java)
                callback(apiResponse.data)
            } else {
                callback(false)
            }
        }
    }

    fun leaveRoom() {
        Timber.tag(TAG).e("leaveRoom")

        socket?.emit("leaveRoom", arrayOf()) { args ->
            // Handle the acknowledgment response from the server here
            if (args.isNotEmpty()) {
                // args[0] contains the response data
                val response = args[0] as JSONObject
                // Process the response data

                Timber.tag(TAG).v("leaveRoom $response")

            }
        }
    }

    fun sendMessage(message: String, timestamp: String) {
        Timber.tag(TAG).e("sendMessage ")
        Timber.tag(TAG).e("message : $message ")
        Timber.tag(TAG).e("timestamp : $timestamp ")

        val data = JSONObject()
        data.put("messageText", message)
        data.put("createdOn", timestamp)

        val datas = arrayOf(data)

        socket?.emit("messageCreated", datas) { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                Timber.tag(TAG).v("messageCreated ack $response")

                EventBus.getDefault().post(
                    Gson().fromJson(
                        args[0].toString(),
                        MessageResponse::class.java
                    )
                )

                /*connectionStatusLiveData.postValue(
                    SocketStatus.MessageReceived(
                        Gson().fromJson(
                            args[0].toString(),
                            MessageResponse::class.java
                        )
                    )
                )*/
            }
        }
    }

    fun sendBulkStatus(messageId: Int, timestamp: String) {
        Timber.tag(TAG).e("sendBulkStatus | messageId : $messageId ")

        val data = JSONObject()
        data.put("latestMessageId", messageId)
        data.put("status", "seen")
        data.put("createdOn", timestamp)

        val datas = arrayOf(data)

        socket?.emit("bulkMessageStatusChange", datas) { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                Timber.tag(TAG).v("bulkMessageStatusChange callback $response")
            }
        }
    }

    fun statusListener(listener: (SocketStatus) -> Unit) {
        connectionStatusLiveData.observeForever { status ->
            listener(status)
        }
    }

    private fun listenToConnection() {
        socket?.on(Socket.EVENT_CONNECT) {
            Timber.tag(TAG).v("Connected")
            connectionStatusLiveData.postValue(SocketStatus.Connected)
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            Timber.tag(TAG).e("Disconnected $it")
            connectionStatusLiveData.postValue(SocketStatus.Disconnected)
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            if (args.isNotEmpty()) {
                val error = args[0]
                if (error is SocketIOException) {
                    val errorMessage = error.toString()
                    Timber.tag(TAG).e("Connection Error: $errorMessage")
                    connectionStatusLiveData.postValue(SocketStatus.Error(errorMessage))

                } else {
                    Timber.tag(TAG)
                        .e("Unexpected error type received: ${error.javaClass.simpleName}")
                    // Handle the case when the error type is unexpected
                    connectionStatusLiveData.postValue(SocketStatus.Error("Unexpected error type received"))

                }
            }
        }

        socket?.on("notification") { args ->
            val response = args[0] as JSONObject
            Timber.tag(TAG).v("notification $response")

            connectionStatusLiveData.postValue(
                SocketStatus.NewNotification(
                    Gson().fromJson(
                        args[0].toString(),
                        NewNotificationResponse::class.java
                    )
                )
            )
        }

        socket?.on("joined") { args ->
            connectionStatusLiveData.postValue(
                SocketStatus.JoinedRoom(
                    Gson().fromJson(
                        args[0].toString(),
                        JoinResponse::class.java
                    )
                )
            )
        }

        socket?.on("messageCreated") { args ->
            val response = args[0] as JSONObject
            Timber.tag(TAG).v("messageCreated listener $response")

            EventBus.getDefault().post(
                Gson().fromJson(
                    args[0].toString(),
                    MessageResponse::class.java
                )
            )

            /*    connectionStatusLiveData.postValue(
                    SocketStatus.MessageReceived(
                        Gson().fromJson(
                            args[0].toString(),
                            MessageResponse::class.java
                        )
                    )
                )*/
        }

        socket?.on("bulkMessageStatusChange") { args ->
            val response = args[0] as JSONObject
            Timber.tag(TAG).v("bulkMessageStatusChange listener $response")

        }
    }
}


/*
* reset api response ->
* last or first message id -> */
sealed class SocketStatus {
    object Connecting : SocketStatus()
    object Connected : SocketStatus()
    object Disconnected : SocketStatus()
    data class Error(val errorMessage: String) : SocketStatus()
    data class JoinedRoom(val joinResponse: JoinResponse) : SocketStatus()

    // FIXME:  MessageReceived emmits two time regardless the emit from socket listener and acknowledgement
    data class MessageReceived(val messageResponse: MessageResponse) : SocketStatus()

    //    data class SendAck(val messageResponse: MessageResponse) : SocketStatus()
    data class NewNotification(val newNotificationResponse: NewNotificationResponse) :
        SocketStatus()

}


data class ApiResponse(
    @field:SerializedName("message")
    val message: String,
    @field:SerializedName("data")
    val data: Boolean
)