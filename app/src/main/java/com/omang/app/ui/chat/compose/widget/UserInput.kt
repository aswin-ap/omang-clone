package com.omang.app.ui.chat.compose.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omang.app.R
import com.omang.app.utils.themes.Blue30
import com.omang.app.utils.themes.Blue80
import com.omang.app.utils.themes.Blue90
import com.omang.app.utils.themes.Red20
import com.omang.app.utils.themes.Red40
import com.omang.app.utils.themes.Red80
import com.omang.app.utils.themes.bt_emoji_non_selected
import com.omang.app.utils.themes.bt_emoji_selected
import com.omang.app.utils.themes.chat_bg
import com.omang.app.utils.themes.user_input_bg
import timber.log.Timber

enum class InputSelector {
    NONE,
    MAP,
    DM,
    EMOJI,
    PHONE,
    PICTURE
}

enum class EmojiStickerSelector {
    NONE,
    EMOJI,
    STICKER
}

private const val EMOJI_COLUMNS = 10

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    onMessageSent: (String) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
) {
    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    var textState by remember { mutableStateOf(TextFieldValue()) }
    var textFieldFocusState by remember { mutableStateOf(false) }

    Surface(tonalElevation = 2.dp) {
        Column(
            modifier = modifier.background(
                color = user_input_bg
            )
        ) {

            UserInputText(
                textFieldValue = textState,
                onTextChanged = { textState = it },
                keyboardShown = currentInputSelector == InputSelector.NONE && textFieldFocusState,
                onTextFieldFocused = { focused ->
                    if (focused) {
                        currentInputSelector = InputSelector.NONE
                        resetScroll()
                    }
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState
            )


            UserInputSelector(
                onSelectorChange = { currentInputSelector = it },
                sendMessageEnabled = textState.text.isNotBlank(),
                onMessageSent = {
                    onMessageSent(textState.text)
                    // Reset text field and close keyboard
                    textState = TextFieldValue()
                    // Move scroll to bottom
                    resetScroll()
                    dismissKeyboard()
                },
                currentInputSelector = currentInputSelector
            )

            SelectorExpanded(
                onCloseRequested = dismissKeyboard,
                onTextAdded = { textState = textState.addText(it) },
                currentSelector = currentInputSelector
            )
        }
    }
}

@Preview
@Composable
fun UserInputPreview() {
    UserInput(onMessageSent = {})
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean
) {
    val a11ylabel = stringResource(id = R.string.textfield_desc)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 34.dp, max = 200.dp)
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            .semantics {
                contentDescription = a11ylabel
                keyboardShownProperty = keyboardShown
            },
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 34.dp, max = 200.dp)
                .weight(1f)
                .align(Alignment.Bottom)
                .border(1.dp, bt_emoji_non_selected, RoundedCornerShape(17.dp))
        ) {
            var lastFocusState by remember { mutableStateOf(false) }
            BasicTextField(
                value = textFieldValue,
                onValueChange = { onTextChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp)
                    .align(Alignment.CenterStart)
                    .onFocusChanged { state ->
                        if (lastFocusState != state.isFocused) {
                            onTextFieldFocused(state.isFocused)
                        }
                        lastFocusState = state.isFocused
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Default
                ),
                maxLines = 7,
                cursorBrush = SolidColor(bt_emoji_non_selected),
                textStyle = LocalTextStyle.current.copy(color = bt_emoji_non_selected)
            )

            if (textFieldValue.text.isEmpty() && !focusState) {
                Text(
                    text = stringResource(id = R.string.textfield_hint),
                    style = TextStyle.Default.copy(color = bt_emoji_non_selected),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun UserInputSelector(
    onSelectorChange: (InputSelector) -> Unit,
    sendMessageEnabled: Boolean,
    onMessageSent: () -> Unit,
    currentInputSelector: InputSelector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(72.dp)
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputSelectorButton(
            onClick = {
                if (currentInputSelector == InputSelector.EMOJI) {
                    onSelectorChange(InputSelector.NONE)
                } else {
                    onSelectorChange(InputSelector.EMOJI)
                }
            },
            icon = R.drawable.ic_emoji,
            selected = currentInputSelector == InputSelector.EMOJI,
            description = stringResource(id = R.string.emoji_selector_bt_desc)
        )

        InputSelectorButton(
            onClick = {
                if (currentInputSelector == InputSelector.PICTURE) {
                    onSelectorChange(InputSelector.NONE)
                } else {
                    onSelectorChange(InputSelector.PICTURE)
                }
            },
            icon = R.drawable.ic_image,
            selected = currentInputSelector == InputSelector.PICTURE,
            description = stringResource(id = R.string.attach_photo_desc)
        )

        Spacer(modifier = Modifier.weight(1f))

        val border = if (!sendMessageEnabled) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        } else {
            BorderStroke(
                width = 1.dp,
                color = Blue90
            )
        }

        val buttonColors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Blue90,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        // Send button
        Button(
            modifier = Modifier.height(36.dp),
            enabled = sendMessageEnabled,
            onClick = onMessageSent,
            colors = buttonColors,
            border = border,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                stringResource(id = R.string.send),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun InputSelectorButton(
    onClick: () -> Unit,
    icon: Int,
    description: String,
    selected: Boolean
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            color = Blue90,
            shape = RoundedCornerShape(14.dp)
        )
    } else {
        Modifier
    }
    IconButton(
        onClick = onClick,
        modifier = backgroundModifier
    ) {
        val tint = if (selected) {
            bt_emoji_selected

        } else {
            bt_emoji_non_selected

        }
        Icon(
            painterResource(id = icon),
            tint = tint,
            contentDescription = description, modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun SelectorExpanded(
    currentSelector: InputSelector,
    onCloseRequested: () -> Unit,
    onTextAdded: (String) -> Unit
) {
    if (currentSelector == InputSelector.NONE) return

    // Request focus to force the TextField to lose it
    val focusRequester = FocusRequester()
    // If the selector is shown, always request focus to trigger a TextField.onFocusChange.
    SideEffect {
        if (currentSelector == InputSelector.EMOJI) {
            focusRequester.requestFocus()
        }
    }

    Surface(tonalElevation = 8.dp) {
        when (currentSelector) {
            InputSelector.EMOJI -> EmojiSelector(onTextAdded, focusRequester)
//            InputSelector.DM -> NotAvailablePopup(onCloseRequested)
            InputSelector.PICTURE -> FunctionalityNotAvailablePanel()
//            InputSelector.MAP -> FunctionalityNotAvailablePanel()
//            InputSelector.PHONE -> FunctionalityNotAvailablePanel()
            else -> {
                throw NotImplementedError()
            }
        }
    }
}

@Composable
fun EmojiSelector(
    onTextAdded: (String) -> Unit,
    focusRequester: FocusRequester
) {
    var selected by remember { mutableStateOf(EmojiStickerSelector.EMOJI) }
    val a11yLabel = stringResource(id = R.string.emoji_selector_desc)

    Column(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusTarget()
            .semantics { contentDescription = a11yLabel }
    ) {

        // Tab buttons
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExtendedSelectorInnerButton(
                text = stringResource(id = R.string.emojis_label),
                onClick = {
                    Timber.e("c;")
                    selected = EmojiStickerSelector.EMOJI
                },
                selected = selected != EmojiStickerSelector.EMOJI,
                modifier = Modifier.weight(1f)
            )
            ExtendedSelectorInnerButton(
                text = stringResource(id = R.string.stickers_label),
                onClick = {
                    selected = EmojiStickerSelector.STICKER
                    Timber.e("clicked")
                },
                selected = selected != EmojiStickerSelector.STICKER,
                modifier = Modifier.weight(1f)
            )
        }

        // Content based on selected tab
        when (selected) {
            EmojiStickerSelector.EMOJI -> {
                EmojiTable(onTextAdded, modifier = Modifier.padding(8.dp))
            }

            EmojiStickerSelector.STICKER -> {
                FunctionalityNotAvailablePanel()
            }

            EmojiStickerSelector.NONE -> {

            }
        }
    }
}

@Composable
fun ExtendedSelectorInnerButton(
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = ButtonDefaults.buttonColors(
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        containerColor = Color.Transparent,
        disabledContentColor = MaterialTheme.colorScheme.onSurface,
        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
    )
    TextButton(
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .height(36.dp),
        enabled = selected,
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        repeat(4) { x ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(EMOJI_COLUMNS) { y ->
                    val emoji = emojis[x * EMOJI_COLUMNS + y]
                    Text(
                        modifier = Modifier
                            .clickable(onClick = { onTextAdded(emoji) })
                            .sizeIn(minWidth = 42.dp, minHeight = 42.dp)
                            .padding(8.dp),
                        text = emoji,
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NotAvailablePopup(onDismissed: () -> Unit) {
    FunctionalityNotAvailablePopup(onDismissed)
}

@Composable
fun FunctionalityNotAvailablePanel() {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
        enter = expandHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .height(320.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.not_available),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.not_available_subtitle),
                modifier = Modifier.paddingFrom(FirstBaseline, before = 32.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun TextFieldValue.addText(newString: String): TextFieldValue {
    val newText = this.text.replaceRange(
        this.selection.start,
        this.selection.end,
        newString
    )
    val newSelection = TextRange(
        start = newText.length,
        end = newText.length
    )

    return this.copy(text = newText, selection = newSelection)
}
