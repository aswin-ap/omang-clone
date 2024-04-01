package com.omang.app.ui.explore.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.databinding.FragmentWebPlatformsBinding
import com.omang.app.ui.explore.adapter.WebPlatformAdapter
import com.omang.app.ui.explore.adapter.WebPlatformSort
import com.omang.app.ui.explore.viewmodel.ExploreViewModel
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.createPopUp
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showAlertDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AllWebPlatformsFragment : Fragment() {

    private lateinit var binding: FragmentWebPlatformsBinding
    private lateinit var webPlatformAdapter: WebPlatformAdapter
    private val exploreViewModel: ExploreViewModel by viewModels()
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebPlatformsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreViewModel.getAvailableNetworkStatus(requireContext())
        initView()
        observeData()
    }

    private fun observeData() {
        with(exploreViewModel) {

           /* networkStatus.observe(viewLifecycleOwner) { hasInternet ->
                handleNetworkStatusInWebPlatforms(hasInternet)
            }*/
            isConnectionStatus.observe(viewLifecycleOwner) { hasInternet ->
                handleNetworkStatusInWebPlatforms(hasInternet.networkStatus)
            }
            /*exploreDataResponse.observe(viewLifecycleOwner) { exploreList ->
                run {
//                updateRecyclerview(exploreList)
                    Timber.d("Explore list: $exploreList")
                }

            }*/

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

            exploreLiveData.observe(viewLifecycleOwner) { webList ->
                Timber.tag("AllWebFragment").d("PlatformList : ${webList.size}")
                 updateRecyclerview(webList.sortedByDescending { it.timeStamp })

            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
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
        }
    }

    private fun updateRecyclerview(webList: List<MyWebPlatformEntity>?) {
        webList?.let { myWebPlatformEntities ->
            if (myWebPlatformEntities.isNotEmpty()) {
                webPlatformAdapter.updateList(myWebPlatformEntities)
                binding.apply {
                    rvWebPlatforms.visible()
                    rvWebPlatforms.adapter = webPlatformAdapter
                    tvNoContent.gone()
                }
            } else {
                binding.apply {
                    rvWebPlatforms.gone()
                    tvNoContent.visible()
                }
            }
        }
    }


    private fun initView() {
        dialog = Dialog(requireContext(), R.style.mDialogTheme)

        binding.rvWebPlatforms.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        webPlatformAdapter = WebPlatformAdapter(
            emptyList(),
            onItemClick = {
                val formattedDateTime = ViewUtil.getUtcTime()
                try {
                    if (!isClickRecently()) {exploreViewModel.getIndividualWebData(it, findNavController())}
                }catch (e: IllegalArgumentException){
                    Timber.tag("EXPLORE ALL NAVIGATION").e("Exception Message is ${e.message}")
                }
                exploreViewModel.setTimeStamp(it, formattedDateTime)

            },
            favClick = { id, isFav ->
                run {
                    if (isFav == 1) {
                        requireActivity().showAlertDialog(
                            message = getString(R.string.favorite_remove),
                            onPositiveButtonClicked = {
                                lifecycleScope.launch {
                                    exploreViewModel.removeFavorite(id)
                                }
                            },
                            onNegativeButtonClicked = {})

                    } else {
                        requireActivity().showAlertDialog(
                            message = getString(R.string.favorite_add),
                            onPositiveButtonClicked = {
                                lifecycleScope.launch {
                                    exploreViewModel.setFavorite(id)
                                }
                            },
                            onNegativeButtonClicked = {})
                    }
                }
            }
        )
        exploreViewModel.getExploreItems()
        //exploreViewModel.observeNetworkStatus()
        binding.apply {

            lytSort.btnAscending.invisible()

            lytSort.ivTimeSort.setOnClickListener {
                webPlatformAdapter.sort(
                    WebPlatformSort.TIME_SORT
                )
            }

            lytSort.btnAlphaSort.setOnClickListener {
                requireContext().createPopUp(
                    it, R.menu.sort_by_name_menu
                ) { id ->
                    //Sort the items by alphabet
                    if (id == R.id.descending_name_sort)
                        webPlatformAdapter.sort(
                            WebPlatformSort.DESCENDING_NAME
                        )
                    else webPlatformAdapter.sort(
                        WebPlatformSort.ASCENDING_NAME
                    )
                }
            }

            lytSort.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    webPlatformAdapter.filter.filter(query.toString())
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    webPlatformAdapter.filter.filter(query.toString())
                    return true
                }
            })
        }
    }

    private fun handleNetworkStatusInWebPlatforms(hasInternet: Boolean?) {
        hasInternet?.let {
            if (this::webPlatformAdapter.isInitialized)
                webPlatformAdapter.bindViewHolderBasedOnInternetConnection(it)
        }
    }
}