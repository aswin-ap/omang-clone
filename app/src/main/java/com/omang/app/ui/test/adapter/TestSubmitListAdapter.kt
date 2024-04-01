package com.omang.app.ui.test.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.R
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.databinding.ItemQuestionAnsweredWithEditBinding
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadLocalImage
import com.omang.app.utils.extensions.visible

class TestSubmitListAdapter(
    private val attempted: MutableList<QuestionEntity>,
    private val onEditClick: (position: Int) -> Unit,
    private val isDetailsFragment: Boolean = false
) :
    RecyclerView.Adapter<TestSubmitListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionAnsweredWithEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val testResult = attempted[position].also {
            it.questionNUmber = position + 1
        }
        holder.bind(testResult)
        holder.binding.ivEdit.setOnClickListener {
            onEditClick(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(questionList: List<QuestionEntity>) {
        attempted.clear()
        attempted.addAll(questionList)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return attempted.size
    }

    inner class ViewHolder(val binding: ItemQuestionAnsweredWithEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(obj: QuestionEntity?) {
            binding.apply {
                obj?.let { questionEntity ->
                    ivEdit.visibility = if (isDetailsFragment) View.GONE else View.VISIBLE
                    //set questions
                    txtQuestionNumber.text = questionEntity.questionNUmber.toString()
                    if (ValidationUtil.isNotNullOrEmpty(questionEntity.question)) {
                        tvQuestion.text = questionEntity.question
                        tvQuestion.visible()
                    }
                    if (ValidationUtil.isNotNullOrEmpty(questionEntity.questionUrl)) {
                        ivQuestion.loadLocalImage(questionEntity.questionUrl!!)
                    }
                    //set answer
                    if (questionEntity.isMultiQuestion) {
                        if (questionEntity.selectedOptions.isNotEmpty()) {
                            rvAnswer.apply {
                                layoutManager =
                                    LinearLayoutManager(
                                        context,
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                    )
                                adapter = TestImageSelectedAdapter(questionEntity)
                            }
                        } else
                            bindNoQuestionsAttempted()
                    } else {
                        rvAnswer.gone()
                        questionEntity.options.find { it.isSelected }.also { optionItem ->
                            if (ValidationUtil.isNotNullOrEmpty(optionItem?.option)) {
                                tvAnswer.text = optionItem?.option
                                lytTvAnswer.visible()
                            }
                            if (ValidationUtil.isNotNullOrEmpty(optionItem?.optionUrl)) {
                                ivAnswer.loadLocalImage(optionItem?.optionUrl!!)
                                lytIvAnswer.visible()
                            }
                        } ?: kotlin.run {
                            bindNoQuestionsAttempted()
                        }
                    }
                }
            }
        }

        fun bindNoQuestionsAttempted() {
            binding.apply {
                tvAnswer.text = root.context.getString(R.string.not_attempted)
                lytTvAnswer.visible()
            }
        }
    }
}