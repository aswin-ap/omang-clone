package com.omang.app.ui.test.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.ItemAttemptedTestBinding
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.toDate
import kotlin.math.roundToInt

class TestAttemptedListAdapter(
    private val attempted: List<TestEntity>,
    private val onItemClick: (testId: Int,testName: String) -> Unit
) : RecyclerView.Adapter<TestAttemptedListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttemptedTestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val testResult = attempted[position]
        holder.bind(testResult)
    }

    override fun getItemCount(): Int {
        return attempted.size
    }

    inner class ViewHolder(val binding: ItemAttemptedTestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(obj: TestEntity) {
            binding.apply {
                var time = obj.startTime.toDate("dd MMMM YYYY")
                if (!ValidationUtil.isNotNullOrEmpty(time))
                    time = obj.createdOn.toDate("dd MMMM YYYY")
                txtTitle.text = time
                txtTestName.text = obj.name
                txtObtained.text = obj.percentage.roundToInt().toString()
                txtTotalQuestion.text = obj.questionsId.size.toString()
                txtAttempted.text = obj.attemptsCount.toString()
                txtCorrect.text = obj.correctAttempts.toString()
                txtWrong.text = obj.wrongAttempts.toString()
            }

            binding.cvMain.setOnClickListener {
                onItemClick(obj.generalMcqId ?: 0,obj.name)
            }
        }
    }
}