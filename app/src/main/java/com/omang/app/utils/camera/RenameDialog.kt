package com.omang.app.utils.camera

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.omang.app.R
import com.omang.app.databinding.DialogRenameLayoutBinding
import com.omang.app.utils.extensions.showToast
import java.util.Objects

class RenameDialog(
    private val context: Context,
    private val isFromCamera: Boolean,
    private val currentFileName: String,
    private val onSuccess: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogRenameLayoutBinding? = null
    private val binding get() = _binding!!


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.mDialogTheme)
        _binding = DialogRenameLayoutBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvTitle.text = if (isFromCamera) "Rename Image" else "Rename video"
            etRename.setText(currentFileName)
        }

        /**
         * Validates the dialog and send the text back through the lambda function
         */
        binding.btSave.setOnClickListener {
            if (Objects.equals(binding.etRename.text.toString(), "")) {
                context.showToast("File name cannot be empty")
            } else {
                onSuccess(binding.etRename.text.toString())
                dismiss()
            }
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = 400
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
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
        }
        return dialog
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}