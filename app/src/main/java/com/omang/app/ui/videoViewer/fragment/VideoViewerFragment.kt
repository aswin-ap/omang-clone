package com.omang.app.ui.videoViewer.fragment

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.omang.app.R
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.databinding.FragmentVideoViewerBinding
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.videoViewer.viewmodel.UIState
import com.omang.app.ui.videoViewer.viewmodel.VideoViewerViewModel
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class VideoViewerFragment : Fragment() {
    private lateinit var _binding: FragmentVideoViewerBinding
    private val viewModel: VideoViewerViewModel by viewModels()
    private val args: VideoViewerFragmentArgs by navArgs()
    private lateinit var dAE: AnalyticsEntity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentVideoViewerBinding.inflate(inflater, container, false)

        dAE = AnalyticsEntity(
            resourceId = args.resourceId,
            lessonId = if (args.lessonId != 0) args.lessonId else null,
            classroomId = if (args.classroomId != 0) args.classroomId else null,
            unitId = if (args.unitId != 0) args.unitId else null,
            menu = args.menu
        )

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        bindView()
        observer()
    }

    private fun bindView() {
        Timber.v("video file url : ${args.file}")
        viewModel.fetchFile(requireContext(), args.file, args.menu)

        _binding.apply {
            playerView.setFullscreenButtonClickListener {
                viewModel.isFullScreen = !viewModel.isFullScreen
                onChangeOrientation()
                (activity as HomeActivity).setDeviceFullScreen(viewModel.isFullScreen)
                actionBarView.visibility = if (viewModel.isFullScreen) View.GONE else View.VISIBLE
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onPause() {
        super.onPause()
        if (!dAE.startTime.isNullOrEmpty()) { // making sure that the data inserting has star-time with it
            dAE.endTime = ViewUtil.getUtcTimeWithMSec()
            viewModel.insertWatchData(dAE) // insert data to analytics table for explore websites
        }

        val orientation = (requireActivity() as HomeActivity).resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            (requireActivity() as HomeActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun observer() {
        viewModel.uiState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is UIState.Initialized -> {
                    viewModel.preparePlayer(requireContext(), _binding.playerView)
                    dAE.startTime = ViewUtil.getUtcTimeWithMSec()

                }

                is UIState.PlayerPrepared -> {
                    val exoPlayer = state.player
                }

                is UIState.OnError -> {
                    showToast(state.exception.message.toString())
                    showErrorUI()
                }
            }
        })
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun onChangeOrientation() {
        val orientation = (requireActivity() as HomeActivity).resources.configuration.orientation
        Timber.e("vH ${viewModel.vH} : vW ${viewModel.vW}")

        if (viewModel.vW > viewModel.vH) { //landscape video
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                (requireActivity() as HomeActivity).requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                (requireActivity() as HomeActivity).requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } else if (viewModel.vH > viewModel.vW) { // portrait video
            /* if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                 (requireActivity() as HomeActivity).requestedOrientation =
                     ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

             }*/
        }
    }

    private fun showVideo() {
        _binding.apply {
            playerView.visible()
            tvError.gone()
        }
    }

    private fun showErrorUI() {
        _binding.apply {
            playerView.gone()
            tvError.visible()
            tvError.text = getString(R.string.file_loading_error)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.releasePlayer()
        (activity as HomeActivity).setDeviceFullScreen(false)
    }

}