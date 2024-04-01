package com.omang.app.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.data.model.modeMeter.ModeMeterEntity
import com.omang.app.databinding.ItemEmojisBinding
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.loadLocalEmoji
import com.omang.app.utils.extensions.setSafeOnClickListener

class ModeMeterAdapter(
    private var adapterList: List<ModeMeterEntity>,
    private val onModeItemClick: (ModeMeterEntity) -> Unit,
) : RecyclerView.Adapter<ModeMeterAdapter.ModeMeterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeMeterViewHolder {
        val binding = ItemEmojisBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModeMeterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModeMeterViewHolder, position: Int) {
        val mediaItem = adapterList[position]
        holder.bindItem(mediaItem)
    }

    override fun getItemCount(): Int = adapterList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<ModeMeterEntity>) {
        adapterList = (newData)
        notifyDataSetChanged()
    }

    inner class ModeMeterViewHolder(
        private val itemBinding: ItemEmojisBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(mode: ModeMeterEntity) {

            itemBinding.ivIcon.setSafeOnClickListener {
                onModeItemClick(mode)
            }
            if (ValidationUtil.isNotNullOrEmpty(mode.emoji)) {
                itemBinding.ivIcon.loadLocalEmoji(mode.emoji!!)
            }

            itemBinding.moodeMeter = mode
            itemBinding.executePendingBindings()
        }
    }
}

