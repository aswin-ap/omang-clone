package com.omang.app.ui.call

import android.annotation.SuppressLint
import com.omang.app.databinding.DialogCallBinding

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.omang.app.R
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

object CallerUI {
    private const val CALL_TAG = "PHONE_CALL"

    private var windowManager: WindowManager? = null

    @SuppressLint("StaticFieldLeak")
    private var windowLayout: DialogCallBinding? = null

    private const val WINDOW_WIDTH_RATIO = 0.8f
    private var params: WindowManager.LayoutParams? = null
    private var x = 0f
    private var y = 0f

    private var callPicked: Boolean = false
    private var phone: String = ""

    private var elapsedTimeInSeconds: Long = 0
    private lateinit var updateTimer: Timer

    @SuppressLint("SetTextI18n")
    fun showWindow(context: Context, phone: String) {

        Timber.tag(CALL_TAG).e("showWindow $phone")
        this.phone = phone

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowLayout = DialogCallBinding.inflate(LayoutInflater.from(context))

        getLayoutParams(context)
        setOnTouchListener()

        updateTimer = Timer()

        windowLayout?.apply {
            tvPrimary.text = context.getString(R.string.incoming_call) + " " + phone

            ivBt.setOnClickListener {
                if (!callPicked) {
                    answerCall(context)
                    callPicked = true

                } else {
                    hangUpCall(context)

                }
            }
        }

        playRingtone(context)

        windowManager!!.addView(windowLayout?.root, params)
    }

    private fun getLayoutParams(context: Context) {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getWindowsTypeParameter(),
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params!!.gravity = Gravity.CENTER
        params!!.format = 1
        params!!.width = getWindowWidth(context)
    }

    private fun getWindowsTypeParameter(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun getWindowWidth(context: Context): Int {
        val metrics = context.resources.displayMetrics
        return (WINDOW_WIDTH_RATIO * metrics.widthPixels).toInt()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener() {
        windowLayout!!.root.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                }

                MotionEvent.ACTION_MOVE -> updateWindowLayoutParams(event)
                MotionEvent.ACTION_UP -> view.performClick()
            }
            false
        }
    }

    private fun updateWindowLayoutParams(event: MotionEvent) {
        params!!.x = (params!!.x - (x - event.rawX)).toInt()
        params!!.y = (params!!.y - (y - event.rawY)).toInt()
        windowManager!!.updateViewLayout(windowLayout!!.root, params)
        x = event.rawX
        y = event.rawY
    }

    @SuppressLint("MissingPermission")
    private fun answerCall(context: Context) {
        // Handle answering the call
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.acceptRingingCall()
        callPickedUpUI(context)
    }

    @SuppressLint("MissingPermission")
    private fun hangUpCall(context: Context) {
        // Handle hanging up the call
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.endCall()
        callEndUI(context)
    }

    private fun callPickedUpUI(context: Context) {
        windowLayout?.apply {
            tvPrimary.text = context.getString(R.string.ongoing_call) + " " + phone
            ivBt.setImageResource(R.drawable.ic_call_hung_up)
        }
        startTimer()
        stopRingtone()

    }

    private val timer = object : CountDownTimer(3000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            closeWindow()

        }
    }

    fun callEndUI(context: Context) {
        windowLayout?.apply {
            tvPrimary.text = "Aunty A"
            tvSecondary.text = context.getString(R.string.call_ended)
            tvSecondary.setTextColor(
                ContextCompat.getColor(
                    context, R.color.color_tomato_red
                )
            )
            ivBt.setImageResource(R.drawable.ic_call_hunged)
        }

        stopRingtone()
        updateTimer.cancel()
        timer.start()

    }

    private fun startTimer() {
            updateTimer = Timer()
            updateTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    elapsedTimeInSeconds++
                    windowLayout?.tvDuration?.post {
                        windowLayout?.tvDuration?.text = formatTime(elapsedTimeInSeconds)
                        windowLayout?.tvDuration?.visibility = View.VISIBLE

                    }
                }
            }, 0, 1000)

    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun closeWindow() {
        callPicked = false
        elapsedTimeInSeconds = 0

        updateTimer.cancel()

        if (windowLayout != null) {
            windowManager!!.removeView(windowLayout!!.root)
            windowLayout = null
        }
    }

    private var ringtone: Ringtone? = null

    private fun playRingtone(context: Context) {
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
    }
}
