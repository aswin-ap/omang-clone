package com.omang.app.ui.explore.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.omang.app.R
import com.omang.app.databinding.FragmentMainWebplatformsBinding
import com.omang.app.ui.explore.adapter.WebPlatformsViewPagerAdapter
import com.omang.app.ui.explore.viewmodel.ExploreViewModel
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.setLastSyncedOn
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class WebPlatformsFragment : Fragment() {

    private lateinit var binding: FragmentMainWebplatformsBinding
    private lateinit var viewPagerAdapter: WebPlatformsViewPagerAdapter
    private val exploreViewModel: ExploreViewModel by viewModels()
    private lateinit var syncDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainWebplatformsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, R.string.explore)
        bindView()
        observer()
    }

    private fun observer() {
        exploreViewModel.isSyncing.observe(viewLifecycleOwner) { state ->
            when (state) {
                NetworkLoadingState.LOADING -> {
                    loadingDialog(syncDialog, true, requireContext())
                }

                NetworkLoadingState.SUCCESS -> {
                    loadingDialog(syncDialog, false, requireContext())

                }

                NetworkLoadingState.ERROR -> {
                    loadingDialog(syncDialog, false, requireContext())
                }
            }
        }

        exploreViewModel.uiMessageStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is UIMessageState.StringMessage -> {
                    showToast(it.message)
                    exploreViewModel.resetUIUpdate()
                }

                is UIMessageState.StringResourceMessage -> {
                    showToast(getString(it.resId))
                    exploreViewModel.resetUIUpdate()
                }

                else -> {}
            }
        }

        exploreViewModel.exploreLastUpdate.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty() || it.equals("null", ignoreCase = true)) {
                binding.tvLastSync.gone()
            } else {
                setLastUpdate(it)
            }
            setUpdateArrow()
        }
    }

    private fun setLastUpdate(it: String) {
        binding.apply {
            setLastSyncedOn(tvLastSync, it).toString()
            tvLastSync.visible()
        }
    }

    private fun setUpdateArrow() {
        binding.apply {
            if (exploreViewModel.checkUpdates()) {
                ivUpdateArrow.visible()
                tvUpdatesAvailable.visible()
            } else {
                ivUpdateArrow.gone()
                tvUpdatesAvailable.gone()
            }
        }
    }

    private fun bindView() {

        syncDialog = Dialog(requireContext(), R.style.mDialogTheme)
        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, R.string.explore)
        viewPagerAdapter = WebPlatformsViewPagerAdapter(this@WebPlatformsFragment)

        exploreViewModel.fetchLastUpdateTime()

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
            btnAll.setOnClickListener {
                it.bringToFront()
                viewpager.currentItem = 0
            }
            btnMyFavourites.setOnClickListener {
                it.bringToFront()
                viewpager.currentItem = 1
            }
            btnSyncApi.setOnClickListener {
                exploreViewModel.log("\n<---->")
                exploreViewModel.log("btnSyncApi")
                exploreViewModel.getExploreWebData()
            }
        }
    }

    //updates the selected buttons
    private fun setOnPageSelected(position: Int) {
        when (position) {
            0 -> btnAllSelected()
            1 -> btnFavSelected()
        }
    }

    private fun btnAllSelected() {
        binding.apply {
            btnAll.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_true)
                elevation = 5f
            }

            btnMyFavourites.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_false)
                elevation = 0f

            }
        }
    }

    private fun btnFavSelected() {
        binding.apply {
            btnMyFavourites.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_true)
                elevation = 5f

            }

            btnAll.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_false)
                elevation = 0f

            }
        }
    }

}