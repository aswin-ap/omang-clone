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
 * @version CSDK 6.0
 * @since 2023-10-10
 */

class CSDKHandler @Inject constructor(private val context: Context) {

    private var csdkManager: CSDKManager = CSDKManager(context)
    private var componentName: ComponentName = DeviceSystemAdministrator.getComponentName(context)
    private var devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private var packageManager: PackageManager = context.packageManager


    /*
	* check that the license is activated for csdk */
    val isCSDKActivated: Boolean
        get() =
            csdkManager.isLicenseKeyEnabled(BuildConfig.APPLICATION_ID)

    private val licenseKey: String
        get() {
            var licenseKey = ""
            Timber.tag("CSDKHandler").v("MANUFACTURER %s", Build.MANUFACTURER)
            Timber.tag("CSDKHandler").v("MODEL %s", Build.MODEL.replace("Lenovo ", ""))
            Timber.tag("CSDKHandler").v("DEBUG ?  %s", BuildConfig.DEBUG)
            licenseKey = when (Build.MODEL.replace("Lenovo ", "")) {
                "TB310XU" -> {
                    Timber.tag("CSDKHandler").v("M9 1st Gen")
                    BuildConfig.TB310XU
                }

                "TB300XU" -> {
                    Timber.tag("CSDKHandler").v("M8 1st Gen")
                    BuildConfig.TB300XU

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

    fun hideOrShowRecentSoftKey(enabled: Boolean) { //recent button
        Timber.e("hideOrShowRecentSoftKey")
        csdkManager.hideMenuSoftKey(enabled)
    }

    fun hideOrShowBottomNav(enabled: Boolean) { //system bottom nav
        Timber.e("hideOrShowBottomNav")
        csdkManager.hideTaskBar(enabled)
    }

    fun hideOrShowSettings(enabled: Boolean) { //settings
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

        val guestUserComponentName = ComponentName(
            "com.android.wantjoin.settings",
            "com.android.wantjoin.settings.Settings\$UserSettingsActivity"
        )
        val developerComponentName = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity"
        ) // developer option
        val installedAppDetails = ComponentName(
            "com.android.settings",
            "com.android.settings.applications.InstalledAppDetails"
        ) // app info
        val emergencyOptionComponentName =
            ComponentName("com.android.phone", "com.android.phone.EmergencyDialer") // app info
        val shareChooserComponentName =
            ComponentName("android", "com.android.internal.app.ChooserActivity") // app info
        val searchLauncher =
            ComponentName(
                "com.tblenovo.launcher",
                "com.android.searchlauncher.SearchLauncher"
            ) // app info
        try {
            /*            csdkManager.setComponentEnabled(
                            screenAssistantComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )

                        csdkManager.setComponentEnabled(
                            guestUserComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )
                        csdkManager.setComponentEnabled(
                            dndComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )
                        csdkManager.setComponentEnabled(
                            eyeProtectionComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )
                        csdkManager.setComponentEnabled(
                            colorModeComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )
                        csdkManager.setComponentEnabled(
                            screenCaptureComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )
                        csdkManager.setComponentEnabled(
                            screenCaptureMainComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )
                        csdkManager.setComponentEnabled(
                            developerComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            if (enabled) 2 else 0
                        )*/
            csdkManager.setComponentEnabled(
                installedAppDetails,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                if (enabled) 2 else 0
            )
            csdkManager.setComponentEnabled(
                emergencyOptionComponentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                if (enabled) 2 else 0
            )

            /* csdkManager.setComponentEnabled(
                 searchLauncher,
                 PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                 if (enabled) 2 else 0
             )*/ // hiding this will make the launcher crashes
            /*         csdkManager.setComponentEnabled(
                         shareChooserComponentName,
                         PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                         if (enabled) 2 else 0
                     )*/

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
            csdkManager.enableWifiHotspotV3(false)
        }
        csdkManager.disallowWifiHotspotV3(enabled)
        csdkManager.disallowBluetoothShareV3(enabled)
        csdkManager.enableBluetoothTetheringV3(!enabled)
        csdkManager.disallowBluetoothTetheringV3(enabled)
        csdkManager.disallowDataV3(enabled)
//        csdkManager.enableDataV3(enabled)
    }

    private fun hideLockscreen(enabled: Boolean) { //lock screen
        Timber.e("hideLockscreen")

        csdkManager.setLockScreenMode(if (enabled) CSDKManager.LOCKSCREEN_MODE_NONE else CSDKManager.LOCKSCREEN_MODE_SWIPE)
    }

    fun hideOrEnableInstallation(enabled: Boolean) { // app installation
        Timber.e("hideOrEnableInstallation")
        csdkManager.disableInstallation(enabled)
    }

    private fun hideOrEnableSystemSettings(enabled: Boolean) {

        Timber.e("hideOrEnableSystemSettings")

        csdkManager.disableApplicationManageV3(enabled)
        csdkManager.disallowApplicationPermissionsPage(enabled)
        csdkManager.disallowSpecialAccessPermissionsPage(enabled)

        csdkManager.disallowLockScreenModeV3(!enabled)
        csdkManager.disallowSwitchLockScreenMode(!enabled)

    }

    private fun hideOrEnableMultiUser(enabled: Boolean) { // multi user
        Timber.e("hideOrEnableMultiUser")
        csdkManager.disallowMultiUserV3(enabled)
    }

    private fun hideOrEnableUSB(enabled: Boolean) { // usb mode
        Timber.e("hideOrEnableUSB")

        csdkManager.disallowUsbModeV3(enabled)
        csdkManager.disallowUsbDebuggingV3(enabled)
        csdkManager.enableDevModeV3(!enabled)
        csdkManager.disallowDevModeV3(enabled)
        csdkManager.enableUsbDebugging(!enabled)
        csdkManager.disallowUsbDebuggingV3(!enabled)
    }

    private fun setSleepTime(enabled: Boolean) { // disable sleep time change, set 5 min sleep time as default
        Timber.e("setSleepTime $enabled")
        csdkManager.disallowSetSleepTimeoutV3(enabled)
        csdkManager.setSleepTimeoutV3(300000) //5min
    }

    private fun hideRecoveryMode(enabled: Boolean) { // recovery mode
        Timber.e("hideRecoveryMode")

        csdkManager.isCustomRecoveryV3 = !enabled // not the imposter
        csdkManager.isCustomFastBoot = !enabled // not the imposter
        csdkManager.isCustomHardRst = !enabled // not the imposter
        csdkManager.isCustomCharge = !enabled // not the imposter
        csdkManager.enableSystemAutoUpdate(!enabled) // not the imposter
        if (!BuildConfig.DEBUG) {
            csdkManager.disallowFactoryResetV3(enabled) // not the imposter
        }

        // wait?..what?..this is where he suppose to be

    }

    @SuppressLint("BinaryOperationInTimber")
    private fun hideOrEnableEveryApps(status: Boolean) { // usb mode
        Timber.e("hideOrEnableEveryApps")
        if (status) {
            val packageName = mutableListOf<String>()
            val processedPackageNames = mutableListOf<String>()
            val packageInfos: MutableList<PackageInfo> =
                context.packageManager.getInstalledPackages(0)
            for (packageInfo in packageInfos) {
                /* Timber.tag("ApplicationInfo").v(
                     "installed applications --> " + packageInfo.packageName + "   installed app name ------> " + context.packageManager
                         .getApplicationLabel(packageInfo.applicationInfo)
                 )*/
                packageName.add("\"" + packageInfo.packageName + "\"")
                /* Timber.tag("ApplicationInfo").v(
                     "installed app name ------> %s",
                     context.packageManager.getApplicationLabel(packageInfo.applicationInfo)
                 )*/

                /*  Timber.tag("ApplicationInfo")
                      .v(" ------ ------ ------- ------- ------- ------- ------ ------- ------ ")
                      */
                when (Build.VERSION.SDK_INT) {
                    Build.VERSION_CODES.S, Build.VERSION_CODES.S_V2 -> { // Android 12
                        if (!CSDKConstants().ogM9Android12InstalledApps.contains(packageInfo.packageName)) {
                            processedPackageNames.add(packageInfo.packageName)
                        }
                    }

                    Build.VERSION_CODES.TIRAMISU -> { // Android 13
                        if (!CSDKConstants().ogM9Android13InstalledApps.contains(packageInfo.packageName)) {
                            processedPackageNames.add(packageInfo.packageName)
                        }
                    }
                }
            }

            Timber.tag("ApplicationInfo").v("installed application --> %s", packageName.toString())
            Timber.tag("ApplicationInfo")
                .v("Apps to be blacklisted --> %s", processedPackageNames.toString())
            csdkManager.displayBlacklistV3 = processedPackageNames

        } else {
            Timber.tag("ApplicationInfo")
                .v("get display black list  --> %s", csdkManager.displayBlacklistV3)
            try {
                csdkManager.removeDisplayBlacklistV3(csdkManager.displayBlacklistV3)

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    private fun hideOrEnableSwitchLauncher(enabled: Boolean) {
        Timber.e("hideOrEnableSwitchLauncher $enabled" )
        csdkManager.disallowSwitchLauncherV3(enabled)

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
            activity.stopLockTask()
            exitHomeScreenLauncher()
            hideOrShowActions(false)
            activity.moveTaskToBack(true)
            activity.finishAffinity()
            activity.finish() // immediately kill the app

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

    fun installApk(apkPath: File) { //recent button
        Timber.e("hideOrShowRecentSoftKey")
        csdkManager.installPackage(apkPath.toString())
    }

    fun wifi(wifiStatus: Boolean) {
        csdkManager.enableWifiV3(wifiStatus)

    }

    fun data(dataStatus: Boolean) {
        csdkManager.enableDataV3(dataStatus)

    }

    fun getDeviceInfo(deviceConst: CSDKConstants.Device): String {
        return csdkManager.getDeviceInfo(deviceConst.value)
    }

    fun test(enabled: Boolean) {
/*
        hideOrShowStatusBar(enabled)
        hideOrShowBackSoftKey(enabled)
        hideOrShowHomeSoftKey(enabled)
        hideOrShowRecentSoftKey(enabled)
        hideOrShowBottomNav(enabled)
*/
        csdkManager.disableApplicationManageV3(false)

        csdkManager.disallowApplicationPermissionsPage(false)
        csdkManager.disallowSpecialAccessPermissionsPage(false)

/*        csdkManager.disallowLockScreenModeV3(!enabled)
        csdkManager.disallowSwitchLockScreenMode(!enabled)*/

        csdkManager.disallowSetSleepTimeoutV3(false)
        csdkManager.setSleepTimeoutV3(300000) //5min

    }

    fun hideOrShowActions(enabled: Boolean) {
        Timber.e("hideOrShowActions")
        hideOrShowStatusBar(enabled)
        hideOrShowBackSoftKey(enabled)
        hideOrShowHomeSoftKey(enabled)
        hideOrShowRecentSoftKey(enabled)

        hideOrShowBottomNav(enabled)

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


}
