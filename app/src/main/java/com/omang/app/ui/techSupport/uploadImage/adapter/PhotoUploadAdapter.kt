package com.omang.app.ui.techSupport.uploadImage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.R
import com.omang.app.databinding.PhotoUpldItemLayoutBinding
import com.omang.app.ui.gallery.model.MediaFile

class PhotoUploadAdapter(
    private var originalMediaList: List<MediaFile>,
    private val onMediaItemClick: (MediaFile) -> Unit,
    private val onCheckBoxClick: ((Int) -> Unit)
) : RecyclerView.Adapter<PhotoUploadAdapter.PhotoViewHolder>() {

    private val selectedItems = mutableListOf<MediaFile>()

    fun updateData(newPictureList: List<MediaFile>) {
        originalMediaList = newPictureList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoUploadAdapter.PhotoViewHolder {
        val binding = DataBindingUtil.inflate<PhotoUpldItemLayoutBinding>(
            LayoutInflater.from(parent.context),
            R.layout.photo_upld_item_layout, parent, false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoUploadAdapter.PhotoViewHolder, position: Int) {
        val mediaItem = originalMediaList[position]
        holder.bindItem(mediaItem)
    }

    override fun getItemCount(): Int = originalMediaList.size

    inner class PhotoViewHolder(
        private val itemBinding: PhotoUpldItemLayoutBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(mediaFile: MediaFile) {
            itemBinding.file = mediaFile

            itemBinding.ivFile.setOnClickListener {
                selectedItems.add(mediaFile)
                onMediaItemClick(mediaFile)
            }

            itemBinding.executePendingBindings()
        }
    }

    fun getSelectedItems(): List<MediaFile> {
        return selectedItems
    }
}

