package com.omang.app.ui.myClassroom.fragments.subjectContent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.omang.app.R
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.model.bubble.BubbleData
import com.omang.app.databinding.FragmentSubjectContentBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.SubjectContentViewPagerAdapter
import com.omang.app.ui.myClassroom.viewmodel.SubjectContentsViewModel
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.setButtonBackground
import com.omang.app.utils.extensions.setButtonTextColor
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.setLastSyncedOn
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class SubjectContentsFragment : Fragment() {
    @Inject
    lateinit var toastMessage: ToastMessage

    var classRoomId: Int? = null

    private val args: SubjectContentsFragmentArgs by navArgs()

    private lateinit var syncAlert: Dialog

    private var currentPosition = 0

    enum class ContentType(val type: Int) {
        LESSONS(0), DOCS_VIDEOS(1), PLATFORMS(2), FEEDS(3), TESTS(4)
    }

    private var _binding: FragmentSubjectContentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubjectContentsViewModel by viewModels()

    private lateinit var pagerAdapter: SubjectContentViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.v("onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@SubjectContentsFragment)) {
            EventBus.getDefault().register(this@SubjectContentsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSubjectContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        observe()
        registerPagerChangeCallback()

    }


    private fun observe() {
        with(viewModel) {
            isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(syncAlert, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        reInitAdapter(currentPosition)
                        loadingDialog(syncAlert, false, requireContext())
                        viewModel.fetchClassroomDetails(classRoomId!!)
                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(syncAlert, false, requireContext())
                    }

                    else -> {}
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

            classroomDetails.observe(viewLifecycleOwner) {
                setupDetails(it)
            }
        }
    }

    private fun setupDetails(it: MyClassroomEntity?) {
        setUpdateArrow(classRoomId!!)
        it?.let { myClassroomEntity ->
            (activity as HomeActivity).configureToolbar(
                CurrentActivity.OTHER,
                title = myClassroomEntity.name
            )
            binding.apply {
                myClassroomEntity.updatedAt?.let { updated ->
                    if (ValidationUtil.isNotNullOrEmpty(updated)) {
                        setLastSyncedOn(tvLastSync, updated)
                        cardSync.visible()
                    }
                }
                btnLessons.text = "Lessons (${myClassroomEntity.contents.lessons})"
                btnDocVideos.text =
                    "DOCS & VIDEOS (${myClassroomEntity.contents.videos + myClassroomEntity.contents.books})"
                btnPlatforms.text = "PLATFORMS (${myClassroomEntity.contents.webPlatforms})"
            }
        } ?: kotlin.run {
            binding.apply {
                lytContent.gone()
                txtNoContents.visible()
            }
        }
    }

    private fun bindView() {
        //set-up loading dialog
        syncAlert = Dialog(requireContext(), R.style.mDialogTheme)
        classRoomId = args.classRoomId
        viewModel.fetchClassroomDetails(classRoomId!!)
       // viewModel.fetchClassroomResourceIds(classRoomId!!)

        (activity as HomeActivity).hideBottomNavigation(false)
        binding.apply {
            pagerAdapter =
                SubjectContentViewPagerAdapter(requireActivity(), classRoomId!!)
            viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            viewpager.adapter = pagerAdapter

            btnLessons.setOnClickListener {
                viewpager.currentItem = 0
                setOnPageSelected(ContentType.LESSONS.type)
            }
            btnDocVideos.setOnClickListener {
                viewpager.currentItem = 1
                setOnPageSelected(ContentType.DOCS_VIDEOS.type)
            }
            btnPlatforms.setOnClickListener {
                viewpager.currentItem = 2
                setOnPageSelected(ContentType.PLATFORMS.type)
            }
            btnLiveClass.setOnClickListener {
                viewpager.currentItem = 3
                setOnPageSelected(ContentType.FEEDS.type)
            }
            btnChat.setOnClickListener {
                viewpager.currentItem = 4
                setOnPageSelected(ContentType.TESTS.type)
            }
            btnGetUpdates.setOnClickListener {
                getSubjectData(classRoomId!!)
            }
        }
    }

    private fun setUpdateArrow(classRoomId: Int) {
        binding.apply {
            if (BubbleData.classroomList.contains(classRoomId)) {
                ivUpdateArrow.visible()
                tvUpdatesAvailable.visible()
            } else {
                ivUpdateArrow.gone()
                tvUpdatesAvailable.gone()
            }
        }
    }

    private fun setLastSynced(syncedOn: String?) {
        binding.apply {
            syncedOn?.let { updated ->
                if (ValidationUtil.isNotNullOrEmpty(updated)) {
                    setLastSyncedOn(tvLastSync, updated)
                    cardSync.visible()
                }
            }
        }
    }

    private fun reInitAdapter(position: Int) {
        binding.apply {
            viewpager.adapter = pagerAdapter
            viewpager.setCurrentItem(position, false)
        }
    }

    private fun getSubjectData(classroomId: Int) {
        viewModel.getSubjectData(classroomId)

    }


    private fun registerPagerChangeCallback() {
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                setOnPageSelected(position)
                expandedTabCount = 0
                hideOrShowButtons(false)
            }
        })
    }

    private fun setOnPageSelected(type: Int) {
        when (type) {
            ContentType.DOCS_VIDEOS.type -> {
                setEnableSelectedTab(binding.btnDocVideos)
                resetTabUnSelected(
                    binding.btnPlatforms,
                    binding.btnLessons,
                    binding.btnLiveClass,
                    binding.btnChat
                )
            }

            ContentType.PLATFORMS.type -> {
                setEnableSelectedTab(binding.btnPlatforms)
                resetTabUnSelected(
                    binding.btnDocVideos,
                    binding.btnLessons,
                    binding.btnLiveClass,
                    binding.btnChat
                )
            }

            ContentType.LESSONS.type -> {
                setEnableSelectedTab(binding.btnLessons)
                resetTabUnSelected(
                    binding.btnDocVideos,
                    binding.btnPlatforms,
                    binding.btnLiveClass,
                    binding.btnChat
                )
            }

            ContentType.FEEDS.type -> {
                setEnableSelectedTab(binding.btnLiveClass)
                resetTabUnSelected(
                    binding.btnDocVideos,
                    binding.btnLessons,
                    binding.btnPlatforms,
                    binding.btnChat
                )
            }

            ContentType.TESTS.type -> {
                setEnableSelectedTab(binding.btnChat)
                resetTabUnSelected(
                    binding.btnDocVideos,
                    binding.btnLessons,
                    binding.btnLiveClass,
                    binding.btnPlatforms
                )
            }
        }
    }

    private fun resetTabUnSelected(
        btn1: Button,
        btn2: Button,
        btn3: Button,
        btn4: Button,

        ) {
        btn1.run {
            buttonUnselect()
        }
        btn2.run {
            buttonUnselect()
        }

        btn3.run {
            buttonUnselect()
        }
        btn4.run {
            buttonUnselect()
        }
    }

    private fun Button.buttonUnselect() {
        setButtonTextColor(R.color.white)
        setButtonBackground(R.color.deep_blue)
    }


    private fun setEnableSelectedTab(button: Button) {
        button.run {
            buttonSelect()
        }

    }

    private fun Button.buttonSelect() {
        setButtonTextColor(R.color.color_grey)
        setButtonBackground(R.color.color_light_blue)
    }

    fun hideOrShowButtons(hideButton: Boolean) {
        binding.apply {
            if (expandedTabCount != 0) {
                btnLessons.gone()
                btnDocVideos.gone()
                btnPlatforms.gone()
                btnLiveClass.gone()
                btnChat.gone()
                btnGetUpdates.gone()
                tvLastSync.gone()
            } else {
                btnLessons.visible()
                btnDocVideos.visible()
                btnPlatforms.visible()
                btnLiveClass.visible()
                btnChat.visible()
                btnGetUpdates.visible()
                tvLastSync.visible()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(hide: Boolean?) {
        updateExpandedCount(hide!!)
        hideOrShowButtons(hide!!)
    }

    private fun updateExpandedCount(hide: Boolean) {
        if (hide) {
            expandedTabCount += 1
        } else {
            if (expandedTabCount > 0)
                expandedTabCount -= 1
        }
        Timber.d("expandedTabCount $expandedTabCount")
    }

    override fun onResume() {
        super.onResume()
        //saves the current classroomId in HomeActivity static field
        HomeActivity.selectedClassroomId = classRoomId
    }

    override fun onDestroy() {
        super.onDestroy()
        expandedTabCount = 0
        EventBus.getDefault().unregister(this@SubjectContentsFragment)
    }

    companion object {
        private var expandedTabCount = 0

        @JvmStatic
        fun newInstance() =
            SubjectContentsFragment().apply {

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}