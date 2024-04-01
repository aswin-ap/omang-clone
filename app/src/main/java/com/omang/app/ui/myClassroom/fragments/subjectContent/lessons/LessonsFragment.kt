package com.omang.app.ui.myClassroom.fragments.subjectContent.lessons

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.omang.app.R
import com.omang.app.data.database.DBConstants
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.databinding.FragmentLessonsBinding
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.activity.showRatingDialog
import com.omang.app.ui.home.fragments.TestSuccessEvent
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.LessonsHeaderAdapter
import com.omang.app.ui.myClassroom.fragments.subjectContent.lessons.dialog.WeblinksDialog
import com.omang.app.ui.myClassroom.viewmodel.LessonsViewModel
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


@AndroidEntryPoint
class LessonsFragment : Fragment(){

    private lateinit var binding: FragmentLessonsBinding
    private var lessonAdapter: LessonsHeaderAdapter? = null
    private val viewModel: LessonsViewModel by viewModels()
    private lateinit var syncAlert: Dialog
    private lateinit var weblinksDialog: WeblinksDialog
    private var classroomId: Int? = null
    private var groupDownloadId: MutableList<Int> = mutableListOf()
    private var childDownloadId: MutableList<Int> = mutableListOf()

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@LessonsFragment)) {
            EventBus.getDefault().register(this@LessonsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLessonsBinding.inflate(inflater, container, false)
       // viewModel.observeNetworkStatus()
        bindView()
        observe()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchLessonsAndUnits()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@LessonsFragment)
    }

    private fun observe() {
        with(viewModel) {
                groupDownloadStatus.observe(viewLifecycleOwner) {
                    it?.let {
                        lessonAdapter?.updateGroupDownload(
                            it.id,
                            it.progress,
                            it.downloadStatus,
                            it.downloadedFiles
                        )
                        if (it.downloadStatus == DownloadStatus.FINISHED)
                            groupDownloadId.add(it.id)
                    }
                }


            childDownloadStatus.observe(viewLifecycleOwner) {
                it?.let {
                    lessonAdapter?.updateChildDownload(
                        it.itemId,
                        it.progress,
                        it.downloadStatus,
                        it.downloadCount
                    )
                    if (it.downloadStatus == DownloadStatus.FINISHED)
                        childDownloadId.add(it.itemId)
                }
            }

            viewModel.isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(syncAlert, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(syncAlert, false, requireContext())

                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(syncAlert, false, requireContext())
                    }
                }
            }

           baseProgress.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(syncAlert, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(syncAlert, false, requireContext())

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

            lessonsLiveData.observe(viewLifecycleOwner) {
                updateRecyclerview(it)
            }
            isRatingSuccess.observe(viewLifecycleOwner){ ratingResponse ->
                lessonAdapter?.updateChildRatingStatus(ratingResponse.data.entityId, ratingResponse.data.ratingValue)
            }


            webLinks.observe(viewLifecycleOwner) { weblinks ->
                Timber.tag(MyWebPlatformsFragment.TAG).d("PlatformList : ${weblinks.size}")
                val hasInternet = requireContext().hasInternetConnection()
                weblinksDialog = WeblinksDialog(requireContext())
                weblinksDialog.showDialog(weblinks, hasInternet) {
                    // navigate to web view
                    Timber.d("weblink id clicked : $it")

                    viewModel.getWebsiteData(it, findNavController())

                }
            }

           /* internetStatusLiveData.observe(viewLifecycleOwner) { hasInternet ->
                    lessonAdapter?.updateInternetStatus(hasInternet)
                if (::weblinksDialog.isInitialized)
                    weblinksDialog.internetStatus(hasInternet)

            }*/

            isConnectionStatus.observe(viewLifecycleOwner){ hasConnection ->
                lessonAdapter?.updateInternetStatus(hasConnection.networkStatus)
                if (::weblinksDialog.isInitialized)
                    weblinksDialog.internetStatus(hasConnection.networkStatus)
            }
        }
    }

    private fun bindView() {
        syncAlert = Dialog(requireContext(), R.style.mDialogTheme)
        getClassRoomId()
        viewModel.getAvailableNetworkStatus(requireContext())
        (activity as HomeActivity).showPsmByClassroomId(classroomId!!)
        binding.rvLessons.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            ((this.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun updateRecyclerview(unitList: List<UnitWithLessons>) {
        unitList.let { lessonsAndUnitList ->
            if (lessonsAndUnitList.isNotEmpty()) {
                lessonAdapter = LessonsHeaderAdapter(
                    requireContext(),
                    lessonsAndUnitList,
                    onBulkDownload = { id, url, downloadedFiles ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.bulkDownload(url, id, downloadedFiles)

                        }
                    }, onIndividualDownload = { id, url, downloadedFiles ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.individualDownload(url, id, downloadedFiles)
                        }
                    }, onItemClick = { item, unitId ->
                        viewModel.navigate(findNavController(), item, unitId)
                    },
                    onWeblinkClick = {
                        fetchWeblinks(it)
                    }, onTestClick = { id, name ->
                        viewModel.launchTest(findNavController(), id)
                    }, isLessonExpanded = { isTrue ->
                        handleButtons(isTrue)
                    }, onRatingClick = { lessonId ->
                        (activity as HomeActivity).showRatingDialog{ ratingValue ->
                        //    viewModel.rateResources("lesson", lessonId, ratingValue)

                        }

                    })
                binding.apply {
                    rvLessons.adapter = lessonAdapter
                    rvLessons.visible()
                    tvNoLessons.gone()
                }
                lessonAdapter?.updateInternetStatus(requireContext().hasInternetConnection())
            } else {
                binding.apply {
                    rvLessons.gone()
                    tvNoLessons.visible()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(testComplete: TestSuccessEvent?) {
        //after test completed, re fetches the unit and lessons
        viewModel.fetchLessonsAndUnits()
    }

    private fun handleButtons(value: Boolean) {
        EventBus.getDefault().post(value)
    }

    private fun fetchWeblinks(lessonId: Int) {
        viewModel.getLessonWeblinks(lessonId)
    }

    private fun getClassRoomId() {
        classroomId = arguments?.getInt(CLASSROOM_ID)
        viewModel.setClassRoomId(classroomId!!)
        viewModel.addToNavigation(
            event = DBConstants.Event.VISIT,
            page = LessonsFragment::class.java.name,
            comment = "Visited the lesson tab",
            subjectId = classroomId
        )
    }

    companion object {
        private const val CLASSROOM_ID = "classRoomId"
        fun newInstance(id: Int): LessonsFragment = LessonsFragment().apply {
            arguments = Bundle().apply {
                putInt(CLASSROOM_ID, id)
            }
        }
    }



}

class NonScrollableRecyclerView(context: Context, attrs: AttributeSet?) :
    RecyclerView(context, attrs) {

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Prevent parent RecyclerView from intercepting touch events
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        // Prevent parent RecyclerView from intercepting touch events
        parent.requestDisallowInterceptTouchEvent(true)
        return super.onInterceptTouchEvent(e)
    }
}