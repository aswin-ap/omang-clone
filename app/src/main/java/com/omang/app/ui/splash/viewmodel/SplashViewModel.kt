package com.omang.app.ui.splash.viewmodel

import android.app.Activity
import android.app.AppOpsManager
import android.app.AppOpsManager.OnOpChangedListener
import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.dataStore.DataStoreKeys.IS_USER_ASSIGNED
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.utils.Cancellable
import com.omang.app.utils.PermissionRequester
import com.omang.app.utils.State
import com.omang.app.utils.StringUtils.PERMISSION_DENIED_TIMBERLOG
import com.omang.app.utils.StringUtils.PERMISSION_GRANTED_TIMBERLOG
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    application: Application,
    private var networkConnection: NetworkConnection
) : BaseViewModel(application) {
    private val _isUserAssigned = MutableLiveData<Boolean>()
    val isUserAssigned: LiveData<Boolean> = _isUserAssigned

    private val _internet = MutableLiveData<Boolean>()
    val internet: LiveData<Boolean> = _internet

    private var kioskJob: Job? = null

    fun requestPermission(
        context: Context,
        permissions: Array<String>,
    ): Cancellable {
        return PermissionRequester.requestPermissions(
            context, *permissions
        ) {
            if (it.firstOrNull()?.state == State.GRANTED) {
                Timber.i(PERMISSION_GRANTED_TIMBERLOG)
            } else
                Timber.i(PERMISSION_DENIED_TIMBERLOG)

        }
    }

    fun getInternetStatus() {
        viewModelScope.launch {
            networkConnection.observeNetworkConnection().collectLatest {
                Timber.d("NetworkStatus : $it")
                _internet.postValue(it)
            }
        }
    }

    fun isUsageAccessEnabled(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = appOps.unsafeCheckOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED)
            return true

        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
            context.packageName,
            object : OnOpChangedListener {
                override fun onOpChanged(op: String, packageName: String) {
                    if (mode != AppOpsManager.MODE_ALLOWED)
                        return
                    appOps.stopWatchingMode(this)
                }
            })
        return false
    }

    fun launchReadNetworkHistoryAccess(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun launchOverlayPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isDefaultDialer(context: Context): Boolean {
        val roleManager = context.getSystemService(ROLE_SERVICE) as RoleManager
        val role = roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        Timber.e("isDefaultDialer $role")
        return role
    }

    fun requestDialerRole(context: Context) {
        Timber.e("request role")

        val roleManager = context.getSystemService(ROLE_SERVICE) as RoleManager?
        val intent = roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun launchAllFileAccess(context: Context) {
        /*        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        //        ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)*/

        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)

    }

    fun launchSystemSettingWritePermission(context: Context) {
        // If do not have write settings permission then open the Can modify system settings panel.
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

    }

    fun checkPermissions(permissions: Array<String>, activity: Activity?): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(activity!!, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        return listPermissionsNeeded.isEmpty()

    }

    fun getNavigationLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.getMobileNavigationEntries().also {
                Timber.d("navigation full: ${it.toJson()} /n")

                /* it.forEach { entity ->
                     Timber.d("navigation list: ${entity.toJson()} /n")
                 }*/
            }
        }
    }

    fun toNextScreen() {
        Timber.tag("ERROR_3").e("toNextScreen in view model")

        val flag = dataManagerRepository.getFromDataStore(IS_USER_ASSIGNED).asLiveData()
        flag.observeForever { value ->
            value?.let {
                Timber.d("IS_USER_ASSIGNED -> $it")
                _isUserAssigned.postValue(it)
                flag.removeObserver { }

            } ?: kotlin.run {
                Timber.d("IS_USER_ASSIGNED -> null")
                _isUserAssigned.postValue(false)
                flag.removeObserver { }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        kioskJob?.cancel()
//        networkConnectionManager.stopListenNetworkState()

    }
}