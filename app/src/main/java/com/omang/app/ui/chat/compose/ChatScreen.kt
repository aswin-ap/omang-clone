package com.omang.app.ui.chat.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.omang.app.ui.chat.compose.widget.ChatUIState
import com.omang.app.ui.chat.compose.widget.Messages
import com.omang.app.ui.chat.compose.widget.UserInput
import com.omang.app.utils.themes.chat_bg
import com.omang.app.utils.themes.user_input_bg
import timber.log.Timber


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    uiState: ChatUIState,
    onMessageSent: (String) -> Unit
) {
    var textState = remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberLazyListState()

    Surface(modifier = Modifier.navigationBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = chat_bg)
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = 60.dp,
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Timber.e("size in Messages ${uiState.messages.size}")

                    Messages(
                        messages = uiState.messages,
                        userId = uiState.userId,
                        userPic = uiState.userPic,
//                        navigateToProfile = navigateToProfile,
                        modifier = Modifier.weight(1f),
                        scrollState = scrollState
                    )
                }
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .background(
                            color = user_input_bg, // Change the background color as needed
                            shape = RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp
                            ) // Adjust the corner radii as needed
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    UserInput(
                        onMessageSent = {
                            onMessageSent(it)

                        },
                        resetScroll = {

                        },
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()

                    )
                }
            }
        }
    }
}
