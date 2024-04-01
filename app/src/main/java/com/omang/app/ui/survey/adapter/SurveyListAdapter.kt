package com.omang.app.ui.survey.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.BR
import com.omang.app.R
import com.omang.app.databinding.ItemsSurveyNewBinding
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.toDate
import com.omang.app.utils.extensions.visible
import com.omang.app.ui.survey.models.Survey

class SurveyListAdapter(newSurveyList: List<Survey>) :
    RecyclerView.Adapter<SurveyListAdapter.ViewHolder>() {
    private var newSurveyList: List<Survey> = ArrayList()

    init {
        this.newSurveyList = newSurveyList

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemsSurveyNewBinding>(
            LayoutInflater.from(parent.context),
            R.layout.items_survey_new, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val survey = newSurveyList[position]
        holder.bind(survey)
        holder.binding.apply {
            txtMonth.text = survey.starttime.toDate("MMM")
            txtDate.text = survey.starttime.toDate("dd")

        }
    }

    override fun getItemCount(): Int {
        return newSurveyList.size
    }


    inner class ViewHolder(var binding: ItemsSurveyNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(obj: Survey?) {
            binding.setVariable(BR.survey, obj)
            binding.executePendingBindings()
        }


    }
}

@BindingAdapter("start_time", "end_time")
fun setVisibility(view: View, startTime: String, endTime: String) {
    if (startTime == "" || endTime == "") {
        view.gone()
    } else {
        view.visible()
    }
}