package com.omang.app.data.repository

import com.omang.app.utils.socket.SocketManager
import com.omang.app.utils.socket.SocketStatus
import javax.inject.Inject

class SocketRepository @Inject constructor(private val socketManager: SocketManager) {

    fun socketInit() {
        socketManager.init()
    }

    fun socketReconnect() {
        socketManager.socketReconnect()
    }

    fun socketDisconnect() {
        socketManager.socketDisconnect()
    }

    fun statusListener(listener: (SocketStatus) -> Unit) {
        socketManager.statusListener(listener)
    }

    fun joinRoom(roomId: Int, callback: (Boolean) -> Unit) {
        socketManager.joinRoom(roomId, callback)
    }

    fun leaveRoom() {
        socketManager.leaveRoom()
    }

    fun sendMessage(message: String, timestamp: String) {
        socketManager.sendMessage(message, timestamp)
    }

    fun sendBulkStatus(messageId: Int, timestamp: String) {
        socketManager.sendBulkStatus(messageId, timestamp)
    }
}