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
import com.omang.app.databinding.FragmentDocumentsBinding
import com.omang.app.ui.gallery.adapter.MediaAdapter
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

class DocumentsFragment : Fragment() {

    private lateinit var binding: FragmentDocumentsBinding
    private lateinit var docAdapter: MediaAdapter

    /*
        Sets the GalleryFragment as ownerProducer. So the same instance of
        ViewModel in the GalleryFragment will be created
    */
    private val viewModel: GalleryViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            docList.observe(viewLifecycleOwner) { docList ->
                Timber.d("Doc list: $docList")
                updateData(docList)
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

                        else -> {

                        }
                    }
                }
            }
        }
    }

    private fun initView() {

        docAdapter = MediaAdapter(mutableListOf(),
            onMediaItemClick = { item ->

                if (item.urlPath.endsWith(".pdf")) {
                    GalleryFragmentDirections.actionGalleryFragmentToDocumentViewerFragment4(
                        0,
                        item.urlPath,
                        0,
                        0,
                        0,
                        DBConstants.AnalyticsMenu.GALLERY.value
                    ).also {
                        findNavController().navigate(it)
                    }
                } else {
                    showToast("Can't open file !!")
                }

            },
            onCheckBoxClick = { position ->
                //updates the selected Position
                viewModel.updateSelectionStatus(MEDIATYPE.DOCUMENT, position)
            },
            onItemDelete = {
                requireActivity().showAlertDialog(message = getString(R.string.delete_confirmation_message),
                    onPositiveButtonClicked = {
                        lifecycleScope.launch {
                            viewModel.deleteSelectedFile(
                                MEDIATYPE.DOCUMENT, requireActivity().contentResolver, it
                            )
                        }
                        docAdapter.deleteItem(it)
                        showToast("file deleted")
                    },
                    onNegativeButtonClicked = {

                    })
            })
        binding.apply {
            rvDocs.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = docAdapter
            }

//            lytSort.cvDelete.setOnClickListener {
//                //shows delete  confirmation dialog
//                requireActivity().showAlertDialog(message = getString(R.string.delete_confirmation_message),
//                    onPositiveButtonClicked = {
//                        lifecycleScope.launch {
//                            viewModel.deleteSelectedFiles(
//                                MEDIATYPE.DOCUMENT, requireActivity().contentResolver
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
                        MEDIATYPE.DOCUMENT,
                        false
                    )
                    else viewModel.sortListByDate(MEDIATYPE.DOCUMENT, true)
                }
            }

            lytSort.btnAlphaSort.setOnClickListener {
                requireContext().createPopUp(
                    it, R.menu.sort_by_name_menu
                ) { id ->
                    //Sort the items by alphabet
                    if (id == R.id.descending_name_sort) viewModel.sortListByName(
                        MEDIATYPE.DOCUMENT,
                        false
                    )
                    else viewModel.sortListByName(MEDIATYPE.DOCUMENT, true)
                }
            }

            lytSort.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        //search functionality
                        docAdapter.filter.filter(it)
                    }
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let {
                        //search functionality
                        docAdapter.filter.filter(it)
                    }
                    return false
                }
            })
        }

    }

    //Updates data in the recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private fun updateData(docList: List<MediaFile>?) {
        docList?.let { list ->
            if (list.isNotEmpty()) {
                docAdapter.updatePictureList(list)
                docAdapter.notifyDataSetChanged()
                binding.apply {
                    rvDocs.visible()
                    tvNoContent.invisible()
                }
            } else {
                binding.apply {
                    rvDocs.invisible()
                    tvNoContent.visible()
                }
            }
        }
    }
}