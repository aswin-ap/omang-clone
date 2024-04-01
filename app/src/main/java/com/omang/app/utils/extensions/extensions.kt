package com.omang.app.utils.extensions

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.omang.app.R
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.databinding.DialogTestCompletedBinding
import com.omang.app.databinding.MoodemeterDialogBinding
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.gallery.model.MediaFile
import com.omang.app.utils.FileUtil
import com.omang.app.utils.SafeClickListener
import com.omang.app.utils.home.DeviceStatusDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


//show toast

//Makes view visible
fun View.visible() {
    this.visibility = View.VISIBLE
}

//Makes view gone
fun View.gone() {
    this.visibility = View.GONE
}

//Makes view Invisible
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.hideKeyBoard() {
    try {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
    } catch (e: Exception) {
        Timber.d(e)
    }
}

//show toast
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun View.showSnackBar(message: String, isSuccess: Boolean = false) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .setBackgroundTint(
            ContextCompat.getColor(
                this.context,
                if (isSuccess) R.color.text_success_color else R.color.text_error_color
            )
        )
        .show()
}


//Show toast
fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Activity.getBitmapFromUrl(url: String): Bitmap =
    BitmapFactory.decodeFile(url)


fun String.toDate(format: String): String {

    if (format.isEmpty()) {
        return ""
    }
    try {
        val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val date: Date = srcDf.parse(this) as Date
        val destDf: DateFormat = SimpleDateFormat(format, Locale.US)
        return destDf.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

inline infix fun String.timeDifference(time: String): String {
    var timeDifference = ""

    try {
        val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        var date1 = srcDf.parse(this)
        var date2 = srcDf.parse(time)
        val destMOnth: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US)
        val date1Str = destMOnth.format(date1)
        val date2Str = destMOnth.format(date2)
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US)
        date1 = simpleDateFormat.parse(date1Str)
        date2 = simpleDateFormat.parse(date2Str)
        val difference = date2.time - date1.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        var hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
        val min =
            (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours).toInt() / (1000 * 60)
        hours = if (hours < 0) -hours else hours
        Log.e("======= Hours", " :: $hours")
        Log.e("======= days", " :: $days")
        Log.e("======= min", " :: $min")
        if (days > 0) {
            timeDifference = days.toString() + "d "
        }
        if (hours > 0) {
            timeDifference = timeDifference + hours + "h "
        }
        if (min > 0) {
            timeDifference = timeDifference + min + "min "
        }
        return timeDifference
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return timeDifference
}

infix fun String.timeInSpecificFormat(format: String?): String {
    if (this.isEmpty()) {
        return ""
    }
    var date1Str = ""
    try {
        val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val date1 = srcDf.parse(this)
        val destMonth: DateFormat = SimpleDateFormat(format)
        date1Str = destMonth.format(date1)
        return date1Str
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date1Str
}

/**
 * Creates AlertDialog
 */
fun Activity.showAlertDialog(
    title: String = "",
    message: String,
    icon: Int = R.mipmap.ic_launcher,
    positiveButtonTitle: String = "Yes",
    negativeButtonTitle: String = "No",
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: () -> Unit,
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setIcon(icon)
        .setPositiveButton(
            positiveButtonTitle
        ) { dialog, _ ->
            dialog.dismiss()
            onPositiveButtonClicked()
        }
        .setNegativeButton(
            negativeButtonTitle
        ) { dialog, _ ->
            dialog.dismiss()
            onNegativeButtonClicked()
        }.create()
        .show()
}

/**
 * Creates PopupMenu
 */
fun Context.createPopUp(
    view: View,
    menuItems: Int,
    onMenuItemClicked: (Int) -> Unit,
) {
    PopupMenu(this, view).apply {
        menuInflater.inflate(menuItems, menu)
        setOnMenuItemClickListener {
            onMenuItemClicked(it.itemId)
            true
        }
        show()
    }
}

/**
 * Sort the list based on the created time of file
 */
fun List<MediaFile>.sortFilesByCreationTime(isAscendingOrder: Boolean): List<MediaFile> =
    if (isAscendingOrder)
        sortedBy { it.createdAt }
    else
        sortedWith(compareByDescending { it.createdAt })

/**
 * Sort the list based on the name of file
 */
fun List<MediaFile>.sortFilesByName(isAscendingOrder: Boolean): List<MediaFile> =
    if (isAscendingOrder)
        sortedBy { it.name.lowercase(Locale.ROOT) }
    else
        sortedWith(compareByDescending { it.name.lowercase(Locale.ROOT) })

fun TextView.expand() {
    isSingleLine = false
    ellipsize = null
}


fun Button.setButtonTextColor(color: Int) {
    setTextColor(
        ContextCompat.getColor(
            this.context,
            color
        )
    )

}

fun Button.setButtonBackground(color: Int) {
    backgroundTintList =
        ContextCompat.getColorStateList(this.context, color)
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun FragmentActivity.showDeviceStatusDialog(title: String, body: String?) {
    try {
        val deviceStatusDialog =
            supportFragmentManager.findFragmentByTag("device_status") as? DeviceStatusDialog
        if (deviceStatusDialog == null) {
            DeviceStatusDialog(title, body).apply {
                isCancelable = false
            }.also {
                Timber.tag("DeviceStatusDialog").d("Dialog $it shows")
                it.show(supportFragmentManager, "device_status")
            }
        }
    } catch (e: NoSuchMethodException) {
        Timber.tag("Device Status No Such Method Exception").e("No such method: ${e.message}")
    }
}

fun FragmentActivity.hideDeviceStatusDialog() {
    supportFragmentManager.findFragmentByTag("device_status")?.let {
        (it as DialogFragment).dismiss()
        Timber.tag("DeviceStatusDialog").d("Dialog $it dismiss")
    }
}

fun FragmentActivity.showUserStatusDialog(title: String, body: String?) {
    val deviceStatusDialog =
        supportFragmentManager.findFragmentByTag("device_status") as? DeviceStatusDialog
    if (deviceStatusDialog == null) {
        DeviceStatusDialog(title, body).apply {
            isCancelable = false
        }.also {
            Timber.tag("DeviceStatusDialog").d("Dialog $it shows")
            it.show(supportFragmentManager, "user_status")
        }
    }
}

fun FragmentActivity.hideUserStatusDialog() {
    supportFragmentManager.findFragmentByTag("user_status")?.let {
        (it as DialogFragment).dismiss()
        Timber.tag("DeviceStatusDialog").d("Dialog $it dismiss")
    }
}


fun Context.hasInternetConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false

    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                socket.close()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
}


fun moodMeterDialog(
    dialog: Dialog,
    context: Context,
    hiTextName: String,
    itemClick: (Int, String) -> Unit,
) {
    var modeId: Int = 0
    var modeName = ""

    val dialogBinding = MoodemeterDialogBinding.inflate(LayoutInflater.from(context))
    dialog.setContentView(dialogBinding.root)

//    val modeMeterAdapter = ModeMeterAdapter(
//        adapterList,
//        onModeItemClick = {
//            modeId = it.id!!
//            modeName = it.displayName!!
//            itemClick(modeId, modeName)
//            dialog.cancel()
//        }
//    )

    dialogBinding.apply {
//        hiTextView.text = "Hi $hiTextName"
//        rvMoods.apply {
//            setHasFixedSize(true)
//            layoutManager = GridLayoutManager(context, 6)
//            adapter = modeMeterAdapter
//        }

        val moodList = listOf(
            Pair(1, "happy"),
            Pair(2, "angry"),
            Pair(3, "meh"),
            Pair(4, "sad"),
            Pair(5, "anxious"),
            Pair(6, "excited")
        )

        hiTextView.text = "Hi $hiTextName"


        moodList.forEach { (id, mood) ->
            val clMood = when (id) {
                1 -> dialogBinding.clHappy
                2 -> dialogBinding.clAngry
                3 -> dialogBinding.clMeh
                4 -> dialogBinding.clSad
                5 -> dialogBinding.clAnxious
                6 -> dialogBinding.clExcited
                else -> null
            }

            clMood?.setOnClickListener {
                itemClick(id, mood)
                dialog.dismiss()
            }
        }

        dialog.setCancelable(false)

        if (!dialog.isShowing) {
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

}

fun Fragment.showTestCompletedDialog(
    onOkButtonClicked: () -> Unit,
) {
    val dialog = Dialog(requireContext())
    val binding = DialogTestCompletedBinding.inflate(LayoutInflater.from(requireContext()))
    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)
        binding.apply {
            btnCancel.setOnClickListener {
                dismiss()
            }
            btnOk.setOnClickListener {
                dismiss()
                onOkButtonClicked()
            }
        }
        show()
    }
}

fun Any.toJson(): String {
    return Gson().toJson(this)
}

/*fun Fragment.showRatingDialog() {
    val syncDialog = Dialog(requireContext())
    syncDialog.setContentView(R.layout.dialog_rating)
    syncDialog.setCancelable(true)
    syncDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val lp = WindowManager.LayoutParams()
    lp.copyFrom(syncDialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    syncDialog.window!!.attributes = lp
    val ratingBar = syncDialog.findViewById(R.id.ratingBar) as RatingBar
     ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
        Timber.tag("RATING VALUE").e("Selected rating: $rating")
    }
    syncDialog.show()
}*/

fun Float.floorToInt(): Int {
    return kotlin.math.floor(this).toInt()
}

/*fun loadingDialog(syncDialog: Dialog, flag: Boolean) {
    if (flag) {
        syncDialog.dismiss()
        syncDialog.setContentView(R.layout.dialog_syncing)
        syncDialog.setCancelable(false)
        *//* val imageView = syncDialog.findViewById<ImageView>(R.id.lt_alert)
         val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
         rotation.fillAfter = true
         imageView.startAnimation(rotation)*//*

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
}*/

fun loadingDialog(syncDialog: Dialog, flag: Boolean, context: Context) {

    Timber.e("loading dialog " + flag)

    if (flag) {
        syncDialog.dismiss()
        syncDialog.setContentView(R.layout.dialog_syncing)
        syncDialog.setCancelable(false)
        val imageView = syncDialog.findViewById<ImageView>(R.id.lt_alert)
        imageView.load(R.raw.logo, imageLoader = ImageLoader.Builder(context)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build())
        syncDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(syncDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        syncDialog.window!!.attributes = lp
        syncDialog.window!!.setDimAmount(0f)
        syncDialog.show()
    } else {
        syncDialog.dismiss()
    }
}

fun ImageView.loadLocalImage(url: String) {
    val fileName = url.substring(
        url.lastIndexOf(
            '/',
            url.lastIndex
        ) + 1
    )
    val file = File(FileUtil.getImageUrlPath(context), fileName)
    load(file) {
        listener(
            onError = { _, _ ->
                setImageResource(R.drawable.place_holder)
            }
        )
    }
    visible()
}

fun ImageView.loadLocalEmoji(url: String) {
    val fileName = url.substring(
        url.lastIndexOf(
            '/',
            url.lastIndex
        ) + 1
    )
    val file = File(FileUtil.getEmojiUrlPath(context), fileName)
    load(file) {
        listener(
            onError = { _, _ ->
                setImageResource(R.drawable.user_place_holder)
            }
        )
    }
    visible()
}

enum class DateTimeFormat {
    TIME,
    DATE,
    TIME_N_DATE,
    MONTH_N_DATE,
    TO_LOCALE_TIME_N_DATE,
    MONTH_N_HR,
    TIME_N_AM_PM
}


fun convertTimestampToLocale(dateTime: String, format: DateTimeFormat): String {
    if (dateTime.isEmpty()) {
        return ""
    }
    val inputFormat = if (dateTime.length > 10) {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    } else {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    var date: Date = inputFormat.parse(dateTime)

    val outputFormat = when (format) {
        DateTimeFormat.TIME -> SimpleDateFormat("HH:mm", Locale.getDefault())
        DateTimeFormat.DATE -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        DateTimeFormat.TIME_N_DATE -> SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        DateTimeFormat.MONTH_N_DATE -> SimpleDateFormat(
            "dd'${getDayOfMonthSuffix(date)}' MMM",
            Locale.getDefault()
        )

        DateTimeFormat.TO_LOCALE_TIME_N_DATE -> {
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            date = inputFormat.parse(dateTime)
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

        }

        DateTimeFormat.MONTH_N_HR -> {
            SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
        }

        DateTimeFormat.TIME_N_AM_PM -> {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
        }
    }

    outputFormat.timeZone = TimeZone.getDefault()
    return outputFormat.format(date)
}

fun convertLocaleTimestampToLocale(dateTime: String, format: DateTimeFormat): String {
    if (dateTime.isEmpty()) {
        return ""
    }

    val inputFormat = if (dateTime.length > 10) {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    } else {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Set time zone before parsing

    val date: Date = try {
        inputFormat.parse(dateTime)
    } catch (e: ParseException) {
        e.printStackTrace()
        return ""
    }

    val outputFormat = when (format) {
        DateTimeFormat.TIME -> SimpleDateFormat("HH:mm", Locale.getDefault())
        DateTimeFormat.DATE -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        DateTimeFormat.TIME_N_DATE -> SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        DateTimeFormat.MONTH_N_DATE -> SimpleDateFormat(
            "dd'${getDayOfMonthSuffix(date)}' MMM",
            Locale.getDefault()
        )

        DateTimeFormat.TO_LOCALE_TIME_N_DATE -> TODO()
        DateTimeFormat.MONTH_N_HR -> {
            SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
        }

        DateTimeFormat.TIME_N_AM_PM -> {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
        }
    }

    outputFormat.timeZone = TimeZone.getDefault()
    return outputFormat.format(date)
}


fun getDayOfMonthSuffix(date: Date): String {
    val day = SimpleDateFormat("d", Locale.getDefault()).format(date)
    return when (day.toInt()) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }
}

fun isToday(timestamp1: String, timestamp2: String): Boolean {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val calendar1 = Calendar.getInstance()
    calendar1.time = sdf.parse(timestamp1)
    val formattedDate1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar1.time)

    val calendar2 = Calendar.getInstance()
    calendar2.time = sdf.parse(timestamp2)
    val formattedDate2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar2.time)

//    Timber.e("msgDate : $formattedDate1 :: todayDate : $formattedDate2 -> ${formattedDate1 == formattedDate2}")

    return formattedDate1 == formattedDate2
}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

fun View.updateAlpha(value: Float) {
    alpha = value
    isClickable = alpha == 1.0f
}

infix fun String.justReceive(param: Int): String {
    /*      `asa`   10*/
    return "$this $param"
}

infix fun String.timeDifferenceInDays(time: String): String {
    var timeDifference = ""

    try {
        val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        var date1 = srcDf.parse(this)
        var date2 = srcDf.parse(time)
        val destMOnth: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US)
        val date1Str = destMOnth.format(date1)
        val date2Str = destMOnth.format(date2)
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US)
        date1 = simpleDateFormat.parse(date1Str)
        date2 = simpleDateFormat.parse(date2Str)
        val difference = date2.time - date1.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        timeDifference = when (days) {
            0 -> "Today"
            1 -> "Yesterday"
            else -> "$days days"
        }
        return timeDifference
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return timeDifference
}


fun log(analyticsEntity: AnalyticsEntity) {

    val TAG = "ANALYTICS"

    Timber.tag(TAG)
        .v("\n<- - - - - - - - - - - - - - - - - - - - - - - ╾━━━━╤デ╦︻(▀̿Ĺ̯▀̿ ̿) - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ->\n")
    val logStatement = StringBuilder("inserted -> ")
    val fields = analyticsEntity.javaClass.declaredFields

    for (field in fields) {
        field.isAccessible = true
        logStatement.append("${field.name}=${field.get(analyticsEntity)}, ")
    }

    Timber.tag(TAG).v(logStatement.toString())
    Timber.tag(TAG)
        .v("\n<- - - - - - - - - - - - - - - - - - - - - - - ╾━━━━╤デ╦︻(▀̿Ĺ̯▀̿ ̿) - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ->\n")

}

fun AppUpdateActivity.showSettingsPopup() {

    Timber.tag("WIFI").e("wifistatus dialog : ${viewModel.isOnWifi(applicationContext)}")

    val dialog = Dialog(this)
    dialog.setContentView(R.layout.registration_settings)
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
    val ivDataEnable: AppCompatImageView = dialog.findViewById(R.id.ivDataEnable)
    val ivDataDisable: AppCompatImageView = dialog.findViewById(R.id.ivDataDisable)

    ivWifi.setBackgroundResource(if (viewModel.isOnWifi(applicationContext)) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
    ivDataEnable.setBackgroundResource(if (viewModel.isOnMobileNetwork(applicationContext)) R.drawable.setting_bt_bg_enabled else R.drawable.setting_bt_bg_disabled)
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
        //binding.lyToolbar.ivInternet.visible()
        viewModel.data(true)
        ivDataEnable.setBackgroundResource(R.drawable.setting_bt_bg_enabled)
        ivDataDisable.setBackgroundResource(R.drawable.setting_bt_bg_disabled)

    }

    ivDataDisable.setOnClickListener {
        toast.run { showToast(getString(R.string.mobile_data_disabled)) }
        //  binding.lyToolbar.ivInternet.gone()
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

fun gcdOfStrings(str1: String, str2: String): String {
    return if (str1.contains(str2)) {
        if (str1.startsWith(str2)) {
            str1.replaceFirst(str2, "")
        } else {
            ""

        }
    } else {
        ""
    }
}