package com.omang.app.ui.myLibrary.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.omang.app.R
import com.omang.app.databinding.FragmentMyLibraryBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myLibrary.adapter.MyLibraryViewPagerAdapter
import com.omang.app.ui.myLibrary.viewmodel.MyLibraryViewModel
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.setLastSyncedOn
import timber.log.Timber

class MyLibraryFragment : Fragment() {

    private lateinit var binding: FragmentMyLibraryBinding
    private lateinit var viewPagerAdapter: MyLibraryViewPagerAdapter
    private lateinit var dialog: Dialog


    private val viewModel: MyLibraryViewModel by hiltNavGraphViewModels(R.id.navigation_library)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        observer()
    }

    private fun bindView() {

        dialog = Dialog(requireContext(),R.style.mDialogTheme)

        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, R.string.my_library)

        viewPagerAdapter = MyLibraryViewPagerAdapter(this@MyLibraryFragment)
        updateButtonStatus(binding.btnDocs, binding.btnVideos)

        viewModel.fetchLastUpdateTime()

        binding.apply {
            viewpager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = viewPagerAdapter
                currentItem = 0
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setOnPageSelected(position)
                    }
                })
            }
            btnDocs.setOnClickListener {
                viewpager.currentItem = 0
            }
            btnVideos.setOnClickListener {
                viewpager.currentItem = 1
            }
            btnSyncApi.setOnClickListener {
                viewModel.getLibraryData()
            }
        }
    }

    private fun setUpdateArrow() {
        binding.apply {
            if (viewModel.checkUpdates()) {
                ivUpdateArrow.visible()
                tvUpdatesAvailable.visible()
            } else {
                ivUpdateArrow.gone()
                tvUpdatesAvailable.gone()
            }
        }
    }

    //updates the selected buttons
    private fun setOnPageSelected(position: Int) {
        when (position) {
            0 -> updateButtonStatus(binding.btnDocs, binding.btnVideos)
            1 -> updateButtonStatus(binding.btnVideos, binding.btnDocs)
        }
    }

    private fun updateButtonStatus(selectedButton: Button, otherButton: Button) {
        selectedButton.run {
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
        otherButton.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_grey
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
            bringToFront()
        }
    }

    private fun observer() {
        with(viewModel) {
            isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(dialog, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(dialog, false, requireContext())

                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(dialog, false, requireContext())
                    }
                }
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    is UIMessageState.StringMessage -> {
                        showToast(it.message)
                        viewModel.resetUIUpdate()
                    }

                    is UIMessageState.StringResourceMessage -> {
                        showToast(getString(it.resId))
                        viewModel.resetUIUpdate()
                    }

                    else -> {}
                }
            }
            exploreLastUpdate.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty() || it.equals("null", ignoreCase = true)) {
                    binding.tvLastSync.gone()
                } else {
                    setLastUpdate(it)
                }
                setUpdateArrow()
            }
        }
    }

    private fun setLastUpdate(it: String) {
        binding.apply {
            setLastSyncedOn(tvLastSync, it).toString()
            tvLastSync.visible()
        }
    }

}