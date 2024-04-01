package com.omang.app.ui.gallery.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.omang.app.R
import com.omang.app.databinding.MediaItemLayoutBinding
import com.omang.app.ui.gallery.model.MediaFile

class MediaAdapter(
    private var originalMediaList: List<MediaFile>,
    private val onMediaItemClick: (MediaFile) -> Unit,
    private val onItemDelete: (MediaFile) -> Unit,
    private val onCheckBoxClick: (Int) -> Unit
) : RecyclerView.Adapter<MediaAdapter.PhotosAndVideoViewHolder>(), Filterable {

    private var mediaFilterList = mutableListOf<MediaFile>()


    init {
        mediaFilterList.addAll(originalMediaList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosAndVideoViewHolder {
        val binding =
            MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotosAndVideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotosAndVideoViewHolder, position: Int) {
        val mediaItem = mediaFilterList[position]
        holder.bindItem(mediaItem, position)
    }

    fun setOriginalDataList(newPictureList: List<MediaFile>) {
        originalMediaList = newPictureList
        updatePictureList(newPictureList)
    }

    fun updatePictureList(newPictureList: List<MediaFile>) {
        val diffCallback = MediaDiffUtilCallback(mediaFilterList, newPictureList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        mediaFilterList.clear()
        mediaFilterList.addAll(newPictureList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateData(media: MediaFile) {
        val diffCallback = MediaDiffUtilCallback(mediaFilterList, originalMediaList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        mediaFilterList.clear()
        mediaFilterList.addAll(originalMediaList)
        diffResult.dispatchUpdatesTo(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(file: MediaFile) {
        mediaFilterList.remove(file)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = mediaFilterList.size

    inner class PhotosAndVideoViewHolder(
        private val itemBinding: MediaItemLayoutBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(mediaFile: MediaFile, position: Int) {
            itemBinding.file = mediaFile
            itemBinding.run { executePendingBindings() }

            if (mediaFile.urlPath.endsWith(".pdf")) {
                itemBinding.ivFile.setImageResource(R.drawable.pdf_placeholder)
                itemBinding.ivFile.scaleType = ImageView.ScaleType.FIT_XY

            } else if (mediaFile.urlPath.endsWith(".xlsx")) {
                itemBinding.ivFile.setImageResource(R.drawable.xlsx_icon)
                itemBinding.ivFile.scaleType = ImageView.ScaleType.FIT_XY

            } else if (mediaFile.urlPath.endsWith(".txt")) {
                itemBinding.ivFile.setImageResource(R.drawable.txt_icon)
                itemBinding.ivFile.scaleType = ImageView.ScaleType.FIT_XY

            } else if (mediaFile.urlPath.endsWith(".ods")) {
                itemBinding.ivFile.setImageResource(R.drawable.ods_icon)
                itemBinding.ivFile.scaleType = ImageView.ScaleType.FIT_XY

            } else if (mediaFile.urlPath.endsWith(".docx")) {
                itemBinding.ivFile.setImageResource(R.drawable.docx_icon)
                itemBinding.ivFile.scaleType = ImageView.ScaleType.FIT_XY

            } else {
                Glide.with(itemBinding.ivFile.context)
                    .load(mediaFile.urlPath)
                    .into(itemBinding.ivFile)
                itemBinding.ivFile.scaleType = ImageView.ScaleType.CENTER

            }

            itemBinding.ivFile.setOnClickListener {
                onMediaItemClick(mediaFile)

            }

            itemBinding.btnDelete.setOnClickListener {
                onItemDelete(mediaFile)

            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val filterResults = FilterResults()
                if (charSearch.isEmpty()) {
                    filterResults.values = originalMediaList
                } else {
                    filterResults.values = originalMediaList.filter {
                        it.name.lowercase().contains(charSearch.lowercase())

                    }
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                //  mediaFilterList = results?.values as MutableLie (results?.values as MutableList<MediaFile>)
                updatePictureList((results?.values as MutableList<MediaFile>))
            }
        }
    }
}

class MediaDiffUtilCallback(
    private val oldList: List<MediaFile>, private val newList: List<MediaFile>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].urlPath == newList[newItemPosition].urlPath
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}
