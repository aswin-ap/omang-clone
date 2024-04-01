package com.omang.app.ui.techSupport.ticketList.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.R
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity
import com.omang.app.databinding.ItemTicketsBinding
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertLocaleTimestampToLocale
import com.omang.app.utils.extensions.convertTimestampToLocale
import timber.log.Timber

class TicketAdapter(
    private val ticketsEntities: List<TicketsEntity>,
    private val onItemClick: (TicketsEntity) -> Unit,
    private val onChatClicked: (Int) -> Unit,
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(
        val itemBinding: ItemTicketsBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItem(ticketsEntity: TicketsEntity) {
            itemBinding.tickets = ticketsEntity
            itemBinding.executePendingBindings()
            itemView.setOnClickListener { onItemClick(ticketsEntity) }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = DataBindingUtil.inflate<ItemTicketsBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_tickets, parent, false
        )
        return TicketViewHolder(binding)
    }

    override fun getItemCount(): Int = ticketsEntities.size

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bindItem(ticketsEntities[position])
        holder.itemBinding.llBtChat.setOnClickListener {
            onChatClicked(ticketsEntities[position].roomId)
        }

        if (ticketsEntities[position].isClosed) {
            holder.itemBinding.ivIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_closed
                )
            )
        } else {
            holder.itemBinding.ivIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_open
                )
            )
        }

        holder.itemBinding.ivDotNotification.visibility =
            if (ticketsEntities[position].unreadMessages > 0) View.VISIBLE else View.INVISIBLE

        Timber.e("ticket ${ticketsEntities[position].issue}")

        holder.itemBinding.tvRaised.text = (if (ticketsEntities[position].isClosed) {
            Timber.e("1")
            "Closed on :${
                ticketsEntities[position].closedAt?.let {
                    convertTimestampToLocale(
                        it,
                        DateTimeFormat.TIME_N_DATE
                    )
                }
            }"
        } else {
            if (ticketsEntities[position].reopenedAt?.isEmpty() != true) {
                Timber.e("2")
                "Raised on :${
                    convertTimestampToLocale(
                        ticketsEntities[position].createdAt,
                        DateTimeFormat.TIME_N_DATE
                    )
                }"
            } else {
                Timber.e("3")
                if (ticketsEntities[position].createdAt.isNotEmpty()) {
                    "Raised on :${
                        convertLocaleTimestampToLocale(
                            ticketsEntities[position].createdAt,
                            DateTimeFormat.TIME_N_DATE
                        )
                    }"
                } else {
                    "Closed on :${
                        ticketsEntities[position].closedAt?.let {
                            convertTimestampToLocale(
                                it,
                                DateTimeFormat.TIME_N_DATE
                            )
                        }
                    }"
                }
            }
        }).toString()

        holder.itemBinding.tvReopen.visibility =
            if (ticketsEntities[position].reopenedAt?.isEmpty() == true) {
                View.GONE
            } else {
                holder.itemBinding.tvReopen.text = ticketsEntities[position].reopenedAt?.let {
                    " Reopened on :${
                        convertTimestampToLocale(
                            it,
                            DateTimeFormat.TIME_N_DATE
                        )
                    }"
                }
                View.VISIBLE
            }

        holder.itemBinding.llRating.visibility = if (ticketsEntities[position].rating != null) {
            holder.itemBinding.ratingBar.rating = ticketsEntities[position].rating?.toFloat() ?: .0f
            holder.itemBinding.tvRating.text = " ${ticketsEntities[position].rating} of 5"
            View.VISIBLE

        } else {
            View.GONE

        }
    }
}