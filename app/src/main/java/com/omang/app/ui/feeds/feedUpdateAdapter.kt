package com.omang.app.ui.feeds


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.databinding.ItemFeedBinding
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.justReceive
import com.omang.app.utils.extensions.timeDifferenceInDays
import com.omang.app.utils.extensions.visible

class feedUpdateAdapte(
    private val listener: OnClick

) : ListAdapter<FeedEntity, RecyclerView.ViewHolder>(DiffCallback) {

    interface OnClick {
        fun itemClick(feedEntity: FeedEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as NotificationViewHolder).bind(getItem(position))

    }

    override fun onCurrentListChanged(
        previousList: MutableList<FeedEntity>,
        currentList: MutableList<FeedEntity>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        if (currentList.size > previousList.size) {
        }
    }

    private inner class NotificationViewHolder(private val itemBinding: ItemFeedBinding) :
        RecyclerView.ViewHolder(
            itemBinding.root
        ) {
        fun bind(notificationEntity: FeedEntity) {
            itemBinding.root.setOnClickListener {
                listener.itemClick(notificationEntity)
            }
            itemBinding.apply {
                notificationEntity.apply {
                    tvTitle.text = title
                    tvBody.text = description
                    imageUrl?.let {
                        ivNotification.load(it)
                        ivNotification.visible()
                    } ?: kotlin.run {
                        ivNotification.gone()
                    }
                    createdAt?.let {
                        tvDate.text = it timeDifferenceInDays ViewUtil.getUtcTime()

                        val returnValue = "asa" justReceive 10

                    }



                    createdBy?.let { createdByEntity ->
                        createdByEntity.avatar?.let {
                            ivAvatar.load(it)
                        }
                        createdByEntity.firstName?.let {
                            tvUser.text = "$it ${createdByEntity.lastName}"
                        }
                    }
                }
            }
        }
    }


}

object DiffCallback : DiffUtil.ItemCallback<FeedEntity>() {
    override fun areItemsTheSame(
        oldItem: FeedEntity,
        newItem: FeedEntity,
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: FeedEntity,
        newItem: FeedEntity,
    ): Boolean {
        return oldItem.id == newItem.id
    }
}
