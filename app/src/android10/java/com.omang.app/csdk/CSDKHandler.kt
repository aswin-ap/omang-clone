package com.omang.app.csdk

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.app.csdk.CSDKManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.omang.app.BuildConfig
import com.omang.app.ui.splash.activity.LauncherActivity
import com.omang.app.utils.csdk.CSDKConstants
import com.omang.app.utils.csdk.DeviceSystemAdministrator
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * <h1>CSDK for Lenovo devices</h1>
 * This class handles the CSDK library for the Lenovo devices, with the help of the sdk we can restrict various functionalities and security exceptions
 *
 * @author Allwin Johnson
 * @version CSDK 3.0
 * @since 2022-09-19
 */

class CSDKHandler @Inject constructor(private val context: Context) {

    private var csdkManager: CSDKManager = CSDKManager(context)
    private var componentName: ComponentName = DeviceSystemAdministrator.getComponentName(context)
    private var devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private var packageManager: PackageManager = context.packageManager

    private val isLenovoDevice: Boolean
        get() = Build.MANUFACTURER.equals("LENOVO")

    /*
	* check that the license is activated for csdk */
    val isCSDKActivated: Boolean
        get() = if (isLenovoDevice)
            csdkManager.isLicenseKeyEnabled(BuildConfig.APPLICATION_ID)
        else
            false

    private val licenseKey: String
        get() {
            var licenseKey = ""
            Timber.tag("CSDKHandler").v("MANUFACTURER %s", Build.MANUFACTURER)
            Timber.tag("CSDKHandler").v("MODEL %s", Build.MODEL.replace("Lenovo ", ""))
            Timber.tag("CSDKHandler").v("DEBUG ?  %s", BuildConfig.DEBUG)
            licenseKey = when (Build.MODEL.replace("Lenovo ", "")) {
                "TB-300XU" -> {
                    Timber.tag("CSDKHandler").v("M8 4th Gen")
                    BuildConfig.TB310XU
                }

                "TB-8505X" -> {
                    Timber.tag("CSDKHandler").v("M8 1st Gen")
                    BuildConfig.TB8505X
                }

                else -> ""
            }
            Timber.tag("CSDKHandler").v("License key $licenseKey")
            return licenseKey
        }

    fun activateCSDKLicense() {
        Timber.v("Device manufacturer " + Build.MANUFACTURER)

        if (Build.MANUFACTURER.equals("LENOVO")) {
            Timber.v("Initiating activation of CSDK license " + Build.MANUFACTURER)
            csdkManager.activateLicense(licenseKey)

        } else {
            Timber.e("Manufacture doesn't support : ${Build.MANUFACTURER}")
            Toast.makeText(context, "ERROR:111", Toast.LENGTH_SHORT).show()

        }
    }

    fun deactivateCSDKLicense() {

        csdkManager.deactivateLicense(licenseKey)
    }

    /**/
    private fun hideOrShowStatusBar(enabled: Boolean) { //status bar
        Timber.e("hideOrShowStatusBar")
        csdkManager.hideStatusBar(enabled)
        csdkManager.disableStatusBarNotification(enabled) // notification bar
        csdkManager.disableStatusBarPanel(enabled) // status bar pulling
    }

    private fun hideOrShowBackSoftKey(enabled: Boolean) { //back button
        Timber.e("hideOrShowBackSoftKey")
        csdkManager.hideBackSoftKey(enabled)
    }

    private fun hideOrShowHomeSoftKey(enabled: Boolean) { //home button
        Timber.e("hideOrShowHomeSoftKey")
        csdkManager.hideHomeSoftKey(enabled)
    }

    private fun hideOrShowRecentSoftKey(enabled: Boolean) { //recent button
        Timber.e("hideOrShowRecentSoftKey")
        csdkManager.hideMenuSoftKey(enabled)
    }

    private fun hideOrShowSettings(enabled: Boolean) { //settings
        Timber.e("hideOrShowSettings")

        val screenAssistantComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$ScreenAssitantActivity"
        )
        val dndComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$ZenModeSettingsActivity"
        ) // DND
        val eyeProtectionComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$NightDisplaySettingsActivity"
        ) // eye protection
        val colorModeComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$ScreenColorModeSettings"
        ) // color mode
        val screenCaptureComponentName = ComponentName(
            "com.lenovo.screencapture",
            "com.lenovo.screencapture.activity.SettingsActivityNew"
        ) // screen capture lenovo
        val screenCaptureMainComponentName = ComponentName(
            "com.lenovo.screencapture",
            "com.lenovo.screencapture.activity.MainActivity"
        ) // screen capture lenovo
        val installedAppDetailsComponentName = ComponentName(
            "com.lenovo.screencapture",
            "com.lenovo.screencapture.activity.SettingsActivityNew"
        )
        val guestUserComponentName = ComponentName(
            "com.android.wantjoin.settings",
            "com.android.wantjoin.settings.Settings\$UserSettingsActivity"
        )
        val developerComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity"
        ) // developer option
        val installedOptionComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.applications.InstalledAppDetails"
        ) // app info
        val emergencyOptionComponentName =
            ComponentName("com.android.phone", "com.android.phone.EmergencyDialer") // app info
        val shareChooserComponentName =
            ComponentName("android", "com.android.internal.app.ChooserActivity") // app info
        try {
            if (enabled) {
                csdkManager.setComponentEnabled(
                    screenAssistantComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    installedAppDetailsComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    guestUserComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    dndComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    eyeProtectionComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    colorModeComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    screenCaptureComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    screenCaptureMainComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    developerComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    installedOptionComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    emergencyOptionComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
                csdkManager.setComponentEnabled(
                    shareChooserComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    2
                )
            } else {
                csdkManager.setComponentEnabled(
                    screenAssistantComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    installedAppDetailsComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    guestUserComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    dndComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    eyeProtectionComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    colorModeComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    screenCaptureComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    screenCaptureMainComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    developerComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    installedOptionComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    emergencyOptionComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
                csdkManager.setComponentEnabled(
                    shareChooserComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Timber.tag("CSDKHandler").e(" hideOrShowSettings $enabled")
    }

    private fun hideOrShowFullscreen(enabled: Boolean) { //fullscreen

        Timber.e("hideOrShowFullscreen")

        csdkManager.fullScreenForever(enabled)
    }

    private fun hideOrEnableDataSharing(enabled: Boolean) { //data
        Timber.e("hideOrEnableDataSharing")

        Timber.tag("hideOrEnableDataSharing").e("enabled ? %s", enabled)
        if (enabled) {
            csdkManager.enableWifiHotspot_v3(false)
        }
        csdkManager.disallowWifiHotspot_v3(enabled)
        csdkManager.disallowBluetoothShare_v3(enabled)
        csdkManager.enableBluetoothtethering_v3(!enabled)
        csdkManager.disallowedBluetoothtethering_v3(enabled)
        csdkManager.disallowData_v3(enabled)
        csdkManager.enableData_v3(enabled)
    }

    private fun hideLockscreen(enabled: Boolean) { //lock screen
        Timber.e("hideLockscreen")

        csdkManager.setLockScreenMode(if (enabled) CSDKManager.LOCKSCREEN_MODE_NONE else CSDKManager.LOCKSCREEN_MODE_SWIPE)
    }

    fun hideOrEnableInstallation(enabled: Boolean) { // app installation
        Timber.e("hideOrEnableInstallation")

        csdkManager.disableInstallation(enabled)
    }

    private fun hideOrEnableMultiUser(enabled: Boolean) { // multi user
        Timber.e("hideOrEnableMultiUser")

        csdkManager.disallowMultiUser_v3(enabled)
    }

    private fun hideOrEnableUSB(enabled: Boolean) { // usb mode
        Timber.e("hideOrEnableUSB")

        csdkManager.disallowUsbMode_v3(enabled)
        csdkManager.disallowUsbDebugging_v3(enabled)
        csdkManager.enableDevMode_v3(!enabled)
        csdkManager.disallowDevMode_v3(enabled)
        csdkManager.enableUsbDebugging(!enabled)
        csdkManager.disallowUsbDebugging_v3(!enabled)
    }

    private fun setSleepTime(enabled: Boolean) { // disable sleep time change, set 5 min sleep time as default
        Timber.e("setSleepTime")

        Timber.e("setSleepTime $enabled")
        csdkManager.disallowSetSleepTimeout_V3(enabled)
        csdkManager.setSleepTimeout_V3(300000) //5min
    }

    private fun hideRecoveryMode(enabled: Boolean) { // recovery mode
        Timber.e("hideRecoveryMode")

        csdkManager.customRecovery_v3 = !enabled
        csdkManager.customFASTBOOT = !enabled
        csdkManager.customHARDRST = !enabled
        csdkManager.customCHARGE = !enabled
        csdkManager.enableSystemAutoUpdate(!enabled)
        if (!BuildConfig.DEBUG) {
            csdkManager.disallowFactoryReset_v3(enabled)
        }
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun hideOrEnableEveryApps(status: Boolean) { // usb mode
        if (!isLenovoDevice) {
            return
        }
        Timber.e("hideOrEnableEveryApps")

        if (status) {
        val packageName = mutableListOf<String>()
        val processedPackageNames = mutableListOf<String>()
        val packageInfos: MutableList<PackageInfo> = context.packageManager.getInstalledPackages(0)
        for (packageInfo in packageInfos) {
            Timber.tag("ApplicationInfo").v(
                "installed applications --> " + packageInfo.packageName + "   installed app name ------> " + context.packageManager
                    .getApplicationLabel(packageInfo.applicationInfo)
            )
            packageName.add("\"" + packageInfo.packageName + "\"")
            Timber.tag("ApplicationInfo").v(
                "installed app name ------> %s",
                context.packageManager.getApplicationLabel(packageInfo.applicationInfo)
            )

            Timber.tag("ApplicationInfo")
                .v(" ------ ------ ------- ------- ------- ------- ------ ------- ------ ")

            if (!CSDKConstants().ogM8InstalledApps.contains(packageInfo.packageName)) {
                processedPackageNames.add(packageInfo.packageName)
            }
        }
        Timber.tag("ApplicationInfo").v("installed application --> %s", packageName.toString())
        Timber.tag("ApplicationInfo")
            .v("Apps to be blacklisted --> %s", processedPackageNames.toString())
            csdkManager.displayBlacklist_v3 = processedPackageNames
        } else {
            Timber.tag("ApplicationInfo")
                .v("get display black list  --> %s", csdkManager.displayBlacklist_v3)
            csdkManager.removeDisplayBlacklist_v3(csdkManager.displayBlacklist_v3)
        }
    }

    private fun hideOrEnableSwitchLauncher(enabled: Boolean) {
        Timber.e("hideOrEnableSwitchLauncher")

        csdkManager.disallowSwitchLauncher_v3(enabled)
    }

    private fun hideOrEnableSystemSettings(enabled: Boolean) {

        Timber.e("hideOrEnableSystemSettings")

        csdkManager.disableApplicationManage_v3(enabled)
//        csdkManager.disallowApplicationPermissionsPage(enabled)
//        csdkManager.disallowSpecialAccessPermissionsPage(enabled)

        csdkManager.disallowLockScreenMode_v3(!enabled)
//        csdkManager.disallowSwitchLockScreenMode(!enabled)

    }

    private fun hideOrShowActions(enabled: Boolean) {
        hideOrShowStatusBar(enabled)
        hideOrShowBackSoftKey(enabled)
        hideOrShowHomeSoftKey(enabled)
        hideOrShowRecentSoftKey(enabled)


        // FIXME:  java.lang.NoSuchMethodError: No virtual method hideTaskBar(Z)
        //  in class Landroid/app/csdk/CSDKManager; or its super classes (declaration of 'android.app.csdk.CSDKManager'
        //  appears in /system/framework/framework.jar)

//        hideOrShowFullscreen(enabled) // not the imposter // commenting because it has compatibility with taskbar hiding case

        hideLockscreen(enabled)
        hideOrEnableMultiUser(enabled)
        hideOrShowSettings(enabled)
        hideOrEnableInstallation(enabled) // not the imposter

        hideOrEnableSystemSettings(enabled)
        if (!BuildConfig.DEBUG) {
            hideOrEnableUSB(enabled)
        }
        hideOrEnableDataSharing(enabled) // not the imposter

        hideRecoveryMode(enabled) // the imposter is in here.. [he is not in here]
        hideOrEnableEveryApps(enabled) // you peace of **😡.. found you

        setSleepTime(enabled)
        hideOrEnableSwitchLauncher(enabled)


    }

    /*-------------------------------------------------------------------------------------*/

    fun setDeviceOwner() {
        try {
            csdkManager.setDeviceOwner(BuildConfig.APPLICATION_ID + "/com.omang.app.utils.csdk.DeviceSystemAdministrator")

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun removeDeviceOwner() {
        csdkManager.removeDeviceOwner(BuildConfig.APPLICATION_ID)

    }

    fun isDeviceOwner(): Boolean {
        return devicePolicyManager.isDeviceOwnerApp(BuildConfig.APPLICATION_ID)
    }

    fun enableKioskMode(activity: Activity, enabled: Boolean) {
        if (enabled) {
            devicePolicyManager.setLockTaskPackages(
                componentName,
                arrayOf(BuildConfig.APPLICATION_ID, "com.android.settings")
            )
            if (devicePolicyManager.isLockTaskPermitted(BuildConfig.APPLICATION_ID)) {
                setUpHomeScreenLauncher()
                activity.startLockTask()
                hideOrShowActions(true)

            } else {
                Log.e("Not permitted", "task Lock")

            }
        } else {
            hideOrShowActions(false)
            activity.stopLockTask()
            exitHomeScreenLauncher()
            activity.moveTaskToBack(true)
            activity.finishAffinity()

        }
    }

    private fun setUpHomeScreenLauncher() {
        packageManager.setComponentEnabledSetting(
            ComponentName(context.applicationContext, LauncherActivity::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        setDefaultCosuPolicies(true, context)
    }

    private fun exitHomeScreenLauncher() {
        setDefaultCosuPolicies(false, context)
    }

    private fun setDefaultCosuPolicies(active: Boolean, context: Context) {
        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        if (active) {
            // set COSU activity as home intent receiver so that it is started
            // on reboot
            devicePolicyManager.addPersistentPreferredActivity(
                componentName, intentFilter, ComponentName(
                    BuildConfig.APPLICATION_ID, LauncherActivity::class.java.name
                )
            )
        } else {
            if (devicePolicyManager.isAdminActive(componentName) && isDeviceOwner()) {
                devicePolicyManager.clearPackagePersistentPreferredActivities(
                    componentName,
                    BuildConfig.APPLICATION_ID
                )
                packageManager.setComponentEnabledSetting(
                    ComponentName(context.applicationContext, LauncherActivity::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    fun wifi(wifiStatus : Boolean){
        csdkManager.enableWifi_v3(wifiStatus)

    }

    fun data(dataStatus : Boolean){
        csdkManager.enableData_v3(dataStatus)

    }

    fun getDeviceInfo(deviceConst: CSDKConstants.Device): String {
        return csdkManager.getDeviceInfo(deviceConst.value)
    }
}