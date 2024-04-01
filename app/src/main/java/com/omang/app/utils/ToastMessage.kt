package com.omang.app.utils


import android.content.Context
import android.view.Gravity
import android.widget.Toast

class ToastMessage(private val context: Context) {
    fun showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun showCenterAlignedToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(context, message, duration)
        toast.setGravity(Gravity.CENTER, 0, 130)
        toast.show()
    }


}
