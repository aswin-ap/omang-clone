package com.omang.app.ui.listeners

interface ClickListener<T> {
    fun <T> cardClicked(item: T)
}
