package com.omang.app.ui.camera.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.omang.app.R
import com.omang.app.databinding.ActivityPhotoBinding
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.camera.CameraUtil
import com.omang.app.utils.camera.RenameDialog
import com.omang.app.ui.camera.viewmodel.CameraViewModel
import com.omang.app.utils.FileUtil
import com.omang.app.utils.FullscreenHelper
import com.omang.app.utils.extensions.invisible
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.properties.Delegates


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var screenAspectRatio by Delegates.notNull<Int>()
    private var rotation by Delegates.notNull<Int>()
    private lateinit var metrics: DisplayMetrics
    private var isLensFacingBack = true
    private var currentTime: Long? = null
    private lateinit var photoPathFile: File
    private lateinit var inAppFilePath: File
    private lateinit var videoPath: File
    private var isVideoMode = false
    private var countDownJob: Job? = null
    private lateinit var shutterAnimation: Animation
    private var toProfileUpload = false
    private var croppedImagePath: String? = null
    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullscreenHelper.enableFullscreen(this)

        binding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        observeData()

        if (isAllPermissionsGranted())
            initCamera()
        else
            requestPermissions()

        val intent = intent
        val flag = intent.getBooleanExtra("profileUpload", false)
        if (flag) {

            binding.ivSwitchMode.invisible()
            binding.tvModeText.invisible()
            toProfileUpload = true
        } else {

        }

    }

    /**
     * Initializes all global variables and setup click events
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        metrics = applicationContext.resources.displayMetrics
        screenAspectRatio = CameraUtil.getAspectRatio(metrics.widthPixels, metrics.heightPixels)
        shutterAnimation =
            AnimationUtils.loadAnimation(this@CameraActivity, R.anim.shutter_animation)
        rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            display!!.rotation
        else
            windowManager.defaultDisplay.rotation

        binding.apply {
            ivSwitchCamera.setOnClickListener {
                //Toggle the camera
                cameraSelector =
                    if (isLensFacingBack) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                isLensFacingBack = !isLensFacingBack
                if (isVideoMode) {
                    initVideo()
                } else
                    initCamera()
            }

            ivCapture.setOnTouchListener { _, event ->
                if (isVideoMode)
                    captureVideo(event)
                else
                    takePicture(event, imageCapture, toProfileUpload)
                true
            }

            ivSwitchMode.setOnClickListener {
                toggleMode()
            }

            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    /**
     * Observes the countdown timer from the ViewModel
     */
    private fun observeData() {
        viewModel.countdownTimer.observe(this) { time ->
            //if countdown is 0, cancels the recording
            if (time != "0 : 00")
                binding.tvTimer.text = time
            else {
                recording?.close()
                countDownJob?.cancel()
                resetTimer()
            }
        }
    }

    /**
     * Toggles between the camera modes (Photo and video)
     */
    private fun toggleMode() {
        if (!isVideoMode) {
            initVideo()
        } else {
            initCamera()
        }

        binding.tvTimer.visibility = if (!isVideoMode) View.VISIBLE else View.GONE
        binding.ivSwitchMode.setImageDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                if (!isVideoMode) R.drawable.ic_phone_camera else R.drawable.ic_video_cam
            )
        )
        binding.tvModeText.text = (if (!isVideoMode) "Switch to photo" else "Switch to video")
        isVideoMode = !isVideoMode
    }

    /**
     * Captures the image using the "ImageCapture" class and saves the image in Internal storage/Omang/Pictures
     * folder (Later the image will be deleted) and sends the image to crop activity
     */
    private fun takePicture(
        event: MotionEvent?,
        imageCapture: ImageCapture?,
        toProfileUpload: Boolean
    ) {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            currentTime = System.currentTimeMillis()

            binding.ivCapture.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_record_active
                )
            )

            val imageCapture = imageCapture ?: return

            ///Path of the image to be saved on capture
            photoPathFile = File(
                FileUtil.getTempPhotoPath(this@CameraActivity),
                "${currentTime.toString()}-photo.jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(photoPathFile).build()

            ///sets the shutter animation
            binding.PreviewView.startAnimation(shutterAnimation)

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Timber.d("Photo capture succeeded")

                        if (toProfileUpload) {
                            ///Launches the CropActivity
                            lifecycleScope.launch {
                                cropResultLauncher.launch(
                                    CropImage.activity(Uri.fromFile(photoPathFile))
                                        .setCropMenuCropButtonTitle("Crop and Upload to Profile")
                                        .getIntent(this@CameraActivity)
                                )
                            }
                        } else {
                            ///Launches the CropActivity
                            lifecycleScope.launch {
                                cropResultLauncher.launch(
                                    CropImage.activity(Uri.fromFile(photoPathFile))
                                        .setCropMenuCropButtonTitle("Crop and Save")
                                        .getIntent(this@CameraActivity)
                                )
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Timber.e("Photo capture failed: ${exception.message}")
                    }
                })

        } else if (event?.action == MotionEvent.ACTION_UP) {
            binding.ivCapture.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_record
                )
            )
        }
    }

    /**
     * Captures the image using the "ImageCapture" class and saves the image in android-> app ->Movies
     * folder
     */
    private fun captureVideo(event: MotionEvent?) {
        if (event?.action == MotionEvent.ACTION_DOWN) {

            val videoCapture = this.videoCapture ?: return

            val curRecording = recording
            if (curRecording != null) {
                // If already running, Stop the current recording session.
                curRecording.stop()
                recording = null
                return
            }

            binding.ivCapture.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_record_active
                )
            )

            //Launches the countdown
            lifecycleScope.launch {
                countDownJob = viewModel.startCountDown()
            }

            currentTime = System.currentTimeMillis()
            videoPath = File(
                FileUtil.getMoviesPathInAppFolder(this@CameraActivity),
                "${currentTime.toString()}-video.mp4"
            )

            val outputFileOptions = FileOutputOptions.Builder(videoPath).build()

            recording = videoCapture.output
                .prepareRecording(this, outputFileOptions)
                .apply {
                    if (PermissionChecker.checkSelfPermission(
                            this@CameraActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) ==
                        PermissionChecker.PERMISSION_GRANTED
                    ) {
                        withAudioEnabled()
                    }
                }
                .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            binding.apply {
                                ivSwitchCamera.isEnabled = false
                                cameraOptions.alpha = 0.2f
                                ivSwitchMode.gone()
                                tvModeText.gone()
                            }
                        }

                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                resetTimer()

                                //Shows rename dialog and replaces the video name
                                RenameDialog(
                                    this@CameraActivity,
                                    false,
                                    videoPath.nameWithoutExtension
                                ) { fileName ->
                                    viewModel.renameVideo(
                                        File(
                                            FileUtil.getMoviesPathInAppFolder(this@CameraActivity),
                                            "$fileName.mp4"
                                        ),
                                        videoPath
                                    ) {
                                        showToast("Video Renamed Successfully")
                                    }
                                }.show(
                                    supportFragmentManager,
                                    "rename_dialog"
                                )

                            } else {
                                recording?.close()
                                recording = null
                                showToast("Something went wrong while capturing video")
                            }

                            binding.apply {
                                ivSwitchCamera.isEnabled = true
                                cameraOptions.alpha = 1.0f
                                ivSwitchMode.visible()
                                tvModeText.visible()
                                ivCapture.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        applicationContext,
                                        R.drawable.ic_record
                                    )
                                )

                            }
                        }
                    }
                }
        }
    }

    /**
     * Reset the UI of the timer textview
     */
    private fun resetTimer() {
        binding.tvTimer.apply {
            text = ""
            gone()
        }
    }

    ///Initialize and starts the camera
    private fun initCamera() {
        cameraProviderFuture.addListener({
            processCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setSurfaceProvider(binding.PreviewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(rotation)
                .build()

            try {
                processCameraProvider.unbindAll()

                processCameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exe: Exception) {
                Timber.d("Exception while binding camera: $exe")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    ///Initializes the camera with videomode
    private fun initVideo() {
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            processCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setSurfaceProvider(binding.PreviewView.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                // Unbind use cases before rebinding
                processCameraProvider.unbindAll()

                // Bind use cases to camera
                processCameraProvider
                    .bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch (exc: Exception) {
                Timber.d("Exception while binding camera: $exc")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() =
        activityResultLauncher.launch(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2)


    private fun isAllPermissionsGranted(): Boolean {

        val per =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2

        return per.all {
            ContextCompat.checkSelfPermission(
                baseContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                val per =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2
                if (it.key in per && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                showToast(getString(R.string.permission_denied))
            } else
                initCamera()
        }

    /**
     * Returns the cropped image and saves the image into the in-app folder (Pictures) and
     * deletes the old image. if the user doesn't crop the image, the taken photo will be deleted.
     */
    private val cropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val cropResultData = CropImage.getActivityResult(result?.data)
                val iStream = contentResolver.openInputStream(cropResultData.uri)
                val inputData = iStream?.let { getBytes(it) }

                inAppFilePath = File(
                    FileUtil.getPicturesPathInAppFolder(this@CameraActivity),
                    currentTime.toString() + "-photo" + ".jpg"
                )


                viewModel.saveImage(inAppFilePath, inputData) {
                    croppedImagePath = inAppFilePath.absolutePath
                    if (toProfileUpload) {

                        val resultIntent = Intent()
                        Timber.d("image file $croppedImagePath")
                        resultIntent.putExtra("image_path", croppedImagePath.toString())
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()

                    } else {
                        /**
                         * Deletes the Camerax image and Shows the rename dialog after the picture is saved
                         */
                        photoPathFile.delete()
                        RenameDialog(
                            this@CameraActivity,
                            true,
                            inAppFilePath.nameWithoutExtension
                        ) { fileName ->
                            viewModel.renameImage(
                                File(
                                    FileUtil.getPicturesPathInAppFolder(this@CameraActivity),
                                    "$fileName.jpg"
                                ), inAppFilePath
                            ) {
                                showToast("Imaged Renamed Successfully")
                            }
                        }.show(
                            supportFragmentManager,
                            "rename_dialog"
                        )
                    }

                }

            } else {
                photoPathFile.delete()
            }
        }


    override fun onResume() {
        super.onResume()
        if (isAllPermissionsGranted())
            initCamera()
        else
            requestPermissions()
    }


    companion object {
        val permissions1 =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).toTypedArray()

        val permissions2 =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            ).toTypedArray()

        private fun getBytes(inputStream: InputStream): ByteArray? {
            val byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            return byteBuffer.toByteArray()
        }
    }

    //Removes the all callbacks
    override fun onDestroy() {
        super.onDestroy()
        processCameraProvider.unbindAll()
        countDownJob.let {
            it?.cancel()
        }
    }
}