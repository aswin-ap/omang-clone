package com.omang.app.ui.chat.compose.widget

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.omang.app.R
import com.omang.app.data.model.chat.message.Message
import com.omang.app.utils.ViewUtil.getUtcTimeWithMSec
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertTimestampToLocale
import com.omang.app.utils.extensions.isToday
import com.omang.app.utils.themes.DarkBlue80
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

private val JumpToBottomThreshold = 56.dp
private val ChatBubbleShapeSend = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
private val ChatBubbleShapeReceive = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

@Composable
fun Messages(
    messages: List<Message>,
//    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    userId: Int,
    userPic: String
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        val todayDate = getUtcTimeWithMSec()

        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {

            for (message in messages) {
                Timber.e("message --> " + message.messageText)
            }

            val groupedMessages = messages.groupBy {
                convertTimestampToLocale(it.createdOn, DateTimeFormat.DATE)
            }

            groupedMessages.forEach { (msgDate, messages) ->
                Timber.e("message --> date " + msgDate)

                messages.forEachIndexed { index, message ->
                    val prevAuthor = messages.getOrNull(index - 1)?.user?.id
                    val nextAuthor = messages.getOrNull(index + 1)?.user?.id
                    val isFirstMessageByAuthor = prevAuthor != message.user.id
                    val isLastMessageByAuthor = nextAuthor != message.user.id

                    item {
                        Message(
                            msg = message,
                            isUserMe = message.user.id == userId,
                            userPic = userPic,
                            isFirstMessageByAuthor = isFirstMessageByAuthor,
                            isLastMessageByAuthor = isLastMessageByAuthor
                        )
                    }
                }

                item {
                    DayHeader(
                        if (isToday(msgDate, todayDate)) {
                            "Today"
                        } else {
                            convertTimestampToLocale(msgDate, DateTimeFormat.MONTH_N_DATE)
                        }
                    )
                }
            }
        }

        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun DayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    Divider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
fun Message(
//    onAuthorClick: (Int) -> Unit,
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    userPic: String
) {
    val borderColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(modifier = spaceBetweenAuthors) {

        if (isUserMe) {
            val alignment = Alignment.End
            ReceiverUI(
                msg = msg,
                isUserMe = isUserMe,
                isFirstMessageByAuthor = isFirstMessageByAuthor,
                isLastMessageByAuthor = isLastMessageByAuthor,
//            authorClicked = onAuthorClick,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                alignment
            )

            val imgBitmap = BitmapFactory.decodeFile(userPic)

            if (isLastMessageByAuthor) {
                // Avatar
                Image(
                    modifier = Modifier
//                    .clickable(onClick = { onAuthorClick(msg.data.) })
                        .padding(horizontal = 16.dp)
                        .size(42.dp)
                        .border(1.5.dp, borderColor, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .clip(CircleShape)
                        .align(Alignment.Top),
                    painter = rememberAsyncImagePainter(imgBitmap),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            } else {
                // Space under avatar
                Spacer(modifier = Modifier.width(74.dp))
            }

        } else {
            val alignment = Alignment.Start
            if (isLastMessageByAuthor) {
                // Avatar
                Image(
                    modifier = Modifier
//                    .clickable(onClick = { onAuthorClick(msg.data.) })
                        .padding(horizontal = 16.dp)
                        .size(42.dp)
                        .border(1.5.dp, borderColor, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .clip(CircleShape)
                        .align(Alignment.Top),
                    painter = painterResource(id = R.drawable.tech_ammachi_passport_size),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            } else {
                // Space under avatar
                Spacer(modifier = Modifier.width(74.dp))
            }

            ReceiverUI(
                msg = msg,
                isUserMe = isUserMe,
                isFirstMessageByAuthor = isFirstMessageByAuthor,
                isLastMessageByAuthor = isLastMessageByAuthor,
//            authorClicked = onAuthorClick,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .weight(1f),
                alignment
            )
        }
    }
}

@Composable
fun ReceiverUI(
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
//    authorClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        if (isLastMessageByAuthor) {
            AuthorNameTimestamp(msg, isUserMe)
        }
        ChatItemBubble(msg, isUserMe/*, authorClicked = authorClicked*/)
        if (isFirstMessageByAuthor) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


@Composable
private fun AuthorNameTimestamp(msg: Message, isUserMe: Boolean) {
    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        if (isUserMe) {
            Text(
                text = formatTimestampToTime(msg.createdOn),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alignBy(LastBaseline),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = msg.user.firstName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .alignBy(LastBaseline)
                    .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
            )
        } else {
//            Timber.e("crash ${msg.messageId} ${msg.user.firstName}")
            Text(
                text = msg.user.firstName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .alignBy(LastBaseline)
                    .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatTimestampToTime(msg.createdOn),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alignBy(LastBaseline),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}


@Composable
fun ChatItemBubble(
    message: Message,
    isUserMe: Boolean,
//    authorClicked: (String) -> Unit
) {

    val alignment = if (isUserMe) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val backgroundBubbleColor = if (isUserMe) {
        DarkBlue80
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = backgroundBubbleColor,
            shape = if (isUserMe) ChatBubbleShapeSend else ChatBubbleShapeReceive
        ) {
            ClickableMessage(
                message = message,
                isUserMe = isUserMe,
//                authorClicked = authorClicked
            )
        }

        // below is for showing the sticker in chat item bubble
        /*     message.data.user.avatar.let {
                 Spacer(modifier = Modifier.height(4.dp))
                 Surface(
                     color = backgroundBubbleColor,
                     shape = ChatBubbleShape
                 ) {
                     Image(
                         painter = rememberAsyncImagePainter(message.data.user.avatar),
                         contentScale = ContentScale.Fit,
                         modifier = Modifier.size(160.dp),
                         contentDescription = stringResource(id = R.string.attached_image)
                     )
                 }
             }*/
    }
}

@Composable
fun ClickableMessage(
    message: Message,
    isUserMe: Boolean,
//    authorClicked: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = message.messageText,
        primary = isUserMe
    )

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        modifier = Modifier.padding(16.dp),
        onClick = {
            styledMessage
                .getStringAnnotations(start = it, end = it)
                .firstOrNull()
                ?.let { annotation ->
                    /* when (annotation.tag) {
                         SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                         SymbolAnnotationType.PERSON.name -> authorClicked(annotation.item)
                         else -> Unit
                     }*/
                }
        }
    )
}
