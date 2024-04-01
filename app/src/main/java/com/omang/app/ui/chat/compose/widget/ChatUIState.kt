package com.omang.app.ui.chat.compose.widget

import androidx.compose.runtime.mutableStateListOf
import com.omang.app.data.model.chat.message.Message
import com.omang.app.data.model.chat.message.MessageResponse
import com.omang.app.data.model.chat.message.User
import timber.log.Timber

class ChatUIState(
    val channelName: String,
    val channelMembers: Int,
    val userId: Int,
    val userPic: String,
    initialMessages: List<Message>
) {
    private val _messages: MutableList<Message> =
        mutableStateListOf(*initialMessages.toTypedArray())
    var messages: List<Message> = _messages
    var TAG = "WebSocket"

    fun clear() {
        _messages.clear()
        Timber.tag(TAG).e("size --> %s", messages.size)
        messages = emptyList()
    }

    fun addMessage(msg: Message) {
        Timber.tag(TAG).e("message received " + msg.messageText)
        _messages.add(0, msg)
    }

    fun addAllMessage(messages: List<Message>) {
        _messages.clear()
        _messages.addAll(messages)

    }
}
