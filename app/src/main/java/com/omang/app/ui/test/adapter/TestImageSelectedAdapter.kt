package com.omang.app.ui.test.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.model.resources.OptionItem
import com.omang.app.databinding.ItemImageAnswerBinding
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.loadLocalImage
import com.omang.app.utils.extensions.visible

class TestImageSelectedAdapter(
    private val questionEntity: QuestionEntity,
) : RecyclerView.Adapter<TestImageSelectedAdapter.ImageSelectViewHolder>() {
    override fun onBindViewHolder(holder: ImageSelectViewHolder, position: Int) {
        val optionItem = questionEntity.selectedOptions[position]
        holder.bind(optionItem)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSelectViewHolder {
        val binding = ItemImageAnswerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ImageSelectViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return questionEntity.selectedOptions.size
    }

    inner class ImageSelectViewHolder(val binding: ItemImageAnswerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(optionItem: OptionItem) {
            binding.apply {
                if (ValidationUtil.isNotNullOrEmpty(optionItem.option)) {
                    tvAnswer.text = optionItem.option
                    lytTvAnswer.visible()
                }
                if (ValidationUtil.isNotNullOrEmpty(optionItem.optionUrl)) {
                    ivAnswer.loadLocalImage(optionItem.optionUrl!!)
                    lytIvAnswer.visible()
                }
            }
        }
    }
}