package com.omang.app.ui.imageViewer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.databinding.FragmentImageViewerBinding
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.imageViewer.viewmodel.ImageViewModel
import com.omang.app.utils.FileUtil
import com.omang.app.utils.ViewUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageViewerFragment : Fragment() {

    private lateinit var binding: FragmentImageViewerBinding
    private val viewModel: ImageViewModel by viewModels()

    private val args: ImageViewerFragmentArgs by navArgs()
    private var isFullScreen: Boolean = false

    private lateinit var dAE: AnalyticsEntity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImageViewerBinding.inflate(inflater, container, false)
        dAE = AnalyticsEntity(
            resourceId = args.resourceId,
            lessonId = if (args.lessonId != 0) args.lessonId else null,
            classroomId = if (args.classroomId != 0) args.classroomId else null,
            unitId = if (args.unitId != 0) args.unitId else null,
            menu = args.menu,
            startTime = ViewUtil.getUtcTimeWithMSec()
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        loadImage()
    }

    override fun onPause() {
        super.onPause()
        if (!dAE.startTime.isNullOrEmpty()) { // making sure that the data inserting has star-time with it
            dAE.endTime = ViewUtil.getUtcTimeWithMSec()
            viewModel.insertWatchData(dAE)
        }
    }

    private fun bindView() {
        binding.ivImage.setOnClickListener {
            isFullScreen = !isFullScreen
            (activity as HomeActivity).setDeviceFullScreen(isFullScreen)
        }
        binding.actionBarView.visibility = if (isFullScreen) View.GONE
        else View.VISIBLE
    }

    private fun loadImage() {
        if (args.menu == DBConstants.AnalyticsMenu.GALLERY.value) {
            binding.ivImage.load(args.file)
        } else {
            FileUtil.getFileNameFromUrl(args.file).also {
                FileUtil.getFileFromFileName(requireContext(), it)?.let { file ->
                    binding.ivImage.load(file.absolutePath)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as HomeActivity).setDeviceFullScreen(false)
    }
}