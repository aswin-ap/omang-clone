package com.omang.app.ui.gallery.fragments

import com.omang.app.ui.gallery.adapter.MediaAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.data.database.DBConstants
import com.omang.app.databinding.FragmentVideosBinding
import com.omang.app.ui.gallery.model.MediaFile
import com.omang.app.ui.gallery.viewmodel.FileStatus
import com.omang.app.ui.gallery.viewmodel.GalleryViewModel
import com.omang.app.ui.gallery.viewmodel.MEDIATYPE
import com.omang.app.utils.extensions.createPopUp
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.showAlertDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import kotlinx.coroutines.launch
import timber.log.Timber

class VideosFragment : Fragment() {

    private lateinit var binding: FragmentVideosBinding
    private lateinit var videoAdapter: MediaAdapter

    /*Sets the GalleryFragment as ownerProducer. So the same instance of
    ViewModel in the GalleryFragment will be created*/
    private val viewModel: GalleryViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideosBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            videoList.observe(viewLifecycleOwner) { pictureList ->
                Timber.d("Video list: $pictureList")
                updateData(pictureList)
            }
            //If the fragment is started, the data will be observe
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fileStatus.observe(viewLifecycleOwner) { value ->
                    when (value) {
                        is FileStatus.DeleteOption -> {
                            if (value.hasDeleteOption) {
                                binding.lytSort.cvDelete.visible()
                            } else binding.lytSort.cvDelete.gone()
                        }

                        FileStatus.DeleteSuccess -> {
                            showToast(getString(R.string.delete_success))
                            binding.lytSort.cvDelete.gone()
                        }

                        is FileStatus.Error -> {
                            showToast(value.error.toString())
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun initView() {
        videoAdapter = MediaAdapter(
            mutableListOf(),
            onMediaItemClick = { item ->
                GalleryFragmentDirections.actionGalleryFragmentToVideoViewerFragment4(
                    0,
                    item.urlPath,
                    0,
                    0,
                    0,
                    DBConstants.AnalyticsMenu.GALLERY.value
                ).also {
                    findNavController().navigate(it)
                }
            },
            onCheckBoxClick = { position ->
                //updates the selected Position
                viewModel.updateSelectionStatus(MEDIATYPE.VIDEO, position)
            }, onItemDelete = {
                requireActivity().showAlertDialog(message = getString(R.string.delete_confirmation_message),
                    onPositiveButtonClicked = {
                        lifecycleScope.launch {
                            viewModel.deleteSelectedFile(
                                MEDIATYPE.VIDEO, requireActivity().contentResolver, it
                            )
                        }
                        videoAdapter.deleteItem(it)
                        showToast("file deleted")
                    },
                    onNegativeButtonClicked = {})
            })

        binding.apply {
            rvVideos.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = videoAdapter
            }

//            lytSort.cvDelete.setOnClickListener {
//                //shows delete  confirmation dialog
//                requireActivity().showAlertDialog(message = getString(R.string.delete_confirmation_message),
//                    onPositiveButtonClicked = {
//                        viewModel.deleteSelectedFiles(
//                            MEDIATYPE.VIDEO,
//                            requireActivity().contentResolver
//                        )
//
//                    },
//                    onNegativeButtonClicked = {})
//            }

            lytSort.btnAscending.setOnClickListener {
                requireContext().createPopUp(
                    it, R.menu.sort_by_date_menu
                ) { id ->
                    //Sort the items by time
                    if (id == R.id.descending_date_sort) viewModel.sortListByDate(
                        MEDIATYPE.VIDEO,
                        false
                    )
                    else viewModel.sortListByDate(MEDIATYPE.VIDEO, true)
                }
            }

            lytSort.btnAlphaSort.setOnClickListener {
                requireContext().createPopUp(
                    it, R.menu.sort_by_name_menu
                ) { id ->
                    //Sort the items by alphabet
                    if (id == R.id.descending_name_sort) viewModel.sortListByName(
                        MEDIATYPE.VIDEO,
                        false
                    )
                    else viewModel.sortListByName(MEDIATYPE.VIDEO, true)
                }
            }

            lytSort.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        //search functionality
                        videoAdapter.filter.filter(it)
                    }
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let {
                        //search functionality
                        videoAdapter.filter.filter(it)
                    }
                    return false
                }
            })
        }
    }

    //Updates data in the recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private fun updateData(videoList: List<MediaFile>?) {
        videoList?.let { list ->
            if (list.isNotEmpty()) {
                videoAdapter.updatePictureList(list)
                videoAdapter.notifyDataSetChanged()
                binding.apply {
                    rvVideos.visible()
                    tvNoContent.invisible()
                }
            } else {
                binding.apply {
                    rvVideos.invisible()
                    tvNoContent.visible()
                }
            }
        }
    }
}