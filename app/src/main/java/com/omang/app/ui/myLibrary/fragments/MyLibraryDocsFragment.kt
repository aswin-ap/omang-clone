package com.omang.app.ui.myLibrary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.R
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.databinding.FragmentMyLibraryDocsBinding
import com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.DocsVideosAdapter
import com.omang.app.ui.myClassroom.viewmodel.QueueDownload
import com.omang.app.ui.myLibrary.viewmodel.MyLibraryViewModel
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class MyLibraryDocsFragment : Fragment() {

    private lateinit var binding: FragmentMyLibraryDocsBinding
    private lateinit var docsVideosAdapter: DocsVideosAdapter
    private val viewModel: MyLibraryViewModel by hiltNavGraphViewModels(R.id.navigation_library)
    private var downloadDocId: MutableList<Int> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyLibraryDocsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        binding.apply {
            rvDocs.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun observeData() {
        with(viewModel) {
            myLibraryDocsLiveData.observe(viewLifecycleOwner) {
                Timber.tag(MyWebPlatformsFragment.TAG).d("myLibraryList : ${it.size}")
                updateRecyclerview(it)
                download(it)
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                it?.let {
                    when (it) {
                        is UIMessageState.StringMessage -> {
                            showToast(it.message)
                            if (it.status) {
                                showErrorUI()
                            }
                            viewModel.resetUIUpdate()
                        }

                        is UIMessageState.StringResourceMessage -> {
                            showToast(getString(it.resId))
                            if (it.status) {
                                showErrorUI()
                            }
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

    private fun download(download: List<ResourcesEntity>?) {

        if (download != null) {
            if (download.isNotEmpty()){
                binding.buAscending.setOnClickListener{
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.bulkDownload(download)
                    }
                }
            }
        }
    }

    private fun observeDownload() {
        viewModel.downloadStatus.observe(viewLifecycleOwner) {
            it?.let {
                Timber.tag("DOWNLOAD STATUS").v("${it.downloadStatus}")
                docsVideosAdapter.updateDownload(it.id, it.progress, it.downloadStatus)
                if (it.downloadStatus == DownloadStatus.FINISHED)
                    downloadDocId.add(it.id)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }

    private fun removeObservers() {
        with(viewModel) {
            myLibraryDocsLiveData.removeObservers(viewLifecycleOwner)
            //downloadStatus.removeObservers(viewLifecycleOwner)
            uiMessageStateLiveData.removeObservers(viewLifecycleOwner)
        }
    }

    private fun showErrorUI() {
        binding.apply {
            groupLoad.gone()
            tvNoContent.visible()
            tvNoContent.text = getString(R.string.something_went_wrong)
        }
    }

    private fun updateRecyclerview(classRoomList: List<ResourcesEntity>) {
        classRoomList.let { myLibraryEntities ->
            if (myLibraryEntities.isNotEmpty()) {
                docsVideosAdapter = DocsVideosAdapter(requireContext(),myLibraryEntities.toMutableList(),
                    onDownloadClick = { id, url ->
                        viewModel.list.add(QueueDownload(id = id, downloadId = -1, url))
                        if (viewModel.list.first().downloadId == -1) {
                            viewModel.downloadQueuedFiles()
                            observeDownload()

                        } else {
                            Toast.makeText(requireContext(), "Docs queued", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    onItemClick = { entity ->
                        viewModel.navigate(findNavController(), entity)
                    })
                binding.apply {
                    rvDocs.adapter = docsVideosAdapter
                    rvDocs.visible()
                    groupLoad.visible()
                    tvNoContent.gone()
                }
                enableFilterOption()
                observeDownload()
            } else {
                binding.apply {
                    rvDocs.gone()
                    groupLoad.gone()
                    tvNoContent.visible()
                }
            }
        }
    }

    private fun enableFilterOption() {
        binding.apply {
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
                docsVideosAdapter.sort(com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.SortOrder.ASCENDING)
            }

//            buAscending.setOnClickListener {
//                docsVideosAdapter.sort(com.omang.app.ui.myClassroom.fragments.subjectContent.adapter.SortOrder.DESCENDING)
//            }

        }

    }
}