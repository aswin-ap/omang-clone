package com.omang.app.ui.registration.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.omang.app.R
import com.omang.app.databinding.ActivityCustomScannerBinding
import com.omang.app.utils.FullscreenHelper
import java.util.Random

/**
 * Custom Activity extending from Activity to display a custom layout form scanner view.
 */
class BarcodeScannerActivity() :
    AppCompatActivity() {
    private val capture: CaptureManager? = null
    private lateinit var scanType: String

    companion object {
        const val BARCODE_SCANNER_DATA = "data"
        const val SCANNER_TYPE = "scanner_type"
    }

    private lateinit var beepManager: BeepManager
    private lateinit var binding: ActivityCustomScannerBinding
    private val callback: BarcodeCallback = BarcodeCallback { result ->
        beepManager.playBeepSoundAndVibrate()
        val intent = Intent().apply {
            putExtra(BARCODE_SCANNER_DATA, result.text)
            putExtra(SCANNER_TYPE, scanType)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullscreenHelper.enableFullscreen(this)
        setContentView(R.layout.activity_custom_scanner)
        binding = ActivityCustomScannerBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        intent.getStringExtra(SCANNER_TYPE)?.let {
            scanType = it
        }

        binding.zxingBarcodeScanner.apply {
            val formats: Collection<BarcodeFormat> =
                listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
            decoderFactory = DefaultDecoderFactory(formats)
            initializeFromIntent(intent)
            decodeContinuous(callback)
        }
        beepManager = BeepManager(this)
        changeMaskColor()
        changeLaserVisibility(true)
    }

    override fun onResume() {
        super.onResume()
        binding.zxingBarcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.zxingBarcodeScanner.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return binding.zxingBarcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(
            keyCode,
            event
        )
    }

    private fun changeMaskColor() {
        val rnd = Random()
        val color = Color.argb(100, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        binding.zxingBarcodeScanner.viewFinder.setMaskColor(color)
    }

    private fun changeLaserVisibility(visible: Boolean) {
        binding.zxingBarcodeScanner.viewFinder.setLaserVisibility(visible)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}