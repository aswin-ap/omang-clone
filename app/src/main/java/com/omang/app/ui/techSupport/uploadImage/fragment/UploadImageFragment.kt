package com.omang.app.ui.techSupport.uploadImage.fragment

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.databinding.FragmentUploadImageBinding
import com.omang.app.ui.gallery.model.MediaFile
import com.omang.app.ui.gallery.viewmodel.GalleryViewModel
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.techSupport.uploadImage.adapter.PhotoUploadAdapter
import com.omang.app.utils.FileUtil
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class UploadImageFragment : Fragment() {

    private lateinit var binding: FragmentUploadImageBinding

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var photoUploadAdapter: PhotoUploadAdapter
    private lateinit var contentResolver: ContentResolver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
        getMedias()


    }

    private fun initView() {
        contentResolver = requireActivity().contentResolver

        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TECH_SUPPORT,
            R.string.image_upload
        )

        binding.apply {
            rvUploadPhotos.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(requireContext(), 4)
                photoUploadAdapter = PhotoUploadAdapter(
                    emptyList(),
                    onMediaItemClick = { item ->

                        val selectedItems = photoUploadAdapter.getSelectedItems()
                        val imageList = mutableListOf<String>()
                        for (item in selectedItems) {
                            imageList.add(item.urlPath)
                        }
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            "key",
                            imageList.toString()
                        )
                        findNavController().popBackStack()
                    },
                    onCheckBoxClick = { position -> })
                adapter = photoUploadAdapter
            }
        }
    }

    private fun getMedias() {
        viewModel.fetchPhoto(
            FileUtil.getPicturesPathInAppFolder(requireContext()),
            contentResolver
        )
    }

    private fun observeData() {
        with(viewModel) {
            pictureList.observe(viewLifecycleOwner) { pictureList ->
                Timber.d("Picture list: $pictureList")
                updateData(pictureList)

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateData(pictureList: List<MediaFile>?) {
        pictureList?.let { list ->
            if (list.isNotEmpty()) {
                photoUploadAdapter.updateData(pictureList)
                binding.apply {
                    rvUploadPhotos.visible()
                    tvNoContent.invisible()
                }
            } else {
                binding.apply {
                    rvUploadPhotos.invisible()
                    tvNoContent.visible()
                }
            }
        }
    }
}