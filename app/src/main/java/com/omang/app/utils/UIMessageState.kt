package com.omang.app.utils

import androidx.annotation.StringRes

sealed class UIMessageState {
    class Empty : UIMessageState()
    class StringResourceMessage(val status: Boolean, @StringRes val resId: Int) : UIMessageState()
    class StringMessage(val status: Boolean, val message: String) : UIMessageState()
    class ScreenTransaction(val status: Boolean,  @StringRes val resId: Int) : UIMessageState()

}