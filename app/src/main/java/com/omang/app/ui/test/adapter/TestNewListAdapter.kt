package com.omang.app.ui.test.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.BR
import com.omang.app.R
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.ItemsTestNewBinding
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertLocaleTimestampToLocale
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.timeDifference
import com.omang.app.utils.extensions.timeInSpecificFormat
import com.omang.app.utils.extensions.toDate
import com.omang.app.utils.extensions.visible
import java.util.Locale

class TestNewListAdapter(
    val context: Context,
    newTests: List<TestEntity>,
    val onItemClick: (Int) -> Unit,
) : RecyclerView.Adapter<TestNewListAdapter.ViewHolder>(), Filterable {
    private var newTests: List<TestEntity> = ArrayList()
    private var filterNewTests: List<TestEntity> = ArrayList()


    init {
        this.newTests = newTests
        filterNewTests = newTests
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemsTestNewBinding>(
            LayoutInflater.from(parent.context),
            R.layout.items_test_new, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val test = filterNewTests[position]
        holder.bind(test)
        /*holder.binding.apply {
            txtMonth.text = test.startTime.toDate("MMM")
            txtMin.text = test.startTime timeDifference test.endTime
            txtMin.text = test.startTime timeDifference test.endTime
            val startTime: String =
                filterNewTests[position].startTime timeInSpecificFormat "hh:mm a"
            val endTime: String =
                filterNewTests[position].endTime timeInSpecificFormat "hh:mm a"
            //holder.itemRowBinding.txt_time.setText("$startTime - $endTime")
        }*/
    }

    override fun getItemCount(): Int {
        return filterNewTests.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charstring = charSequence.toString()
                filterNewTests = if (charstring.isEmpty()) {
                    newTests
                } else {
                    val newTest: MutableList<TestEntity> = ArrayList()
                    for (item in newTests) {
                        if (item.name.lowercase(Locale.getDefault())
                                .contains(charstring.lowercase(Locale.getDefault()))
                        ) {
                            newTest.add(item)
                        }
                    }
                    newTest
                }
                val filterResults = FilterResults()
                filterResults.values = filterNewTests
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterNewTests = filterResults.values as ArrayList<TestEntity>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(var binding: ItemsTestNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(obj: TestEntity?) {
            binding.setVariable(BR.newTest, obj)
            binding.executePendingBindings()
        }

        init {
            binding.cvNewTest.setOnClickListener {
                // onItemClick(filterNewTests[absoluteAdapterPosition].id)
                if (ViewUtil.isTimeBefore(filterNewTests[absoluteAdapterPosition].startTime)) {
                    context.showToast("Test not yet started")
                } else {
                    filterNewTests[absoluteAdapterPosition].generalMcqId?.let { it1 ->
                        onItemClick(
                            it1
                        )
                    }
                }
            }
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

@BindingAdapter("start_time", "end_time")
fun setTime(view: TextView, startTime: String, endTime: String) {
    val startTime = convertLocaleTimestampToLocale(startTime, DateTimeFormat.TIME_N_AM_PM)
    val endTime =convertLocaleTimestampToLocale(endTime, DateTimeFormat.TIME_N_AM_PM)
    view.text = "$startTime - $endTime"
}

@BindingAdapter("start_time")
fun setTestDay(view: TextView, startTime: String) {
    val day = startTime.toDate("dd")
    view.text = day
}

@BindingAdapter("end_time")
fun setTestMonth(view: TextView, startTime: String) {
    val month = startTime.toDate("MMM")
    view.text = month
}

@BindingAdapter("test_start_time", "test_end_time")
fun setTestTime(view: TextView, startTime: String, endTime: String) {
    val testTotalTime = startTime.timeDifference(endTime)
    view.text = testTotalTime
}