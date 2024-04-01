package com.omang.app.ui.myWebPlatforms.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.databinding.FragmentMyWebPlatformsBinding
import com.omang.app.ui.explore.adapter.WebPlatformAdapter
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myWebPlatforms.viewmodel.MyWebPlatformViewModel
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.setLastSyncedOn
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MyWebPlatformsFragment : Fragment() {

    private lateinit var binding: FragmentMyWebPlatformsBinding
    private lateinit var webPlatformAdapter: WebPlatformAdapter
    private val viewModel: MyWebPlatformViewModel by viewModels()
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyWebPlatformsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun observeData() {
        with(viewModel) {

            exploreLiveData.observe(viewLifecycleOwner) { webList ->
                Timber.tag(TAG).d("PlatformList : ${webList.size}")
                if (webList != null) {
                    updateRecyclerview(webList)
                }
            }

            individualWebResponse.observe(viewLifecycleOwner) { individualWebList ->
//                Toast.makeText(context, individualWebList[0].data.url, Toast.LENGTH_LONG).show()
            }

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

            networkStatus.observe(viewLifecycleOwner) { hasInternet ->
                handleNetworkStatusInWebPlatforms(hasInternet)
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

    private fun handleNetworkStatusInWebPlatforms(hasInternet: Boolean?) {
        hasInternet?.let {
            if (this::webPlatformAdapter.isInitialized)
                webPlatformAdapter.bindViewHolderBasedOnInternetConnection(it)
        }
    }

    private fun updateRecyclerview(webList: List<MyWebPlatformEntity>?) {
        webList?.let { myWebPlatformEntities ->
            if (myWebPlatformEntities.isNotEmpty()) {
                webPlatformAdapter.updateList(myWebPlatformEntities)
                binding.apply {
                    rvMyWebPlatforms.adapter = webPlatformAdapter
                    webPlatformAdapter.hideFavButton()
                    rvMyWebPlatforms.visible()
                    tvNoContent.gone()
                }
            } else {
                binding.apply {
                    rvMyWebPlatforms.gone()
                    tvNoContent.visible()
                }
            }
        }
    }

    private fun initView() {

        dialog = Dialog(requireContext(), R.style.mDialogTheme)

        (activity as HomeActivity).configureToolbar(
            CurrentActivity.OTHER,
            R.string.my_web_platforms
        )
        viewModel.observeNetworkStatus()
        viewModel.fetchLastUpdateTime()

        binding.rvMyWebPlatforms.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
        binding.btnSyncApi.setOnClickListener {
            viewModel.getMyWebPlatformData()
        }

        webPlatformAdapter = WebPlatformAdapter(
            emptyList(),
            onItemClick = { result ->

                viewModel.getIndividualWebData(result, findNavController())

            },
            favClick = { _, _ ->

            }
        )
    }

    companion object {
        const val TAG = "My WebPlatform"
    }
}