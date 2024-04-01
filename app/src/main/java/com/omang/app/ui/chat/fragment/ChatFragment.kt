package com.omang.app.ui.chat.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.omang.app.R
import com.omang.app.data.model.chat.message.MessageResponse
import com.omang.app.ui.chat.compose.ChatScreen
import com.omang.app.ui.chat.compose.widget.ChatUIState
import com.omang.app.ui.chat.viewmodel.ChatViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.FileUtil
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.socket.SocketStatus
import com.omang.app.utils.themes.ChatTheme
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class ChatFragment : Fragment() {
    var TAG = "WebSocket"

    private val viewModel: ChatViewModel by viewModels()

    private var roomId: Int? = null
    private var userId: Int? = null
    private var userPic: String? = null

    private var chatUIState: ChatUIState? = null
    private lateinit var syncDialog: Dialog

    private var isJoined = false
    var receiverFlag = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            roomId = it.getInt("roomId")
            userId = it.getInt("userId")
            val tempUserPic = it.getString("userPic")

            tempUserPic?.let { tempUserPic ->
                userPic =
                    File(
                        FileUtil.getEmojiUrlPath(context),
                        tempUserPic.substring(
                            tempUserPic.lastIndexOf(
                                '/',
                                tempUserPic.lastIndex
                            ) + 1
                        )
                    ).toString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        observer()

        Timber.e("user pic $userPic")
        chatUIState =
            ChatUIState(
                channelName = "YourChannelName",
                channelMembers = 42,
                userId = userId!!,
                userPic = userPic!!,
                initialMessages = emptyList()
            )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                ChatTheme {
                    for (message in chatUIState!!.messages) {
                        Timber.e("chatUIState " + message.messageText)
                    }

                    chatUIState?.let {
                        ChatScreen(
                            it
                        ) { message ->
                            if (requireActivity().hasInternetConnection()) {
                                viewModel.sendMessage(message)
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    "No internet connection to send message!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        syncDialog = Dialog(requireContext(), R.style.mDialogTheme)

    }

    private fun observer() {
        viewModel.isSyncing.observe(viewLifecycleOwner) { state ->
            when (state) {
                NetworkLoadingState.LOADING -> {
                    loadingDialog(syncDialog, true, requireContext())
                }

                NetworkLoadingState.SUCCESS -> {
                    loadingDialog(syncDialog, false, requireContext())

                }

                NetworkLoadingState.ERROR -> {
                    loadingDialog(syncDialog, false, requireContext())
                }
            }
        }

        viewModel.messageResponse.observe(viewLifecycleOwner) {
            Timber.tag(TAG).e("message received in api history")
            if (it.isNotEmpty()) {
                viewModel.sendBulkStatus(it[0].messageId)
                chatUIState?.addAllMessage(it)
            }
        }

        viewModel.socketStatus.observe(viewLifecycleOwner) {
            when (it) {
                SocketStatus.Connected -> {
                    roomId?.let { roomId ->
                        viewModel.joinRoom(roomId) { success ->
                            isJoined = success
                        }
                    }
                }

                SocketStatus.Connecting -> {

                }

                SocketStatus.Disconnected -> {

                }

                is SocketStatus.Error -> {
                    Timber.tag(TAG).e(" Error ${it.errorMessage}")
                }

                is SocketStatus.MessageReceived -> {
                    Timber.tag(TAG).e("message received in observer")
                    viewModel.sendBulkStatus(it.messageResponse.data.messageId)
                    chatUIState?.addMessage(it.messageResponse.data)
                }

                /*              is SocketStatus.SendAck -> {
                                  Timber.tag(TAG).e("send ack in observer : $isJoined")

                                  if (!receiverFlag) {
                                      receiverFlag = true
                                  }

                                  viewModel.sendBulkStatus(it.messageResponse.data.messageId)
                                  chatUIState?.addMessage(it.messageResponse.data)
                              }
              */
                is SocketStatus.JoinedRoom -> {
                    Timber.tag(TAG).e("Joined Room ${it.joinResponse}")
                }

                is SocketStatus.NewNotification -> {
                }
            }
        }
    }


    @Subscribe
    fun messageReceived(messageResponse: MessageResponse) {
        Timber.tag(TAG).e("message received in event bus")
        viewModel.sendBulkStatus(messageResponse.data.messageId)
        chatUIState?.addMessage(messageResponse.data)

    }

    override fun onResume() {
        super.onResume()
        isJoined = false
        viewModel.socketStatus()
        roomId?.let { roomId ->
            viewModel.joinRoom(roomId) { success ->
                isJoined = success
            }
        }
        viewModel.getChatHistory(roomId!!)
    }

    override fun onPause() {
        super.onPause()
        chatUIState?.clear()

    }

    override fun onStop() {
        super.onStop()
        viewModel.leaveRoom()
        isJoined = false
        onDestroyView()
        EventBus.getDefault().unregister(this)
    }

}
