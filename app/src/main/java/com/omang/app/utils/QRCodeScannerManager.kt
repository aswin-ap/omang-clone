package com.omang.app.utils

import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanOptions


class QRCodeScannerManager {

    suspend fun startScan(launcher: ActivityResultLauncher<ScanOptions>) =
        launcher.launch(qrCodeScanner)

    companion object {
        val qrCodeScanner = ScanOptions().apply {
            setPrompt("Place a barcode inside the viewfinder rectangle to scan it.")
            setBeepEnabled(true)
            setOrientationLocked(true)
            captureActivity = CaptureActivity::class.java
        }
    }

}