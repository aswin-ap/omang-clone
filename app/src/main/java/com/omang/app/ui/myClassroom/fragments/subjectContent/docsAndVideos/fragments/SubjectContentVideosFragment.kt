package com.omang.app.ui.myClassroom.fragments.subjectContent.docsAndVideos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.omang.app.R
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.databinding.FragmentSubjectVideosBinding
import com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.DocsVideosAdapter
import com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.SortOrder
import com.omang.app.ui.myClassroom.viewmodel.DocAndVideoViewModel
import com.omang.app.ui.myClassroom.viewmodel.QueueDownload
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SubjectContentVideosFragment : Fragment() {

    private var _binding: FragmentSubjectVideosBinding? = null
    private val binding get() = _binding!!

    private val docsViewModel by hiltNavGraphViewModels<DocAndVideoViewModel>(R.id.nav_videos_docs)

    lateinit var videosAdapter: DocsVideosAdapter

    private var classRoomId = 0
    private var downloadVideoIdList: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSubjectVideosBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    fun bindView() {
        downloadVideoIdList.clear()
        docsViewModel.clearLiveData()
        getClassRoomId()
        videosAdapter = DocsVideosAdapter(
            requireContext(),
            mutableListOf(),
            onDownloadClick = { id, url ->
                docsViewModel.list.add(QueueDownload(id = id, downloadId = -1, url))
                if (docsViewModel.list.first().downloadId == -1) {
                    docsViewModel.downloadQueuedFiles()
                } else {
                    Toast.makeText(requireContext(), "Docs queued", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            onItemClick = { entity ->
                val navController =
                    parentFragment?.parentFragment?.findNavController()
                docsViewModel.navigate(navController!!, entity, classRoomId)
            }
        )
        binding.apply {
            rvVideos.apply {
                adapter = videosAdapter
                layoutManager = LinearLayoutManager(requireActivity())
                ((this.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
            }
            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    videosAdapter.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    videosAdapter.filter.filter(query)
                    return false
                }
            })
            buDescending.setOnClickListener {
                videosAdapter.sort(SortOrder.DESCENDING)
            }

            buAscending.setOnClickListener {
                videosAdapter.sort(SortOrder.ASCENDING)
            }
        }
    }

    private fun observeData() {
        downloadVideoIdList.clear()
        docsViewModel.resourceLiveData.observe(viewLifecycleOwner) { docsList ->
            Timber.tag("MyClassroom").d("videoList is $docsList")
            updateRecyclerview(docsList)
        }
        docsViewModel.downloadStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it.downloadStatus == DownloadStatus.FINISHED) {
                    downloadVideoIdList.add(it.id)
                    Timber.tag("Video Download Ids").d("Video Download List is ${downloadVideoIdList.toSet()}")
                }
                videosAdapter.updateDownload(it.id, it.progress, it.downloadStatus)
            }
        }

        docsViewModel.uiMessageStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is UIMessageState.StringMessage -> {
                    showToast(it.message)
                    docsViewModel.resetUIUpdate()
                }

                is UIMessageState.StringResourceMessage -> {
                    showToast(getString(it.resId))
                    docsViewModel.resetUIUpdate()
                }

                else -> {}
            }
        }
    }

    private fun getClassRoomId() {
        val navHostFragment = parentFragment as NavHostFragment?
        val parent: DocsVideosContentsFragment? =
            navHostFragment!!.parentFragment as DocsVideosContentsFragment?
        classRoomId = parent?.classroomId!!
    }

    private fun updateRecyclerview(docsList: List<ResourcesEntity>?) {
        docsList?.let { docsEntities ->
            if (docsEntities.isNotEmpty()) {
                videosAdapter.updateList(docsEntities)
                binding.apply {
                    rvVideos.visible()
                    tvNoContent.gone()
                    mainGroup.visible()
                }
            } else {
                binding.apply {
                    rvVideos.gone()
                    tvNoContent.visible()
                    mainGroup.gone()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        docsViewModel.getVideosByClassroomId(classRoomId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SubjectContentVideosFragment {
            return SubjectContentVideosFragment()
        }
    }
}