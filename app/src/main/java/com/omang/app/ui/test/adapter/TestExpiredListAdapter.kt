package com.omang.app.ui.test.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.BR
import com.omang.app.R
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.ItemsTestExpiredBinding
import com.omang.app.utils.extensions.toDate

class TestExpiredListAdapter(private val expired: List<TestEntity>) :
    RecyclerView.Adapter<TestExpiredListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemsTestExpiredBinding>(
            LayoutInflater.from(parent.context),
            R.layout.items_test_expired, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expired = expired[position]
        holder.bind(expired)
    }

    override fun getItemCount(): Int {
        return expired.size
    }

    inner class ViewHolder(val binding: ItemsTestExpiredBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(obj: TestEntity) {
            binding.tvExpiredOn.text = obj.createdOn.toDate("dd MMMM YYYY")
            binding.setVariable(BR.expired, obj)
            binding.executePendingBindings()
        }
    }
}