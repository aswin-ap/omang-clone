package com.omang.app.ui.myClassroom.fragments.subjectContent.liveClassRoom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.databinding.ItemLiveClassRoomLayoutBinding
import com.omang.app.ui.myClassroom.fragments.subjectContent.liveClassRoom.model.LiveClassRoomEntity
import java.util.Locale

enum class LiveClassRoomSort { ASCENDING_NAME, DESCENDING_NAME, ASCENDING_DATE, DESCENDING_DATE }
class LiveClassRoomAdapter(
    private var webPlatformList: List<LiveClassRoomEntity>,
    private val onItemClick: (Int) -> Unit,
    private val favClick: (Int) -> Unit,
) : RecyclerView.Adapter<LiveClassRoomAdapter.LiveClassRoomViewHolder>(), Filterable {
    private var filterWebPlatforms: MutableList<LiveClassRoomEntity> = ArrayList()

    init {
        filterWebPlatforms = webPlatformList.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveClassRoomViewHolder {
        val binding =
            ItemLiveClassRoomLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return LiveClassRoomViewHolder(binding)
    }

    override fun getItemCount(): Int = filterWebPlatforms.size

    override fun onBindViewHolder(holder: LiveClassRoomViewHolder, position: Int) {
        val subjectItem = filterWebPlatforms[position]
        holder.bindItem(subjectItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun classify(s: String) {
        filterWebPlatforms = if (s.isEmpty()) {
            webPlatformList.toMutableList()
        } else {
            val filterDocs: MutableList<LiveClassRoomEntity> = ArrayList()
            for (item in webPlatformList) {
                if (item.status?.lowercase(Locale.getDefault())
                        ?.contains(s.lowercase(Locale.getDefault())) == true
                ) {
                    filterDocs.add(item)
                }
            }
            filterDocs
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(dataList: List<LiveClassRoomEntity>) {
        filterWebPlatforms.clear()
        filterWebPlatforms.addAll(dataList)
        webPlatformList = filterWebPlatforms.toList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sort(sortOrder: LiveClassRoomSort) {
        when (sortOrder) {
            LiveClassRoomSort.ASCENDING_NAME -> {
                filterWebPlatforms.sortBy {
                    it.name.lowercase(Locale.ROOT)
                }
            }

            LiveClassRoomSort.DESCENDING_NAME -> {
                filterWebPlatforms.sortByDescending {
                    it.name.lowercase(Locale.ROOT)
                }
            }

            LiveClassRoomSort.ASCENDING_DATE -> {
                //TODO : sort
            }

            LiveClassRoomSort.DESCENDING_DATE -> {
                //TODO: sort
            }
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun bindViewHolderBasedOnInternetConnection(hasInternet: Boolean) {
        webPlatformList.forEach {
            it.hasInternetConnection = hasInternet
        }
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filteredList = if (charString.isEmpty()) {
                    webPlatformList.toMutableList()
                } else {
                    val filterDocs: MutableList<LiveClassRoomEntity> = ArrayList()
                    for (item in webPlatformList) {
                        if (item.name.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filterDocs.add(item)
                        }
                    }
                    filterDocs
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterWebPlatforms = filterResults.values as MutableList<LiveClassRoomEntity>
                notifyDataSetChanged()
            }
        }
    }


    inner class LiveClassRoomViewHolder(
        private val itemBinding: ItemLiveClassRoomLayoutBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("ResourceAsColor")
        fun bindItem(webPlatformItem: LiveClassRoomEntity) {
            itemBinding.cardView.apply {
                if (webPlatformItem.hasInternetConnection) {
                    alpha = 1.0f
                } else {
                    alpha = 0.4f
                }
            }
            itemBinding.liveClassRoom = webPlatformItem
            itemBinding.executePendingBindings()

            itemBinding.ivWebPlatform.setOnClickListener {
                if (webPlatformItem.hasInternetConnection)
                    onItemClick(webPlatformItem.id)
                // else
                // itemView.showSnackBar("Poor Internet connection!")
            }
        }
    }
}