package com.omang.app.utils.signal

import kotlinx.coroutines.flow.Flow

interface SignalObserver {

    fun observe(): Flow<Status>

    enum class Status {
        EXCELLENT, GOOD, AVERAGE, WEAK, NOT_AVAILABLE
    }
}