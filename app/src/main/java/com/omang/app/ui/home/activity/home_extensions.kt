package com.omang.app.ui.home.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import android.util.Range
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.NavController
import coil.load
import com.google.gson.Gson
import com.omang.app.BuildConfig
import com.omang.app.R
import com.omang.app.data.database.psm.PsmEntity
import com.omang.app.data.model.updates.DetailsItem
import com.omang.app.data.model.userAssign.Psm
import com.omang.app.databinding.DialogBrightnessBinding
import com.omang.app.databinding.DialogCbtBinding
import com.omang.app.ui.home.fragments.HomeFragmentDirections
import com.omang.app.utils.FileUtil
import com.omang.app.utils.SHA256Hasher
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.socket.SocketStatus
import timber.log.Timber
import java.io.File
import java.util.Calendar


fun HomeActivity.showMessagePopup() {
    val dialog = Dialog(this)
    val binding = DialogCbtBinding.inflate(LayoutInflater.from(this))

    var pathWayCount = 0

    binding.tvPathWay.setOnClickListener {
        pathWayCount++
        if (pathWayCount == viewModel.sharedPref.acc) {
            pathWayCount = 0
            if (adminFlag) {
                binding.llPathWay.visible()
            }
        }
    }

    binding.btClear.setOnClickListener {
        val editable: Editable = binding.etPassword.text
        val length = editable.length
        if (length > 0) {
            editable.delete(length - 1, length)
        }
    }

    binding.btCheck.setOnClickListener {
        val ePassword: String = binding.etPassword.text.trim().toString()

        val hashedPassword = SHA256Hasher.sha256(ePassword)
        val oPassword = viewModel.sharedPref.dap.toString()

        Timber.tag("-->").e(hashedPassword)
        Timber.tag("-->").e(oPassword)
        Timber.tag("-->").e("${(hashedPassword == oPassword)}")

        if (hashedPassword == oPassword) {// Use == for content comparison
            HomeFragmentDirections.actionNavigationHomeToAdminPage().also {
                navController.navigate(it)
            }
        } else {
            showToast("Error")
        }

        dialog.dismiss()
    }


    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)
        show()

    }
}

fun HomeActivity.showAdjustBrightnessPopup() {
    val dialog = Dialog(this)
    val binding = DialogBrightnessBinding.inflate(LayoutInflater.from(this))
    binding.apply {
        seekBar.run {
            max = 255
            progress = Settings.System.getInt(
                this@showAdjustBrightnessPopup.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0
            )
            incrementProgressBy(1)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    Settings.System.putInt(
                        this@showAdjustBrightnessPopup.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        progress
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        btnOk.setOnClickListener { _: View? ->
            dialog.dismiss()
        }
    }
    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCancelable(true)
        show()
    }
}

fun HomeActivity.isSimCardInserted(): Boolean {
    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telephonyManager.simState == TelephonyManager.SIM_STATE_READY
}


fun HomeActivity.showSettingsPopup() {

    var isClickedBlutooth = false

    Timber.tag("WIFI").e("wifistatus dialog : ${viewModel.isOnWifi(applicationContext)}")
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_settings)
    dialog.setCancelable(true)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    val tv = TypedValue()
    if (this.theme.resolveAttribute(0, tv, true)) {
        val actionBarHeight =
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        lp.y = actionBarHeight + 8
        lp.x = 16
    }

    val ivWifi: AppCompatImageView = dialog.findViewById(R.id.btn_wifi)
    val ibSync: AppCompatImageButton = dialog.findViewById(R.id.ib_sync)
    val btnBluetooth1: AppCompatImageView = dialog.findViewById(R.id.btn_bluetooth)
    val ivDataEnable: AppCompatImageView = dialog.findViewById(R.id.ivDataEnable)
    val ivDataDisable: AppCompatImageView = dialog.findViewById(R.id.ivDataDisable)
    val ibCsdk: AppCompatImageButton = dialog.findViewById(R.id.ib_csdk)
    val imgBrightness: AppCompatImageView = dialog.findViewById(R.id.img_brightness)

    if (BuildConfig.DEBUG) {
        ibCsdk.visible()
    }

    ivWifi.setBackgroundResource(if (viewModel.isOnWifi(applicationContext)) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
    ivDataDisable.setBackgroundResource(if (!viewModel.isOnMobileNetwork(applicationContext)) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)

    ivWifi.setOnClickListener {
        val flag = viewModel.isOnWifi(applicationContext)
        viewModel.wifi(flag)
        ivWifi.setBackgroundResource(if (!flag) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
        if (!flag) {
            openWifi()
        }
    }

    ivDataEnable.setOnClickListener {
        toast.run { showToast(getString(R.string.mobile_data_enabled)) }
        binding.lyToolbar.ivInternet.gone()
        if (isSimCardInserted()) {
            binding.lyToolbar.ivSim.visible()
            binding.lyToolbar.ivSim.setImageResource(R.drawable.mobile_data_blue)
        }
        viewModel.data(true)
        ivDataEnable.setBackgroundResource(R.drawable.setting_bt_bg_enabled)
        ivDataDisable.setBackgroundResource(R.drawable.setting_bt_bg_disabled)
    }

    ivDataDisable.setOnClickListener {
        toast.run { showToast(getString(R.string.mobile_data_disabled)) }
        binding.lyToolbar.ivInternet.gone()

        if (isSimCardInserted()) {
            binding.lyToolbar.ivSim.visible()
            binding.lyToolbar.ivSim.setImageResource(R.drawable.sim_inserted_blue)
        }
        viewModel.data(false)
        ivDataDisable.setBackgroundResource(R.drawable.setting_bt_bg_enabled)
        ivDataEnable.setBackgroundResource(R.drawable.setting_bt_bg_disabled)
    }

    btnBluetooth1.setOnClickListener {   /*onWifiClick()*/
        isClickedBlutooth = !isClickedBlutooth

        if (isClickedBlutooth) {
            //check if BT already on or not !!!
            btnBluetooth1.setBackgroundResource(R.drawable.setting_bt_bg_enabled)
        } else {
            btnBluetooth1.setBackgroundResource(R.drawable.setting_bt_bg_disabled)

        }
    }

    ibSync.setOnClickListener {   /*onWifiClick()*/ }

    ibCsdk.setOnClickListener {
        csdkUtil()
    }

    imgBrightness.setOnClickListener {
        showAdjustBrightnessPopup()
        dialog.dismiss()
    }

    lp.flags = lp.flags and WindowManager.LayoutParams.DIM_AMOUNT_CHANGED.inv()
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    dialog.window!!.attributes = lp
    dialog.show()
}

fun setBatteryIconResource(level: Int): Int {
    var resource: Int = R.drawable.bat_j
    val firstRange = Range(15, 20)
    val secondRange = Range(21, 30)
    val thirdRange = Range(31, 40)
    val fourthRange = Range(41, 60)
    val fifthRange = Range(61, 80)
    val sixthRange = Range(81, 99)
    val seventhRange = Range(0, 14)
    if (firstRange.contains(level)) {
        resource = R.drawable.bat_a
    } else if (secondRange.contains(level)) {
        resource = R.drawable.bat_b
    } else if (secondRange.contains(level)) {
        resource = R.drawable.bat_c
    } else if (thirdRange.contains(level)) {
        resource = R.drawable.bat_d
    } else if (fourthRange.contains(level)) {
        resource = R.drawable.bat_e
    } else if (fifthRange.contains(level)) {
        resource = R.drawable.bat_f
    } else if (sixthRange.contains(level)) {
        resource = R.drawable.bat_g
    } else if (level == 100) {
        resource = R.drawable.bat_h
    }
    return resource
}

fun HomeActivity.showWifiSettings() {
    val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
    this.startActivity(intent)
}

fun HomeActivity.showUnpinDialog() {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_unpin_device)
    dialog.setCancelable(false)

    val btOk: Button = dialog.findViewById(R.id.btOk)
    btOk.setOnClickListener {
        dialog.dismiss()
        unPinDevice()

    }

    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    dialog.window!!.attributes = lp
    dialog.show()
}

fun HomeActivity.showPinDialog() {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_pin_device)
    dialog.setCancelable(false)

    val countDownTextView: TextView = dialog.findViewById(R.id.tvTitle)

    val timer = object : CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val secondsLeft = millisUntilFinished / 1000
            countDownTextView.text =
                "Device is about to be pinned. \nRestarting in $secondsLeft seconds..."
        }

        override fun onFinish() {
            dialog.dismiss()
            restartApp()
        }
    }

    timer.start()

    val window = dialog.window
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val layoutParams = WindowManager.LayoutParams().apply {
        copyFrom(window?.attributes)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    window?.attributes = layoutParams
    dialog.show()
}

fun HomeActivity.showWifiSelectionTimeoutPopup() {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_wifi_connection_timeout)
    dialog.setCancelable(false)

    val btn_yes = dialog.findViewById<Button>(R.id.btn_yes)
    val btn_no = dialog.findViewById<Button>(R.id.btn_no)

    btn_yes.setOnClickListener {
        openWifi()
        dialog.dismiss()
    }

    btn_no.setOnClickListener { dialog.dismiss() }
    /* val lp = WindowManager.LayoutParams()
     lp.copyFrom(dialog.window!!.attributes)
     lp.width = WindowManager.LayoutParams.MATCH_PARENT
     lp.height = WindowManager.LayoutParams.WRAP_CONTENT
     dialog.window!!.attributes = lp*/
    dialog.show()
}

fun HomeActivity.syncAlert(syncDialog: Dialog, flag: Boolean) {
    runOnUiThread {
        Timber.e("syncAlert : $flag")
        if (flag) {

            syncDialog.setContentView(R.layout.dialog_syncing)
            syncDialog.setCancelable(false)
            /* val imageView = syncDialog.findViewById<ImageView>(R.id.lt_alert)
             val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
             rotation.fillAfter = true
             imageView.startAnimation(rotation)*/

            syncDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(syncDialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            syncDialog.window!!.attributes = lp

            syncDialog.show()
        } else {
            syncDialog.dismiss()
        }
    }

}

fun HomeActivity.showDeviceLockedDialog() {
    val dialog = Dialog(this).apply {
        setContentView(R.layout.dialog_device_lock)
        setCancelable(false)
    }
    val window = dialog.window
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val layoutParams = WindowManager.LayoutParams().apply {
        copyFrom(window?.attributes)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    window?.attributes = layoutParams
    dialog.show()
}

fun HomeActivity.customNotification(dataList: List<DetailsItem?>, addFlag: Boolean) {
    var customNotificationList = mutableListOf<DetailsItem?>()

    Timber.tag("API DATA").e("API list is --> ${dataList.size}")

    val noShowFlag = customNotificationList.isNotEmpty() && addFlag

    if (addFlag) {
        customNotificationList = (customNotificationList + dataList).toMutableList()
    }

    if (customNotificationList.isNotEmpty() && !noShowFlag) {

        displayDialog(customNotificationList.first()!!) {
            if (customNotificationList.isNotEmpty()) {
                customNotificationList.removeFirst()
                customNotification(customNotificationList, false)
            }
        }
    }

}

fun HomeActivity.displayDialog(detailItem: DetailsItem, onDialogDismissed: () -> Unit) {
    val dialog = Dialog(this).apply {
        setContentView(R.layout.custom_notification_layout)
        setCancelable(false)
    }
    val window = dialog.window
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
    val tvBody = dialog.findViewById<TextView>(R.id.tv_body)
    val tvUser = dialog.findViewById<TextView>(R.id.tv_user)
    val tvPostDetails = dialog.findViewById<TextView>(R.id.tv_post_details)
    val imageView = dialog.findViewById<ImageView>(R.id.iv_post)
    val ivAvatar = dialog.findViewById<ImageView>(R.id.iv_avatar)
    val btClose = dialog.findViewById<ImageView>(R.id.bt_close)

    tvTitle?.text = detailItem.name
    tvBody?.text = detailItem.description
    tvBody?.movementMethod = ScrollingMovementMethod()
    detailItem.file?.let {
        imageView?.visible()
        imageView?.load(it)
        imageView?.scaleType = ImageView.ScaleType.FIT_XY
    }
    detailItem.createdBy?.let { createdByEntity ->
        createdByEntity.avatar?.let {
            ivAvatar.load(it)
        }
        createdByEntity.firstName?.let {
            tvUser.text = "$it ${createdByEntity.lastName}"
        }
    } ?: run {
        ivAvatar.load(R.drawable.home_logo)
        tvUser.gone()
    }
    detailItem.classroomDetails?.let { classRoom ->
        tvPostDetails.text =
            "Classroom : ${classRoom.name}"
        tvPostDetails.visible()
    } ?: run {
        tvPostDetails.gone()
    }

    btClose?.setOnClickListener {
        dialog.dismiss()
        onDialogDismissed()
    }

    val layoutParams = WindowManager.LayoutParams().apply {
        copyFrom(window?.attributes)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }
    window?.attributes = layoutParams

    if (!dialog.isShowing && detailItem.isPopUp == true) {
        dialog.show()
    }
}

fun NavController.safeNavigateToProfile() {
    if (currentDestination?.id != R.id.navigation_profile) {
        navigate(R.id.action_anyFragment_to_profileFragment)
    }
}

fun HomeActivity.tvStatusLogic(socketStatus: SocketStatus) {
    binding.apply {
        when (socketStatus) {
            SocketStatus.Connecting -> {
                if (!hasInternetConnection())
                    return
                tvSocket.text = getString(R.string.connecting)
                tvSocket.visibility = View.VISIBLE
                tvSocket.setBackgroundColor(applicationContext.getColor(R.color.socket_connecting))
                tvSocket.setTextColor(applicationContext.getColor(R.color.text_heading_color))
            }

            SocketStatus.Connected -> {
                tvSocket.text = getString(R.string.connected)
                tvSocket.visibility = View.VISIBLE
                tvSocket.setBackgroundColor(applicationContext.getColor(R.color.socket_connected))
                tvSocket.setTextColor(applicationContext.getColor(R.color.text_heading_color))

                statusHandler.postDelayed({
                    tvSocket.visibility = View.GONE
                }, 2000)
            }

            SocketStatus.Disconnected -> {
                tvSocket.text = if (viewModel.networkConnection.hasInternet())
                    getString(R.string.disconnected)
                else
                    getString(R.string.internet_error)
                tvSocket.visibility = View.VISIBLE
                tvSocket.setBackgroundColor(applicationContext.getColor(R.color.socket_disconnected))
                tvSocket.setTextColor(applicationContext.getColor(R.color.white))

                statusHandler.postDelayed({
                    tvSocket.visibility = View.GONE
                }, 2000)
            }

            is SocketStatus.Error -> {
                tvSocket.text = getString(R.string.error_socket)
                tvSocket.visibility = View.VISIBLE
                tvSocket.setBackgroundColor(applicationContext.getColor(R.color.socket_error))
                tvSocket.setTextColor(applicationContext.getColor(R.color.white))

                statusHandler.postDelayed({
                    tvSocket.visibility = View.GONE
                }, 2000)
            }

            else -> {}
        }
    }
}

fun HomeActivity.showPsmDialog(image: String?) {
    val psmDialog = Dialog(this, R.style.mDialogTheme).apply {
        setCancelable(false)
        setContentView(R.layout.dialog_overlay)
        findViewById<ImageView>(R.id.iv_psm).apply {
            setOnClickListener { dismiss() }
            //if image is not null we are loading it else sets the default psm logo
            image?.let {
                val psmImage = it.substring(
                    it.lastIndexOf(
                        '/',
                        it.lastIndex
                    ) + 1
                )
                val file = File(FileUtil.getPsmUrlPath(context), psmImage)
                load(file) {
                    crossfade(true)
                }
            } ?: run {
                setImageResource(R.drawable.omang_psm)
            }
        }
    }
    psmDialog.show()
}

//get psm image by current day, 0 refers to sunday....until 6 refers saturday
fun getTodaysPsm(psmEntity: PsmEntity): Psm? {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> {
            getPsmByIndex(0, psmEntity)
        }

        Calendar.MONDAY -> {
            getPsmByIndex(1, psmEntity)
        }

        Calendar.TUESDAY -> {
            getPsmByIndex(2, psmEntity)
        }

        Calendar.WEDNESDAY -> {
            getPsmByIndex(3, psmEntity)
        }

        Calendar.THURSDAY -> {
            getPsmByIndex(4, psmEntity)
        }

        Calendar.FRIDAY -> {
            getPsmByIndex(5, psmEntity)
        }

        Calendar.SATURDAY -> {
            getPsmByIndex(6, psmEntity)
        }

        else -> null

    }
}

fun getPsmByIndex(index: Int, psmEntity: PsmEntity): Psm? {
    Timber.tag("psm").d(psmEntity.psmResponse)
    val psmData = Gson().fromJson(psmEntity.psmResponse, Array<Psm>::class.java)
    return psmData.find { it.index == index }
}

fun getPsmByEntity(psmEntity: PsmEntity): Psm {
    return Gson().fromJson(psmEntity.psmResponse, Psm::class.java)
}

fun HomeActivity.showRatingDialog(callback: (ratingValue: Float) -> Unit) {
    val syncDialog = Dialog(this)
    syncDialog.setContentView(R.layout.dialog_rating)
    syncDialog.setCancelable(true)
    syncDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    var ratingValue: Float = 0.0F
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(syncDialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    syncDialog.window!!.attributes = lp
    val ratingBar = syncDialog.findViewById(R.id.ratingBar) as RatingBar
    val textComment = syncDialog.findViewById(R.id.ed_comment) as EditText
    val buttonSubmit = syncDialog.findViewById(R.id.btn_submit) as Button
    ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
        Timber.tag("RATING VALUE").e("Selected rating: $rating")
        ratingValue = rating
    }
    buttonSubmit.setOnClickListener {
        callback(ratingValue)
        syncDialog.dismiss()
    }
    syncDialog.show()
}
