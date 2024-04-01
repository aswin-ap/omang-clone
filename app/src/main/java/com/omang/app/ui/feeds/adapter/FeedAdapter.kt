package com.omang.app.ui.feeds.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omang.app.R
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.databinding.ItemFeedBinding
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.timeDifferenceInDays
import com.omang.app.utils.extensions.visible

class FeedAdapter(
    private val notificationEntities: MutableList<FeedEntity>,
    private val onItemClick: (feedType: Int?, classroomId: Int?) -> Unit,
) : RecyclerView.Adapter<FeedAdapter.NotificationViewHolder>() {

    private var feedList = mutableListOf<FeedEntity>()

    inner class NotificationViewHolder(
        private val itemBinding: ItemFeedBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItem(notificationEntity: FeedEntity) {
            itemView.setSafeOnClickListener {
                onItemClick(
                    notificationEntity.feedType,
                    notificationEntity.classroomDetails?.classRoomId
                )
            }
            itemBinding.apply {
                notificationEntity.apply {
                    tvTitle.text = title
                    tvBody.text = description
                    imageUrl?.let {
                        ivNotification.load(it) {
                            crossfade(true)
                        }
                        ivNotification.visible()
                    } ?: kotlin.run {
                        ivNotification.gone()
                    }
                    createdAt?.let {
                        tvDate.text = it timeDifferenceInDays (ViewUtil.getUtcTime())
                    }
                    createdBy?.let { createdByEntity ->
                        createdByEntity.avatar?.let {
                            ivAvatar.load(it)
                        }
                        createdByEntity.firstName?.let {
                            tvUser.text = "$it ${createdByEntity.lastName}"
                        }
                    } ?: run {
                        ivAvatar.load(R.drawable.home_logo)
                        tvUser.text = "Jendamark"
                    }
                    postedTo?.let { postedTo ->
                        classroomDetails?.let { classRoom ->
                            tvPostDetails.text =
                                "Posted to : $postedTo | Classroom : ${classRoom.classRoomOrClub}"
                        } ?: kotlin.run {
                            tvPostDetails.text = "Posted to : $postedTo"
                        }
                        tvPostDetails.visible()
                    } ?: run {
                        tvPostDetails.gone()
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun initData(feedEntities: List<FeedEntity>?) {
        if (feedEntities != null) {
            feedList.clear()
            feedList.addAll(feedEntities)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        /*      val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context))
              return NotificationViewHolder(binding)
      */
        val binding = ItemFeedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int = notificationEntities.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bindItem(notificationEntities[position])
    }

    fun updateFeedsList(feedList: List<FeedEntity>) {
        notificationEntities.clear()
        notificationEntities.addAll(feedList)
        notifyDataSetChanged()
    }

}