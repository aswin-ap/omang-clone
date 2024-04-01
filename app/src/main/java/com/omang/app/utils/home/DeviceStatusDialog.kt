package com.omang.app.utils.home

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.omang.app.R
import com.omang.app.databinding.DialogDeviceLockBinding

class DeviceStatusDialog(private val title: String, private val contentBody: String?) :
    DialogFragment() {

    private var _binding: DialogDeviceLockBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.mDialogTheme)
        _binding = DialogDeviceLockBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        bindData()

        val lp = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }
        dialog.apply {
            window?.setBackgroundDrawable(
                ColorDrawable(
                    resources.getColor(
                        R.color.dialog_grey_bg,
                        activity?.theme
                    )
                )
            )
            window?.attributes = lp
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
        return dialog
    }

    private fun bindData() {
        binding.tvDescription.text = contentBody ?: ""
        binding.tvLocked.text = title
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}