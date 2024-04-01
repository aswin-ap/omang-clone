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
import com.omang.app.databinding.FragmentDocsBinding
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
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
class SubjectContentDocsFragment : Fragment() {

    private val docsViewModel by hiltNavGraphViewModels<DocAndVideoViewModel>(R.id.nav_videos_docs)

    private var _binding: FragmentDocsBinding? = null
    private val binding get() = _binding!!

    private lateinit var docsVideosAdapter: DocsVideosAdapter

    private var classRoomId = 0
    private var downloadDocIdList: MutableList<Int> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDocsBinding.inflate(inflater, container, false)
        bindView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        downloadDocIdList.clear()
        with(docsViewModel) {
            resourceLiveData.observe(viewLifecycleOwner) { docsList ->
                Timber.tag("Doc").d("docList is $docsList")
                updateRecyclerview(docsList)
            }

            downloadStatus.observe(viewLifecycleOwner) {
                it?.let {
                    if (it.downloadStatus == DownloadStatus.FINISHED && it.progress == 100) {
                        downloadDocIdList.add(it.id)
                        Timber.tag("Doc Download Ids").d("Doc Download List is ${downloadDocIdList.toSet()}")
                    }
                    docsVideosAdapter.updateDownload(it.id, it.progress, it.downloadStatus)
                }
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
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

    }

    private fun updateRecyclerview(docsList: List<ResourcesEntity>?) {
        docsList?.let { docsEntities ->
            if (docsEntities.isNotEmpty()) {
                docsVideosAdapter.updateList(docsEntities)
                binding.apply {
                    ryDocs.visible()
                    tvNoContent.gone()
                    mainGroup.visible()
                }
            } else {
                binding.apply {
                    ryDocs.gone()
                    tvNoContent.visible()
                    mainGroup.gone()
                }
            }
        }
    }

    private fun getClassRoomId() {
        Timber.tag("SubjectContentDocsFragment").d("classRoomId: $classRoomId")
        val navHostFragment = parentFragment as NavHostFragment?
        val parent: DocsVideosContentsFragment? =
            navHostFragment!!.parentFragment as DocsVideosContentsFragment?
        classRoomId = parent?.classroomId!!
    }

    fun bindView() {
        downloadDocIdList.clear()
        docsViewModel.clearLiveData()
        getClassRoomId()
        binding.apply {
            docsVideosAdapter = DocsVideosAdapter(
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
                    try {
                        if (!isClickRecently()) { val navController =
                            parentFragment?.parentFragment?.findNavController()
                            docsViewModel.navigate(navController!!, entity, classRoomId)
                        }
                    }catch (e: IllegalArgumentException){
                        Timber.tag("SUBJECT NAVIGATION EXCEPTION").e("Exception Message is ${e.message}")
                    }

                }
            )
            ryDocs.apply {
                adapter = docsVideosAdapter
                layoutManager = LinearLayoutManager(requireActivity())
                ((this.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
            }

            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    docsVideosAdapter.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    docsVideosAdapter.filter.filter(query)
                    return false
                }
            })
            buDescending.setOnClickListener {
                docsVideosAdapter.sort(SortOrder.DESCENDING)
            }

            buAscending.setOnClickListener {
                docsVideosAdapter.sort(SortOrder.ASCENDING)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        docsViewModel.getDocumentsByClassroomId(classRoomId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        fun newInstance(): SubjectContentDocsFragment {
            return SubjectContentDocsFragment()
        }
    }
}