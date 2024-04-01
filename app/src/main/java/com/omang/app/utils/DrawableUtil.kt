package com.omang.app.utils

import com.omang.app.R

object DrawableUtil {

    fun getProgressTint(progress: Int): Pair<Int, Int> {
        return when (progress) {
            in 0..9 -> Pair(R.color.progress_zero, R.color.progress_zero_secondary)
            in 10..19 -> Pair(R.color.progress_ten, R.color.progress_ten_secondary)
            in 20..29 -> Pair(R.color.progress_twenty, R.color.progress_twenty_secondary)
            in 30..39 -> Pair(R.color.progress_thirty, R.color.progress_thirty_secondary)
            in 40..49 -> Pair(R.color.progress_forty, R.color.progress_forty_secondary)
            in 50..59 -> Pair(R.color.progress_fifty, R.color.progress_fifty_secondary)
            in 60..69 -> Pair(R.color.progress_sixty, R.color.progress_sixty_secondary)
            in 70..79 -> Pair(R.color.progress_seventy, R.color.progress_seventy_secondary)
            in 80..89 -> Pair(R.color.progress_eighty, R.color.progress_eighty_secondary)
            in 90..99 -> Pair(R.color.progress_ninety, R.color.progress_ninety_secondary)
            100 -> Pair(R.color.progress_hundred, R.color.progress_hundred)
            else -> Pair(R.color.progress_zero, R.color.progress_zero_secondary)
        }
    }
}