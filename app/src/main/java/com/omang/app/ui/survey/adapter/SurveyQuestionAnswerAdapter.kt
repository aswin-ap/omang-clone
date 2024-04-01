package com.omang.app.ui.survey.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.BR
import com.omang.app.R
import com.omang.app.databinding.ItemQuestionAmsweredBinding
import com.omang.app.ui.test.models.Question

class SurveyQuestionAnswerAdapter(private val questions: List<Question> = ArrayList()) :
    RecyclerView.Adapter<SurveyQuestionAnswerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SurveyQuestionAnswerAdapter.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemQuestionAmsweredBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_question_amswered, parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = questions[position]
        holder.bind(question)
        holder.binding.apply {
            if (question.answer!!.isEmpty()) {
                txtAnswerStatusLabel.visibility = View.VISIBLE
                ivAnswer.visibility = View.GONE
                txtAnswerStatusLabel.text = "Not Attempted"
            } else {
                txtAnswerStatusLabel.visibility = View.VISIBLE
                ivAnswer.visibility = View.GONE
                txtAnswerStatusLabel.text = question.answer
            }
            txtQuestionNumber.text = "${position + 1}"
        }


    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(val binding: ItemQuestionAmsweredBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(obj: Question) {
            binding.setVariable(BR.question, obj)
            binding.executePendingBindings()
        }


    }
}