package com.omang.app.ui.survey.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.databinding.ItemQuestionAnsweredWithEditBinding
import com.omang.app.ui.test.models.Question


class SurveySubmitListAdapter(
    private val context: Context,
    private val isTestCompleted: Boolean,
    private var questionList: List<Question> = ArrayList(),
    private val isViewTest: Boolean
) : RecyclerView.Adapter<SurveySubmitListAdapter.ViewHolder?>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionAnsweredWithEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return questionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = questionList[position]
        holder.bind(question)
        holder.binding.apply {
            txtQuestionNumber.text = "${position + 1}"
            ivEdit.visibility = if (isTestCompleted) {
                View.GONE
            } else View.VISIBLE

        }


        /*if (questionList[position].getStudent_answer().isEmpty()) {
            holder.txt_anser_status_label.setVisibility(View.VISIBLE)
            holder.iv_answer.setVisibility(View.GONE)
            holder.txt_anser_status_label.setText("Not Attempted")
        } else if (questionList[position].getTypes()
                .equals(Constants.TestOptionType.TEXT.toString())
        ) {
            holder.txt_anser_status_label.setVisibility(View.VISIBLE)
            holder.iv_answer.setVisibility(View.GONE)
            holder.txt_anser_status_label.setText(questionList[position].getStudent_answer())
        } else {
            holder.txt_anser_status_label.setVisibility(View.GONE)
            holder.iv_answer.setVisibility(View.VISIBLE)
            var path = ""
            var url = ""
            for (answerOption in questionList[position].getAnswerOptions()) {
                if (answerOption.getAnswer().equals(questionList[position].getStudent_answer())) {
                    path = answerOption.getAnswerUrl()
                    url = answerOption.getAnswer()
                    break
                }
            }
            ViewUtil.loadImage(
                path,
                url,
                context,
                R.drawable.error_imageview,
                R.drawable.error_imageview,
                holder.iv_answer
            )*/
    }


    inner class ViewHolder(val binding: ItemQuestionAnsweredWithEditBinding) :
        RecyclerView.ViewHolder(binding.root) {


        init {
            /*    iv_edit.setOnClickListener {
                    Editable_Question_Position = getAdapterPosition()
                    if (isViewTest) {
                        val navController: NavController =
                            findNavController(context as Activity, R.id.nav_host_fragment)
                        navController.navigate(R.id.action_submit_to_question)
                    } else {
                        val navController: NavController =
                            findNavController(context as Activity, R.id.nav_host_fragment)
                        navController.navigate(R.id.action_survey_submit_to_question)
                    }
                }*/
        }

        fun bind(obj: Question) {}
    }
}