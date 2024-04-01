package com.omang.app.ui.gallery.fragments

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.omang.app.R
import com.omang.app.databinding.FragmentGalleryBinding
import com.omang.app.ui.camera.activity.CameraActivity
import com.omang.app.ui.gallery.adapter.GalleryViewPagerAdapter
import com.omang.app.ui.gallery.viewmodel.GalleryViewModel
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.utils.FileUtil
import com.omang.app.utils.Resource
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class GalleryFragment : Fragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var viewPagerAdapter: GalleryViewPagerAdapter
    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var contentResolver: ContentResolver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        bindView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        if (isAllPermissionsGranted())
            getMedias()
        else
            requestPermissions()

        registerPagerChangeCallback()

    }

    override fun onResume() {
        getMedias()
        super.onResume()
    }

    private fun registerPagerChangeCallback() {
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setOnPageSelected(position)
            }
        })
    }

    private fun observeData() {
        viewModel.mediaStatus.observe(viewLifecycleOwner) { result ->
            lifecycleScope.launch {
                when (result) {
                    is Resource.Success -> {
                        binding.apply {
                            lytProgress.invisible()
                            viewpager.visible()
//                            viewpager.adapter = viewPagerAdapter
                        }
                    }

                    is Resource.Error -> {
                        showToast(result.message.toString())
                        binding.apply {
                            viewpager.invisible()
                            lytProgress.invisible()
                        }
                    }

                    is Resource.Loading -> {
                        binding.apply {
                            viewpager.invisible()
                            lytProgress.visible()
                        }
                    }
                }
            }
        }
    }

    //fetches all images,videos and documents from the device
    private fun getMedias() = lifecycleScope.launch {
        viewModel.fetchGallery(
            FileUtil.getPicturesPathInAppFolder(requireContext()),
            FileUtil.getMoviesPathInAppFolder(requireContext()),
            FileUtil.getDocumentsPathInAppFolder(requireContext()),
            contentResolver
        )
    }

    private fun isAllPermissionsGranted(): Boolean {

        val per =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2

        return per.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() = activityResultLauncher.launch(
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2
    )

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            val per =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2

            permissions.entries.forEach {
                if (it.key in per && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                showToast(getString(R.string.permission_denied))
            } else
                getMedias()
        }

    private fun bindView() {
        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, R.string.my_gallery)
        viewPagerAdapter = GalleryViewPagerAdapter(this@GalleryFragment)
        contentResolver = requireActivity().contentResolver
        binding.apply {
            viewpager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = viewPagerAdapter
                currentItem = 0
                offscreenPageLimit = 1
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setOnPageSelected(position)
                    }
                })
            }

            btnPhotos.setOnClickListener {
                viewpager.currentItem = 0
            }

            btnVideos.setOnClickListener {
                viewpager.currentItem = 1
            }

            btnDocs.setOnClickListener {
                viewpager.currentItem = 2
            }
        }
    }

    //updates the selected buttons
    private fun setOnPageSelected(position: Int) {
        when (position) {
            0 -> {
                setEnableSelectedButton(binding.btnPhotos)
                resetButtonUnSelected(binding.btnVideos, binding.btnDocs)
            }

            1 -> {
                setEnableSelectedButton(binding.btnVideos)
                resetButtonUnSelected(binding.btnPhotos, binding.btnDocs)
            }

            2 -> {
                setEnableSelectedButton(binding.btnDocs)
                resetButtonUnSelected(binding.btnVideos, binding.btnPhotos)
            }
        }
    }

    private fun resetButtonUnSelected(btn1: Button, btn2: Button) {
        btn1.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.card_txt_primary
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
        }
        btn2.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.card_txt_primary
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
        }
    }

    private fun setEnableSelectedButton(button: Button) {
        button.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.deep_blue)
            elevation = 5f
            bringToFront()
        }

    }


//    private fun resetButtonUnSelected(btn1: Button, btn2: Button) {
//        btn1.run {
//            setTextColor(
//                ContextCompat.getColor(
//                    requireActivity(),
//                    R.color.white
//                )
//            )
//            backgroundTintList =
//                ContextCompat.getColorStateList(requireActivity(), R.color.deep_blue)
//        }
//        btn2.run {
//            setTextColor(
//                ContextCompat.getColor(
//                    requireActivity(),
//                    R.color.white
//                )
//            )
//            backgroundTintList =
//                ContextCompat.getColorStateList(requireActivity(), R.color.deep_blue)
//        }
//    }

    //updates the color of button
//    private fun setEnableSelectedButton(button: Button) {
//        button.run {
//            setTextColor(
//                ContextCompat.getColor(
//                    requireActivity(),
//                    R.color.color_grey
//                )
//            )
//            backgroundTintList =
//                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
//        }
//    }

    companion object {
        private val permissions1 = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).toTypedArray()

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val permissions2 = listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        ).toTypedArray()
    }
}