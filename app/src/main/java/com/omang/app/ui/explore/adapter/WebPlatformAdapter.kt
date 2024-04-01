package com.omang.app.ui.explore.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omang.app.R
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.databinding.WebplatformItemLayoutBinding
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.showSnackBar
import okhttp3.internal.lowercase
import timber.log.Timber
import java.util.Locale


enum class WebPlatformSort { ASCENDING_NAME, DESCENDING_NAME, ASCENDING_DATE, DESCENDING_DATE, TIME_SORT }
class WebPlatformAdapter(
    private var filterList: List<MyWebPlatformEntity>,
    private val onItemClick: (Int) -> Unit,
    private val favClick: (Int, Int) -> Unit,
) : RecyclerView.Adapter<WebPlatformAdapter.WebPlatformViewHolder>(), Filterable {

    private var adapterList: MutableList<MyWebPlatformEntity> = ArrayList()
    private var favButton = false

    init {
        adapterList = filterList.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebPlatformViewHolder {
        val binding =
            WebplatformItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WebPlatformViewHolder(binding)
    }

    override fun getItemCount(): Int = adapterList.size

    override fun onBindViewHolder(holder: WebPlatformViewHolder, position: Int) {
        val subjectItem = adapterList[position]
        holder.bindItem(subjectItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(dataList: List<MyWebPlatformEntity>) {
        adapterList.clear()
        adapterList.addAll(dataList)
        filterList = adapterList.toList()
        notifyDataSetChanged()
    }

    fun hideFavButton() {
        favButton = true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sort(sortOrder: WebPlatformSort) {
        when (sortOrder) {
            WebPlatformSort.ASCENDING_NAME -> {
                adapterList.sortBy {
                    it.name.lowercase(Locale.ROOT)
                }
            }

            WebPlatformSort.DESCENDING_NAME -> {
                adapterList.sortByDescending {
                    it.name.lowercase(Locale.ROOT)
                }
            }

            WebPlatformSort.TIME_SORT -> {
                adapterList.sortByDescending {
                    it.timeStamp
                }
            }

            WebPlatformSort.ASCENDING_DATE -> {
                //TODO : sort
            }

            WebPlatformSort.DESCENDING_DATE -> {
                //TODO: sort
            }
        }
        notifyDataSetChanged()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun bindViewHolderBasedOnInternetConnection(hasInternet: Boolean) {
        filterList.forEach {
            it.hasInternetConnection = hasInternet
        }
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                Timber.d("Filter", "performFiltering: $charSequence")
                val charString = charSequence.toString()
                val filteredList = if (charString.isEmpty()) {
                    filterList.toMutableList()
                } else {
                    val filterDocs: MutableList<MyWebPlatformEntity> = ArrayList()
                    for (item in filterList) {
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
                adapterList = filterResults.values as MutableList<MyWebPlatformEntity>
                notifyDataSetChanged()
            }
        }
    }

    inner class WebPlatformViewHolder(
        private val itemBinding: WebplatformItemLayoutBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("ResourceAsColor")
        fun bindItem(webPlatformItem: MyWebPlatformEntity) {
            itemBinding.cardView.apply {

                if (favButton) {
                    itemBinding.ivFavourite.gone()
                } else {
                    if (webPlatformItem.isFav == 1) {
                        itemBinding.ivFavourite.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.context,
                                R.drawable.ic_favorite
                            )
                        )
                    } else {
                        itemBinding.ivFavourite.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.context,
                                R.drawable.ic_star
                            )
                        )
                    }
                }

                alpha = if (webPlatformItem.hasInternetConnection) {
                    1.0f
                } else {
                    0.4f
                }
            }
            itemBinding.ivWebPlatform.load(
                webPlatformItem.logo
            ) {
                crossfade(true)
            }
            itemBinding.webplatform = webPlatformItem
            itemBinding.executePendingBindings()

            itemBinding.llImg.setSafeOnClickListener {
                if (webPlatformItem.hasInternetConnection)
                    onItemClick(webPlatformItem.id)
                else
                    itemView.showSnackBar("Poor Internet connection!")
            }
            itemBinding.ivFavourite.setSafeOnClickListener {
                favClick(webPlatformItem.id, webPlatformItem.isFav)
            }
        }
    }
}
