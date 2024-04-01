package com.omang.app.ui.gallery.fragments

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
import com.omang.app.databinding.FragmentPhotosBinding
import com.omang.app.ui.gallery.adapter.MediaAdapter
import com.omang.app.utils.extensions.createPopUp
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.showAlertDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.omang.app.ui.gallery.model.MediaFile
import com.omang.app.ui.gallery.viewmodel.FileStatus
import com.omang.app.ui.gallery.viewmodel.GalleryViewModel
import com.omang.app.ui.gallery.viewmodel.MEDIATYPE
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
import com.omang.app.ui.imageViewer.fragment.ImageViewerFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PhotosFragment : Fragment() {

    private lateinit var binding: FragmentPhotosBinding

    /*Sets the GalleryFragment as ownerProducer. So the same instance of
    ViewModel in the GalleryFragment will be created*/
    private val viewModel: GalleryViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var photosAdapter: MediaAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotosBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        with(viewModel) {
            pictureList.observe(viewLifecycleOwner) { pictureList ->
                updateData(pictureList)
            }
            //If the fragment is started, the data will be observe
            fileStatus.observe(viewLifecycleOwner) { value ->
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
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

    //Updates data in the recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private fun updateData(pictureList: List<MediaFile>?) {
        pictureList?.let { list ->
            if (list.isNotEmpty()) {
                photosAdapter.setOriginalDataList(list)
                binding.apply {
                    rvPhotos.visible()
                    tvNoContent.invisible()
                }
            } else {
                binding.apply {
                    rvPhotos.invisible()
                    tvNoContent.visible()
                }
            }
        }
    }

    private fun initView() {
        photosAdapter = MediaAdapter(mutableListOf(),
            onMediaItemClick = { item ->
                try {
                    if (!isClickRecently()) {
                        GalleryFragmentDirections.actionGalleryFragmentToImageViewerFragment(
                            0,
                            item.urlPath,
                            0,
                            0,
                            0,
                            DBConstants.AnalyticsMenu.GALLERY.value
                        ).also {
                            findNavController().navigate(it)

                        }
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.tag("GALLERY IMAGE VIEWER NAVIGATION").e("Exception Message is ${e.message}")
                }
            },
            onCheckBoxClick = { position ->
                //updates the selected Position
                viewModel.updateSelectionStatus(MEDIATYPE.PHOTO, position)
            },
            onItemDelete = {
                requireActivity().showAlertDialog(message = getString(R.string.delete_confirmation_message),
                    onPositiveButtonClicked = {
                        lifecycleScope.launch {
                            viewModel.deleteSelectedFile(
                                MEDIATYPE.PHOTO, requireActivity().contentResolver, it
                            )
                        }
                        photosAdapter.deleteItem(it)
                        showToast("file deleted")
                    },
                    onNegativeButtonClicked = {})
            })
        binding.apply {
            rvPhotos.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = photosAdapter
            }

//            lytSort.cvDelete.setOnClickListener {
//                //shows delete  confirmation dialog
//                requireActivity().showAlertDialog(message = getString(R.string.delete_confirmation_message),
//                    onPositiveButtonClicked = {
//                        lifecycleScope.launch {
//                            viewModel.deleteSelectedFiles(
//                                MEDIATYPE.PHOTO, requireActivity().contentResolver
//                            )
//                        }
//                    },
//                    onNegativeButtonClicked = {})
//            }

            lytSort.btnAscending.setOnClickListener {
                requireContext().createPopUp(
                    it, R.menu.sort_by_date_menu
                ) { id ->
                    //Sort the items by time
                    if (id == R.id.descending_date_sort) viewModel.sortListByDate(
                        MEDIATYPE.PHOTO,
                        false
                    )
                    else viewModel.sortListByDate(MEDIATYPE.PHOTO, true)
                }
            }

            lytSort.btnAlphaSort.setOnClickListener {
                requireContext().createPopUp(
                    it, R.menu.sort_by_name_menu
                ) { id ->
                    //Sort the items by alphabet
                    if (id == R.id.descending_name_sort) viewModel.sortListByName(
                        MEDIATYPE.PHOTO,
                        false
                    )
                    else viewModel.sortListByName(MEDIATYPE.PHOTO, true)
                }
            }

            lytSort.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        //search functionality
                        photosAdapter.filter.filter(it)
                    }
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let {
                        //search functionality
                        photosAdapter.filter.filter(it)
                    }
                    return false
                }
            })
        }
    }
}