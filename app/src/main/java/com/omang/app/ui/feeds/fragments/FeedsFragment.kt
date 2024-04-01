package com.omang.app.ui.feeds.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.omang.app.R
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.databinding.FragmentNotificationsBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.feeds.adapter.FeedAdapter
import com.omang.app.ui.feeds.viewmodel.FeedsViewModel
import com.omang.app.ui.home.activity.ControlNavigation.safeNavigateWithArgs
import com.omang.app.ui.home.fragments.HomeFragmentDirections
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showAlertDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.toJson
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FeedsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by viewModels()
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        dialog = Dialog(requireContext())
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.OTHER,
            R.string.title_feed
        )
        binding.apply {
            feedAdapter = FeedAdapter(mutableListOf()) {type,classroomId->
                if (findNavController().currentDestination?.label == "Notifications" && isAdded) {
                    viewModel.navigate(type,classroomId,findNavController())
                }
            }
            rv.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                ((this.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
                adapter = feedAdapter
            }
            btnClearAll.setOnClickListener {
                requireActivity().showAlertDialog(message = getString(R.string.title_feed_clear),
                    onPositiveButtonClicked = {
                        viewModel.clearAllNotifications()
//                        viewModel.deleteNotification()
                    },
                    onNegativeButtonClicked = {})
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getFeeds()
    }

    private fun observeData() {
        with(viewModel) {
            feedLiveData.observe(viewLifecycleOwner) {
                updateRecyclerview(it)
            }

            isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        showProgress(true)
                    }

                    NetworkLoadingState.SUCCESS -> {
                        showProgress(false)
                    }

                    NetworkLoadingState.ERROR -> {
                        showProgress(false)
                    }
                }
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                it?.let {
                    when (it) {
                        is UIMessageState.StringMessage -> {
                            showToast(it.message)
                            viewModel.resetUIUpdate()
                        }

                        is UIMessageState.StringResourceMessage -> {
                            showToast(getString(it.resId))
                            viewModel.resetUIUpdate()
                        }

                        else -> {
                            return@let
                        }
                    }
                }
            }
        }
    }


    private fun showProgress(show: Boolean) {
        binding.apply {
            if (show) {
                pbLoader.visible()
                btnClearAll.gone()
            } else {
                pbLoader.gone()
                btnClearAll.visible()
            }
        }
    }

    private fun updateRecyclerview(notifications: List<FeedEntity>?) {
        notifications?.let { notificationEntities ->
            Timber.tag("HomeFeed").d("in updateRecyclerview : ${notificationEntities.toJson()}")
            if (notificationEntities.isNotEmpty()) {
                feedAdapter.updateFeedsList(notifications)
                binding.rv.visible()
                binding.tvNoContent.gone()
            } else {
                binding.tvNoContent.visible()
                binding.rv.gone()
            }
        } ?: kotlin.run {
            binding.rv.gone()
            binding.tvNoContent.visible()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}