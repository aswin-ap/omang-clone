package com.omang.app.ui.registration.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.omang.app.BuildConfig
import com.omang.app.R
import com.omang.app.databinding.FragmentRegistrationBinding
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.registration.activity.BarcodeScannerActivity
import com.omang.app.ui.registration.activity.RegistrationActivity
import com.omang.app.ui.registration.viewmodel.RegistrationViewModel
import com.omang.app.utils.QRCodeScannerManager
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.csdk.CSDKConstants
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hideKeyBoard
import com.omang.app.utils.extensions.showSnackBar
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject


@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegistrationViewModel>()

    @Inject
    lateinit var toast: ToastMessage

    private var wifiSelectionHandler = Handler(Looper.getMainLooper())
    private var wifiSelectionRunnable = Runnable {
        val intent = Intent(requireContext(), RegistrationActivity::class.java)
        val bundle = Bundle()
        bundle.putString("OPEN_FROM_WIFI_SETTINGS", "true")
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private val wifiSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                wifiSelectionHandler.removeCallbacks(wifiSelectionRunnable)
            }
        }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateToken()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    private fun bindView() {

        if (BuildConfig.DEBUG) {
            binding.apply {
                BtDeviceM9.visibility = View.VISIBLE
                BtAssign.visibility = View.VISIBLE
            }
        }

        binding.apply {
            registerPassword.addTextChangedListener(afterTextChanged = { text ->
                text?.let {
                    viewModel.updatePassword(password = text.toString())
                }
            })
            userAssignPassword.addTextChangedListener(afterTextChanged = { text ->
                text?.let {
                    viewModel.updatePassword(password = text.toString())
                }
            })

            btRegister.setOnClickListener {
                requireActivity().hideKeyBoard()
                viewModel.registerDevice()
            }

            btAssign.setOnClickListener {
                requireActivity().hideKeyBoard()
                viewModel.assignUser()
            }

            llRegisterImeiScan.setOnClickListener {
                //  scanCode(ScanType.IMEI)
                binding.tvError.gone()
                barcodeLauncher.launch(
                    Intent(
                        requireContext(),
                        BarcodeScannerActivity()::class.java
                    ).apply {
                        putExtra(BarcodeScannerActivity.SCANNER_TYPE, ScanType.IMEI.name)
                    })
            }

            llRegisterSimScan.setOnClickListener {
                // scanCode(ScanType.SIM)
                barcodeLauncher.launch(
                    Intent(
                        requireContext(),
                        BarcodeScannerActivity()::class.java
                    ).apply {
                        putExtra(BarcodeScannerActivity.SCANNER_TYPE, ScanType.SIM.name)
                    })
            }

            llUserScan.setOnClickListener {
                // scanCode(ScanType.USER_ID)
                lifecycleScope.launch {
                    QRCodeScannerManager().startScan(barLauncher)
                }
            }

            llUserSimScan.setOnClickListener {
                //  scanCode(ScanType.SIM)
                barcodeLauncher.launch(
                    Intent(
                        requireContext(),
                        BarcodeScannerActivity()::class.java
                    ).apply {
                        putExtra(BarcodeScannerActivity.SCANNER_TYPE, ScanType.SIM.name)
                    })
            }

            BtDeviceM9.setOnClickListener {
                viewModel.updateImei(imei = "42248729")
                viewModel.updateSimNo(simNo = "46464655")

                val text = Editable.Factory.getInstance().newEditable("s1234")
                registerPassword.text = text

                viewModel.registerDevice()

            }

            BtAssign.setOnClickListener {
                viewModel.updateUserId(
                    userId = if (BuildConfig.BASE_URL.contains("uat")) 6 else 30,
                    userName = "Allwin m9"
                )

                val text = Editable.Factory.getInstance().newEditable("s1234")
                userAssignPassword.text = text

                viewModel.assignUser()

            }
            settings.setOnClickListener {
                showRegistrationSettings()
            }
        }
    }

    private fun showRegistrationSettings() {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.registration_settings)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        val tv = TypedValue()
        if (context?.theme?.resolveAttribute(android.R.attr.actionBarSize, tv, true) == true) {
            val actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            lp.y = actionBarHeight + 8
            lp.x = 16
        }

        val ivWifi: AppCompatImageView = dialog.findViewById(R.id.btn_wifi)
        val ivDataEnable: AppCompatImageView = dialog.findViewById(R.id.ivDataEnable)
        val ivDataDisable: AppCompatImageView = dialog.findViewById(R.id.ivDataDisable)

        ivWifi.setBackgroundResource(if (viewModel.isOnWifi(requireContext())) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
        ivDataEnable.setBackgroundResource(if (viewModel.isOnMobileNetwork(requireContext())) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
        ivDataDisable.setBackgroundResource(if (!viewModel.isOnMobileNetwork(requireContext())) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)


        ivWifi.setOnClickListener {
            val flag = viewModel.isOnWifi(requireContext())
            viewModel.wifi(flag)
            ivWifi.setBackgroundResource(if (!flag) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
            if (!flag) {
                openWifi()
            }
        }

        ivDataEnable.setOnClickListener {
            toast.run { showToast(getString(R.string.mobile_data_enabled)) }
            viewModel.data(true)
            ivDataEnable.setBackgroundResource(R.drawable.setting_bt_bg_enabled)
            ivDataDisable.setBackgroundResource(R.drawable.setting_bt_bg_disabled)

        }

        ivDataDisable.setOnClickListener {
            toast.run { showToast(getString(R.string.mobile_data_disabled)) }
            viewModel.data(false)
            ivDataDisable.setBackgroundResource(R.drawable.setting_bt_bg_enabled)
            ivDataEnable.setBackgroundResource(R.drawable.setting_bt_bg_disabled)

        }

        lp.flags = lp.flags and WindowManager.LayoutParams.DIM_AMOUNT_CHANGED.inv()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp
        dialog.show()
    }

    private fun openWifi() {
        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
        wifiSelectionHandler.postDelayed(wifiSelectionRunnable, 30000)
    }

    private val barLauncher: ActivityResultLauncher<ScanOptions> =
        registerForActivityResult(ScanContract()) { result ->
            result.contents?.let { data ->
                try {
                    val gson = Gson()
                    val userDetails: UserDetails =
                        gson.fromJson(data, UserDetails::class.java)
                    viewModel.updateUserId(
                        userId = userDetails.id!!,
                        userName = userDetails.name!!
                    )
                    toast.showToast(" ${userDetails.name} (${userDetails.accessionNumber}) \n ${userDetails.school}")
                } catch (e: Exception) {

                    toast.showToast(getString(R.string.something_went_wrong))
                }
            } ?: kotlin.run {

            }
        }

    @SuppressLint("SetTextI18n")
    private val barcodeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.extras?.let {
                    val scanType = it.getString(BarcodeScannerActivity.SCANNER_TYPE)
                    val value = it.getString(BarcodeScannerActivity.BARCODE_SCANNER_DATA)
                    when (ScanType.valueOf(scanType!!)) {
                        ScanType.IMEI -> {
//                            if (/*true*/viewModel.isImeiValid(value.toString())) {
                            if (true) {
                                viewModel.updateImei(imei = value.toString())

                            } else {
                                toast.showToast(
                                    "The IMEI: ${value.toString()} is invalid, Please scan again",
                                    Toast.LENGTH_LONG
                                )
                                binding.tvError.text =
                                    "The IMEI: ${value.toString()} is invalid, Please scan again"
                                binding.tvError.visible()
                                viewModel.updateImei(imei = "")

                            }
                        }

                        ScanType.SIM -> {
                            viewModel.updateSimNo(simNo = value.toString())
                        }

                        ScanType.USER_ID -> TODO()
                    }
                }
            } else {
                showToast(getString(R.string.something_went_wrong))
            }
        }

    enum class ScanType {
        IMEI, SIM, USER_ID
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observerView()
    }

    private fun observerView() {
        with(viewModel) {
            registrationState.observe(viewLifecycleOwner) {
                binding.apply {
                    if (accessToken.value.isNullOrEmpty()) {
                        tvRegisterImeiNo.text =
                            it.imei.ifEmpty { resources.getString(R.string.click_here_to_scan) }
                        tvRegisterSimNo.text =
                            it.simNo.ifEmpty { resources.getString(R.string.click_here_to_scan) }
                    } else {
//                        updateDeviceStatus()

                        tvUserSIMNo.text =
                            it.simNo.ifEmpty { resources.getString(R.string.click_here_to_scan) }
                        tvUserName.text =
                            it.userName.ifEmpty { resources.getString(R.string.click_here_to_scan) }
                    }
                }
            }

            viewModel.updateImei(
                try {
                    viewModel.getDeviceInfo(CSDKConstants.Device.IMEI)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
            )

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                it?.let {
                    when (it) {
                        is UIMessageState.StringMessage -> {
                            showMessage(it.message, it.status)
                            viewModel.resetUIUpdate()
                        }

                        is UIMessageState.StringResourceMessage -> {
                            showMessage(getString(it.resId), it.status)
                            viewModel.resetUIUpdate()
                        }

                        is UIMessageState.ScreenTransaction -> {
                            showMessage(getString(it.resId), it.status)
                            viewModel.resetUIUpdate()
                            toNextScreen()
                        }

                        else -> {
                            return@let
                        }
                    }
                }
            }
            accessToken.observe(viewLifecycleOwner) { accessToken ->

                binding.apply {
                    if (accessToken.isNullOrEmpty()) {
                        clUserAssign.visibility = View.GONE
                        cvUserAssign.visibility = View.GONE
                        clRegistration.visibility = View.VISIBLE
                        cvRegistration.visibility = View.VISIBLE
                        imDeviceRegisterStatus.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_disabled
                            )
                        )
                    } else {
                        clUserAssign.visibility = View.VISIBLE
                        cvUserAssign.visibility = View.VISIBLE
                        clRegistration.visibility = View.GONE
                        cvRegistration.visibility = View.GONE
                        imDeviceRegisterStatus.backgroundTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.text_success_color
                                )
                            )
                        imUserAssignStatus.backgroundTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.color_disabled
                                )
                            )
                    }
                }
            }

            progress.observe(viewLifecycleOwner) {
                binding.progressCircular.visibility = if (it) View.VISIBLE else View.GONE

            }

            pin.observe(viewLifecycleOwner) { isPinned ->
                if (isPinned)
                    pinDevice()
            }
        }
    }

    private fun pinDevice() {
        viewModel.setDeviceOwner()
        showPinDialog()
    }

    private fun showMessage(message: String, status: Boolean) {
        binding.root.showSnackBar(message, status)
    }

    private fun toNextScreen() {
        val i = Intent(requireActivity(), AppUpdateActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        requireActivity().finish()
    }

    private fun showPinDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_pin_device)
        dialog.setCancelable(false)

        val countDownTextView: TextView = dialog.findViewById(R.id.tvTitle)

        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countDownTextView.text =
                    "Device is about to be pinned.. \nRestarting in $secondsLeft seconds.."
            }

            override fun onFinish() {
                dialog.dismiss()
                restartApp()
            }
        }

        dialog.setOnShowListener {
            timer.start()
        }

        val window = dialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(window?.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        window?.attributes = layoutParams
        dialog.show()
    }

    fun restartApp() {
        viewModel.restartApp(requireContext())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = RegistrationFragment().apply {

        }
    }
}

data class UserDetails(
    val name: String?,
    val accessionNumber: String?,
    val id: Int?,
    val school: String?,
)