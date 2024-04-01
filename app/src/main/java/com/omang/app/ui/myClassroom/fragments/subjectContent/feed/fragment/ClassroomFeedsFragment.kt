package com.omang.app.ui.myClassroom.fragments.subjectContent.feed.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.databinding.FragmentClassroomFeedsBinding
import com.omang.app.ui.feeds.adapter.FeedAdapter
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.fragments.subjectContent.feed.viewmodel.ClassroomFeedsViewModel
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ClassroomFeedsFragment : Fragment() {
    private lateinit var binding: FragmentClassroomFeedsBinding
    private val viewModel: ClassroomFeedsViewModel by viewModels()
    private lateinit var feedAdapter: FeedAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClassroomFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }
    private fun initView() {
        viewModel.setClassroomId(arguments?.getInt(CLASSROOM_ID)!!)
        binding.rv.apply {
            feedAdapter = FeedAdapter(mutableListOf()) { type, classroomid -> }
            layoutManager = LinearLayoutManager(requireActivity())
            ((this.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
            adapter = feedAdapter
        }
        viewModel.getFeedsByClassroomId()
        viewModel.getFeedsFromAPI()
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
            } else {
                pbLoader.gone()
            }
        }
    }
    private fun updateRecyclerview(notifications: List<FeedEntity>?) {
        notifications?.let { notificationEntities ->
            Timber.tag("ClassroomFeed").d("in updateRecyclerview : $notificationEntities")
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

    companion object {
        private const val CLASSROOM_ID = "classRoomId"
        fun newInstance(id: Int): ClassroomFeedsFragment = ClassroomFeedsFragment().apply {
            arguments = Bundle().apply {
                putInt(CLASSROOM_ID, id)
            }
        }
    }

}