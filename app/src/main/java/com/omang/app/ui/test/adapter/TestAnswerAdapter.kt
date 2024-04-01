package com.omang.app.ui.test.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omang.app.R
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.model.resources.OptionItem
import com.omang.app.databinding.ItemImageAnswersBinding
import com.omang.app.databinding.ItemNormalQuestionBinding
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.loadLocalImage
import com.omang.app.utils.extensions.visible

class TestAnswerAdapter(
    val context: Context,
    private val questionEntity: QuestionEntity,
    private val onItemClick: (questionId: Int, selectedOptionId: Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // private var selectedItemPos = -1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val optionItem = questionEntity.options[position]
        /* if (holder.itemViewType == IMAGE_TYPE) {
            (holder as ImageTestViewHolder).bind(questionEntity)
        } else {
            (holder as TestViewHolder).apply {
                bind(optionItem, position)
                binding.apply {
                    mainCv.setOnClickListener {
                        selectedItemPos = absoluteAdapterPosition
                        notifyDataSetChanged()
                        onItemClick(questionEntity.id, optionItem.id)
                    }
                }
            }
        }*/
        (holder as TestViewHolder).apply {
            bind(optionItem)
            binding.apply {
                mainCv.setOnClickListener {
                    //   selectedItemPos = absoluteAdapterPosition
                    updateSelection(
                        optionItem.isSelected,
                        questionEntity.isMultiQuestion,
                        absoluteAdapterPosition
                    )
                    onItemClick(questionEntity.id, optionItem.id)
                }
            }
        }
    }

    private fun updateSelection(
        isSelected: Boolean,
        multiQuestion: Boolean,
        absoluteAdapterPosition: Int
    ) {
        if (multiQuestion) {
            questionEntity.options[absoluteAdapterPosition].isSelected = !isSelected
            notifyItemChanged(absoluteAdapterPosition)
        } else {
            questionEntity.options.forEachIndexed { pos, item ->
                item.isSelected = false
                if (pos == absoluteAdapterPosition)
                    item.isSelected = true
            }
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        /*   return if (viewType == IMAGE_TYPE) {
               val binding = DataBindingUtil.inflate<ItemImageAnswersBinding>(
                   LayoutInflater.from(parent.context),
                   R.layout.item_image_answers, parent, false
               )
               ImageTestViewHolder(binding)
           } else {
               val binding = ItemNormalQuestionBinding.inflate(
                   LayoutInflater.from(parent.context),
                   parent, false
               )
               return TestViewHolder(binding)
           }*/
        val binding = ItemNormalQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return TestViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return questionEntity.options.size
    }

    /*override fun getItemViewType(position: Int): Int {
        return when (questionEntity.questionType) {
            *//*0 -> TEXT_TYPE
            1 -> IMAGE_TYPE
            else -> position*//*
            2 -> TEXT_TYPE
            1 -> IMAGE_TYPE
            else -> position
        }
    }*/

    inner class TestViewHolder(val binding: ItemNormalQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(optionItem: OptionItem) {
            binding.apply {
                optionItem.apply {
                    if (ValidationUtil.isNotNullOrEmpty(option)) {
                        txtAnswer.text = option
                        txtAnswer.visible()
                    }
                    if (ValidationUtil.isNotNullOrEmpty(optionUrl)) {
                        ivQuestion.loadLocalImage(optionUrl!!)
                    }
                    optionAnswer.isChecked = isSelected
                }
            }
        }
    }

    inner class ImageTestViewHolder(val binding: ItemImageAnswersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(answerOption: QuestionEntity) {
            /* binding.answerOption =
             binding.executePendingBindings()*/
        }
    }

    companion object {
        private const val IMAGE_TYPE = 0
        private const val TEXT_TYPE = 1
    }
}