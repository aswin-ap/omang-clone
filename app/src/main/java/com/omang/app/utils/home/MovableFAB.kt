package com.omang.app.utils.home

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.cardview.widget.CardView


class MovableFAB : CardView, OnTouchListener {
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = motionEvent.rawX
                downRawY = motionEvent.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                return true // Consumed
            }

            MotionEvent.ACTION_MOVE -> {
                val newX = motionEvent.rawX + dX
                val newY = motionEvent.rawY + dY

                view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val screenWidth = view.context.resources.displayMetrics.widthPixels
                val screenHeight = view.context.resources.displayMetrics.heightPixels

                val distances = listOf(
                    view.x,
                    screenWidth - (view.x + view.width),
                    view.y,
                    screenHeight - (view.y + view.height)
                )

                val minDistance = distances.minOrNull()
                val targetX = when (distances.indexOf(minDistance)) {
                    0 -> 0f
                    1 -> (screenWidth - view.width).toFloat()
                    else -> view.x
                }
                val targetY = when (distances.indexOf(minDistance)) {
                    2 -> 0f
                    3 -> (screenHeight - view.height).toFloat()
                    else -> view.y
                }

                view.animate()
                    .x(targetX)
                    .y(targetY)
                    .setDuration(300)
                    .withEndAction {
                        if (motionEvent.eventTime - motionEvent.downTime < CLICK_TIME_THRESHOLD) {
                            performClick()
                        }
                    }
                    .start()
                return true // Consumed
            }

            else -> return super.onTouchEvent(motionEvent)
        }
    }

    companion object {
        private const val CLICK_TIME_THRESHOLD = 200L
    }
}