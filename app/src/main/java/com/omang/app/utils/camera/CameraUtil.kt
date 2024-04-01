package com.omang.app.utils.camera

import androidx.camera.core.AspectRatio
import kotlin.math.abs

object CameraUtil {
    private const val RATIO_4_3_VALUE = 4.0 / 3.0
    private const val RATIO_16_9_VALUE = 16.0 / 9.0

    fun getAspectRatio(width: Int, height: Int): Int {
        val previewRatio =
            width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height).toDouble()
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(
                previewRatio - RATIO_16_9_VALUE
            )
        ) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
    }
}